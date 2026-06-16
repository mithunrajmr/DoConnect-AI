package com.doconnect.backend.auth;

import com.doconnect.backend.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final Clock clock;
	private final long expirationMinutes;
	private final ObjectMapper objectMapper;
	private final byte[] secret;

	@Autowired
	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-minutes}") long expirationMinutes,
			ObjectMapper objectMapper
	) {
		this(Clock.systemUTC(), secret, expirationMinutes, objectMapper);
	}

	JwtService(Clock clock, String secret, long expirationMinutes, ObjectMapper objectMapper) {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
		}
		this.clock = clock;
		this.expirationMinutes = expirationMinutes;
		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
	}

	public String generateToken(User user) {
		Instant now = Instant.now(clock);
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getEmail());
		payload.put("userId", user.getId());
		payload.put("name", user.getName());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());

		String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
		return unsignedToken + "." + sign(unsignedToken);
	}

	public Optional<String> extractSubject(String token) {
		return parsePayload(token).map(payload -> payload.get("sub")).map(String::valueOf);
	}

	public boolean isTokenValid(String token, String expectedSubject) {
		Optional<Map<String, Object>> payload = parsePayload(token);
		return payload
				.filter(claims -> expectedSubject.equals(claims.get("sub")))
				.filter(claims -> Instant.now(clock).getEpochSecond() < asLong(claims.get("exp")))
				.filter(claims -> hasValidSignature(token))
				.isPresent();
	}

	private Optional<Map<String, Object>> parsePayload(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return Optional.empty();
			}
			byte[] payload = BASE64_URL_DECODER.decode(parts[1]);
			return Optional.of(objectMapper.readValue(payload, MAP_TYPE));
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

	private String encodeJson(Map<String, Object> data) {
		try {
			return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(data));
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to encode JWT", ex);
		}
	}

	private String sign(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to sign JWT", ex);
		}
	}

	private long asLong(Object value) {
		if (value instanceof Number number) {
			return number.longValue();
		}
		return Long.parseLong(String.valueOf(value));
	}
}
