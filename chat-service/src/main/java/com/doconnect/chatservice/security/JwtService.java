package com.doconnect.chatservice.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Validates HS256 JWTs produced by the main DoConnect service.
 */
@Service
public class JwtService {

	private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final Clock clock;
	private final ObjectMapper objectMapper;
	private final byte[] secret;

	@Autowired
	public JwtService(@Value("${app.jwt.secret}") String secret, ObjectMapper objectMapper) {
		this(Clock.systemUTC(), secret, objectMapper);
	}

	JwtService(Clock clock, String secret, ObjectMapper objectMapper) {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
		}
		this.clock = clock;
		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
	}

	public Optional<ChatPrincipal> authenticate(String token) {
		return parsePayload(token)
				.filter(payload -> hasValidSignature(token))
				.filter(payload -> Instant.now(clock).getEpochSecond() < asLong(payload.get("exp")))
				.map(this::toPrincipal);
	}

	private Optional<Map<String, Object>> parsePayload(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return Optional.empty();
			}
			return Optional.of(objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), MAP_TYPE));
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	private boolean hasValidSignature(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			return false;
		}
		return sign(parts[0] + "." + parts[1]).equals(parts[2]);
	}

	private String sign(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to validate JWT signature", ex);
		}
	}

	private ChatPrincipal toPrincipal(Map<String, Object> payload) {
		Long userId = payload.containsKey("userId") ? asLong(payload.get("userId")) : null;
		String username = String.valueOf(payload.getOrDefault("name", payload.get("sub")));
		String role = String.valueOf(payload.getOrDefault("role", "USER"));
		return new ChatPrincipal(userId, username, role);
	}

	private long asLong(Object value) {
		if (value instanceof Number number) {
			return number.longValue();
		}
		return Long.parseLong(String.valueOf(value));
	}
}
