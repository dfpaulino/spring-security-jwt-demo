package com.eazybytes.securityDemo.config;

import com.eazybytes.securityDemo.model.Authority;
import com.eazybytes.securityDemo.model.Customer;
import com.eazybytes.securityDemo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EazyBankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public EazyBankUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * @param email the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);
        Customer customer = customerOptional.orElseThrow(() ->
                new UsernameNotFoundException("User details not found for user " + email));

        //List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(customer.getRole()));
        List<GrantedAuthority> authorities = customer.getAuthorities().stream()
                .map(authority ->new SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());

        UserDetails user = User.withUsername(customer.getEmail())
                .password(customer.getPwd()).authorities(authorities).build();
        return user;
    }
}
