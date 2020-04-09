package wingchaincase.chaindiaryapi.aspect;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;

@ControllerAdvice("wingchaincase.chaindiaryapi")
public class ResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (converterType.equals(ResourceHttpMessageConverter.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof LinkedHashMap && ((LinkedHashMap) body).containsKey("status")) {
            LinkedHashMap error = (LinkedHashMap) body;
            Integer status = (Integer) error.get("status");
            if (200 != status) {
                Response resp = new Response();
                resp.setCode(1L);
                resp.setMessage((String) error.get("error"));
                resp.setData(error.get("message"));
                return resp;
            }
        }

        Response resp = new Response();
        resp.setCode(0L);
        resp.setMessage("OK");
        resp.setData(body);

        return resp;
    }

}
