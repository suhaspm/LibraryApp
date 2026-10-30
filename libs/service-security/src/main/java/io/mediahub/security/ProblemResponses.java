package io.mediahub.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * 401 and 403 in the same RFC 9457 format as every other error, so clients handle one shape.
 *
 * <p>401 Unauthorized means "I don't know who you are" (no token, expired, forged).
 * 403 Forbidden means "I know who you are, and you may not do this".
 */
final class ProblemResponses {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ProblemResponses() {
    }

    static AuthenticationEntryPoint unauthorized() {
        return (request, response, e) -> {
            // Tells the client which scheme to use, as RFC 6750 requires for bearer-token APIs.
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"mediahub\"");
            write(response, HttpStatus.UNAUTHORIZED, "Sign in to do this.");
        };
    }

    static AccessDeniedHandler forbidden() {
        return (request, response, e) -> write(response, HttpStatus.FORBIDDEN, "Your account is not allowed to do this.");
    }

    private static void write(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        JSON.writeValue(response.getOutputStream(), problem);
    }
}
