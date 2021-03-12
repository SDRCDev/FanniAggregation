package org.sdrc.datum19.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.ClusterDataValue;
import org.sdrc.datum19.document.ClusterForAggregation;
import org.sdrc.datum19.document.ClusterMapping;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.ClusterDataValueRepository;
import org.sdrc.datum19.repository.ClusterMappingRepository;
import org.sdrc.datum19.repository.ClusterRepositoty;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.util.AreaMapObject;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class ClusterAggregationService {
	
	@Autowired
	private ClusterMappingRepository clusterMappingRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private ClusterDataValueRepository clusterDataValueRepository;
	
	@Autowired
	private ClusterRepositoty clusterRepositoty;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	public String createClusterCollection() {
		List<ClusterForAggregation> clusterForAggregations=new ArrayList<>();
		List<ClusterMapping> clusterList=clusterMappingRepository.findAll();
		clusterList.forEach(c->{
			ClusterForAggregation clusterForAggregation=new ClusterForAggregation();
			clusterForAggregation.setBlock(c.getBlock().getAreaId());
			clusterForAggregation.setClusterNumber(c.getClusterNumber());
			clusterForAggregation.setDistrict(c.getDistrict().getAreaId());
			clusterForAggregation.setVillage(c.getVillage().getAreaId());
			
			clusterForAggregations.add(clusterForAggregation);
		});
		clusterRepositoty.saveAll(clusterForAggregations);
		return "success";
	}
	
	public AreaMapObject getClusterList(List<Integer> villageList){
		AreaMapObject amb = new AreaMapObject();
		List<ClusterMapping> clusterList=new ArrayList<>();
		List<Integer> childIds=new ArrayList<Integer>();
		Map<Integer, Integer> areaMap=new HashMap<Integer, Integer>();
		clusterList=clusterMappingRepository.findAllByVillageIn(areaRepository.findByAreaIdIn(villageList));
		clusterList.forEach(child->{
			childIds.add(child.getVillage().getAreaId());
			areaMap.put(child.getVillage().getAreaId(), child.getClusterNumber());
		});
		amb.setAreaList(childIds);
		amb.setAreaMap(areaMap);
		return amb;
	}
	Integer timePeriodId=null;
	public String aggregateCluster(String periodicity,Integer tp) {
		timePeriodId=tp;
		try {
//				AreaMapObject amb = getClusterList(villageIds);
//				List<Integer> areaList=amb.getAreaList();
			List<Map> areaDataMap=mongoTemplate.aggregate(aggregateAreaTree(tp),DataValue.class,Map.class).getMappedResults();
			List<ClusterDataValue> datalist2=new ArrayList<>();
			areaDataMap.forEach(data->{
				ClusterDataValue datavalue=new ClusterDataValue();
				datavalue.setInid(Integer.valueOf(String.valueOf(data.get("inid"))));
				datavalue.setAreaId(Integer.valueOf(String.valueOf(data.get("clusterNumber"))));
				datavalue.setDataValue(Double.valueOf(String.valueOf(data.get("dataValue"))));
				datavalue.setTp(Integer.valueOf(String.valueOf(data.get("tp"))));
				datavalue.set_case(String.valueOf(data.get("_case")));
				
				datalist2.add(datavalue);
			});
			clusterDataValueRepository.saveAll(datalist2);
			aggregateFinalIndicators(periodicity,"indicator");
		} catch (Exception e) {
			System.out.println("error encountered :: ");
			e.printStackTrace();
		}
		return null;
	}
	
private TypedAggregation<DataValue> aggregateAreaTree(Integer tp) {
		
		MatchOperation matchOperation=Aggregation.match(Criteria.where("tp").is(tp));
		LookupOperation lookupOperation=Aggregation.lookup("clusterForAggregation", "datumId", "village", "parent");
		UnwindOperation unwindOperation=Aggregation.unwind("parent");
		GroupOperation groupOperation=Aggregation.group("parent.clusterNumber","inid","tp","_case").sum("dataValue").as("dataValue");
		
		return Aggregation.newAggregation(DataValue.class,matchOperation,lookupOperation,unwindOperation,groupOperation);
	}

List<ClusterDataValue> percentDataMap=null;
List<ClusterDataValue> percentDataMapAll=null;
public List<ClusterDataValue> aggregateFinalIndicators(String periodicity, String indicatorType) {
	percentDataMap=new ArrayList<>();
	percentDataMapAll=new ArrayList<>();
	List<Indicator> indicatorList = indicatorRepository.getPercentageIndicators(periodicity,indicatorType);
	indicatorList.forEach(indicator->{
		if(Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("formId")))==6 || 
				Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("formId")))==12) {
			return;
		}
		List<Integer> dependencies=new ArrayList<>();
		List<Integer> numlist=new ArrayList<>();
		String[] numerators=String.valueOf(indicator.getIndicatorDataMap().get("numerator")).split(",");
		Integer inid=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")));
		System.out.println(inid);
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
				percentDataMap=mongoTemplate.aggregate(getPercentData(dependencies,numlist,denolist,aggrule),ClusterDataValue.class,ClusterDataValue.class).getMappedResults();
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
				percentDataMap=mongoTemplate.aggregate(getAvgData(dependencies,numlist,denolist,aggrule),ClusterDataValue.class,ClusterDataValue.class).getMappedResults();
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
	clusterDataValueRepository.saveAll(percentDataMapAll);
	return percentDataMapAll;
}

private TypedAggregation<ClusterDataValue> getAvgData(List<Integer> dependencies, List<Integer> numlist, List<Integer> denolist,
		String aggrule) {
	// TODO Auto-generated method stub
	MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dependencies).and("tp").is(timePeriodId));
	GroupOperation groupOperation=null;
	ProjectionOperation projectionOperation=null;
	ProjectionOperation p1=null;
	ProjectionOperation p2=null;
	groupOperation=Aggregation.group("areaId","tp").sum(when(where("inid").in(numlist)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
			.sum(when(where("inid").in(denolist)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
	projectionOperation=Aggregation.project().and("_id.areaId").as("areaId")
			.and("_id.tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
			.andExclude("_id")
			.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf("numerator").divideBy("denominator")).otherwise(0)).as("dataValue");
	return Aggregation.newAggregation(ClusterDataValue.class,matchOperation,groupOperation,projectionOperation);
}

private TypedAggregation<ClusterDataValue> getPercentData(List<Integer> dep,List<Integer> num,List<Integer> deno, String rule){
	MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dep).and("tp").is(timePeriodId));
	GroupOperation groupOperation=null;
	ProjectionOperation projectionOperation=null;
	ProjectionOperation p1=null;
	ProjectionOperation p2=null;
	if(rule.equals("sub")) {
		groupOperation=Aggregation.group("areaId","tp").sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator")
				.sum(Sum.sumOf(when(where("inid").is(num.get(0))).then("$dataValue").otherwise(0))).as("n1")
				.sum(when(where("inid").is(num.get(1))).then("$dataValue").otherwise(0)).as("n2");
		
		p1=Aggregation.project().and("areaId").as("areaId").and("tp").as("tp").and(Subtract.valueOf("n1").subtract("n2")).as("numerator").and("denominator").as("denominator");
		
		p2=Aggregation.project().and("areaId").as("areaId").and("tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
				.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(ClusterDataValue.class,matchOperation,groupOperation,p1,p2);
	}else {
	groupOperation=Aggregation.group("areaId","tp").sum(when(where("inid").in(num)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
			.sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
	projectionOperation=Aggregation.project().and("_id.areaId").as("areaId")
			.and("_id.tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
			.andExclude("_id")
			.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
			.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
	return Aggregation.newAggregation(ClusterDataValue.class,matchOperation,groupOperation,projectionOperation);
	}
}
	
}
