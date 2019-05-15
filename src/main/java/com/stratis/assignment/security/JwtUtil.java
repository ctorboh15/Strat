package com.stratis.assignment.security;

import com.stratis.assignment.model.People;
import com.stratis.assignment.service.ResidentialPropertyDataService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
public class JwtUtil implements Serializable {
  Logger logger = LoggerFactory.getLogger(JwtUtil.class);

  @Value("${application.signing.key}")
  private String SIGNING_KEY;

  @Autowired private ResidentialPropertyDataService residentialPropertyDataService;

  private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 5 * 60 * 60;

  public String getFullNameFromToken(String token) {
    return getAllClaimsFromToken(token).getSubject();
  }

  public Date getExpirationDateFromToken(String token) {
    return getAllClaimsFromToken(token).getExpiration();
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public String generateToken(People user) {
    return doGenerateToken(user.getFullName());
  }

  private String doGenerateToken(String subject) {

    Claims claims = Jwts.claims().setSubject(subject);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuer("residential-application")
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
        .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
        .compact();
  }

  public Boolean validateToken(String token) {
    token = token.replace("Bearer ", "");

    try {
      if (isTokenExpired(token)) {
        logger.info("Jwt Token has expired");
        return false;
      }

      final String fullName = getFullNameFromToken(token);
      People resident = residentialPropertyDataService.getResident(fullName);

      if (resident == null) {
        logger.info("No valid resident found from token");
        return false;
      }

      return true;
    } catch (ExpiredJwtException expiredJwtException) {
      logger.info(expiredJwtException.getMessage());
    }

    return false;
  }
}
