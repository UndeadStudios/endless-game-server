package com.zenyte.api.security.token;

import com.zenyte.util.ApiUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public final class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final String BEARER = "Bearer";
    
    public TokenAuthenticationFilter(RequestMatcher requiresAuth) {
        super(requiresAuth);
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        final String param = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse(request.getParameter("t"));
        
        final String cloudflare = request.getHeader("CF-Connecting-IP");
        final String ip = (cloudflare == null) ? request.getRemoteAddr() : cloudflare;

        String token = Optional.ofNullable(param)
                               .map(value -> ApiUtils.removeStart(value, BEARER))
                               .map(String::trim)
                               .orElseThrow(() -> new BadCredentialsException("Missing authentication token."));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(token, ip);
        return getAuthenticationManager().authenticate(authentication);
    }
    
    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
    
}