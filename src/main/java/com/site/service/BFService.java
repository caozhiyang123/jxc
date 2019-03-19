package com.site.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseService;
import com.site.core.model.Dealer;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.Product;
import com.site.core.model.User;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.ExcelUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BFService extends BaseService {
    public static BFService me = new BFService();

    /**
     * 导入系统分配采购量
     * 备用的东西
     *
     * @param file
     * @param loginUser
     */
    public void setXtfpcg(UploadFile file, User loginUser) {
        JSONObject excelResult = null;
        try {
            Map map = new HashMap();
            map.put("mep", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "4", "num"});
            map.put("alm", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "4", "num"});
            map.put("sed21", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "6", "num"});
            map.put("sed42", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "6", "num"});
            map.put("gas", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "6", "num"});
            map.put("lon", new String[]{"dealer_name", "dealer_code", "1", "2", "3", "product_name", "6", "num"});
            excelResult = ExcelUtil.readMultipleExcel(file, map);

            JSONArray mepList = (JSONArray) excelResult.get("mep");
            setWorker(mepList, loginUser);

            JSONArray alm = (JSONArray) excelResult.get("alm");
            setWorker(alm, loginUser);

            JSONArray sed21 = (JSONArray) excelResult.get("sed21");
            setWorker(sed21, loginUser);

            JSONArray sed42 = (JSONArray) excelResult.get("sed42");
            setWorker(sed42, loginUser);

            JSONArray gas = (JSONArray) excelResult.get("gas");
            setWorker(gas, loginUser);

            JSONArray lon = (JSONArray) excelResult.get("lon");
            setWorker(lon, loginUser);

        } catch (Exception e) {

        }
    }

    public void serOrderNum(UploadFile file, User loginUser) throws IOException {
        JSONObject excelResult = null;
        // 选择	业务类型	销售类型	发票号	开票日期   客户简称
        // 客户名称
        // 币种	汇率	销售部门	业务员	合同编码	表体订单号	仓库	存货编码	存货代码	存货名称	规格型号	批号	失效日期	有效期至
        // 数量
        // 报价	含税单价	无税单价	无税金额	税率（%）	税额	价税合计	扣率（%）	扣率2（%）	折扣额	制单人	复核人	账期	到期日	发运方式编码	发运方式
        // 项目名称
        // 收款日期	未回款金额	未回款金额本币	导出次数	复核日期	复核时间	客户权限大类编码	客户权限大类名称	客户编号
        Map map = new HashMap();
        map.put("销售发票列表_周", new String[]{
                        "select",
                        "1", "2", "3", "4", "5",   //5
                        "dealer_name",
                        "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",      //14
                        "num",
                        "21", "22", "23", "24", "25", "26", "27", "28", "28", "30", "31", "32", "33", "34", "35", "36",         //16
                        "product_project_name",
                        "37", "38", "39", "40", "41", "42", "43", "44", "dealer_code"
                }
        );
        excelResult = ExcelUtil.readMultipleExcel(file, map);

        JSONArray thisMonthJH = (JSONArray) excelResult.get("销售发票列表_周");
        if (thisMonthJH.size() == 0) {
            return;
        }

        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        openTimeStep.setYearAndMonth(201807);
        List<Long> haveClearOrderNum = new ArrayList<>();
        for (int i = 0; i < thisMonthJH.size(); i++) {
            JSONArray finalJsonArray = thisMonthJH;
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
                    String dealerName = (String) obj.get("dealer_code");
                    String num = (String) obj.get("num");
                    Long orderNum = Long.valueOf(num);
                    Dealer dealer = Dealer.dao.findFirst("select * from dealer where order_calculate_code = ?", dealerName);
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
        System.out.println("success");
    }


    private void setWorker(JSONArray jsonArray, User loginUser) {
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject mep = (JSONObject) jsonArray.get(i);
                String productProjectName = (String) mep.get("product_name");
                Product product = Product.dao.findFirst("select * from product where  name =?", productProjectName);
                if (product == null) {
                    continue;
                }
                String dealerCode = (String) mep.get("dealer_code");
                Dealer dealer = Dealer.dao.findFirst("select * from dealer where code = ?", dealerCode);
                if (dealer == null) {
                    continue;
                }

                String num = (String) mep.get("num");
                Long orderNum = Long.valueOf(num);

                FirstDealerInventoryData data = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where product_id=? and dealer_id =? and month = ?", product.getId(), dealer.getId(), 201808);
                data = data == null ? FirstDealerInventoryData.init(dealer, loginUser) : data;
                data.setPurchaseQuantity(new BigDecimal(orderNum));
                if (data.getId() == null) {
                    data.setMonth(201808);
                    data.setProductId(product.getId());
                    data.save();
                } else {
                    data.update();
                }
            }
        }
    }


}


