package com.zenyte.api.security;

import com.zenyte.api.Profiles;
import com.zenyte.api.schema.NoRedirectStrategy;
import com.zenyte.api.security.token.TokenAuthenticationFilter;
import com.zenyte.api.security.token.TokenAuthenticationProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Objects;

/**
 * @author Noele
 * @author Corey
 * see https://noeles.life || noele@zenyte.com
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile(Profiles.PRODUCTION)
class ProductionSecurityConfig extends WebSecurityConfigurerAdapter {
    
    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
            new AntPathRequestMatcher("/hiscores/**", "GET"),
            new AntPathRequestMatcher("/worldinfo/**", "GET"),
            new AntPathRequestMatcher("/user/info/*", "GET"),
            new AntPathRequestMatcher("/user/awards/*", "GET"),
            new AntPathRequestMatcher("/user/adv/*", "GET"),
            new AntPathRequestMatcher("/runelite/items/**", "GET"),
            new AntPathRequestMatcher("/public/**", "GET"),
            new AntPathRequestMatcher("/favicon.ico", "GET")
    );
    
    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);
    
    private final TokenAuthenticationProvider provider;
    
    ProductionSecurityConfig(TokenAuthenticationProvider provider) {
        this.provider = Objects.requireNonNull(provider);
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }
    
    @Override
    public void configure(WebSecurity security) {
        security.ignoring().requestMatchers(PUBLIC_URLS);
    }
    
    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security
                // Use a stateless session. Tells Spring to never create a HttpSession for requests.
                // Not necessary in a RESTful environment
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        
                // This entry point handles when you request a protected page and you are not yet authenticated
                .and()
                .exceptionHandling()
                .defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_URLS)
        
                // Specify our authentication method and authorize requests under certain circumstances
                .and()
                .authenticationProvider(provider)
                .addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest()
                .authenticated()
        
                // Disable any functions that are not necessary in our RESTful environment
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();
    }
    
    @Bean
    public TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(PROTECTED_URLS);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }
    
    @Bean
    public SimpleUrlAuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy(new NoRedirectStrategy());
        return successHandler;
    }
    
    @Bean
    public FilterRegistrationBean disableAutoRegistration(TokenAuthenticationFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
    
    @Bean
    public AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }
    
}