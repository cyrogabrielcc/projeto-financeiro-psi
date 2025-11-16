package cef.financial.api.error;

import cef.financial.domain.exception.ApiErrorResponse;
import cef.financial.domain.exception.BusinessException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.OffsetDateTime;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(BusinessException exception) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.status = exception.getStatus();
        error.error = Response.Status.fromStatusCode(exception.getStatus()).getReasonPhrase();
        error.message = exception.getMessage();
        error.path = uriInfo != null ? uriInfo.getPath() : null;
        error.timestamp = OffsetDateTime.now();

        return Response.status(exception.getStatus())
                .entity(error)
                .build();
    }
}
