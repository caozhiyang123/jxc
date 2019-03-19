package com.site.core.model.dto;

import com.jfinal.plugin.activerecord.Model;
import com.site.core.model.Dealer;
import com.site.core.model.SecondaryDealerInventoryData;
import com.site.utils.StringUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SecondaryDealerDto implements Cloneable,Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long lastId;
    private Long areaId;
    private Long regionId;
    private Long areaManagerUserId;
    private Long businessManagerUserId;
    private Long dealerId;
    private Integer month;
    private Integer lastMonth;
    private Long productId;
    private Long precedingMonthActualStockQuantity;
    private Long precedingMonthActualSalesQuantity;
    private Long precedingMonthActualSalesQuantityHeadOffice;
    private Long lastMonthPlanPurchaseQuantity;
    private Long lastMonthActualPurchaseQuantity;
    private Long lastMonthPlanSalesQuantity;
    private Long lastMonthActualSalesQuantity;
    private Long lastMonthActualSalesQuantityHeadOffice;
    private Long lastMonthTheoryStockQuantity;
    private Long lastMonthOriginalTheoryStockQuantity;
    private Long lastMonthActualStockQuantity;
    private BigDecimal lastMonthInventoryDay;
    private Long planPurchaseQuantity;
    private Long planSalesQuantity;
    private Long actualStockQuantity;
    private BigDecimal inventoryDay;
    private String diffCause;
    private Long userId;
    private String column;
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLastId() {
        return lastId;
    }

    public void setLastId(Long lastId) {
        this.lastId = lastId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Long getAreaManagerUserId() {
        return areaManagerUserId;
    }

    public void setAreaManagerUserId(Long areaManagerUserId) {
        this.areaManagerUserId = areaManagerUserId;
    }

    public Long getBusinessManagerUserId() {
        return businessManagerUserId;
    }

    public void setBusinessManagerUserId(Long businessManagerUserId) {
        this.businessManagerUserId = businessManagerUserId;
    }

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getPrecedingMonthActualStockQuantity() {
        return precedingMonthActualStockQuantity;
    }

    public void setPrecedingMonthActualStockQuantity(Long precedingMonthActualStockQuantity) {
        this.precedingMonthActualStockQuantity = precedingMonthActualStockQuantity;
    }

    public Long getPrecedingMonthActualSalesQuantity() {
        return precedingMonthActualSalesQuantity;
    }

    public void setPrecedingMonthActualSalesQuantity(Long precedingMonthActualSalesQuantity) {
        this.precedingMonthActualSalesQuantity = precedingMonthActualSalesQuantity;
    }

    public Long getLastMonthPlanPurchaseQuantity() {
        return lastMonthPlanPurchaseQuantity;
    }

    public void setLastMonthPlanPurchaseQuantity(Long lastMonthPlanPurchaseQuantity) {
        this.lastMonthPlanPurchaseQuantity = lastMonthPlanPurchaseQuantity;
    }

    public Long getLastMonthActualPurchaseQuantity() {
        return lastMonthActualPurchaseQuantity;
    }

    public void setLastMonthActualPurchaseQuantity(Long lastMonthActualPurchaseQuantity) {
        this.lastMonthActualPurchaseQuantity = lastMonthActualPurchaseQuantity;
    }

    public Long getLastMonthPlanSalesQuantity() {
        return lastMonthPlanSalesQuantity;
    }

    public void setLastMonthPlanSalesQuantity(Long lastMonthPlanSalesQuantity) {
        this.lastMonthPlanSalesQuantity = lastMonthPlanSalesQuantity;
    }

    public Long getLastMonthActualSalesQuantity() {
        return lastMonthActualSalesQuantity;
    }

    public void setLastMonthActualSalesQuantity(Long lastMonthActualSalesQuantity) {
        this.lastMonthActualSalesQuantity = lastMonthActualSalesQuantity;
    }

    public Long getLastMonthTheoryStockQuantity() {
        return lastMonthTheoryStockQuantity;
    }

    public void setLastMonthTheoryStockQuantity(Long lastMonthTheoryStockQuantity) {
        this.lastMonthTheoryStockQuantity = lastMonthTheoryStockQuantity;
    }

    public Long getLastMonthActualStockQuantity() {
        return lastMonthActualStockQuantity;
    }

    public void setLastMonthActualStockQuantity(Long lastMonthActualStockQuantity) {
        this.lastMonthActualStockQuantity = lastMonthActualStockQuantity;
    }

    public BigDecimal getLastMonthInventoryDay() {
        return lastMonthInventoryDay;
    }

    public void setLastMonthInventoryDay(BigDecimal lastMonthInventoryDay) {
        this.lastMonthInventoryDay = lastMonthInventoryDay;
    }

    public Long getPlanPurchaseQuantity() {
        return planPurchaseQuantity;
    }

    public void setPlanPurchaseQuantity(Long planPurchaseQuantity) {
        this.planPurchaseQuantity = planPurchaseQuantity;
    }

    public Long getPlanSalesQuantity() {
        return planSalesQuantity;
    }

    public void setPlanSalesQuantity(Long planSalesQuantity) {
        this.planSalesQuantity = planSalesQuantity;
    }

    public Long getActualStockQuantity() {
        return actualStockQuantity;
    }

    public void setActualStockQuantity(Long actualStockQuantity) {
        this.actualStockQuantity = actualStockQuantity;
    }

    public BigDecimal getInventoryDay() {
        return inventoryDay;
    }

    public void setInventoryDay(BigDecimal inventoryDay) {
        this.inventoryDay = inventoryDay;
    }

    public String getDiffCause() {
        return diffCause;
    }

    public void setDiffCause(String diffCause) {
        this.diffCause = diffCause;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getLastMonth() {
        return lastMonth;
    }

    public void setLastMonth(Integer lastMonth) {
        this.lastMonth = lastMonth;
    }

    public Long getLastMonthOriginalTheoryStockQuantity() {
        return lastMonthOriginalTheoryStockQuantity;
    }

    public void setLastMonthOriginalTheoryStockQuantity(Long lastMonthOriginalTheoryStockQuantity) {
        this.lastMonthOriginalTheoryStockQuantity = lastMonthOriginalTheoryStockQuantity;
    }

    public Long getLastMonthActualSalesQuantityHeadOffice() {
        return lastMonthActualSalesQuantityHeadOffice;
    }

    public void setLastMonthActualSalesQuantityHeadOffice(Long lastMonthActualSalesQuantityHeadOffice) {
        this.lastMonthActualSalesQuantityHeadOffice = lastMonthActualSalesQuantityHeadOffice;
    }

    public Long getPrecedingMonthActualSalesQuantityHeadOffice() {
        return precedingMonthActualSalesQuantityHeadOffice;
    }

    public void setPrecedingMonthActualSalesQuantityHeadOffice(Long precedingMonthActualSalesQuantityHeadOffice) {
        this.precedingMonthActualSalesQuantityHeadOffice = precedingMonthActualSalesQuantityHeadOffice;
    }

    public Long setColumnValue() {
        String column = StringUtil.lineToHump(this.column);
        if(Character.isLowerCase(column.charAt(0))) {
            column = Character.toUpperCase(column.charAt(0)) + column.substring(1);
        }

        Object obj = null;
        if ("DiffCause".equals(column)) {
            diffCause = value;
        } else {
            Method fieldGetMet = null;
            try {
                fieldGetMet = SecondaryDealerDto.class.getDeclaredMethod("get" + column);
                obj = fieldGetMet.invoke(this);
                fieldGetMet = SecondaryDealerDto.class.getDeclaredMethod("set" + column, Long.class);
                fieldGetMet.invoke(this, Long.parseLong(value));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return obj == null ? null : (Long) obj;
    }

    public SecondaryDealerInventoryData parseInventoryData() {
        SecondaryDealerInventoryData inventoryData = new SecondaryDealerInventoryData();
        inventoryData.setPlanPurchaseQuantity(planPurchaseQuantity);
        inventoryData.setPlanSalesQuantity(planSalesQuantity);
        inventoryData.setRefStockQuantity(actualStockQuantity);
        inventoryData.setRefInventoryDay(inventoryDay);
        if (id != null && id > 0) {
            inventoryData.setId(id);
            inventoryData.setUpdateUserId(userId);
        } else {
            inventoryData.setAreaId(areaId);
            inventoryData.setRegionId(regionId);
            inventoryData.setDealerId(dealerId);
            inventoryData.setProductId(productId);
            inventoryData.setMonth(month);
            inventoryData.setAreaManagerUserId(areaManagerUserId);
            inventoryData.setBusinessManagerUserId(businessManagerUserId);
            inventoryData.setCreateUserId(userId);
            inventoryData.setCreateTime(new Date());
        }
        return inventoryData;
    }

    public SecondaryDealerInventoryData parseLastMonthInventoryData() {
        SecondaryDealerInventoryData inventoryData = new SecondaryDealerInventoryData();
        inventoryData.setActualPurchaseQuantity(lastMonthActualPurchaseQuantity);
        inventoryData.setActualSalesQuantity(lastMonthActualSalesQuantity);
        inventoryData.setActualSalesQuantityHeadOffice(lastMonthActualSalesQuantityHeadOffice);
        inventoryData.setActualStockQuantity(lastMonthActualStockQuantity);
        inventoryData.setTheoryStockQuantity(lastMonthTheoryStockQuantity);
        inventoryData.setOriginalTheoryStockQuantity(lastMonthOriginalTheoryStockQuantity);
        inventoryData.setInventoryDay(lastMonthInventoryDay);
        inventoryData.setDiffCause(diffCause);
        if (lastId != null && lastId > 0) {
            inventoryData.setId(lastId);
            inventoryData.setUpdateUserId(userId);
        } else {
            inventoryData.setAreaId(areaId);
            inventoryData.setRegionId(regionId);
            inventoryData.setDealerId(dealerId);
            inventoryData.setProductId(productId);
            inventoryData.setMonth(lastMonth);
            inventoryData.setAreaManagerUserId(areaManagerUserId);
            inventoryData.setBusinessManagerUserId(businessManagerUserId);
            inventoryData.setCreateUserId(userId);
            inventoryData.setCreateTime(new Date());
        }
        return inventoryData;
    }

    public void setDealer(Dealer dealer) {
        if (dealer == null) {
            return;
        }

        areaId = dealer.getAreaId();
        regionId = dealer.getRegionId();
        areaManagerUserId = dealer.getAreaManagerUserId();
        businessManagerUserId = dealer.getBusinessManagerUserId();
    }

    /**
     * 设置当月数据
     * @return
     */
    public void setInventoryData(SecondaryDealerInventoryData inventoryData) {
        if (inventoryData == null) {
            return;
        }

        id = inventoryData.getId();
        areaId = inventoryData.getAreaId();
        regionId = inventoryData.getRegionId();
        areaManagerUserId = inventoryData.getAreaManagerUserId();
        businessManagerUserId = inventoryData.getBusinessManagerUserId();
        dealerId = inventoryData.getDealerId();
        productId = inventoryData.getProductId();
        month = inventoryData.getMonth();
        planPurchaseQuantity = inventoryData.getPlanPurchaseQuantity();
        planSalesQuantity = inventoryData.getPlanSalesQuantity();
        actualStockQuantity = inventoryData.getRefStockQuantity();
        inventoryDay = inventoryData.getRefInventoryDay();
    }

    /**
     * 设置上月数据
     * @return
     */
    public void setLastMonthInventoryData(SecondaryDealerInventoryData inventoryData) {
        if (inventoryData == null) {
            return;
        }

        lastId = inventoryData.getId();
        lastMonthActualPurchaseQuantity = inventoryData.getActualPurchaseQuantity();
        lastMonthActualSalesQuantity = inventoryData.getActualSalesQuantity();
        lastMonthActualSalesQuantityHeadOffice = inventoryData.getActualSalesQuantityHeadOffice();
        lastMonthActualStockQuantity = inventoryData.getActualStockQuantity();
        lastMonthTheoryStockQuantity = inventoryData.getTheoryStockQuantity();
        lastMonthOriginalTheoryStockQuantity = inventoryData.getOriginalTheoryStockQuantity();
        lastMonthInventoryDay = inventoryData.getInventoryDay();
        diffCause = inventoryData.getDiffCause();
        lastMonth = inventoryData.getMonth();
    }

    @Override
    public SecondaryDealerDto clone() {
        try {
            return (SecondaryDealerDto) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getUpdateData() {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("last_month_actual_purchase_quantity", lastMonthActualPurchaseQuantity);
        hashMap.put("last_month_actual_sales_quantity", lastMonthActualSalesQuantity);
        hashMap.put("last_month_actual_sales_quantity_head_office", lastMonthActualSalesQuantityHeadOffice);
        hashMap.put("last_month_actual_stock_quantity", lastMonthActualStockQuantity);
        hashMap.put("plan_purchase_quantity", planPurchaseQuantity);
        hashMap.put("plan_sales_quantity", planSalesQuantity);
        return hashMap;
    }
}