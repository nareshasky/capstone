//package com.ecom.filter;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private static final String SECRET = "my-secret-key-which-is-very-secure-and-at-least-32-bytes";
//
//        @Override
//        protected void doFilterInternal(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        FilterChain filterChain)
//                throws ServletException, IOException {
//
//            String header = request.getHeader("Authorization");
//
//            if (header != null && header.startsWith("Bearer ")) {
//
//                String token = header.substring(7);
//
//                Claims claims = Jwts.parserBuilder()
//                        .setSigningKey(getSigningKey())
//                        .build()
//                        .parseClaimsJws(token)
//                        .getBody();
//
//                @SuppressWarnings("unchecked")
//                List<String> roles = (List<String>) claims.get("roles");
//
//                List<GrantedAuthority> authorities = roles.stream()
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());
//
//                Authentication authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                claims.getSubject(),
//                                null,
//                                authorities
//                        );
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//
//            filterChain.doFilter(request, response);
//        }
//
//        private Key getSigningKey() {
//            return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
//        }
//    }
