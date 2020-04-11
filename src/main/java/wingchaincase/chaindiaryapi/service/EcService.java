package wingchaincase.chaindiaryapi.service;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class EcService {

    private final static Logger logger = LoggerFactory.getLogger(EcService.class);

    @PostConstruct
    public void loadEcLib() throws Exception {

        String libFileName = System.getProperty("os.name").toLowerCase().contains("mac") ? "/jec/libjec.dylib" : "/jec/libjec.so";

        InputStream is = getClass().getResourceAsStream(libFileName);
        File file = File.createTempFile("lib", ".so");
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();

        System.load(file.getAbsolutePath());
        file.deleteOnExit();

    }

    public static native String eciesEncrypt(String publicKeyHex, String plainHex);

    public static native String eciesDecrypt(String secretKeyHex, String cypherHex);

    public static native String ecdsaSign(String secretKeyHex, String message);

    public static native boolean ecdsaVerify(String publicKeyHex, String message, String signature);

    public static String eciesEncryptSafe(String publicKeyHex, String plainHex) throws BaseBadRequestException {

        if (!EcService.isValidPublicKeyHex(publicKeyHex)) {
            throw new BaseBadRequestException("invalid publicKeyHex");
        }
        if (!EcService.isValidHex(plainHex)) {
            throw new BaseBadRequestException("invalid plainHex");
        }
        return eciesEncrypt(publicKeyHex, plainHex);
    }

    public static String eciesDecryptSafe(String secretKeyHex, String cypherHex) throws BaseBadRequestException {

        if (!EcService.isValidSecretKeyHex(secretKeyHex)) {
            throw new BaseBadRequestException("invalid secretKeyHex");
        }
        if (!EcService.isValidHex(cypherHex)) {
            throw new BaseBadRequestException("invalid cypherHex");
        }
        return eciesDecrypt(secretKeyHex, cypherHex);
    }

    public static String ecdsaSignSafe(String secretKeyHex, String messageHex) throws BaseBadRequestException {
        if (!EcService.isValidSecretKeyHex(secretKeyHex)) {
            throw new BaseBadRequestException("invalid secretKeyHex");
        }
        if (!EcService.isValidHex(messageHex)) {
            throw new BaseBadRequestException("invalid messageHex");
        }
        return ecdsaSign(secretKeyHex, messageHex);
    }

    public static boolean ecdsaVerifySafe(String publicKeyHex, String messageHex, String signatureHex) throws BaseBadRequestException {
        if (!EcService.isValidPublicKeyHex(publicKeyHex)) {
            throw new BaseBadRequestException("invalid publicKeyHex");
        }
        if (!EcService.isValidSignatureHex(signatureHex)) {
            throw new BaseBadRequestException("invalid signatureHex");
        }
        if (!EcService.isValidHex(messageHex)) {
            throw new BaseBadRequestException("invalid messageHex");
        }
        return ecdsaVerify(publicKeyHex, messageHex, signatureHex);
    }


    public static boolean isValidPublicKeyHex(String publicKeyHex) {
        try {
            Hex.decodeHex(publicKeyHex);
        } catch (Exception e) {
            return false;
        }

        if (publicKeyHex.length() != 66) {
            return false;
        }
        return true;
    }

    public static boolean isValidSecretKeyHex(String secretKeyHex) {
        try {
            Hex.decodeHex(secretKeyHex);
        } catch (Exception e) {
            return false;
        }

        if (secretKeyHex.length() != 64) {
            return false;
        }
        return true;
    }

    public static boolean isValidSignatureHex(String signatureHex) {
        try {
            Hex.decodeHex(signatureHex);
        } catch (Exception e) {
            return false;
        }

        if (signatureHex.length() != 128) {
            return false;
        }
        return true;
    }

    public static boolean isValidHex(String hex) {
        try {
            Hex.decodeHex(hex);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
