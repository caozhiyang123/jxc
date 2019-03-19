package com.site.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.site.base.BaseService;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.Job;
import com.site.core.model.SecondaryDealerInventoryData;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 封存T1商户数据
 */
public class ChangeToHistoryService extends BaseService {
    public static ChangeToHistoryService me = new ChangeToHistoryService();

    private static final int RETRY_TIME = 3;

    /**
     * 开始封存
     */
    public void runTask() {
        OpenTimeStep firstLevelTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        OpenTimeStep secondLevelTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(2);
        //每次计算好之后
        saveFirstLevelData();
        //下一个开放起的第一天开始执行或者job表 中没有执行成功的任务也会立即执行
        int executeTime = 1;
        boolean noError = true;
        while (executeTime < RETRY_TIME && noError) {
            saveSecondLevelData();
            executeTime = executeTime + 1;
        }
    }

    public void saveFirstLevelData() {
        //找出数据表中上个月数据
        OpenTimeStep currentOpenTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int lastMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, -1);
        System.out.println("========封存任务开始==========");

        Job job = insertJob(1);

        //真正开始执行
        Db.tx(() -> {
            //存入历史表中，封存上月的数据
            Db.update("delete from  dealer_inventory_data_history where dealer_level= 1 and month = ? ", lastMonth);
            
            // 计算近六个月月均销售
            updateOnePreSixMonthAverageSales(lastMonth);
            
            System.out.println("========取数据，存入历史表中==========");
            int update = Db.update("INSERT INTO dealer_inventory_data_history ( `area_id`, `area_name`, `region_id`, `region_name`, `area_manager_user_id`, `area_manager_user_name`, `area_manager_employee_number`, `business_manager_user_id`, `business_manager_user_name`, `business_manager_employee_number`, `dealer_id`, `dealer_name`, `dealer_level`, `dealer_code`, `month`, `product_id`, `product_name`, `upstream_name`, `purchase_quantity`, `plan_purchase_quantity`, `actual_purchase_quantity`, `plan_sales_quantity`, `actual_sales_quantity`, actual_sales_quantity_head_office, `theory_stock_quantity`, `actual_stock_quantity`, `inventory_day`, `diff_cause`, `create_time`, `create_user_id`, `create_user_name`, `create_user_employee_number`, `update_time`, `update_user_id`, `update_user_name`, `update_user_employee_number`, `comment`, `archive_time`, `order_num`, `diff_order_num` ) SELECT c.id AS area_id, c.`name` AS area_name, d.id AS regin_id, d.`name` AS regin_name, h.id AS area_manager_id, h.`name` AS area_manager_name, h.employee_number AS area_manager_code, g.id AS business_manager_id, g.`name` AS business_manager_name, g.employee_number AS business_manager_code, a.id AS dealer_id, a.`name` AS dealer_name, a.`level` AS delear_level, a.`code` AS dealer_code, ? AS MONTH, p.product_id AS product_id, p.product_name AS product_name, a.upstream_name AS upstream_name, b.purchase_quantity, CASE WHEN b.plan_purchase_quantity IS NULL THEN 0 ELSE b.plan_purchase_quantity END AS plan_purchase_quantity, CASE WHEN b.actual_purchase_quantity IS NULL THEN 0 ELSE b.actual_purchase_quantity END AS by_jhl, CASE WHEN b.plan_sales_quantity IS NULL THEN 0 ELSE b.plan_sales_quantity END AS by_yg_xsl, CASE WHEN b.actual_sales_quantity IS NULL THEN 0 ELSE b.actual_sales_quantity END AS actual_sales_quantity, CASE WHEN b.actual_sales_quantity_head_office IS NULL THEN 0 ELSE b.actual_sales_quantity_head_office END AS actual_sales_quantity_head_office, CASE WHEN b.theory_stock_quantity IS NULL THEN 0 ELSE b.theory_stock_quantity END AS theory_stock_quantity, CASE WHEN b.actual_stock_quantity IS NULL THEN 0 ELSE b.actual_stock_quantity END AS by_kc, CASE WHEN b.inventory_day IS NULL THEN 0 ELSE b.inventory_day END AS inventory_day, b.diff_cause AS diff_cause, NOW(), g.id AS create_user_id, i. NAME AS create_user_name, i.employee_number AS create_user_employee_num, NOW() AS update_time, j.id AS update_user_id, j.`name` AS update_user_name, j.employee_number AS update_user_employee_num, '' AS memo, NOW() AS archive_time, b.order_num, b.diff_order_num FROM ( SELECT pro.id AS product_id, pro. NAME AS product_name, dea.id AS dealer_id, dea.area_id AS area_id, dea.region_id AS region_id, dea.business_manager_user_id AS business_manager_user_id, dea.area_manager_user_id AS area_manager_user_id FROM dealer_product dp, product pro, dealer dea WHERE ( dp.product_id = pro.id AND dp.dealer_id = dea.id AND dea.`level` = 1 AND dea.is_delete = '0' AND pro.is_delete = '0' ) ORDER BY dea.id DESC ) p LEFT JOIN first_dealer_inventory_data b ON ( p.product_id = b.product_id AND p.dealer_id = b.dealer_id AND b.`month` = ? ) LEFT JOIN dealer a ON a.id = p.dealer_id LEFT JOIN area c ON a.area_id = c.id LEFT JOIN region d ON a.region_id = d.id LEFT JOIN `user` g ON g.id = p.business_manager_user_id LEFT JOIN `user` h ON h.id = p.area_manager_user_id LEFT JOIN `user` i ON i.id = b.create_user_id LEFT JOIN `user` j ON j.id = b.update_user_id", lastMonth, lastMonth);
            //更新当前表中本月的数据状态为已经封存
            if (update == 0) {
                updateJobStatus(job, 3);
                return false;
            }
            //任务表中更新flag
            System.out.println("========更新任务表中的Flag==========");
            if (!updateJobStatus(job, 2)) {
                updateJobStatus(job, 3);
                return false;
            }
            return true;
        });

        //删除数据表中的最前一个月的数据
        int month = DateUtils.getYearAndMonth(currentOpenTimeStep, 3);
        Db.update("delete from  first_dealer_inventory_data where month= ?", month);
    }
    
    private void updateOnePreSixMonthAverageSales(int nowtMonth)
    {
    	String preSixMonth = DateUtils.getYearAndMonth(String.valueOf(nowtMonth),"yyyyMM",-6);
    	
    	List<FirstDealerInventoryData> oneList = FirstDealerInventoryData.dao.find("select * from first_dealer_inventory_data where month = ?", nowtMonth);    	
    	if (null != oneList && !oneList.isEmpty())
    	{
    		for (FirstDealerInventoryData one : oneList)
    		{
    			if (null != one)
    			{
    				one.setPreSixMonthAverageSales(getPreSixMonthAverageSales(preSixMonth,one.getDealerId(),one.getProductId()));
    				one.update();
    			}
    		}
    	}
    }
    
    private Long getPreSixMonthAverageSales(String preSixMonth, Long dealerId, Long productId)
    {
    	List<Long> sales = Db.query("select actual_sales_quantity_head_office from dealer_inventory_data_history where `month` > ? and dealer_id =? and product_id=?", preSixMonth, dealerId, productId);
    	
    	if (null == sales || sales.isEmpty())
    	{
    		return null;
    	}
    	
    	if (sales.size() > 6)
    	{
    		sales = sales.subList(sales.size() - 6, sales.size());
    	}
    	
    	BigDecimal total = BigDecimal.ZERO;
    	List<Long> salesTemp = new ArrayList<Long>();
    	for (Long b : sales)
    	{
    		if (null != b)
    		{
    			total = total.add(new BigDecimal(b));
    			salesTemp.add(b);
    		}
    	}
    	
    	return total.divide(new BigDecimal(salesTemp.size()),0, BigDecimal.ROUND_DOWN).longValue();
    }

    public boolean saveSecondLevelData() {
        //找出数据表中上上个月数据
        OpenTimeStep currentOpenTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(2);
        int lastMonth = DateUtils.getYearAndMonth(currentOpenTimeStep, -1);
        System.out.println("========封存2任务开始==========");

        Job job = insertJob(2);

        //真正开始执行
        Db.tx(() -> {
            //存入历史表中，封存上月的数据
            Db.update("delete from  dealer_inventory_data_history where dealer_level= 2 and month = ? ", lastMonth);
            
            // 计算近六个月月均销售
            updateTwoPreSixMonthAverageSales(lastMonth);
            
            System.out.println("========取数据，存入历史表中==========");
            int update = Db.update("INSERT INTO dealer_inventory_data_history ( `area_id`, `area_name`, `region_id`, `region_name`, `area_manager_user_id`, `area_manager_user_name`, `area_manager_employee_number`, `business_manager_user_id`, `business_manager_user_name`, `business_manager_employee_number`, `dealer_id`, `dealer_name`, `dealer_level`, `dealer_code`, `month`, `product_id`, `product_name`, `upstream_name`, `purchase_quantity`, `plan_purchase_quantity`, `actual_purchase_quantity`, `plan_sales_quantity`, `actual_sales_quantity`, actual_sales_quantity_head_office, `theory_stock_quantity`, `actual_stock_quantity`, `inventory_day`, `diff_cause`, `create_time`, `create_user_id`, `create_user_name`, `create_user_employee_number`, `update_time`, `update_user_id`, `update_user_name`, `update_user_employee_number`, `comment`, `archive_time` ) SELECT c.id AS area_id, c.`name` AS area_name, d.id AS regin_id, d.`name` AS regin_name, h.id AS area_manager_id, h.`name` AS area_manager_name, h.employee_number AS area_manager_code, g.id AS business_manager_id, g.`name` AS business_manager_name, g.employee_number AS business_manager_code, a.id AS dealer_id, a.`name` AS dealer_name, a.`level` AS delear_level, a.`code` AS dealer_code, ? AS MONTH, p.product_id AS product_id, p.product_name AS product_name, a.upstream_name AS upstream_name, NULL AS purchase_quantity, CASE WHEN b.plan_purchase_quantity IS NULL THEN 0 ELSE b.plan_purchase_quantity END AS plan_purchase_quantity, CASE WHEN b.actual_purchase_quantity IS NULL THEN 0 ELSE b.actual_purchase_quantity END AS by_jhl, CASE WHEN b.plan_sales_quantity IS NULL THEN 0 ELSE b.plan_sales_quantity END AS by_yg_xsl, CASE WHEN b.actual_sales_quantity IS NULL THEN 0 ELSE b.actual_sales_quantity END AS actual_sales_quantity, CASE WHEN b.actual_sales_quantity_head_office IS NULL THEN 0 ELSE b.actual_sales_quantity_head_office END AS actual_sales_quantity_head_office, CASE WHEN b.theory_stock_quantity IS NULL THEN 0 ELSE b.theory_stock_quantity END AS theory_stock_quantity, CASE WHEN b.actual_stock_quantity IS NULL THEN 0 ELSE b.actual_stock_quantity END AS by_kc, CASE WHEN b.inventory_day IS NULL THEN 0 ELSE b.inventory_day END AS inventory_day, b.diff_cause AS diff_cause, b.create_time, b.create_user_id, i. NAME AS create_user_name, i.employee_number AS create_user_employee_num, b.update_time AS update_time, b.update_user_id AS update_user_id, j.`name` AS update_user_name, j.employee_number AS update_user_employee_num, '' AS memo, NOW() AS archive_time FROM ( SELECT pro.id AS product_id, pro. NAME AS product_name, dea.id AS dealer_id, dea.area_id AS area_id, dea.region_id AS region_id, dea.business_manager_user_id AS business_manager_user_id, dea.area_manager_user_id AS area_manager_user_id FROM dealer_product dp, product pro, dealer dea WHERE ( dp.product_id = pro.id AND dp.dealer_id = dea.id AND dea.`level` = 2 AND dea.is_delete = '0' AND pro.is_delete = '0' ) ORDER BY dea.id DESC ) p LEFT JOIN secondary_dealer_inventory_data b ON ( p.product_id = b.product_id AND p.dealer_id = b.dealer_id AND b.`month` = ? ) LEFT JOIN dealer a ON a.id = p.dealer_id LEFT JOIN area c ON a.area_id = c.id LEFT JOIN region d ON a.region_id = d.id LEFT JOIN `user` g ON g.id = p.business_manager_user_id LEFT JOIN `user` h ON h.id = p.area_manager_user_id LEFT JOIN `user` i ON i.id = b.create_user_id LEFT JOIN `user` j ON j.id = b.update_user_id", lastMonth, lastMonth);
            //更新当前表中本月的数据状态为已经封存
            if (update == 0) {
                return false;
            }
            //任务表中更新flag
            System.out.println("========更新任务表中的Flag==========");
            if (!updateJobStatus(job, 2)) {
                return false;
            }
            return true;
        });

        //删除数据表中的最前一个月的数据
        int month = DateUtils.getYearAndMonth(currentOpenTimeStep, -3);
        Db.update("delete from secondary_dealer_inventory_data where month=?", month);
        return true;
    }
    
    private void updateTwoPreSixMonthAverageSales(int nowtMonth)
    {
    	String preSixMonth = DateUtils.getYearAndMonth(String.valueOf(nowtMonth),"yyyyMM",-6);
    	
    	List<SecondaryDealerInventoryData> twoList = SecondaryDealerInventoryData.dao.find("select * from secondary_dealer_inventory_data where month = ?", nowtMonth);    	
    	if (null != twoList && !twoList.isEmpty())
    	{
    		for (SecondaryDealerInventoryData two : twoList)
    		{
    			if (null != two)
    			{
    				two.setPreSixMonthAverageSales(getPreSixMonthAverageSales(preSixMonth,two.getDealerId(),two.getProductId()));
    				two.update();
    			}
    		}
    	}
    }

    /**
     * 任务表中插入一条任务
     *
     * @return
     */
    private Job insertJob(int level) {
        Job job = new Job();
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(level);
        job.setMonth(openTimeStep.getYearAndMonth());
        job.setTaskType(level);
        job.setStatus(0);
        job.setCreateTime(new Date());
        job.setEndTime(new Date());
        job.save();
        return job;
    }

    /**
     * 每个月的一号执行
     * 封存上个月的数据
     *
     * @param level
     * @return
     */
    private boolean checkJobState(int level) {
        String nowDate = DateUtils.getDate(new Date(), "dd", 0);

        //T2经销商
        if (2 == level) {
            if (!"01".equalsIgnoreCase(nowDate)) {
                System.out.println("不在每个月的第一天，不予执行");
                return false;
            }
        }

        int lastMonth = DateUtils.getYearAndMonth(-2);

        synchronized (this) {
            Job job = Job.dao.findFirst("select * from job where month = ? and task_type = ?", lastMonth, level);
            if (job == null || (3 == job.getStatus())) {
                if (job == null) {
                    job = insertJob(2);
                }
                updateJobStatus(job, 1);
                return true;
            }
        }
        return false;
    }

    private boolean updateJobStatus(Job job, int status) {
        job.setStatus(status);
        return job.update();
    }

}
