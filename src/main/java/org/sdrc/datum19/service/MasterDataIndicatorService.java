package org.sdrc.datum19.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class MasterDataIndicatorService {
	
	public Aggregation aggregateCountData(Integer formId, String groupKeys, Integer timePeriodId, String conditions) {
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data").and("rejected").as("rejected").and("uniqueId").as("uniqueId");
		List<String> condarr=new ArrayList<>();
		if(!conditions.equals(null))
			condarr=Arrays.asList(conditions.split(";"));
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId);
		Criteria criteriaQuery=new Criteria();
		if(!condarr.isEmpty()) {
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.lte(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$eq":
//				if(i==0)
//					criteriaQuery=Criteria.where(expression.split(":")[0]).is(expression.split(":")[1]);
//				else
//					criteriaQuery=criteriaQuery.is(Integer.parseInt(expression.split(":")[1]));
				switch (expression.split(":")[2]) {
				case "boolean":
					matchCriteria.and(expression.split(":")[0]).is(Boolean.parseBoolean(expression.split(":")[1]));
					break;
				case "number":
					matchCriteria.and(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1]));
					break;

				default:
					matchCriteria.and(expression.split(":")[0]).is(expression.split(":")[1]);
					break;
				}
				break;
			case "and$gte":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.gte(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$gt":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.gt(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$lt":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.lt(Integer.parseInt(expression.split(":")[1]));
				break;

			default:
				break;
			}
		}
		}
//		matchCriteria.andOperator(andcriterias.toArray(new Criteria[andcriterias.size()]));
		MatchOperation mp1=Aggregation.match(matchCriteria);
		GroupOperation groupop=Aggregation.group(groupKeys.split(",")).sum(when(criteriaQuery).then(1).otherwise(0)).as("_value");
		GroupOperation go2=Aggregation.group(groupKeys.split(",")[0].contains("data.")?groupKeys.split(",")[0].split("data.")[1]:groupKeys.split(",")[0]).sum("_value").as("value");
		return Aggregation.newAggregation(mp1,projectionOperation,groupop,go2);
	}

}
