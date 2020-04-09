package wingchaincase.chaindiaryapi.service.remoting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import wingchaincase.chaindiaryapi.exception.BaseInternalServerErrorException;

public class RemoteUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static HttpEntity<MultiValueMap<String, String>> buildFormRequest(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        return request;
    }

    public static HttpEntity buildJSONRequest(Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(object, headers);
        return request;
    }

    public static UriComponentsBuilder buildBuilder(String url, MultiValueMap<String, String> map) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                builder.queryParam(key, value);
            }
        }
        return builder;
    }

    public static Long parseLong(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Integer) {
            return Long.valueOf((Integer) object);
        } else if (object instanceof Long) {
            return (Long) object;
        } else {
            return Long.parseLong((String) object);
        }
    }

    public static String convertCharset(String str, String fromCharset, String toCharset) throws BaseInternalServerErrorException {
        try {
            str = new String(str.getBytes(fromCharset), toCharset);
        } catch (Exception e) {
            throw new BaseInternalServerErrorException("failed to convert charset");
        }
        return str;
    }

    public static Object jsonDecode(String str, Class clazz) throws BaseInternalServerErrorException {
        Object obj = null;
        try {
            obj = objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            throw new BaseInternalServerErrorException("failed to json decode");
        }
        return obj;
    }

}
