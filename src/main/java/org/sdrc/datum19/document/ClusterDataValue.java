package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class ClusterDataValue {

	private String id;
	private Integer areaId;
	private Double dataValue;
	private Integer tp;
	private String _case;
	private Integer inid;
	private Double numerator;
	private Double denominator;
}
