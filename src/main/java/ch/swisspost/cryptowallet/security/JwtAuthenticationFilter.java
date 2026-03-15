package ch.swisspost.cryptowallet.security;

import ch.swisspost.cryptowallet.dtos.ResponseObject;
import ch.swisspost.cryptowallet.entities.Token;
import ch.swisspost.cryptowallet.repositories.TokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            final String username = jwtService.extractUsername(jwt);
            final String jti = jwtService.extractClaim(jwt, Claims::getId);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                boolean isRevoked = tokenRepository.findById(jti)
                        .map(Token::isRevoked)
                        .orElse(true);

                if (jwtService.isTokenValid(jwt, userDetails) && !isRevoked) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Token [{}] is revoked or invalid for user: {}", jti, username);
                    handleException(response, "Token has been revoked or is invalid", HttpStatus.UNAUTHORIZED);
                    return;
                }
            }
            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            handleException(response, "Unauthorized: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("Authentication filter error: ", ex);
            handleException(response, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ResponseObject<?> errorResponse = ResponseObject.builder()
                .status(ResponseObject.ResponseStatus.FAILED)
                .message(message)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}