package com.doconnect.backend.question;

import com.doconnect.backend.tag.Tag;
import com.doconnect.backend.tag.TagRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TagService {

	private final TagRepository tagRepository;

	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	public Set<Tag> resolveTags(List<String> rawTags) {
		Set<Tag> tags = new LinkedHashSet<>();
		if (rawTags == null) {
			return tags;
		}

		rawTags.stream()
				.map(this::normalize)
				.filter(tag -> !tag.isBlank())
				.distinct()
				.limit(8)
				.forEach(name -> {
					Tag tag = tagRepository.findByName(name).orElseGet(() -> createTag(name));
					tag.incrementUsageCount();
					tags.add(tagRepository.save(tag));
				});
		return tags;
	}

	private Tag createTag(String name) {
		Tag tag = new Tag();
		tag.setName(name);
		tag.setDisplayName(name);
		return tag;
	}

	private String normalize(String tag) {
		return tag.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
	}
}
