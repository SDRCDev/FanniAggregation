package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class QuestionModel {
	
	public String questionName;
	
	public Integer questionId;
	
	private String controllerType;

	private String fieldType;
	
	private String columnName;
	
	private String parentColumnName;
	
	private Integer typeId;
	
	private boolean isArea;
	
	private Integer formId;
	
	private String sector;
	
	private String subsector;
}
