package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Question;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * @author Ashutosh Dang(ashutosh@sdrc.co.in) Created Date : 26-Jun-2018 8:38:34 pm
 *        
 */

public interface QuestionRepository extends MongoRepository<Question, String> {

		List<Question> findAllByOrderByQuestionOrderAsc();

		List<Question> findAllByFormIdOrderByQuestionOrderAsc(Integer formId);

		List<Question> findAllByFormIdAndQuestionIdInOrderByQuestionOrderAsc(Integer formId, List<Integer> questionIds);

}
