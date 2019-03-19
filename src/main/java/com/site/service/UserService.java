package com.site.service;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.site.base.BaseService;
import com.site.core.model.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notice 管理
 * 描述：
 */
public class UserService extends BaseService {

    private static final Log log = Log.getLog(UserService.class);

    public static final UserService me = new UserService();


    /**
     * 列表-分页
     */
    public Page<User> paginate(int pageNumber, int pageSize) {
        return User.dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM user  ORDER BY create_time DESC");
    }

    /**
     * 保存
     */
    public void save(User user) {
        user.save();
    }

    /**
     * 更新
     */
    public void update(User user) {
        user.update();
    }

    /**
     * 查询
     */
    public User findById(int userId) {
        return User.dao.findFirst("select * from user where id=?", userId);
    }

    /**
     * 删除
     */
    public void delete(int userId) {
        Db.update("update user set is_delete='1' where id=?", userId);
    }

    /**
     * 根据大区经理获取当前角色空缺的商务经理ID
     *
     * @param amUserId 大区经理 user_id
     * @param relatedProducts 是否关联产品
     */
    public List<Long> getVacancyBMUserId(Long amUserId, boolean relatedProducts ) {
        List<Long> list = new ArrayList<>();
        List<User> users = null;
        if (relatedProducts) {
            users = User.dao.find("select user.id from dealer d left join user user on d.business_manager_user_id=user.id left join dealer_product product on product.dealer_id = d.id where d.is_delete!='1' and (user.is_delete='1' or user.enable='1') and parent_id = ? and role_name='商务经理'  group by user.id", amUserId);
        } else {
            users = User.dao.find("select user.id from dealer d left join user user on d.business_manager_user_id=user.id where d.is_delete!='1' and (user.is_delete='1' or user.enable='1') and parent_id = ? and role_name='商务经理' group by user.id", amUserId);
        }
        users.forEach(user -> list.add(user.getId()));
        return list;
    }
}