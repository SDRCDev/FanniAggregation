package org.sdrc.datum19.document;

import java.util.HashMap;
import java.util.Map;

import org.sdrc.datum19.model.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in) Created Date:26-Jun-2018 2:22:51 PM
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Question {

	@Id
	private String id;

	private Integer slugId;

	private Integer formId;

	private Integer questionId;

	private String section;

	private String subsection;

	private String question;

	private String columnName;

	private String controllerType;

	private String parentColumnName;

	private String fieldType;

	private Type typeId;

	private String repeatSubSection;

	@Indexed
	private Integer questionOrder;

	private String relevance;

	private String constraints;

	private String features;

	private String defaultSettings;

	private String tableName;

	private String defaultValue;

	private Boolean active = true;

	private Boolean mask;

	private Boolean saveMandatory = false;

	private Boolean finalizeMandatory = false;

	private Boolean displayScore = false;

	private String query;

	private String reviewHeader;

	private String fileExtensions;

	private String scoreExp;

	private Integer formVersion = 0;

	private String cmsg;
	
	private Map<Language, String> languages = new HashMap<>();

}
