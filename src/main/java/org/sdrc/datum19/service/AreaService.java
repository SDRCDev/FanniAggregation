package org.sdrc.datum19.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.repository.AreaLevelRepository;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.util.AreaMapObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Service
public class AreaService {

	@Autowired
	private AreaRepository areaRepo;
	
	@Autowired
	private AreaLevelRepository areaLevelRepository;
	
	public AreaMapObject getAreaForAggregation(Integer areaLevel){
		AreaMapObject amb = new AreaMapObject();
		Map<Integer, Integer> areaMap=new HashMap<Integer, Integer>();
		List<Integer> childIds=new ArrayList<Integer>();
//		areaList.forEach(area->{
			List<Area> childList=areaRepo.findByAreaLevel(areaLevelRepository.findByAreaLevelId(areaLevel));
			childList.forEach(child->{
				childIds.add(child.getAreaId());
				areaMap.put(child.getAreaId(), child.getParentAreaId());
			});
//		});
		amb.setAreaList(childIds);
		amb.setAreaMap(areaMap);
		return amb;
	}
}
