package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.DataValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataDomainRepository extends MongoRepository<DataValue, String> {

	List<DataValue> findByDatumIdAndTpIn(Integer blockId, List<Integer> asList);

	List<DataValue> findByDatumIdAndInidAndTpIn(Integer blockId, Integer indicatorId, List<Integer> asList);

	List<DataValue> findTop4ByDatumIdAndInidOrderByTpDesc(Integer areaId, Integer indicatorId);

	List<DataDomainRepository> deleteByTpIn(List<Integer> asList);

	
}
