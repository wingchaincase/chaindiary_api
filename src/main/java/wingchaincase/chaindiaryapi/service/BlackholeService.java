package wingchaincase.chaindiaryapi.service;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class BlackholeService {

    private final static Logger logger = LoggerFactory.getLogger(BlackholeService.class);

    @Autowired
    Environment environment;

    @Value("${note.blackhole.enc.enabled}")
    private Boolean encEnabled;

    @PostConstruct
    public void loadBlackholeLib() throws Exception {

        if (encEnabled) {
            String libFileName = System.getProperty("os.name").toLowerCase().contains("mac") ? "/jblackhole/libjblackhole.dylib" : "/jblackhole/libjblackhole.so";

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

    }

    public static native String encrypt(String profile, String plainHex);

    public static native String decrypt(String profile, String cypherHex);

    public String encryptSafe(String plainHex) throws BaseBadRequestException {

        if (!encEnabled) {
            return plainHex;
        }

        if (!BlackholeService.isValidHex(plainHex)) {
            throw new BaseBadRequestException("invalid plainHex");
        }
        return encrypt(determineProfile(), plainHex);
    }

    public String decryptSafe(String cypherHex) throws BaseBadRequestException {

        if (!encEnabled) {
            return cypherHex;
        }

        if (!BlackholeService.isValidHex(cypherHex)) {
            throw new BaseBadRequestException("invalid cypherHex");
        }
        return decrypt(determineProfile(), cypherHex);
    }


    public static boolean isValidHex(String hex) {
        try {
            Hex.decodeHex(hex);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String determineProfile() {
        String[] profiles = environment.getActiveProfiles();

        String profile = profiles == null || profiles.length == 0 ? "dev" : profiles[0];

        return profile;
    }

}
