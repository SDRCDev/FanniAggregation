package org.sdrc.datum19.util;

import lombok.Data;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Data
public class DataValue {
	private String _id;
	private Integer areaId;
	private Double dataValue;
	private Integer tp;
	private String _case;
	private Integer inid;
	private String unit;
	private String subgroup;
	
	public DataValue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DataValue(Integer areaId, Double dataValue, Integer tp,
			String _case, Integer inid, String unit, String subgroup) {
		super();
		this.areaId = areaId;
		this.dataValue = dataValue;
		this.tp = tp;
		this._case = _case;
		this.inid = inid;
		this.unit = unit;
		this.subgroup = subgroup;
	}
	
}
