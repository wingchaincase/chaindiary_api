package wingchaincase.chaindiaryapi.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import wingchaincase.chaindiaryapi.exception.BaseBadRequestException;
import wingchaincase.chaindiaryapi.exception.BaseInternalServerErrorException;
import wingchaincase.chaindiaryapi.exception.BaseUnauthorizedException;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {

    private final static Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @Autowired
    Environment environment;

    @ResponseBody
    @ExceptionHandler(value = BaseBadRequestException.class)
    public ResponseEntity<Object> errorHandler400(Exception ex) {

        logger.warn("errorHandler400: ", ex);

        Map map = new LinkedHashMap();
        map.put("timestamp", new Date().getTime());
        map.put("status", 400);
        map.put("error", ex.getClass().getSimpleName());
        map.put("message", ex.getMessage());
        return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> errorHandler500(Exception ex) {

        logger.warn("errorHandler500: ", ex);

        String[] profiles = environment.getActiveProfiles();

        String profile = profiles == null || profiles.length == 0 ? null : profiles[0];

        if ("prod".equals(profile) && !ex.getClass().getName().startsWith("wingchaincase.chaindiary")) {
            ex = new BaseInternalServerErrorException();
        }

        Map map = new LinkedHashMap();
        map.put("timestamp", new Date().getTime());
        map.put("status", 500);
        map.put("error", ex.getClass().getSimpleName());
        map.put("message", ex.getMessage());
        return new ResponseEntity<Object>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(value = BaseUnauthorizedException.class)
    public ResponseEntity<Object> errorHandler401(Exception ex) {

        logger.warn("errorHandler401: ", ex);

        Map map = new LinkedHashMap();
        map.put("timestamp", new Date().getTime());
        map.put("status", 401);
        map.put("error", ex.getClass().getSimpleName());
        map.put("message", ex.getMessage());
        return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
    }
}
