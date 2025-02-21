package com.zenyte.api.schema;


import org.springframework.security.web.RedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class NoRedirectStrategy implements RedirectStrategy {
    
    @Override
    public void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, final String url) throws IOException {
        // No redirect is required with pure REST
    }
}