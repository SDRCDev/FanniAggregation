package org.sdrc.datum19.service;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.datum19.document.EnginesForm;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.Question;
import org.sdrc.datum19.document.Sector;
import org.sdrc.datum19.document.Subsector;
import org.sdrc.datum19.model.ConditionModel;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.IndicatorMapModel;
import org.sdrc.datum19.model.IndicatorModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.repository.EngineFormRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.QuestionRepository;
import org.sdrc.datum19.repository.SectorRepository;
import org.sdrc.datum19.repository.SubsectorRepository;
import org.sdrc.datum19.repository.TypeDetailsRepository;
import org.sdrc.datum19.util.AggregationType;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

@Service
public class AggregationConfigServiceImpl implements AggregationConfigService {

	@Autowired
	public EngineFormRepository engineFormRepository;

	@Autowired
	public QuestionRepository questionRepository;

	@Autowired
	public IndicatorRepository indicatorRepository;

	@Autowired
	public TypeDetailsRepository typeDetailsRepository;

	@Autowired
	public SectorRepository sectorRepository;

	@Autowired
	public SubsectorRepository subsectorRepository;

	@Autowired
	private ConfigurableEnvironment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	private final Path outputPath = Paths.get("/temp/datum/DatumTemp");

	@Override
	public List<ValueObject> getAllForm() {
		return engineFormRepository.findAll().stream().map(v -> {
			ValueObject object = new ValueObject();
			object.setKey(v.getFormId());
			object.setValue(v.getName());
			return object;
		}).collect(Collectors.toList());
	}

	@Override
	public Map<Integer, List<QuestionModel>> getQuestions() {
		return questionRepository.findAllByOrderByQuestionOrderAsc().stream()
				.filter(v -> (!v.getQuestion().equals("Others (specify)")
						&& !v.getQuestion().equals("Other Please specify") && !v.getQuestion().equals("Specify where")
						&& !v.getQuestion().equals("Intervention Cluster No.")))
				.map(v -> {
					QuestionModel questionModel = new QuestionModel();
					questionModel.setQuestionId(v.getQuestionId());
					questionModel.setQuestionName(v.getQuestion().trim());
					questionModel.setControllerType(v.getControllerType());
					questionModel.setFieldType(v.getFieldType());
					questionModel.setColumnName(v.getColumnName());
					questionModel.setParentColumnName(v.getParentColumnName().equals("") ? v.getParentColumnName()
							: "data." + v.getParentColumnName());
					questionModel.setTypeId(v.getTypeId() != null ? v.getTypeId().getSlugId() : null);
					questionModel.setArea(v.getTableName() != null ? true : false);
					questionModel.setFormId(v.getFormId());
					questionModel.setSector(v.getSection());
					questionModel.setSubsector(v.getSubsection());

					return questionModel;
				}).collect(Collectors.groupingBy(QuestionModel::getFormId, Collectors.toList()));
	}

	@Override
	public List<ValueObject> getAggregationTypes() {
		return Arrays.asList(AggregationType.values()).stream().map(v -> {
			ValueObject object = new ValueObject();
			object.setKey(v.getId());
			object.setValue(v.getAggregationType());
			return object;
		}).collect(Collectors.toList());
	}

	@Override
	public List<?> getAllIndicators() {
		return indicatorRepository.getNumberIndicators().stream().map(v -> {
			IndicatorModel indicatorModel = new IndicatorModel();
			indicatorModel.setIndicatorId(Integer.valueOf((String) v.getIndicatorDataMap().get("indicatorNid")));
			indicatorModel.setIndicatorName((String) v.getIndicatorDataMap().get("indicatorName"));
			indicatorModel.setFormId(Integer.valueOf((String) v.getIndicatorDataMap().get("formId")));

			return indicatorModel;
		}).collect(Collectors.toList());

	}

	@Override
	public Map<Integer, Map<Integer, List<TypeDetailsModel>>> getTypeDetails() {
		return typeDetailsRepository.findAll().stream().filter(v->v.getFormId()!=null).map(typeDetail -> {
			TypeDetailsModel detailsModel = new TypeDetailsModel();
			detailsModel.setFormId(typeDetail.getFormId());
			detailsModel.setTypeDetailId(typeDetail.getSlugId());
			detailsModel.setTypeDetailName(typeDetail.getName());
			detailsModel.setTypeId(typeDetail.getType().getSlugId());

			return detailsModel;
		}).collect(Collectors.groupingBy(TypeDetailsModel::getFormId,
				Collectors.groupingBy(type -> type.getTypeId(), Collectors.toList())));
	}

	@Override
	public String saveIndiactor(IndicatorConfigModel dataModel) {
		try {
			Map<Integer, Question> questions = questionRepository
					.findAllByFormIdOrderByQuestionOrderAsc(dataModel.getFormId()).stream()
					.collect(Collectors.toMap(Question::getQuestionId, v -> v));

			Map<Integer, Map<String, Object>> indicatorsMap = indicatorRepository.getNumberIndicators().stream()
					.collect(
							Collectors.toMap(k -> Integer.valueOf((String) k.getIndicatorDataMap().get("indicatorNid")),
									v -> v.getIndicatorDataMap()));

			Indicator lastIndicatorNid = indicatorRepository.findTopByOrderByIdDesc();

			Indicator indicator = new Indicator();
			indicator.setIndicatorDataMap(setIndicatorsValue(dataModel, questions, lastIndicatorNid, indicatorsMap));

			indicatorRepository.save(indicator);

			return env.getProperty("msg.success");
		} catch (Exception e) {
			e.printStackTrace();
			return env.getProperty("msg.failure");
		}

	}

	private Map<String, Object> setIndicatorsValue(IndicatorConfigModel dataModel, Map<Integer, Question> question,
			Indicator lastIndicatorNid, Map<Integer, Map<String, Object>> indicatorsMap) {
		Map<String, Object> indicatorMap = new HashMap<String, Object>();

		indicatorMap.put("formId", dataModel.getFormId().toString());
		indicatorMap.put("area", "data." + dataModel.getAreaColumn());
		indicatorMap.put("aggregationType", dataModel.getAggregationType());
		indicatorMap.put("indicatorName", dataModel.getIndicatorName());

		indicatorMap.put("subgroup", dataModel.getSubgroup());

		if (dataModel.getConditions().get(0).getQuestion() != null) {
			String aggRule = getAggRule(dataModel.getConditions(), question);
			indicatorMap.put("aggregationRule", aggRule);
		} else if (dataModel.getAggregationRule() != null && dataModel.getAggregationRule().equals("repeatCount")) {
			String aggRule = dataModel.getAggregationRule() + ":"
					+ (Arrays.asList(dataModel.getQuestionColumnName().split(","))).stream().map(v -> "data." + v)
							.collect(Collectors.joining(","));
			indicatorMap.put("aggregationRule", aggRule);
		} else {
			indicatorMap.put("aggregationRule", dataModel.getAggregationRule());
		}

		indicatorMap.put("subsector", dataModel.getSubsector());
		indicatorMap.put("collection", env.getProperty("form.id." + dataModel.getFormId()));
		indicatorMap.put("parentColumn", dataModel.getParentColumnName());

		Integer indicatorNid = (lastIndicatorNid != null)
				? (Integer.valueOf((String) lastIndicatorNid.getIndicatorDataMap().get("indicatorNid")) + 1)
				: 1;

		indicatorMap.put("indicatorNid", indicatorNid.toString());

		indicatorMap.put("parentType",
				dataModel.getParentColumnName() != null
						? (dataModel.getParentColumnName().contains("BEGINREPEAT")
								? ("begin-repeat:" + dataModel.getControllerType())
								: ((dataModel.getControllerType().equals("segment")
										|| (dataModel.getControllerType().equals("dropdown"))
												? "dropdown"
												: (dataModel.getControllerType().equals("textbox") ? "numeric"
														: dataModel.getControllerType()))))
						: ((dataModel.getAggregationType().equals("percent")
								|| dataModel.getAggregationType().equals("avg")) ? "indicator"
										: (dataModel.getAggregationType().equals("count")) ? "form" : null));

		indicatorMap.put("numerator", dataModel.getParentColumnName() != null
				? (dataModel.getParentColumnName().contains("BEGINREPEAT")
						? (dataModel.getParentColumnName() + "."
								+ question.get(dataModel.getQuestionId()).getColumnName())
						: (dataModel.getQuestionId() != null ? question.get(dataModel.getQuestionId()).getColumnName()
								: null))
				: (dataModel.getAggregationType().equals("percent") || dataModel.getAggregationType().equals("avg"))
						? dataModel.getNumerator()
						: null);

		indicatorMap.put("denominator", dataModel.getDenominator());
		indicatorMap.put("unit", dataModel.getUnit());
		indicatorMap.put("periodicity", dataModel.getPeriodicity());
		indicatorMap.put("highIsGood", dataModel.isHighIsGood());
		indicatorMap.put("sector", dataModel.getSector());
		indicatorMap.put("typeDetailId", (dataModel.getTypeDetails() != null && dataModel.getTypeDetails().size() > 0)
				? dataModel.getTypeDetails().stream().map(v -> String.valueOf(v)).collect(Collectors.joining("#"))
				: null);

		return indicatorMap;
	}

	private String getAggRule(List<ConditionModel> conditions, Map<Integer, Question> question) {
		String aggRule = null;
		if (conditions.size() > 0 && conditions.get(0).getQuestion() != null) {
			aggRule = "";
			for (ConditionModel conditionModel : conditions) {
				if (question.get(conditionModel.getQuestion()).getParentColumnName().contains("BEGINREPEAT")) {
					aggRule += "and$" + conditionModel.getOperator() + "(data."
							+ question.get(conditionModel.getQuestion()).getParentColumnName() + "."
							+ question.get(conditionModel.getQuestion()).getColumnName() + ":"
							+ conditionModel.getValue().intValue() + ");";
				} else {
					aggRule += "and$" + conditionModel.getOperator() + "(data."
							+ question.get(conditionModel.getQuestion()).getColumnName() + ":"
							+ conditionModel.getValue().intValue() + ");";
				}
			}
		}
		return aggRule != null ? aggRule.substring(0, aggRule.length() - 1) : null;
	}

	@Override
	public String addSector(SectorModel sectorModel) {
		Sector lastSector = sectorRepository.findTopByOrderByIdDesc();

		Sector sector = new Sector();
		sector.setFormId(sectorModel.getFormId());
		sector.setSectorId((lastSector != null) ? (lastSector.getSectorId() + 1) : 1);
		sector.setSectorName(sectorModel.getSectorName());

		sectorRepository.save(sector);
		return env.getProperty("sector.add");
	}

	@Override
	public String addSubSector(SubsectorModel subsectorModel) {
		Map<Integer, Sector> sectors = sectorRepository.findAll().stream()
				.collect(Collectors.toMap(Sector::getSectorId, v -> v));
		Subsector lastSubSector = subsectorRepository.findTopByOrderByIdDesc();

		Subsector subsector = new Subsector();
		subsector.setFormId(subsectorModel.getSectorId());
		subsector.setSector(sectors.get(subsectorModel.getSectorId()));
		subsector.setSubsectorName(subsectorModel.getSubsectorName());
		subsector.setSubSectorId((lastSubSector != null) ? (lastSubSector.getSubSectorId() + 1) : 1);

		subsectorRepository.save(subsector);
		return env.getProperty("subsector.add");
	}

	@Override
	public List<?> getIndicatorsForView() {
		return indicatorRepository.findAll();
	}

	@Override
	public List<?> getAllSectors() {
		return sectorRepository.findAll();
	}

	@Override
	public List<?> getAllSubSectors() {
		return subsectorRepository.findAll();
	}

	@Override
	public String updateOne(Indicator indicatorInfo) {
		System.out.println(indicatorInfo);
		Query query = new Query(Criteria.where("_id").is(indicatorInfo.getId()));
		UpdateResult result = mongoTemplate.updateFirst(query,
				Update.update("indicatorDataMap", indicatorInfo.getIndicatorDataMap()), Indicator.class);

		return result.getModifiedCount() > 0 ? env.getProperty("indicator.update")
				: env.getProperty("indicator.update.failed");
	}

	@Override
	public String excelDownloadIndicators() {
		String outputPathExcel = null;
		try {
			String date = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
			String path = outputPath.toAbsolutePath().toString();

			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}

			outputPathExcel = path + "/All_Indicators_" + date + ".xlsx";

			Map<Integer, String> engineForms = engineFormRepository.findAll().stream()
					.collect(Collectors.toMap(EnginesForm::getFormId, EnginesForm::getName));
			
			Map<String, List<IndicatorMapModel>> indicatorsMap = indicatorRepository.findAll().stream().map(v -> {

				IndicatorMapModel model = new IndicatorMapModel();
				model.setIndicatorNid(v.getIndicatorDataMap().get("indicatorNid") != null? v.getIndicatorDataMap().get("indicatorNid").toString(): null);
				model.setIndicatorName(v.getIndicatorDataMap().get("indicatorName").toString());
				model.setParentColumn(v.getIndicatorDataMap().get("parentColumn") != null? v.getIndicatorDataMap().get("parentColumn").toString(): null);
				model.setUnit(v.getIndicatorDataMap().get("unit") != null ? v.getIndicatorDataMap().get("unit").toString(): null);
				model.setSubgroup(v.getIndicatorDataMap().get("subgroup") != null? v.getIndicatorDataMap().get("subgroup").toString(): null);
				model.setNumerator(v.getIndicatorDataMap().get("numerator") != null	? v.getIndicatorDataMap().get("numerator").toString(): null);
				model.setDenominator(v.getIndicatorDataMap().get("denominator") != null	? v.getIndicatorDataMap().get("denominator").toString()	: null);
				model.setAggregationType(v.getIndicatorDataMap().get("aggregationType").toString());
				model.setParentType(v.getIndicatorDataMap().get("parentType") != null? v.getIndicatorDataMap().get("parentType").toString(): null);
				model.setFormId(v.getIndicatorDataMap().get("formId").toString());
				model.setPeriodicity(v.getIndicatorDataMap().get("periodicity") != null	? v.getIndicatorDataMap().get("periodicity").toString()	: null);
				model.setAggregationRule(v.getIndicatorDataMap().get("aggregationRule") != null? v.getIndicatorDataMap().get("aggregationRule").toString(): null);
				model.setHighIsGood(v.getIndicatorDataMap().get("highIsGood") != null ? v.getIndicatorDataMap().get("highIsGood").toString(): null);
				model.setTypeDetailId(v.getIndicatorDataMap().get("typeDetailId") != null? v.getIndicatorDataMap().get("typeDetailId").toString(): null);
				model.setArea(v.getIndicatorDataMap().get("area").toString());
				model.setCollection(v.getIndicatorDataMap().get("collection") != null? v.getIndicatorDataMap().get("collection").toString()	: null);
				model.setSector(v.getIndicatorDataMap().get("sector") != null ? v.getIndicatorDataMap().get("sector").toString(): null);
				model.setSubsector(v.getIndicatorDataMap().get("subsector") != null? v.getIndicatorDataMap().get("subsector").toString(): null);
				
				
				
				return model;
			}).collect(Collectors.groupingBy(IndicatorMapModel::getFormId));

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = null;

			XSSFRow row = null;
			XSSFCell cell = null;
			int rowNum = 0;
			int cellNum = 0;
			
			Field[] fields = IndicatorMapModel.class.getDeclaredFields();
			
			for (Entry<String, List<IndicatorMapModel>> indicator : indicatorsMap.entrySet()) {

				sheet = workbook.createSheet(engineForms.containsKey(Integer.valueOf(indicator.getKey()))
						? engineForms.get(Integer.valueOf(indicator.getKey()))
						: "Master Data");
				
				row = sheet.createRow(rowNum);
				for(Field field : fields) {
					cell = row.createCell(cellNum);
					cell.setCellValue(field.getName());
					cellNum++;
				}
				
				rowNum++;
				cellNum = 0;
				for (IndicatorMapModel mapModel : indicator.getValue()) {

					row = sheet.createRow(rowNum);
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getIndicatorNid());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getIndicatorName());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getParentColumn());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getUnit());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getSubgroup());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getNumerator());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getDenominator());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getAggregationType());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getParentType());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getFormId());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getPeriodicity());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getAggregationRule());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getHighIsGood());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getTypeDetailId());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getArea());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getCollection());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getSector());
					
					cellNum++;
					cell = row.createCell(cellNum);
					cell.setCellValue(mapModel.getSubsector());

					
					rowNum++;
					cellNum = 0;
				}
				rowNum = 0;
				cellNum = 0;
			}

			FileOutputStream fileOutputStream = new FileOutputStream(new File(outputPathExcel));
			workbook.write(fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();

			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputPathExcel;
	}

}
