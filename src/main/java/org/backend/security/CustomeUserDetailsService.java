package org.backend.security;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.model.Users;
import org.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details based on the provided id (mobile number). It retrieves the user from the database using the UserRepository.
     * If the user is found, it constructs a UserDetails object containing the user's mobile number, password, and authorities (roles).
     * The authorities are derived from the user's role and prefixed with "ROLE_". If the user is not found, it throws a UsernameNotFoundException.
     * This method is essential for Spring Security to authenticate users and manage their roles and permissions within the application.
     * @param id
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Long userId = Long.valueOf(id);
        log.info("Attempting to load user by userId: {}", userId);
        Users users = userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                users.getMobile(),
                users.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_"+users.getRole().name()))
        );
    }
}
