package com.site.controller;

import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.Product;
import com.site.service.ProductService;
import com.site.utils.JacksonUtil;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * Product 管理
 * 描述：
 */
public class ProductController extends BaseController {

    private static final Log log = Log.getLog(ProductController.class);

    static ProductService srv = ProductService.me;

    /**
     * 列表
     * /site/product/list
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(Product.class);
        queryUtil.setSqlSelect("SELECT a.* , b. NAME AS create_user_name");
        queryUtil.setSqlExceptSelect("FROM product a LEFT JOIN `user` b ON a.create_user_id = b.id ");
        queryUtil.setSort("create_time");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"a.name"});
        renderJson(queryUtil.getResult());
    }

    /**
     * 准备添加
     * /site/product/add
     */
    public void add() {
        render("add.html");
    }

    /**
     * 保存
     * /site/product/save
     */
    public void save() {
        String s = HttpKit.readData(getRequest());
        Map<String, Object> jsonMap = JacksonUtil.json2Map(s);
        Object id = jsonMap.get("id");
        Object name = jsonMap.get("name");
        Object projectName = jsonMap.get("project_name");
        if (name == null || StringUtils.isEmpty(name.toString())) {
            throw new ExceptionForJson("产品名称不能为空");
        }
        Product product = new Product();
        if (id != null && !StringUtils.isEmpty(id.toString())) {
            product.setId(Long.valueOf(id.toString()));
        }
        product.setName(name.toString());
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product.setProjectName(projectName.toString());
        product.setCreateUserId(getLoginUser().getId());
        product.setUpdateUserId(getLoginUser().getId());
        if (product.getId() == null) {
            srv.save(product);
        } else {
            srv.update(product);
        }
        renderJson(result);
    }

    /**
     * 准备更新
     * /site/product/edit
     */
    public void edit() {
        Product product = srv.findById(getParaToInt("id"));
        setAttr("product", product);
        render("add.html");
    }

    /**
     * 更新
     * /site/product/update
     */
    public void update() {
        srv.update(getModel(Product.class));
        renderJson("isOk", true);
    }

    /**
     * 查看
     * /site/product/view
     */
    public void detail() {
        Product product = srv.findById(getParaToInt("id"));
        setAttr("product", product);
        render("detail.html");
    }

    /**
     * 删除
     * /site/product/delete
     */
    public void delete() {
        srv.delete(getParaToInt("id"));
        renderJson(result);
    }

}