package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Indicator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IndicatorRepository extends MongoRepository<Indicator, String> {
	@Query("{'indicatorDataMap.periodicity':?0}")
	List<Indicator> getIndicatorByPeriodicity(String periodcity);

	@Query("{'indicatorDataMap.periodicity':?0, 'indicatorDataMap.parentType':?1}")
	List<Indicator> getPercentageIndicators(String periodicity, String indicatorType);
	
	Indicator findTopByOrderByIdDesc();
	
	@Query("{'indicatorDataMap.unit':'number'}")
	List<Indicator> getNumberIndicators();

}
