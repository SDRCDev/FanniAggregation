package org.sdrc.datum19.controller;

import org.sdrc.datum19.model.ApplicationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OverviewController {
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@GetMapping("applicationDetails")
	public ApplicationDetails getAplplicationdetails() {
		ApplicationDetails appDetails = new ApplicationDetails();
		appDetails.setApplicationName(configurableEnvironment.getProperty("datum.project.name"));
		appDetails.setDbHost(configurableEnvironment.getProperty("spring.data.mongodb.host"));
		appDetails.setDbName(configurableEnvironment.getProperty("spring.data.mongodb.database"));
		return appDetails;
	}
}
