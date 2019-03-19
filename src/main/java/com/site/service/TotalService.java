package com.site.service;

import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.User;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TotalService {
    public static TotalService me = new TotalService();


    public BigDecimal getTotalSyKcts(User loginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int l_month = DateUtils.getYearAndMonth(openTimeStep, -1);
        int ll_month = DateUtils.getYearAndMonth(openTimeStep, -2);
        int lll_month = DateUtils.getYearAndMonth(openTimeStep, -3);

        /**
         * 公式除数部分的数组
         */
        List<BigDecimal> totalCount = new ArrayList<>();
        //上月库存天数 ==上月实际库存*90/((上上上月实际销售+上上月实际销售+上月实际销售))
        QueryUtil queryUtil1 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal stockNum = TotalService.me.sumStockNum(queryUtil1, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, l_month);

        QueryUtil queryUtil4 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal l_saleNum = TotalService.me.sumSaleNum(queryUtil4, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, l_month);
        totalCount.add(l_saleNum);

        QueryUtil queryUtil3 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal ll_saleNum = TotalService.me.sumSaleNum(queryUtil3, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, ll_month);
        totalCount.add(ll_saleNum);

        QueryUtil queryUtil2 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal lll_saleNum = TotalService.me.sumSaleNum(queryUtil2, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, lll_month);
        totalCount.add(lll_saleNum);

        BigDecimal a = stockNum.multiply(new BigDecimal(90));
        BigDecimal add = CalculateService.me.genThreeMonthTotal(totalCount);
        if (add.compareTo(BigDecimal.ZERO) != 0) {
            return a.divide(add, 1, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalByKcts(User loginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        Integer thisMonth = openTimeStep.getYearAndMonth();
        int l_month = DateUtils.getYearAndMonth(openTimeStep, -1);
        int ll_month = DateUtils.getYearAndMonth(openTimeStep, -2);


        List<BigDecimal> totalCount = new ArrayList<>();
        //本月库存天数 = (本月库存*90）/(上上月实际销售+上月实际销售+本月销售预估)
        QueryUtil queryUtil1 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal stockNumThisMonth = TotalService.me.sumStockNum(queryUtil1, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, thisMonth);

        //本月销售预估
        QueryUtil queryUtil2 = new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef();
        BigDecimal planSaleNum = TotalService.me.planSaleNum(queryUtil2, loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, thisMonth);
        totalCount.add(planSaleNum);

        //上月实际销售
        BigDecimal l_saleNum = TotalService.me.sumSaleNum(new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef(), loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, l_month);
        totalCount.add(l_saleNum);

        //上上月实际销售
        BigDecimal ll_saleNum = TotalService.me.sumSaleNum(new QueryUtil(FirstDealerInventoryData.class).setQueryUtilDef(), loginUser, productId, businessId, dealerId, areaId, regionId, isSelf, ll_month);
        totalCount.add(ll_saleNum);

        BigDecimal s = stockNumThisMonth.multiply(new BigDecimal(90));
        BigDecimal d = CalculateService.me.genThreeMonthTotal(totalCount);

        if (!d.equals(BigDecimal.ZERO)) {
            return s.divide(d, 1, BigDecimal.ROUND_HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    //获取库存
    public BigDecimal sumStockNum(QueryUtil<FirstDealerInventoryData> queryUtil, User LoginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf, int month) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        if (openTimeStep.getYearAndMonth().equals(month)) {
            queryUtil.setSqlSelect("SELECT SUM(ref_stock_quantity) AS total");
        } else {
            queryUtil.setSqlSelect("SELECT SUM(actual_stock_quantity) AS total");
        }
        queryUtil.setSqlExceptSelect("FROM first_dealer_inventory_data");

        return genQuery(queryUtil, LoginUser, productId, businessId, dealerId, areaId, regionId, isSelf, month);
    }

    //获取销售
    public BigDecimal sumSaleNum(QueryUtil<FirstDealerInventoryData> queryUtil, User LoginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf, int month) {
        queryUtil.setSqlSelect("SELECT SUM(CASE  WHEN actual_sales_quantity_head_office IS NULL THEN actual_sales_quantity ELSE actual_sales_quantity_head_office END) as total");
        queryUtil.setSqlExceptSelect("FROM first_dealer_inventory_data");
        return genQuery(queryUtil, LoginUser, productId, businessId, dealerId, areaId, regionId, isSelf, month);
    }

    //获取本月预估销售
    public BigDecimal planSaleNum(QueryUtil<FirstDealerInventoryData> queryUtil, User LoginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf, int month) {
        queryUtil.setSqlSelect("SELECT SUM(plan_sales_quantity) AS total");
        queryUtil.setSqlExceptSelect("FROM first_dealer_inventory_data");
        return genQuery(queryUtil, LoginUser, productId, businessId, dealerId, areaId, regionId, isSelf, month);
    }

    private BigDecimal genQuery(QueryUtil<FirstDealerInventoryData> queryUtil, User LoginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf, int month) {
        queryUtil.addQueryParam("month", "=", month);
        if ("大区经理".equalsIgnoreCase(LoginUser.getRoleName())) {
            //是否获取自己能看的数据，还是需要顶替填写的数据
            if (isSelf) {
                //这里获取大区经理下面的所有商务经理
                List<User> businessManager = RoleConfigService.me.getBusinessManager(LoginUser.getId());
                List ids = new ArrayList();
                businessManager.forEach(k -> {
                    ids.add(k.getId());
                });
                queryUtil.addQueryParam("business_manager_user_id", "in", StringUtils.join(ids, ","));
            }
        }
        if ("商务经理".equalsIgnoreCase(LoginUser.getRoleName())) {
            queryUtil.addQueryParam("business_manager_user_id", "=", LoginUser.getId());
        }

        if (StringUtils.isNotEmpty(productId)) {
            queryUtil.addQueryParam("product_id", "in", productId);
        }

        if (StringUtils.isNotEmpty(areaId)) {
            queryUtil.addQueryParam("area_id", "in", areaId);
        }

        if (StringUtils.isNotEmpty(regionId)) {
            queryUtil.addQueryParam("region_id", "in", regionId);
        }
        if (StringUtils.isNotEmpty(dealerId)) {
            queryUtil.addQueryParam("dealer_id", "in", dealerId);
        }
        if (StringUtils.isNotEmpty(businessId)) {
            queryUtil.addQueryParam("business_manager_user_id", "in", businessId);
        }

        List<FirstDealerInventoryData> data = queryUtil.getSearchResult().getData();
        if (data == null || data.isEmpty()) {
            return BigDecimal.ZERO;
        }
        FirstDealerInventoryData o = data.get(0);
        Object total_stock = o.get("total");
        return new BigDecimal(total_stock == null ? "0" : total_stock.toString());
    }


}
