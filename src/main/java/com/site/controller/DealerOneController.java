package com.site.controller;

import com.google.common.collect.ImmutableMap;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.*;
import com.site.core.model.common.OpenTimeStep;
import com.site.service.*;
import com.site.utils.DateUtils;
import com.site.utils.LongUtil;
import com.site.utils.QueryUtil;
import com.site.utils.ToolFunction;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

public class DealerOneController extends BaseController {

    private RoleConfigService roleConfigService;
    private UserService userService;
    private ProductService productService;
    private CalculateService calculateService;
    private DealerOneService dealerOneService;

    /**
     * 初始页面的渲染数据准备
     */
    @Override
    public void index() {
        //根据权限和当前处在的开放期得到可操纵的数据列
        String column = roleConfigService.getCanEditColumn(getLoginUser().getRoleName(), 1);
        OpenTimeStep openTimeStep = roleConfigService.getCurrentOpenTimeStep(1);
        setAttr("canEdit", column);
        setAttr("openTimeStepDesc", openTimeStep.getDesc());
        Integer thisMonth = openTimeStep.getYearAndMonth();
        int yearAndMonth = DateUtils.getYearAndMonth(openTimeStep, 1);
        String month = thisMonth.toString().substring(4, 6);
        String nextMonth = (yearAndMonth + "").substring(4, 6);
        setAttr("month", month);
        setAttr("next_month", nextMonth);
        setAttr("step", openTimeStep.getStep());
        String ll_month = DateUtils.getYearAndMonth(thisMonth + "", "yyyyMM", -2).substring(4, 6);
        setAttr("ll_month", ll_month);
        String l_month = DateUtils.getYearAndMonth(thisMonth + "", "yyyyMM", -1).substring(4, 6);
        setAttr("l_month", l_month);
        setAttr("role_name", getLoginUser().getRoleName());
        List<Long> list = userService.getVacancyBMUserId(getLoginUser().getId(), true);
        if ("大区经理".equalsIgnoreCase(getLoginUser().getRoleName())) {
            //这里只针对大区经理，商务经理走不到这里来
            boolean b = list.size() > 0 && (openTimeStep.getStep() == 1 || openTimeStep.getStep() == 3) && (openTimeStep.getDesc() != 1);
            setAttr("show_dtj_sj", b);
            if (b) {
                //这就说明有待填写数据，需要传入待填写数据的canEdit
                String columns = roleConfigService.getCanEditColumn("商务经理", 1);
                setAttr("canEdit_dtx", columns);
                setAttr("area_code", getLoginUser().getAreaId());
            }
        }

        if ("商务经理".equalsIgnoreCase(getLoginUser().getRoleName())) {
            setAttr("show_dtj_sj", false);
            String columns = roleConfigService.getCanEditColumn("商务经理", 1);
            setAttr("canEdit_dtx", columns);
        }
        if ("总部".equalsIgnoreCase(getLoginUser().getRoleName())) {
            String columns = roleConfigService.getCanEditColumn("总部", 1);
            //如果再第二个锁定期，就看是否能再次填写product的数据，因为确定计算一次之后就不能再修改
            //如果最后一次计算的时间加上第二个锁定期的结束时间==现在，那么就表示已经计算过了
            setAttr("product_purpose_status", (openTimeStep.getProductPurposeStatus()));
            setAttr("canEdit_dtx", columns);
            setAttr("show_dtj_sj", false);
        }

        //上传本月已下单数
        setAttr("show_import_order", "1");

        render("list.html");
    }

    public void list() {
        String productId = getPara("product_id");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String dealerId = getPara("dealer_id");
        String businessId = getPara("business_id");
        String sort = getPara("sort");
        String order = getPara("order");
        Boolean isSelf = getParaToBoolean("is_self", true);
        if (StringUtils.isEmpty(productId)) {
            renderJson(result);
            return;
        }
        QueryUtil queryUtil = getQueryUtil(FirstDealerInventoryData.class);
        QueryUtil.SearchResult searchResult = dealerOneService.getList(queryUtil, getLoginUser(), productId, areaId, regionId, dealerId, businessId, sort, order, isSelf);
        dealerOneService.genTotal(searchResult.getData(), getLoginUser(), productId, businessId, dealerId, areaId, regionId, isSelf);
        renderJson(searchResult);
    }

    /**
     * 保存商务经理和大区经理代替商务经理填写的数据
     */
    public void commitBusinessData() {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        String dealerId = getPara("dealer_id");
        String productId = getPara("product_id");
        String column = getPara("column");
        String val = getPara("val");
        Map<String, String> map = convertUpdateColumn(column);
        String month = map.get("month");
        int yearAndMonth = DateUtils.getYearAndMonth(openTimeStep, Integer.valueOf(month));
        FirstDealerInventoryData oldDate = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealerId, productId, yearAndMonth);

        //非空的值进行判断
        if ("cyyy".equalsIgnoreCase(column)) {
            if (StringUtils.isEmpty(dealerId) || StringUtils.isEmpty(productId) || StringUtils.isEmpty(column)) {
                renderJson(result);
                return;
            }
        } else {
            if (StringUtils.isEmpty(dealerId) || StringUtils.isEmpty(productId) || StringUtils.isEmpty(column) || StringUtils.isEmpty(val)) {
                renderJson(result);
                return;
            }
        }

        GenSql genSqlResult = genSql(dealerId, productId, column, val);
        String sql = genSqlResult.getSql();
        String type = genSqlResult.getType();
        Long bizId = genSqlResult.getBizId();
        Record resultRecord = genSqlResult.getRecord();
        
        String diffOrderNum = ""; 

        int updateRow = 0;
        if ("update".equalsIgnoreCase(type)) {
            updateRow = Db.update(sql);
        } else {
            boolean save = true;
            try {
                Db.save("first_dealer_inventory_data", resultRecord);
            } catch (Exception e) {
                save = false;
            }
            if (save) {
                updateRow = 1;
            }
            bizId = resultRecord.get("id");
            System.out.println(save);
        }
        
        // 如果修改了本月进货预估，那下单差异数跟着变化
        if("by_yg_jhl".equals(column))
        {
        	FirstDealerInventoryData newData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealerId, productId, yearAndMonth);
        	newData.setDiffOrderNum(newData.getPlanPurchaseQuantity() - newData.getOrderNum());
        	newData.update();
        	diffOrderNum = newData.getDiffOrderNum().toString();
        }        

        if (updateRow == 0) {
            //这里要有自动修复作用，主要是防止网络延迟，两条插入同时进来，导致一条会失败，这时候就，失败的那条sql就要转换成update
            FirstDealerInventoryData inventoryData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and month =? and product_id =?", dealerId, openTimeStep.getYearAndMonth(), productId);
            if (inventoryData == null) {
                //如果还是为空，可能就是代码写错了
                renderJson(result);
                return;
            }
            updateRow = Db.update(genSql(dealerId, productId, column, val).getSql());
            if (updateRow == 0) {
                throw new ExceptionForJson("代码写错了");
            }
            renderJson(result);
            return;
        }

        Long lastMonthBizId = null;
        Long thisMonthBizId = null;
        FirstDealerInventoryData lastMonthData = null;
        //修改的是本月的数据
        if ("0".equalsIgnoreCase(month)) {
            thisMonthBizId = bizId;

            int lastMonth = DateUtils.getYearAndMonth(openTimeStep, -1);
            lastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealerId, productId, lastMonth);
            if (lastMonthData != null) {
                lastMonthBizId = lastMonthData.getId();
            }
        }

        if ("-1".equalsIgnoreCase(month)) {
            lastMonthBizId = bizId;

            int thisMonth = DateUtils.getYearAndMonth(openTimeStep, 0);
            FirstDealerInventoryData thisMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealerId, productId, thisMonth);
            if (thisMonthData != null) {
                thisMonthBizId = thisMonthData.getId();
            }
        }


        String lastMonthStockNum = "";
        String lastMonthStockDay = "";
        Long actualStockQuantity = null;
        BigDecimal by_kc = BigDecimal.ZERO;
        BigDecimal by_kc_ts = null;
        //如果改的是上个月数据
        if (lastMonthBizId != null && "-1".equalsIgnoreCase(month)) {
            //计算下上月理论库存(6)
            lastMonthStockNum = calculateService.theoryStockQuantityTask(lastMonthBizId, false);

            //计算本月的实际库存
            if (thisMonthBizId != null) {
                by_kc = calculateService.stockNum(thisMonthBizId);
                by_kc_ts = calculateService.stockDayNowMonth(thisMonthBizId);
            }

            //上月实际库存
            FirstDealerInventoryData inventoryData = FirstDealerInventoryData.dao.findById(lastMonthBizId);
            actualStockQuantity = inventoryData.getActualStockQuantity() == null ? 0L : inventoryData.getActualStockQuantity();

            //上月库存天数
            lastMonthStockDay = calculateService.stockDayLastMonth(lastMonthBizId).toString();
        }

        //如果上月理论库存被计算出来，那么当月库存也要随着变
        if (thisMonthBizId != null && "0".equalsIgnoreCase(month)) {
            //当月库存
            by_kc = calculateService.stockNum(thisMonthBizId);
            //当月库存天数
            by_kc_ts = calculateService.stockDayNowMonth(thisMonthBizId);
        }

        Map<Object, Object> taskResult = new HashMap<>();
        taskResult.put("row_id", bizId);
        taskResult.put("sy_ll_kc", lastMonthStockNum);
        taskResult.put("stock_day", lastMonthStockDay);
        taskResult.put("old_value", genSqlResult.getOldValue());

        taskResult.put("by_kc", by_kc.toString());
        taskResult.put("by_kc_ts", by_kc_ts == null ? "" : by_kc_ts.toString());
        taskResult.put("sy_sj_kc", actualStockQuantity == null ? "" : actualStockQuantity.toString());

        taskResult.put("month", month);
        taskResult.put("old_date", oldDate);
        taskResult.put("diff_order_num", diffOrderNum);

        taskResult.put("flagStatus", 0);
        if (lastMonthData != null) {
            lastMonthData = FirstDealerInventoryData.dao.findById(lastMonthData.getId());
            Long originalTheoryStockQuantity = lastMonthData.getOriginalTheoryStockQuantity();
            Long theoryStockQuantity = lastMonthData.getTheoryStockQuantity();
            boolean equal = LongUtil.equal(originalTheoryStockQuantity, theoryStockQuantity);
            if (!equal) {
                taskResult.put("flagStatus", 3);
            }

        }
        result.setData(taskResult);
        renderJson(result);
    }

    //获取大区列表
    public void getAreaList() {
        List list = roleConfigService.getAreaListByUser(getLoginUser().getId());
        result.setData(list);
        renderJson(result);
    }

    //获取省份列表
    public void getRegionList() {
        User loginUser = getLoginUser();
        String areaName = getPara("area_ids");
        Boolean isSelf = getParaToBoolean("is_self", true);
        int level = getParaToInt("level", 1);

        if (loginUser.getRoleName().equalsIgnoreCase("大区经理")) {
            areaName = loginUser.getAreaId() + "";
        }

        if (loginUser.getRoleName().equalsIgnoreCase("商务经理")) {
            List re = Region.dao.find("SELECT b.* FROM dealer a LEFT JOIN region b ON a.region_id = b.id WHERE business_manager_user_id = (?) and  a.`level` =?  GROUP BY b.id", loginUser.getId(), level);
            result.setData(re);
            renderJson(result);
            return;
        }

        if (StringUtils.isEmpty(areaName)) {
            result.setData(new ArrayList<>());
            renderJson(result);
            return;
        }
        List list = new ArrayList();
        if (isSelf) {
            list = roleConfigService.getRegionList(Arrays.asList(areaName.split(",")), loginUser.getId());
        } else {
            list = roleConfigService.getRegionListForDtx(Arrays.asList(areaName.split(",")), loginUser.getId(), level);
        }
        result.setData(list);
        renderJson(result);
    }

    //获取商务经理列表
    public void getBusinessManagerList() {
    	String areaName = getPara("area_ids");
    	String regionIds = getPara("region_ids");
    	String dealerIds = getPara("dealer_ids");
        List<User> list = roleConfigService.getBusinessManagerByAreaIds(ToolFunction.str2List(areaName), ToolFunction.str2List(regionIds), ToolFunction.str2List(dealerIds),getLoginUser().getId());
        result.setData(list);
        renderJson(result);
    }

    //获取待填写页面的商务经理列表
    public void getBusinessManagerListForDtx() {
    	String areaName = getPara("area_ids");
    	String regionIds = getPara("region_ids");
    	String dealerIds = getPara("dealer_ids");
    	List<User> userList = roleConfigService.getBusinessManagerByAreaIds(ToolFunction.str2List(areaName), ToolFunction.str2List(regionIds), ToolFunction.str2List(dealerIds),getLoginUser().getId());
    	
    	Set<Long> userIdSet = new HashSet<Long>();
    	for (User user : userList)
    	{
    		if (null != user)
    		{
    			userIdSet.add(user.getId());
    		}
    	}
    	
        List<User> users = new ArrayList<>();
        List<Long> list = userService.getVacancyBMUserId(getLoginUser().getId(), true);
        list.forEach(k -> {
        	if (userIdSet.contains(k))
        	{
                User byId = User.dao.findById(k);
                users.add(byId);
        	}
        });
        result.setData(users);
        renderJson(result);
    }

    //获取经销商列表
    public void getDealerList() {
    	String areaName = getPara("area_ids");
    	String regionIds = getPara("region_ids");
    	String businessIds = getPara("business_ids");
        Boolean isSelf = getParaToBoolean("isSelf", true);
        List<Dealer> list = roleConfigService.getDealerList(ToolFunction.str2List(areaName),ToolFunction.str2List(regionIds),ToolFunction.str2List(businessIds), getLoginUser().getId(), getPara("level"), isSelf);
        result.setData(list);
        renderJson(result);
    }

    //获取商品列表
    public void getProductList() {
        Boolean isSelf = getParaToBoolean("isSelf");
        int level = getParaToInt("level", 1);
        List<Product> productList = productService.getProductList(getLoginUser(), level, isSelf);
        result.setData(productList);
        renderJson(result);
    }

    //总部在第一个锁定期开始导入的数据3，5，10   系统更新6和8
    public void importData() {
        UploadFile file = getFile();
        Map map = dealerOneService.calculateStockDayAndStockNum(file, getLoginUser());
        file.getFile().delete();
        renderJson(map);
    }

    //总部导入本月下单数量
    public void importOrderData() {
        UploadFile file = getFile();
        Map map = dealerOneService.calculateDiffOrder(file, getLoginUser());
        file.getFile().delete();
        renderJson(map);
//===========
//        BFService.me.setXtfpcg(file, getLoginUser());
//        renderJson("success");


//        try {
//            BFService.me.serOrderNum(file, getLoginUser());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        renderJson("success");
    }

    /**
     * 计算本月库存天数和上月库存天数
     */
    public void getStockDayForTotal() {
        Map map = new HashMap();
        String productId = getPara("product_id");
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String dealerId = getPara("dealer_id");
        String businessId = getPara("business_id");
        Boolean isSelf = getParaToBoolean("is_self", true);

        BigDecimal totalSyKcts = TotalService.me.getTotalSyKcts(getLoginUser(), productId, businessId, dealerId, areaId, regionId, isSelf);
        BigDecimal totalByKcts = TotalService.me.getTotalByKcts(getLoginUser(), productId, businessId, dealerId, areaId, regionId, isSelf);

        map.put("sy_kcts", totalSyKcts.toString());
        map.put("by_kcts", totalByKcts.toString());

        result.setData(map);
        renderJson(result);
    }

    /**
     * 根据前台传进来的字段名字，来推出更新条件个被更新的准确字段
     */
    private GenSql genSql(String dealerId, String productId, String column, String val) {
        GenSql sqlResult = new GenSql();
        StringBuilder sql = new StringBuilder();
        Dealer dealer = Dealer.dao.findById(dealerId);
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);

        if (StringUtils.isEmpty(val)) {
            val = "''";
        }

        Map<String, String> updateColumn = convertUpdateColumn(column);
        String databaseColumn = updateColumn.get("column");
        String monthDiffer = updateColumn.get("month");
        int updateYearAndMonth = Integer.parseInt(DateUtils.getYearAndMonth(openTimeStep.getYearAndMonth() + "", "yyyyMM", new Integer(monthDiffer)));

        if (openTimeStep.getYearAndMonth() == updateYearAndMonth) {
            //本月数据
            FirstDealerInventoryData first = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id=? and product_id=? and month=?", dealerId, productId, updateYearAndMonth);
            if (first != null) {
                sqlResult.setOldValue(first.get(databaseColumn) == null ? "" : first.get(databaseColumn).toString());
                //本月数据存在
                sqlResult.setBizId(Long.valueOf(first.getId()));
                sqlResult.setType("update");
                sql.append("update first_dealer_inventory_data set ")
                        .append(databaseColumn)
                        .append(" = ")
                        .append(val)
                        .append("  where id =")
                        .append(first.getId());
            } else {
                sqlResult.setOldValue(null);
                //本月数据不存在
                Record record = new Record();
                record.set("area_id", dealer.getAreaId());
                record.set("region_id", dealer.getRegionId());
                record.set("area_manager_user_id", dealer.getAreaManagerUserId());
                record.set("business_manager_user_id", dealer.getBusinessManagerUserId());
                record.set("dealer_id", dealer.getId());
                record.set("month", updateYearAndMonth);
                record.set("product_id", productId);
                record.set("status", "0");
                record.set("create_time", new Date());
                record.set("create_user_id", getLoginUser().getId());
                record.set("update_time", new Date());
                record.set("update_user_id", getLoginUser().getId());
                record.set(databaseColumn, val);
                sql.append("insert into first_dealer_inventory_data " +
                        "(area_id,region_id,area_manager_user_id,business_manager_user_id," +
                        "dealer_id,month,product_id,status,create_time,create_user_id,update_time,update_user_id,")
                        .append(databaseColumn)
                        .append(") values (")

                        .append(dealer.getAreaId()).append(",")
                        .append(dealer.getRegionId()).append(",")

                        .append(dealer.getAreaManagerUserId()).append(",")
                        .append(dealer.getBusinessManagerUserId()).append(",")
                        .append(dealer.getId()).append(",")
                        .append(updateYearAndMonth).append(",")
                        .append(productId).append(",")
                        .append("0").append(",")
                        .append(new Date()).append(",")
                        .append(getLoginUser().getId()).append(",")
                        .append(new Date()).append(",")
                        .append(getLoginUser().getId()).append(",")
                        .append(val).append(")")
                ;
                sqlResult.setRecord(record);
                sqlResult.setType("insert");
            }
        } else {
            //上月数据
            FirstDealerInventoryData first = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id=? and product_id=? and month=?", dealerId, productId, updateYearAndMonth);
            if (first != null) {
                sqlResult.setOldValue(first.get(databaseColumn) == null ? "" : first.get(databaseColumn).toString());
                //上月数据存在
                sql.append("update first_dealer_inventory_data set ")
                        .append(databaseColumn)
                        .append(" = ")
                        .append(val)
                        .append("  where  dealer_id=")
                        .append(dealerId)
                        .append(" and product_id =")
                        .append(productId)
                        .append(" and month=")
                        .append(updateYearAndMonth);
                sqlResult.setBizId(first.getId());
                sqlResult.setType("update");
            } else {
                sqlResult.setOldValue(null);
                //上月数据不存在
                Record record = new Record();
                record.set("area_id", dealer.getAreaId());
                record.set("region_id", dealer.getRegionId());
                record.set("area_manager_user_id", dealer.getAreaManagerUserId());
                record.set("business_manager_user_id", dealer.getBusinessManagerUserId());
                record.set("dealer_id", dealer.getId());
                record.set("month", updateYearAndMonth);
                record.set("product_id", productId);
                record.set("status", "0");
                record.set("create_time", new Date());
                record.set("create_user_id", getLoginUser().getId());
                record.set("update_time", new Date());
                record.set("update_user_id", getLoginUser().getId());
                record.set(databaseColumn, val);

                sql.append("insert into first_dealer_inventory_data " +
                        "(area_id,region_id,area_manager_user_id,business_manager_user_id," +
                        "dealer_id,month,product_id,status,create_time,create_user_id,update_time,update_user_id,")
                        .append(databaseColumn)
                        .append(") values (")
                        .append(dealer.getAreaId()).append(",")
                        .append(dealer.getRegionId()).append(",")
                        .append(dealer.getAreaManagerUserId()).append(",")
                        .append(dealer.getBusinessManagerUserId()).append(",")
                        .append(dealer.getId()).append(",")
                        .append(updateYearAndMonth).append(",")
                        .append(productId).append(",")
                        .append("0").append(",")
                        .append(DateUtils.getNowDate("yyyyMMdd")).append(",")
                        .append(getLoginUser().getId()).append(",")
                        .append(DateUtils.getNowDate("yyyyMMdd")).append(",")
                        .append(getLoginUser().getId()).append(",")
                        .append(val).append(")");
                sqlResult.setType("insert");
                sqlResult.setRecord(record);
            }
        }

        sqlResult.setSql(sql.toString());
        return sqlResult;
    }

    /**
     * 前台字段名称和后台字段名称互转
     *
     * @return
     */
    private Map<String, String> convertUpdateColumn(String column) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        switch (column) {
            case "sy_yg_jhl":
                builder.put("column", "plan_purchase_quantity").put("month", "-1");
                break;
            case "sy_jhl":
                builder.put("column", "actual_purchase_quantity").put("month", "-1");
                break;
            case "sy_yg_xsl":
                builder.put("column", "plan_sales_quantity").put("month", "-1");
                break;
            case "sy_sj_xsl":
                builder.put("column", "actual_sales_quantity").put("month", "-1");
                break;
            case "sy_ll_kc":
                builder.put("column", "theory_stock_quantity").put("month", "-1");
                break;
            case "sy_sj_kc":
                builder.put("column", "actual_stock_quantity").put("month", "-1");
                break;
            case "sy_kcts":
                builder.put("column", "inventory_day").put("month", "-1");
                break;
            case "by_xtfp_cgl":
                builder.put("column", "purchase_quantity").put("month", "0");
                break;
            case "by_jhl":
                builder.put("column", "actual_purchase_quantity").put("month", "0");
                break;
            case "by_yg_jhl":
                builder.put("column", "plan_purchase_quantity").put("month", "0");
                break;
            case "by_yg_xsl":
                builder.put("column", "plan_sales_quantity").put("month", "0");
                break;
            case "by_kc":
                builder.put("column", "actual_stock_quantity").put("month", "0");
                break;
            case "by_kcts":
                builder.put("column", "inventory_day").put("month", "0");
                break;
            case "cyyy":
                builder.put("column", "diff_cause").put("month", "-1");
                break;
        }
        return builder.build();
    }

    public static class GenSql {
        private String sql;
        private Record record;
        private String type;
        private Long bizId;
        private String oldValue;

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public Record getRecord() {
            return record;
        }

        public void setRecord(Record record) {
            this.record = record;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getBizId() {
            return bizId;
        }

        public void setBizId(Long bizId) {
            this.bizId = bizId;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }
    }

    public void re() {
        List<FirstDealerInventoryData> inventoryData = FirstDealerInventoryData.dao.find("select * from first_dealer_inventory_data where month = 201808");
        inventoryData.forEach(k -> {
            if (k.getPlanSalesQuantity() == null && k.getPlanPurchaseQuantity() == null) {
                return;
            }
            calculateService.stockNum(k.getId());
        });
        renderNull();
    }


    public void re2() {
        List<FirstDealerInventoryData> inventoryData = FirstDealerInventoryData.dao.find("select * from secondary_dealer_inventory_data where month = 201807");
        inventoryData.forEach(k -> {
            calculateService.theoryStockQuantitySecondLevelTask(k.getId());
        });
        renderJson("success");
    }

}
