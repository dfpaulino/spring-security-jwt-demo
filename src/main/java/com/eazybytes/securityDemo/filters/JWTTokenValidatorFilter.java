package com.eazybytes.securityDemo.filters;

import com.eazybytes.securityDemo.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwtHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
        if (null != jwtHeader && jwtHeader.startsWith("Bearer")) {
            try {
                //decode JWT
                jwtHeader = jwtHeader.substring(7);
                Environment env = getEnvironment();
                String key = env.getProperty(ApplicationConstants.JWT_SECRET_PROPERTY,ApplicationConstants.JWT_SECRET_DEFAULT);
                SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwtHeader).getPayload();

                // get userName from claims
                String userName =claims.getSubject();

                //Get Authorities from claims
                List<GrantedAuthority> authorities = Arrays.stream(claims.get(ApplicationConstants.AUTHORITIES_CLAIM,String.class)
                                .split(ApplicationConstants.AUTHORITIES_STR_SEPARATOR))
                                .map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableList());


                Authentication authentication = new UsernamePasswordAuthenticationToken(userName,null,authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                throw new BadCredentialsException("invalid Token received");
            }
        }
        //this is not a login request, then we must validate JWT
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Do not validate whe login operation. we have to skip validation on this case
        return request.getServletPath().equals("/user");
    }
}
