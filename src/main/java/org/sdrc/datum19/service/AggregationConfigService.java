package org.sdrc.datum19.service;

import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.util.ValueObject;

public interface AggregationConfigService {
	
	List<ValueObject> getAllForm();
	
	Map<Integer, List<QuestionModel>> getQuestions();

	String saveIndiactor(IndicatorConfigModel dataModel);
	
	Map<Integer, Map<Integer, List<TypeDetailsModel>>> getTypeDetails();

	List<ValueObject> getAggregationTypes();
	
	List<?> getAllIndicators();

	String addSector(SectorModel sectorModel);
	
	String addSubSector(SubsectorModel subsectorModel);

	List<?> getIndicatorsForView();

	List<?> getAllSectors();

	List<?> getAllSubSectors();
	
	String updateOne(Indicator indicatorInfo);

	String excelDownloadIndicators();

}
