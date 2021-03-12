package org.sdrc.datum19.document;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.model.FormAttachmentsModel;
import org.sdrc.datum19.model.SubmissionStatus;
import org.sdrc.datum19.usermgmt.Account;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author subham
 *
 */
@Document
@Data
public class CFInputFormData {

	@Id
	private String id;

	private String userName;

	private String userId;

	private Date createdDate;

	private Date updatedDate;

	private Date syncDate;

	private Map<String, Object> data;

	private Integer formId;

	private String uniqueId;

	private boolean rejected = false;

	private String rejectMessage;

	private TimePeriod timePeriod;

	private Boolean isAggregated = false;

	private Boolean isValid;

	private Integer attachmentCount = 0;

	Map<String, List<FormAttachmentsModel>> attachments;

	private SubmissionStatus submissionCompleteStatus = SubmissionStatus.PC;

	@DBRef
	private Account rejectedBy;

	private Date rejectedDate;

	private SubmissionStatus submissionStatus;

	private String uniqueName;
	
	private Boolean isReSubmitted=false;
	
	private Integer version;

}
