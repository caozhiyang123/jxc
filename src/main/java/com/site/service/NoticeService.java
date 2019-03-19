package com.site.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.site.core.model.Notice;

/**
 * Notice 管理
 * 描述：
 */
public class NoticeService {

//private static final Log log = Log.getLog(NoticeService.class);

    public static final NoticeService me = new NoticeService();
    private final Notice dao = new Notice().dao();


    /**
     * 列表-分页
     */
    public Page<Notice> paginate(int pageNumber, int pageSize) {
        return dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM notice  ORDER BY create_time DESC");
    }

    /**
     * 保存
     */
    public void save(Notice notice) {
        notice.save();
    }

    /**
     * 更新
     */
    public void update(Notice notice) {
        notice.update();
    }

    /**
     * 查询
     */
    public Notice findById(int noticeId) {
        return dao.findFirst("select * from notice where id=?", noticeId);
    }

    /**
     * 删除
     */
    public void delete(int noticeId) {
        Db.update("delete from notice where id=?", noticeId);
    }


}