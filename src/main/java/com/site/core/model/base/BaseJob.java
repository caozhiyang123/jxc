package com.site.core.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseJob<M extends BaseJob<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Long id) {
		set("id", id);
	}

	public java.lang.Long getId() {
		return getLong("id");
	}

	public void setMonth(java.lang.Integer month) {
		set("month", month);
	}

	public java.lang.Integer getMonth() {
		return getInt("month");
	}

	public void setTaskType(java.lang.Integer taskType) {
		set("task_type", taskType);
	}

	public java.lang.Integer getTaskType() {
		return getInt("task_type");
	}

	public void setStatus(java.lang.Integer status) {
		set("status", status);
	}

	public java.lang.Integer getStatus() {
		return getInt("status");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setEndTime(java.util.Date endTime) {
		set("end_time", endTime);
	}

	public java.util.Date getEndTime() {
		return get("end_time");
	}

}
