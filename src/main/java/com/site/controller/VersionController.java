package com.site.controller;

import com.google.common.collect.ImmutableMap;
import com.jfinal.log.Log;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.DealerInventoryDataVersion;
import com.site.core.model.DealerInventoryDataVersionList;
import com.site.core.model.User;
import com.site.definition.Constants;
import com.site.utils.DateUtils;
import com.site.utils.PoiUtils;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 版本数据
 */
public class VersionController extends BaseController {

    private static final Log log = Log.getLog(VersionController.class);

    @Override
    public void index() {
        User user = getLoginUser();
        String roleName = user.getRoleName();
        setAttr("roleName", roleName);
        String date = DateUtils.getDate(new Date(), -1) + " - " + DateUtils.getDate(new Date(), 0);
        setAttr("date", date);
        render("list.html");
    }

    /**
     * 列表
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(DealerInventoryDataVersionList.class);
        queryUtil.setSqlSelect("SELECT *");
        queryUtil.setSqlExceptSelect("FROM dealer_inventory_data_version_list a ");
        queryUtil.setSort("create_time");
        queryUtil.setOrder("DESC");

        String date = getPara("date");
        if (StringUtils.isNotBlank(date)) {
            String[] monthArr = date.split(" - ");
            if (monthArr.length >= 1 && StringUtils.isNotBlank(monthArr[0])) {
                queryUtil.addQueryParam("create_time", ">=", monthArr[0].replaceAll("-", ""));
            }
            if (monthArr.length >= 2 && StringUtils.isNotBlank(monthArr[1])) {
                queryUtil.addQueryParam("create_time", "<", DateUtils.getDate(monthArr[1], "yyyy-MM-dd", 1));
            }
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<DealerInventoryDataVersionList> versions = searchResult.getData();
        if (versions != null) {
            versions.forEach(version -> version.put("status_name", Constants.VersionState.getNameByValue(version.getStatus())));
        }
        renderJson(searchResult);
    }

    /**
     * 导出版本数据
     *
     * @return
     */
    public void export() {
        String version = getPara("version");

        if (StringUtils.isBlank(version)) {
            throw new ExceptionForJson("请选择版本");
        }

        QueryUtil queryUtil = getQueryUtil(DealerInventoryDataVersion.class);
        queryUtil.setSqlSelect("select month,area_name,region_name,area_manager_user_id,area_manager_user_name," +
                "business_manager_user_id,business_manager_user_name,dealer_id,dealer_name,concat('T',dealer_level) dealer_level,upstream_name,product_name," +
                "preceding_month_actual_stock_quantity,last_month_plan_purchase_quantity,last_month_actual_purchase_quantity,last_month_plan_sales_quantity,last_month_actual_sales_quantity,last_month_actual_sales_quantity_head_office,last_month_theory_stock_quantity,last_month_actual_stock_quantity,last_month_purchase_quantity,last_month_inventory_day," +
                "plan_purchase_quantity,actual_purchase_quantity,plan_sales_quantity,actual_sales_quantity,theory_stock_quantity,actual_stock_quantity,purchase_quantity,inventory_day,diff_cause,pre_six_month_average_sales");
        queryUtil.setSqlExceptSelect("from dealer_inventory_data_version");
        queryUtil.setSort("dealer_id");
        queryUtil.setOrder("desc");
        queryUtil.setPageSize(Integer.MAX_VALUE);
        queryUtil.addQueryParam("version", "=", version);

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<DealerInventoryDataVersion> list = searchResult.getData();
        Map<String, List<DealerInventoryDataVersion>> exportMap = new HashMap<>();
        List<DealerInventoryDataVersion> oneLevelList = new ArrayList();
        List<DealerInventoryDataVersion> twoLevelList = new ArrayList();
        exportMap.put("一级商", oneLevelList);
        exportMap.put("二级商", twoLevelList);
        if (list != null) {
            list.forEach(versionData -> {
                String level = versionData.getDealerLevel();
                if ("T1".equals(level)) {
                    oneLevelList.add(versionData);
                }
                if ("T2".equals(level)) {
                    twoLevelList.add(versionData);
                }
            });
        }

        //生成文件目录
        String fileName = "进销存版本数据" + System.currentTimeMillis() + ".xls";
        Map<String, String> mergeMap = new ImmutableMap.Builder<String, String>()
                .put("month", "dealer_id")
                .put("area_name", "dealer_id")
                .put("region_name", "dealer_id")
                .put("area_manager_user_name", "dealer_id")
                .put("business_manager_user_name", "dealer_id")
                .put("dealer_name", "dealer_id")
                .put("dealer_level", "dealer_id")
                .put("upstream_name", "dealer_id")
                .build();
        Map<String, String> colNameMap = new ImmutableMap.Builder<String, String>()
                .put("一级商", "时间,大区,省份,大区经理名称,商务经理名称,经销商等级,经销商名称,产品名称,上上月实际库存数,上月预估进货,上月实际进货,上月销售预估,上月实际销售,总部上传上月实际销售,上月理论库存,上月实际库存,上月系统分配采购量,上月库存天数,当月预估进货,当月实际进货,当月销售预估,当月实际销售,当月理论库存,当月实际库存,当月系统分配采购量,当月库存天数,库存差异原因,近6月月均销售")
                .put("二级商", "时间,大区,省份,大区经理名称,商务经理名称,上游商业,经销商等级,经销商名称,产品名称,上上月实际库存数,上月预估进货,上月实际进货,上月销售预估,上月实际销售,总部上传上月实际销售,上月理论库存,上月实际库存,上月库存天数,当月预估进货,当月实际进货,当月销售预估,当月实际销售,当月理论库存,当月实际库存,当月库存天数,库存差异原因,近6月月均销售")
                .build();
        Map<String, String> colModeMap = new ImmutableMap.Builder<String, String>()
                .put("一级商", "month,area_name,region_name,area_manager_user_name,business_manager_user_name,dealer_level,dealer_name,product_name,preceding_month_actual_stock_quantity,last_month_plan_purchase_quantity,last_month_actual_purchase_quantity,last_month_plan_sales_quantity,last_month_actual_sales_quantity,last_month_actual_sales_quantity_head_office,last_month_theory_stock_quantity,last_month_actual_stock_quantity,last_month_purchase_quantity,last_month_inventory_day,plan_purchase_quantity,actual_purchase_quantity,plan_sales_quantity,actual_sales_quantity,theory_stock_quantity,actual_stock_quantity,purchase_quantity,inventory_day,diff_cause,pre_six_month_average_sales")
                .put("二级商", "month,area_name,region_name,area_manager_user_name,business_manager_user_name,upstream_name,dealer_level,dealer_name,product_name,preceding_month_actual_stock_quantity,last_month_plan_purchase_quantity,last_month_actual_purchase_quantity,last_month_plan_sales_quantity,last_month_actual_sales_quantity,last_month_actual_sales_quantity_head_office,last_month_theory_stock_quantity,last_month_actual_stock_quantity,last_month_inventory_day,plan_purchase_quantity,actual_purchase_quantity,plan_sales_quantity,actual_sales_quantity,theory_stock_quantity,actual_stock_quantity,inventory_day,diff_cause,pre_six_month_average_sales")
                .build();
        try {
            PoiUtils.exportMergeData(getResponse(), DealerInventoryDataVersion.class, exportMap, mergeMap, fileName, colNameMap, colModeMap, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

}