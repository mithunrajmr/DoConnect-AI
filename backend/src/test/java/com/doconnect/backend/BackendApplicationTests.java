package com.doconnect.backend;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doconnect.backend.ai.GeminiClient;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import com.doconnect.backend.user.UserRole;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@MockBean
	private GeminiClient geminiClient;

	@Test
	void registerLoginAndReadCurrentUser() throws Exception {
		String token = registerAndGetToken("Mithun", "mithun-auth@example.com");

		mockMvc.perform(get("/api/auth/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("mithun-auth@example.com"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "mithun-auth@example.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", not(blankOrNullString())));
	}

	@Test
	void rejectsProtectedEndpointWithoutJwt() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isForbidden());
	}

	@Test
	void questionAndAnswerCrudHonorsOwnership() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "owner@example.com");
		String otherToken = registerAndGetToken("Other", "other@example.com");

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "How do I secure a Spring Boot API?",
								  "body": "I need JWT based authentication with roles for my capstone project.",
								  "tags": ["Spring Boot", "JWT", "Security"]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("How do I secure a Spring Boot API?"))
				.andExpect(jsonPath("$.tags", hasItem("jwt")))
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		mockMvc.perform(get("/api/questions/" + questionId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answerCount").value(0));

		mockMvc.perform(put("/api/questions/" + questionId)
						.header("Authorization", "Bearer " + otherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Attempted edit by another user",
								  "body": "This update should be rejected because the user is not the owner.",
								  "tags": ["security"]
								}
								"""))
				.andExpect(status().isForbidden());

		String answerResponse = mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + otherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Use Spring Security, BCrypt, and a stateless JWT filter."
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.questionId").value(Integer.parseInt(questionId)))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String answerId = extractJsonNumber(answerResponse, "id");

		mockMvc.perform(get("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(Integer.parseInt(answerId)));

		mockMvc.perform(put("/api/answers/" + answerId)
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "The question owner cannot edit another user's answer."
								}
								"""))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/answers/" + answerId)
						.header("Authorization", "Bearer " + otherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Use Spring Security with BCrypt and a stateless JWT authentication filter."
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.body").value("Use Spring Security with BCrypt and a stateless JWT authentication filter."));

		mockMvc.perform(delete("/api/answers/" + answerId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/answers/" + answerId)
						.header("Authorization", "Bearer " + otherToken))
				.andExpect(status().isNoContent());
	}

	@Test
	void adminCanDeleteAnyQuestion() throws Exception {
		String ownerToken = registerAndGetToken("Question Owner", "question-owner@example.com");
		String adminToken = createAdminAndLogin("admin@example.com");

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Can admins moderate questions?",
								  "body": "I need to verify that admins can remove content they do not own.",
								  "tags": ["admin", "moderation"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		mockMvc.perform(delete("/api/questions/" + questionId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/questions/" + questionId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void validatesQuestionAndAnswerPayloads() throws Exception {
		String token = registerAndGetToken("Validator", "validator@example.com");

		mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "",
								  "body": "short"
								}
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/questions/99999/answers")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "This question does not exist."
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void questionOwnerCanAcceptAnswerAndQuestionBecomesSolved() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "accept-owner@example.com");
		String helperToken = registerAndGetToken("Helper", "accept-helper@example.com");

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "How do I mark a solved question?",
								  "body": "I want to accept one answer.",
								  "tags": ["java"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		String answerResponse = mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + helperToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Use the accept endpoint."
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String answerId = extractJsonNumber(answerResponse, "id");

		mockMvc.perform(post("/api/questions/" + questionId + "/accept/" + answerId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLVED"))
				.andExpect(jsonPath("$.acceptedAnswerId").value(Integer.parseInt(answerId)));

		mockMvc.perform(get("/api/questions/" + questionId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLVED"))
				.andExpect(jsonPath("$.acceptedAnswerId").value(Integer.parseInt(answerId)));

		mockMvc.perform(get("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].accepted").value(true));
	}

	@Test
	void adminCanAcceptAnyAnswer() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "accept-admin-owner@example.com");
		String helperToken = registerAndGetToken("Helper", "accept-admin-helper@example.com");
		String adminToken = createAdminAndLogin("accept-admin@example.com");

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Can admin accept answers?",
								  "body": "Testing admin permissions.",
								  "tags": ["admin"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		String answerResponse = mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + helperToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Admins should be allowed."
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String answerId = extractJsonNumber(answerResponse, "id");

		mockMvc.perform(post("/api/questions/" + questionId + "/accept/" + answerId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLVED"))
				.andExpect(jsonPath("$.acceptedAnswerId").value(Integer.parseInt(answerId)));
	}

	@Test
	void otherUsersCannotAcceptAnswer() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "accept-forbidden-owner@example.com");
		String helperToken = registerAndGetToken("Helper", "accept-forbidden-helper@example.com");
		String otherToken = registerAndGetToken("Other", "accept-forbidden-other@example.com");

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Who can accept answers?",
								  "body": "Testing permission checks.",
								  "tags": ["security"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		String answerResponse = mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + helperToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Only owner or admin should accept this."
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String answerId = extractJsonNumber(answerResponse, "id");

		mockMvc.perform(post("/api/questions/" + questionId + "/accept/" + answerId)
						.header("Authorization", "Bearer " + otherToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void answerMustBelongToQuestionToBeAccepted() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "accept-mismatch-owner@example.com");
		String helperToken = registerAndGetToken("Helper", "accept-mismatch-helper@example.com");

		String firstQuestionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "First question",
								  "body": "This owns no accepted answer yet.",
								  "tags": ["one"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String secondQuestionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Second question",
								  "body": "Its answer should not be accepted on another question.",
								  "tags": ["two"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String firstQuestionId = extractJsonNumber(firstQuestionResponse, "id");
		String secondQuestionId = extractJsonNumber(secondQuestionResponse, "id");

		String answerResponse = mockMvc.perform(post("/api/questions/" + secondQuestionId + "/answers")
						.header("Authorization", "Bearer " + helperToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "This answer belongs to the second question only."
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String answerId = extractJsonNumber(answerResponse, "id");

		mockMvc.perform(post("/api/questions/" + firstQuestionId + "/accept/" + answerId)
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void moderationAndAnalyticsEndpointsReturnExpectedPayloads() throws Exception {
		when(geminiClient.generateText(anyString()))
				.thenReturn("""
						{
						  "toxic": false,
						  "spam": false,
						  "score": 0.12,
						  "reason": "Content appears safe."
						}
						""")
				.thenReturn("""
						{
						  "toxic": false,
						  "spam": false,
						  "score": 0.09,
						  "reason": "Question looks safe and technical."
						}
						""")
				.thenReturn("""
						{
						  "overallSentiment": "neutral",
						  "positiveScore": 0.25,
						  "neutralScore": 0.65,
						  "negativeScore": 0.10,
						  "summary": "Recent content is mostly neutral and solution-oriented."
						}
						""");

		String token = registerAndGetToken("Analytics User", "analytics-user@example.com");
		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "How do I validate JWT tokens?",
								  "body": "I need help validating JWTs in Spring Security.",
								  "tags": ["JWT", "Security"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "Use a JWT filter and validate expiration and signature."
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/ai/moderation/check")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "content": "Can someone explain JWT expiration handling?"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.toxic").value(false))
				.andExpect(jsonPath("$.spam").value(false))
				.andExpect(jsonPath("$.score").value(0.12));

		mockMvc.perform(post("/api/ai/moderation/question/" + questionId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reason").value("Question looks safe and technical."));

		mockMvc.perform(get("/api/analytics/overview")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalUsers").value(greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.totalQuestions").value(greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.totalAnswers").value(greaterThanOrEqualTo(1)));

		mockMvc.perform(get("/api/analytics/tags")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tag").isNotEmpty())
				.andExpect(jsonPath("$[0].usageCount").value(greaterThanOrEqualTo(1)));

		mockMvc.perform(get("/api/analytics/activity")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").isNotEmpty())
				.andExpect(jsonPath("$[0].totalActivity").value(greaterThanOrEqualTo(1)));

		mockMvc.perform(get("/api/analytics/sentiment")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.overallSentiment").value("neutral"));
	}

	@Test
	void notificationsSupportAnswerAndChatFlows() throws Exception {
		String ownerToken = registerAndGetToken("Owner", "notify-owner@example.com");
		String helperToken = registerAndGetToken("Helper", "notify-helper@example.com");
		registerAndGetToken("Observer", "notify-observer@example.com");
		Long helperUserId = userRepository.findByEmail("notify-helper@example.com").orElseThrow().getId();

		String questionResponse = mockMvc.perform(post("/api/questions")
						.header("Authorization", "Bearer " + ownerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "How do notifications work?",
								  "body": "Testing answer notifications.",
								  "tags": ["notifications"]
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String questionId = extractJsonNumber(questionResponse, "id");

		mockMvc.perform(post("/api/questions/" + questionId + "/answers")
						.header("Authorization", "Bearer " + helperToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "body": "The owner should receive a notification."
								}
								"""))
				.andExpect(status().isCreated());

		String answerNotificationResponse = mockMvc.perform(get("/api/notifications")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].type").value("ANSWER"))
				.andExpect(jsonPath("$[0].read").value(false))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String notificationId = extractJsonNumber(answerNotificationResponse, "id");

		mockMvc.perform(get("/api/notifications/unread-count")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unreadCount").value(1));

		mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.read").value(true));

		mockMvc.perform(get("/api/notifications/unread-count")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unreadCount").value(0));

		mockMvc.perform(post("/internal/notifications/chat-message")
						.header("X-Internal-Token", "test-notification-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "senderId": %d,
								  "senderName": "Helper",
								  "messageId": 500,
								  "content": "Hello from global chat",
								  "roomId": "global"
								}
								""".formatted(helperUserId)))
				.andExpect(status().isAccepted());

		mockMvc.perform(get("/api/notifications")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].type").value("CHAT"))
				.andExpect(jsonPath("$[0].occurrenceCount").value(1));

		mockMvc.perform(get("/api/notifications")
						.header("Authorization", "Bearer " + helperToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(put("/api/notifications/read-all")
						.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unreadCount").value(0));
	}

	private String registerAndGetToken(String name, String email) throws Exception {
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "%s",
								  "email": "%s",
								  "password": "password123"
								}
								""".formatted(name, email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token", not(blankOrNullString())))
				.andReturn()
				.getResponse()
				.getContentAsString();

		return extractJsonString(response, "token");
	}

	private String createAdminAndLogin(String email) throws Exception {
		User admin = new User();
		admin.setName("Admin");
		admin.setEmail(email);
		admin.setPasswordHash(passwordEncoder.encode("password123"));
		admin.setRole(UserRole.ADMIN);
		userRepository.save(admin);

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "password123"
								}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return extractJsonString(response, "token");
	}

	private String extractJsonString(String json, String field) {
		return json.replaceAll(".*?\\\"" + field + "\\\":\\\"([^\\\"]+)\\\".*", "$1");
	}

	private String extractJsonNumber(String json, String field) {
		return json.replaceAll(".*?\\\"" + field + "\\\":([0-9]+).*", "$1");
	}
}
