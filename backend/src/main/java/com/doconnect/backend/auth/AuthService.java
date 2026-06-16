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
import org.springframework.stereotype.Service;

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

		return new AuthResponse(jwtService.generateToken(savedUser), UserResponse.from(savedUser));
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password())
			);
		} catch (AuthenticationException ex) {
			throw new BadCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
	}

	@Transactional
	public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
		currentUser.setName(request.name().trim());
		if (request.password() != null && !request.password().isBlank()) {
			currentUser.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		return UserResponse.from(userRepository.save(currentUser));
	}
}
