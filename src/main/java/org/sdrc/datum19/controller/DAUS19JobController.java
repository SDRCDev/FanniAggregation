package org.sdrc.datum19.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.sdrc.datum19.service.MongoAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * 
 */

@Controller
@EnableScheduling
public class DAUS19JobController {
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	
	@Autowired
	private MongoAggregationService mongoAggregationService;

	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	private SimpleDateFormat simpleDateformater = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	@Scheduled(cron="0 0 0 11 * ?")
	@GetMapping("/runJob")
	public void runMonthlyJob() throws ParseException, InvalidFormatException, IOException {
		TimePeriod tp=getTimePeriodForAggregation();
//		aggregationService.aggregateDependencies(tp.getTimePeriodId(), "monthly");
		mongoAggregationService.aggregate(tp.getTimePeriodId(), "monthly");
	}
	
	@Scheduled(cron="0 0 0 10 JAN,APR,JUL,OCT ?")
	@GetMapping("/runQuarterlyJob")
	public void runQuarterlyJob() throws ParseException, InvalidFormatException, IOException {
		TimePeriod tp=getQuarterForAggregation();
//		aggregationService.aggregateDependencies(tp.getTimePeriodId(), "quarterly");
		mongoAggregationService.aggregate(tp.getTimePeriodId(), "quarterly");
	}
	
	@Scheduled(cron="0 0 0 10 JAN ?")
	@GetMapping("/runYearlyJob")
	public void runYearlyJob() throws ParseException, InvalidFormatException, IOException {
		TimePeriod tp=getYearForAggregation();
//		aggregationService.aggregateDependencies(tp.getTimePeriodId(), "yearly");
		mongoAggregationService.aggregate(tp.getTimePeriodId(), "once");
	}
	
	public TimePeriod getYearForAggregation() throws ParseException {
		// TODO Auto-generated method stub
		Calendar endDateCalendar = Calendar.getInstance();
		endDateCalendar.add(Calendar.MONTH, -1);
		endDateCalendar.set(Calendar.DATE, 1);
		endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		

		Date eDate = endDateCalendar.getTime();
		String endDateStr = simpleDateformater.format(eDate);
		Date endDate = (Date) formatter.parse(endDateStr + " 23:59:59.000");
		
		Calendar startDateCalendar1 = Calendar.getInstance();
		startDateCalendar1.add(Calendar.MONTH, -12);
		startDateCalendar1.set(Calendar.DATE, 1);
		Date startDate1 = (Date) formatter.parse(simpleDateformater.format(startDateCalendar1.getTime()) + " 00:00:00.000");
		String sd=toISO8601UTC(new java.util.Date(startDate1.getTime()));
		String ed=toISO8601UTC(new java.util.Date(endDate.getTime()));
		
		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(sd, ed);
		return timePeriod;
	}

	public TimePeriod getQuarterForAggregation() throws ParseException {
		// TODO Auto-generated method stub
		Calendar endDateCalendar = Calendar.getInstance();
		endDateCalendar.add(Calendar.MONTH, -1);
		endDateCalendar.set(Calendar.DATE, 1);
		endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		

		Date eDate = endDateCalendar.getTime();
		String endDateStr = simpleDateformater.format(eDate);
		Date endDate = (Date) formatter.parse(endDateStr + " 23:59:59.000");
		
		Calendar startDateCalendar1 = Calendar.getInstance();
		startDateCalendar1.add(Calendar.MONTH, -3);
		startDateCalendar1.set(Calendar.DATE, 1);
		Date startDate1 = (Date) formatter.parse(simpleDateformater.format(startDateCalendar1.getTime()) + " 00:00:00.000");
		String sd=toISO8601UTC(new java.util.Date(startDate1.getTime()));
		String ed=toISO8601UTC(new java.util.Date(endDate.getTime()));
		
		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(sd, ed);
		return timePeriod;
	}

	public TimePeriod getTimePeriodForAggregation() throws ParseException {
		Calendar endDateCalendar = Calendar.getInstance();
		endDateCalendar.add(Calendar.MONTH, -1);
		endDateCalendar.set(Calendar.DATE, 1);
		endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		Date eDate = endDateCalendar.getTime();
		String endDateStr = simpleDateformater.format(eDate);
		Date endDate = (Date) formatter.parse(endDateStr + " 23:59:59.000");
		
		Calendar startDateCalendar1 = Calendar.getInstance();
		startDateCalendar1.add(Calendar.MONTH, -1);
		startDateCalendar1.set(Calendar.DATE, 1);
		Date startDate1 = (Date) formatter.parse(simpleDateformater.format(startDateCalendar1.getTime()) + " 00:00:00.000");
		String sd=toISO8601UTC(new java.util.Date(startDate1.getTime()));
		String ed=toISO8601UTC(new java.util.Date(endDate.getTime()));
		
//		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(cd);
		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(sd, ed);
		return timePeriod;
	}
	
	public static String toISO8601UTC(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		return df.format(date);
		}
		public static Date fromISO8601UTC(String dateStr) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		try {
		return df.parse(dateStr);
		} catch (ParseException e) {
		e.printStackTrace();
		}
		return null;
		}
		
		@ResponseBody
		@Scheduled(cron="0 1 0 1/1 * ? ")
		@GetMapping("/testUrl")
	public void dailyJob() throws ParseException {
		
		
		// Timeperiod creation for nextday
		String financialyear;
		int month = Calendar.getInstance().get(Calendar.MONTH);
		if(month==0 || month==1||month==2) {
			financialyear=String.valueOf(Calendar.getInstance().get(Calendar.YEAR)-1)+"-"+String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		}
		else {
			financialyear=String.valueOf(Calendar.getInstance().get(Calendar.YEAR))+"-"+String.valueOf(Calendar.getInstance().get(Calendar.YEAR)+1);
		}
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		//calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date today = calendar.getTime();
		SimpleDateFormat formatter =  new SimpleDateFormat("dd-M-yyyy");  
		String strDate  = formatter.format(today)+" 12:00:00";
		Date startdate=new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse(strDate);
		String endDate  = formatter.format(today)+" 11:59:59";
		Date enddate=new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse(endDate);
		
		
		int year = Calendar.getInstance().get(Calendar.YEAR);
		TimePeriod timePeriod = null;
		List<TimePeriod> timePeriods = timePeriodRepository.findByPeriodicity("0");
		
		// Aggregation
		dataDomainRepository.deleteByTpIn(Arrays.asList(timePeriods.get(0).getTimePeriodId()));
		
		if(!timePeriods.isEmpty() && timePeriods != null) {
		mongoAggregationService.aggregate(timePeriods.get(0).getTimePeriodId(),"daily");
		}
		mongoAggregationService.importCRCDataValue();
		
		if(timePeriods.isEmpty() && timePeriods== null) {
			 timePeriod = new TimePeriod();
			 timePeriod.setCreatedDate(new Date());
			 timePeriod.setStartDate(enddate);
			 timePeriod.setTimePeriodId(timePeriodRepository.findAll().size()+1);
		}else {
			timePeriod=timePeriods.get(0);
		}
		
		
		timePeriod.setEndDate(startdate);
		timePeriod.setFinancialYear(financialyear);
		timePeriod.setPeriodicity(String.valueOf(0));
		timePeriod.setYear(Calendar.getInstance().get(Calendar.YEAR));
		timePeriodRepository.save(timePeriod);

	}
}
