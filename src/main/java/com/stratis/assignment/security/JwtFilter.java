package com.stratis.assignment.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This filter verifies that the jwt is valid for accessing the application
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

  @Autowired private JwtUtil jwtTokenUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {

    String token = httpServletRequest.getHeader("Authorization");

    if (token == null) {
      logger.info("No token found in request");
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
      return;
    }

    if (!jwtTokenUtil.validateToken(token)) {
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
      return;
    }

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    List<String> excludeUrlPatterns = new ArrayList<>();
    excludeUrlPatterns.add("/login");
    AntPathMatcher pathMatcher = new AntPathMatcher();

    return excludeUrlPatterns.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, request.getServletPath()));
  }
}
