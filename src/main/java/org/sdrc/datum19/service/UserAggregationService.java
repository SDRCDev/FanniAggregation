package org.sdrc.datum19.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.UserDatumValue;
import org.sdrc.datum19.repository.AccountRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.UserDataValueRepository;
import org.sdrc.datum19.usermgmt.Account;
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
public class UserAggregationService {
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private UserDataValueRepository userDataValueRepository;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	private Integer tp;
	public void aggregateUserWise(String periodicity,Integer timePeriod) {
		this.tp=timePeriod;
		Map<Integer, String> _map=getUsersByArea();
		List<Map>userDataMap=mongoTemplate.aggregate(getUserData(timePeriod),Account.class,Map.class).getMappedResults();
		List<UserDatumValue> userDataList=new ArrayList<UserDatumValue>();
		userDataMap.forEach(userData->{
			UserDatumValue userDatumValue=new UserDatumValue();
			userDatumValue.setDatumId(String.valueOf(userData.get("userName")));
			userDatumValue.setDatumtype("userDatum");
			userDatumValue.setDataValue(Double.parseDouble(String.valueOf(userData.get("dataValue"))));
			userDatumValue.setTp(timePeriod);
			userDatumValue.setInid(Integer.parseInt(String.valueOf(userData.get("inid"))));
			
			userDataList.add(userDatumValue);
		});
		userDataValueRepository.saveAll(userDataList);
		try {
			aggregateFinalIndicators(periodicity,"indicator");
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(userDataMap);
	}
	
	List<UserDatumValue> percentDataMap=null;
	List<UserDatumValue> percentDataMapAll=null;
	public List<UserDatumValue> aggregateFinalIndicators(String periodicity, String indicatorType) {
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
					percentDataMap=mongoTemplate.aggregate(getPercentData(dependencies,numlist,denolist,aggrule),UserDatumValue.class,UserDatumValue.class).getMappedResults();
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
					percentDataMap=mongoTemplate.aggregate(getAvgData(dependencies,numlist,denolist,aggrule),UserDatumValue.class,UserDatumValue.class).getMappedResults();
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
		userDataValueRepository.saveAll(percentDataMapAll);
		return percentDataMapAll;
	}
	
	private TypedAggregation<UserDatumValue> getPercentData(List<Integer> dep,List<Integer> num,List<Integer> deno, String rule){
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dep).and("tp").is(tp));
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
			return Aggregation.newAggregation(UserDatumValue.class,matchOperation,groupOperation,p1,p2);
		}else {
		groupOperation=Aggregation.group("datumId","tp").sum(when(where("inid").in(num)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
				.sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
		projectionOperation=Aggregation.project().and("_id.datumId").as("datumId")
				.and("_id.tp").as("tp").and("numerator").as("numerator").and("denominator").as("denominator")
				.andExclude("_id")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
				.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(UserDatumValue.class,matchOperation,groupOperation,projectionOperation);
		}
	}
	
	private TypedAggregation getAvgData(List<Integer> dependencies, List<Integer> numlist, List<Integer> denolist,
			String aggrule) {
		// TODO Auto-generated method stub
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dependencies).and("tp").is(tp));
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
		return Aggregation.newAggregation(UserDatumValue.class,matchOperation,groupOperation,projectionOperation);
	}
	
	private Aggregation getUserData(Integer timePeriodId){
//		MatchOperation matchOperation = Aggregation.match(Criteria.where("tp").is(timePeriodId));
//		LookupOperation lookupOperation=Aggregation.lookup("account", "datumId", "mappedAreaIds", "user");
//		UnwindOperation unwindOperation=Aggregation.unwind("user");
//		GroupOperation groupOperation=Aggregation.group("inid","user.userName").sum("dataValue").as("dataValue");
		
		LookupOperation lookupOperation=Aggregation.lookup("dataValue", "mappedAreaIds", "datumId", "data");
		UnwindOperation unwindOperation=Aggregation.unwind("data");
		MatchOperation matchOperation = Aggregation.match(Criteria.where("data.tp").is(timePeriodId).and("data.datumtype").is("area"));
		GroupOperation groupOperation=Aggregation.group("data.inid","userName").sum("data.dataValue").as("dataValue");
		return Aggregation.newAggregation(lookupOperation,unwindOperation,matchOperation,groupOperation);
	}
	private Map<Integer, String> getUsersByArea(){
		Map<Integer, String> userAreaMap=new HashMap<>();
		List<Account> accounts=accountRepository.findAll();
		accounts.forEach(user->{
			for (int i = 0; i < user.getMappedAreaIds().size(); i++) {
				userAreaMap.put(user.getMappedAreaIds().get(i), user.getUserName());
			}
		});
		return userAreaMap;
	}

}
