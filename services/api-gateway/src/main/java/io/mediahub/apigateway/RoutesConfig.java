package io.mediahub.apigateway;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.removeRequestHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions.tokenRelay;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Which /api paths go to which service. Each new service adds one route here.
 * Every route gets the same two filters:
 * - tokenRelay: adds "Authorization: Bearer <the user's access token>" (refreshed if expired)
 * - remove Cookie: the session cookie is the gateway's business, not the services'.
 */
@Configuration
class RoutesConfig {

    @Bean
    RouterFunction<ServerResponse> catalogRoute(RouteProperties routes) {
        return route("catalog")
                .route(path("/api/v1/media").or(path("/api/v1/media/**")), http())
                .before(uri(routes.catalog()))
                .before(removeRequestHeader(HttpHeaders.COOKIE))
                .filter(tokenRelay())
                .build();
    }
}
