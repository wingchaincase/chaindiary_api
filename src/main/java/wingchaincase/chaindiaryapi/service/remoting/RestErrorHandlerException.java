package wingchaincase.chaindiaryapi.service.remoting;

import wingchaincase.chaindiaryapi.exception.BaseException;

public class RestErrorHandlerException extends RuntimeException {

    private BaseException baseException;

    public RestErrorHandlerException(BaseException baseException) {
        this.baseException = baseException;
    }

    public BaseException getBaseException() {
        return baseException;
    }
}
