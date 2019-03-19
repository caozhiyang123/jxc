package com.site.controller;

import com.jfinal.plugin.activerecord.Db;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.SendEmailList;
import com.site.definition.Constants;
import com.site.quartz.Cron4jPluginConfig;
import com.site.quartz.SendEmailTask;
import com.site.utils.DateUtils;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SendEmailController extends BaseController {

    /**
     * 列表
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(SendEmailList.class);
        queryUtil.setSqlSelect("SELECT a.*,u.name create_user_name");
        queryUtil.setSqlExceptSelect("FROM send_email_list a left join user u on u.id=a.create_user_id");
        queryUtil.setSort("send_time");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"a.subject"});

        String date = getPara("date");
        if (StringUtils.isNotBlank(date)) {
            String[] monthArr = date.split(" - ");
            if (monthArr.length >= 1 && StringUtils.isNotBlank(monthArr[0])) {
                queryUtil.addQueryParam("send_time", ">=", monthArr[0]);
            }
            if (monthArr.length >= 2 && StringUtils.isNotBlank(monthArr[1])) {
                queryUtil.addQueryParam("send_time", "<", DateUtils.getDate(monthArr[1], "yyyy-MM-dd", 1));
            }
        }

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<SendEmailList> emails = searchResult.getData();
        if (emails != null) {
            emails.forEach(email -> {
                email.put("status_name", Constants.EmailState.getNameByValue(email.getStatus()));
            });

        }
        renderJson(searchResult);
    }

    /**
     * 保存或更新
     * /site/notice/save
     */
    public void save() {
        SendEmailList email = getModel(SendEmailList.class, "");

        Long executeTime = email.getSendTime().getTime();
        Long nowTime = new Date().getTime();

        if (nowTime >= executeTime) {
            throw new ExceptionForJson("发送时间不能小于当前时间");
        }

        if (email.getId() != null && email.getId() > 0) {
            email.update();
        } else {
            email.setCreateTime(new Date());
            email.setCreateUserId(getLoginUser().getId());
            email.save();
            Cron4jPluginConfig.addTask(parseSendTimeToCronExpression(email.getSendTime()), new SendEmailTask());
            Cron4jPluginConfig.getCron4jPlugin().startTheLastOne();
        }
        renderJson(result);
    }

    /**
     * 准备添加
     */
    public void add() {
        setAttr("downloadUrl", getServerPath());
        render("add.html");
    }

    /**
     * 删除
     */
    public void delete() {
        Db.update("update send_email_list set is_delete='1' where id=?", getParaToInt("id"));
        renderJson(result);
    }

    private String parseSendTimeToCronExpression(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("m H d M *");
        String expression = sdf.format(time);
        return expression;
    }
}
