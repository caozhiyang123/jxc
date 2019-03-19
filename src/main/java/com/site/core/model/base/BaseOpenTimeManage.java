package com.site.core.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOpenTimeManage<M extends BaseOpenTimeManage<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Long id) {
		set("id", id);
	}

	public java.lang.Long getId() {
		return getLong("id");
	}

	public void setFirstStartDay(java.lang.Integer firstStartDay) {
		set("first_start_day", firstStartDay);
	}

	public java.lang.Integer getFirstStartDay() {
		return getInt("first_start_day");
	}

	public void setFirstEndDay(java.lang.Integer firstEndDay) {
		set("first_end_day", firstEndDay);
	}

	public java.lang.Integer getFirstEndDay() {
		return getInt("first_end_day");
	}

	public void setFirstLockStartDay(java.lang.Integer firstLockStartDay) {
		set("first_lock_start_day", firstLockStartDay);
	}

	public java.lang.Integer getFirstLockStartDay() {
		return getInt("first_lock_start_day");
	}

	public void setFirstLockEndDay(java.lang.Integer firstLockEndDay) {
		set("first_lock_end_day", firstLockEndDay);
	}

	public java.lang.Integer getFirstLockEndDay() {
		return getInt("first_lock_end_day");
	}

	public void setSecondStartDay(java.lang.Integer secondStartDay) {
		set("second_start_day", secondStartDay);
	}

	public java.lang.Integer getSecondStartDay() {
		return getInt("second_start_day");
	}

	public void setSecondEndDay(java.lang.Integer secondEndDay) {
		set("second_end_day", secondEndDay);
	}

	public java.lang.Integer getSecondEndDay() {
		return getInt("second_end_day");
	}

	public void setSecondLockStartDay(java.lang.Integer secondLockStartDay) {
		set("second_lock_start_day", secondLockStartDay);
	}

	public java.lang.Integer getSecondLockStartDay() {
		return getInt("second_lock_start_day");
	}

	public void setSecondLockEndDay(java.lang.Integer secondLockEndDay) {
		set("second_lock_end_day", secondLockEndDay);
	}

	public java.lang.Integer getSecondLockEndDay() {
		return getInt("second_lock_end_day");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setCreateUserId(java.lang.Long createUserId) {
		set("create_user_id", createUserId);
	}

	public java.lang.Long getCreateUserId() {
		return getLong("create_user_id");
	}

	public void setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	public void setUpdateUserId(java.lang.Long updateUserId) {
		set("update_user_id", updateUserId);
	}

	public java.lang.Long getUpdateUserId() {
		return getLong("update_user_id");
	}

	public void setStatus(java.lang.String status) {
		set("status", status);
	}

	public java.lang.String getStatus() {
		return getStr("status");
	}

	public void setLevel(java.lang.Integer level) {
		set("level", level);
	}

	public java.lang.Integer getLevel() {
		return getInt("level");
	}

	public void setLastMonth(java.lang.Integer lastMonth) {
		set("last_month", lastMonth);
	}

	public java.lang.Integer getLastMonth() {
		return getInt("last_month");
	}

	public void setNextMonthProductPurposeStatus(java.lang.Integer nextMonthProductPurposeStatus) {
		set("next_month_product_purpose_status", nextMonthProductPurposeStatus);
	}

	public java.lang.Integer getNextMonthProductPurposeStatus() {
		return getInt("next_month_product_purpose_status");
	}

}