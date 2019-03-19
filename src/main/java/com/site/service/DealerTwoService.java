package com.site.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.site.core.model.SecondaryDealerInventoryData;
import com.site.core.model.dto.SecondaryDealerDto;
import com.site.utils.BigDecimalUtil;
import com.site.utils.LongUtil;
import com.site.utils.DateUtils;
import com.site.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * SecondaryDealer 管理
 * 描述：
 */
public class DealerTwoService {

    private static final Log log = Log.getLog(DealerTwoService.class);

    public static final DealerTwoService me = new DealerTwoService();
    private final SecondaryDealerInventoryData dao = new SecondaryDealerInventoryData().dao();


    /**
     * 列表-分页
     */
    public Page<SecondaryDealerInventoryData> paginate(int pageNumber, int pageSize) {
        return dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM secondary_dealer_inventory_data  ORDER BY create_time DESC");
    }

    /**
     * 保存
     */
    public void save(SecondaryDealerInventoryData secondaryDealer) {
        secondaryDealer.save();
    }

    /**
     * 根据商务经理 user_id 更新状态为已提交
     *
     * @param bmUserIds 商务经理 user_id
     */
    public void updateStatusByBMUserId(List<Long> bmUserIds, Long userId) {
        String bmUserIdStr = StringUtils.join(bmUserIds, ",");
        Db.update("update secondary_dealer_inventory_data set status='1',update_user_id=? where business_manager_user_id in (?)", bmUserIdStr, userId);
    }

    /**
     * 根据大区经理 user_id 更新状态为已审核
     *
     * @param amUserId 大区经理 user_id
     */
    public void updateStatusByAMUserId(Long amUserId, Long userId) {
        Db.update("update secondary_dealer_inventory_data set status='2',update_user_id=? where area_manager_user_id=?", amUserId, userId);
    }

    /**
     * 查询
     */
    public SecondaryDealerInventoryData findById(int secondaryDealerId) {
        return dao.findFirst("select * from secondary_dealer_inventory_data where id=?", secondaryDealerId);
    }

    /**
     * 删除
     */
    public void delete(int secondaryDealerId) {
        Db.update("delete from secondary_dealer_inventory_data where id=?", secondaryDealerId);
    }

    /**
     * 根据商务经理提交的数据更新数据
     *
     * @return 是否需要填写差异原因
     */
    public boolean updateDataByBmSubmit(SecondaryDealerDto secondaryDealerDto) {
        //上上月实际库存(1)
        Long precedingActStoQuan = LongUtil.isNull(secondaryDealerDto.getPrecedingMonthActualStockQuantity());
        //上上月实际销售
        Long precedingActSalQuan = secondaryDealerDto.getPrecedingMonthActualSalesQuantity();
        //总部上传的上上月实际销售
        Long precedingActSalQuanHeadOffice = secondaryDealerDto.getPrecedingMonthActualSalesQuantityHeadOffice();
        if (precedingActSalQuanHeadOffice != null) {
            precedingActSalQuan = precedingActSalQuanHeadOffice;
        }
        //上月实际进货(3)
        Long lastActPurQuan = LongUtil.isNull(secondaryDealerDto.getLastMonthActualPurchaseQuantity());
        //上月实际销售(5)
        Long lastActSaleQuan = secondaryDealerDto.getLastMonthActualSalesQuantity();
        //总部上传的上月实际销售
        Long lastActSaleQuanHeadOffice = secondaryDealerDto.getLastMonthActualSalesQuantityHeadOffice();
        if (lastActSaleQuanHeadOffice != null) {
            lastActSaleQuan = lastActSaleQuanHeadOffice;
        }
        //未设置上月理论库存默认值之前的上月实际库存(7)
        Long beforeLastActStoQuan = secondaryDealerDto.getLastMonthActualStockQuantity();
        //当月进货预估(9)
        Long planPurQuan = LongUtil.isNull(secondaryDealerDto.getPlanPurchaseQuantity());
        //当月销售预估(10)
        Long planSalQuan = LongUtil.isNull(secondaryDealerDto.getPlanSalesQuantity());

        //计算上月理论库存(6)
        Long beforeLastTheStoQuan = secondaryDealerDto.getLastMonthTheoryStockQuantity();
        Long lastTheStoQuan = precedingActStoQuan + lastActPurQuan - LongUtil.isNull(lastActSaleQuan);
        secondaryDealerDto.setLastMonthTheoryStockQuantity(lastTheStoQuan);
        if (beforeLastActStoQuan == null || beforeLastActStoQuan.equals(beforeLastTheStoQuan)) {
            secondaryDealerDto.setLastMonthActualStockQuantity(lastTheStoQuan);
        }
        //设置上月理论库存原始值
        secondaryDealerDto.setLastMonthOriginalTheoryStockQuantity(lastTheStoQuan);

        //上月实际库存(7)
        Long lastActStoQuan = LongUtil.isNull(secondaryDealerDto.getLastMonthActualStockQuantity());

        //取出上上上月实际销售
        Long dealerId = secondaryDealerDto.getDealerId();
        Long productId = secondaryDealerDto.getProductId();
        String upMonth = DateUtils.getYearAndMonth(secondaryDealerDto.getMonth().toString(), "yyyyMM", -3);
        SecondaryDealerInventoryData upInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where product_id=? and dealer_id=? and month=?", productId, dealerId, upMonth);
        Long upActualSalesQuanity = null;
        if (upInventoryData != null) {
            upActualSalesQuanity = upInventoryData.getActualSalesQuantity();
            Long upActualSalesQuanityHeadOffice = upInventoryData.getActualSalesQuantityHeadOffice();
            if (upActualSalesQuanityHeadOffice != null) {
                upActualSalesQuanity = upActualSalesQuanityHeadOffice;
            }
        }
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
        BigDecimal lastAverage = caculateAverage(lastMonthList);
        if (BigDecimalUtil.gt(lastAverage, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(lastActStoQuan * 90).divide(lastAverage, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            secondaryDealerDto.setLastMonthInventoryDay(inventoryDay);
        } else {
            secondaryDealerDto.setLastMonthInventoryDay(new BigDecimal(0));
        }

        //计算当月库存(11)
        Long actualStockQuantity = lastActStoQuan + planPurQuan - planSalQuan;
        secondaryDealerDto.setActualStockQuantity(actualStockQuantity);

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
        BigDecimal average = caculateAverage(nowMonthList);
        if (BigDecimalUtil.gt(average, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(actualStockQuantity * 90).divide(average, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            secondaryDealerDto.setInventoryDay(inventoryDay);
        } else {
            secondaryDealerDto.setInventoryDay(new BigDecimal(0));
        }

        //更新当月数据
        SecondaryDealerInventoryData secondaryDealerData = secondaryDealerDto.parseInventoryData();
        Long id = upsertData(secondaryDealerData);
        secondaryDealerDto.setId(id);

        //更新上月进销存数据
        SecondaryDealerInventoryData lastSecondaryDealerData = secondaryDealerDto.parseLastMonthInventoryData();
        upsertData(lastSecondaryDealerData);

        //是否需要填写差异原因
        boolean needEditDiffCause = false;
        String diffCause = secondaryDealerDto.getDiffCause();
        if (!LongUtil.equal(lastTheStoQuan, lastActStoQuan) && StringUtils.isBlank(diffCause)) {
            //TODO 需要填写差异原因
            needEditDiffCause = true;
        }
        return needEditDiffCause;
    }

    /**
     * 根据总部提交的数据更新数据
     *
     * @return 是否与之前理论库存不符
     */
    public Map<String, Boolean> updateDataByBossSubmit(SecondaryDealerDto secondaryDealerDto, boolean setLastMonthOriginalTheoryStockQuantity) {
        //上上月实际库存(1)
        Long precedingActStoQuan = LongUtil.isNull(secondaryDealerDto.getPrecedingMonthActualStockQuantity());
        //上上月实际销售
        Long precedingActSalQuan = secondaryDealerDto.getPrecedingMonthActualSalesQuantity();
        //总部上传的上上月实际销售
        Long precedingActSalQuanHeadOffice = secondaryDealerDto.getPrecedingMonthActualSalesQuantityHeadOffice();
        if (precedingActSalQuanHeadOffice != null) {
            precedingActSalQuan = precedingActSalQuanHeadOffice;
        }
        //上月实际进货(3)
        Long lastActPurQuan = LongUtil.isNull(secondaryDealerDto.getLastMonthActualPurchaseQuantity());
        //上月实际销售(5)
        Long lastActSaleQuan = secondaryDealerDto.getLastMonthActualSalesQuantity();
        //总部上传的上月实际销售
        Long lastActSaleQuanHeadOffice = secondaryDealerDto.getLastMonthActualSalesQuantityHeadOffice();
        if (lastActSaleQuanHeadOffice != null) {
            lastActSaleQuan = lastActSaleQuanHeadOffice;
        }
        //未设置上月理论库存默认值之前的上月实际库存(7)
        Long beforeLastActStoQuan = secondaryDealerDto.getLastMonthActualStockQuantity();
        //当月进货预估(9)
        Long planPurQuan = LongUtil.isNull(secondaryDealerDto.getPlanPurchaseQuantity());
        //当月销售预估(10)
        Long planSalQuan = LongUtil.isNull(secondaryDealerDto.getPlanSalesQuantity());

        //计算上月理论库存(6)
        Long beforeLastTheStoQuan = secondaryDealerDto.getLastMonthTheoryStockQuantity();
        Long lastTheStoQuan = precedingActStoQuan + lastActPurQuan - LongUtil.isNull(lastActSaleQuan);
        secondaryDealerDto.setLastMonthTheoryStockQuantity(lastTheStoQuan);
        if (beforeLastActStoQuan == null || beforeLastActStoQuan.equals(beforeLastTheStoQuan)) {
            secondaryDealerDto.setLastMonthActualStockQuantity(lastTheStoQuan);
        }
        //设置上月理论库存原始值
        if (setLastMonthOriginalTheoryStockQuantity) {
            secondaryDealerDto.setLastMonthOriginalTheoryStockQuantity(lastTheStoQuan);
        }

        //上月实际库存(7)
        Long lastActStoQuan = LongUtil.isNull(secondaryDealerDto.getLastMonthActualStockQuantity());

        //取出上上上月实际销售
        Long dealerId = secondaryDealerDto.getDealerId();
        Long productId = secondaryDealerDto.getProductId();
        String upMonth = DateUtils.getYearAndMonth(secondaryDealerDto.getMonth().toString(), "yyyyMM", -3);
        SecondaryDealerInventoryData upInventoryData = SecondaryDealerInventoryData.dao.findFirst("select * from secondary_dealer_inventory_data where product_id=? and dealer_id=? and month=?", productId, dealerId, upMonth);
        Long upActualSalesQuanity = null;
        if (upInventoryData != null) {
            upActualSalesQuanity = upInventoryData.getActualSalesQuantity();
            Long upActualSalesQuanityHeadOffice = upInventoryData.getActualSalesQuantityHeadOffice();
            if (upActualSalesQuanityHeadOffice != null) {
                upActualSalesQuanity = upActualSalesQuanityHeadOffice;
            }
        }
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
        BigDecimal lastAverage = caculateAverage(lastMonthList);
        if (BigDecimalUtil.gt(lastAverage, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(lastActStoQuan * 90).divide(lastAverage, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            secondaryDealerDto.setLastMonthInventoryDay(inventoryDay);
        } else {
            secondaryDealerDto.setLastMonthInventoryDay(new BigDecimal(0));
        }

        //计算当月库存(11)
        Long actualStockQuantity = lastActStoQuan + planPurQuan - planSalQuan;
        secondaryDealerDto.setActualStockQuantity(actualStockQuantity);

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
        BigDecimal average = caculateAverage(nowMonthList);
        if (BigDecimalUtil.gt(average, 0d)) {
            BigDecimal inventoryDay = new BigDecimal(actualStockQuantity * 90).divide(average, 4, BigDecimal.ROUND_DOWN).setScale(1, BigDecimal.ROUND_DOWN);
            secondaryDealerDto.setInventoryDay(inventoryDay);
        } else {
            secondaryDealerDto.setInventoryDay(new BigDecimal(0));
        }


        //更新当月数据
        SecondaryDealerInventoryData secondaryDealerData = secondaryDealerDto.parseInventoryData();
        Long id = upsertData(secondaryDealerData);
        secondaryDealerDto.setId(id);

        //更新上月进销存数据
        SecondaryDealerInventoryData lastSecondaryDealerData = secondaryDealerDto.parseLastMonthInventoryData();
        upsertData(lastSecondaryDealerData);

        Map<String, Boolean> result = new HashMap<>();
        //是否需要填写差异原因
        boolean needEditDiffCause = false;
        String diffCause = secondaryDealerDto.getDiffCause();
        if (!LongUtil.equal(lastTheStoQuan, lastActStoQuan) && StringUtils.isBlank(diffCause)) {
            //TODO 需要填写差异原因
            needEditDiffCause = true;
        }
        result.put("diffCause", needEditDiffCause);
        //判断是否与之前理论库存不符
        boolean equal = false;
        //之前理论库存
        Long lastOriginTheStoQuan = secondaryDealerDto.getLastMonthOriginalTheoryStockQuantity();
        if (!LongUtil.equal(lastOriginTheStoQuan, lastTheStoQuan)) {
            //TODO 需要标记成红色
            equal = true;
        }
        result.put("diffData", equal);
        return result;
    }

    /**
     * 保存或更新进销存数据
     */
    private Long upsertData(SecondaryDealerInventoryData dealerInventoryData) {
        Long id = dealerInventoryData.getId();
        if (id != null && id > 0) {
            dealerInventoryData.update();
        } else {
            boolean success = dealerInventoryData.save();
            if (!success) {
                updateBySelecteds(dealerInventoryData);
            }
        }
        return dealerInventoryData.getId();
    }

    /**
     * 更新
     */
    public void updateBySelecteds(SecondaryDealerInventoryData dealerInventoryData) {
        JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(dealerInventoryData));

        StringBuilder body = new StringBuilder();

        Set<String> set = data.keySet();

        if (!set.contains("id") || set.size() <= 2) {
            return;
        }

        set.forEach(key -> {
            if ("id".equals(key)) {
                return;
            }

            Object value = data.get(key);
            if (value == null) {
                return;
            }

            //驼峰转下划线
            String parseKey = StringUtil.humpToLine(key);
            if (value instanceof String) {
                body.append(parseKey + "='" + value + "',");
            } else {
                body.append(parseKey + "=" + value + ",");
            }
        });
        body.deleteCharAt(body.length() - 1);

        String head = "update secondary_dealer_inventory_data ";
        String foot = "where month=" + dealerInventoryData.getMonth() + " and product_id=" + dealerInventoryData.getProductId() + " and dealer_id=" + dealerInventoryData.getDealerId();
        String sql = head + "set " + body + " " + foot + ";";

        Db.update(sql);
    }

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
    public BigDecimal caculateAverage(List<Long> list) {
        if (list.size() == 0) {
            return BigDecimal.ZERO;
        }

        if (list.size() == 1) {
            return new BigDecimal(list.get(0) * 3);
        }

        if (list.size() == 2) {
            BigDecimal first = new BigDecimal(list.get(0));
            BigDecimal second = new BigDecimal(list.get(1));
            BigDecimal third = first.add(second).divide(new BigDecimal(2), 1, BigDecimal.ROUND_DOWN);
            return first.add(second).add(third);
        }

        if (list.size() == 3) {
            Long first = list.get(0);
            Long second = list.get(1);
            Long third = list.get(2);
            return new BigDecimal(first + second + third);
        }
        return BigDecimal.ZERO;
    }
}