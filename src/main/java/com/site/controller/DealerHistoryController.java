package com.site.controller;

import com.google.common.collect.ImmutableMap;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.*;
import com.site.definition.Constants;
import com.site.service.DealerTwoService;
import com.site.service.ProductService;
import com.site.service.RoleConfigService;
import com.site.utils.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class DealerHistoryController extends BaseController {

    private RoleConfigService roleConfigService;
    private ProductService productService;
    static DealerTwoService srv = DealerTwoService.me;

    //字段优先级数据
    final static Map<String, Constants.JxcColumnType> COLUMN_TYPE = new ImmutableMap.Builder<String, Constants.JxcColumnType>()
            .put("plan_purchase_quantity", Constants.JxcColumnType.PLAN_PURCHASE_QUANTITY)
            .put("actual_purchase_quantity", Constants.JxcColumnType.ACTUAL_PURCHASE_QUANTITY)
            .put("plan_sales_quantity", Constants.JxcColumnType.PLAN_SALES_QUANTITY)
            .put("actual_sales_quantity", Constants.JxcColumnType.ACTUAL_SALES_QUANTITY)
            .put("actual_sales_quantity_head_office", Constants.JxcColumnType.ACTUAL_SALES_QUANTITY_HEAD_OFFICE)
            .put("theory_stock_quantity", Constants.JxcColumnType.THEORY_STOCK_QUANTITY)
            .put("actual_stock_quantity", Constants.JxcColumnType.ACTUAL_STOCK_QUANTITY)
            .put("inventory_day", Constants.JxcColumnType.INVENTORY_DAY)
            .put("diff_cause", Constants.JxcColumnType.DIFF_CAUSE)
            .build();

    public void index() {
        User user = getLoginUser();
        String roleName = user.getRoleName();
        setAttr("roleName", roleName);
        String month = DateUtils.getYearAndMonth("yyyy-MM", -1) + " - " + DateUtils.getYearAndMonth("yyyy-MM", 0);
        setAttr("month", month);
        render("list.html");
    }

    /**
     * 进销存历史数据列表
     * dealerHistory/list
     */
    public void list() {
        //查询历史数据
        QueryUtil.SearchResult nowMonthResult = getHistoryData();
        renderJson(nowMonthResult);
    }

    /**
     * 获取当月进销存数据
     *
     * @return
     */
    private QueryUtil.SearchResult getHistoryData() {
        User user = getLoginUser();
        String roleName = user.getRoleName();

        String month = getPara("month");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String businessUserId = getPara("business_manager_user_id");
        String dealerId = getPara("dealer_id");
        String dealerLevel = getPara("dealer_level");
        String productId = getPara("product_id");
        String columns = getPara("column");

        String replactMonth = month.replaceAll(" - ", "").trim();
        if (StringUtils.isBlank(columns)) {
            throw new ExceptionForJson("请选择类型");
        }
        if (StringUtils.isBlank(replactMonth)) {
            throw new ExceptionForJson("请选择时间");
        }

        //组装column
        String[] columnArr = columns.split(",");
        boolean existInventoryDay = false;
        List<String> columnList = Arrays.asList(columnArr.clone());
        Set<String> columnSet = new HashSet<>();
        Set<String> countColumnSet = new HashSet<>();
        for (int i = 0; i < columnArr.length; i++) {
            if ("inventory_day".equals(columnArr[i])) {
                existInventoryDay = true;
                columnSet.add("sum(actual_stock_quantity) actual_stock_quantity");
                columnSet.add("sum(actual_sales_quantity) actual_sales_quantity");
                columnSet.add("sum(inventory_day) inventory_day");
                columnSet.add("sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                countColumnSet.add("actual_stock_quantity");
                countColumnSet.add("actual_sales_quantity");
                countColumnSet.add("inventory_day");
                countColumnSet.add("actual_sales_quantity_head_office");
                continue;
            }
            columnSet.add("sum(" + columnArr[i] + ") " + columnArr[i]);
            countColumnSet.add(columnArr[i]);
        }
        String queryColumn = StringUtils.join(columnList, ",");

        QueryUtil queryUtil = getQueryUtil(DealerInventoryDataHistory.class);
        queryUtil.setSqlSelect("select history.dealer_id,history.dealer_name,history.dealer_level,history.business_manager_user_name,history.area_name,history.region_name,history.product_id,history.product_name,history.month," + queryColumn);
        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history inner join dealer dealer on dealer.id=history.dealer_id");
        queryUtil.setGroupColunm("history.dealer_id,history.product_id,history.month");
        queryUtil.setSort("history.dealer_id,history.month");
        queryUtil.setOrder("desc,desc");
        queryUtil.setPageSize(Integer.MAX_VALUE);

        Long userId = user.getId();
        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("history.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("history.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(businessUserId)) {
            queryUtil.addQueryParam("history.business_manager_user_id", "in", businessUserId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("history.dealer_id", "in", dealerId);
        } else {
            if ("商务经理".equals(roleName)) {
                queryUtil.addQueryParam("dealer.business_manager_user_id", "=", userId);
            } else if ("大区经理".equals(roleName)) {
                queryUtil.addQueryParam("dealer.area_manager_user_id", "=", userId);
            }
        }
        if (StringUtils.isNotBlank(dealerLevel)) {
            queryUtil.addQueryParam("history.dealer_level", "in", dealerLevel);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("history.product_id", "in", productId);
        }
        List<String> monthList = new ArrayList<>();
        if (StringUtils.isNotBlank(month)) {
            String[] monthArr = month.split(" - ");
            if (monthArr.length >= 1 && StringUtils.isNotBlank(monthArr[0])) {
                String monthStr = monthArr[0].replaceAll("-", "");
                queryUtil.addQueryParam("history.month", ">=", monthStr);
                monthList.add(monthStr);
            }
            if (monthArr.length >= 2 && StringUtils.isNotBlank(monthArr[1])) {
                String monthStr = monthArr[1].replaceAll("-", "");
                queryUtil.addQueryParam("history.month", "<=", monthStr);
                monthList.add(monthStr);
            }
            if (monthList.size() == 1) {
                monthList.add(DateUtils.getYearAndMonth("yyyyMM", 0));
            }
        }

        //记录经销商信息
        Map<String, DealerInventoryDataHistory> dealerMap = new HashMap();
        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<DealerInventoryDataHistory> list = searchResult.getData();
        list.forEach(history -> columnList.forEach(column -> {
            Long historyDealerId = history.getDealerId();
            String productName = history.getProductName();
            String key = historyDealerId + "_" + productName + "_" + column;

            DealerInventoryDataHistory dealerHistory = dealerMap.get(key);
            if (dealerHistory == null) {
                dealerHistory = new DealerInventoryDataHistory();
                dealerHistory.setDealerName(history.getDealerName());
                dealerHistory.setDealerLevel(history.getDealerLevel());
                dealerHistory.setBusinessManagerUserName(history.getBusinessManagerUserName());
                dealerHistory.setAreaName(history.getAreaName());
                dealerHistory.setRegionName(history.getRegionName());
                dealerHistory.setProductName(productName);
                dealerHistory.setProductId(history.getProductId());
                dealerHistory.setDealerId(historyDealerId);

                dealerMap.put(key, dealerHistory);
            }

            dealerHistory.put("type", COLUMN_TYPE.get(column).getName());
            dealerHistory.put("score_type", column);
            dealerHistory.put(history.getMonth() + "", history.get(column, 0));
        }));

        //计算总计数据
        queryUtil.setSqlSelect("select history.product_name,history.month," + StringUtils.join(columnSet, ","));
        queryUtil.setGroupColunm("month");
        queryUtil.setExecuteSql(null);
        queryUtil.setParamValue(new LinkedList<>());
        String countSql = queryUtil.getExecuteSql();
        List<DealerInventoryDataHistory> countDatas = DealerInventoryDataHistory.dao.find(countSql, queryUtil.getParamValue());
        //记录总计信息
        Map<String, DealerInventoryDataHistory> countMap = new HashMap();
        countDatas.forEach(countData -> countColumnSet.forEach(column -> {
            Long historyDealerId = 0l;
            String key = historyDealerId + column;

            DealerInventoryDataHistory countHistory = countMap.get(key);
            if (countHistory == null) {
                countHistory = new DealerInventoryDataHistory();
                countHistory.setDealerName("总计");
                countHistory.setDealerId(historyDealerId);
                countHistory.setProductId(0l);

                countMap.put(key, countHistory);
            }

            countHistory.put("type", COLUMN_TYPE.get(column).getName());
            countHistory.put("score_type", column);
            countHistory.put(countData.getMonth() + "", countData.get(column));
        }));

        if (existInventoryDay) {
            List<String> months = DateUtils.getDifferenceMonth(monthList.get(0), monthList.get(1));
            countMap.forEach((key,value) -> {
                if (key.indexOf("inventory_day") < 0) {
                    return;
                }

                String column = value.getStr("score_type");
                String actualSalesKey = key.replace(column, "actual_sales_quantity");
                String actualStockKey = key.replace(column, "actual_stock_quantity");
                String actualSalesHeadOfficeKey = key.replace(column, "actual_sales_quantity_head_office");
                DealerInventoryDataHistory actualSalesData = countMap.get(actualSalesKey);
                DealerInventoryDataHistory actualStockData = countMap.get(actualStockKey);
                DealerInventoryDataHistory actualSalesHeadOfficeData = countMap.get(actualSalesHeadOfficeKey);

                months.forEach(nowMonth -> {
                    SecondaryDealerInventoryData data = new SecondaryDealerInventoryData();
                    //拉取当月数据
                    data.put("last_month_actual_sales_quantity", actualSalesData.get(nowMonth));
                    data.put("last_month_actual_stock_quantity", actualStockData.get(nowMonth));
                    data.put("last_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(nowMonth));

                    //拉取上月数据
                    String lastMonth = DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -1);
                    if (months.contains(lastMonth)) {
                        data.put("preceding_month_actual_sales_quantity", actualSalesData.get(lastMonth));
                        data.put("preceding_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(lastMonth));
                    } else {
                        queryUtil.setSqlSelect("select sum(actual_sales_quantity) actual_sales_quantity,sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history");
                        queryUtil.setExecuteSql(null);
                        queryUtil.setParamValue(new LinkedList<>());
                        String lastPrecedingSql = queryUtil.getExecuteSql();
                        Object[] params = queryUtil.getParamValue();
                        params[params.length-2] = lastMonth;
                        params[params.length-1] = lastMonth;
                        SecondaryDealerInventoryData secondaryDealerInventoryData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, params);
                        if (secondaryDealerInventoryData != null) {
                            data.put("preceding_month_actual_sales_quantity", secondaryDealerInventoryData.getLong("actual_sales_quantity"));
                            data.put("preceding_month_actual_sales_quantity_head_office", secondaryDealerInventoryData.getLong("sales_quantity_head_office"));
                        }
                    }

                    //拉取上上月数据
                    String precedingMonth = DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -2);
                    if (months.contains(precedingMonth)) {
                        data.put("last_preceding_month_actual_sales_quantity", actualSalesData.get(precedingMonth));
                        data.put("last_preceding_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(precedingMonth));
                    } else {
                        queryUtil.setSqlSelect("select sum(actual_sales_quantity) actual_sales_quantity,sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history");
                        queryUtil.setExecuteSql(null);
                        queryUtil.setParamValue(new LinkedList<>());
                        String lastPrecedingSql = queryUtil.getExecuteSql();
                        Object[] params = queryUtil.getParamValue();
                        params[params.length-2] = precedingMonth;
                        params[params.length-1] = precedingMonth;
                        SecondaryDealerInventoryData secondaryDealerInventoryData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, params);
                        if (secondaryDealerInventoryData != null) {
                            data.put("last_preceding_month_actual_sales_quantity", secondaryDealerInventoryData.getLong("actual_sales_quantity"));
                            data.put("last_preceding_month_actual_sales_quantity_head_office", secondaryDealerInventoryData.getLong("sales_quantity_head_office"));
                        }
                    }

                    value.put(nowMonth, caculateLastInventoryDay(data));
                });
            });
        }

        //计算 total
        queryUtil.setSqlSelect("select count(*) count from (select 1 count");
        queryUtil.setGroupColunm("history.dealer_id,history.product_id");
        queryUtil.setExecuteSql(null);
        queryUtil.setParamValue(new LinkedList<>());
        String totalSql = queryUtil.getExecuteSql() + ") tb";
        DealerInventoryDataHistory totalData = DealerInventoryDataHistory.dao.findFirst(totalSql, queryUtil.getParamValue());
        //设置总页数和结果数量
        int count = totalData.getInt("count");
        searchResult.setCount(count);
        int totalPage = (count-1)/queryUtil.getPageSize() +1;
        searchResult.setTotalPage(totalPage);

        //将总计数据和查询数据合并
        List<DealerInventoryDataHistory> mergeData = new ArrayList<>();
        dealerMap.forEach((key,value) -> mergeData.add(value));
        countMap.forEach((key,value) -> mergeData.add(value));

        //去除多余的数据
        List<DealerInventoryDataHistory> result = new ArrayList<>();
        Set<String> originalColumnSet = new HashSet<>(Arrays.asList(columnArr));
        mergeData.forEach(data -> {
            String column = data.get("score_type");
            if (originalColumnSet.contains(column)) {
                result.add(data);
            }
        });

        //根据经销商 id 降序排序
        Collections.sort(result, (a , b) -> {
            Long aId = a.getDealerId();
            Long bId = b.getDealerId();
            int score = bId.compareTo(aId);
            //先行比较 dealer_id
            if (score != 0) {
                return score;
            }

            //继续比较 product_id
            Long aProductId = a.getProductId();
            Long bProductId = b.getProductId();
            int proScore = bProductId.compareTo(aProductId);
            //先行比较 dealer_id
            if (proScore != 0) {
                return proScore;
            }

            //继续比较字段优先级
            int x = COLUMN_TYPE.get(a.get("score_type")).getScore();
            int y = COLUMN_TYPE.get(b.get("score_type")).getScore();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        });

        searchResult.setData(result);
        return searchResult;
    }

    /**
     * 导出历史数据
     */
    public void exportHistory() {
        User user = getLoginUser();
        String roleName = user.getRoleName();

        String month = getPara("month");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String businessUserId = getPara("business_manager_user_id");
        String dealerId = getPara("dealer_id");
        String dealerLevel = getPara("dealer_level");
        String productId = getPara("product_id");
        String columns = getPara("column");

        String replactMonth = month.replaceAll(" - ", "").trim();
        if (StringUtils.isBlank(columns)) {
            throw new ExceptionForJson("请选择类型");
        }
        if (StringUtils.isBlank(replactMonth)) {
            throw new ExceptionForJson("请选择时间");
        }

        //组装column
        // columns = columns;
        String[] columnArr = columns.split(",");
        boolean existInventoryDay = false;
        List<String> columnList = Arrays.asList(columnArr.clone());
        Set<String> columnSet = new HashSet<>();
        Set<String> countColumnSet = new HashSet<>();
        for (int i = 0; i < columnArr.length; i++) {
            if ("diff_cause".equals(columnArr[i])) {
                continue;
            }
            if ("inventory_day".equals(columnArr[i])) {
                existInventoryDay = true;
                columnSet.add("sum(actual_stock_quantity) actual_stock_quantity");
                columnSet.add("sum(actual_sales_quantity) actual_sales_quantity");
                columnSet.add("sum(inventory_day) inventory_day");
                columnSet.add("sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                countColumnSet.add("actual_stock_quantity");
                countColumnSet.add("actual_sales_quantity");
                countColumnSet.add("inventory_day");
                countColumnSet.add("actual_sales_quantity_head_office");
                countColumnSet.add("diff_cause");
                continue;
            }
            columnSet.add("sum(" + columnArr[i] + ") " + columnArr[i]);
            countColumnSet.add(columnArr[i]);
        }
        String queryColumn = StringUtils.join(columnList, ",");

        QueryUtil queryUtil = getQueryUtil(DealerInventoryDataHistory.class);
        queryUtil.setSqlSelect("select history.dealer_id,history.area_name,history.dealer_name,history.dealer_level,history.business_manager_user_name,history.region_name,history.product_id,history.product_name,history.month," + queryColumn);
        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history inner join dealer dealer on dealer.id=history.dealer_id");
        queryUtil.setGroupColunm("history.region_id,history.dealer_id,history.dealer_level,history.product_id,history.month");
        queryUtil.setSort("history.dealer_id,history.month");
        queryUtil.setOrder("desc,desc");
        queryUtil.setPageSize(Integer.MAX_VALUE);

        Long userId = user.getId();
        if (StringUtils.isNotBlank(areaId)) {
            queryUtil.addQueryParam("history.area_id", "in", areaId);
        }
        if (StringUtils.isNotBlank(regionId)) {
            queryUtil.addQueryParam("history.region_id", "in", regionId);
        }
        if (StringUtils.isNotBlank(businessUserId)) {
            queryUtil.addQueryParam("history.business_manager_user_id", "in", businessUserId);
        }
        if (StringUtils.isNotBlank(dealerId)) {
            queryUtil.addQueryParam("history.dealer_id", "in", dealerId);
        } else {
            if ("商务经理".equals(roleName)) {
                queryUtil.addQueryParam("dealer.business_manager_user_id", "=", userId);
            } else if ("大区经理".equals(roleName)) {
                queryUtil.addQueryParam("dealer.area_manager_user_id", "=", userId);
            }
        }
        if (StringUtils.isNotBlank(dealerLevel)) {
            queryUtil.addQueryParam("history.dealer_level", "in", dealerLevel);
        }
        if (StringUtils.isNotBlank(productId)) {
            queryUtil.addQueryParam("history.product_id", "in", productId);
        }
        List<String> monthList = new ArrayList<>();
        if (StringUtils.isNotBlank(month)) {
            String[] monthArr = month.split(" - ");
            if (monthArr.length >= 1 && StringUtils.isNotBlank(monthArr[0])) {
                String monthStr = monthArr[0].replaceAll("-", "");
                queryUtil.addQueryParam("history.month", ">=", monthStr);
                monthList.add(monthStr);
            }
            if (monthArr.length >= 2 && StringUtils.isNotBlank(monthArr[1])) {
                String monthStr = monthArr[1].replaceAll("-", "");
                queryUtil.addQueryParam("history.month", "<=", monthStr);
                monthList.add(monthStr);
            }
            if (monthList.size() == 1) {
                monthList.add(DateUtils.getYearAndMonth("yyyyMM", 0));
            }
        }

        //记录经销商信息
        Map<String, DealerInventoryDataHistory> dealerMap = new HashMap();
        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<DealerInventoryDataHistory> list = searchResult.getData();
        list.forEach(history -> columnList.forEach(column -> {
            Long historyDealerId = history.getDealerId();
            String productName = history.getProductName();
            String key = historyDealerId + "_" + productName + "_" + column;

            DealerInventoryDataHistory dealerHistory = dealerMap.get(key);
            if (dealerHistory == null) {
                dealerHistory = new DealerInventoryDataHistory();
                dealerHistory.setDealerName(history.getDealerName());
                dealerHistory.setDealerLevel("T" + history.getDealerLevel());
                dealerHistory.setBusinessManagerUserName(history.getBusinessManagerUserName());
                dealerHistory.setAreaName(history.getAreaName());
                dealerHistory.setRegionName(history.getRegionName());
                dealerHistory.setProductName(productName);
                dealerHistory.setProductId(history.getProductId());
                dealerHistory.setDealerId(historyDealerId);

                dealerMap.put(key, dealerHistory);
            }

            Constants.JxcColumnType columnType = COLUMN_TYPE.get(column);
            dealerHistory.put("type", columnType.getName());
            dealerHistory.put("score_type", column);
            dealerHistory.put("backgroundColor", columnType.getColor());
            dealerHistory.put(history.getMonth() + "", history.get(column));
        }));

        //计算总计数据
        queryUtil.setSqlSelect("select history.product_name,history.product_id,history.month," + StringUtils.join(columnSet, ","));
        queryUtil.setGroupColunm("history.month,history.product_id");
        queryUtil.setExecuteSql(null);
        queryUtil.setParamValue(new LinkedList<>());
        String countSql = queryUtil.getExecuteSql();
        List<DealerInventoryDataHistory> countDatas = DealerInventoryDataHistory.dao.find(countSql, queryUtil.getParamValue());
        //记录总计信息
        Map<String, DealerInventoryDataHistory> countMap = new HashMap();
        countDatas.forEach(countData -> countColumnSet.forEach(column -> {
            String key = countData.getProductName() + column;

            DealerInventoryDataHistory countHistory = countMap.get(key);
            if (countHistory == null) {
                countHistory = new DealerInventoryDataHistory();
                countHistory.setDealerName("总计");
                countHistory.setProductName(countData.getProductName());
                countHistory.setDealerId(0l);
                countHistory.setProductId(countData.getProductId());

                countMap.put(key, countHistory);
            }

            Constants.JxcColumnType columnType = COLUMN_TYPE.get(column);
            countHistory.put("type", columnType.getName());
            countHistory.put("score_type", column);
            countHistory.put("backgroundColor", columnType.getColor());
            countHistory.put(countData.getMonth() + "", countData.get(column));
        }));

        List<String> months = DateUtils.getDifferenceMonth(monthList.get(0), monthList.get(1));
        if (existInventoryDay) {
            countMap.forEach((key,value) -> {
                if (key.indexOf("inventory_day") < 0) {
                    return;
                }

                Long nowProductId = value.getProductId();
                String column = value.getStr("score_type");
                String actualSalesKey = key.replace(column, "actual_sales_quantity");
                String actualStockKey = key.replace(column, "actual_stock_quantity");
                String actualSalesHeadOfficeKey = key.replace(column, "actual_sales_quantity_head_office");
                DealerInventoryDataHistory actualSalesData = countMap.get(actualSalesKey);
                DealerInventoryDataHistory actualStockData = countMap.get(actualStockKey);
                DealerInventoryDataHistory actualSalesHeadOfficeData = countMap.get(actualSalesHeadOfficeKey);

                months.forEach(nowMonth -> {
                    SecondaryDealerInventoryData data = new SecondaryDealerInventoryData();
                    //拉取当月数据
                    data.put("last_month_actual_sales_quantity", actualSalesData.get(nowMonth));
                    data.put("last_month_actual_stock_quantity", actualStockData.get(nowMonth));
                    data.put("last_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(nowMonth));

                    //拉取上月数据
                    String lastMonth = DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -1);
                    if (months.contains(lastMonth)) {
                        data.put("preceding_month_actual_sales_quantity", actualSalesData.get(lastMonth));
                        data.put("preceding_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(lastMonth));
                    } else {
                        queryUtil.setSqlSelect("select sum(actual_sales_quantity) actual_sales_quantity,sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history");
                        queryUtil.setExecuteSql(null);
                        queryUtil.setParamValue(new LinkedList<>());
                        String lastPrecedingSql = queryUtil.getExecuteSql();
                        lastPrecedingSql = lastPrecedingSql.replaceFirst("group by", "and product_id=? group by");
                        List<Object> params = new ArrayList<>();
                        Object[] paramArr = queryUtil.getParamValue();
                        params.addAll(Arrays.asList(paramArr));
                        params.set(paramArr.length-2, lastMonth);
                        params.set(paramArr.length-1, lastMonth);
                        params.add(nowProductId);
                        SecondaryDealerInventoryData secondaryDealerInventoryData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, params.toArray(new Object[params.size()]));
                        if (secondaryDealerInventoryData != null) {
                            data.put("preceding_month_actual_sales_quantity", secondaryDealerInventoryData.getLong("actual_sales_quantity"));
                            data.put("preceding_month_actual_sales_quantity_head_office", secondaryDealerInventoryData.getLong("sales_quantity_head_office"));
                        }
                    }

                    //拉取上上月数据
                    String precedingMonth = DateUtils.getYearAndMonth(nowMonth + "", "yyyyMM", -2);
                    if (months.contains(precedingMonth)) {
                        data.put("last_preceding_month_actual_sales_quantity", actualSalesData.get(precedingMonth));
                        data.put("last_preceding_month_actual_sales_quantity_head_office", actualSalesHeadOfficeData.get(precedingMonth));
                    } else {
                        queryUtil.setSqlSelect("select sum(actual_sales_quantity) actual_sales_quantity,sum(actual_sales_quantity_head_office) actual_sales_quantity_head_office");
                        queryUtil.setSqlExceptSelect("from dealer_inventory_data_history history");
                        queryUtil.setExecuteSql(null);
                        queryUtil.setParamValue(new LinkedList<>());
                        String lastPrecedingSql = queryUtil.getExecuteSql();
                        lastPrecedingSql = lastPrecedingSql.replaceFirst("group by", "and product_id=? group by");
                        List<Object> params = new ArrayList<>();
                        Object[] paramArr = queryUtil.getParamValue();
                        params.addAll(Arrays.asList(paramArr));
                        params.set(paramArr.length-2, precedingMonth);
                        params.set(paramArr.length-1, precedingMonth);
                        params.add(nowProductId);
                        SecondaryDealerInventoryData secondaryDealerInventoryData = SecondaryDealerInventoryData.dao.findFirst(lastPrecedingSql, params.toArray(new Object[params.size()]));
                        if (secondaryDealerInventoryData != null) {
                            data.put("last_preceding_month_actual_sales_quantity", secondaryDealerInventoryData.getLong("actual_sales_quantity"));
                            data.put("last_preceding_month_actual_sales_quantity_head_office", secondaryDealerInventoryData.getLong("sales_quantity_head_office"));
                        }
                    }

                    value.put(nowMonth, caculateLastInventoryDay(data));
                });
            });
        }

        //将总计数据和查询数据合并
        Map<String, List<DealerInventoryDataHistory>> exportMap = new HashMap<>();
        Set<String> originalColumnSet = new HashSet<>(Arrays.asList(columnArr));
        dealerMap.forEach((key,value) -> {
            String productName = value.getProductName();
            List<DealerInventoryDataHistory> historys = exportMap.get(productName);
            if (historys == null) {
                historys = new ArrayList<>();
                exportMap.put(productName, historys);
            }

            historys.add(value);
        });
        countMap.forEach((key,value) -> {
            String productName = value.getProductName();
            List<DealerInventoryDataHistory> counts = exportMap.get(productName);
            if (counts == null) {
                counts = new ArrayList<>();
                exportMap.put(productName, counts);
            }

            String column = value.get("score_type");
            if (originalColumnSet.contains(column)) {
                counts.add(value);
            }
        });

        Set<String> productNames = exportMap.keySet();
        productNames.forEach(productName -> {
            //根据经销商ID降序排序
            Collections.sort(exportMap.get(productName), (a , b) -> {
                Long aId = a.getDealerId();
                Long bId = b.getDealerId();
                int score = bId.compareTo(aId);
                //先行比较 dealer_id
                if (score != 0) {
                    return score;
                }

                //继续比较 product_id
                Long aProductId = a.getProductId();
                Long bProductId = b.getProductId();
                int proScore = bProductId.compareTo(aProductId);
                //先行比较 dealer_id
                if (proScore != 0) {
                    return proScore;
                }

                //继续比较字段优先级
                int x = COLUMN_TYPE.get(a.get("score_type")).getScore();
                int y = COLUMN_TYPE.get(b.get("score_type")).getScore();
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            });
        });

        //生成文件目录
        String fileName = "进销存历史数据" + System.currentTimeMillis() + ".xls";
        String monthStr = StringUtils.join(months, ",");
        Map<String, String> mergeMap = new ImmutableMap.Builder<String, String>()
                .put("dealer_name", "dealer_id")
                .put("dealer_level", "dealer_id")
                .put("business_manager_user_name", "dealer_id")
                .put("area_name", "dealer_id")
                .put("region_name", "dealer_id")
                .put("product_name", "dealer_id")
                .build();
        try {
            PoiUtils.exportMergeData(getResponse(), DealerInventoryDataHistory.class, exportMap, mergeMap, fileName, "商业公司名称,经销商级别,大区,省份,商务负责人,产品名称,类型," + monthStr, "dealer_name,dealer_level,area_name,region_name,business_manager_user_name,product_name,type," + monthStr, new String[]{"type"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

    //获取大区列表
    public void getAreaList() {
        List list = roleConfigService.getAllAreaListByUser(getLoginUser().getId());
        result.setData(list);
        renderJson(result);
    }

    //获取省份列表
    public void getRegionList() {
        String areaName = getPara("area_ids");
        if (StringUtils.isEmpty(areaName)) {
            throw new ExceptionForJson("请先选择大区");
        }
        List list = roleConfigService.getAllRegionList(Arrays.asList(areaName.split(",")), getLoginUser().getId());

        result.setData(list);
        renderJson(result);
    }

    //获取商务经理列表
    public void getBusinessManagerList() {
    	String areaName = getPara("area_ids");
    	String regionIds = getPara("region_ids");
    	String dealerIds = getPara("dealer_ids");
        List<User> list = roleConfigService.getAllBusinessManager(ToolFunction.str2List(areaName),ToolFunction.str2List(regionIds),ToolFunction.str2List(dealerIds), getLoginUser().getId());
        list.forEach(k -> {
            k.put("selected", "");
            k.put("disabled", "");
        });
        result.setData(list);
        renderJson(result);
    }

    //获取经销商列表
    public void getDealerList() {
    	String areaName = getPara("area_ids");
    	String levels = getPara("levels");
    	String regionIds = getPara("region_ids");
    	String businessIds = getPara("business_ids");
        List<Dealer> list = roleConfigService.getAllDealerList(ToolFunction.str2List(areaName), ToolFunction.str2List(regionIds),ToolFunction.str2List(businessIds),getLoginUser().getId(), levels);
        list.forEach(k -> {
            k.put("selected", "");
            k.put("disabled", "");
        });
        result.setData(list);
        renderJson(result);
    }

    //获取商品列表
    public void getProductList() {
        List<Product> productList = productService.getAllProductList(getLoginUser());
        result.setData(productList);
        renderJson(result);
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
        List<Long> saleList = new ArrayList<>();
        if (lastActSaleQuan != null) {
            saleList.add(lastActSaleQuan);
        }
        if (precedingActSalQuan != null) {
            saleList.add(precedingActSalQuan);
        }
        if (upActualSalesQuanity != null) {
            saleList.add(upActualSalesQuanity);
        }
        BigDecimal lastAverage = srv.caculateAverage(saleList);
        if (BigDecimalUtil.gt(lastAverage, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(lastActStoQuan * 90).divide(lastAverage, 4, BigDecimal.ROUND_DOWN).setScale(2, BigDecimal.ROUND_DOWN);
            return inventoryDay;
        } else {
            return new BigDecimal(0);
        }
    }

}
