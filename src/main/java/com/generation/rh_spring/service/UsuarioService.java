package com.generation.rh_spring.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.Usuario;
import com.generation.rh_spring.model.UsuarioLogin;
import com.generation.rh_spring.repository.UsuarioRepository;
import com.generation.rh_spring.security.JwtService;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<Usuario> getAll() {
		return usuarioRepository.findAll();
	}

	public Optional<Usuario> getById(Long id) {
		return usuarioRepository.findById(id);
	}

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
			return Optional.empty();
		}

		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		usuario.setId(null);
		
		return Optional.of(usuarioRepository.save(usuario));
	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {

		if (!usuarioRepository.findById(usuario.getId()).isPresent()) {
			return Optional.empty();
		}

		Optional<Usuario> usuarioExistente = usuarioRepository.findByUsuario(usuario.getUsuario());
		
		if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
		}

		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		return Optional.of(usuarioRepository.save(usuario));
	}
	
	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

		if (!usuarioLogin.isPresent()) {
			return Optional.empty();
		}

		UsuarioLogin login = usuarioLogin.get();
		
		try {
 
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(login.getUsuario(), login.getSenha()));

			return usuarioRepository.findByUsuario(login.getUsuario())
				.map(usuario -> construirRespostaLogin(login, usuario));

		} catch (Exception e) {

			return Optional.empty();
		}
	}

	private UsuarioLogin construirRespostaLogin(UsuarioLogin usuarioLogin, Usuario usuario) {
		
		usuarioLogin.setId(usuario.getId());
		usuarioLogin.setNome(usuario.getNome());
		usuarioLogin.setFoto(usuario.getFoto());
		usuarioLogin.setSenha("");
		usuarioLogin.setToken(gerarToken(usuario.getUsuario()));
		return usuarioLogin;
		
	}

	private String gerarToken(String usuario) {
		return "Bearer " + jwtService.generateToken(usuario);
	}
}