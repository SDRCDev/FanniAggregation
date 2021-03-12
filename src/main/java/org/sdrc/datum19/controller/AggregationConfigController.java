package org.sdrc.datum19.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.repository.TypeDetailsRepository;
import org.sdrc.datum19.service.AggregationConfigService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregationConfigController {

	@Autowired
	public AggregationConfigService aggregationConfigService;

	@Autowired
	public TypeDetailsRepository typeDetailsRepository;

	@GetMapping("/getAllForm")
	public List<ValueObject> getAllForm() {
		return aggregationConfigService.getAllForm();
	}

	@GetMapping("/getQuestions")
	@ResponseBody
	public Map<Integer, List<QuestionModel>> getQuestions() {
		return aggregationConfigService.getQuestions();
	}

	@GetMapping("/getTypeDetails")
	@ResponseBody
	public Map<Integer, Map<Integer, List<TypeDetailsModel>>> getTypeDetails() {
		return aggregationConfigService.getTypeDetails();
	}

	@GetMapping("/getAggregationTypes")
	@ResponseBody
	public List<ValueObject> getAggregationTypes() {
		return aggregationConfigService.getAggregationTypes();
	}

	@GetMapping("/getAllIndicators")
	@ResponseBody
	public List<?> getAllIndicators() {
		return aggregationConfigService.getAllIndicators();
	}

	@PostMapping("/saveIndicator")
	@ResponseBody
	public String saveIndiactor(@RequestBody IndicatorConfigModel indicatorConfigModel) {
		return aggregationConfigService.saveIndiactor(indicatorConfigModel);
	}

	@PostMapping("/addSector")
	@ResponseBody
	public String addSector(@RequestBody SectorModel sectorModel) {
		return aggregationConfigService.addSector(sectorModel);
	}

	@PostMapping("/addSubSector")
	@ResponseBody
	public String addSubSector(@RequestBody SubsectorModel subsectorModel) {
		return aggregationConfigService.addSubSector(subsectorModel);
	}

	@GetMapping("/getIndicatorsForView")
	@ResponseBody
	public List<?> getIndicatorsForView() {
		return aggregationConfigService.getIndicatorsForView();
	}

	@GetMapping("/getAllSectors")
	@ResponseBody
	public List<?> getAllSectors() {
		return aggregationConfigService.getAllSectors();
	}

	@GetMapping("/getAllSubSectors")
	@ResponseBody
	public List<?> getAllSubSectors() {
		return aggregationConfigService.getAllSubSectors();
	}

	@PostMapping("/updateOne")
	@ResponseBody
	public ResponseEntity<String> updateOne(@RequestBody Indicator indicatorInfo) {
		return new ResponseEntity<String>(aggregationConfigService.updateOne(indicatorInfo), HttpStatus.OK);
	}
	
	@GetMapping(value = "excelDownloadIndicators")
	public ResponseEntity<InputStreamResource> excelDownloadIndicators() {

		String filePath = "";
		try {
			filePath = aggregationConfigService.excelDownloadIndicators();
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
			
			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
}
