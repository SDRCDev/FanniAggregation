package org.sdrc.datum19.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Service
public class IndicatorConfigService {
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	public void importIndicators() throws InvalidFormatException, IOException{
		XSSFWorkbook indicatorWorkbook = new XSSFWorkbook(new File(configurableEnvironment.getProperty("spark.template.uri")));
		XSSFSheet indicatorSheet = indicatorWorkbook.getSheet(configurableEnvironment.getProperty("spark.template.sheet"));
		List<XSSFRow> rowList = new ArrayList<XSSFRow>();
		Map<String, Object> indicatorMap=new HashMap<String, Object>();
		XSSFRow header=indicatorSheet.getRow(0);
		for (int rowNum = 1; rowNum <= indicatorSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=indicatorSheet.getRow(rowNum);
			for(int cellnum=0;cellnum<row.getLastCellNum();cellnum++){
				indicatorMap.put(header.getCell(cellnum).getStringCellValue(), getCellValueAsString(row.getCell(cellnum)));
			}
			System.out.println(indicatorMap);
			Indicator i=new Indicator();
			i.setIndicatorDataMap(indicatorMap);
			indicatorRepository.save(i);
		}
		indicatorWorkbook.close();
	}
	@SuppressWarnings("deprecation")
	public static String getCellValueAsString(Cell cell) {
        String strCellValue = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                strCellValue = cell.toString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "dd/MM/yyyy");
                    strCellValue = dateFormat.format(cell.getDateCellValue());
                } else {
                    Double value = cell.getNumericCellValue();
                    Long longValue = value.longValue();
                    strCellValue = new String(longValue.toString());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                strCellValue = new String(new Boolean(
                        cell.getBooleanCellValue()).toString());
                break;
            case Cell.CELL_TYPE_BLANK:
                strCellValue = "";
                break;
            }
        }
        return strCellValue;
    }
}
