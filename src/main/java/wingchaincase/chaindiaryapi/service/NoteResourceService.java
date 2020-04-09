package wingchaincase.chaindiaryapi.service;

import com.aliyun.oss.OSSClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.exception.URLNotFoundException;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class NoteResourceService {

    Logger logger = LoggerFactory.getLogger(NoteResourceService.class);

    private final static Map<String, String> EXT_TO_MIME = new HashMap<String, String>() {{
        put("txt", "plain/text");
    }};

    private final static Map<String, String> MIME_TO_EXT = new HashMap<String, String>() {{
        for (String key : EXT_TO_MIME.keySet()) {
            put(EXT_TO_MIME.get(key), key);
        }
    }};

    @Value("${note.oss.access-key-id}")
    private String ossAccessKeyId;

    @Value("${note.oss.access-key-secret}")
    private String ossAccessKeySecret;

    @Value("${note.oss.end-point.internal}")
    private String ossEndPointInternal;

    @Value("${note.oss.end-point}")
    private String ossEndPoint;

    public void upload(String bucketName, String key, byte[] raw, String mime) {

        OSSClient ossClient = new OSSClient(ossEndPointInternal, ossAccessKeyId, ossAccessKeySecret);
        ossClient.putObject(bucketName, key, new ByteArrayInputStream(raw));
        ossClient.shutdown();

    }

    public String getPresignedUrl(String bucketName, String key) {
        OSSClient ossClient = new OSSClient(ossEndPoint, ossAccessKeyId, ossAccessKeySecret);

        Date expiration = new Date(new Date().getTime() + 3600 * 1000);

        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);

        ossClient.shutdown();

        return url.toString();
    }

    public String getResourceString(String url) throws BaseBadRequestException {

        String str = null;

        try {
            URL urlObj = new URL(url);
            str = new String(IOUtils.toByteArray(urlObj));
        } catch (Exception e) {
            throw new URLNotFoundException();
        }

        return str;

    }

    public ResponseEntity<Resource> getResource(String url) throws BaseBadRequestException {

        Resource resource = null;
        String mime = null;

        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            String ext = path.substring(path.lastIndexOf(".") + 1);
            mime = getMimeByExt(ext);

            resource = new InputStreamResource(urlObj.openStream());
        } catch (Exception e) {
            throw new URLNotFoundException();
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mime)).body(resource);

    }

    public String getExtByMime(String mime) {
        return MIME_TO_EXT.get(mime);
    }

    public String getMimeByExt(String ext) {
        return EXT_TO_MIME.get(ext);
    }

}
