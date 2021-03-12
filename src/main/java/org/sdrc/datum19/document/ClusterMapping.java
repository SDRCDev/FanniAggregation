package org.sdrc.datum19.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
@Document
public class ClusterMapping {

	@Id
	private String id;

	@DBRef
	private Area district;

	@DBRef
	private Area block;

	@DBRef
	private Area village;
	
	private Integer clusterNumber;
}
