package com.site.service;

import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.site.base.BaseService;
import com.site.core.model.*;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;
import com.site.utils.LongUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 计算各种值都在这
 *
 * @author zhang.peng802
 */
public class CalculateService extends BaseService {
    public static CalculateService me = new CalculateService();


    /**
     * 计算一级商上月理论库存 （6）
     *
     * @param id 上月的ID
     */
    public String theoryStockQuantityTask(Long id, boolean isUpload) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        FirstDealerInventoryData lData = FirstDealerInventoryData.dao.findById(id);
        Long oldStockNum = lData.getActualStockQuantity();
        Long oldTheoryStockNum = lData.getTheoryStockQuantity();

        //这里就有可能是上个月的还是这个月的数据
        if (openTimeStep.getYearAndMonth().equals(lData.getMonth())) {
            //更新的就是本月数据
            return "";
        }

        if (openTimeStep.getYearAndMonth() < lData.getMonth()) {
            //更新的就是下月的预估数据
            return "";
        }

        //上上月实际库存+上月实际进货量-上月实际销售量 = 上月理论库存
        Integer month = lData.getMonth();
        String yearAndMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        FirstDealerInventoryData llData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", lData.getDealerId(), lData.getProductId(), yearAndMonth);
        BigDecimal ssySjKc = BigDecimal.ZERO;
        if (llData != null) {
            ssySjKc = llData.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(llData.getActualStockQuantity());
        }

        BigDecimal sySjJh = BigDecimal.ZERO;
        sySjJh = lData.getActualPurchaseQuantity() == null ? BigDecimal.ZERO : new BigDecimal(lData.getActualPurchaseQuantity());

        BigDecimal sySjXs = BigDecimal.ZERO;
        if (lData.getActualSalesQuantityHeadOffice() != null) {
            sySjXs = new BigDecimal(lData.getActualSalesQuantityHeadOffice());
        } else {
            sySjXs = lData.getActualSalesQuantity() == null ? BigDecimal.ZERO : new BigDecimal(lData.getActualSalesQuantity());
        }

        BigDecimal syLlKc = ssySjKc.add(sySjJh).subtract(sySjXs);

        lData.setTheoryStockQuantity(syLlKc.longValue());
        if (!isUpload && openTimeStep.getStep().equals(1)) {
            //总部如果上传了数据的，那么这个也不能一起同步
            lData.setOriginalTheoryStockQuantity(syLlKc.longValue());
        }

        //如果实际库存为空、被更新前的实际库存和理论库存一致，则全量覆盖
        if (oldStockNum == null || oldStockNum.equals(oldTheoryStockNum)) {
            lData.setActualStockQuantity(syLlKc.longValue());
        }

        lData.update();
        return syLlKc.toString();
    }

    /**
     * 计算二级商上月理论库存 （6）
     * 注意：这里是随着一级商第一个锁定期一起上传，这时候可能二级商还在第一个开放期
     *
     * @param id 上月的ID
     */
    public String theoryStockQuantitySecondLevelTask(Long id) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(2);

        //上个月数据
        SecondaryDealerInventoryData lData = SecondaryDealerInventoryData.dao.findById(id);
        Long oldStockNum = lData.getActualStockQuantity();
        Long oldTheoryStockNum = lData.getTheoryStockQuantity();
        //这里就有可能是上个月的还是这个月的数据
        if (openTimeStep.getYearAndMonth().equals(lData.getMonth())) {
            //更新的就是本月数据
            return "";
        }

        if (openTimeStep.getYearAndMonth() < lData.getMonth()) {
            //更新的就是下月的预估数据
            return "";
        }

        if (openTimeStep.getYearAndMonth() > lData.getMonth()) {
            //更新的就是上月的预估数据,就继续往下走
        }


        //上上月实际库存+上月实际进货量-总部上传的上月实际销售量=上月理论库存
        Integer month = lData.getMonth();
        String yearAndMonth = DateUtils.getYearAndMonth(month + "", "yyyyMM", -1);
        SecondaryDealerInventoryData llData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", lData.getDealerId(), lData.getProductId(), yearAndMonth);

        //上上月实际库存
        BigDecimal ssySjKc = BigDecimal.ZERO;
        if (llData != null) {
            ssySjKc = llData.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(llData.getActualStockQuantity());
        }

        //上上月实际进货
        BigDecimal sySjJh = BigDecimal.ZERO;
        if (lData.getActualPurchaseQuantity() != null) {
            sySjJh = new BigDecimal(lData.getActualPurchaseQuantity());
        }

        //上上月实际销售
        BigDecimal sySjXs = BigDecimal.ZERO;
        if (lData.getActualSalesQuantityHeadOffice() != null) {
            sySjXs = new BigDecimal(lData.getActualSalesQuantityHeadOffice());
        } else {
            sySjXs = lData.getActualSalesQuantity() == null ? BigDecimal.ZERO : new BigDecimal(lData.getActualSalesQuantity());
        }

        if (ssySjKc == null || sySjJh == null || sySjXs == null) {
            return "";
        }
        BigDecimal syLlKc = ssySjKc.add(sySjJh).subtract(sySjXs);
        lData.setTheoryStockQuantity(syLlKc.longValue());
        //如果实际库存为空、被更新前的实际库存和理论库存一致，则全量覆盖
        if (oldStockNum == null || oldStockNum.equals(oldTheoryStockNum)) {
            lData.setActualStockQuantity(syLlKc.longValue());
        }

        lData.update();
        return syLlKc.toString();
    }

    /**
     * 计算库上月存天数 （8）保留2位小数
     *
     * @param id 上月的id
     * @return
     */
    public BigDecimal stockDayLastMonth(Long id) {
        //上月实际库存*90/((上上上月实际销售+上上月实际销售+上月实际销售))

        /**
         * 公式除数部分的数组
         */
        List<BigDecimal> totalCount = new ArrayList<>();


        //上个月的数据
        FirstDealerInventoryData updateDate = FirstDealerInventoryData.dao.findById(id);
        BigDecimal actualSalesQuantity_l = updateDate.getActualSalesQuantity() == null ? null : new BigDecimal(updateDate.getActualSalesQuantity());
        if (actualSalesQuantity_l != null) totalCount.add(actualSalesQuantity_l);

        BigDecimal actualStockQuantity_l = updateDate.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getActualStockQuantity());

        //上上个月的实际销售
        String ll_year_month = DateUtils.getYearAndMonth(updateDate.getMonth().toString(), "yyyyMM", -1);
        FirstDealerInventoryData lastLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), new Integer(ll_year_month));
        BigDecimal actualSalesQuantity_ll;
        if (lastLastMonthData != null) {
            actualSalesQuantity_ll = lastLastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_ll != null) totalCount.add(actualSalesQuantity_ll);
        }

        //上上上个月的实际销售
        String lll_year_month = DateUtils.getYearAndMonth(updateDate.getMonth().toString(), "yyyyMM", -2);
        FirstDealerInventoryData lastLastLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), Integer.valueOf(lll_year_month));
        //默认等于后一个月的
        BigDecimal actualSalesQuantity_lll;
        if (lastLastLastMonthData != null) {
            actualSalesQuantity_lll = lastLastLastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_lll != null) totalCount.add(actualSalesQuantity_lll);
        }

        BigDecimal add = genThreeMonthTotal(totalCount);
        BigDecimal decimal = BigDecimal.ZERO;
        if (add.compareTo(BigDecimal.ZERO) != 0) {
            decimal = actualStockQuantity_l.multiply(new BigDecimal(90)).divide(add, 1, BigDecimal.ROUND_HALF_UP);
        }

        updateDate.setInventoryDay(decimal);
        updateDate.update();
        return decimal;
    }

    /**
     * 计算库上月存天数 二级商（8）保留2位小数
     *
     * @param id 上月的id
     * @return
     */
    public BigDecimal stockDaySecondLevelLastMonth(Long id) {
        //上月实际库存*90/((上上上月实际销售+上上月实际销售+上月实际销售))

        /**
         * 公式除数部分的数组
         */
        List<BigDecimal> totalCount = new ArrayList<>();

        //上个月的数据
        SecondaryDealerInventoryData updateDate = SecondaryDealerInventoryData.dao.findById(id);
        BigDecimal actualSalesQuantity_l = updateDate.getActualSalesQuantity() == null ? null : new BigDecimal(updateDate.getActualSalesQuantity());
        if (actualSalesQuantity_l != null) totalCount.add(actualSalesQuantity_l);

        BigDecimal actualStockQuantity_l = updateDate.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getActualStockQuantity());

        //上上个月的实际销售
        String ll_year_month = DateUtils.getYearAndMonth(updateDate.getMonth().toString(), "yyyyMM", -1);
        SecondaryDealerInventoryData lastLastMonthData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), new Integer(ll_year_month));
        BigDecimal actualSalesQuantity_ll;
        if (lastLastMonthData != null) {
            actualSalesQuantity_ll = lastLastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_ll != null) totalCount.add(actualSalesQuantity_ll);
        }

        //上上上个月的实际销售
        String lll_year_month = DateUtils.getYearAndMonth(updateDate.getMonth().toString(), "yyyyMM", -2);
        SecondaryDealerInventoryData lastLastLastMonthData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), Integer.valueOf(lll_year_month));
        BigDecimal actualSalesQuantity_lll;
        if (lastLastLastMonthData != null) {
            actualSalesQuantity_lll = lastLastLastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_lll != null) totalCount.add(actualSalesQuantity_lll);
        }

        BigDecimal add = genThreeMonthTotal(totalCount);
        BigDecimal decimal = BigDecimal.ZERO;
        if (add.compareTo(BigDecimal.ZERO) != 0) {
            decimal = actualStockQuantity_l.multiply(new BigDecimal(90)).divide(add, 1, BigDecimal.ROUND_HALF_UP);
        }

        updateDate.setInventoryDay(decimal);
        updateDate.update();
        return decimal;
    }

    /**
     * 计算当月参照库存 （13）
     *
     * @return
     */
    public BigDecimal stockNum(Long id) {
        //上月实际库存+本月进货预估-本月销售预估
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        //本月的数据
        FirstDealerInventoryData updateDate = FirstDealerInventoryData.dao.findById(id);
        BigDecimal planPurchaseQuantity = updateDate.getPlanPurchaseQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getPlanPurchaseQuantity());
        BigDecimal planSalesQuantity = updateDate.getPlanSalesQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getPlanSalesQuantity());

        //上个月的实际库存
        int lll_year_month = DateUtils.getYearAndMonth(openTimeStep, -1);
        FirstDealerInventoryData lastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), lll_year_month);
        BigDecimal actualStockQuantity = BigDecimal.ZERO;
        if (lastMonthData != null) {
            actualStockQuantity = lastMonthData.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(lastMonthData.getActualStockQuantity());
        }

        BigDecimal subtract = actualStockQuantity.add(planPurchaseQuantity).subtract(planSalesQuantity);

        //这是参照库存
        updateDate.setRefStockQuantity(subtract.longValue());
        updateDate.update();
        return subtract;
    }

    /**
     * 计算本月参照库存天数 （14）保留2位小数
     * 库存天数的计算公式：（本月库存*90）/(上上月实际销售+上月实际销售+本月销售预估)
     *
     * @param id 本月的id
     * @return
     */
    public BigDecimal stockDayNowMonth(Long id) {
        //  （本月库存*90）/(上上月实际销售+上月实际销售+本月销售预估)
        /**
         * 公式除数部分的数组
         */
        List<BigDecimal> totalCount = new ArrayList<>();

        //如果没有值，就取后一个月的值
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);

        FirstDealerInventoryData updateDate = FirstDealerInventoryData.dao.findById(id);

        //本月库存
//        BigDecimal actualStockQuantity = updateDate.getActualStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getActualStockQuantity());
        BigDecimal actualStockQuantity = updateDate.getRefStockQuantity() == null ? BigDecimal.ZERO : new BigDecimal(updateDate.getRefStockQuantity());

        //本月销售预估
        BigDecimal planSalesQuantity = updateDate.getPlanSalesQuantity() == null ? null : new BigDecimal(updateDate.getPlanSalesQuantity());
        if (planSalesQuantity != null) totalCount.add(planSalesQuantity);

        int l_year_month = DateUtils.getYearAndMonth(openTimeStep, -1);
        FirstDealerInventoryData lastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), l_year_month);
        //上月实际销售
        BigDecimal actualSalesQuantity_l;
        if (lastMonthData != null) {
            actualSalesQuantity_l = lastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_l != null) totalCount.add(actualSalesQuantity_l);
        }


        int ll_year_month = DateUtils.getYearAndMonth(openTimeStep, -2);
        FirstDealerInventoryData lastLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", updateDate.getDealerId(), updateDate.getProductId(), ll_year_month);
        //上上月实际销售
        BigDecimal actualSalesQuantity_ll;
        if (lastLastMonthData != null) {
            actualSalesQuantity_ll = lastLastMonthData.getCustomActualSalesQuantity();
            if (actualSalesQuantity_ll != null) totalCount.add(actualSalesQuantity_ll);
        }

        BigDecimal add = genThreeMonthTotal(totalCount);

        BigDecimal divide = BigDecimal.ZERO;
        if (add.compareTo(BigDecimal.ZERO) != 0) {
            divide = actualStockQuantity.multiply(new BigDecimal(90)).divide(add, 1, BigDecimal.ROUND_HALF_UP);
        }

        updateDate.setRefInventoryDay(divide);
        updateDate.update();
        return divide;
    }

    /**
     * 计算下月逻辑分配采购量 （9）保留2位小数
     *
     * @return
     */
    public void purposeProductNextMonth() {
        List<Product> products = Product.dao.find("select * from product where is_delete = '0'");
        if (products.isEmpty()) {
            return;
        }

        for (Product product : products) {
            calculate(product);
        }
    }

    /**
     * 计算下月逻辑分配采购量 ,预备
     *
     * @return
     */
    public void purposeProductNextMonth(Long productId, int yearMonth, Integer num) {
        Product product = Product.dao.findById(productId);
        if (product == null) {
            return;
        }
        calculate2(product, yearMonth, new BigDecimal(num));
    }

    private void calculate(Product product) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        ArrayList<Integer> arrayList = Lists.newArrayList(DateUtils.getYearAndMonth(openTimeStep, -1), DateUtils.getYearAndMonth(openTimeStep, -2), DateUtils.getYearAndMonth(openTimeStep, 0));
        String join = StringUtils.join(arrayList, ",");

        //全国下个月销售 = 前三个月销售的平均值---》average下前三个月的实际销售数量
        BigDecimal saleTotalNumNextMonth = getAvgLast3MonthSale(product.getId(), openTimeStep);

        //全国下个月理论库存天数=（当月实际库存总数 + 下月采购量 - 下月销售）/下月销售 * 30
        BigDecimal stockDayNum = getNextMonthStockDayNum(saleTotalNumNextMonth, openTimeStep, product.getId(), null);

        Map<Long, BigDecimal> nextMonthPurposeNum = new HashMap();
        //每一家客户下月进货量 = 全国下月库存天数 * 该客户近三个月的平均销售/30 - 该客户当月库存 + 该客户下月销售

        List<Dealer> dealerList = Dealer.dao.find("SELECT b.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id WHERE b.`level` = 1 AND a.product_id = ? ", product.getId());
        //每一家客户下月进货量的总和
        BigDecimal totalCalculateNum = BigDecimal.ZERO;
        for (Dealer dealer : dealerList) {
            //--该客户近三个月的平均销售
//            Dealer avgSales = Dealer.dao.findFirst("SELECT TRUNCATE(SUM(actual_sales_quantity_head_office) / COUNT(*), 2) AS avg_sales FROM ( SELECT CASE  WHEN actual_sales_quantity_head_office IS NULL THEN plan_sales_quantity ELSE actual_sales_quantity_head_office END AS actual_sales_quantity_head_office FROM first_dealer_inventory_data WHERE (product_id = " + product.getId() + " AND `month` IN (" + join + ") AND dealer_id = " + dealer.getId() + ") ) p;");
//            BigDecimal avg_sales = avgSales.get("avg_sales") == null ? BigDecimal.ZERO : avgSales.get("avg_sales");

            //当月数据
            FirstDealerInventoryData dealerThisMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), openTimeStep.getYearAndMonth());
            BigDecimal devideNum = BigDecimal.valueOf(3);
            if (dealerThisMonthData == null) {
                continue;
            }
            //上月数据
            FirstDealerInventoryData dealerLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, -1));
            if (dealerLastMonthData == null) {
//                devideNum = devideNum.subtract(BigDecimal.ONE);
                dealerLastMonthData = new FirstDealerInventoryData();
                dealerLastMonthData.setActualSalesQuantity(0L);
            } else {
                if (LongUtil.isNotEmpty(dealerLastMonthData.getActualSalesQuantityHeadOffice())) {
                    dealerLastMonthData.setActualSalesQuantity(dealerLastMonthData.getActualSalesQuantityHeadOffice());
                } else {
                    if (LongUtil.isEmpty(dealerLastMonthData.getActualSalesQuantity())) {
                        dealerLastMonthData.setActualSalesQuantity(0L);
                    }
                }
            }

            //上上月数据
            FirstDealerInventoryData dealerLastLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, -2));
            if (dealerLastLastMonthData == null) {
//                devideNum = devideNum.subtract(BigDecimal.ONE);
                dealerLastLastMonthData = new FirstDealerInventoryData();
                dealerLastLastMonthData.setActualSalesQuantity(0L);
            } else {
                if (LongUtil.isNotEmpty(dealerLastLastMonthData.getActualSalesQuantityHeadOffice())) {
                    dealerLastLastMonthData.setActualSalesQuantity(dealerLastLastMonthData.getActualSalesQuantityHeadOffice());
                } else {
                    if (LongUtil.isEmpty(dealerLastLastMonthData.getActualSalesQuantity())) {
                        dealerLastLastMonthData.setActualSalesQuantity(0L);
                    }
                }
            }

            if (dealerThisMonthData.getPlanSalesQuantity() == null) {
                dealerThisMonthData.setPlanSalesQuantity(0L);
            }

            BigDecimal decimal = new BigDecimal(dealerThisMonthData.getPlanSalesQuantity()).add(new BigDecimal(dealerLastMonthData.getActualSalesQuantity())).add(new BigDecimal(dealerLastLastMonthData.getActualSalesQuantity()));
            //客户下月销售 =近三个月的均值= （当月预估+上月销售+上上月销售）/ 3
            BigDecimal nextMonthSale = decimal.divide(devideNum, 2, BigDecimal.ROUND_UP);

            //客户本月库存
            BigDecimal actualStockQuantity = new BigDecimal(dealerThisMonthData.getRefStockQuantity() == null ? 0L : dealerThisMonthData.getRefStockQuantity());
            if (actualStockQuantity == null) {
                dealerThisMonthData.setActualStockQuantity(0L);
                actualStockQuantity = BigDecimal.ZERO;
            }

            //开始带入公式计算  全国下月库存天数 * 该客户近三个月的平均销售/30 - 该客户当月库存 + 该客户下月销售
            BigDecimal divide = stockDayNum.multiply(nextMonthSale).divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_UP);
            BigDecimal bigDecimal = divide.subtract(actualStockQuantity).add(nextMonthSale);
            BigDecimal add = bigDecimal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : bigDecimal;
            BigDecimal scale = add.setScale(0, BigDecimal.ROUND_HALF_DOWN);
            if (scale.compareTo(BigDecimal.ZERO) != 0) {
                nextMonthPurposeNum.put(dealer.getId(), scale);
                totalCalculateNum = totalCalculateNum.add(scale);
            }
        }


        //按照下月每家客户的购进占比，分配全国采购总量
        PurchaseTotalProduct nextMonthPurchaseTotalNum = PurchaseTotalProduct.dao.findFirst("select * from purchase_total_product where product_id = ? and month = ?", product.getId(), DateUtils.getYearAndMonth(openTimeStep, 1));
        if (nextMonthPurchaseTotalNum == null) {
            return;
        }
        BigDecimal totalNumNum = nextMonthPurchaseTotalNum.getNum();

        BigDecimal finalTotalCalculateNum = totalCalculateNum;
        BigDecimal total_xy_lj_fp_cgl = BigDecimal.ZERO;
        User zb = User.dao.findFirst("select * from user where role_name = '总部'");
        for (Long id : nextMonthPurposeNum.keySet()) {
            BigDecimal num = nextMonthPurposeNum.get(id);
            BigDecimal xy_lj_fp_cgl = BigDecimal.ZERO;
            if (finalTotalCalculateNum.compareTo(BigDecimal.ZERO) != 0) {
                xy_lj_fp_cgl = totalNumNum.multiply(num.divide(finalTotalCalculateNum, 10, BigDecimal.ROUND_DOWN)).setScale(0, BigDecimal.ROUND_DOWN);
            }
            total_xy_lj_fp_cgl = total_xy_lj_fp_cgl.add(xy_lj_fp_cgl);
            //下月逻辑分配采购量，需要插入数据了

            //这里要看下是否已经被插入了
            Dealer dealer = Dealer.dao.findById(id);
            FirstDealerInventoryData inventoryData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id= ? and product_id =? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, 1));
            if (inventoryData != null) {
                inventoryData.setPurchaseQuantity(xy_lj_fp_cgl);
                inventoryData.setUpdateTime(new Date());
                inventoryData.update();
                continue;
            }
            Record record = new Record();
            record.set("area_id", dealer.getAreaId());
            record.set("region_id", dealer.getRegionId());
            record.set("area_manager_user_id", dealer.getAreaManagerUserId());
            record.set("business_manager_user_id", dealer.getBusinessManagerUserId());
            record.set("dealer_id", dealer.getId());
            record.set("month", DateUtils.getYearAndMonth(openTimeStep, 1));
            record.set("product_id", product.getId());
            record.set("status", "0");
            record.set("create_time", new Date());
            record.set("create_user_id", zb.getId());
            record.set("update_time", new Date());
            record.set("update_user_id", zb.getId());
            record.set("purchase_quantity", new BigDecimal(xy_lj_fp_cgl.intValue()));
            Db.save("first_dealer_inventory_data", record);
        }
    }


    private void calculate2(Product product, int yearMonth, BigDecimal totalNum) {
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        openTimeStep.setYearAndMonth(yearMonth);
        int i = yearMonth + 1;
        int update = Db.update("UPDATE first_dealer_inventory_data set purchase_quantity =null where `month` =" + i + " and  product_id =" + product.getId() + "");

        //全国下个月销售 = 前三个月销售的平均值---》average下前三个月的实际销售数量
        BigDecimal saleTotalNumNextMonth = getAvgLast3MonthSale(product.getId(), openTimeStep);

        //全国下个月理论库存天数=（当月实际库存总数 + 下月采购量 - 下月销售）/下月销售 * 30
        BigDecimal stockDayNum = getNextMonthStockDayNum(saleTotalNumNextMonth, openTimeStep, product.getId(), totalNum);

        Map<Long, BigDecimal> nextMonthPurposeNum = new HashMap();
        //每一家客户下月进货量 = 全国下月库存天数 * 该客户近三个月的平均销售/30 - 该客户当月库存 + 该客户下月销售

        List<Dealer> dealerList = Dealer.dao.find("SELECT b.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id WHERE b.`level` = 1 AND a.product_id = ? ", product.getId());
        //每一家客户下月进货量的总和
        BigDecimal totalCalculateNum = BigDecimal.ZERO;
        for (Dealer dealer : dealerList) {
            //--该客户近三个月的平均销售
//            Dealer avgSales = Dealer.dao.findFirst("SELECT TRUNCATE(SUM(actual_sales_quantity_head_office) / COUNT(*), 2) AS avg_sales FROM ( SELECT CASE  WHEN actual_sales_quantity_head_office IS NULL THEN actual_sales_quantity ELSE actual_sales_quantity_head_office END AS actual_sales_quantity_head_office FROM first_dealer_inventory_data WHERE (product_id = " + product.getId() + " AND `month` IN (" + join + ") AND dealer_id = " + dealer.getId() + " ) ) p;");
//            BigDecimal avg_sales = avgSales.get("avg_sales") == null ? BigDecimal.ZERO : avgSales.get("avg_sales");

            //当月数据
            FirstDealerInventoryData dealerThisMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), openTimeStep.getYearAndMonth());
            BigDecimal devideNum = BigDecimal.valueOf(3);
            if (dealerThisMonthData == null) {
                continue;
            }
            //上月数据
            FirstDealerInventoryData dealerLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, -1));
            if (dealerLastMonthData == null) {
                dealerLastMonthData = new FirstDealerInventoryData();
                dealerLastMonthData.setActualSalesQuantity(0L);
            } else {
                if (LongUtil.isNotEmpty(dealerLastMonthData.getActualSalesQuantityHeadOffice())) {
                    dealerLastMonthData.setActualSalesQuantity(dealerLastMonthData.getActualSalesQuantityHeadOffice());
                } else {
                    if (LongUtil.isEmpty(dealerLastMonthData.getActualSalesQuantity())) {
                        dealerLastMonthData.setActualSalesQuantity(0L);
                    }
                }
            }

            //上上月数据
            FirstDealerInventoryData dealerLastLastMonthData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id =? and product_id=? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, -2));
            if (dealerLastLastMonthData == null) {
                dealerLastLastMonthData = new FirstDealerInventoryData();
                dealerLastLastMonthData.setActualSalesQuantity(0L);
            } else {
                if (LongUtil.isNotEmpty(dealerLastLastMonthData.getActualSalesQuantityHeadOffice())) {
                    dealerLastLastMonthData.setActualSalesQuantity(dealerLastLastMonthData.getActualSalesQuantityHeadOffice());
                } else {
                    if (LongUtil.isEmpty(dealerLastLastMonthData.getActualSalesQuantity())) {
                        dealerLastLastMonthData.setActualSalesQuantity(0L);
                    }
                }
            }

            if (dealerThisMonthData.getPlanSalesQuantity() == null) {
                dealerThisMonthData.setPlanSalesQuantity(0L);
            }

            BigDecimal decimal = new BigDecimal(dealerThisMonthData.getPlanSalesQuantity()).add(new BigDecimal(dealerLastMonthData.getActualSalesQuantity())).add(new BigDecimal(dealerLastLastMonthData.getActualSalesQuantity()));
            //客户下月销售 =近三个月的均值= （当月预估+上月销售+上上月销售）/ 3
            BigDecimal nextMonthSale = decimal.divide(devideNum, 2, BigDecimal.ROUND_UP);

            //客户本月库存
            BigDecimal actualStockQuantity = new BigDecimal(dealerThisMonthData.getRefStockQuantity() == null ? 0L : dealerThisMonthData.getRefStockQuantity());
            if (actualStockQuantity == null) {
                dealerThisMonthData.setRefStockQuantity(0L);
                actualStockQuantity = BigDecimal.ZERO;
            }

            //开始带入公式计算  全国下月库存天数 * 该客户近三个月的平均销售/30 - 该客户当月库存 + 该客户下月销售
            BigDecimal divide = stockDayNum.multiply(nextMonthSale).divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_UP);
            BigDecimal bigDecimal = divide.subtract(actualStockQuantity).add(nextMonthSale);
            BigDecimal add = bigDecimal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : bigDecimal;
            BigDecimal scale = add.setScale(0, BigDecimal.ROUND_HALF_DOWN);
            if (scale.compareTo(BigDecimal.ZERO) != 0) {
                nextMonthPurposeNum.put(dealer.getId(), scale);
                totalCalculateNum = totalCalculateNum.add(scale);
            }
        }


        //按照下月每家客户的购进占比，分配全国采购总量
        BigDecimal totalNumNum = BigDecimal.ZERO;
        if (totalNum == null) {
            PurchaseTotalProduct nextMonthPurchaseTotalNum = PurchaseTotalProduct.dao.findFirst("select * from purchase_total_product where product_id = ? and month = ?", product.getId(), DateUtils.getYearAndMonth(openTimeStep, 1));
            if (nextMonthPurchaseTotalNum == null) {
                return;
            }
            totalNumNum = nextMonthPurchaseTotalNum.getNum();
        } else {
            totalNumNum = totalNum;
        }

        BigDecimal finalTotalCalculateNum = totalCalculateNum;
        BigDecimal total_xy_lj_fp_cgl = BigDecimal.ZERO;
        User zb = User.dao.findFirst("select * from user where role_name = '总部'");
        for (Long id : nextMonthPurposeNum.keySet()) {
            BigDecimal num = nextMonthPurposeNum.get(id);
            BigDecimal xy_lj_fp_cgl = BigDecimal.ZERO;
            if (finalTotalCalculateNum.compareTo(BigDecimal.ZERO) != 0) {
                xy_lj_fp_cgl = totalNumNum.multiply(num.divide(finalTotalCalculateNum, 10, BigDecimal.ROUND_DOWN)).setScale(0, BigDecimal.ROUND_DOWN);
            }
            total_xy_lj_fp_cgl = total_xy_lj_fp_cgl.add(xy_lj_fp_cgl);
            //下月逻辑分配采购量，需要插入数据了

            //这里要看下是否已经被插入了
            Dealer dealer = Dealer.dao.findById(id);
            FirstDealerInventoryData inventoryData = FirstDealerInventoryData.dao.findFirst("select * from first_dealer_inventory_data where dealer_id= ? and product_id =? and month = ?", dealer.getId(), product.getId(), DateUtils.getYearAndMonth(openTimeStep, 1));
            if (inventoryData != null) {
                inventoryData.setPurchaseQuantity(xy_lj_fp_cgl);
                inventoryData.setUpdateTime(new Date());
                inventoryData.update();
                continue;
            }
            Record record = new Record();
            record.set("area_id", dealer.getAreaId());
            record.set("region_id", dealer.getRegionId());
            record.set("area_manager_user_id", dealer.getAreaManagerUserId());
            record.set("business_manager_user_id", dealer.getBusinessManagerUserId());
            record.set("dealer_id", dealer.getId());
            record.set("month", DateUtils.getYearAndMonth(openTimeStep, 1));
            record.set("product_id", product.getId());
            record.set("status", "0");
            record.set("create_time", new Date());
            record.set("create_user_id", zb.getId());
            record.set("update_time", new Date());
            record.set("update_user_id", zb.getId());
            record.set("purchase_quantity", new BigDecimal(xy_lj_fp_cgl.intValue()));
            Db.save("first_dealer_inventory_data", record);
        }
    }

    /**
     * 全国下个月销售
     * 计算公式：前三个月销售的平均值---》average下前三个月的实际销售数量
     *
     * @return
     */
    private BigDecimal getAvgLast3MonthSale(Long productId, OpenTimeStep openTimeStep) {
        ArrayList<Integer> arrayList = Lists.newArrayList(DateUtils.getYearAndMonth(openTimeStep, 0), DateUtils.getYearAndMonth(openTimeStep, -1), DateUtils.getYearAndMonth(openTimeStep, -2));
        String join = StringUtils.join(arrayList, ",");

        FirstDealerInventoryData first = FirstDealerInventoryData.dao.findFirst("SELECT AVG(sum) AS avg_num FROM ( SELECT MONTH, SUM(CASE  WHEN actual_sales_quantity_head_office IS NULL THEN plan_sales_quantity ELSE actual_sales_quantity_head_office END) AS sum FROM first_dealer_inventory_data WHERE (product_id = " + productId + " AND `month` IN (" + join + ") ) GROUP BY `month` ) p;");
        BigDecimal saleTotalNumNextMonth = first.get("avg_num") == null ? BigDecimal.ZERO : new BigDecimal(first.get("avg_num").toString());
        return saleTotalNumNextMonth;
    }

    /**
     * 全国下月理论库存天数
     * 计算公式：(当月实际库存总数 + 下月采购量 - 下月销售）/下月销售 * 30
     *
     * @return
     */
    private BigDecimal getNextMonthStockDayNum(BigDecimal saleTotalNumNextMonth, OpenTimeStep openTimeStep, Long productId, BigDecimal purposeTotal) {
        BigDecimal stockDayNum = BigDecimal.ZERO;
        //当月实际库存总数
        FirstDealerInventoryData total_stock_num = FirstDealerInventoryData.dao.findFirst("SELECT COUNT(id) AS total_count , IFNULL(SUM(ref_stock_quantity), 0) AS total_stock FROM first_dealer_inventory_data WHERE (product_id = ? AND `month` = ? )", productId, openTimeStep.getYearAndMonth());
        BigDecimal totalStock = total_stock_num.get("total_stock") == null ? BigDecimal.ZERO : new BigDecimal(total_stock_num.get("total_stock").toString());

        BigDecimal purpose_total = purposeTotal;
        if (purpose_total == null) {
            //下月采购量总量
            PurchaseTotalProduct purchaseTotalProduct = PurchaseTotalProduct.dao.findFirst("select * from purchase_total_product where product_id =? and month =?", productId, DateUtils.getYearAndMonth(openTimeStep, 1));
            if (purchaseTotalProduct == null) {
                return stockDayNum;
            }
            purpose_total = purchaseTotalProduct.getNum() == null ? BigDecimal.ZERO : purchaseTotalProduct.getNum();
        }

        //--当月实际库存总数 + 下月采购量 - 下月销售
        //--上面的结果/下月销售 * 30
        if (saleTotalNumNextMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal subtract = totalStock.add(purpose_total).subtract(saleTotalNumNextMonth);
            stockDayNum = subtract.divide(saleTotalNumNextMonth, 10, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(30));
        }
        return stockDayNum;
    }


    /**
     * 按照规则计算三个月的加总
     * 只适用于库存天数的计算
     *
     * @param list
     * @return
     */
    /**
     * 库存天数计算规则中的后三个月加值计算规则
     * <p>
     * 6月     7月    8月    结果          规则
     * 0       0      0       0    三月都为空，则直接置0
     * 0       0      1            8月值 x 3
     * 0       1      0            7月值 x 3
     * 0       1      1            7月和8月的均值 + 7月值 + 8月值
     * 1       0      0            6月值 x 3
     * 1       0      1            6月值 + 6月和8月的均值 + 8月的值
     * 1       1      0            6月值 + 7月值 + 6月和7月均值
     * 1       1      1            6月值 + 7月值 + 8月值
     */
    public BigDecimal genThreeMonthTotal(List<BigDecimal> list) {
        if (list.size() == 0) {
            return BigDecimal.ZERO;
        }

        if (list.size() == 1) {
            return list.get(0).multiply(new BigDecimal(3));
        }

        if (list.size() == 2) {
            BigDecimal a = list.get(0);
            BigDecimal b = list.get(1);
            BigDecimal average = a.add(b).divide(new BigDecimal(2), 1, BigDecimal.ROUND_DOWN);
            return a.add(b).add(average);
        }

        if (list.size() == 3) {
            BigDecimal a = list.get(0);
            BigDecimal b = list.get(1);
            BigDecimal c = list.get(2);
            return a.add(b).add(c);
        }
        return BigDecimal.ZERO;
    }
}
