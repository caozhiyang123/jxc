package com.site.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.site.base.BaseService;
import com.site.core.model.Product;
import com.site.core.model.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Product 管理
 * 描述：
 */
public class ProductService extends BaseService {
    private UserService userService = new UserService();
    public static final ProductService me = new ProductService();
    private final Product dao = new Product().dao();


    /**
     * 列表-分页
     */
    public Page<Product> paginate(int pageNumber, int pageSize) {
        return dao.paginate(pageNumber, pageSize, "SELECT a.*, b. NAME AS create_user_name ", "FROM product a LEFT JOIN `user` b ON a.create_user_id = b.id ORDER BY create_time DESC");
    }

    /**
     * 保存
     */
    public void save(Product product) {
        product.save();
    }

    /**
     * 更新
     */
    public void update(Product product) {
        product.update();
    }

    /**
     * 查询
     */
    public Product findById(int productId) {
        return dao.findFirst("select * from product where id=?", productId);
    }

    /**
     * 删除
     */
    public void delete(int productId) {
        Db.update("delete from product where id=?", productId);
    }

    /**
     * @param loginUser 登录用户
     * @param level     经销商等级
     * @param isSelf    是否获取自身权限的所有产品，否就获取空缺用户的所有Product列表
     * @return
     */
    public List<Product> getProductList(User loginUser, int level, boolean isSelf) {
        List<Product> products = new ArrayList<>();
        if ("大区经理".equals(loginUser.getRoleName())) {
            if (!isSelf) {
                List<Long> vacancyBMUserId = userService.getVacancyBMUserId(loginUser.getId(), true);
                if (!vacancyBMUserId.isEmpty()) {
                    String join = StringUtils.join(vacancyBMUserId, ",");
                    products = Product.dao.find("SELECT c.id,c.name FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id LEFT JOIN product c ON a.product_id = c.id WHERE b.business_manager_user_id in (?) and b.level=? and b.is_delete='0' and c.is_delete='0' GROUP BY c.id", join, level);
                }
            } else {
                products = Product.dao.find("SELECT c.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id LEFT JOIN product c ON a.product_id = c.id WHERE b.area_manager_user_id = ? and b.level=? and c.is_delete='0' and b.is_delete='0' GROUP BY c.id", loginUser.getId(), level);
            }
        }

        if ("商务经理".equals(loginUser.getRoleName())) {
            products = Product.dao.find("SELECT c.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id LEFT JOIN product c ON a.product_id = c.id WHERE b.business_manager_user_id = ? and b.level=? and b.is_delete='0' and c.is_delete='0' GROUP BY c.id", loginUser.getId(), level);
        }

        if ("总部".equals(loginUser.getRoleName())) {
            products = Product.dao.find("select * from product where is_delete='0'");
        }

        if (products != null && products.size() > 0) {
            products.get(0).put("selected", "selected");
        }
        return products;
    }

    /**
     * 获取不同用户角色下所有的商品
     *
     * @param loginUser 登录用户
     * @return
     */
    public List<Product> getAllProductList(User loginUser) {
        List<Product> products = new ArrayList<>();
        if ("大区经理".equals(loginUser.getRoleName())) {
            products = Product.dao.find("SELECT c.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id LEFT JOIN product c ON a.product_id = c.id WHERE b.area_manager_user_id = ? GROUP BY c.id", loginUser.getId());
        }

        if ("商务经理".equals(loginUser.getRoleName())) {
            products = Product.dao.find("SELECT c.* FROM dealer_product a LEFT JOIN dealer b ON a.dealer_id = b.id LEFT JOIN product c ON a.product_id = c.id WHERE b.business_manager_user_id = ? GROUP BY c.id", loginUser.getId());
        }

        if ("总部".equals(loginUser.getRoleName())) {
            products = Product.dao.find("select * from product");
        }

        if (products != null && products.size() > 0) {
            products.get(0).put("selected", "selected");
        }
        return products;
    }


}