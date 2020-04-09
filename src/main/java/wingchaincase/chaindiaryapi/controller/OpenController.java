package wingchaincase.chaindiaryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import wingchaincase.chaindiaryapi.domain.NoteSeed;
import wingchaincase.chaindiaryapi.domain.vo.CallRequestVO;
import wingchaincase.chaindiaryapi.domain.vo.InitRequestVO;
import wingchaincase.chaindiaryapi.domain.vo.InitResponseVO;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.exception.BaseInternalServerErrorException;
import wingchaincase.chaindiaryapi.service.BlackholeService;
import wingchaincase.chaindiaryapi.service.EcService;
import wingchaincase.chaindiaryapi.service.NoteService;
import wingchaincase.chaindiaryapi.service.remoting.WeixinService;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/open")
public class OpenController {

    private final static Logger logger = LoggerFactory.getLogger(OpenController.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private BlackholeService blackholeService;

    @ApiOperation("初始化")
    @RequestMapping(value = {"/init"}, method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public InitResponseVO init(@RequestBody InitRequestVO requestVO) throws BaseBadRequestException, BaseInternalServerErrorException {

        String weixinUnionId = getWeixinUnionId(requestVO);

        NoteSeed noteSeed = noteService.noteSeedRepository.findByWeixinUnionId(weixinUnionId);

        if (noteSeed == null) {
            byte[] entropy = noteService.generateEntropy();
            String seedHex = Hex.encodeHexString(entropy);
            String seedEncHex = blackholeService.encryptSafe(seedHex);

            noteSeed = new NoteSeed();
            noteSeed.setWeixinUnionId(weixinUnionId);
            noteSeed.setSeed(seedEncHex);

            noteService.noteSeedRepository.save(noteSeed);
        }

        String seedEncHex = noteSeed.getSeed();
        String seedHex = blackholeService.decryptSafe(seedEncHex);
        seedEncHex = EcService.eciesEncrypt(requestVO.getSeedPublicKeyHex(), seedHex);

        InitResponseVO responseVO = new InitResponseVO();
        responseVO.setSeedEncHex(seedEncHex);

        return responseVO;

    }

    @ApiOperation("执行操作")
    @RequestMapping(value = {"/call"}, method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Object call(@RequestBody CallRequestVO requestVO) throws BaseBadRequestException, BaseInternalServerErrorException {

        String txHex = requestVO.getTxHex();

        Map<String, String> tx = noteService.extractTx(txHex);

        String methodHex = tx.get("methodHex");
        String paramsHex = tx.get("paramsHex");

        Map<String, String> params = noteService.extractParams(methodHex, paramsHex);

        String publicKeyHex = tx.get("publicKeyHex");
        String signatureHex = tx.get("signatureHex");

        String messageHex = methodHex + paramsHex;
        Boolean verified = EcService.ecdsaVerifySafe(publicKeyHex, messageHex, signatureHex);
        logger.info("Signature verified: {}", verified);
        if (!verified) {
            throw new BaseBadRequestException("Invalid signature");
        }

        String address = noteService.getAddress(publicKeyHex);

        String blobHex = requestVO.getBlobHex();

        if (methodHex.equals(NoteService.METHOD_NONCE)) {
            return noteService.callNonce(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_LIST)) {
            return noteService.callList(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_CREATE)) {
            return noteService.callCreate(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_UPDATE)) {
            return noteService.callUpdate(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_DELETE)) {
            return noteService.callDelete(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_CREATE_ATTACHMENT)) {
            return noteService.callCreateAttachment(address, params, blobHex);
        } else if (methodHex.equals(NoteService.METHOD_GET_RESOURCE)) {
            return noteService.callGetResource(address, params, blobHex);
        } else {
            logger.error("invalid methodHex: {}", methodHex);
            throw new BaseBadRequestException("invalid methodHex");
        }

    }


    private String getWeixinUnionId(InitRequestVO requestVO) throws BaseBadRequestException, BaseInternalServerErrorException {

        Map result = weixinService.snsJscode2session(requestVO.getAppid(), requestVO.getCode());

        String sessionKey = (String) result.get("session_key");
        String openId = (String) result.get("openid");

        String encryptedDataB64 = requestVO.getEncryptedData();
        String ivB64 = requestVO.getIv();

        String plain = weixinService.decrypt(encryptedDataB64, sessionKey, ivB64);
        String userInfoStr = new String(Base64.getDecoder().decode(plain));
        Map userInfo = null;
        try {
            userInfo = objectMapper.readValue(userInfoStr, LinkedHashMap.class);
        } catch (Exception e) {
            logger.error("userInfo json decode error: {}", e);
            throw new BaseBadRequestException("userInfo json decode error");
        }

        String weixinUnionId = (String) userInfo.get("unionId");

        return weixinUnionId;
    }
}
