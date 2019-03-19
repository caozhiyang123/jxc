package com.site.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.*;
import com.site.core.model.common.OpenTimeStep;
import com.site.core.model.dto.SecondaryDealerDto;
import com.site.service.DealerTwoService;
import com.site.service.RoleConfigService;
import com.site.service.UserService;
import com.site.utils.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 二级经销商 管理
 * 描述：
 */
public class DealerTwoController extends BaseController {

    private static final Log log = Log.getLog(DealerTwoController.class);

    static DealerTwoService srv = DealerTwoService.me;

    private RoleConfigService roleConfigService;

    private UserService userService;

    public void index() {
        User user = getLoginUser();
        String roleName = user.getRoleName();

        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        //大区经理在开放期最后一天之前可编辑角色空缺的经销商数据
        boolean isOpenTime = currentOpenTimeStep.getStep() == 1;
        boolean isEditDay = currentOpenTimeStep.getDesc() > 1;
        boolean isEditVacancyList = false;
        if (isOpenTime && isEditDay && "大区经理".equals(roleName)) {
            List<Long> bmUserIds = userService.getVacancyBMUserId(user.getId(), true);
            isEditVacancyList = bmUserIds != null && bmUserIds.size() > 0;
            if (isEditVacancyList) {
                //根据权限和当前处在的开放期得到可操纵的数据列
                String vacancyColumn = roleConfigService.getCanEditColumn("商务经理", 2);
                setAttr("canVacancy", vacancyColumn);
            }
        }

        //大区经理在第一个开放期内不能编辑待审核数据
        if (!isEditVacancyList) {
            String column = roleConfigService.getCanEditColumn(roleName, 2);
            setAttr("canEdit", column);
        }

        int month = currentOpenTimeStep.getYearAndMonth();
        setAttr("isEditVacancyList", isEditVacancyList);
        setAttr("month", DateUtils.getMonth(month + "", "yyyyMM", 0));
        setAttr("lastMonth", DateUtils.getMonth(month + "", "yyyyMM", -1));
        setAttr("precedingMonth", DateUtils.getMonth(month + "", "yyyyMM", -2));
        setAttr("roleName", roleName);
        render("list.html");
    }

    /**
     * 大区经理在开放期前一天可填写空缺的商务经理数据
     * dealerTwo/vacancyList
     */
    public void vacancyList() {
        //获取当前用户权限
        User user = getLoginUser();
        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        Integer step = currentOpenTimeStep.getStep();
        Integer descDay = currentOpenTimeStep.getDesc();
        Integer month = currentOpenTimeStep.getYearAndMonth();

        //大区经理在开放期最后一天之前可填写空缺的商务经理数据
        if ("大区经理".equals(user.getRoleName()) && step == 1 && descDay > 1) {
            //空缺的商务经理
            List<Long> bmUserIds = userService.getVacancyBMUserId(user.getId(), true);

            //获取空缺的商务经理数据
            QueryUtil.SearchResult nowMonthResult = getNowMonthBmVacancyData(bmUserIds, month);

            renderJson(nowMonthResult);
            return;
        }

        throw new ExceptionForJson("权限操作异常，当前用户没有权限操作此数据");
    }

    /**
     * 大区经理在开放期前一天可导出空缺的商务经理数据
     * dealerTwo/exportVacancyList
     */
    public void exportVacancyList() {
        //获取当前用户权限
        User user = getLoginUser();
        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        Integer step = currentOpenTimeStep.getStep();
        Integer descDay = currentOpenTimeStep.getDesc();
        Integer month = currentOpenTimeStep.getYearAndMonth();

        //大区经理在开放期最后一天之前可填写空缺的商务经理数据
        if ("大区经理".equals(user.getRoleName()) && step == 1 && descDay > 1) {
            //空缺的商务经理
            List<Long> bmUserIds = userService.getVacancyBMUserId(user.getId(), true);

            //导出空缺的商务经理数据
            exportNowMonthBmVacancyData(bmUserIds, month);
            return;
        }

        throw new ExceptionForJson("权限操作异常，当前用户没有权限操作此数据");
    }

    /**
     * 当前用户角色可操作的数据列表
     * 待填写数据列表
     * dealerTwo/editlist
     */
    public void editList() {
        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        //获取当前用户权限
        User user = getLoginUser();

//        Integer step = currentOpenTimeStep.getStep();
//        Integer descDay = currentOpenTimeStep.getDesc();
        Integer month = currentOpenTimeStep.getYearAndMonth();

//        //大区经理在开放期最后一天之前不可查看商务经理数据
//        if ("大区经理".equals(user.getRoleName()) && step == 1 && descDay > 1) {
//            QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
//            renderJson(queryUtil.new SearchResult());
//            return;
//        }

        //获取当月数据
        QueryUtil.SearchResult nowMonthResult = getNowMonthData(user, month);
        renderJson(nowMonthResult);
    }

    /**
     * 当前用户角色可导出的数据列表
     * dealerTwo/exportEditList
     */
    public void exportEditList() {
        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        //获取当前用户权限
        User user = getLoginUser();

//        Integer step = currentOpenTimeStep.getStep();
//        Integer descDay = currentOpenTimeStep.getDesc();
        Integer month = currentOpenTimeStep.getYearAndMonth();

//        //大区经理在开放期最后一天之前不可导出商务经理数据
//        if ("大区经理".equals(user.getRoleName()) && step == 1 && descDay > 1) {
//            QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
//            renderJson(queryUtil.new SearchResult());
//            return;
//        }

        //导出当月数据
        exportNowMonthData(user, month);
    }

    /**
     * 保存
     * dealerTwo/save
     */
    @Before(Tx.class)
    public void save() {
        //获取开放期
        OpenTimeStep currentOpenTimeStep = roleConfigService.getCurrentOpenTimeStep(2);
        User user = getLoginUser();
        String roleName = user.getRoleName();
        Integer step = currentOpenTimeStep.getStep();
        Integer month = currentOpenTimeStep.getYearAndMonth();

        if ("商务经理".equals(roleName)) {
            saveBmData(user, month);
            return;
        }

        if ("大区经理".equals(roleName)) {
            saveBmData(user, month);
            return;
        }

        if ("总部".equals(roleName)) {
            saveBossData(user, month, step);
            return;
        }

        throw new ExceptionForJson("权限操作异常，当前用户没有权限操作此数据");
    }

    /**
     * 获取当月进销存数据
     *
     * @param user 当前登录用户
     * @return
     */
    private QueryUtil.SearchResult getNowMonthData(User user, Integer month) {
        String roleName = user.getRoleName();
        String productId = getPara("product_id");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String businessUserId = getPara("business_user_id");
        String dealerId = getPara("dealer_id");

        QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
        if (StringUtils.isBlank(productId)) {
            return queryUtil.new SearchResult();
        }

        //上月
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        //上上月
        String precedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -2);

        queryUtil.setSqlSelect("select secondary.id," + month + " month,p.name product_name,dp.product_id product_id,last_secondary.id last_id,d.upstream_name,area.id area_id,area.name area_name,region.id region_id,region.name region_name,d.id dealer_id,d.name dealer_name," +
                "d.area_manager_user_id,d.business_manager_user_id,business_manager.name business_manager_name," +
                "secondary.plan_purchase_quantity,secondary.plan_sales_quantity,secondary.ref_stock_quantity actual_stock_quantity,secondary.ref_inventory_day inventory_day,preceding_secondary.pre_six_month_average_sales pre_six_month_average_sales,last_secondary.diff_cause," +
                "last_secondary.plan_purchase_quantity last_month_plan_purchase_quantity,last_secondary.actual_purchase_quantity last_month_actual_purchase_quantity," +
                "last_secondary.plan_sales_quantity last_month_plan_sales_quantity,last_secondary.actual_sales_quantity last_month_actual_sales_quantity,last_secondary.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office,last_secondary.theory_stock_quantity last_month_theory_stock_quantity," +
                "last_secondary.actual_stock_quantity last_month_actual_stock_quantity,last_secondary.inventory_day last_month_inventory_day,last_secondary.original_theory_stock_quantity last_month_original_theory_stock_quantity," +
                "preceding_secondary.actual_sales_quantity preceding_month_actual_sales_quantity,preceding_secondary.actual_sales_quantity_head_office preceding_month_actual_sales_quantity_head_office,preceding_secondary.actual_stock_quantity preceding_month_actual_stock_quantity");
        queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                " left join secondary_dealer_inventory_data secondary on d.id=secondary.dealer_id and secondary.month=" + month + " and secondary.product_id=dp.product_id" +
                " left join user business_manager on business_manager.id=d.business_manager_user_id" +
                " left join area area on area.id=d.area_id" +
                " left join region region on region.id=d.region_id" +
                " left join secondary_dealer_inventory_data last_secondary on d.id=last_secondary.dealer_id and last_secondary.product_id=dp.product_id" + " and last_secondary.month=" + lastMonth +
                " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id" + " and preceding_secondary.month=" + precedingMonth);
        queryUtil.addQueryParam("p.is_delete", "=", "0");
        queryUtil.addQueryParam("d.is_delete", "=", "0");
        queryUtil.addQueryParam("d.level", "=", "2");
        queryUtil.setSort("d.id");
        queryUtil.setOrder("desc");

        Long userId = user.getId();
        if ("商务经理".equals(roleName)) {
            queryUtil.addQueryParam("d.business_manager_user_id", "=", userId);
        } else if ("大区经理".equals(roleName)) {
            queryUtil.addQueryParam("d.area_manager_user_id", "=", userId);
        }

        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("d.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("d.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(businessUserId)) {
            queryUtil.addQueryParam("d.business_manager_user_id", "in", businessUserId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("d.id", "in", dealerId);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("dp.product_id", "in", productId);
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<SecondaryDealerInventoryData> list = searchResult.getData();

        //计算总计数据
        if (list != null && list.size() > 0) {
            queryUtil.setSqlSelect("select '总计' dealer_name," + "sum(secondary.plan_purchase_quantity) plan_purchase_quantity,sum(secondary.plan_sales_quantity) plan_sales_quantity,sum(secondary.ref_stock_quantity) actual_stock_quantity,sum(secondary.ref_inventory_day) inventory_day," +
                    "sum(last_secondary.plan_purchase_quantity) last_month_plan_purchase_quantity,sum(last_secondary.actual_purchase_quantity) last_month_actual_purchase_quantity," +
                    "sum(last_secondary.plan_sales_quantity) last_month_plan_sales_quantity,sum(last_secondary.actual_sales_quantity) last_month_actual_sales_quantity,sum(last_secondary.actual_sales_quantity_head_office) last_month_actual_sales_quantity_head_office,sum(last_secondary.theory_stock_quantity) last_month_theory_stock_quantity," +
                    "sum(last_secondary.actual_stock_quantity) last_month_actual_stock_quantity,sum(last_secondary.inventory_day) last_month_inventory_day," +
                    "sum(preceding_secondary.actual_sales_quantity) preceding_month_actual_sales_quantity,sum(preceding_secondary.actual_sales_quantity_head_office) preceding_month_actual_sales_quantity_head_office,sum(preceding_secondary.actual_stock_quantity) preceding_month_actual_stock_quantity");
            queryUtil.setParamValue(new LinkedList<>());
            String countSql = queryUtil.getExecuteSql();
            SecondaryDealerInventoryData countData = SecondaryDealerInventoryData.dao.findFirst(countSql, queryUtil.getParamValue());

            //上上上月
            String lastPrecedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -3);
            queryUtil.setSqlSelect("select sum(preceding_secondary.actual_sales_quantity) last_preceding_month_actual_sales_quantity,sum(preceding_secondary.actual_sales_quantity_head_office) last_preceding_month_actual_sales_quantity_head_office");
            queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                    " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id and preceding_secondary.month=" + lastPrecedingMonth);
            queryUtil.setExecuteSql(null);
            queryUtil.setParamValue(new LinkedList<>());
            String lastPrecedingSql = queryUtil.getExecuteSql();
            SecondaryDealerInventoryData lastPrecedingData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, queryUtil.getParamValue());
            countData.put("last_preceding_month_actual_sales_quantity", lastPrecedingData.getLong("last_preceding_month_actual_sales_quantity"));
            countData.put("last_preceding_month_actual_sales_quantity_head_office", lastPrecedingData.getLong("last_preceding_month_actual_sales_quantity_head_office"));

            countData.put("inventory_day", caculateInventoryDay(countData));
            countData.put("last_month_inventory_day", caculateLastInventoryDay(countData));

            list.add(new SecondaryDealerInventoryData());
            list.add(countData);
        }
        return searchResult;
    }

    /**
     * 大区经理在开放期前一天可填写空缺的商务经理数据
     *
     * @return
     */
    private QueryUtil.SearchResult getNowMonthBmVacancyData(List<Long> bmUserIds, Integer month) {
        QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
        if (bmUserIds == null || bmUserIds.size() == 0) {
            return queryUtil.new SearchResult();
        }

        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String dealerId = getPara("dealer_id");
        String productId = getPara("product_id");

        if (StringUtils.isBlank(productId)) {
            return queryUtil.new SearchResult();
        }

        //上月
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        //上上月
        String precedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -2);

        queryUtil.setSqlSelect("select secondary.id," + month + " month,p.name product_name,dp.product_id product_id,last_secondary.id last_id,d.upstream_name,area.id area_id,area.name area_name,region.id region_id,region.name region_name,d.id dealer_id,d.name dealer_name," +
                "d.area_manager_user_id,d.business_manager_user_id,business_manager.name business_manager_name," +
                "secondary.plan_purchase_quantity,secondary.plan_sales_quantity,secondary.ref_stock_quantity actual_stock_quantity,secondary.ref_inventory_day inventory_day,preceding_secondary.pre_six_month_average_sales pre_six_month_average_sales,last_secondary.diff_cause," +
                "last_secondary.plan_purchase_quantity last_month_plan_purchase_quantity,last_secondary.actual_purchase_quantity last_month_actual_purchase_quantity," +
                "last_secondary.plan_sales_quantity last_month_plan_sales_quantity,last_secondary.actual_sales_quantity last_month_actual_sales_quantity,last_secondary.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office,last_secondary.theory_stock_quantity last_month_theory_stock_quantity," +
                "last_secondary.actual_stock_quantity last_month_actual_stock_quantity,last_secondary.inventory_day last_month_inventory_day," +
                "preceding_secondary.actual_sales_quantity preceding_month_actual_sales_quantity,preceding_secondary.actual_sales_quantity_head_office preceding_month_actual_sales_quantity_head_office,preceding_secondary.actual_stock_quantity preceding_month_actual_stock_quantity");
        queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                " left join secondary_dealer_inventory_data secondary on d.id=secondary.dealer_id and secondary.month=" + month + " and secondary.product_id=dp.product_id" +
                " left join user business_manager on business_manager.id=d.business_manager_user_id" +
                " left join area area on area.id=d.area_id" +
                " left join region region on region.id=d.region_id" +
                " left join secondary_dealer_inventory_data last_secondary on d.id=last_secondary.dealer_id and last_secondary.product_id=dp.product_id and last_secondary.month=" + lastMonth +
                " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id and preceding_secondary.month=" + precedingMonth);
        queryUtil.addQueryParam("p.is_delete", "=", "0");
        queryUtil.addQueryParam("d.is_delete", "=", "0");
        queryUtil.addQueryParam("d.level", "=", "2");
        queryUtil.setSort("d.id");
        queryUtil.setOrder("desc");

        //获取当前商务经理下空缺角色数据
        if (bmUserIds != null && bmUserIds.size() > 0) {
            String bmIdStr = StringUtils.join(bmUserIds, ",");
            queryUtil.addQueryParam("d.business_manager_user_id", "in", bmIdStr);
        }

        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("d.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("d.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("d.id", "in", dealerId);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("dp.product_id", "in", productId);
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<SecondaryDealerInventoryData> list = searchResult.getData();

        //计算总计数据
        if (list != null && list.size() > 0) {
            queryUtil.setSqlSelect("select '总计' dealer_name," + "sum(secondary.plan_purchase_quantity) plan_purchase_quantity,sum(secondary.plan_sales_quantity) plan_sales_quantity,sum(secondary.ref_stock_quantity) actual_stock_quantity,sum(secondary.ref_inventory_day) inventory_day," +
                    "sum(last_secondary.plan_purchase_quantity) last_month_plan_purchase_quantity,sum(last_secondary.actual_purchase_quantity) last_month_actual_purchase_quantity," +
                    "sum(last_secondary.plan_sales_quantity) last_month_plan_sales_quantity,sum(last_secondary.actual_sales_quantity) last_month_actual_sales_quantity,sum(last_secondary.actual_sales_quantity_head_office) last_month_actual_sales_quantity_head_office,sum(last_secondary.theory_stock_quantity) last_month_theory_stock_quantity," +
                    "sum(last_secondary.actual_stock_quantity) last_month_actual_stock_quantity,sum(last_secondary.inventory_day) last_month_inventory_day," +
                    "sum(preceding_secondary.actual_sales_quantity) preceding_month_actual_sales_quantity,sum(preceding_secondary.actual_sales_quantity_head_office) preceding_month_actual_sales_quantity_head_office,sum(preceding_secondary.actual_stock_quantity) preceding_month_actual_stock_quantity");
            queryUtil.setParamValue(new LinkedList<>());
            String countSql = queryUtil.getExecuteSql();
            SecondaryDealerInventoryData countData = SecondaryDealerInventoryData.dao.findFirst(countSql, queryUtil.getParamValue());

            //上上上月
            String lastPrecedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -3);
            queryUtil.setSqlSelect("select sum(preceding_secondary.actual_sales_quantity) last_preceding_month_actual_sales_quantity,sum(preceding_secondary.actual_sales_quantity_head_office) last_preceding_month_actual_sales_quantity_head_office");
            queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                    " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id and preceding_secondary.month=" + lastPrecedingMonth);
            queryUtil.setExecuteSql(null);
            queryUtil.setParamValue(new LinkedList<>());
            String lastPrecedingSql = queryUtil.getExecuteSql();
            SecondaryDealerInventoryData lastPrecedingData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, queryUtil.getParamValue());
            countData.put("last_preceding_month_actual_sales_quantity", lastPrecedingData.getLong("last_preceding_month_actual_sales_quantity"));
            countData.put("last_preceding_month_actual_sales_quantity_head_office", lastPrecedingData.getLong("last_preceding_month_actual_sales_quantity_head_office"));

            countData.put("inventory_day", caculateInventoryDay(countData));
            countData.put("last_month_inventory_day", caculateLastInventoryDay(countData));

            list.add(new SecondaryDealerInventoryData());
            list.add(countData);
        }
        return searchResult;
    }

    /**
     * 保存商务经理提交的进销存数据
     */
    private void saveBmData(User user, Integer month) {
        String data = HttpKit.readData(getRequest());
        SecondaryDealerDto secondaryDealerDto = JSONObject.parseObject(data, SecondaryDealerDto.class);
        secondaryDealerDto.setUserId(user.getId());

        Dealer dealer = Dealer.dao.findById(secondaryDealerDto.getDealerId());
        secondaryDealerDto.setDealer(dealer);

        //取出当月数据
        secondaryDealerDto.setMonth(month);
        SecondaryDealerInventoryData nowInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", month, secondaryDealerDto.getDealerId(), secondaryDealerDto.getProductId());

        //取出上月数据
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        secondaryDealerDto.setLastMonth(Integer.parseInt(lastMonth));
        SecondaryDealerInventoryData lastInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", lastMonth, secondaryDealerDto.getDealerId(), secondaryDealerDto.getProductId());

        //设置当月数据
        secondaryDealerDto.setInventoryData(nowInventoryData);

        //设置上月数据
        secondaryDealerDto.setLastMonthInventoryData(lastInventoryData);

        //得到修改前该行数据
        SecondaryDealerDto beforeData = secondaryDealerDto.clone();

        //设置修改的列的值并取出该字段之前的值
        secondaryDealerDto.setColumnValue();

        //是否需要填写差原因
        boolean needEditDiffCause = srv.updateDataByBmSubmit(secondaryDealerDto);

        JSONObject map = new JSONObject();
        JSONObject update = new JSONObject();
        update.put(secondaryDealerDto.getColumn(), secondaryDealerDto.getValue());
        update.put("last_month_theory_stock_quantity", secondaryDealerDto.getLastMonthTheoryStockQuantity());
        update.put("last_month_actual_stock_quantity", secondaryDealerDto.getLastMonthActualStockQuantity());
        update.put("actual_stock_quantity", secondaryDealerDto.getActualStockQuantity());
        update.put("inventory_day", secondaryDealerDto.getInventoryDay());
        update.put("id", secondaryDealerDto.getId());
        map.put("update", update);
        map.put("diffCause", needEditDiffCause);
        map.put("beforeData", beforeData.getUpdateData());
        result.setData(map);
        renderJson(result);
    }

    /**
     * 保存总部提交的进销存数据
     */
    private void saveBossData(User user, Integer month, Integer step) {
        String data = HttpKit.readData(getRequest());
        SecondaryDealerDto secondaryDealerDto = JSONObject.parseObject(data, SecondaryDealerDto.class);
        secondaryDealerDto.setUserId(user.getId());

        Dealer dealer = Dealer.dao.findById(secondaryDealerDto.getDealerId());
        secondaryDealerDto.setDealer(dealer);

        //取出当月数据
        secondaryDealerDto.setMonth(month);
        SecondaryDealerInventoryData nowInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", month, secondaryDealerDto.getDealerId(), secondaryDealerDto.getProductId());

        //取出上月数据
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        secondaryDealerDto.setLastMonth(Integer.parseInt(lastMonth));
        SecondaryDealerInventoryData lastInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where month=? and dealer_id=? and product_id=?", lastMonth, secondaryDealerDto.getDealerId(), secondaryDealerDto.getProductId());

        //设置当月数据
        secondaryDealerDto.setInventoryData(nowInventoryData);

        //设置上月数据
        secondaryDealerDto.setLastMonthInventoryData(lastInventoryData);

        //得到修改前该行数据
        SecondaryDealerDto beforeData = secondaryDealerDto.clone();

        //设置修改的列的值并取出该字段之前的值
        secondaryDealerDto.setColumnValue();

        //是否需要设置上月理论库存原始值
        boolean setLastMonthOriginalTheoryStockQuantity = false;
        if (step == 1) {
            setLastMonthOriginalTheoryStockQuantity = true;
        }
        //是否需要标红
        Map<String, Boolean> needWarn = srv.updateDataByBossSubmit(secondaryDealerDto, setLastMonthOriginalTheoryStockQuantity);

        JSONObject map = new JSONObject();
        JSONObject update = new JSONObject();
        update.put(secondaryDealerDto.getColumn(), secondaryDealerDto.getValue());
        update.put("last_month_theory_stock_quantity", secondaryDealerDto.getLastMonthTheoryStockQuantity());
        update.put("last_month_actual_stock_quantity", secondaryDealerDto.getLastMonthActualStockQuantity());
        update.put("last_month_inventory_day", secondaryDealerDto.getLastMonthInventoryDay());
        update.put("actual_stock_quantity", secondaryDealerDto.getActualStockQuantity());
        update.put("inventory_day", secondaryDealerDto.getInventoryDay());
        update.put("id", secondaryDealerDto.getId());
        map.put("update", update);
        map.put("diffCause", needWarn.get("diffCause"));
        map.put("diffData", needWarn.get("diffData"));
        map.put("beforeData", beforeData.getUpdateData());
        result.setData(map);
        renderJson(result);
    }

    /**
     * 导出当月进销存数据
     *
     * @param user  当前登录用户
     * @param month 当月 yyyyMM
     * @return
     */
    private void exportNowMonthData(User user, Integer month) {
        String roleName = user.getRoleName();
        String productId = getPara("product_id");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String businessUserId = getPara("business_user_id");
        String dealerId = getPara("dealer_id");


        if (StringUtils.isBlank(productId)) {
            throw new ExceptionForJson("请选择产品");
        }

        //上月
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        //上上月
        String precedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -2);

        QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
        queryUtil.setSqlSelect("select secondary.id," + month + " month,p.name product_name,dp.product_id product_id,last_secondary.id last_id,d.upstream_name,area.id area_id,area.name area_name,region.id region_id,region.name region_name,d.id dealer_id,d.name dealer_name," +
                "d.area_manager_user_id,d.business_manager_user_id,business_manager.name business_manager_name," +
                "secondary.plan_purchase_quantity,secondary.plan_sales_quantity,secondary.ref_stock_quantity actual_stock_quantity,secondary.ref_inventory_day inventory_day,last_secondary.diff_cause," +
                "last_secondary.plan_purchase_quantity last_month_plan_purchase_quantity,last_secondary.actual_purchase_quantity last_month_actual_purchase_quantity," +
                "last_secondary.plan_sales_quantity last_month_plan_sales_quantity,last_secondary.actual_sales_quantity last_month_actual_sales_quantity,last_secondary.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office,last_secondary.theory_stock_quantity last_month_theory_stock_quantity," +
                "last_secondary.actual_stock_quantity last_month_actual_stock_quantity,last_secondary.inventory_day last_month_inventory_day," +
                "preceding_secondary.actual_sales_quantity preceding_month_actual_sales_quantity,preceding_secondary.actual_stock_quantity preceding_month_actual_stock_quantity");
        queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                " left join secondary_dealer_inventory_data secondary on d.id=secondary.dealer_id and secondary.month=" + month + " and secondary.product_id=dp.product_id" +
                " left join user business_manager on business_manager.id=d.business_manager_user_id" +
                " left join area area on area.id=d.area_id" +
                " left join region region on region.id=d.region_id" +
                " left join secondary_dealer_inventory_data last_secondary on d.id=last_secondary.dealer_id and last_secondary.product_id=dp.product_id and last_secondary.month=" + lastMonth +
                " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id and preceding_secondary.month=" + precedingMonth);
        queryUtil.addQueryParam("p.is_delete", "=", "0");
        queryUtil.addQueryParam("d.is_delete", "=", "0");
        queryUtil.addQueryParam("d.level", "=", "2");
        queryUtil.setSort("d.id");
        queryUtil.setOrder("desc");
        queryUtil.setPageSize(Integer.MAX_VALUE);

        Long userId = user.getId();
        if ("商务经理".equals(roleName)) {
            queryUtil.addQueryParam("d.business_manager_user_id", "=", userId);
        } else if ("大区经理".equals(roleName)) {
            queryUtil.addQueryParam("d.area_manager_user_id", "=", userId);
        }

        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("d.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("d.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(businessUserId)) {
            queryUtil.addQueryParam("d.business_manager_user_id", "in", businessUserId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("d.id", "in", dealerId);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("dp.product_id", "in", productId);
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<SecondaryDealerInventoryData> list = searchResult.getData();
        if (list == null) {
            list = new ArrayList<>();
        }

        Map<String, List<SecondaryDealerInventoryData>> exportMap = new HashMap<>();
        list.forEach(data -> {
            String productName = data.get("product_name");
            List<SecondaryDealerInventoryData> datas = exportMap.get(productName);
            if (datas == null) {
                datas = new ArrayList<>();
                exportMap.put(productName, datas);
            }
            datas.add(data);
        });

        //生成文件目录
        String fileName = "二级商进销存数据" + System.currentTimeMillis() + ".xls";
        Map<String, String> mergeMap = new ImmutableMap.Builder<String, String>()
                .put("upstream_name", "dealer_id")
                .put("area_name", "dealer_id")
                .put("dealer_name", "dealer_id")
                .build();
        try {
            PoiUtils.exportMergeData(getResponse(), SecondaryDealerInventoryData.class, exportMap, mergeMap, fileName,
                    "大区,省份,商务经理,商业公司名称,上游商业,产品名称," + precedingMonth + "月实际库存," + lastMonth + "月预估进货," + lastMonth + "月实际进货," + lastMonth + "月销售预估," + lastMonth + "月实际销售," + lastMonth + "月总部上传实际销售," + lastMonth + "月理论库存," + lastMonth + "月实际库存," + lastMonth + "月库存天数," + lastMonth + "月库存差异原因," + month + "月进货预估," + month + "月销售预估," + month + "月库存," + month + "月库存天数",
                    "area_name,region_name,business_manager_name,dealer_name,upstream_name,product_name,preceding_month_actual_stock_quantity,last_month_plan_purchase_quantity,last_month_actual_purchase_quantity,last_month_plan_sales_quantity,last_month_actual_sales_quantity,last_month_actual_sales_quantity_head_office,last_month_theory_stock_quantity,last_month_actual_stock_quantity,last_month_inventory_day,diff_cause,plan_purchase_quantity,plan_sales_quantity,actual_stock_quantity,inventory_day", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * 大区经理在开放期前一天可导出空缺的商务经理数据
     *
     * @param bmUserIds 空缺的用户
     * @param month     当月 yyyyMM
     * @return
     */
    private void exportNowMonthBmVacancyData(List<Long> bmUserIds, Integer month) {
        if (bmUserIds == null || bmUserIds.size() == 0) {
            return;
        }

        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String dealerId = getPara("dealer_id");
        String productId = getPara("product_id");

        if (StringUtils.isBlank(productId)) {
            throw new ExceptionForJson("请选择产品");
        }

        //上月
        String lastMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        //上上月
        String precedingMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -2);
        QueryUtil queryUtil = getQueryUtil(SecondaryDealerInventoryData.class);
        queryUtil.setSqlSelect("select secondary.id," + month + " month,p.name product_name,dp.product_id product_id,last_secondary.id last_id,d.upstream_name,area.id area_id,area.name area_name,region.id region_id,region.name region_name,d.id dealer_id,d.name dealer_name," +
                "d.area_manager_user_id,d.business_manager_user_id,business_manager.name business_manager_name," +
                "secondary.plan_purchase_quantity,secondary.plan_sales_quantity,secondary.ref_stock_quantity actual_stock_quantity,secondary.ref_inventory_day inventory_day,last_secondary.diff_cause," +
                "last_secondary.plan_purchase_quantity last_month_plan_purchase_quantity,last_secondary.actual_purchase_quantity last_month_actual_purchase_quantity," +
                "last_secondary.plan_sales_quantity last_month_plan_sales_quantity,last_secondary.actual_sales_quantity last_month_actual_sales_quantity,last_secondary.actual_sales_quantity_head_office last_month_actual_sales_quantity_head_office,last_secondary.theory_stock_quantity last_month_theory_stock_quantity," +
                "last_secondary.actual_stock_quantity last_month_actual_stock_quantity,last_secondary.inventory_day last_month_inventory_day," +
                "preceding_secondary.actual_sales_quantity preceding_month_actual_sales_quantity,preceding_secondary.actual_stock_quantity preceding_month_actual_stock_quantity");
        queryUtil.setSqlExceptSelect("from dealer_product dp left join dealer d on d.id=dp.dealer_id left join product p on p.id=dp.product_id" +
                " left join secondary_dealer_inventory_data secondary on d.id=secondary.dealer_id and secondary.month=" + month + " and secondary.product_id=dp.product_id" +
                " left join user business_manager on business_manager.id=d.business_manager_user_id" +
                " left join area area on area.id=d.area_id" +
                " left join region region on region.id=d.region_id" +
                " left join secondary_dealer_inventory_data last_secondary on d.id=last_secondary.dealer_id and last_secondary.product_id=dp.product_id and last_secondary.month=" + lastMonth +
                " left join secondary_dealer_inventory_data preceding_secondary on d.id=preceding_secondary.dealer_id and preceding_secondary.product_id=dp.product_id and preceding_secondary.month=" + precedingMonth);
        queryUtil.addQueryParam("p.is_delete", "=", "0");
        queryUtil.addQueryParam("d.is_delete", "=", "0");
        queryUtil.addQueryParam("d.level", "=", "2");
        queryUtil.setSort("d.id");
        queryUtil.setOrder("desc");
        queryUtil.setPageSize(Integer.MAX_VALUE);

        //获取当前商务经理下空缺角色数据
        if (bmUserIds != null && bmUserIds.size() > 0) {
            String bmIdStr = StringUtils.join(bmUserIds, ",");
            queryUtil.addQueryParam("d.business_manager_user_id", "in", bmIdStr);
        }

        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("d.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("d.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("d.id", "in", dealerId);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("dp.product_id", "in", productId);
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<SecondaryDealerInventoryData> list = searchResult.getData();
        if (list == null) {
            list = new ArrayList<>();
        }

        Map<String, List<SecondaryDealerInventoryData>> exportMap = new HashMap<>();
        list.forEach(data -> {
            String productName = data.get("product_name");
            List<SecondaryDealerInventoryData> datas = exportMap.get(productName);
            if (datas == null) {
                datas = new ArrayList<>();
                exportMap.put(productName, datas);
            }
            datas.add(data);
        });

        //生成文件目录
        String fileName = "二级商进销存数据" + System.currentTimeMillis() + ".xls";
        Map<String, String> mergeMap = new ImmutableMap.Builder<String, String>()
                .put("upstream_name", "dealer_id")
                .put("area_name", "dealer_id")
                .put("dealer_name", "dealer_id")
                .build();
        try {
            PoiUtils.exportMergeData(getResponse(), SecondaryDealerInventoryData.class, exportMap, mergeMap, fileName,
                    "大区,省份,商务经理,商业公司名称,上游商业,产品名称," + precedingMonth + "月实际库存," + lastMonth + "月预估进货," + lastMonth + "月实际进货," + lastMonth + "月销售预估," + lastMonth + "月实际销售," + lastMonth + "月总部上传实际销售," + lastMonth + "月理论库存," + lastMonth + "月实际库存," + lastMonth + "月库存天数," + lastMonth + "库存差异原因," + month + "月进货预估," + month + "月销售预估," + month + "月库存," + month + "月库存天数",
                    "area_name,region_name,business_manager_name,dealer_name,upstream_name,product_name,preceding_month_actual_stock_quantity,last_month_plan_purchase_quantity,last_month_actual_purchase_quantity,last_month_plan_sales_quantity,last_month_actual_sales_quantity,last_month_actual_sales_quantity_head_office,last_month_theory_stock_quantity,last_month_actual_stock_quantity,last_month_inventory_day,diff_cause,plan_purchase_quantity,plan_sales_quantity,actual_stock_quantity,inventory_day", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

    //计算上月库存天数
    private BigDecimal caculateLastInventoryDay(SecondaryDealerInventoryData secondaryDealerInventoryData) {
        //上上上月实际销售
        Long upActualSalesQuanity = secondaryDealerInventoryData.getLong("last_preceding_month_actual_sales_quantity");
        Long upActualSalesQuanityHeadOffice = secondaryDealerInventoryData.getLong("last_preceding_month_actual_sales_quantity_head_office");
        if (upActualSalesQuanityHeadOffice != null) {
            upActualSalesQuanity = upActualSalesQuanityHeadOffice;
        }
        //上上月实际销售
        Long precedingActSalQuan = secondaryDealerInventoryData.getLong("preceding_month_actual_sales_quantity");
        Long precedingActSalQuanHeadOffice = secondaryDealerInventoryData.getLong("preceding_month_actual_sales_quantity_head_office");
        if (precedingActSalQuanHeadOffice != null) {
            precedingActSalQuan = precedingActSalQuanHeadOffice;
        }
        //上月实际销售(5)
        Long lastActSaleQuan = LongUtil.isNull(secondaryDealerInventoryData.getLong("last_month_actual_sales_quantity"));
        Long lastActSaleQuanHeadOffice = secondaryDealerInventoryData.getLong("last_month_actual_sales_quantity_head_office");
        if (lastActSaleQuanHeadOffice != null) {
            lastActSaleQuan = lastActSaleQuanHeadOffice;
        }
        //上月实际库存(7)
        Long lastActStoQuan = LongUtil.isNull(secondaryDealerInventoryData.getLong("last_month_actual_stock_quantity"));

        //计算上月库存天数(8)
        List<Long> lastMonthList = new ArrayList<>();
        if (lastActSaleQuan != null) {
            lastMonthList.add(lastActSaleQuan);
        }
        if (precedingActSalQuan != null) {
            lastMonthList.add(precedingActSalQuan);
        }
        if (upActualSalesQuanity != null) {
            lastMonthList.add(upActualSalesQuanity);
        }
        BigDecimal lastAverage = srv.caculateAverage(lastMonthList);
        if (BigDecimalUtil.gt(lastAverage, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(lastActStoQuan * 90).divide(lastAverage, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            return inventoryDay;
        } else {
            return new BigDecimal(0);
        }
    }

    //计算当月库存天数
    private BigDecimal caculateInventoryDay(SecondaryDealerInventoryData secondaryDealerInventoryData) {
        //上上月实际销售
        Long precedingActSalQuan = secondaryDealerInventoryData.getLong("preceding_month_actual_sales_quantity");
        Long precedingActSalQuanHeadOffice = secondaryDealerInventoryData.getLong("preceding_month_actual_sales_quantity_head_office");
        if (precedingActSalQuanHeadOffice != null) {
            precedingActSalQuan = precedingActSalQuanHeadOffice;
        }
        //上月实际销售(5)
        Long lastActSaleQuan = secondaryDealerInventoryData.getLong("last_month_actual_sales_quantity");
        Long lastActSaleQuanHeadOffice = secondaryDealerInventoryData.getLong("last_month_actual_sales_quantity_head_office");
        if (lastActSaleQuanHeadOffice != null) {
            lastActSaleQuan = lastActSaleQuanHeadOffice;
        }
        //当月销售预估(10)
        Long planSalQuan = LongUtil.isNull(secondaryDealerInventoryData.getLong("plan_sales_quantity"));
        //当月实际库存(11)
        Long actualStockQuantity = LongUtil.isNull(secondaryDealerInventoryData.getLong("actual_stock_quantity"));

        //计算当月库存天数(12)
        List<Long> nowMonthList = new ArrayList<>();
        if (planSalQuan != null) {
            nowMonthList.add(planSalQuan);
        }
        if (lastActSaleQuan != null) {
            nowMonthList.add(lastActSaleQuan);
        }
        if (precedingActSalQuan != null) {
            nowMonthList.add(precedingActSalQuan);
        }
        BigDecimal average = srv.caculateAverage(nowMonthList);
        if (BigDecimalUtil.gt(average, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(actualStockQuantity * 90).divide(average, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            return inventoryDay;
        } else {
            return new BigDecimal(0);
        }
    }
}