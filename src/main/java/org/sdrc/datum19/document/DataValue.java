package org.sdrc.datum19.document;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class DataValue implements Serializable {

	private static final long serialVersionUID = -1636923412521819247L;
	private String id;
	private Integer datumId;
	private Double dataValue;
	private Integer tp;
	private String _case;
	private Integer inid;
	private Double numerator;
	private Double denominator;
	private String datumtype;
	private String _class;
}
