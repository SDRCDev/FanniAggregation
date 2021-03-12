package org.sdrc.datum19.controller;

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.service.IndicatorConfigService;
import org.sdrc.datum19.service.MongoAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@RestController
public class MongoAggregationController {

	@Autowired
	private MongoAggregationService mongoAggregationService;
	
	@Autowired
	private IndicatorConfigService indicatorConfigService;
	
	@GetMapping("/mongoAggregate")
	public String getMongoAggregatedData(@RequestParam("tp") Integer tp,@RequestParam("periodicity") String periodicity) {
		return mongoAggregationService.aggregate(tp,periodicity);
	}
	
	@GetMapping("/aggregatepercent")
	public List<DataValue> aggregatepercent(@RequestParam("periodicity") String periodicity, @RequestParam("type") String type){
		return mongoAggregationService.aggregateFinalIndicators(periodicity, type);
	}
	
	@GetMapping("/importIndicators")
	public void importIndicators() throws InvalidFormatException, IOException{
		indicatorConfigService.importIndicators();
	}
}
