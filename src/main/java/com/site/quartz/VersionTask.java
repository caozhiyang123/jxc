package com.site.quartz;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.cron4j.ITask;
import com.site.core.model.Dealer;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.OpenTimeManage;
import com.site.core.model.SecondaryDealerInventoryData;
import com.site.core.model.common.OpenTimeStep;
import com.site.core.model.dto.SecondaryDealerDto;
import com.site.service.CalculateService;
import com.site.service.DealerTwoService;
import com.site.service.RoleConfigService;
import com.site.service.VersionDataService;
import com.site.utils.DateUtils;
import com.site.utils.EMailUtil;

import java.util.*;

/**
 * 封存版本数据
 */
public class VersionTask implements ITask {

    private static final List<String> emails = Arrays.asList(new String[]{"jason.wang@jetdata.com.cn"});
    
    
    public static boolean addZeroForT1 = true;
    
    public static boolean addZeroForT2 = true;

    @Override
    public void stop() {
    }

    @Override
    public void run() {
    	
    	OpenTimeManage openTime1 = OpenTimeManage.dao.findFirst("select * from open_time_manage where status =? and level = 1", "1");
    	OpenTimeManage openTime2 = OpenTimeManage.dao.findFirst("select * from open_time_manage where status =? and level = 2", "1");

       Calendar calendar = Calendar.getInstance();
    	if (null != openTime1 && (openTime1.getFirstEndDay() - 1) == calendar.get(Calendar.DATE) && addZeroForT1) 
    	{
    		addZeroForT1 = false;
    		addZeroForT1();
    	}
    	
     	if (null != openTime2 && (openTime2.getFirstEndDay() - 1) == calendar.get(Calendar.DATE) && addZeroForT2) 
     	{
     		addZeroForT2 = false;
     		addZeroForT2();
     	}    	
    	
        String version = DateUtils.getNowDate();
        Map<String, JSONObject> result = VersionDataService.me.saveVersionData(version);
        //T1经销商任务详情
        JSONObject firstJson = result.get("firstLevel");
        //任务执行失败发送邮件通知
        if (!firstJson.getBoolean("success")) {
            sendEmail(firstJson);
        }
        //二级经销商任务详情
        JSONObject secondaryJson = result.get("secondaryLevel");
        //任务执行失败发送邮件通知
        if (!secondaryJson.getBoolean("success")) {
            sendEmail(secondaryJson);
        }
    }

    private void sendEmail(JSONObject emailParamMap) {
        String host = PropKit.get("email.smtp");
        String from = PropKit.get("email.from");
        String copyto = PropKit.get("email.copyto");
        String username = PropKit.get("email.username");
        String password = PropKit.get("email.password");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("content", getEmailTemplate(emailParamMap));
        emails.forEach(email -> EMailUtil.sendAndCc(host, from, email, copyto, "版本数据任务提醒", paramMap, username, password));
    }

    /**
     * 设置邮件模板
     * @param map
     * @return
     */
    public String getEmailTemplate(JSONObject map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;font-weight:bolder;\">您好：</p>");
        sb.append("<p class=\"body\" style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style=\"color:#FF5722;\">版本号：" + map.getString("version") + "<br/>时间:" + map.getString("month") + "</span><br/><span style=\"color:#5FB878;\">"+ map.getString("level") +"级经销商</span> 保存版本数据任务执行失败！");
        sb.append("<div class=\"body salutation\" style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;float:right;margin-left:50%;\">来自系统邮件</div>");
        return sb.toString();
    }
    
    public void addZeroForT1()
    {
    	OpenTimeStep currentOpenTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
    	int lastMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, -1);
    	int thisMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, 0);
    	
    	List<FirstDealerInventoryData> oneDataList = FirstDealerInventoryData.dao.find("select * from first_dealer_inventory_data where month = ? and (actual_purchase_quantity is NULL or actual_sales_quantity is NULL)", lastMonth);
    	
    	Db.update("update first_dealer_inventory_data set actual_purchase_quantity = 0 where actual_purchase_quantity is NULL and month = ?", lastMonth);
    	Db.update("update first_dealer_inventory_data set actual_sales_quantity = 0 where actual_sales_quantity is NULL and month = ?", lastMonth);
            	
    	for (FirstDealerInventoryData oneData : oneDataList)
    	{
    		if (null != oneData)
    		{
    	        Long lastMonthBizId = oneData.getId();
    	        Long thisMonthBizId = null;
    	        
                FirstDealerInventoryData thisMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", oneData.getDealerId(), oneData.getProductId(), thisMonth);
                if (thisMonthData != null) {
                    thisMonthBizId = thisMonthData.getId();
                }
    	        
    	        //如果改的是上个月数据
    	        if (lastMonthBizId != null) {
    	            //计算下上月理论库存(6)
    	            CalculateService.me.theoryStockQuantityTask(lastMonthBizId, false);

    	            //计算本月的实际库存
    	            if (thisMonthBizId != null) {
    	                CalculateService.me.stockNum(thisMonthBizId);
    	                CalculateService.me.stockDayNowMonth(thisMonthBizId);
    	            }

    	            //上月库存天数
    	            CalculateService.me.stockDayLastMonth(lastMonthBizId);
    	        }
    		}
    	}
    }
    
    public void addZeroForT2()
    {
    	OpenTimeStep currentOpenTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
    	int precedingMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, -2);
    	int lastMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, -1);
    	int thisMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, 0);
    	
    	List<SecondaryDealerInventoryData> twoDataList = SecondaryDealerInventoryData.dao.find("select * from secondary_dealer_inventory_data where month = ? and (actual_purchase_quantity is NULL or actual_sales_quantity is NULL)", lastMonth);
    	
    	Db.update("update secondary_dealer_inventory_data set actual_purchase_quantity = 0 where actual_purchase_quantity is NULL and month = ?", lastMonth);
    	Db.update("update secondary_dealer_inventory_data set actual_sales_quantity = 0 where actual_sales_quantity is NULL and month = ?", lastMonth);  
    	
    	for (SecondaryDealerInventoryData twoData : twoDataList)
    	{
    		if (null != twoData)
    		{
    			twoData.setActualPurchaseQuantity(0L);
    			twoData.setActualSalesQuantity(0L);
    			
    			SecondaryDealerInventoryData precedingData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", precedingMonth, twoData.getDealerId(), twoData.getProductId());
    			
    			SecondaryDealerDto secondaryDealerDto = new SecondaryDealerDto();
    			secondaryDealerDto.setDealerId(twoData.getDealerId());
    			secondaryDealerDto.setProductId(twoData.getProductId());
    			if (null != precedingData)
    			{
        			secondaryDealerDto.setPrecedingMonthActualStockQuantity(precedingData.getActualStockQuantity());
        			secondaryDealerDto.setPrecedingMonthActualSalesQuantity(precedingData.getActualSalesQuantity());
        			secondaryDealerDto.setPrecedingMonthActualSalesQuantityHeadOffice(precedingData.getActualSalesQuantityHeadOffice());
    			}
    			
    	        Dealer dealer = Dealer.dao.findById(secondaryDealerDto.getDealerId());
    	        secondaryDealerDto.setDealer(dealer);
    	        secondaryDealerDto.setMonth(thisMonth);
    	        secondaryDealerDto.setLastMonth(lastMonth);
    	        
    	        secondaryDealerDto.setUserId(twoData.getBusinessManagerUserId());
    			
    			SecondaryDealerInventoryData nowInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", thisMonth, twoData.getDealerId(), twoData.getProductId());
    			
    	        //设置当月数据
    	        secondaryDealerDto.setInventoryData(nowInventoryData);

    	        //设置上月数据
    	        secondaryDealerDto.setLastMonthInventoryData(twoData);

    	        if (null == twoData.getActualPurchaseQuantity())
    	        {
    	  			secondaryDealerDto.setColumn("last_month_actual_purchase_quantity");
        			secondaryDealerDto.setValue("0");
        			DealerTwoService.me.updateDataByBossSubmit(secondaryDealerDto,true);
    	        }
  
    	        if (null == twoData.getActualSalesQuantity())
    	        {
        			secondaryDealerDto.setColumn("last_month_actual_sales_quantity");
        			secondaryDealerDto.setValue("0");
        			DealerTwoService.me.updateDataByBossSubmit(secondaryDealerDto,false);    	        	
    	        }

    		}
    	}
    }

}
