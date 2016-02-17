package org.qipeng.test.example01;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DataDetailJob implements Job{
	
	/*private String stringData;
	
	private float floatValue;
	
	private List<String> listData;
	
	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public void setListData(List<String> listData) {
		this.listData = listData;
	}*/

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		
		System.out.println("stringData:"+jobDataMap.getString("stringData"));
		System.out.println("floatValue:"+jobDataMap.getFloatValue("floatValue"));
		System.out.println("listData size:"+((List<String>)jobDataMap.get("listData")).size());
		
		/*System.out.println("stringData:"+stringData);
		System.out.println("floatValue:"+floatValue);
		System.out.println("listData size:"+listData.size());*/
		
		System.out.println("DataDetailJob " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		
	}

}
