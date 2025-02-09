package com.eazybytes.securityDemo.filters;

import com.eazybytes.securityDemo.ApplicationConstants;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This filter is executed after the BasicAuthenticationFilter, therfore we have
 * the Authentication object already in the security Context.
 * This Authentication object has already the authorities
 */
public class JWTTokenGenerator extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            String user = authentication.getName();
            String grantedAuthorityList = authentication.getAuthorities().stream().map(ga ->ga.getAuthority())
                    .collect(Collectors.joining(ApplicationConstants.AUTHORITIES_STR_SEPARATOR));
            // key to sign the Token
            Environment env = getEnvironment();
            String key = env.getProperty(ApplicationConstants.JWT_SECRET_PROPERTY,ApplicationConstants.JWT_SECRET_DEFAULT);
            SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));


            Date expiryDt= new Date(new Date().getTime()+3600000);
            String JWT = Jwts.builder().issuer("EazyBank").subject(user)
                    .claim(ApplicationConstants.USERNAME_CLAIM,authentication.getName())
                    .claim(ApplicationConstants.AUTHORITIES_CLAIM,grantedAuthorityList)
                    .issuedAt(new Date())
                    .expiration(expiryDt)
                    .signWith(secretKey)
                            .compact();

            response.setHeader(ApplicationConstants.JWT_HEADER,"Bearer "+JWT);
        }

        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // generate the JWT when invoke login operation only, skip otherwise
        // so should not filter when path is NOT login path (/user)
        return !request.getServletPath().equals("/user");
    }
}
