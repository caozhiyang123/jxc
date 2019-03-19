package com.site.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.*;
import com.site.service.DealerService;
import com.site.service.RoleConfigService;
import com.site.utils.*;
import net.sf.cglib.beans.BeanCopier;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Dealer 管理
 * 描述：
 */
public class DealerController extends BaseController {

    private static final Log log = Log.getLog(DealerController.class);

    static DealerService srv = DealerService.me;

    /**
     * 列表
     * /site/dealer/list
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(Dealer.class);
        queryUtil.setSqlSelect("SELECT a.*,b.name business_manager_name,b.employee_number business_manager_employee_id,c.name area_manager_name,c.employee_number AS area_manager_employee_id");
        queryUtil.setSqlExceptSelect("FROM dealer a LEFT JOIN `user` b ON a.business_manager_user_id = b.id LEFT JOIN `user` c ON a.area_manager_user_id = c.id");
        queryUtil.addQueryParam("a.is_delete", "=", "0");
        queryUtil.setSort("a.id");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"b.name", "b.employee_number", "c.name", "c.employee_number", "a.name", "a.code", "upstream_name"});

        if (!StrKit.isBlank(getPara("area_id")))
            queryUtil.addQueryParam("a.area_id", "=", getPara("area_id"));

        if (!StrKit.isBlank(getPara("region_id")))
            queryUtil.addQueryParam("a.region_id", "=", getPara("region_id"));

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<Dealer> dealers = searchResult.getData();
        if (dealers != null) {
            dealers.forEach(dealer -> {
                if (dealer.getAreaId() != null) {
                    Area area = Area.dao.findById(dealer.getAreaId());
                    if (area != null) {
                        dealer.put("area_name", area.getName());
                    }
                }

                if (dealer.getRegionId() != null) {
                    Region region = Region.dao.findById(dealer.getRegionId());
                    if (region != null) {
                        dealer.put("region_name", region.getName());
                    }
                }

                if (dealer.getLevel() != null) {
                    dealer.setLevel("T" + dealer.getLevel());
                }

                if (dealer.getCreateUserId() != null) {
                    User createUser = User.dao.findById(dealer.getCreateUserId());
                    if (createUser != null) {
                        dealer.put("create_user_name", createUser.getName());
                    }
                }

                if (dealer.getUpdateUserId() != null) {
                    User updateUser = User.dao.findById(dealer.getLong("update_user_id"));
                    if (updateUser != null) {
                        dealer.put("update_user_name", updateUser.getName());
                    }
                }

                if (dealer.getId() != null) {
                    List<Product> products = Product.dao.find("select p.name from dealer_product dp left join product p on dp.product_id=p.id where dp.dealer_id=?", dealer.getId());
                    List<String> productNames = new ArrayList<>();
                    String productName = "";
                    if (products.size() > 0) {
                        products.forEach(product -> productNames.add(product.getName()));
                        productName = StringUtils.join(productNames, "，");
                    }
                    dealer.put("product_name", productName);
                }
            });
        }
        renderJson(searchResult);
    }

    /**
     * 获取所有大区
     */
    public void getAreaList() {
        List<Area> list = Area.dao.find("select id,name from area");
        result.setData(list);
        renderJson(result);
    }

    /**
     * 获取所有省份
     */
    public void getRegionList() {
        List<Region> list = Region.dao.find("select id,name from region");
        result.setData(list);
        renderJson(result);
    }

    /**
     * 准备添加
     * /site/dealer/add
     */
    public void add() {
        render("add.html");
    }

    /**
     * 保存
     * /site/dealer/save
     */
    @Before(Tx.class)
    public void save() {
        srv.save(getModel(Dealer.class));
        renderJson(result);
    }

    /**
     * 准备更新
     * /site/dealer/edit
     */
    public void edit() {
        Dealer dealer = srv.findById(getParaToInt("id"));
        setAttr("dealer", dealer);
        render("edit.html");
    }

    /**
     * 更新
     * /site/dealer/update
     */
    @Before(Tx.class)
    public void update() {
        srv.update(getModel(Dealer.class));
        renderJson(result);
    }

    /**
     * 查看
     * /site/dealer/view
     */
    public void detail() {
        Dealer dealer = srv.findById(getParaToInt("id"));
        setAttr("dealer", dealer);
        render("detail.html");
    }

    /**
     * 导出架构数据
     */
    public void exportDealer() {
        QueryUtil queryUtil = getQueryUtil(Dealer.class);
        queryUtil.setSqlSelect("SELECT a.id,area.name area_name,region.name region_name,a.name dealer_name,a.code dealer_code,a.order_calculate_code,a.level dealer_level,a.upstream_name,b.name business_manager_name,b.employee_number business_manager_employee_number,c.name area_manager_name,c.employee_number AS area_manager_employee_number");
        queryUtil.setSqlExceptSelect("FROM dealer a LEFT JOIN area area ON area.id=a.area_id LEFT JOIN region region ON region.id=a.region_id LEFT JOIN `user` b ON a.business_manager_user_id = b.id LEFT JOIN `user` c ON a.area_manager_user_id = c.id");
        queryUtil.addQueryParam("a.is_delete", "=", "0");
        queryUtil.setPageSize(Integer.MAX_VALUE);
        queryUtil.setSort("a.id");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"b.name", "b.employee_number", "c.name", "c.employee_number", "a.name", "a.code", "upstream_name"});

        if (StringUtils.isNotBlank(getPara("area_id")))
            queryUtil.addQueryParam("a.area_id", "=", getPara("area_id"));

        if (StringUtils.isNotBlank(getPara("region_id")))
            queryUtil.addQueryParam("a.region_id", "=", getPara("region_id"));

        List<Dealer> dealers = (List<Dealer>) queryUtil.result().getList();

        if (dealers != null) {
            dealers.forEach(dealer -> {
                List<Product> productNames = Product.dao.find("select p.name from dealer_product dp LEFT JOIN product p ON p.id=dp.product_id where dp.dealer_id=?", dealer.getId());
                if (productNames != null && productNames.size() > 0) {
                    productNames.forEach(productName -> dealer.put(productName.getName(), "是"));
                }
            });
        }

        //生成文件目录
        String fileName = "架构数据" + System.currentTimeMillis() + ".xls";
        //获取所有的产品名称
        List<Product> products = Product.dao.find("select name from product where is_delete='0' order by id desc");
        List<String> excelColumns = new ArrayList<>();
        products.forEach(product -> excelColumns.add(product.getName()));
        String fieldStr = StringUtils.join(excelColumns, ",");
        try {
            PoiUtils.exportData(getResponse(), Dealer.class, dealers, fileName, "大区,省份,大区经理姓名,大区经理员工号,商务经理姓名,商务经理员工号,经销商名称,经销商编码,ERP编码,商业公司级别,上游商业," + fieldStr, "area_name,region_name,area_manager_name,area_manager_employee_number,business_manager_name,business_manager_employee_number,dealer_name,dealer_code,order_calculate_code,dealer_level,upstream_name," + fieldStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * 下载模板
     */
    public void downloadTemplate() {
        //获取所有的产品名称
        List<Product> products = Product.dao.find("select name from product where is_delete='0' order by id desc");
        List<String> excelColumns = new ArrayList<>();
        products.forEach(product -> {
            String productName = product.getName();
            excelColumns.add(productName);
        });

        //生成文件目录
        String fileName = "架构数据" + System.currentTimeMillis() + ".xls";
        String fieldStr = StringUtils.join(excelColumns, ",");
        try {
            PoiUtils.exportData(getResponse(), Dealer.class, new ArrayList<>(), fileName, "大区,省份,大区经理姓名,大区经理员工号,商务经理姓名,商务经理员工号,经销商名称,经销商编码,ERP编码,商业公司级别,上游商业," + fieldStr, "area_name,region_name,area_manager_name,area_manager_employee_number,business_manager_name,business_manager_employee_number,dealer_name,dealer_code,order_calculate_code,dealer_level,upstream_name," + fieldStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * 删除
     * /site/dealer/delete
     */
    @Before(Tx.class)
    public void delete() {
        Integer id = getParaToInt("id");
        if (id != null) {
            //删除经销商
            Dealer dealer = srv.findById(id);
            dealer.setIsDelete("1");
            dealer.update();

            //删除经销商关联的产品数据
            Db.update("delete from dealer_product where dealer_id=?", dealer.getId());
        }
        renderJson(result);
    }

    /**
     * 文件上传
     * /user/import
     */
    @Before(Tx.class)
    public void importFile() {
        List<String> excelColumns = new ArrayList<>();
        excelColumns.add("area_name");
        excelColumns.add("region_name");
        excelColumns.add("area_manager_name");
        excelColumns.add("area_manager_employee_number");
        excelColumns.add("business_manager_name");
        excelColumns.add("business_manager_employee_number");
        excelColumns.add("dealer_name");
        excelColumns.add("dealer_code");
        excelColumns.add("order_calculate_code");
        excelColumns.add("dealer_level");
        excelColumns.add("upstream_name");
        //获取所有的产品名称
        List<Product> products = Product.dao.find("select id,name from product where is_delete='0' order by id desc");
        Map<String, Long> productMap = new HashMap<>();
        products.forEach(product -> {
            excelColumns.add(product.getName());
            productMap.put(product.getName(), product.getId());
        });

        UploadFile file = getFile();
        JSONArray jsonArray = null;

        try {
            jsonArray = ExcelUtil.readExcel(file, excelColumns.toArray(new String[excelColumns.size()]));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.getFile().delete();
        }

        if (jsonArray == null || jsonArray.size() == 0) {
            throw new ExceptionForJson("文件数据异常，请修改后重试");
        }

        JSONObject validate = validateExcelData(jsonArray);
        Map result = new HashMap<>();
        String warnMessage = validate.getString("warnMessage");
        if (!validate.getBoolean("validate")) {
            result.put("code", "2");
            result.put("message", "上传失败");
            result.put("warnMessage", warnMessage);
            result.put("errorMessage", validate.getString("errorMessage"));
            renderJson(result);
            return;
        }


        JSONObject dealerMap = validate.getJSONObject("dealerMap");
        Set<String> dealerCodes = dealerMap.keySet();
        int oneLevelMonth = RoleConfigService.me.getCurrentOpenTimeStep(1).getYearAndMonth();
        int twoLevelMonth = RoleConfigService.me.getCurrentOpenTimeStep(2).getYearAndMonth();
        long userId = getLoginUser().getId();
        //记录哪些大区已经新增过，后续不再新增
        Map<String, Area> insertAreaMap = new HashMap<>();
        //记录哪些省份已经新增过，后续不再新增
        Map<String, Region> insertRegionMap = new HashMap<>();
//        将所有的经销商版本置为1
//        Db.update("update dealer set version='1'");
        dealerCodes.forEach(productName -> {
            JSONObject jsonObject = dealerMap.getJSONObject(productName);

            Dealer dealer = new Dealer();

            Long areaId = jsonObject.getLong("area_id");
            if (areaId != null) {
                dealer.setAreaId(areaId);
            } else {
                //判断前面一行是否新增了此大区
                String areaName = jsonObject.getString("area_name");
                Area area = insertAreaMap.get(areaName);
                if (area == null) {
                    area = insertArea(areaName, userId);
                    insertAreaMap.put(areaName, area);
                }
                dealer.setAreaId(area.getId());
                areaId = area.getId();
            }

            Long regionId = jsonObject.getLong("region_id");
            if (regionId != null) {
                if (jsonObject.getBoolean("update_region") != null) {
                    Region region = new Region();
                    region.setId(regionId);
                    region.setAreaId(areaId);
                    region.update();
                    //大区省份变化需要更新经销商数据表中的大区省份
                    Db.update("update first_dealer_inventory_data set area_id=? where region_id=? and month>=?", areaId, regionId, oneLevelMonth);
                    Db.update("update secondary_dealer_inventory_data set area_id=? where region_id=? and month=?", areaId, regionId, twoLevelMonth);
                    Db.update("update dealer set area_id=? where region_id=?", areaId, regionId);
                }
                dealer.setRegionId(regionId);
            } else {
                //判断前面一行是否新增了此省份
                String regionName = jsonObject.getString("region_name");
                Region region = insertRegionMap.get(regionName);
                if (region == null) {
                    region = insertRegion(areaId, regionName, userId);
                    insertRegionMap.put(regionName, region);
                } else {
                    if (areaId != region.getAreaId()) {
                        region.setAreaId(areaId);
                        region.setUpdateUserId(userId);
                        region.update();
                    }
                }
                dealer.setRegionId(region.getId());
                regionId = dealer.getRegionId();
            }

            Long areaManagerId = jsonObject.getLong("area_manager_user_id");
            if (areaManagerId != null) {
                if (jsonObject.getBoolean("update_area_manager") != null) {
                    User user = new User();
                    user.setId(areaManagerId);
                    user.setAreaId(areaId);
                    user.setUpdateUserId(userId);
                    user.update();
                }
                dealer.setAreaManagerUserId(areaManagerId);
            }

            Long businessManagerId = jsonObject.getLong("business_manager_user_id");
            if (businessManagerId != null) {
                if (jsonObject.getBoolean("update_business_manager") != null) {
                    User user = new User();
                    user.setId(businessManagerId);
                    user.setParentId(areaManagerId);
                    user.setAreaId(areaId);
                    user.setUpdateUserId(userId);
                    user.update();
                }
                if (jsonObject.getBoolean("update_business_dealer_data") != null) {
                    //商务经理上级大区经理变化需要更新经销商数据表中的大区经理和商务经理
                    Db.update("update first_dealer_inventory_data set area_manager_user_id=? where business_manager_user_id=? and month>=?", areaManagerId, businessManagerId, oneLevelMonth);
                    Db.update("update secondary_dealer_inventory_data set area_manager_user_id=? where business_manager_user_id=? and month=?", areaManagerId, businessManagerId, twoLevelMonth);
                    Db.update("update dealer set area_manager_user_id=? where business_manager_user_id=?", areaManagerId, businessManagerId);
                }
                dealer.setBusinessManagerUserId(businessManagerId);
            }

            String dealerName = jsonObject.getString("dealer_name");
            if (StringUtils.isNotBlank(dealerName)) {
                dealer.setName(dealerName);
            }

            String upstreamName = jsonObject.getString("upstream_name");
            if (StringUtils.isNotBlank(upstreamName)) {
                dealer.setUpstreamName(upstreamName);
            }

            String dealerCode = jsonObject.getString("dealer_code");
            if (StringUtils.isNotBlank(dealerCode)) {
                dealer.setCode(dealerCode);
            }

            String dealerLevel = jsonObject.getString("dealer_level");
            if (dealerLevel != null) {
                dealer.setLevel(dealerLevel.replaceAll("T", ""));
                
                if (jsonObject.getString("update_dealer_level") != null) 
                {
                	dealer.setOldLevel(jsonObject.getString("update_dealer_level"));
                	dealer.setChangeLevelTime(new Date());
                	
                	moveDataForDealerLevelChange(jsonObject.getString("dealer_id"), dealer.getLevel());
                }
            }

            String orderCalculateCode = jsonObject.getString("order_calculate_code");
            if (orderCalculateCode != null) {
                dealer.setOrderCalculateCode(orderCalculateCode);
            }

            Long dealerId = jsonObject.getLong("dealer_id");
            if (dealerId == null) {
                dealer.setCreateUserId(userId);
                dealer.setCreateTime(new Date());
                dealer.setVersion("0");
                dealer.save();
            } else {
                dealer.setId(dealerId);
                dealer.setUpdateUserId(userId);
                dealer.setIsDelete("0");
                dealer.setVersion("0");
                dealer.update();

                //更新该经销商进销存数据表中的大区省份等数据
                if (jsonObject.getBoolean("update_dealer_data") != null) {
                    Db.update("update first_dealer_inventory_data set area_id=?,region_id=?,area_manager_user_id=?,business_manager_user_id=?  where dealer_id=? and month>=?", areaId, regionId, areaManagerId, businessManagerId, dealerId, oneLevelMonth);
                    Db.update("update secondary_dealer_inventory_data set area_id=?,region_id=?,area_manager_user_id=?,business_manager_user_id=? where dealer_id=? and month=?", areaId, regionId, areaManagerId, businessManagerId, dealerId, twoLevelMonth);
                }
            }

            Db.update("delete from dealer_product where dealer_id=?", dealer.getId());

            excelColumns.forEach(excelColumn -> {
                String isHave = jsonObject.getString(excelColumn);
                if (StringUtils.isNotBlank(isHave) && isHave.equals("是")) {
                    long productId = productMap.get(excelColumn);
                    DealerProduct dealerProduct = new DealerProduct();
                    dealerProduct.setDealerId(dealer.getId());
                    dealerProduct.setProductId(productId);
                    dealerProduct.setCreateTime(new Date());
                    dealerProduct.setCreateUserId(userId);
                    dealerProduct.save();
                }
            });
        });

//        Db.update("update dealer set is_delete='1',version='0' where version='1'");

        String code = "0";
        if (StringUtils.isNotBlank(warnMessage)) {
            code = "1";
        }

        result.put("code", code);
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("warnMessage", warnMessage);
        result.put("errorMessage", "");
        file.getFile().delete();
        renderJson(result);
    }

    /**
     * 验证数据是否正确并获取员工号
     * @param jsonArray
     * @return
     */
    private JSONObject validateExcelData(JSONArray jsonArray){
        JSONObject result = new JSONObject();
        result.put("validate", true);
        JSONObject dealerMap = new JSONObject();
        StringBuilder warnMessage = new StringBuilder();
        StringBuilder errorMessage = new StringBuilder();

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            StringBuilder warnMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();

            String areaName = jsonObject.getString("area_name");
            long areaId = 0l;
            if (StringUtils.isBlank(areaName)) {
                result.put("validate", false);
                errorMsg.append("大区为必填选项;");
            } else {
                Area area = Area.dao.findFirst("select id from area where name = ?", areaName);
                if (area == null) {
                    warnMsg.append("系统中未有此大区，系统将默认新增;");
                } else {
                    jsonObject.put("area_id", area.getId());
                    areaId = area.getId();
                }
            }

            String regionName = jsonObject.getString("region_name");
            long regionId = 0l;
            if (StringUtils.isBlank(regionName)) {
                result.put("validate", false);
                errorMsg.append("省份为必填选项;");
            } else {
                String value = map.get(regionName);
                if (value == null) {
                    map.put(regionName, areaName);
                } else {
                    if (value != areaName ) {
                        result.put("validate", false);
                        errorMsg.append("(" + regionName + ")省份所属多个不同大区，请修改数据后重新导入;");
                    }
                }
                Region region = Region.dao.findFirst("select id,area_id from region where name like ?", "%" + regionName + "%");
                if (region == null) {
                    warnMsg.append("系统中未有此省份，系统将默认新增;");
                } else {
                    if (areaId != region.getAreaId()) {
                        jsonObject.put("update_region", true);
                    }
                    jsonObject.put("region_id", region.getId());
                    regionId = region.getId();
                }
            }

            String areaManagerEmployId = jsonObject.getString("area_manager_employee_number");
            long areaManagerId = 0l;
            if (StringUtils.isBlank(areaManagerEmployId)) {
                result.put("validate", false);
                errorMsg.append("大区经理员工号为必填选项;");
            } else {
                User user = User.dao.findFirst("select id,area_id from user where employee_number=? and role_name='大区经理'", areaManagerEmployId);
                if (user == null) {
                    result.put("validate", false);
                    errorMsg.append("该大区经理员工号不存在;");
                } else {
                    if (user.getAreaId() == null || areaId != user.getAreaId()) {
                        jsonObject.put("update_area_manager", true);
                    }
                    jsonObject.put("area_manager_user_id", user.getId());
                    areaManagerId = user.getId();
                }
            }

            String businessManagerEmployId = jsonObject.getString("business_manager_employee_number");
            long businessManagerId = 0l;
            if (StringUtils.isBlank(businessManagerEmployId)) {
                result.put("validate", false);
                errorMsg.append("商务经理员工号为必填选项;");
            } else {
                String value = map.get(businessManagerEmployId);
                if (value == null) {
                    map.put(businessManagerEmployId, areaManagerEmployId);
                } else {
                    if (value != areaManagerEmployId ) {
                        result.put("validate", false);
                        errorMsg.append("(" + businessManagerEmployId + ")商务经理所属多个不同大区经理，请修改数据后重新导入;");
                    }
                }
                User user = User.dao.findFirst("select id,area_id,parent_id from user where employee_number=? and role_name='商务经理'", businessManagerEmployId);
                if (user == null) {
                    result.put("validate", false);
                    errorMsg.append("该商务经理员工号不存在;");
                } else {
                    if (user.getAreaId() == null || areaId != user.getAreaId() ) {
                        jsonObject.put("update_business_manager", true);
                    }
                    if (user.getParentId() == null || areaManagerId != user.getParentId()) {
                        jsonObject.put("update_business_manager", true);
                        jsonObject.put("update_business_dealer_data", true);
                    }
                    jsonObject.put("business_manager_user_id", user.getId());
                    businessManagerId = user.getId();
                }
            }

            String dealerCode = jsonObject.getString("dealer_code");
            if (StringUtils.isBlank(dealerCode)) {
                result.put("validate", false);
                errorMsg.append("经销商编码为必填选项;");
            } else {
                Dealer dealer = Dealer.dao.findFirst("select id,area_id,region_id,area_manager_user_id,business_manager_user_id,`level` from dealer where code=?", dealerCode);
                if (dealer == null) {
                    warnMsg.append("系统中未有此经销商编码，系统将自动新增此经销商;");
                } else {
                    if (areaId != dealer.getAreaId()
                            || regionId != dealer.getRegionId()
                            || areaManagerId != dealer.getAreaManagerUserId()
                            || businessManagerId != dealer.getBusinessManagerUserId()) {
                        jsonObject.put("update_dealer_data", true);
                    }
                    jsonObject.put("dealer_id", dealer.getId());
                    if (null != dealer.getLevel() && null != jsonObject.getString("dealer_level") 
                    		&& !dealer.getLevel().equals(jsonObject.getString("dealer_level")))
                    {
                    	jsonObject.put("update_dealer_level", dealer.getLevel());
                    	if (null != dealer.getChangeLevelTime())
                    	{
                    		/*if (DateUtils.differentDays(dealer.getChangeLevelTime(), new Date()) < 150)
                    		{
                    			warnMsg.append("经销商不能频繁修改商业级别;");
                    			jsonObject.put("update_change_level_time", true);
                    		}*/
                    	}
                    }
                }
                dealerMap.put(dealerCode, jsonObject);
            }

            if (StringUtils.isNotBlank(warnMsg)) {
                warnMessage.append("第" + (i+2) + "行：" + warnMsg + "<br/>");
            }

            if (StringUtils.isNotBlank(errorMsg)) {
                errorMessage.append("第" + (i+2) + "行：" + errorMsg + "<br/>");
            }

        }
        result.put("errorMessage", errorMessage.toString());
        result.put("warnMessage", warnMessage.toString());
        result.put("dealerMap", dealerMap);
        return result;
    }

    private Area insertArea(String areaName, Long userId) {
        Area area = new Area();
        area.setName(areaName);
        area.setCreateTime(new Date());
        area.setCreateUserId(userId);
        area.save();
        return area;
    }

    private Region insertRegion(Long areaId, String regionName, Long userId) {
        Region region = new Region();
        region.setAreaId(areaId);
        region.setName(regionName);
        region.setCreateTime(new Date());
        region.setCreateUserId(userId);
        region.save();
        return region;
    }
    
    private void moveDataForDealerLevelChange(String dealerId,String dealerLevel)
    {
    	if (StringUtils.isEmpty(dealerId) || StringUtils.isEmpty(dealerLevel))
    	{
    		return;
    	}

    	// "2"级商修改成"1"级商
    	if ("1".equals(dealerLevel))
    	{
    		List<SecondaryDealerInventoryData> twoList = SecondaryDealerInventoryData.dao.find("select * from secondary_dealer_inventory_data where dealer_id =?", dealerId);
    		List<FirstDealerInventoryData> oneList = new ArrayList<FirstDealerInventoryData>();
    		if (null != twoList && !twoList.isEmpty())
    		{
        		for (SecondaryDealerInventoryData twoData : twoList)
        		{
        			FirstDealerInventoryData oneData = new FirstDealerInventoryData();
        			BeanCopier beanCopier = BeanCopier.create(SecondaryDealerInventoryData.class, FirstDealerInventoryData.class, false);
        			beanCopier.copy(twoData, oneData, null);
        			
        			// 设置2表示是复制过来的临时数据
        			oneData.setLevelChangeFlag("2");
        			
        			oneData.setLevelChangeId(oneData.getId());
        			oneData.setId(null);
        			
        			oneList.add(oneData);//oneData.save();
        			
        			// 设置1表示经销商等级变化后，已经无用的数据
        			twoData.delete();
        		}
        		
        		Db.batchSave(oneList, oneList.size());
    		}

    	}
    	// "1"级商修改为"2"级商
    	else if ("2".equals(dealerLevel))
    	{
    		List<FirstDealerInventoryData> oneList = FirstDealerInventoryData.dao.find("select * from first_dealer_inventory_data where dealer_id =?", dealerId);
    		List<SecondaryDealerInventoryData> twoList = new ArrayList<SecondaryDealerInventoryData>();
    		if (null != oneList && !oneList.isEmpty())
    		{
        		for (FirstDealerInventoryData oneData : oneList)
        		{
        			SecondaryDealerInventoryData twoData = new SecondaryDealerInventoryData();
        			BeanCopier beanCopier = BeanCopier.create(FirstDealerInventoryData.class, SecondaryDealerInventoryData.class, false);
        			beanCopier.copy(oneData, twoData, null);
        			
        			// 设置2表示是复制过来的临时数据
        			twoData.setLevelChangeFlag("2");
        			twoData.setLevelChangeId(twoData.getId());
        			twoData.setId(null);
        			twoList.add(twoData);//twoData.save();
        			
        			// 设置1表示经销商等级变化后，已经无用的数据
        			oneData.delete();
        		}
        		
        		Db.batchSave(twoList, twoList.size());
    		}
    	}
    }

}