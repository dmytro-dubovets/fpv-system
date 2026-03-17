package ua.fpv.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeRequestAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.Collections;
import java.util.Map;

public class CustomScopeConverter implements AuthenticationConverter {
    private final OAuth2AuthorizationCodeRequestAuthenticationConverter delegate =
            new OAuth2AuthorizationCodeRequestAuthenticationConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {
        // Створюємо обгортку над запитом, яка зливає масив scope в один рядок
        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (OAuth2ParameterNames.SCOPE.equals(name)) {
                    String[] values = super.getParameterValues(name);
                    return (values != null && values.length > 0) ? String.join(" ", values) : null;
                }
                return super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> map = new java.util.HashMap<>(super.getParameterMap());
                if (map.containsKey(OAuth2ParameterNames.SCOPE)) {
                    map.compute(OAuth2ParameterNames.SCOPE, (k, values) -> new String[]{String.join(" ", values)});
                }
                return Collections.unmodifiableMap(map);
            }
        };

        return delegate.convert(wrappedRequest);
    }
}