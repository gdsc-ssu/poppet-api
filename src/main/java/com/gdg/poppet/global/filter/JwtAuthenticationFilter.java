package com.gdg.poppet.global.filter;

import com.gdg.poppet.auth.application.service.JwtService;
import com.gdg.poppet.global.service.CustomUserDetailsService;
import com.gdg.poppet.user.domain.enums.Provider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) 헤더에서 토큰 추출
        String token = jwtService
                .extractAccessToken(request)
                .orElse(null);

        if (token != null && jwtService.isTokenValid(token)) {
            // 2) 토큰에서 UserId(과 Provider) 꺼내고
            String userId = jwtService.extractUserId(token).orElseThrow();
            Provider provider = jwtService.extractProvider(token).orElseThrow();

            // 3) UserDetails 조회 후 Authentication 생성
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId + "#" + provider);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4) SecurityContext에 Authentication 등록
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}

