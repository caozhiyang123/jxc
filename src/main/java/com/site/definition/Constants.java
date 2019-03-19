package com.site.definition;

import org.apache.poi.hssf.util.HSSFColor;

public class Constants {

    /**
     * 通用的活跃状态
     * @author Richard
     * @date 2018-6-26
     */
    public enum ActiveState{

        ACTIVE("0","活跃"),NOT_ACTIVE("1","不活跃");

        ActiveState(String value,String name){
            this.value = value;
            this.name = name;
        }
        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        // 根据活跃状态值获取名称
        public static String getNameByValue(String value) {
            for (ActiveState active : ActiveState.values()) {
                if (active.getValue().equals(value)) {
                    return active.name;
                }
            }
            return null;
        }
    }

    /**
     * 通用的启用禁用状态
     * @author Richard
     * @date 2018-6-26
     */
    public enum EnableState{

        ENABLE("0","启用"),DISABLE("1","禁用");

        EnableState(String value,String name){
            this.value = value;
            this.name = name;
        }
        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        // 根据启用禁用值获取名称
        public static String getNameByValue(String value) {
            for (EnableState enable : EnableState.values()) {
                if (enable.getValue().equals(value)) {
                    return enable.name;
                }
            }
            return null;
        }

        // 根据启用禁用值获取相反的值
        public static String getReverseValue(String value) {
            if ("0".equals(value)) {
                return EnableState.DISABLE.getValue();
            }
            if ("1".equals(value)) {
                return EnableState.ENABLE.getValue();
            }
            return null;
        }
    }

    /**
     * 进销存数据类型
     * @author Richard
     * @date 2018-6-26
     */
    public enum JxcColumnType{
        PLAN_PURCHASE_QUANTITY("plan_purchase_quantity","预估进货量", 1, HSSFColor.LIGHT_YELLOW.index),
        ACTUAL_PURCHASE_QUANTITY("actual_purchase_quantity","实际进货量", 2, HSSFColor.LIGHT_GREEN.index),
        PLAN_SALES_QUANTITY("plan_sales_quantity","预估销售量", 3, HSSFColor.LIGHT_TURQUOISE.index),
        ACTUAL_SALES_QUANTITY("actual_sales_quantity","实际销售量", 4, HSSFColor.TURQUOISE.index),
        ACTUAL_SALES_QUANTITY_HEAD_OFFICE("actual_sales_quantity","总部上传实际销售量", 5, HSSFColor.TURQUOISE.index),
        THEORY_STOCK_QUANTITY("theory_stock_quantity","理论库存量", 6, HSSFColor.LIGHT_CORNFLOWER_BLUE.index),
        ACTUAL_STOCK_QUANTITY("actual_stock_quantity","实际库存数", 7, HSSFColor.LIME.index),
        INVENTORY_DAY("inventory_day","库存天数", 8, HSSFColor.PALE_BLUE.index),
        DIFF_CAUSE("diff_cause","库存差异原因", 9, HSSFColor.LIGHT_YELLOW.index);

        JxcColumnType(String value, String name, Integer score, Short color){
            this.value = value;
            this.name = name;
            this.score = score;
            this.color = color;
        }
        private final String value;
        private final String name;
        private final Integer score;
        private final short color;

        public short getColor() {
            return color;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public Integer getScore() {
            return score;
        }
    }

    /**
     * 版本管理任务执行状态
     * @author Richard
     * @date 2018-6-26
     */
    public enum VersionState{

        SUCCESS("0","成功"),ERROR("1","失败");

        VersionState(String value, String name){
            this.value = value;
            this.name = name;
        }
        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        // 根据任务执行状态值获取名称
        public static String getNameByValue(String value) {
            for (VersionState active : VersionState.values()) {
                if (active.getValue().equals(value)) {
                    return active.name;
                }
            }
            return null;
        }
    }

    /**
     * 发送邮件任务执行状态
     * @author Richard
     * @date 2018-6-26
     */
    public enum EmailState{

        TO_BE_SEND("-1","待发送"),SEND_SUCCESS("0","发送成功"),SEND_ERROR("1","发送失败");

        EmailState(String value, String name){
            this.value = value;
            this.name = name;
        }
        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        // 根据任务执行状态值获取名称
        public static String getNameByValue(String value) {
            for (EmailState active : EmailState.values()) {
                if (active.getValue().equals(value)) {
                    return active.name;
                }
            }
            return null;
        }
    }
}
