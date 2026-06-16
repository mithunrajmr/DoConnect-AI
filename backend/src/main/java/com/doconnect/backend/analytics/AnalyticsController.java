package com.doconnect.backend.analytics;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/overview")
	public OverviewAnalyticsResponse overview() {
		return analyticsService.overview();
	}

	@GetMapping("/tags")
	public List<TagAnalyticsResponse> tags() {
		return analyticsService.tags();
	}

	@GetMapping("/activity")
	public List<UserActivityResponse> activity() {
		return analyticsService.activity();
	}

	@GetMapping("/sentiment")
	public SentimentSummaryResponse sentiment() {
		return analyticsService.sentiment();
	}
}

record OverviewAnalyticsResponse(
		long totalUsers,
		long totalQuestions,
		long totalAnswers
) {
}

record TagAnalyticsResponse(
		String tag,
		long usageCount
) {
}

record UserActivityResponse(
		Long userId,
		String name,
		String email,
		long questionCount,
		long answerCount,
		long totalActivity
) {
}

record SentimentSummaryResponse(
		int analyzedItems,
		String overallSentiment,
		double positiveScore,
		double neutralScore,
		double negativeScore,
		String summary
) {
}
