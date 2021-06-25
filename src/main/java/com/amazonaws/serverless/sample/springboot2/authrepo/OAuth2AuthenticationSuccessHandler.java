package com.amazonaws.serverless.sample.springboot2.authrepo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.AUTHORIZATION_REQUEST_COOKIE_NAME;
import static com.amazonaws.serverless.sample.springboot2.authrepo.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI;
import static com.amazonaws.serverless.sample.springboot2.configuration.SecurityConfiguration.SECURITY_CONTEXT;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationSuccessHandler
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(OAuth2AuthenticationSuccessHandler.class);


	@Override
	protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response) {

		SecurityContext context = SecurityContextHolder.getContext();
		if(context != null && context.getAuthentication()!= null && context.getAuthentication().getPrincipal() != null ) {
			DefaultOAuth2User user = (DefaultOAuth2User) context.getAuthentication().getPrincipal();
			String userInfo = HttpCookieOAuth2AuthorizationRequestRepository.serialize(user);
			Cookie cookie = new Cookie(SECURITY_CONTEXT, userInfo);
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			cookie.setMaxAge(60);
			//cookie.setSecure(true);
			response.addCookie(cookie);
		}
		String targetUrl = fetchCookie(request,
				REDIRECT_URI)
				.map(Cookie::getValue).get();

		HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response,
				AUTHORIZATION_REQUEST_COOKIE_NAME,
				REDIRECT_URI);
		
		return targetUrl ;
	}

	/**
	 * Fetches a cookie from the request
	 */
	public static Optional<Cookie> fetchCookie(HttpServletRequest request, String name) {

		Cookie[] cookies = request.getCookies();

		if (cookies != null && cookies.length > 0)
			for (int i = 0; i < cookies.length; i++)
				if (cookies[i].getName().equals(name))
					return Optional.of(cookies[i]);

		return Optional.empty();
	}
}
