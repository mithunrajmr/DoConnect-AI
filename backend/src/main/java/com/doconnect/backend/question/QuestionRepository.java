package com.doconnect.backend.question;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

	List<Question> findAllByOrderByCreatedAtDesc();

	List<Question> findTop10ByOrderByCreatedAtDesc();

	@Query("""
			select t.displayName, count(q)
			from Question q
			join q.tags t
			group by t.id, t.displayName
			order by count(q) desc, t.displayName asc
			""")
	List<Object[]> findTagUsageCounts();

	@Query("""
			select q.author.id, count(q)
			from Question q
			group by q.author.id
			""")
	List<Object[]> findQuestionCountsByAuthor();

	@Modifying
	@Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :id")
	void incrementViewCount(@Param("id") Long id);
}
