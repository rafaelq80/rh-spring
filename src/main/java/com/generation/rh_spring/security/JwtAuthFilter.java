package com.generation.rh_spring.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		try {

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				token = authHeader.substring(7); // Remove o prefixo "Bearer "
				username = jwtService.extractUsername(token);
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}


			filterChain.doFilter(request, response);

		} catch (ExpiredJwtException e) {
			logUnauthorizedAccess(response, "Token expirado.", e);
		} catch (UnsupportedJwtException e) {
			logUnauthorizedAccess(response, "Token não suportado.", e);
		} catch (MalformedJwtException e) {
			logUnauthorizedAccess(response, "Token malformado.", e);
		} catch (SignatureException e) {
			logUnauthorizedAccess(response, "Assinatura inválida no token.", e);
		} catch (IllegalArgumentException e) {
			logUnauthorizedAccess(response, "Token JWT inválido ou ausente.", e);
		} catch (Exception e) {
			logUnauthorizedAccess(response, "Erro desconhecido ao processar o token.", e);
		}
	}

	private void logUnauthorizedAccess(HttpServletResponse response, String message, Exception e) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		System.err.println("Erro de autenticação: " + message);
	}
}
