package com.site.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseService;
import com.site.base.Result;
import com.site.core.model.*;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;
import com.site.utils.ExcelUtil;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.util.*;

public class DealerOneService extends BaseService {
    public static DealerOneService me = new DealerOneService();
    private static final Log log = Log.getLog(DealerOneService.class);
    private RoleConfigService roleConfigService = new RoleConfigService();

    public QueryUtil.SearchResult getList(QueryUtil queryUtil, User LoginUser, String productId, String areaId, String regionId, String dealerId, String businessId, String sort, String order, Boolean isSelf) {
        OpenTimeStep openTimeStep = roleConfigService.getCurrentOpenTimeStep(1);
        String yearAndMonth = DateUtils.getYearAndMonth(openTimeStep.getYearAndMonth() + "", "yyyyMM", 0) + "";
        String yearAndMonthAndDate = DateUtils.getYearAndMonth(openTimeStep.getYearAndMonth() + "", "yyyyMM", 0) + "01";
        queryUtil.setSqlSelect("SELECT b.id AS id, c.`name` AS area_name, h.id AS area_manager_id, h.`name` AS area_manager_name, g.id AS business_manager_id, g.`name` AS business_manager_name, d.`name` AS regin_name, a.`name` AS dealer_name, a.id AS dealer_id, p.product_id AS product_id, p.product_name AS product_name, f.actual_stock_quantity AS ssy_sj_kc, e.plan_purchase_quantity AS sy_yg_jhl, e.actual_purchase_quantity AS sy_jhl, e.plan_sales_quantity AS sy_yg_xsl, e.actual_sales_quantity AS sy_sj_xsl, e.actual_sales_quantity_head_office AS sy_sj_xsl_zb, e.theory_stock_quantity AS sy_ll_kc,CASE WHEN IFNULL( e.original_theory_stock_quantity, 0 ) = IFNULL(e.theory_stock_quantity, 0) THEN '0' ELSE '3' END AS flag_status, e.actual_stock_quantity AS sy_sj_kc, e.inventory_day AS sy_kcts, cast( b.purchase_quantity AS DECIMAL (9, 0)) AS by_xtfp_cgl, CAST( i.purchase_quantity AS DECIMAL (9, 0)) AS xy_xtfp_cgl, b.actual_purchase_quantity AS by_jhl, b.plan_purchase_quantity AS by_yg_jhl, b.plan_sales_quantity AS by_yg_xsl, b.ref_stock_quantity AS by_kc, b.ref_inventory_day AS by_kcts, e.diff_cause AS cyyy, b.order_num AS order_num, b.diff_order_num AS diff_order_num, e.pre_six_month_average_sales AS qly_xs_pjz");
        queryUtil.setSqlExceptSelect("FROM ( SELECT pro.id AS product_id, pro. NAME AS product_name, dea.id AS dealer_id, dea.area_id AS area_id, dea.region_id AS region_id, dea.business_manager_user_id AS business_manager_user_id, dea.area_manager_user_id AS area_manager_user_id FROM dealer_product dp, product pro, dealer dea WHERE dp.product_id = pro.id AND dp.dealer_id = dea.id AND dea.`level` = 1  AND dea.is_delete = '0'  AND pro.is_delete ='0' ORDER BY dea.id DESC ) p LEFT JOIN first_dealer_inventory_data b ON ( p.product_id = b.product_id AND p.dealer_id = b.dealer_id AND b.`month` = '" + yearAndMonth + "' ) LEFT JOIN dealer a ON a.id = p.dealer_id LEFT JOIN area c ON (a.area_id = c.id) LEFT JOIN region d ON (a.region_id = d.id) LEFT JOIN first_dealer_inventory_data e ON ( CAST( DATE_FORMAT( DATE_SUB('" + yearAndMonthAndDate + "', INTERVAL 1 MONTH), '%Y%m' ) AS SIGNED ) = e.`month` AND e.dealer_id = p.dealer_id AND e.product_id = p.product_id ) LEFT JOIN first_dealer_inventory_data f ON ( CAST( DATE_FORMAT( DATE_SUB('" + yearAndMonthAndDate + "', INTERVAL 2 MONTH), '%Y%m' ) AS SIGNED ) = f.`month` AND f.dealer_id = p.dealer_id AND f.product_id = p.product_id ) LEFT JOIN `user` g ON g.id = p.business_manager_user_id LEFT JOIN `user` h ON h.id = p.area_manager_user_id LEFT JOIN first_dealer_inventory_data i ON ( CAST( DATE_FORMAT( DATE_ADD( '" + yearAndMonthAndDate + "', INTERVAL 1 MONTH ), '%Y%m' ) AS SIGNED ) = i.`month` AND i.dealer_id = p.dealer_id AND i.product_id = p.product_id )");
        queryUtil.setPageSize(Integer.MAX_VALUE);
        queryUtil.addQueryParam("p.product_id", "in", productId, "where");

        if ("大区经理".equalsIgnoreCase(LoginUser.getRoleName())) {
            //是否获取自己能看的数据，还是需要顶替填写的数据
            if (isSelf) {
                //这里获取大区经理下面的所有商务经理
                List<User> businessManager = RoleConfigService.me.getBusinessManager(LoginUser.getId());
                List ids = new ArrayList();
                businessManager.forEach(k -> {
                    ids.add(k.getId());
                });
                queryUtil.addQueryParam("p.business_manager_user_id", "in", StringUtils.join(ids, ","));
            } else {
                List<Long> vacancyBMUserId = UserService.me.getVacancyBMUserId(LoginUser.getId(), true);
                List ids = new ArrayList();
                vacancyBMUserId.forEach(k -> {
                    ids.add(k);
                });
                queryUtil.addQueryParam("p.business_manager_user_id", "in", StringUtils.join(ids, ","));
            }
        }
        if ("商务经理".equalsIgnoreCase(LoginUser.getRoleName())) {
            queryUtil.addQueryParam("p.business_manager_user_id", "=", LoginUser.getId());
        }

        if (StringUtils.isNotEmpty(areaId)) {
            queryUtil.addQueryParam("c.id", "in", areaId);
        }
        if (StringUtils.isNotEmpty(regionId)) {
            queryUtil.addQueryParam("d.id", "in", regionId);
        }
        if (StringUtils.isNotEmpty(dealerId)) {
            queryUtil.addQueryParam("p.dealer_id", "in", dealerId);
        }
        if (StringUtils.isNotEmpty(businessId)) {
            queryUtil.addQueryParam("p.business_manager_user_id", "in", businessId);
        }

        return queryUtil.getSearchResult();
    }

    /**
     * 插入商务经理未填写数据
     *
     * @param
     */
    public void insertBmUnfilledData(int month, Long userId, List<Long> bmUserIds) {
        String bmUserIdStr = StringUtils.join(bmUserIds, ",");

        StringBuilder sb = new StringBuilder("insert into first_dealer_inventory_data(area_id,region_id,area_manager_user_id,business_manager_user_id,dealer_id,month,product_id,status,create_time,create_user_id)" +
                " select d.area_id,d.region_id,d.area_manager_user_id,d.business_manager_user_id,d.id dealer_id, ? as  month,dp.product_id,'1' as  status,CURRENT_TIMESTAMP create_time, ? create_user_id" +
                " from dealer d " +
                " left join dealer_product dp on dp.dealer_id=d.id " +
                " left join first_dealer_inventory_data secondary on dp.dealer_id=secondary.dealer_id and secondary.month=? and secondary.status='0' and dp.product_id=secondary.product_id" +
                " where d.is_delete!='1' and d.level=1 and d.id in (?) and secondary.id is null");
        Db.update(sb.toString(), month, userId, month, bmUserIdStr);
    }

    //汇总所有数据
    public void genTotal(List<FirstDealerInventoryData> list, User loginUser, String productId, String businessId, String dealerId, String areaId, String regionId, Boolean isSelf) {
        BigDecimal ssy_sj_kc_t = BigDecimal.ZERO;
        BigDecimal sy_yg_jhl_t = BigDecimal.ZERO;
        BigDecimal sy_jhl_t = BigDecimal.ZERO;
        BigDecimal sy_yg_xsl_t = BigDecimal.ZERO;
        BigDecimal sy_sj_xsl_t = BigDecimal.ZERO;
        BigDecimal sy_sj_xsl_t_zb = BigDecimal.ZERO;
        BigDecimal sy_ll_kc_t = BigDecimal.ZERO;
        BigDecimal sy_sj_kc_t = BigDecimal.ZERO;
        BigDecimal by_xtfp_cgl_t = BigDecimal.ZERO;
        BigDecimal xy_xtfp_cgl_t = BigDecimal.ZERO;
        BigDecimal by_jhl_t = BigDecimal.ZERO;
        BigDecimal by_yg_jhl_t = BigDecimal.ZERO;
        BigDecimal by_yg_xsl_t = BigDecimal.ZERO;
        BigDecimal by_kc_t = BigDecimal.ZERO;
        BigDecimal by_xds_t = BigDecimal.ZERO;
        BigDecimal by_xds_cy_t = BigDecimal.ZERO;


        if (!list.isEmpty()) {
            FirstDealerInventoryData total = new FirstDealerInventoryData();
            list.add(new FirstDealerInventoryData());
            for (int i = 0; i < list.size(); i++) {
                FirstDealerInventoryData k = list.get(i);
                Object ssy_sj_kc = k.get("ssy_sj_kc");
                ssy_sj_kc_t = ssy_sj_kc_t.add(ssy_sj_kc == null ? BigDecimal.ZERO : new BigDecimal(ssy_sj_kc.toString()));

                Object sy_yg_jhl = k.get("sy_yg_jhl");
                sy_yg_jhl_t = sy_yg_jhl_t.add(sy_yg_jhl == null ? BigDecimal.ZERO : new BigDecimal(sy_yg_jhl.toString()));

                Object sy_jhl = k.get("sy_jhl");
                sy_jhl_t = sy_jhl_t.add(sy_jhl == null ? BigDecimal.ZERO : new BigDecimal(sy_jhl.toString()));

                Object sy_yg_xsl = k.get("sy_yg_xsl");
                sy_yg_xsl_t = sy_yg_xsl_t.add(sy_yg_xsl == null ? BigDecimal.ZERO : new BigDecimal(sy_yg_xsl.toString()));

                Object sy_sj_xsl = k.get("sy_sj_xsl");
                sy_sj_xsl_t = sy_sj_xsl_t.add(sy_sj_xsl == null ? BigDecimal.ZERO : new BigDecimal(sy_sj_xsl.toString()));

                Object sy_sj_xsl_zb = k.get("sy_sj_xsl_zb");
                sy_sj_xsl_t_zb = sy_sj_xsl_t_zb.add(sy_sj_xsl_zb == null ? BigDecimal.ZERO : new BigDecimal(sy_sj_xsl_zb.toString()));

                Object sy_ll_kc = k.get("sy_ll_kc");
                sy_ll_kc_t = sy_ll_kc_t.add(sy_ll_kc == null ? BigDecimal.ZERO : new BigDecimal(sy_ll_kc.toString()));

                Object sy_sj_kc = k.get("sy_sj_kc");
                sy_sj_kc_t = sy_sj_kc_t.add(sy_sj_kc == null ? BigDecimal.ZERO : new BigDecimal(sy_sj_kc.toString()));

                Object by_xtfp_cgl = k.get("by_xtfp_cgl");
                by_xtfp_cgl_t = by_xtfp_cgl_t.add(by_xtfp_cgl == null ? BigDecimal.ZERO : new BigDecimal(by_xtfp_cgl.toString()));

                Object by_jhl = k.get("by_jhl");
                by_jhl_t = by_jhl_t.add(by_jhl == null ? BigDecimal.ZERO : new BigDecimal(by_jhl.toString()));

                Object by_yg_jhl = k.get("by_yg_jhl");
                by_yg_jhl_t = by_yg_jhl_t.add(by_yg_jhl == null ? BigDecimal.ZERO : new BigDecimal(by_yg_jhl.toString()));

                Object by_yg_xsl = k.get("by_yg_xsl");
                by_yg_xsl_t = by_yg_xsl_t.add(by_yg_xsl == null ? BigDecimal.ZERO : new BigDecimal(by_yg_xsl.toString()));

                Object by_kc = k.get("by_kc");
                by_kc_t = by_kc_t.add(by_kc == null ? BigDecimal.ZERO : new BigDecimal(by_kc.toString()));

                Object by_xds = k.get("order_num");
                by_xds_t = by_xds_t.add(by_xds == null ? BigDecimal.ZERO : new BigDecimal(by_xds.toString()));

                Object by_xds_cy = k.get("diff_order_num");
                by_xds_cy_t = by_xds_cy_t.add(by_xds_cy == null ? BigDecimal.ZERO : new BigDecimal(by_xds_cy.toString()));

                Object xy_xtfp_cgl = k.get("xy_xtfp_cgl");
                xy_xtfp_cgl_t = xy_xtfp_cgl_t.add(xy_xtfp_cgl == null ? BigDecimal.ZERO : new BigDecimal(xy_xtfp_cgl.toString()));
            }

            BigDecimal totalSyKcts = TotalService.me.getTotalSyKcts(loginUser, productId, businessId, dealerId, areaId, regionId, isSelf);
            BigDecimal totalByKcts = TotalService.me.getTotalByKcts(loginUser, productId, businessId, dealerId, areaId, regionId, isSelf);
            total.put("id", "");
            total.put("dealer_name", "总计数据");
            total.put("ssy_sj_kc", ssy_sj_kc_t);
            total.put("sy_yg_jhl", sy_yg_jhl_t);
            total.put("sy_jhl", sy_jhl_t);
            total.put("sy_yg_xsl", sy_yg_xsl_t);
            total.put("sy_sj_xsl", sy_sj_xsl_t);
            total.put("sy_sj_xsl_zb", sy_sj_xsl_t_zb);
            total.put("sy_ll_kc", sy_ll_kc_t);

            total.put("sy_sj_kc", sy_sj_kc_t);
            total.put("sy_kcts", totalSyKcts);
            total.put("by_xtfp_cgl", by_xtfp_cgl_t);
            total.put("xy_xtfp_cgl", new BigDecimal(xy_xtfp_cgl_t.toString()).intValue());
            total.put("by_jhl", by_jhl_t);

            total.put("by_yg_jhl", by_yg_jhl_t);
            total.put("by_yg_xsl", by_yg_xsl_t);
            total.put("by_kc", by_kc_t);
            total.put("by_kcts", totalByKcts);

            total.put("order_num", by_xds_t);
            total.put("diff_order_num", by_xds_cy_t);
            list.add(total);
        }
    }

    /**
     * 计算库存天数8
     * 计算出上月理论库存6
     *
     * @param file
     * @param loginUser
     * @return
     */
    public Map calculateStockDayAndStockNum(UploadFile file, User loginUser) {
        HashMap resultMap = new HashMap();
        resultMap.put("err", new ArrayList<>());
        resultMap.put("success", "");

        JSONObject excelResult = null;
        try {
            //总部导入上月实际进（3），销（5），以及当月进（10）
            Map map = new HashMap();
            //选择
            //业务类型	销售类型	发票号	开票日期	客户简称  6
            //客户名称
            //币种	汇率	销售部门	业务员	合同编码	表体订单号	仓库	存货编码	存货代码  存货名称 规格型号	批号  失效日期	有效期至  14
            //数量
            //报价	含税单价	无税单价	无税金额	税率（%）	税额	价税合计	扣率（%）	扣率2（%）	折扣额	制单人	复核人	账期	到期日	发运方式编码	发运方式
            //项目名称
            //收款日期	未回款金额	未回款金额本币	导出次数	复核日期	复核时间	客户权限大类编码	客户权限大类名称
            //客户编号
            map.put("上传T1上月进货实际", new String[]{
                            "select",
                            "", "", "", "", "",   //5
                            "dealer_name",
                            "", "", "", "", "", "", "", "", "", "", "", "", "", "",      //14
                            "num",
                            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",         //16
                            "product_project_name",
                            "", "", "", "", "", "", "", "",
                            "order_calculate_code"
                    }
            );
            //年月	日期	卖出省份	卖出城市	经销商名称   //5
            // 经销商编码
            // 经销商级别	机构名称	机构原始名称	机构编码	性质	省份	城市	产品编码  8
            // 产品名称
            // 商务负责人            //1
            // 销量
            map.put("上传T1,T2上月销售实际", new String[]{"", "", "", "", "",   //5
                    "dealer_code",
                    "", "", "", "", "", "", "", "",    //8
                    "product_name",
                    "",
                    "num"
            });
            excelResult = ExcelUtil.readMultipleExcel(file, map);
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put("worn_msg", "读取数据失败");
            return resultMap;
        }

        JSONArray lastMonthJH = (JSONArray) excelResult.get("上传T1上月进货实际");

        JSONArray lastMonthXS = (JSONArray) excelResult.get("上传T1,T2上月销售实际");

        //校验数据
        Map<String, Integer> calculateIds = new HashMap<>();
        //上月进货实际T1
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int lastMonth = DateUtils.getYearAndMonth(openTimeStep, -1);
        for (int i = 0; i < lastMonthJH.size(); i++) {
            JSONArray finalJsonArray = lastMonthJH;
            int finalI = i;
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    JSONObject obj = finalJsonArray.getJSONObject(finalI);
                    Object select = obj.get("select");
                    if (select != null && select.toString().length() > 0 && ("小计".equalsIgnoreCase(select.toString()) || "合计".equalsIgnoreCase(select.toString()))) {
                        return false;
                    }
                    String productProjectName = (String) obj.get("product_project_name");
                    Product product = Product.dao.findFirst("select * from product where  project_name =?", productProjectName);
                    if (product == null) {
                        return false;
                    }
                    String orderCalculateCode = (String) obj.get("order_calculate_code");
                    Dealer dealer = Dealer.dao.findFirst("select * from dealer where order_calculate_code = ?", orderCalculateCode);
                    if (dealer == null) {
                        return false;
                    }

                    String sySjJhlStr = (String) obj.get("num");
                    BigDecimal sySjJhl = BigDecimal.ZERO;

                    if (StringUtils.isNotEmpty(sySjJhlStr)) {
                        sySjJhl = new BigDecimal(sySjJhlStr).setScale(0, BigDecimal.ROUND_HALF_UP);
                    }
                    FirstDealerInventoryData lastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where product_id=? and dealer_id =? and month = ?", product.getId(), dealer.getId(), lastMonth);
                    lastMonthData = lastMonthData == null ? FirstDealerInventoryData.init(dealer, loginUser) : lastMonthData;
                    //第一次覆盖，后续还有这条就要开始加总
                    if (lastMonthData.getId() == null || !calculateIds.containsKey("1_" + lastMonthData.getId() + "_jh")) {
                        //这就说明是第一次来,直接覆盖
                        lastMonthData.setActualPurchaseQuantity(sySjJhl.longValue());
                    } else {
                        //这就说明不是第一次来，要加总之前的
                        Long old = lastMonthData.getActualPurchaseQuantity();
                        lastMonthData.setActualPurchaseQuantity(sySjJhl.longValue() + old);
                    }
                    if (lastMonthData.getId() == null) {
                        //这里要新增，因为总部导入了，那么就代表这数据肯定有
                        lastMonthData.setMonth(lastMonth);
                        lastMonthData.setProductId(product.getId());
                        lastMonthData.save();
                    } else {
                        lastMonthData.update();
                    }
                    calculateIds.put("1_" + lastMonthData.getId() + "_jh", 1);
                    return true;
                }
            });
        }

        Map<String, FirstDealerInventoryData> oneMap = new HashMap<String, FirstDealerInventoryData>();
        Map<String, SecondaryDealerInventoryData> twoMap = new HashMap<String, SecondaryDealerInventoryData>();
        
        //上月销售实际T1 T2
        for (int i = 0; i < lastMonthXS.size(); i++) {
            JSONArray finalJsonArray = lastMonthXS;
            int finalI = i;
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    JSONObject obj = finalJsonArray.getJSONObject(finalI);
                    String productName = (String) obj.get("product_name");
                    Product product = Product.dao.findFirst("select * from product where  name =?", productName);
                    if (product == null) {
                        return false;
                    }
                    String dealerCode = (String) obj.get("dealer_code");
                    Dealer dealer = Dealer.dao.findFirst("select * from dealer where code = ?", dealerCode);
                    if (dealer == null) {
                        return false;
                    }

                    String sySjXslStr = (String) obj.get("num");
                    BigDecimal sySjXsl = BigDecimal.ZERO;
                    if (StringUtils.isNotEmpty(sySjXslStr)) {
                    	sySjXsl = new BigDecimal(sySjXslStr);
//                        sySjXsl = new BigDecimal(sySjXslStr).setScale(0, BigDecimal.ROUND_HALF_UP);
                    }
                    if ("1".equalsIgnoreCase(dealer.getLevel())) {

                        //第一次覆盖，后续还有这条就要开始加总
                        String mapKey = "P"+product.getId()+"D"+ dealer.getId()+"L"+ lastMonth + "_xs";
                        if (!oneMap.containsKey(mapKey)) {
                        	//这就说明是第一次来,直接覆盖
                            FirstDealerInventoryData lastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where product_id=? and dealer_id =? and month = ?", product.getId(), dealer.getId(), lastMonth);
                            lastMonthData = lastMonthData == null ? FirstDealerInventoryData.init(dealer, loginUser) : lastMonthData;
                            if (lastMonthData.getId() == null) {
                                //这里要新增，因为总部导入了，那么就代表这数据肯定有
                                lastMonthData.setMonth(lastMonth);
                                lastMonthData.setProductId(product.getId());
                            }
                            lastMonthData.setActualSalesQuantityHeadOffice(sySjXsl.longValue());
                        	oneMap.put(mapKey, lastMonthData);
                        } else {
                            //这就说明不是第一次来，要加总之前的
                        	FirstDealerInventoryData lastMonthData = oneMap.get(mapKey);
                        	Long old = lastMonthData.getActualSalesQuantityHeadOffice();
                            lastMonthData.setActualSalesQuantityHeadOffice(sySjXsl.longValue() + old);
                        }

/*                        if (lastMonthData.getId() == null) {
                            //这里要新增，因为总部导入了，那么就代表这数据肯定有
                            lastMonthData.setMonth(lastMonth);
                            lastMonthData.setProductId(product.getId());
                            lastMonthData.save();
                        } else {
                            lastMonthData.update();
                        }*/
                        //calculateIds.put("1_" + lastMonthData.getId() + "_xs", 1);
                    } else if ("2".equalsIgnoreCase(dealer.getLevel())) {
                        //第一次覆盖，后续还有这条就要开始加总
                        String mapKey = "P"+product.getId()+"D"+ dealer.getId()+"L"+ lastMonth + "_xs";
                        if (!twoMap.containsKey(mapKey)) {
                        	//这就说明是第一次来,直接覆盖
                        	SecondaryDealerInventoryData lastMonthData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where product_id=? and dealer_id =? and month = ?", product.getId(), dealer.getId(), lastMonth);
                        	lastMonthData = lastMonthData == null ? SecondaryDealerInventoryData.init(dealer, loginUser) : lastMonthData;
                        	if (lastMonthData.getId() == null) {
                                //这里要新增，因为总部导入了，那么就代表这数据肯定有
                                lastMonthData.setMonth(lastMonth);
                                lastMonthData.setProductId(product.getId());
                        	}
                        	lastMonthData.setActualSalesQuantityHeadOffice(sySjXsl.longValue());
                        	twoMap.put(mapKey, lastMonthData);
                        } else {
                            //这就说明不是第一次来，要加总之前的
                        	SecondaryDealerInventoryData lastMonthData = twoMap.get(mapKey);
                        	Long old = lastMonthData.getActualSalesQuantityHeadOffice();
                            lastMonthData.setActualSalesQuantityHeadOffice(sySjXsl.longValue() + old);
                        }
                        /*if (lastMonthData.getId() == null) {
                            //这里要新增，因为总部导入了，那么就代表这数据肯定有
                            lastMonthData.setMonth(lastMonth);
                            lastMonthData.setProductId(product.getId());
                            lastMonthData.save();
                        } else {
                            lastMonthData.update();
                        }
                        calculateIds.put("2_" + lastMonthData.getId() + "_xs", 2);*/
                    }
                    return true;
                }
            });
        }
        
        Db.tx(new IAtom() {

			@Override
			public boolean run() throws SQLException {
				try {
					Collection<FirstDealerInventoryData> firstDealerInventoryDatas = oneMap.values();
					Object[][] oneArr =  new Object[firstDealerInventoryDatas.size()][];
					int i = 0;
					for (FirstDealerInventoryData firstDealerInventoryData : firstDealerInventoryDatas) {
						oneArr[i] = new Object[] {firstDealerInventoryData.getId(),firstDealerInventoryData.getDealerId(),
								firstDealerInventoryData.getAreaId(),firstDealerInventoryData.getRegionId(),firstDealerInventoryData.getAreaManagerUserId()
								,firstDealerInventoryData.getBusinessManagerUserId(),firstDealerInventoryData.getMonth(),firstDealerInventoryData.getProductId()
								,firstDealerInventoryData.getActualSalesQuantityHeadOffice(),firstDealerInventoryData.getCreateTime()
								,firstDealerInventoryData.getCreateUserId(),firstDealerInventoryData.getUpdateUserId()
								,new Date(),firstDealerInventoryData.getActualSalesQuantityHeadOffice()};
						i++;
					}
					Db.batch("insert into first_dealer_inventory_data (id,dealer_id,area_id,region_id,area_manager_user_id,business_manager_user_id,month,product_id,actual_sales_quantity_head_office,create_time,create_user_id,update_user_id,update_time) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE actual_sales_quantity_head_office = ?"
							, oneArr, oneArr.length);
				} catch (Exception e1) {
					e1.printStackTrace();
					return false;
				}
				
				try {
					Collection<SecondaryDealerInventoryData> SecondaryDealerInventoryDatas = twoMap.values();
					Object[][] twoArr =  new Object[SecondaryDealerInventoryDatas.size()][];
					int j = 0;
					for (SecondaryDealerInventoryData secondaryDealerInventoryData : SecondaryDealerInventoryDatas) {
						twoArr[j] = new Object[] {secondaryDealerInventoryData.getId(),secondaryDealerInventoryData.getDealerId(),
								secondaryDealerInventoryData.getAreaId(),secondaryDealerInventoryData.getRegionId(),secondaryDealerInventoryData.getAreaManagerUserId()
								,secondaryDealerInventoryData.getBusinessManagerUserId(),secondaryDealerInventoryData.getMonth(),secondaryDealerInventoryData.getProductId()
								,secondaryDealerInventoryData.getActualSalesQuantityHeadOffice(),secondaryDealerInventoryData.getCreateTime()
								,secondaryDealerInventoryData.getCreateUserId(),secondaryDealerInventoryData.getUpdateUserId()
								,new Date(),secondaryDealerInventoryData.getActualSalesQuantityHeadOffice()};
						j++;
					}
					Db.batch("insert into secondary_dealer_inventory_data (id,dealer_id,area_id,region_id,area_manager_user_id,business_manager_user_id,month,product_id,actual_sales_quantity_head_office,create_time,create_user_id,update_user_id,update_time) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE actual_sales_quantity_head_office = ?"
							, twoArr, twoArr.length);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
        	
        });


        calculateIds.forEach((k, v) -> {
            String s = k.split("_")[1];
            Long id = Long.valueOf(s);
            if (v.equals(1)) {
                CalculateService.me.stockDayLastMonth(id);
                CalculateService.me.theoryStockQuantityTask(id, true);
            } else {
                CalculateService.me.stockDaySecondLevelLastMonth(id);
                CalculateService.me.theoryStockQuantitySecondLevelTask(id);
            }
        });

        resultMap.put("success", "success");
        return resultMap;
    }

    /**
     * @param file
     * @param loginUser
     * @return
     */
    public Map calculateDiffOrder(UploadFile file, User loginUser) {
        HashMap resultMap = new HashMap();
        resultMap.put("err", new ArrayList<>());
        resultMap.put("success", "");

        JSONObject excelResult = null;
        try {
            //总部导入上月实际进（3），销（5），以及当月进（10）
            Map map = new HashMap();
            //选择
            //业务类型	销售类型	发票号	开票日期	客户简称  6
            //客户名称
            //币种	汇率	销售部门	业务员	合同编码	表体订单号	仓库	存货编码	存货代码  存货名称 规格型号	批号  失效日期	有效期至  14
            //数量
            //报价	含税单价	无税单价	无税金额	税率（%）	税额	价税合计	扣率（%）	扣率2（%）	折扣额	制单人	复核人	账期	到期日	发运方式编码	发运方式
            //项目名称
            //收款日期	未回款金额	未回款金额本币	导出次数	复核日期	复核时间	客户权限大类编码	客户权限大类名称
            //客户编号
            map.put("上传T1本月进货实际", new String[]{
                            "select",
                            "", "", "", "", "order_calculate_code",   //6
                            "",
                            "dealer_name", "", "", "", "", "", "", "", "", "", "num",//18
                            "", "", "",      
                            "",
                            "", "", "", "", "", "", "product_project_name", "", "", "", "", "", "", "", "", "",         //38
                            "",
                            "", "", "", "", ""
                    }
            );
            excelResult = ExcelUtil.readMultipleExcel(file, map);
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put("worn_msg", "读取数据失败");
            return resultMap;
        }

        JSONArray thisMonthJH = (JSONArray) excelResult.get("上传T1本月进货实际");
        if (thisMonthJH.size() == 0) {
            resultMap.put("worn_msg", "没有读取到上传T1本上月进货实际数据");
            return resultMap;
        }

        //校验数据
//        List errorList = checkMonthJH(thisMonthJH);

//        if (errorList.size() > 0) {
//            resultMap.put("err", errorList);
//            return resultMap;
//        }

        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        List<Long> haveClearOrderNum = new ArrayList<>();
        for (int i = 0; i < thisMonthJH.size(); i++) {
            JSONArray finalJsonArray = thisMonthJH;
            int finalI = i;
            Db.tx(new IAtom() {
                @Override
                public boolean run() {
                    JSONObject obj = finalJsonArray.getJSONObject(finalI);
                    Object select = obj.get("select");
                    if (select != null && select.toString().length() > 0 && ("小计".equalsIgnoreCase(select.toString()) || "合计".equalsIgnoreCase(select.toString()))) {
                        return false;
                    }
                    String productProjectName = (String) obj.get("product_project_name");
                    Product product = Product.dao.findFirst("select * from product where  project_name =?", productProjectName);
                    if (product == null) {
                        return false;
                    }
                    String orderCalculateCode = (String) obj.get("order_calculate_code");
                    String num = (String) obj.get("num");
                    Long orderNum = Long.valueOf(num);
                    Dealer dealer = Dealer.dao.findFirst("select * from dealer where order_calculate_code = ?", orderCalculateCode);
                    if (dealer == null) {
                        return false;
                    }
                    FirstDealerInventoryData thisMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where product_id=? and dealer_id =? and month = ?", product.getId(), dealer.getId(), openTimeStep.getYearAndMonth());
                    thisMonthData = thisMonthData == null ? FirstDealerInventoryData.init(dealer, loginUser) : thisMonthData;

                    if (haveClearOrderNum.contains(thisMonthData.getId())) {
                        Long oldOrderNum = thisMonthData.getOrderNum();
                        orderNum = orderNum + oldOrderNum;
                    }

                    thisMonthData.setActualPurchaseQuantity(Long.valueOf(orderNum));
                    thisMonthData.setOrderNum(orderNum);

                    if (thisMonthData.getId() == null) {
                        thisMonthData.setMonth(openTimeStep.getYearAndMonth());
                        thisMonthData.setProductId(product.getId());
                        thisMonthData.setDiffOrderNum(Long.valueOf(orderNum * -1));
                        thisMonthData.save();
                    } else {
                        if (thisMonthData.getPlanPurchaseQuantity() == null) {
                            thisMonthData.setDiffOrderNum(new BigDecimal(thisMonthData.getOrderNum()).negate().longValue());
                        } else {
                            BigDecimal planPurchaseQuantity = new BigDecimal(thisMonthData.getPlanPurchaseQuantity());
                            BigDecimal subtract = planPurchaseQuantity.subtract(new BigDecimal(thisMonthData.getOrderNum()));
                            thisMonthData.setDiffOrderNum(subtract.longValue());
                        }
                        thisMonthData.update();
                    }
                    haveClearOrderNum.add(thisMonthData.getId());
                    return true;
                }
            });
        }

        //没有被导入的也要算一遍
        List<FirstDealerInventoryData> inventoryData = FirstDealerInventoryData.dao.find("SELECT * FROM first_dealer_inventory_data WHERE `month` = ?", openTimeStep.getYearAndMonth());
        if (inventoryData != null && inventoryData.size() > 0) {
            inventoryData.forEach(data -> {
                Long planPurchaseQuantity = data.getPlanPurchaseQuantity() == null ? 0L : data.getPlanPurchaseQuantity();
                Long orderNum = data.getOrderNum() == null ? 0L : data.getOrderNum();
                data.setDiffOrderNum(planPurchaseQuantity - orderNum);
                data.update();
            });
        }

        resultMap.put("success", "success");
        return resultMap;
    }


    public static boolean isNumber(String str) {
        String reg = "-?[0-9]+.*[0-9]*";
        return str.matches(reg);
    }

    private List<String> checkMonthJH(JSONArray jsonArray) {
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Object select = obj.get("select");
            if (select != null && select.toString().length() > 0 && ("小计".equalsIgnoreCase(select.toString()) || "合计".equalsIgnoreCase(select.toString()))) {
                continue;
            }
            Object dealerName = obj.get("dealer_name");
            if (dealerName == null || dealerName.toString().length() == 0) {
                errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的经销商名称不能为空");
            } else {
                Dealer dealer = Dealer.dao.findFirst("select * from dealer where name = ?", dealerName.toString());
                if (dealer == null) {
                    errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的经销商名称错误");
                }
            }


            Object num = obj.get("num");
            if (num == null || num.toString().length() == 0) {
                errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的实际进货量不能为空");
            } else {
                if (!isNumber(num.toString())) {
                    errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的实际进货量填写错误");
                }
            }

            Object productProjectName = obj.get("product_project_name");
            if (productProjectName == null || productProjectName.toString().length() == 0) {
                errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的项目名称不能为空");
            } else {
                Product product = Product.dao.findFirst("select * from product where project_name = ?", productProjectName.toString());
                if (product == null) {
                    errorList.add("上传T1上月进货实际数据中，第" + (i + 2) + "行的上项目名称填写错误");
                }
            }
        }
        return errorList;
    }

    private List<String> checkMonthXs(JSONArray jsonArray) {
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Object select = obj.get("select");
            if (select != null && select.toString().length() > 0 && ("小计".equalsIgnoreCase(select.toString()) || "合计".equalsIgnoreCase(select.toString()))) {
                continue;
            }
            Object dealerCode = obj.get("dealer_code");
            if (dealerCode == null || dealerCode.toString().length() == 0) {
                errorList.add("上传T1,T2上月销售实际数据中，第" + (i + 2) + "行的经销商编码不能为空");
            } else {
                Dealer dealer = Dealer.dao.findFirst("select * from dealer where code = ?", dealerCode.toString());
                if (dealer == null) {
                    errorList.add("上传T1,T2上月销售实际数据中，第" + (i + 2) + "行的经销商编码填写错误");
                }
            }

            Object num = obj.get("num");
            if (num == null || num.toString().length() == 0) {
                errorList.add("上传T1,T2上月销售实际数据中，第" + (i + 2) + "行的实际销售量不能为空");
            } else {
                if (!isNumber(num.toString())) {
                    errorList.add("上传T1,T2上月销售实际数据中，第" + (i + 2) + "行的实际销售填写错误");
                }
            }

            Object productName = obj.get("product_name");
            if (productName == null || productName.toString().length() == 0) {
                errorList.add("上传的T1,T2上月销售实际数据中，第" + (i + 2) + "行的产品名称不能为空");
            } else {
                Product product = Product.dao.findFirst("select * from product where name = ?", productName.toString());
                if (product == null) {
                    errorList.add("上传的T1,T2上月销售实际数据中，第" + (i + 2) + "行的上产品名称填写错误");
                }
            }
        }
        return errorList;
    }
}