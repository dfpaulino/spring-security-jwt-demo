package com.eazybytes.securityDemo.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class RequestValidationBeforeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse rsp = (HttpServletResponse) response;

        String authheader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if(authheader !=null) {
            authheader=authheader.trim();
            String username;
            if(StringUtils.startsWithIgnoreCase(authheader,"Basic ")){
                try{
                    byte[] Base64EncodedString = authheader.substring(6).getBytes(StandardCharsets.UTF_8);
                    username= new String(Base64.getDecoder().decode(Base64EncodedString)).split(":")[0];
                }catch (IllegalArgumentException e){
                    throw new BadCredentialsException("Invalid Basic Authentication Token ");
                }

                if(username!=null && username.contains("test")) {
                    rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        }
        chain.doFilter(request,response);

    }
}
