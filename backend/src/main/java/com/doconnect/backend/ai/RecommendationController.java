package com.doconnect.backend.ai;

import com.doconnect.backend.question.QuestionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	public RecommendationController(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}

	@PostMapping("/predict-tags")
	public TagPredictionResponse predictTags(@Valid @RequestBody TagPredictionRequest request) {
		return new TagPredictionResponse(recommendationService.predictTags(request.title(), request.description()));
	}

	@GetMapping("/similar/{questionId}")
	public List<QuestionResponse> similarQuestions(@PathVariable Long questionId) {
		return recommendationService.findSimilarQuestions(questionId);
	}

	@PostMapping("/search")
	public List<QuestionResponse> search(@Valid @RequestBody SearchRequest request) {
		return recommendationService.search(request.query());
	}

	public record TagPredictionRequest(
			@NotBlank @Size(max = 180) String title,
			@NotBlank @Size(max = 5000) String description
	) {
	}

	public record TagPredictionResponse(List<String> tags) {
	}

	public record SearchRequest(@NotBlank @Size(max = 500) String query) {
	}
}
