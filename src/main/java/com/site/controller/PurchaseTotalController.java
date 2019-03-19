package com.site.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jfinal.aop.Before;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.site.base.BaseController;
import com.site.core.model.Dealer;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.OpenTimeManage;
import com.site.core.model.Product;
import com.site.core.model.PurchaseTotalProduct;
import com.site.core.model.SecondaryDealerInventoryData;
import com.site.core.model.common.OpenTimeStep;
import com.site.core.model.dto.SecondaryDealerDto;
import com.site.quartz.VersionTask;
import com.site.service.CalculateService;
import com.site.service.ChangeToHistoryService;
import com.site.service.DealerTwoService;
import com.site.service.RoleConfigService;
import com.site.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PurchaseTotalController extends BaseController {

    @Override
    public void index() {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int yearAndMonth = openTimeStep.getYearAndMonth();
        int yyyyMM = Integer.parseInt(DateUtils.getYearAndMonth(yearAndMonth + "", "yyyyMM", 1));
        List<Product> products = Product.dao.find("select a.id ,a.name ,b.num from product a left join purchase_total_product b on a.id =b.product_id and b.month= ?", yyyyMM);
        result.setData(products);
        renderJson(result);
    }

    @Before(Tx.class)
    public void savePurchaseTotal() {
        String data = HttpKit.readData(getRequest());
        List objects = JSON.parseArray(data);
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        int yearAndMonth = openTimeStep.getYearAndMonth();
        int yyyyMM = Integer.parseInt(DateUtils.getYearAndMonth(yearAndMonth + "", "yyyyMM", 1));

        if (!objects.isEmpty()) {
            for (int i = 0; i < objects.size(); i++) {
                Map pro = (Map) objects.get(i);
                Object id = pro.get("id");
                Object num = pro.get("num");
                if (num == null) {
                    continue;
                }
                PurchaseTotalProduct product = PurchaseTotalProduct.dao.findFirst("select * from purchase_total_product where product_id =? and month = ?", id.toString(), yyyyMM);
                if (product == null) {
                    PurchaseTotalProduct purchaseTotalProduct = new PurchaseTotalProduct();
                    purchaseTotalProduct.setProductId(Long.valueOf(id.toString()));
                    purchaseTotalProduct.setNum(new BigDecimal(num.toString()));
                    purchaseTotalProduct.setCreateTime(new Date());
                    purchaseTotalProduct.setCreateUserId(getLoginUser().getId());
                    purchaseTotalProduct.setUpdateTime(new Date());
                    purchaseTotalProduct.setUpdateUserId(getLoginUser().getId());
                    purchaseTotalProduct.setMonth(yyyyMM);
                    purchaseTotalProduct.save();
                } else {
                    product.setNum(new BigDecimal(num.toString()));
                    product.update();
                }
            }
        }
        renderJson(result);
    }

    /**
     * 填好下月采购量总量之后，系统自动计算当月库存（13）和库存天数（14）以及计算出下月逻辑分配采购量（9）
     */
    public void startCalculate() {
        //开始更新开放期状态
        OpenTimeManage thisMonth = OpenTimeManage.dao.findFirst("select * from open_time_manage where `status` ='1' and level =?", 1);
        thisMonth.setNextMonthProductPurposeStatus(1);
        //滞空，代表这进入了计算期，所有人都没有权限进行操作
        thisMonth.setSecondLockEndDay(null);
        thisMonth.setFirstStartDay(null);
        thisMonth.update();

        Thread thread = new Thread(() -> {
            calculate();
        });
        thread.start();
        renderJson(result);
    }

    public void ca() {
        String pid = getPara("pid");
        Integer yyyy = getParaToInt("yyyy");
        Integer num = getParaToInt("num");
        if (StringUtils.isEmpty(pid)) {
            renderJson("不予执行");
            return;
        }
        if (yyyy == null) {
            yyyy = RoleConfigService.me.getCurrentOpenTimeStep(1).getYearAndMonth();
        }
        CalculateService.me.purposeProductNextMonth(Long.valueOf(pid), yyyy, num);

        renderJson("success");
    }

    private void calculate() {
        //这里开始执行任务
        System.out.println("系统开始计算 13 -14 -9");
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        Integer step = openTimeStep.getStep();
        if (4 != step) {
            System.out.println("系统开放期不在第二个锁定期，取消执行");
            return;
        }
        //系统自动计算当月库存（13）和库存天数（14）
        Boolean flag = true;
//        while (flag) {
//            List<FirstDealerInventoryData> dataList = FirstDealerInventoryData.dao.find("select * from first_dealer_inventory_data where status = ? and month = ? limit 0,100", "0", openTimeStep.getYearAndMonth());
//            if (dataList.size() == 0) {
//                flag = false;
//                break;
//            }
//
//            dataList.forEach(k -> {
//                Db.tx(new IAtom() {
//                    @Override
//                    public boolean run() throws SQLException {
//                        CalculateService.me.stockNum(k.getId());
//                        CalculateService.me.stockDayNowMonth(k.getId());
//                        k.setStatus("1");
//                        k.update();
//                        return true;
//                    }
//                });
//            });
//        }

        //最后计算全国分配采购量 （9）
        CalculateService.me.purposeProductNextMonth();

        //开始封存T1商的上个月数据
        ChangeToHistoryService.me.saveFirstLevelData();
        //开始封存T2商的上个月数据
        ChangeToHistoryService.me.saveSecondLevelData();

        //计算完成，更新openTime表
        String yyyyMM = DateUtils.getDate(new Date(), "yyyyMM");
        String lastUpdateData = DateUtils.getYearAndMonth(yyyyMM, 0);
        OpenTimeManage time = OpenTimeManage.dao.findFirst("select * from open_time_manage where `status` ='1' and level =?", 1);
        OpenTimeManage time2 = OpenTimeManage.dao.findFirst("select * from open_time_manage where `status` ='1' and level =?", 2);
        time.setLastMonth(new Integer(lastUpdateData));
        String today = DateUtils.getDate(new Date(), "yyyyMMdd", 0);
        String nextDay = DateUtils.getDate(new Date(), "yyyyMMdd", 1);
        String s1 = today.substring(6, 8).startsWith("0") ? today.substring(7, 8) : today.substring(6, 8);
        String s2 = nextDay.substring(6, 8).startsWith("0") ? nextDay.substring(7, 8) : nextDay.substring(6, 8);
        time.setSecondLockEndDay(new Integer(s1));
        time.setFirstStartDay(new Integer(s2));
        //捎带手更新下 下月商品采购量总量计算状态
        time.setNextMonthProductPurposeStatus(0);
        time.update();

        time2.setLastMonth(new Integer(lastUpdateData));
        time2.setFirstLockEndDay(new Integer(s1));
        time2.setFirstStartDay(new Integer(s2));
        time2.update();
        
        VersionTask.addZeroForT1 = true;
        VersionTask.addZeroForT2 = true;
    }


}
