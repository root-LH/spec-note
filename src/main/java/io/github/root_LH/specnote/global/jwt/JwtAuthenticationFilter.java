package io.github.root_LH.specnote.global.jwt;

import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 Authorization: Bearer {토큰} 헤더를 읽어 인증 정보를 등록하는 필터.
 * 토큰이 없거나 잘못돼도 여기서 바로 막지 않고 다음 필터로 넘김.
 * → 인증이 필요한 API라면 Security가 JwtAuthenticationEntryPoint를 호출해 401 응답.
 *
 * @Component를 붙이지 않음: 붙이면 서블릿 필터로도 한 번 더 등록돼 두 번 실행됨.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 실패 원인을 EntryPoint에 전달하기 위한 request attribute 이름 */
    public static final String EXCEPTION_ATTRIBUTE = "jwtException";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenProvider.parse(token);
                if (jwtTokenProvider.isAccessToken(claims)) {
                    SecurityContextHolder.getContext()
                            .setAuthentication(jwtTokenProvider.getAuthentication(claims));
                } else {
                    request.setAttribute(EXCEPTION_ATTRIBUTE, ErrorCode.INVALID_TOKEN);
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute(EXCEPTION_ATTRIBUTE, ErrorCode.EXPIRED_TOKEN);
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(EXCEPTION_ATTRIBUTE, ErrorCode.INVALID_TOKEN);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
