package org.sdrc.datum19.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author Biswabhusan Pradhan
 *
 */

@Data
@Document
public class ClusterForAggregation {
	@Id
	private String id;

	private Integer district;

	private Integer block;

	private Integer village;
	
	private Integer clusterNumber;
}
