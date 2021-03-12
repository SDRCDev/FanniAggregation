package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class FormAttachmentsModel {

	private String columnName;

	private String filePath;

	private String originalName;

	private Long fileSize;

	private String submissionId;
	
	private Integer formId;
	
	private String fileExtension;

}
