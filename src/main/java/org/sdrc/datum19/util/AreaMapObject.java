package org.sdrc.datum19.util;

import java.util.List;
import java.util.Map;

import lombok.Data;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Data
public class AreaMapObject {
	private List<Integer> areaList;
	private Map<Integer, Integer> areaMap;
}
