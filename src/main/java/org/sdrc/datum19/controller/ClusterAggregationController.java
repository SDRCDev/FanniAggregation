package org.sdrc.datum19.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.datum19.document.TypeDetail;
import org.sdrc.datum19.repository.TypeDetailsRepository;
import org.sdrc.datum19.service.ClusterAggregationService;
import org.sdrc.datum19.service.UserAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClusterAggregationController {

	@Autowired
	private ClusterAggregationService clusterAggregationService;
	
	@Autowired
	private TypeDetailsRepository typeDetailsRepository;
	
	@Autowired
	private UserAggregationService userAggregationService;
	
	
	@GetMapping("createCluster")
	public String createClusterCollection() {
		return clusterAggregationService.createClusterCollection();
	}
	
	@GetMapping("exportTypeDetails")
	public String exportTypeDetails() throws IOException, InvalidFormatException {
		XSSFWorkbook wb=new XSSFWorkbook();
		XSSFSheet sheet=wb.createSheet("typedetails");
		List<TypeDetail> tds=typeDetailsRepository.findAll();
		for (int i = 0; i < tds.size(); i++) {
			XSSFRow row=sheet.createRow(i);
			row.createCell(0).setCellValue(String.valueOf(tds.get(i).getFormId()));
			row.createCell(1).setCellValue(tds.get(i).getSlugId());
			row.createCell(2).setCellValue(tds.get(i).getName());
		}
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\SDRC_DEV\\Desktop\\typedetails.xlsx")); 
        wb.write(out);
		wb.close();
		return "success";
	}
	
	@GetMapping("testUserAggregation")
	public String testUserAggregation() {
		userAggregationService.aggregateUserWise("monthly",7);
		return "success";
	}
}
