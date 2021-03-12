package org.sdrc.datum19.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class CustomAggregationOperation implements AggregationOperation {

	private String jsonOperation;

    public CustomAggregationOperation(String jsonOperation) {
        this.jsonOperation = jsonOperation;
    }
    
	@Override
	public Document toDocument(AggregationOperationContext aggregationOperationContext) {
		// TODO Auto-generated method stub
		return aggregationOperationContext.getMappedObject(Document.parse(jsonOperation));
	}

}
