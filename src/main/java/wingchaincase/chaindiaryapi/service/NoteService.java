package wingchaincase.chaindiaryapi.service;

import io.github.novacrypto.bip39.Words;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wingchaincase.chaindiaryapi.domain.Note;
import wingchaincase.chaindiaryapi.domain.NoteAttachment;
import wingchaincase.chaindiaryapi.domain.vo.NoteAttachmentVO;
import wingchaincase.chaindiaryapi.domain.vo.NoteVO;
import wingchaincase.chaindiaryapi.domain.vo.PageVO;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.repository.NoteAttachmentRepository;
import wingchaincase.chaindiaryapi.repository.NoteRepository;
import wingchaincase.chaindiaryapi.repository.NoteSeedRepository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class NoteService {

    public final static String METHOD_NONCE = "00";

    public final static String METHOD_LIST = "01";

    public final static String METHOD_CREATE = "02";

    public final static String METHOD_UPDATE = "03";

    public final static String METHOD_DELETE = "04";

    public final static String METHOD_CREATE_ATTACHMENT = "05";

    public final static String METHOD_GET_RESOURCE = "06";

    private final static Logger logger = LoggerFactory.getLogger(NoteService.class);

    @Value("${note.storage.bucket}")
    private String storageBucket;

    @Value("${note.storage.key-base}")
    private String storageKeyBase;

    @Autowired
    private NoteResourceService noteResourceService;

    @Autowired
    private NoteSeedRepository noteSeedRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteAttachmentRepository noteAttachmentRepository;

    public NoteSeedRepository getNoteSeedRepository() {
        return noteSeedRepository;
    }

    public void setNoteSeedRepository(NoteSeedRepository noteSeedRepository) {
        this.noteSeedRepository = noteSeedRepository;
    }

    public byte[] generateEntropy() {
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        return entropy;
    }

    public String getAddress(String publicKeyHex) throws BaseBadRequestException {

        byte[] publicKeyBytes = null;
        try {
            publicKeyBytes = Hex.decodeHex(publicKeyHex);
        } catch (Exception e) {
            logger.error("get address from public error: {}", e);
            throw new BaseBadRequestException("get address from public error");
        }

        byte[] hash = sha256(publicKeyBytes);

        String hashStr = Hex.encodeHexString(hash);

        String address = hashStr.substring(0, 40);

        return address;
    }

    public byte[] sha256(byte[] input) throws BaseBadRequestException {

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (Exception e) {
            logger.error("sha256 error: {}", e);
            throw new BaseBadRequestException("sha256 error");
        }
    }

    public Integer hexToIntLE(final String hex) throws BaseBadRequestException {
        try {
            byte[] bytes = Hex.decodeHex(hex);
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.put(bytes);
            Integer val = bb.getInt(0);
            return val;

        } catch (Exception e) {
            throw new BaseBadRequestException("Failed to parse hex to int le");
        }

    }

    public String intToHexLE(final int val) {

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(val);
        return Hex.encodeHexString(bb.array());
    }

    public Map<String, String> extractTx(String txHex) throws BaseBadRequestException {

        try {
            String publicKeyHex = txHex.substring(0, 66);
            String signatureHex = txHex.substring(66, 194);
            String methodHex = txHex.substring(194, 196);
            String paramsHex = txHex.substring(196);

            return new HashMap<String, String>() {{
                put("publicKeyHex", publicKeyHex);
                put("signatureHex", signatureHex);
                put("methodHex", methodHex);
                put("paramsHex", paramsHex);
            }};

        } catch (Exception e) {
            logger.error("invalid tx: {}", e);
            throw new BaseBadRequestException();
        }
    }

    public Map<String, String> extractParams(String methodHex, String paramsHex) throws BaseBadRequestException {

        if (methodHex.equals(METHOD_NONCE)) {
            return new HashMap<String, String>() {{
            }};
        } else if (methodHex.equals(METHOD_LIST)) {// list

            String startHex = paramsHex.substring(0, 8);
            String sizeHex = paramsHex.substring(8, 16);
            return new HashMap<String, String>() {{
                put("startHex", startHex);
                put("sizeHex", sizeHex);
            }};
        } else if (methodHex.equals(METHOD_CREATE)) { // create
            String contentHashHex = paramsHex.substring(0, 64);
            String contentEncHashHex = paramsHex.substring(64, 128);
            String nonceHex = paramsHex.substring(128, 136);

            return new HashMap<String, String>() {{
                put("contentHashHex", contentHashHex);
                put("contentEncHashHex", contentEncHashHex);
                put("nonceHex", nonceHex);
            }};
        } else if (methodHex.equals(METHOD_UPDATE)) { // update
            String noteIdHex = paramsHex.substring(0, 8);
            String contentHashHex = paramsHex.substring(8, 72);
            String contentEncHashHex = paramsHex.substring(72, 136);
            String nonceHex = paramsHex.substring(136, 144);

            return new HashMap<String, String>() {{
                put("noteIdHex", noteIdHex);
                put("contentHashHex", contentHashHex);
                put("contentEncHashHex", contentEncHashHex);
                put("nonceHex", nonceHex);
            }};
        } else if (methodHex.equals(METHOD_DELETE)) { // delete
            String noteIdHex = paramsHex.substring(0, 8);
            String nonceHex = paramsHex.substring(8, 16);
            return new HashMap<String, String>() {{
                put("noteIdHex", noteIdHex);
                put("nonceHex", nonceHex);
            }};
        } else if (methodHex.equals(METHOD_CREATE_ATTACHMENT)) { // create attachment
            String attachmentHashHex = paramsHex.substring(0, 64);
            String attachmentEncHashHex = paramsHex.substring(64, 128);
            String nonceHex = paramsHex.substring(128, 136);

            return new HashMap<String, String>() {{
                put("attachmentHashHex", attachmentHashHex);
                put("attachmentEncHashHex", attachmentEncHashHex);
                put("nonceHex", nonceHex);
            }};
        } else if (methodHex.equals(METHOD_GET_RESOURCE)) {// get

            String encHashHex = paramsHex.substring(0, 64);
            return new HashMap<String, String>() {{
                put("encHashHex", encHashHex);
            }};
        } else {
            logger.error("invalid methodHex: {}", methodHex);
            throw new BaseBadRequestException("invalid methodHex");
        }
    }

    public Object callNonce(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String nonce = generateNonce(address);

        return nonce;
    }

    public Object callList(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        int start = hexToIntLE(params.get("startHex"));
        int size = hexToIntLE(params.get("sizeHex"));

        Pageable pageable = PageRequest.of((int) (start / size), size, Sort.by(new Sort.Order(Sort.Direction.DESC, "noteId")));

        List<Note> list = noteRepository.findByAddressAndIsDeleted(address, false, pageable);
        Long count = noteRepository.countByAddressAndIsDeleted(address, false);

        List<NoteVO> voList = getNoteListWithContentEnc(list);

        PageVO pageVO = new PageVO();
        pageVO.setList(voList);
        pageVO.setCount(count);

        return pageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Object callCreate(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String nonceHex = params.get("nonceHex");
        verifyNonce(address, nonceHex);

        String contentHashHex = params.get("contentHashHex");
        String contentEncHashHex = params.get("contentEncHashHex");
        String contentEncHex = blobHex;

        String expectContentEncHash = null;

        try {
            expectContentEncHash = Hex.encodeHexString(sha256(Hex.decodeHex(contentEncHex)));
        } catch (Exception e) {
            logger.error("invalid contentEncHex: {}", e);
            throw new BaseBadRequestException("invalid contentEncHex");
        }

        if (!contentEncHashHex.equals(expectContentEncHash)) {
            throw new BaseBadRequestException("invalid contentEncHash");
        }

        saveNote(address, contentEncHashHex, contentEncHex);

        Note note = new Note();
        note.setAddress(address);
        note.setContentEncHash(contentEncHashHex);
        note.setIsDeleted(false);
        noteRepository.save(note);

        NoteVO vo = new NoteVO(note);

        return vo;

    }

    @Transactional(rollbackFor = Exception.class)
    public Object callUpdate(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String nonceHex = params.get("nonceHex");
        verifyNonce(address, nonceHex);

        int noteId = hexToIntLE(params.get("noteIdHex"));

        Note note = noteRepository.findById(Long.valueOf(noteId)).orElse(null);

        if (note == null || note.getIsDeleted()) {
            throw new BaseBadRequestException("note not exist");
        }

        if (!note.getAddress().equals(address)) {
            throw new BaseBadRequestException("note not belong to requester");
        }

        String contentHashHex = params.get("contentHashHex");
        String contentEncHashHex = params.get("contentEncHashHex");
        String contentEncHex = blobHex;

        String expectContentEncHash = null;

        try {
            expectContentEncHash = Hex.encodeHexString(sha256(Hex.decodeHex(contentEncHex)));
        } catch (Exception e) {
            logger.error("invalid contentEncHex: {}", e);
            throw new BaseBadRequestException("invalid contentEncHex");
        }

        if (!contentEncHashHex.equals(expectContentEncHash)) {
            throw new BaseBadRequestException("invalid contentEncHash");
        }

        saveNote(address, contentEncHashHex, contentEncHex);

        note.setContentEncHash(contentEncHashHex);
        noteRepository.save(note);

        NoteVO vo = new NoteVO(note);

        return vo;

    }

    @Transactional(rollbackFor = Exception.class)
    public Object callDelete(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String nonceHex = params.get("nonceHex");
        verifyNonce(address, nonceHex);

        int noteId = hexToIntLE(params.get("noteIdHex"));

        Note note = noteRepository.findById(Long.valueOf(noteId)).orElse(null);

        if (note == null || note.getIsDeleted()) {
            throw new BaseBadRequestException("note not exist");
        }

        if (!note.getAddress().equals(address)) {
            throw new BaseBadRequestException("note not belong to requester");
        }

        note.setIsDeleted(true);
        noteRepository.save(note);

        return "";

    }

    @Transactional(rollbackFor = Exception.class)
    public Object callCreateAttachment(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String nonceHex = params.get("nonceHex");
        verifyNonce(address, nonceHex);

        String attachmentHashHex = params.get("attachmentHashHex");
        String attachmentEncHashHex = params.get("attachmentEncHashHex");
        String attachmentEncHex = blobHex;

        String expectContentEncHash = null;

        try {
            expectContentEncHash = Hex.encodeHexString(sha256(Hex.decodeHex(attachmentEncHex)));
        } catch (Exception e) {
            logger.error("invalid contentEncHex: {}", e);
            throw new BaseBadRequestException("invalid contentEncHex");
        }

        if (!attachmentEncHashHex.equals(expectContentEncHash)) {
            throw new BaseBadRequestException("invalid contentEncHash");
        }

        saveAttachement(address, attachmentEncHashHex, attachmentEncHex);

        NoteAttachment attachment = new NoteAttachment();
        attachment.setAddress(address);
        attachment.setAttachmentEncHash(attachmentEncHashHex);
        attachment.setIsDeleted(false);
        noteAttachmentRepository.save(attachment);

        NoteAttachmentVO vo = new NoteAttachmentVO(attachment);

        return vo;

    }

    public Object callGetResource(String address, Map<String, String> params, String blobHex) throws BaseBadRequestException {

        String encHashHex = params.get("encHashHex");

        String resource = getResource(address, encHashHex);

        return new LinkedHashMap<String, Object>() {{
            put("resourceEnc", resource);
        }};
    }

    public List<NoteVO> getNoteListWithContentEnc(List<Note> list) {
        return list.stream().parallel().map(x -> {
            String contentEncHash = x.getContentEncHash();

            String contentType = "plain/text";

            String ext = noteResourceService.getExtByMime(contentType);

            String address = x.getAddress();

            String noteKey = storageKeyBase + "/" + address + "/" + contentEncHash + "." + ext;

            String url = noteResourceService.getPresignedUrl(storageBucket, noteKey);

            String contentEnc = "";
            try {
                contentEnc = noteResourceService.getResourceString(url);
            } catch (Exception e) {
                logger.error("fail to get content: {}", noteKey);
            }
            return new NoteVO(x, contentEnc);
        }).collect(Collectors.toList());
    }

    public String getResource(String address, String encHashHex) {

        String contentType = "plain/text";

        String ext = noteResourceService.getExtByMime(contentType);

        String key = storageKeyBase + "/" + address + "/" + encHashHex + "." + ext;

        String url = noteResourceService.getPresignedUrl(storageBucket, key);

        String enc = "";
        try {
            enc = noteResourceService.getResourceString(url);
        } catch (Exception e) {
            logger.error("fail to get resource: {}", key);
        }
        return enc;

    }

    public void saveNote(String address, String contentEncHashHex, String contentEncHex) {

        String contentType = "plain/text";

        String ext = noteResourceService.getExtByMime(contentType);

        String noteKey = storageKeyBase + "/" + address + "/" + contentEncHashHex + "." + ext;

        noteResourceService.upload(storageBucket, noteKey, contentEncHex.getBytes(), contentType);

    }

    public void saveAttachement(String address, String contentEncHashHex, String contentEncHex) {

        String contentType = "plain/text";

        String ext = noteResourceService.getExtByMime(contentType);

        String noteKey = storageKeyBase + "/" + address + "/" + contentEncHashHex + "." + ext;

        noteResourceService.upload(storageBucket, noteKey, contentEncHex.getBytes(), contentType);

    }

    private final static Long NONCE_LIFE_IN_S = 1 * 60L;

    private final static String NONCE_KEY_PREFIX = "note_nonce_";

    @Autowired
    private RedisTemplate redisTemplate;

    private String generateNonce(String address) throws BaseBadRequestException {

        Integer rand = new Random().nextInt(Integer.MAX_VALUE);

        String nonce = intToHexLE(rand);

        String key = NONCE_KEY_PREFIX + address;

        redisTemplate.opsForValue().set(key, nonce, NONCE_LIFE_IN_S, TimeUnit.SECONDS);

        return nonce;
    }

    private Boolean verifyNonce(String address, String nonce) {

        String key = NONCE_KEY_PREFIX + address;

        String expectedNonce = (String) redisTemplate.opsForValue().get(key);

        if (nonce.equals(expectedNonce)) {

            redisTemplate.opsForValue().set(key, null);

            return true;
        }
        return false;
    }

}
