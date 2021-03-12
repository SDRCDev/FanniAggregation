package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class UserDatumValue {
	private String id;
	private String datumId;
	private Double dataValue;
	private Integer tp;
	private String _case;
	private Integer inid;
	private Double numerator;
	private Double denominator;
	private String datumtype;
}
