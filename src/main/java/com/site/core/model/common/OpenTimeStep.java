package com.site.core.model.common;

import com.site.utils.DateUtils;

public class OpenTimeStep {
    private Integer step;
    //倒数第几天
    private Integer desc;
    //正数第几天
    private Integer asc;
    //当前可填写的是哪一个月的数据
    private Integer yearAndMonth;
    //下月采购量总量是否填写完成
    private Integer productPurposeStatus = 0;

    public OpenTimeStep() {
    }

    public OpenTimeStep(Integer step, Integer desc, Integer asc, Integer yearAndMonth) {
        this.step = step;
        this.desc = desc;
        this.asc = asc;
        this.yearAndMonth = yearAndMonth;
    }

    public Integer getProductPurposeStatus() {
        return productPurposeStatus;
    }

    public OpenTimeStep setProductPurposeStatus(Integer productPurposeStatus) {
        this.productPurposeStatus = productPurposeStatus;
        return this;
    }

    public Integer getYearAndMonth() {
        return yearAndMonth;
    }

    public void setYearAndMonth(int yearAndMonth) {
        this.yearAndMonth = yearAndMonth;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getDesc() {
        return desc;
    }

    public void setDesc(Integer desc) {
        this.desc = desc;
    }

    public Integer getAsc() {
        return asc;
    }

    public void setAsc(Integer asc) {
        this.asc = asc;
    }
}
