package wingchaincase.chaindiaryapi.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tbc  by 2018/5/31
 */
public class URLNotFoundException extends BaseBadRequestException {

    public Map<String, String> getDisplayMessage() {
        return new HashMap<String, String>() {{

            put("zh", "URL无发访问");
            put("en", "URL not found");

        }};
    }

}
