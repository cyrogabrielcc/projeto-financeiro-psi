package cef.financial.domain.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public class ApiErrorResponse {

    public int status;
    public String error;
    public String message;
    public String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    public OffsetDateTime timestamp;
}
