package com.site.core.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePurchaseTotalProduct<M extends BasePurchaseTotalProduct<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Long id) {
		set("id", id);
	}

	public java.lang.Long getId() {
		return getLong("id");
	}

	public void setProductId(java.lang.Long productId) {
		set("product_id", productId);
	}

	public java.lang.Long getProductId() {
		return getLong("product_id");
	}

	public void setNum(java.math.BigDecimal num) {
		set("num", num);
	}

	public java.math.BigDecimal getNum() {
		return get("num");
	}

	public void setMonth(java.lang.Integer month) {
		set("month", month);
	}

	public java.lang.Integer getMonth() {
		return getInt("month");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	public void setCreateUserId(java.lang.Long createUserId) {
		set("create_user_id", createUserId);
	}

	public java.lang.Long getCreateUserId() {
		return getLong("create_user_id");
	}

	public void setUpdateUserId(java.lang.Long updateUserId) {
		set("update_user_id", updateUserId);
	}

	public java.lang.Long getUpdateUserId() {
		return getLong("update_user_id");
	}

}
