package com.site.controller;

import com.jfinal.log.Log;
import com.site.base.BaseController;
import com.site.core.model.Notice;
import com.site.service.NoticeService;
import com.site.utils.QueryUtil;

import java.util.Date;

/**
 * 公告 管理
 * 描述：
 */
public class NoticeController extends BaseController {

    private static final Log log = Log.getLog(NoticeController.class);

    static NoticeService srv = NoticeService.me;

    /**
     * 列表
     * /site/notice/list
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(Notice.class);
        queryUtil.setSqlSelect("SELECT a.id, a.title, a.create_time, b.name create_user_name, a.update_time, c.NAME AS update_user_name");
        queryUtil.setSqlExceptSelect("FROM notice a LEFT JOIN `user` b ON a.create_user_id = b.id LEFT JOIN `user` c ON a.update_user_id = c.id ");
        queryUtil.setSort("create_time");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"a.title"});
        renderJson(queryUtil.getResult());
    }

    /**
     * 准备添加
     * /site/notice/add
     */
    public void add() {
        setAttr("downloadUrl", getServerPath());
        render("add.html");
    }

    /**
     * 保存或更新
     * /site/notice/save
     */
    public void save() {
        Notice notice = getModel(Notice.class, "");
        if (notice.getId() != null && notice.getId() > 0) {
            notice.setUpdateTime(new Date());
            notice.setUpdateUserId(getLoginUser().getId());
            srv.update(notice);
        } else {
            notice.setCreateTime(new Date());
            notice.setCreateUserId(getLoginUser().getId());
            srv.save(notice);
        }

        renderJson(result);
    }

    /**
     * 准备更新
     * /site/notice/edit
     */
    public void edit() {
        Notice notice = srv.findById(getParaToInt("id"));
        setAttr("notice", notice);
        setAttr("downloadUrl", getServerPath());
        render("add.html");
    }

    /**
     * 查看
     * /site/notice/view
     */
    public void detail() {
        Notice notice = srv.findById(getParaToInt("id"));
        setAttr("notice", notice);
        render("detail.html");
    }

    /**
     * 删除
     * /site/notice/delete
     */
    public void delete() {
        srv.delete(getParaToInt("id"));
        renderJson(result);
    }


}