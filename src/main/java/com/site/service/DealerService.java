package com.site.service;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.site.core.model.Dealer;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dealer 管理
 * 描述：
 */
public class DealerService {

    private static final Log log = Log.getLog(DealerService.class);

    public static final DealerService me = new DealerService();
    private final Dealer dao = new Dealer().dao();


    /**
     * 列表-分页
     */
    public Page<Dealer> paginate(int pageNumber, int pageSize) {
        return dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM dealer  ORDER BY create_time DESC");
    }

    /**
     * 保存
     */
    public void save(Dealer dealer) {
        dealer.save();
    }

    /**
     * 更新
     */
    public void update(Dealer dealer) {
        dealer.update();
    }

    /**
     * 查询
     */
    public Dealer findById(int dealerId) {
        return dao.findFirst("select * from dealer where id=?", dealerId);
    }

    /**
     * 删除
     */
    public void delete(int dealerId) {
        Db.update("update dealer set is_delete='1' where id=?", dealerId);
    }

}