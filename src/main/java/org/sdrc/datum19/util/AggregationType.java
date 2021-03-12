package org.sdrc.datum19.util;

public enum AggregationType {
	
	COUNT(1,"count@count"), 
	REPEATCOUNT(2,"repeatCount@count"),
	UNIQUECOUNT(3,"unique@count"),
	TOTALCOUNT(4,"total@count"),
	NUMBER(5, "number"),
	PERCENT(6, "percent"), 
	AVG(7, "avg");
 
    private Integer id;
    private String aggregationType;
 
    private AggregationType(int id, String aggregationType) {
        this.id = id;
        this.aggregationType = aggregationType;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}
    
}
