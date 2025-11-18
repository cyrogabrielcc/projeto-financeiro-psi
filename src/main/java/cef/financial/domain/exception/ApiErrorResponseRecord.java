package cef.financial.domain.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponseRecord(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {}