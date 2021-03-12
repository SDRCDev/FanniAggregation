package org.sdrc.datum19.document;

import org.sdrc.datum19.util.Status;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in) Created Date:16-Aug-2018 10:47:35 AM
 */
@Document
@Data
public class EnginesForm {

	@Id
	private String id;

	private Integer formId;

	private String name;

	private String dbName;
	
	private Status status=Status.ACTIVE;
	
	private Integer version;
}