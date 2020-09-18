package com.amazonaws.serverless.sample.springboot2.authrepo;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.AUTHORIZATION_REQUEST_COOKIE_NAME;
import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI;

/**
 * OAuth2 Authentication failure handler for removing oauth2 related cookies
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationFailureHandler
	extends SimpleUrlAuthenticationFailureHandler {
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		
		HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response,
			AUTHORIZATION_REQUEST_COOKIE_NAME,
                REDIRECT_URI);
		
		super.onAuthenticationFailure(request, response, exception);
	}
}
