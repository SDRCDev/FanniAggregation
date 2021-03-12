package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class IndicatorConfigModel {
	
	private String indicatorName;
	
	private Integer formId;
	
	private String questionName;
	
	private Integer questionId;
	
	private String questionColumnName;
	
	private String periodicity;
	
	private boolean highIsGood;
	
	private String aggregationType;
	
	private String aggregationRule;
	
	private String areaColumn;
	
	private List<Integer> typeDetails;
	
	private String controllerType;
	
	private String parentColumnName;
	
	private List<ConditionModel> conditions;
	
	private Integer numerator;
	
	private Integer denominator;
	
	private String sector;
	
	private String subsector;
	
	private String subgroup;
	
	private String unit;
	
}
