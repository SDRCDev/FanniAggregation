package org.sdrc.datum19.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.sdrc.datum19.util.AreaMapObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Multiply;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Subtract;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Service
public class MongoAggregationService {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private AreaService areaService;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	@Autowired
	private ClusterAggregationService clusterAggregationService;
	
	@Autowired
	private UserAggregationService userAggregationService;
	
	@Autowired
	private MasterDataIndicatorService masterDataIndicatorService;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	Integer timePeriodId=null;
	List<DataValue> dataValueList;
	public String aggregate(Integer tp, String periodicity){
		dataValueList=new ArrayList<>();
		timePeriodId=tp;
		dataValueList=new ArrayList<>();
		
		List<Area> fetchallArea = areaRepository.findAll();
		Map<String,Area> areaMap = new HashMap<String, Area>();
		for (Area area : fetchallArea) {
			if(area.getParentAreaId()==2) {
			areaMap.put(area.getAreaName().toLowerCase().trim()+"_"+area.getParentAreaId().toString().trim(), area);
			System.out.println(area.getAreaName().toLowerCase().trim()+"_"+area.getParentAreaId().toString().trim());
			}
		}
		
		
		List<Indicator> indicatorList = indicatorRepository.getIndicatorByPeriodicity(periodicity);
		indicatorList.stream().filter(indicator->!indicator.getIndicatorDataMap().get("collection").equals("dataValue")).forEach(indicator->{
			Class<?> inputCollectionClass=null;
			try {
				inputCollectionClass=Class.forName(String.valueOf(indicator.getIndicatorDataMap().get("collection")));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (String.valueOf(indicator.getIndicatorDataMap().get("parentType")).split(":")[0]) {
			case "dropdown":
				System.out.println("indicatorNid :: "+indicator.getIndicatorDataMap().get("indicatorNid"));
				System.out.println("FormId :: "+indicator.getIndicatorDataMap().get("formId"));
			List<Integer> tdlist=new ArrayList<>();
				Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")).split("#")).stream().forEach(i->{tdlist.add(Integer.parseInt(i));});
				List<Map> dataList= mongoTemplate.aggregate(getDropdownAggregationResults(
						Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
						 String.valueOf(indicator.getIndicatorDataMap().get("area")),
						String.valueOf(indicator.getIndicatorDataMap().get("collection")),
						String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
						tdlist,
						String.valueOf(indicator.getIndicatorDataMap().get("indicatorName")),
						String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass, Map.class).getMappedResults();
				dataList.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("44")) || indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("43"))) {
						
						datadoc.setDatumId(areaMap.get(String.valueOf(data.get("_id")).toLowerCase()+"_"+"2").getAreaId());
					}else {
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					}
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
				break;
				
			case "table":
				List<Map> tableDataList=new ArrayList<>();
				switch (String.valueOf(indicator.getIndicatorDataMap().get("aggregationType"))) {
				case "number":
					tableDataList= mongoTemplate.aggregate(getTableAggregationResults(
							Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
							 String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("collection")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							 String.valueOf(indicator.getIndicatorDataMap().get("parentColumn")),
							 String.valueOf(indicator.getIndicatorDataMap().get("indicatorName"))),inputCollectionClass, Map.class).getMappedResults();
					break;
				case "count":
					tableDataList= mongoTemplate.aggregate(getTableCountResults(
							Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
							 String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("collection")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							 String.valueOf(indicator.getIndicatorDataMap().get("parentColumn")),
							 String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")),
							 String.valueOf(indicator.getIndicatorDataMap().get("indicatorName"))),inputCollectionClass, Map.class).getMappedResults();

				default:
					break;
				}
				
				tableDataList.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
				
				break;

			case "numeric":
				System.out.println("indicatorNid :: "+indicator.getIndicatorDataMap().get("indicatorNid"));
				List<Map> numericDataList= mongoTemplate.aggregate(getNumericAggregationResults(
						Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
						 String.valueOf(indicator.getIndicatorDataMap().get("area")),
						String.valueOf(indicator.getIndicatorDataMap().get("collection")),
						String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
						String.valueOf(indicator.getIndicatorDataMap().get("indicatorName")),
						String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass, Map.class).getMappedResults();
				
				numericDataList.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					
					if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("60"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("62"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("63"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("64"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("65"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("66"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("67"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("71"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("82"))) {
						datadoc.setDatumId(2);	
					}else {
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					}
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
				break;
				
			case "checkbox":
				System.out.println(indicator.getIndicatorDataMap().get("indicatorNid"));
				switch (String.valueOf(indicator.getIndicatorDataMap().get("aggregationType"))) {
				case "number":
//				List<Integer> tdlistCb=new ArrayList<>();
//				Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")).split("#")).stream().forEach(i->{tdlistCb.add(Integer.parseInt(i));});
				List<Map> dataListCb= mongoTemplate.aggregate(getCheckboxAggregationResults(
						Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
						 String.valueOf(indicator.getIndicatorDataMap().get("area")),
						String.valueOf(indicator.getIndicatorDataMap().get("collection")),
						String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
						Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId"))),
						String.valueOf(indicator.getIndicatorDataMap().get("indicatorName")),
						String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass, Map.class).getMappedResults();
					
				dataListCb.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
				break;
				
				case "count":
					List<Map> dataListCountCb= mongoTemplate.aggregate(getCheckedCountResult(
							Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
							 String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("collection")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							String.valueOf(indicator.getIndicatorDataMap().get("indicatorName")),
							String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass, Map.class).getMappedResults();
						
					dataListCountCb.forEach(data->{
						DataValue datadoc=new DataValue();
						datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
						datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
						datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
						datadoc.setTp(tp);
						datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
						datadoc.setDatumtype("area");
						dataValueList.add(datadoc);
					});
					break;
				}
				break;
				
			case "form":
				switch (String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")).split(":")[0]) {
				case "unique":
					System.out.println("unique"+Integer.valueOf((String) indicator.getIndicatorDataMap().get("indicatorNid")));
					List<Map> uniqueCountData=mongoTemplate.aggregate(getUniqueCount(
							Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")), 
							String.valueOf(indicator.getIndicatorDataMap().get("area")), 
							String.valueOf(indicator.getIndicatorDataMap().get("collection")), 
							String.valueOf(indicator.getIndicatorDataMap().get("indicatorName")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")).split(":").length>1
							?String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")).split(":")[1]:""), inputCollectionClass,Map.class).getMappedResults();
//					System.out.println("uniqueCountData :: "+uniqueCountData);
					uniqueCountData.forEach(data->{
						DataValue datadoc=new DataValue();
						datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
						if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("44")) || indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("43"))) {
							System.out.println(String.valueOf(data.get("_id")).toLowerCase()+"_2");
							datadoc.setDatumId(areaMap.get(String.valueOf(data.get("_id")).toLowerCase()+"_2").getAreaId());
						}else if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("60"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("62"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("63"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("64"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("65"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("66"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("67"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("71"))
								|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("82"))) {
							datadoc.setDatumId(2);	
						}else {
							System.out.println("Error ID"+Integer.valueOf((String) indicator.getIndicatorDataMap().get("indicatorNid")));
						datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
						}
						datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
						datadoc.setTp(tp);
						datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
						dataValueList.add(datadoc);
					});
					break;
					
				case "total":
					System.out.println("total"+Integer.valueOf((String) indicator.getIndicatorDataMap().get("indicatorNid")));
				List<Map> visitCountData=mongoTemplate.aggregate(getTotalVisitCount(
						Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")), 
						String.valueOf(indicator.getIndicatorDataMap().get("area"))), inputCollectionClass,Map.class).getMappedResults();
				visitCountData.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("44")) || indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("43"))) {
						System.out.println("@@@@@@@@@"+indicator.getIndicatorDataMap().get("formId"));
						datadoc.setDatumId(areaMap.get(String.valueOf(data.get("_id")).toLowerCase().trim()+"_"+"2").getAreaId());
					}else if(indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("60"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("62"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("63"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("64"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("65"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("66"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("67"))
							|| indicator.getIndicatorDataMap().get("formId").equals(String.valueOf("71"))) {
						datadoc.setDatumId(2);	
					}else {
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					}
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
					break;
					
				case "gte":
				case "lte":
				case "eq":
				case "gt":
				case "lt":
				Integer value=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")));
				List<Map> gteCountData=mongoTemplate.aggregate(getCount(
						Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")), 
						String.valueOf(indicator.getIndicatorDataMap().get("area")),
						String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
						value,String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass,Map.class).getMappedResults();
				gteCountData.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
					break;
				case "repeatCount":
					List<Integer> valueList=new ArrayList<>();
					if(indicator.getIndicatorDataMap().get("typeDetailId")!=null)		
					Arrays.asList(
									String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")).split("#"))
									.stream().forEach(i -> {
										if (!i.equals(""))
											valueList.add(Integer.parseInt(i));
									});
					List<Map> repeatCountData=mongoTemplate.aggregate(getRepeatCountQuery(
							Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")), 
							String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							valueList,String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))),inputCollectionClass,Map.class).getMappedResults();
					repeatCountData.forEach(data->{
						DataValue datadoc=new DataValue();
						datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
						datadoc.setDatumId(Integer.parseInt(String.valueOf(data.get("_id"))));
						datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
						datadoc.setTp(tp);
						datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
						datadoc.setDatumtype("area");
						dataValueList.add(datadoc);
					});
					break;

				default:
					break;
				}
				
				break;
				
			case "area":
				String[] rules= String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")).split(";");
				Integer value1=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")));
				List<Map> areaCountData=mongoTemplate.aggregate(getAreaCount(
						String.valueOf(indicator.getIndicatorDataMap().get("area")),
						String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
						value1,rules),inputCollectionClass,Map.class).getMappedResults();
				areaCountData.forEach(data->{
					System.out.println(data);
					if(!String.valueOf(data.get("_id")).equals("null")) {
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
					}
				});
				break;
				
			case "ifa":
				String[] ifarules= String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")).split(";");
				Integer ifavalue1=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")));
				List<Map> ifaCountData=new ArrayList<>();
				if(Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("formId")))==0)
					ifaCountData=mongoTemplate.aggregate(getAreaCount(
							String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							ifavalue1,ifarules),inputCollectionClass,Map.class).getMappedResults();
				else
					ifaCountData=mongoTemplate.aggregate(getIFAAccessedCount(
							Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("formId"))),
							String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							ifavalue1,ifarules),inputCollectionClass,Map.class).getMappedResults();
				ifaCountData.forEach(data->{
					System.out.println(data);
					if(!String.valueOf(data.get("_id")).equals("null")) {
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("ifa");
					dataValueList.add(datadoc);
					}
				});
				break;
				
			case "begin-repeat":
				switch (String.valueOf(indicator.getIndicatorDataMap().get("parentType")).split(":")[1]) {
				case "dropdown":
					List<Map> _brdd=mongoTemplate.aggregate(getBeginRepeatDropdownValue(Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
							String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("parentColumn")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule")),
							Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("typeDetailId")))), inputCollectionClass, Map.class).getMappedResults();
					
					_brdd.forEach(data->{
						DataValue datadoc=new DataValue();
						datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
						datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
						datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
						datadoc.setTp(tp);
						datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
						datadoc.setDatumtype("area");
						dataValueList.add(datadoc);
					});
					break;
					
				case "count":
					List<Map> _brcount=mongoTemplate.aggregate(getBeginRepeatCount(Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
							String.valueOf(indicator.getIndicatorDataMap().get("area")),
							String.valueOf(indicator.getIndicatorDataMap().get("parentColumn")),
							String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
							String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))), inputCollectionClass, Map.class).getMappedResults();
					
					_brcount.forEach(data->{
						DataValue datadoc=new DataValue();
						datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
						datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
						datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
						datadoc.setTp(tp);
						datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
						datadoc.setDatumtype("area");
						dataValueList.add(datadoc);
					});
					break;

				default:
					break;
				}
				break;
				
			case "master":
				if(Integer.valueOf((String) indicator.getIndicatorDataMap().get("indicatorNid"))==954)
					System.out.println("Here it starts");
				List<Map> _brcount=mongoTemplate.aggregate(masterDataIndicatorService
						.aggregateCountData(Integer.valueOf((String) indicator.getIndicatorDataMap().get("formId")),
						String.valueOf(indicator.getIndicatorDataMap().get("area")),
						timePeriodId,
						String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"))), inputCollectionClass, Map.class).getMappedResults();
				_brcount.forEach(data->{
					DataValue datadoc=new DataValue();
					datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
					datadoc.setDatumId(Integer.valueOf(String.valueOf(data.get("_id"))));
					datadoc.setDataValue(Double.valueOf(String.valueOf(data.get("value"))));
					datadoc.setTp(tp);
					datadoc.set_case(String.valueOf(indicator.getIndicatorDataMap().get("aggregationType")));
					datadoc.setDatumtype("area");
					dataValueList.add(datadoc);
				});
				break;
				
				
			default:
				break;
			}
			
		});
		dataDomainRepository.saveAll(dataValueList);
		if(configurableEnvironment.getProperty("datum.user.aggregation").equals("true")) {
		try {
			userAggregationService.aggregateUserWise(periodicity,tp);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		}
		try {
			int areaLevel = Integer.parseInt(configurableEnvironment.getProperty("spark.aggregation.arealevel"));
			for (int i = areaLevel; i >0; i--) {
				AreaMapObject amb = areaService.getAreaForAggregation(i);
				List<Integer> areaList=amb.getAreaList();
			List<Map> areaDataMap=mongoTemplate.aggregate(aggregateAreaTree(tp,areaList),DataValue.class,Map.class).getMappedResults();
			List<DataValue> datalist2=new ArrayList<>();
			areaDataMap.forEach(data->{
				DataValue datavalue=new DataValue();
				datavalue.setInid(Integer.valueOf(String.valueOf(data.get("inid"))));
				datavalue.setDatumId(Integer.valueOf(String.valueOf(data.get("parentAreaId"))));
				datavalue.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
				datavalue.setTp(Integer.valueOf(String.valueOf(data.get("tp"))));
				datavalue.set_case(String.valueOf(data.get("_case")));
				datavalue.setDatumtype("area");
				datalist2.add(datavalue);
			});
			dataDomainRepository.saveAll(datalist2);
			}
			if(configurableEnvironment.getProperty("datum.cluster.aggregation").equals("true")) {
				clusterAggregationService.aggregateCluster(periodicity, tp);
			}
			aggregateFinalIndicators(periodicity,"indicator");
		} catch (Exception e) {
			System.out.println("error encountered :: ");
			e.printStackTrace();
		}
		return "aggregation complete";
	}
	
	List<DataValue> percentDataMap=null;
	List<DataValue> percentDataMapAll=null;
	public List<DataValue> aggregateFinalIndicators(String periodicity, String indicatorType) {
		percentDataMap=new ArrayList<>();
		percentDataMapAll=new ArrayList<>();
		List<Indicator> indicatorList = indicatorRepository.getPercentageIndicators(periodicity,indicatorType);
		indicatorList.forEach(indicator->{
			List<Integer> dependencies=new ArrayList<>();
			List<Integer> numlist=new ArrayList<>();
			String[] numerators=String.valueOf(indicator.getIndicatorDataMap().get("numerator")).split(",");
			Integer inid=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")));
			String aggrule=String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"));
			for (int i = 0; i < numerators.length; i++) {
				numlist.add(Integer.parseInt(numerators[i]));
				dependencies.add(Integer.parseInt(numerators[i]));
			}
			List<Integer> denolist=new ArrayList<>();
			String[] denominators=String.valueOf(indicator.getIndicatorDataMap().get("denominator")).split(",");
			for (int i = 0; i < denominators.length; i++) {
				denolist.add(Integer.parseInt(denominators[i]));
				dependencies.add(Integer.parseInt(denominators[i]));
			}
			try {
				switch (String.valueOf(indicator.getIndicatorDataMap().get("aggregationType"))) {
				case "percent":
					percentDataMap=mongoTemplate.aggregate(getPercentData(dependencies,numlist,denolist,aggrule),DataValue.class,DataValue.class).getMappedResults();
					percentDataMap.forEach(dv->{
						dv.setInid(inid);
						dv.set_case("percent");
						if (dv.getDenominator()==0) {
							dv.setDataValue(null);
						}
					});
					percentDataMapAll.addAll(percentDataMap);
					break;
					
				case "avg":
					percentDataMap=mongoTemplate.aggregate(getAvgData(dependencies,numlist,denolist,aggrule),DataValue.class,DataValue.class).getMappedResults();
					percentDataMap.forEach(dv->{
						dv.setInid(inid);
						dv.set_case("avg");
						if (dv.getDenominator()==0) {
							dv.setDataValue(null);
						}
					});
					percentDataMapAll.addAll(percentDataMap);
					break;

				default:
					break;
				}
					
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		dataDomainRepository.saveAll(percentDataMapAll);
		return percentDataMapAll;
	}
	
	
	private TypedAggregation getAvgData(List<Integer> dependencies, List<Integer> numlist, List<Integer> denolist,
			String aggrule) {
		// TODO Auto-generated method stub
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dependencies).and("tp").is(timePeriodId));
		GroupOperation groupOperation=null;
		ProjectionOperation projectionOperation=null;
		ProjectionOperation p1=null;
		ProjectionOperation p2=null;
		groupOperation=Aggregation.group("datumId","tp").sum(when(where("inid").in(numlist)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
				.sum(when(where("inid").in(denolist)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
		projectionOperation=Aggregation.project().and("_id.datumId").as("datumId")
				.and("_id.tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
				.andExclude("_id")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf("numerator").divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,projectionOperation);
	}


	private TypedAggregation<DataValue> aggregateAreaTree(Integer tp, List<Integer> areaList) {
		
		MatchOperation matchOperation=Aggregation.match(Criteria.where("tp").is(tp).and("datumId").in(areaList));
		LookupOperation lookupOperation=Aggregation.lookup("area", "datumId", "areaId", "parent");
		GroupOperation groupOperation=Aggregation.group("parent.parentAreaId","inid","tp","_case").sum("dataValue").as("dataValue");
		UnwindOperation unwindOperation=Aggregation.unwind("$_id.parentAreaId");
		
		return Aggregation.newAggregation(DataValue.class,matchOperation,lookupOperation,groupOperation,unwindOperation);
	}
	
	private TypedAggregation<DataValue> getPercentData(List<Integer> dep,List<Integer> num,List<Integer> deno, String rule){
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dep).and("tp").is(timePeriodId));
		GroupOperation groupOperation=null;
		ProjectionOperation projectionOperation=null;
		ProjectionOperation p1=null;
		ProjectionOperation p2=null;
		if(rule.equals("sub")) {
			groupOperation=Aggregation.group("datumId","tp").sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator")
					.sum(Sum.sumOf(when(where("inid").is(num.get(0))).then("$dataValue").otherwise(0))).as("n1")
					.sum(when(where("inid").is(num.get(1))).then("$dataValue").otherwise(0)).as("n2");
			
			p1=Aggregation.project().and("datumId").as("datumId").and("tp").as("tp").and(Subtract.valueOf("n1").subtract("n2")).as("numerator").and("denominator").as("denominator");
			
			p2=Aggregation.project().and("datumId").as("datumId").and("tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
					.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
					.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
			return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,p1,p2);
		}else {
		groupOperation=Aggregation.group("datumId","tp").sum(when(where("inid").in(num)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
				.sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
		projectionOperation=Aggregation.project().and("_id.datumId").as("datumId")
				.and("_id.tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
				.andExclude("_id")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
				.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,projectionOperation);
		}
	}
	
//	for count of 0 records
	public Aggregation getDropdownAggregationResults(Integer formId, String area, String collection, String path, List<Integer> tdlist,String name,String conditions) {
		List<String> condarr=new ArrayList<>();
		if(!conditions.equals("null")&&!conditions.isEmpty())
			condarr=Arrays.asList(conditions.split(";"));
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true);
		List<Criteria> andCriteria = new ArrayList<>();
		if(!condarr.isEmpty()) {
		/*condarr.forEach(_cond->{
			matchCriteria.andOperator(Criteria.where(_cond.split(":")[0].split("\\(")[1]).is(Integer.parseInt(_cond.split(":")[1].split("\\)")[0])));
		});*/
			
			for (int i = 0; i < condarr.size(); i++) {
				String condition=condarr.get(i).split("\\(")[0];
				String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
				switch (condition) {
				case "and$lte":
					andCriteria.add(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
					break;
				case "and$eq":
					andCriteria.add(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
					break;
				case "and$gte":
					andCriteria.add(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
					break;
				case "and$gt":
					andCriteria.add(Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1])));
					break;
				case "and$lt":
					andCriteria.add(Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1])));
					break;
				case "and$in":
					List<Integer> valList = new ArrayList<>();
					Arrays.asList(expression.split(":")[1].split("\\[")[1].split("\\]")[0].split(",")).forEach(val ->{
						valList.add(Integer.parseInt(val));
					});
					andCriteria.add(Criteria.where(expression.split(":")[0]).in(valList));
					break;

				default:
					break;
				}
			}
			matchCriteria=matchCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		}
		MatchOperation matchOperation = Aggregation.match(matchCriteria);
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data");
		ProjectionOperation projectionOperation1=Aggregation.project()
				.and(area).as("area")
				.and(Sum.sumOf(when(where("data."+path).in(tdlist)).then(1).otherwise(0))).as("projectedData");
		
		GroupOperation groupOperation= Aggregation.group("area").sum("projectedData").as("value");
		
		return Aggregation.newAggregation(matchOperation,projectionOperation,projectionOperation1,groupOperation);
	}
	
	public Aggregation getTableAggregationResults(Integer formId, String area, String collection, String path, String table,String name) {
		MatchOperation matchOperation = Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true));
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data");
		UnwindOperation unwindOperation = Aggregation.unwind(table);
		GroupOperation groupOperation= Aggregation.group(area).sum(table+"."+path).as("value");
		return Aggregation.newAggregation(matchOperation,projectionOperation,unwindOperation,groupOperation);
	}
	
	private Aggregation getTableCountResults(Integer formId, String area, String collection, String path, String table,String _rule,String name) {
		// TODO Auto-generated method stub
		String[] rules= _rule.split(";");
		GroupOperation _groupOp=null;
		List<Criteria> andCriteria = new ArrayList<>();
		Criteria criteriaQuery=new Criteria();
		for (String rule: rules) {
			switch (rule.split("\\(")[0]) {
			case "and$eq":
				andCriteria.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).gt(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0])));
//				_groupOp=Aggregation.group(area).sum(when(where(rule.split("\\(")[1].split(":")[0])
//						.is(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]))).then(1).otherwise(0)).as("value");
				break;
			case "and$gt":
				andCriteria.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).gt(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0])));
//				_groupOp=Aggregation.group(area).sum(when(where(rule.split("\\(")[1].split(":")[0])
//						.gt(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]))).then(1).otherwise(0)).as("value");
				break;
			case "and$lt":
				andCriteria.add(where(rule.split("\\(")[1].split(":")[0]).lt(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0])));
//				_groupOp=Aggregation.group(area).sum(when(where(rule.split("\\(")[1].split(":")[0])
//						.lt(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]))).then(1).otherwise(0)).as("value");
				break;
			case "and$gte":
				andCriteria.add(where(rule.split("\\(")[1].split(":")[0]).gte(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0])));
//				_groupOp=Aggregation.group(area).sum(when(where(rule.split("\\(")[1].split(":")[0])
//						.gte(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]))).then(1).otherwise(0)).as("value");
				break;
			case "and$lte":
				andCriteria.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).lte(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0])));
//				_groupOp=Aggregation.group(area).sum(when(where(rule.split("\\(")[1].split(":")[0])
//						.lte(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]))).then(1).otherwise(0)).as("value");
				break;

			default:
				break;
			}
		    
		}
		criteriaQuery=criteriaQuery.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		MatchOperation matchOperation = Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true));
		ProjectionOperation _projectOp=Aggregation.project("data");
		UnwindOperation _unwindOp=Aggregation.unwind("data."+table);
		ProjectionOperation pop=Aggregation.project().and(area).as("area").and(when(criteriaQuery).then(1).otherwise(0)).as("count");
		_groupOp = Aggregation.group(area).sum("count").as("value");
		
		return Aggregation.newAggregation(Map.class, matchOperation,_projectOp, _unwindOp,pop,_groupOp);
	}
	
	public Aggregation getNumericAggregationResults(Integer formId, String area, String collection, String path,String name,String conditions) {
		List<String> condarr=new ArrayList<>();
		if(!conditions.equals("null")&&!conditions.isEmpty())
			condarr=Arrays.asList(conditions.split(";"));
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true);
		List<Criteria> andCriteria = new ArrayList<>();
		Criteria criteriaQuery=new Criteria();
		if(!condarr.isEmpty()) {
		/*condarr.forEach(_cond->{
			matchCriteria.andOperator(Criteria.where(_cond.split(":")[0].split("\\(")[1]).is(Integer.parseInt(_cond.split(":")[1].split("\\)")[0])));
		});*/
		
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$eq":
				andCriteria.add(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$lt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$in":
				List<Integer> valList = new ArrayList<>();
				Arrays.asList(expression.split(":")[1].split("\\[")[1].split("\\]")[0].split(",")).forEach(val ->{
					valList.add(Integer.parseInt(val));
				});
				andCriteria.add(Criteria.where(expression.split(":")[0]).in(valList));
				break;

			default:
				break;
			}
		}
		matchCriteria=matchCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		}
		
		String pathString="";
		path="data."+path;
		path=path.replace("+", "+data.");
		path=path.replace("-", "-data.");
		pathString=path;
		MatchOperation matchOperation = Aggregation.match(matchCriteria);
		ProjectionOperation projectionOperation=null;
		ProjectionOperation pop=null;
		GroupOperation groupOperation=null;
		if(pathString.contains("+")||pathString.contains("-")) {
			projectionOperation=Aggregation.project().and("data").as("data");
			pop=Aggregation.project().and(area).as("area").andExpression(pathString).as("value1");
			groupOperation= Aggregation.group("area").sum("value1").as("value");
			return Aggregation.newAggregation(matchOperation,projectionOperation,pop,groupOperation);
		}else {
			projectionOperation=Aggregation.project().and("data").as("data").and("formId").as("formId");
			groupOperation= Aggregation.group(area).sum(pathString).as("value");
			return Aggregation.newAggregation(matchOperation,projectionOperation,groupOperation);
		}
		
	}
	
	public Aggregation getCheckboxAggregationResults(Integer formId, String area, String collection, String path, Integer td,String name,String conditions) {
		Criteria criteriaQuery=new Criteria();
		List<Criteria> andCriteria = new ArrayList<>();
		List<String> condarr=new ArrayList<>();
		
		
		if(conditions!=null || !conditions.isEmpty())
			condarr=Arrays.asList(conditions.split(";"));
		if(!condarr.isEmpty() || condarr.size()!=0 || !condarr.contains(null)) {
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$eq":
				andCriteria.add(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$lt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$in":
				List<Integer> valList = new ArrayList<>();
				Arrays.asList(expression.split(":")[1].split("\\[")[1].split("\\]")[0].split(",")).forEach(val ->{
					valList.add(Integer.parseInt(val));
				});
				andCriteria.add(Criteria.where(expression.split(":")[0]).in(valList));
				break;

			default:
				break;
			}
		}
		}
		criteriaQuery=criteriaQuery.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		MatchOperation mop = Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true));
		UnwindOperation uop = Aggregation.unwind("data");
		String pop = "{\r\n" + 
				"        $project : {\r\n" + 
				"            area : \"$"+area+"\",\r\n" + 
				"            matches : {\r\n" + 
				"                $filter : {input : \"$data."+path+"\", as : \"value\", cond : {$in : [\"$$value\", ["+td+"]]}}\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"        }";
		
		String pop1 = "{\r\n" + 
				"            $project : {\r\n" + 
				"                area : \"$area\",\r\n" + 
				"                count : {$cond : {if : {$eq : [\"$matches\", ["+td+"]]}, then : 1, else : 0}}\r\n" + 
				"                }\r\n" + 
				"            }";
		
		String gop = "{\r\n" + 
				"            $group : {\r\n" + 
				"                _id : \"$area\",\r\n" + 
				"                value : {$sum : \"$count\"}\r\n" + 
				"                }\r\n" + 
				"            }";
//		ProjectionOperation pop1 = Aggregation.project().and(area).as("area").and(when(where(path).in(tdlist)).then(1).otherwise(0)).as("count");
//		GroupOperation gop = Aggregation.group("area").sum("count").as("value");
		return Aggregation.newAggregation(mop,uop,new CustomAggregationOperation(pop),new CustomAggregationOperation(pop1),new CustomAggregationOperation(gop));
	}
	
	public Aggregation getCheckedCountResult(Integer formId, String area, String collection, String path,String name,String conditions) {
		Criteria criteriaQuery=new Criteria();
		List<Criteria> andCriteria = new ArrayList<>();
		List<String> condarr=new ArrayList<>();
		
		
		if(conditions!=null || !conditions.isEmpty())
			condarr=Arrays.asList(conditions.split(";"));
		if(!condarr.isEmpty() || condarr.size()!=0 || !condarr.contains(null)) {
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$eq":
				andCriteria.add(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$lt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$in":
				List<Integer> valList = new ArrayList<>();
				Arrays.asList(expression.split(":")[1].split("\\[")[1].split("\\]")[0].split(",")).forEach(val ->{
					valList.add(Integer.parseInt(val));
				});
				andCriteria.add(Criteria.where(expression.split(":")[0]).in(valList));
				break;

			default:
				break;
			}
		}
		}
		criteriaQuery=criteriaQuery.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		MatchOperation mop = Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true));
		ProjectionOperation pop = Aggregation.project("data");
		UnwindOperation uop = Aggregation.unwind("data."+path);
		ProjectionOperation pop1 = Aggregation.project().and(area).as("area").and(when(criteriaQuery).then(1).otherwise(0)).as("count");
		GroupOperation gop = Aggregation.group("area").sum("count").as("value");
		
		return Aggregation.newAggregation(mop, pop, uop, pop1, gop);
	}
	
	public Aggregation getBeginRepeatDropdownValue(Integer formId,String area,String beginrepeat,String path,String conditions,Integer value) {
		List<String> condarr=new ArrayList<>();
		if(conditions.equals(null))
			condarr=Arrays.asList(conditions.split(";"));
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true);
		if(!condarr.isEmpty()) {
		condarr.forEach(_cond->{
			String condition=_cond.split("\\(")[0];
			String expression=_cond.split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				matchCriteria.andOperator(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$eq":
				matchCriteria.andOperator(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gte":
				matchCriteria.andOperator(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
				break;

			default:
				break;
			}
		});
		}
		MatchOperation matchop=Aggregation.match(matchCriteria);
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data");
		UnwindOperation unwindop=Aggregation.unwind(beginrepeat);
		GroupOperation groupop=Aggregation.group(area).sum(when(where(path).is(value)).then(1).otherwise(0)).as("value");
		return Aggregation.newAggregation(matchop,projectionOperation,unwindop,groupop);
		
	}
	
	public Aggregation getBeginRepeatCount(Integer formId,String area,String beginrepeat,String path,String conditions) {
//		MatchOperation matchop1=Aggregation.match(matchCriteria1);
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data");
		UnwindOperation unwindop=Aggregation.unwind(beginrepeat);
//		Criteria matchCriteria1=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId);
		List<String> condarr=new ArrayList<>();
		if(!conditions.equals(null))
			condarr=Arrays.asList(conditions.split(";"));
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true);
		Criteria criteriaQuery=new Criteria();
		List<Criteria> andCriteria = new ArrayList<>();
		if(!condarr.isEmpty()) {
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$eq":
				andCriteria.add(Criteria.where(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gte":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$gt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1])));
				break;
			case "and$lt":
				andCriteria.add(Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1])));
				break;

			default:
				break;
			}
		}
		}
		criteriaQuery=criteriaQuery.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		MatchOperation mp1=Aggregation.match(matchCriteria);
		ProjectionOperation pop=Aggregation.project().and(area).as("area").and(when(criteriaQuery).then(1).otherwise(0)).as("count");
		GroupOperation groupop=Aggregation.group("area").sum("count").as("value");
		System.out.println(Aggregation.newAggregation(mp1,projectionOperation,pop,groupop));
		return Aggregation.newAggregation(mp1,projectionOperation,unwindop,pop,groupop);
		
	
	}
	
	
	public Aggregation getUniqueCount(Integer formId, String area, String collection, String name,String childArea,String conditions) {
		
		List<String> condarr=new ArrayList<>();
		if(!conditions.isEmpty())
			condarr=Arrays.asList(conditions.split(","));
		Criteria criteria = Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true);
		if(!condarr.isEmpty()) {
		condarr.forEach(_cond->{
			criteria.andOperator(Criteria.where(_cond.split("=")[0]).in(Integer.parseInt(_cond.split("=")[1])));
		});
		}
		
		MatchOperation matchOperation=Aggregation.match(criteria);
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data").and("formId").as("formId");
		GroupOperation groupOperation=Aggregation.group(area).addToSet("data."+childArea).as("childArea");
		UnwindOperation unwindOperation=Aggregation.unwind("childArea");
		GroupOperation groupOperation2=Aggregation.group("$_id").count().as("dataValue");
		return Aggregation.newAggregation(matchOperation,projectionOperation,groupOperation,unwindOperation,groupOperation2);
	}
	
	public Aggregation getTotalVisitCount(Integer formId, String area) {
		MatchOperation matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("rejected").is(false).and("isValid").is(true));
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data").and("formId").as("formId");
		GroupOperation groupOperation=Aggregation.group(area).count().as("dataValue");
		return Aggregation.newAggregation(matchOperation,projectionOperation,groupOperation);
	}
	
	public Aggregation getCount(Integer formId, String area, String path, Object value, String rule) {
		MatchOperation matchOperation=null;
		switch (rule) {
		case "eq":
			matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("rejected").is(false).and("isValid").is(true).and("data."+path).is(value).and("timePeriod.timePeriodId").is(timePeriodId));
			break;
		case "lte":
			matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("rejected").is(false).and("isValid").is(true).and("data."+path).lte(value).and("timePeriod.timePeriodId").is(timePeriodId));
			break;
		case "gte":
			matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("rejected").is(false).and("isValid").is(true).and("data."+path).gte(value).and("timePeriod.timePeriodId").is(timePeriodId));
			break;
		case "gt":
			matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("rejected").is(false).and("isValid").is(true).and("data."+path).gt(value).and("timePeriod.timePeriodId").is(timePeriodId));
			break;
		case "lt":
			matchOperation=Aggregation.match(Criteria.where("formId").is(formId).and("rejected").is(false).and("isValid").is(true).and("data."+path).lt(value).and("timePeriod.timePeriodId").is(timePeriodId));
			break;

		default:
			break;
		}
		
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data");
		GroupOperation groupOperation=Aggregation.group(area).count().as("dataValue");
		return Aggregation.newAggregation(matchOperation,projectionOperation,groupOperation);
	}
	
	public Aggregation getRepeatCountQuery(Integer formId, String area, String path, List<Integer> valueList,
			String query) {

		MatchOperation matchOperation = null;
		ProjectionOperation projectionOperation = null;
		GroupOperation groupOperation = null;
		ProjectionOperation projectionOperation2 = null;
		GroupOperation groupOperation2 = null;

		ProjectionOperation projectionOperation3 = null;
		if (path.equals("") || path.equals("null")) {
			List<String> fields=new ArrayList<>();
			fields.add(area);
			List<String> condarr=Arrays.asList(query.split(":")[1].split(","));
			fields.addAll(condarr);
			
			matchOperation = Aggregation.match(Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
					.and("rejected").is(false).and("isValid").is(true));
			projectionOperation = Aggregation.project().and("data").as("data");
			groupOperation = Aggregation.group(fields.toArray(new String[fields.size()])).count().as("totalcount");
			projectionOperation2 = Aggregation.project(area.split("\\.")[1])
					.and(when(where("totalcount").gt(1)).then(1).otherwise(0)).as("repeatCount");
				groupOperation2 = Aggregation.group(area.split("\\.")[1])
						.sum(when(where("repeatCount").is(1)).then(Sum.sumOf("repeatCount")).otherwise(0)).as("dataValue");

//			projectionOperation3 = Aggregation.project().and(area.split("\\.")[1]).as(area.split("\\.")[1])
//					.and(when(where("repeatCount").is(1)).then(Sum.sumOf("repeatCount")).otherwise(0)).as("dataValue");

		} else {
			List<String> fields=new ArrayList<>();
			fields.add(area);
			fields.add("data." + path);
			List<String> condarr=Arrays.asList(query.split(":")[1].split(","));
			fields.addAll(condarr);
			matchOperation = Aggregation.match(Criteria.where("formId").is(formId).and("data." + path).in(valueList).and("timePeriod.timePeriodId").is(timePeriodId)
					.and("rejected").is(false).and("isValid").is(true));
			projectionOperation = Aggregation.project().and("data").as("data");
			groupOperation = Aggregation.group(fields.toArray(new String[fields.size()])).count().as("totalcount");
			projectionOperation2 = Aggregation.project(path, area.split("\\.")[1])
					.and(when(where("totalcount").gt(1)).then(1).otherwise(0)).as("repeatCount");

			groupOperation2 = Aggregation.group(area.split("\\.")[1])
					.sum(when(where("repeatCount").is(1)).then(Sum.sumOf("repeatCount")).otherwise(0)).as("dataValue");
//			projectionOperation3 = Aggregation.project().and(area.split("\\.")[1]).as(area.split("\\.")[1])
//					.and(when(where("repeatCount").is(1)).then(Sum.sumOf("repeatCount")).otherwise(0)).as("dataValue");

		}

		return Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation, projectionOperation2,
				groupOperation2);

	}
	
	public Aggregation getAreaCount(String area, String path,Integer value,String[] rules) {
		Criteria criteria = Criteria.where(path).is(value);

		List<Criteria> orCriterias = new ArrayList<Criteria>();
		List<Criteria> andCriterias=new ArrayList<Criteria>();
		

		for (String rule: rules) {
			switch (rule.split("\\(")[0]) {
			case "eq":
				criteria=criteria.and(rule.split("\\(")[1].split(":")[0]).is(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]));
				break;
			case "and$in" :
				List<Integer> andtd=new ArrayList<>();
				Arrays.asList(rule.split("\\[")[1].split("\\]")[0].split(",")).forEach(v->andtd.add(Integer.parseInt(v)));
				andCriterias.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).in(andtd));
				break;
			case "or$in" :
				List<Integer> ortd=new ArrayList<>();
				Arrays.asList(rule.split("\\[")[1].split("\\]")[0].split(",")).forEach(v->ortd.add(Integer.parseInt(v)));
				orCriterias.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).in(ortd));
				break;

			default:
				break;
			}
		    
		}
		if (!andCriterias.isEmpty()) {
			criteria=criteria.andOperator(andCriterias.toArray(new Criteria[andCriterias.size()]));
		}
		if(orCriterias.size()!=0)
			criteria = criteria.orOperator(orCriterias.toArray(new Criteria[orCriterias.size()]));
		MatchOperation matchOperation = Aggregation.match(criteria);
		GroupOperation groupOperation=Aggregation.group(area).count().as("dataValue");
		return Aggregation.newAggregation(matchOperation,groupOperation);
	}


public Aggregation getIFAAccessedCount(Integer formId,String area, String path,Integer value,String[] rules) {
	Criteria criteria = Criteria.where("formId").is(formId).and(path).is(value).and("rejected").is(false).and("isValid").is(true);

	List<Criteria> orCriterias = new ArrayList<Criteria>();
	List<Criteria> andCriterias=new ArrayList<Criteria>();
	

	for (String rule: rules) {
		switch (rule.split("\\(")[0]) {
		case "eq":
			criteria=criteria.and(rule.split("\\(")[1].split(":")[0]).is(Integer.parseInt(rule.split("\\(")[1].split(":")[1].split("\\)")[0]));
			break;
		case "and$in" :
			List<Integer> andtd=new ArrayList<>();
			Arrays.asList(rule.split("\\[")[1].split("\\]")[0].split(",")).forEach(v->andtd.add(Integer.parseInt(v)));
			andCriterias.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).in(andtd));
			break;
		case "or$in" :
			List<Integer> ortd=new ArrayList<>();
			Arrays.asList(rule.split("\\[")[1].split("\\]")[0].split(",")).forEach(v->ortd.add(Integer.parseInt(v)));
			orCriterias.add(Criteria.where(rule.split("\\(")[1].split(":")[0]).in(ortd));
			break;

		default:
			break;
		}
	    
	}
	if (!andCriterias.isEmpty()) {
		criteria=criteria.andOperator(andCriterias.toArray(new Criteria[andCriterias.size()]));
	}
	if(orCriterias.size()!=0)
		criteria = criteria.orOperator(orCriterias.toArray(new Criteria[orCriterias.size()]));
	ProjectionOperation projectionOperation=Aggregation.project("data");
	MatchOperation matchOperation = Aggregation.match(criteria);
	GroupOperation groupOperation=Aggregation.group(area).count().as("dataValue");
	return Aggregation.newAggregation(matchOperation,projectionOperation,groupOperation);
	}
public String importCRCDataValue(){

	try {
		List < DataValue > listOfDataValue = new LinkedList <> ();

		DataValue dataValue = null;
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("crsaggData/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}
		
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				System.out.println("START");
				workbook = new XSSFWorkbook(files[f]);
				System.out.println("END");
			} catch (InvalidFormatException | IOException e) {

				e.printStackTrace();
			}
		
		XSSFSheet sheet = workbook.getSheet("Sheet1");

		XSSFRow row = null;
		XSSFCell cell = null;
		List<TimePeriod> timePeriods = timePeriodRepository.findByPeriodicity("0");
		for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
			dataValue = new DataValue();

			int colNum = 0;

			row = sheet.getRow(rowNum);
			cell = row.getCell(colNum);
			dataValue.setDatumId((int)cell.getNumericCellValue());
			
			colNum++;
			cell = row.getCell(colNum);
			dataValue.setDataValue(cell.getNumericCellValue());
			
			colNum++;
			cell = row.getCell(colNum);
			dataValue.setTp(timePeriods.get(0).getTimePeriodId());
			
			colNum++;
			cell = row.getCell(colNum);
			dataValue.setInid((int)cell.getNumericCellValue());
			
			

			
			dataValue.set_case("count");
			dataValue.set_class("org.sdrc.datum19.document.DataValue");
			
			listOfDataValue.add(dataValue);

		}
		

		workbook.close();
		dataDomainRepository.saveAll(listOfDataValue);
		
		
	}} catch (IOException e) {
		e.printStackTrace();
	}finally {
		
	}
	
	return "done";
	}
}
