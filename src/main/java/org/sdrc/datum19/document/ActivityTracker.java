package org.sdrc.datum19.document;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document
public class ActivityTracker {
	@Id
	private String id;

	private Integer submissionKey;

	private String userName;

	private String name;

	private String emailId;

	private Integer userId;

	private Date createdDate;

	private Date updatedDate;

	private Date syncDate;

	private Map<String, Object> data;

	private Integer formId;

	private String uniqueId;

	@Field
	private boolean rejected = false;

	@Field
	private String rejectMessage;

	private String uniqueName;

	private TimePeriod timePeriod;
	
	private Boolean isAggregated;
	
	private Boolean isValid;
}
