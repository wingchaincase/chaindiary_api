package wingchaincase.chaindiaryapi.service.remoting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.exception.BaseInternalServerErrorException;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeixinService {

    private final static Logger logger = LoggerFactory.getLogger(WeixinService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RestTemplate externalRestTemplate;

    private String endpoint = "https://api.weixin.qq.com";

    @Value("${note.ma.note.appid}")
    private String maNoteAppid;

    @Value("${note.ma.note.secret}")
    private String maNoteSecret;

    private Map<String, String> maCredentialMap;

    @PostConstruct
    private void initMaCredentials() {
        maCredentialMap = new HashMap<String, String>() {{
            put(maNoteAppid, maNoteSecret);
        }};
    }

    @Autowired
    private RemoteCacheService remoteCacheService;

    public Map token(String appid) throws BaseInternalServerErrorException, BaseBadRequestException {

        String key = "token_" + appid;
        Map value = (Map) remoteCacheService.getCache(key);
        if (value != null) {
            return value;
        }

        String appSecret = maCredentialMap.get(appid);
        if (appSecret == null) {
            throw new BaseBadRequestException("invalid appid");
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credential");
        map.add("appid", appid);
        map.add("secret", appSecret);
        UriComponentsBuilder builder = RemoteUtils.buildBuilder(endpoint + "/cgi-bin/token", map);

        Map result = externalRestTemplate.getForObject(builder.toUriString(), LinkedHashMap.class);

        Long expiresIn = RemoteUtils.parseLong(result.get("expires_in"));
        if (expiresIn == null) {
            logger.error("token failed: {}", result);
            throw new BaseInternalServerErrorException("token failed");
        }

        remoteCacheService.saveCache(key, result, expiresIn);

        return result;

    }

    public Map ticketGetticket(String accessToken, String type) throws BaseInternalServerErrorException {
        String key = "ticket";

        Map value = (Map) remoteCacheService.getCache(key);
        if (value != null) {
            return value;
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("type", type);
        map.add("access_token", accessToken);
        UriComponentsBuilder builder = RemoteUtils.buildBuilder(endpoint + "/cgi-bin/ticket/getticket", map);

        Map result = externalRestTemplate.getForObject(builder.toUriString(), LinkedHashMap.class);

        Long expiresIn = RemoteUtils.parseLong(result.get("expires_in"));
        if (expiresIn == null) {
            throw new BaseInternalServerErrorException("weixin service failed");
        }

        remoteCacheService.saveCache(key, result, expiresIn);

        return result;

    }

    public Map snsJscode2session(String appid, String code) throws BaseBadRequestException, BaseInternalServerErrorException {

        String appSecret = maCredentialMap.get(appid);
        if (appSecret == null) {
            throw new BaseBadRequestException("invalid appid");
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("appid", appid);
        map.add("secret", appSecret);
        map.add("js_code", code);
        map.add("grant_type", "authorization_code");
        UriComponentsBuilder builder = RemoteUtils.buildBuilder(endpoint + "/sns/jscode2session", map);

        logger.debug("url: {}", builder.toUriString());

        String resultStr = externalRestTemplate.getForObject(builder.toUriString(), String.class);

        Map result = null;
        try {
            result = objectMapper.readValue(resultStr, LinkedHashMap.class);
        } catch (Exception e) {
            logger.error("snsJscode2session json decode error: {}", e);
            throw new BaseInternalServerErrorException("snsJscode2session json decode error");
        }

        Long errcode = RemoteUtils.parseLong(result.get("errcode"));
        if (errcode != null && !errcode.equals(0L)) {
            logger.error("snsJscode2session error: {}", result);
            throw new BaseInternalServerErrorException("weixin service failed");
        }

        return result;
    }

    public byte[] wxaGetwxacodeunlimit(String accessToken, String scene, String page) throws BaseInternalServerErrorException {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("access_token", accessToken);

        UriComponentsBuilder builder = RemoteUtils.buildBuilder(endpoint + "/wxa/getwxacodeunlimit", map);

        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("scene", scene);
        body.put("page", page);

        HttpEntity request = RemoteUtils.buildJSONRequest(body);

        ResponseEntity<byte[]> response = externalRestTemplate.postForEntity(
                builder.toUriString(), request, byte[].class);

        if (!response.getHeaders().getContentType().toString().equals("image/jpeg")) {

            logger.error("wxaGetwxacodeunlimit error: {}", new String(response.getBody()));
            throw new BaseInternalServerErrorException("wxaGetwxacodeunlimit error");
        }

        return response.getBody();
    }

    public String sign(Map<String, String> map) throws BaseInternalServerErrorException {

        List<String[]> list = map.entrySet().stream().map(entry -> new String[]{entry.getKey(), entry.getValue()}).collect(Collectors.toList());

        Collections.sort(list, (a, b) -> a[0].compareTo(b[0]));

        StringBuilder sb = new StringBuilder();
        for (String[] item : list) {
            sb.append(item[0]);
            sb.append("=");
            sb.append(item[1]);
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());//remove last '&'
        }

        String text = sb.toString();

        String sig = sha1(text);

        return sig;
    }

    public String decrypt(String encryptedDataB64, String sessionKeyB64, String ivB64) throws BaseBadRequestException {
        try {
            Base64.Decoder decoder = Base64.getDecoder();

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] encryptedData = decoder.decode(encryptedDataB64);

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decoder.decode(sessionKeyB64), "AES"), new IvParameterSpec(decoder.decode(ivB64)));

            byte[] result = cipher.doFinal(encryptedData);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            logger.error("decrypt error: {}", e);
            throw new BaseBadRequestException("decrypt error");
        }
    }

    private String sha1(String text) throws BaseInternalServerErrorException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new BaseInternalServerErrorException();
        }
        byte[] digest = md.digest(text.getBytes());
        String hex = bytesToHex(digest);
        return hex;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
