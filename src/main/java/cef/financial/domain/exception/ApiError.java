package cef.financial.domain.exception;

public class ApiError {

    public String code;
    public String message;

    public ApiError() {
    }

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
