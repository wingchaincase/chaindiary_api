package wingchaincase.chaindiaryapi.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class LogAspect {

    Logger logger = LoggerFactory.getLogger(LogAspect.class);

    private static ThreadLocal<Date> startTimeThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> traceIdThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> uriThreadLocal = new ThreadLocal<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(public * wingchaincase.chaindiaryapi.controller..*.*(..))")
    public void pointcut() {
    }

    @Pointcut("execution(public * wingchaincase.chaindiaryapi.controller..*.*(..)) || execution(public * wingchaincase.chaindiaryapi.aspect.ErrorHandler.*(..))")
    public void pointcutWithError() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {

        startTimeThreadLocal.set(new Date());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        String uri = request.getRequestURI();
        String method = request.getMethod();
        Map<String, Object> args = new LinkedHashMap<>();

        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            String name = parameterNames[i];
            Object value = parameterValues[i];
            if (!(value instanceof MultipartFile)) {
                args.put(name, value);
            }
        }

        String traceId = request.getHeader("X-Meta-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        traceIdThreadLocal.set(traceId);
        uriThreadLocal.set(uri);

        logger.info("request: traceId={}, uri={}, method={}, args={}", traceId, uri, method, objectMapper.writeValueAsString(args));

    }

    @AfterReturning(returning = "ret", pointcut = "pointcutWithError()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容

        if (ret instanceof ResponseEntity) {
            ret = null;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String traceId = traceIdThreadLocal.get();
        String uri = uriThreadLocal.get();

        logger.info("response: traceId={}, uri={}, costTime={}, ret={}", traceId, uri, getCostTime(), objectMapper.writeValueAsString(ret));

    }

    private BigDecimal getCostTime() {
        Date startTime = startTimeThreadLocal.get();
        BigDecimal costTime = null;
        if (startTime != null) {
            Long costTimeMs = new Date().getTime() - startTime.getTime();
            costTime = BigDecimal.valueOf(costTimeMs).divide(BigDecimal.valueOf(1000)).setScale(3);
        }
        return costTime;
    }
}
