package com.site.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.site.base.BaseService;
import com.site.core.model.DealerInventoryDataVersionList;
import com.site.core.model.Job;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VersionDataService extends BaseService {

    //重试次数
    private static final int RETRY_TIME = 3;

    public static VersionDataService me = new VersionDataService();

    public Map<String, JSONObject> saveVersionData(String version) {
        Map<String, JSONObject> resut = new HashMap<>();
        JSONObject first = new JSONObject();
        JSONObject secondary = new JSONObject();

        System.out.println("======== T1经销商保存版本数据任务开始 ==========");
        //添加版本记录
        DealerInventoryDataVersionList versionList = null;
        try {
            versionList = insertVersionList(version, "1");
        } catch (ActiveRecordException e) {
            System.out.println("======== 已经存在该版本数据，任务终止 ==========");
            first.put("success", true);
            secondary.put("success", true);
            resut.put("firstLevel", secondary);
            resut.put("secondaryLevel", secondary);
            return resut;
        }


        //计算T1经销商周期
        OpenTimeStep firstLevelTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int nowMonth = firstLevelTimeStep.getYearAndMonth();
        int lastMonth = Integer.parseInt(DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -1));
        int precedingMonth = Integer.parseInt(DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -2));
        Job job = insertJob(1, nowMonth);

        boolean firstSuccess = false;
        for (int i = 0; i < RETRY_TIME; i++) {
            firstSuccess = saveFirstLevelData(nowMonth, lastMonth, precedingMonth, version, job);
            if (firstSuccess) {
                break;
            }
        }

        first.put("success", firstSuccess);
        first.put("month", nowMonth);
        first.put("version", version);
        first.put("level", "一");
        resut.put("firstLevel", first);
        System.out.println("======== T1经销商保存版本数据任务结束,任务执行" + (firstSuccess ? "成功" : "失败") + " ==========");

        System.out.println("======== 二级经销商保存版本数据任务开始 ==========");
        //计算二级经销商周期
        OpenTimeStep secondaryLevelTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(2);
        int secNowMonth = secondaryLevelTimeStep.getYearAndMonth();
        int secLastMonth = Integer.parseInt(DateUtils.getYearAndMonth(secNowMonth + "", "yyyyMM", -1));
        int secPrecedingMonth = Integer.parseInt(DateUtils.getYearAndMonth(secNowMonth + "", "yyyyMM", -2));
        Job jobTwo = insertJob(2, secNowMonth);
        boolean secondarySuccess = false;
        for (int i = 0; i < RETRY_TIME; i++) {
            secondarySuccess = saveSecondaryLevelData(secNowMonth, secLastMonth, secPrecedingMonth, version, jobTwo);
            if (secondarySuccess) {
                break;
            }
        }

        secondary.put("success", secondarySuccess);
        secondary.put("month", secNowMonth);
        secondary.put("version", version);
        secondary.put("level", "二");
        resut.put("secondaryLevel", secondary);
        System.out.println("======== 二级经销商保存版本数据任务结束,任务执行" + (secondarySuccess ? "成功" : "失败") + " ==========");

        if (firstSuccess && secondarySuccess) {
            versionList.setStatus("0");
            versionList.update();
        }
        return resut;
    }

    public boolean saveFirstLevelData(int nowMonth, int lastMonth, int precedingMonth, String version, Job job) {
        JSONObject success = new JSONObject();
        success.put("success", false);

        String sql = getFirstSql();

        try {
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    System.out.println("======== 开始保存T1经销商版本数据 ==========");

                    int update = Db.update(sql, nowMonth, version, nowMonth, lastMonth, precedingMonth);

                    //更新当前表中本月的数据状态为已经封存
                    if (update == 0) {
                        return false;
                    }
                    //任务表中更新flag
                    System.out.println("======== 更新任务表中任务状态 ==========");
                    if (!updateJobStatus(job)) {
                        return false;
                    }
                    //新增版本记录

                    success.put("success", true);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success.getBoolean("success");
    }

    public boolean saveSecondaryLevelData(int nowMonth, int lastMonth, int precedingMonth, String version, Job job) {
        JSONObject success = new JSONObject();
        success.put("success", false);

        String sql = getSecondarySql();
        try {
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    System.out.println("======== 开始保存二级经销商版本数据 ==========");
                    //TODO 传入 数据的month 月和当天时间
                    int update = Db.update(sql, nowMonth, version, nowMonth, lastMonth, precedingMonth);

                    //更新当前表中本月的数据状态为已经封存
                    if (update == 0) {
                        return false;
                    }
                    //任务表中更新flag
                    System.out.println("======== 更新任务表中任务状态 ==========");
                    if (!updateJobStatus(job)) {
                        return false;
                    }
                    success.put("success", true);
                    return true;
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success.getBoolean("success");
    }

    /**
     * 版本列表中插入一条数据
     *
     * @return
     */
    private DealerInventoryDataVersionList insertVersionList(String version, String status) {
        DealerInventoryDataVersionList versionList = new DealerInventoryDataVersionList();
        versionList.setVersion(version);
        versionList.setStatus(status);
        versionList.setCreateTime(new Date());
        versionList.save();
        return versionList;
    }

    /**
     * 任务表中插入一条任务
     *
     * @return
     */
    private Job insertJob(int level, int month) {
        Job job = new Job();
        job.setMonth(month);
        if (1 == level) {
            job.setTaskType(5);
        }
        if (2 == level) {
            job.setTaskType(6);
        }
        job.setStatus(1);
        job.setCreateTime(new Date());
        job.setEndTime(new Date());
        job.save();
        return job;
    }

    private boolean updateJobStatus(Job job) {
        job.setStatus(2);
        return job.update();
    }

    /**
     * 获取T1经销商插入sql
     *
     * @return
     */
    private String getFirstSql() {
        String sql = "INSERT INTO dealer_inventory_data_version ( `area_id`, `area_name`, `region_id`, `region_name`, `area_manager_user_id`, `area_manager_user_name`, `area_manager_employee_number`, " +
                "`business_manager_user_id`, `business_manager_user_name`, `business_manager_employee_number`, `dealer_id`, `dealer_name`, `dealer_level`, `dealer_code`, `month`, `product_id`, " +
                "`product_name`, `upstream_name`, `preceding_month_actual_stock_quantity`, `last_month_plan_purchase_quantity`, `last_month_actual_purchase_quantity`, `last_month_plan_sales_quantity`, `last_month_actual_sales_quantity`, `last_month_actual_sales_quantity_head_office`," +
                "`last_month_theory_stock_quantity`, `last_month_actual_stock_quantity`, `last_month_purchase_quantity`, `last_month_inventory_day`, `plan_purchase_quantity`, `actual_purchase_quantity`, " +
                "`plan_sales_quantity`, `actual_sales_quantity`, `theory_stock_quantity`, `actual_stock_quantity`, `purchase_quantity`, `inventory_day`, `diff_cause`, `create_time`, `create_user_id`, `create_user_name`, " +
                "`create_user_employee_number`, `update_time`, `update_user_id`, `update_user_name`, `update_user_employee_number`, `comment`, `archive_time`, `version`,`order_num`,`diff_order_num`,`pre_six_month_average_sales` ) SELECT c.id AS area_id, c.`name` AS area_name, d.id AS regin_id, " +
                "d.`name` AS regin_name, h.id AS area_manager_user_id, h.`name` AS area_manager_user_name, h.employee_number AS area_manager_code, g.id AS business_manager_id, g.`name` AS business_manager_name, g.employee_number AS business_manager_code, " +
                "a.id AS dealer_id, a.`name` AS dealer_name, a.`level` AS delear_level, a.`code` AS dealer_code, ? as MONTH, p.product_id AS product_id, p.product_name AS product_name, '' AS upstream_name, preceding_month.actual_stock_quantity preceding_month_actual_stock_quantity, last_month.plan_purchase_quantity last_month_plan_purchase_quantity, " +
                "last_month.actual_purchase_quantity last_month_actual_purchase_quantity, last_month.plan_sales_quantity last_month_plan_sales_quantity, last_month.actual_sales_quantity last_month_actual_sales_quantity, last_month.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office, last_month.theory_stock_quantity last_month_theory_stock_quantity, " +
                "last_month.actual_stock_quantity last_month_actual_stock_quantity, last_month.purchase_quantity last_month_purchase_quantity, last_month.inventory_day last_month_inventory_day, b.plan_purchase_quantity, b.actual_purchase_quantity, b.plan_sales_quantity, " +
                "b.actual_sales_quantity, b.theory_stock_quantity, b.actual_stock_quantity, b.purchase_quantity, b.inventory_day, last_month.diff_cause AS diff_cause, b.create_time, p.business_manager_user_id create_user_id, i. NAME AS create_user_name, i.employee_number AS create_user_employee_num, " +
                "b.update_time AS update_time, b.update_user_id AS update_user_id, j.`name` AS update_user_name, j.employee_number AS update_user_employee_num, null AS comment, NOW() AS archive_time, ? as `version`,b.order_num,b.diff_order_num,last_month.pre_six_month_average_sales AS pre_six_month_average_sales FROM ( SELECT pro.id AS product_id, pro. NAME AS product_name, " +
                "dea.id AS dealer_id, dea.area_id AS area_id, dea.region_id AS region_id, dea.business_manager_user_id AS business_manager_user_id, dea.area_manager_user_id AS area_manager_user_id " +
                "FROM dealer_product dp, product pro, dealer dea WHERE ( dp.product_id = pro.id AND dp.dealer_id = dea.id AND dea.`level` = 1 AND dea.is_delete = '0' AND pro.is_delete = '0' ) ORDER BY dea.id DESC ) p " +
                "LEFT JOIN first_dealer_inventory_data b ON ( p.product_id = b.product_id AND p.dealer_id = b.dealer_id AND b.`month` = ? ) " +
                "LEFT JOIN first_dealer_inventory_data last_month ON ( p.product_id = last_month.product_id AND p.dealer_id = last_month.dealer_id AND last_month.`month` = ? ) " +
                "LEFT JOIN first_dealer_inventory_data preceding_month ON ( p.product_id = preceding_month.product_id AND p.dealer_id = preceding_month.dealer_id AND preceding_month.`month` = ? ) " +
                "LEFT JOIN dealer a ON a.id = p.dealer_id LEFT JOIN area c ON a.area_id = c.id LEFT JOIN region d ON a.region_id = d.id " +
                "LEFT JOIN `user` g ON g.id = p.business_manager_user_id LEFT JOIN `user` h ON h.id = p.area_manager_user_id " +
                "LEFT JOIN `user` i ON i.id = b.create_user_id LEFT JOIN `user` j ON j.id = b.update_user_id";
        return sql;
    }

    /**
     * 获取二级经销商插入sql
     *
     * @return
     */
    private String getSecondarySql() {
        String sql = "INSERT INTO dealer_inventory_data_version ( `area_id`, `area_name`, `region_id`, `region_name`, `area_manager_user_id`, `area_manager_user_name`, `area_manager_employee_number`, " +
                "`business_manager_user_id`, `business_manager_user_name`, `business_manager_employee_number`, `dealer_id`, `dealer_name`, `dealer_level`, `dealer_code`, `month`, `product_id`, `product_name`, " +
                "`upstream_name`, `preceding_month_actual_stock_quantity`, `last_month_plan_purchase_quantity`, `last_month_actual_purchase_quantity`, `last_month_plan_sales_quantity`, `last_month_actual_sales_quantity`,`last_month_actual_sales_quantity_head_office`, `last_month_theory_stock_quantity`, " +
                "`last_month_actual_stock_quantity`, `last_month_purchase_quantity`, `last_month_inventory_day`, `plan_purchase_quantity`, `actual_purchase_quantity`, `plan_sales_quantity`, `actual_sales_quantity`, " +
                "`theory_stock_quantity`, `actual_stock_quantity`, `purchase_quantity`, `inventory_day`, `diff_cause`, `create_time`, `create_user_id`, `create_user_name`, `create_user_employee_number`, " +
                "`update_time`, `update_user_id`, `update_user_name`, `update_user_employee_number`, `comment`, `archive_time`, `version`,`pre_six_month_average_sales` ) SELECT c.id AS area_id, c.`name` AS area_name, d.id AS regin_id, d.`name` AS regin_name, " +
                "h.id AS area_manager_user_id, h.`name` AS area_manager_user_name, h.employee_number AS area_manager_code, g.id AS business_manager_id, g.`name` AS business_manager_name, g.employee_number AS business_manager_code, " +
                "a.id AS dealer_id, a.`name` AS dealer_name, a.`level` AS delear_level, a.`code` AS dealer_code, ? as MONTH, p.product_id AS product_id, p.product_name AS product_name, a.upstream_name AS upstream_name, " +
                "preceding_month.actual_stock_quantity preceding_month_actual_stock_quantity, last_month.plan_purchase_quantity last_month_plan_purchase_quantity, last_month.actual_purchase_quantity last_month_actual_purchase_quantity, last_month.plan_sales_quantity last_month_plan_sales_quantity, " +
                "last_month.actual_sales_quantity last_month_actual_sales_quantity, last_month.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office, last_month.theory_stock_quantity last_month_theory_stock_quantity, last_month.actual_stock_quantity last_month_actual_stock_quantity, " +
                "null as last_month_purchase_quantity, last_month.inventory_day last_month_inventory_day, b.plan_purchase_quantity, b.actual_purchase_quantity, b.plan_sales_quantity, b.actual_sales_quantity, b.theory_stock_quantity, " +
                "b.actual_stock_quantity, null as purchase_quantity, b.inventory_day, last_month.diff_cause AS diff_cause, b.create_time, p.business_manager_user_id create_user_id, i. NAME AS create_user_name, i.employee_number AS create_user_employee_num, " +
                "b.update_time AS update_time, b.update_user_id AS update_user_id, j.`name` AS update_user_name, j.employee_number AS update_user_employee_num, null AS comment, NOW() AS archive_time, ? as `version`, last_month.pre_six_month_average_sales AS pre_six_month_average_sales " +
                "FROM ( SELECT pro.id AS product_id, pro. NAME AS product_name, dea.id AS dealer_id, dea.area_id AS area_id, dea.region_id AS region_id, dea.business_manager_user_id AS business_manager_user_id, dea.area_manager_user_id AS area_manager_user_id " +
                "FROM dealer_product dp, product pro, dealer dea WHERE ( dp.product_id = pro.id AND dp.dealer_id = dea.id AND dea.`level` = 2 AND dea.is_delete = '0' AND pro.is_delete = '0' ) ORDER BY dea.id DESC ) p " +
                "LEFT JOIN secondary_dealer_inventory_data b ON ( p.product_id = b.product_id AND p.dealer_id = b.dealer_id AND b.`month` = ? ) " +
                "LEFT JOIN secondary_dealer_inventory_data last_month ON ( p.product_id = last_month.product_id AND p.dealer_id = last_month.dealer_id AND last_month.`month` = ? ) " +
                "LEFT JOIN secondary_dealer_inventory_data preceding_month ON ( p.product_id = preceding_month.product_id AND p.dealer_id = preceding_month.dealer_id AND preceding_month.`month` = ? ) " +
                "LEFT JOIN dealer a ON a.id = p.dealer_id LEFT JOIN area c ON a.area_id = c.id LEFT JOIN region d ON a.region_id = d.id " +
                "LEFT JOIN `user` g ON g.id = p.business_manager_user_id LEFT JOIN `user` h ON h.id = p.area_manager_user_id " +
                "LEFT JOIN `user` i ON i.id = b.create_user_id LEFT JOIN `user` j ON j.id = b.update_user_id";
        return sql;
    }

    public static void main(String[] args) {
        VersionDataService vs = new VersionDataService();
        System.out.println(vs.getSecondarySql());
    }
}