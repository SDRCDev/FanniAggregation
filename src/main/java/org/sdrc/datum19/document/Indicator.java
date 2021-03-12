package org.sdrc.datum19.document;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Indicator {
	@Id
	private String id;
	/*private Integer indicatorNid;
	private String indicatorName;
	private String parentColumn;
	private Integer numeratorIndex;
	private Integer denominatorIndex;
	private String numerator;
	private String denominator;
	private String aggregationType;
	private String parentType;
	private Integer formId;
	private Integer periodicity;
	private Integer typeId;
	private Integer typeDetailId;
	private String fieldType;
	private String area;
	private String collection;
	private String unit;*/
	private Map<String, Object> indicatorDataMap;
}
