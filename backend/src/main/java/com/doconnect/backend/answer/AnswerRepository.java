package com.doconnect.backend.answer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

	List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

	List<Answer> findByQuestionId(Long questionId);

	List<Answer> findTop10ByOrderByCreatedAtDesc();

	@Query("""
			select a.author.id, count(a)
			from Answer a
			group by a.author.id
			""")
	List<Object[]> findAnswerCountsByAuthor();
}
