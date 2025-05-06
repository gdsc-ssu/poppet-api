package com.gdg.poppet.global.service;

import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String key) throws UsernameNotFoundException {
        // key 형식: "{userId}#{provider}"
        String[] parts = key.split("#");
        String userId   = parts[0];
        Provider prov   = Provider.valueOf(parts[1]);

        User user = userRepository
                .findByUserIdAndProvider(userId, prov)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                key,
                "",
                Collections.emptyList()
        );
    }
}