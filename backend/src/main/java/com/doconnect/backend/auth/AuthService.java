package com.doconnect.backend.auth;

import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import com.doconnect.backend.user.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	public AuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			PasswordEncoder passwordEncoder,
			UserRepository userRepository
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateEmailException("Email is already registered");
		}

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(UserRole.USER);
		User savedUser = userRepository.save(user);

		log.info("User registered successfully. userId={}, email={}", savedUser.getId(), savedUser.getEmail());

		return new AuthResponse(jwtService.generateToken(savedUser), UserResponse.from(savedUser));
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password())
			);
		} catch (AuthenticationException ex) {
			log.warn("Invalid login attempt. email={}", email);
			throw new BadCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Invalid login attempt. email={}", email);
					return new BadCredentialsException("Invalid email or password");
				});
		
		log.info("User logged in successfully. userId={}, email={}", user.getId(), user.getEmail());
		return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
	}

	@Transactional
	public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
		currentUser.setName(request.name().trim());
		if (request.password() != null && !request.password().isBlank()) {
			currentUser.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		User savedUser = userRepository.save(currentUser);
		log.info("User profile updated. userId={}", savedUser.getId());
		return UserResponse.from(savedUser);
	}
}
