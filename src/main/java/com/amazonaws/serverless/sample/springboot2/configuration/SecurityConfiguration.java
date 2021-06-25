package com.amazonaws.serverless.sample.springboot2.configuration;

import com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository;
import com.amazonaws.serverless.sample.springboot2.authrepo.OAuth2AuthenticationFailureHandler;
import com.amazonaws.serverless.sample.springboot2.authrepo.OAuth2AuthenticationSuccessHandler;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.Base64;

import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI;
import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.fetchCookie;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{

    public static String SECURITY_CONTEXT = "SECURITY_CONTEXT";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(HttpMethod.GET,"/health").permitAll()
                .and().authorizeRequests().anyRequest().authenticated();
        http.setSharedObject(RequestCache.class,new DefaultRequestCache());

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.oauth2Login().
                authorizationEndpoint().
                authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository(60000))
                .and().successHandler(new OAuth2AuthenticationSuccessHandler())
                .failureHandler(new OAuth2AuthenticationFailureHandler());
        http.csrf().disable();
        http.securityContext().securityContextRepository(new HttpCookieSecurityContextRepository());
    }

    /**
     * Request Cache to Save Redirection into cache
     */
    public static class DefaultRequestCache extends NullRequestCache{

        @Override
        public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
            String redirectURL = UrlUtils.buildFullRequestUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString());
            Cookie cookie = new Cookie(REDIRECT_URI,redirectURL);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(60);
            //cookie.setSecure(true);
            response.addCookie(cookie);
        }
    }

    public static class HttpCookieSecurityContextRepository extends HttpSessionSecurityContextRepository {

        @Override
        public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
            if(context != null && context.getAuthentication() != null) {
                Object principal = context.getAuthentication().getPrincipal();
                if (principal != null) {
                    if (principal instanceof DefaultOAuth2User){
                        DefaultOAuth2User user = (DefaultOAuth2User)principal;
                        String userInfo = serialize(user);
                        Cookie cookie = new Cookie(SECURITY_CONTEXT, userInfo);
                        cookie.setPath("/");
                        cookie.setHttpOnly(true);
                        cookie.setMaxAge(60);
                        //cookie.setSecure(true);
                        response.addCookie(cookie);
                    }
                }
            }
        }

        @Override
        public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
            HttpServletRequest request = requestResponseHolder.getRequest();
            DefaultOAuth2User user_context = fetchCookie(request, SECURITY_CONTEXT).map(this::deserialize)
                    .orElse(null);
            if(user_context == null){
                return generateNewContext();
            }else{
                SecurityContext securityContext = new SecurityContextImpl();
                OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(user_context,user_context.getAuthorities(),"cd8b56749faa48a1eddd");
                securityContext.setAuthentication(token);
                return securityContext;
            }
        }

        private DefaultOAuth2User deserialize(Cookie cookie) {

            return SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(cookie.getValue()));
        }

        /**
         * Serializes an object
         */
        public static String serialize(Serializable obj) {

            return Base64.getUrlEncoder().encodeToString(
                    SerializationUtils.serialize(obj));
        }
    }
}
