package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class IndicatorMapModel {

	
	private String indicatorNid;
	private String indicatorName;
	private String parentColumn;
	private String unit;
	private String subgroup;
	private String numerator;
	private String denominator;
	private String aggregationType;
	private String parentType;	
	private String formId;
	private String periodicity;
	private String aggregationRule;
	private String highIsGood;
	private String typeDetailId;	
	private String area;
	private String collection;
	private String sector;
	private String subsector;
	
	
}
