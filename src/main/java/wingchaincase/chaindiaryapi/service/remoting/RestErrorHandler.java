package wingchaincase.chaindiaryapi.service.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.exception.BaseException;
import wingchaincase.chaindiaryapi.exception.BaseInternalServerErrorException;

import java.io.IOException;
import java.util.Map;

public class RestErrorHandler extends DefaultResponseErrorHandler {

    private final static Logger logger = LoggerFactory.getLogger(RestErrorHandler.class);

    private RestTemplate restTemplate;

    public RestErrorHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        HttpMessageConverterExtractor<Object> responseExtractor =
                new HttpMessageConverterExtractor<>(Object.class, restTemplate.getMessageConverters());

        Object object = responseExtractor.extractData(response);

        logger.error("remoting service encountered error, response={}", object);

        String basePackage = getBasePackage();

        BaseException exception = null;

        if (object instanceof Map) {
            Map map = (Map) object;
            String error = (String) map.get("error");
            if (!StringUtils.isEmpty(error)) {
                try {
                    exception = (BaseException) (Class.forName(basePackage + ".exception." + error).newInstance());
                } catch (Exception e) {
                }
            }
        }

        if (exception == null) {

            HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());

            if (statusCode.series() == HttpStatus.Series.CLIENT_ERROR) {
                exception = new BaseBadRequestException();
            } else {
                exception = new BaseInternalServerErrorException();
            }

        }

        if (exception != null) {
            throw new RestErrorHandlerException(exception);
        }

    }

    private String getBasePackage() {
        String packageName = getClass().getPackage().getName();

        packageName = packageName.replace(".service.remoting", "");

        return packageName;
    }
}
