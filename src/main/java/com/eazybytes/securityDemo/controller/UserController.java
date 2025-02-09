package com.eazybytes.securityDemo.controller;

import com.eazybytes.securityDemo.ApplicationConstants;
import com.eazybytes.securityDemo.model.Customer;
import com.eazybytes.securityDemo.model.LoginRequestDTO;
import com.eazybytes.securityDemo.model.LoginResponseDTO;
import com.eazybytes.securityDemo.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment env;

    public UserController(CustomerRepository repository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, Environment env) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.env = env;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {

        try {
            customer.setPwd(passwordEncoder.encode(customer.getPwd()));
            repository.save(customer);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body("Exception occurred "+e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED.value()).body("user created");
    }

    @GetMapping("/user")
    public Customer getUserDetailsAfterLogin(Authentication authentication) {
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        Optional<Customer> optionalCustomer = repository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }

    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequestDTO){
        Authentication auth = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDTO.username(),loginRequestDTO.password());
        Authentication authentication = authenticationManager.authenticate(auth);

        String jwt = null;

        if(null!= authentication && authentication.isAuthenticated()) {
            // generate the JWT token here
            String user = authentication.getName();
            String grantedAuthorityList = authentication.getAuthorities().stream().map(ga ->ga.getAuthority())
                    .collect(Collectors.joining(ApplicationConstants.AUTHORITIES_STR_SEPARATOR));
            String key = env.getProperty(ApplicationConstants.JWT_SECRET_PROPERTY,ApplicationConstants.JWT_SECRET_DEFAULT);
            SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));

            Date expiryDt= new Date(new Date().getTime()+3600000);
            jwt = Jwts.builder().issuer("EazyBank").subject(user)
                    .claim(ApplicationConstants.USERNAME_CLAIM,authentication.getName())
                    .claim(ApplicationConstants.AUTHORITIES_CLAIM,grantedAuthorityList)
                    .issuedAt(new Date())
                    .expiration(expiryDt)
                    .signWith(secretKey)
                    .compact();
        }
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("OK",jwt);
        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER,jwt).body(loginResponseDTO);
    }

}
