package com.site.controller;

import com.jfinal.kit.PathKit;
import com.site.base.BaseController;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.Product;
import com.site.core.model.User;
import com.site.service.ExportService;
import com.site.utils.QueryUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportFileController extends BaseController {

    public void getImportTemp() {
        String name = getPara("name");
        String s = PathKit.getWebRootPath() + File.separator + "download" + File.separator + name;
        System.out.println("地址:" + s);
        File file = new File(s);
        if (!file.exists()) {
            System.out.println("文件不存在");
        } else {
            System.out.println("文件找到了");
        }
        renderFile(file);
    }

    /**
     * 导出数据到excel中
     */
    public void exportFistLevelData() {
        HttpServletResponse response = getResponse();

        Boolean isSelf = getParaToBoolean("is_self", true);
        String areaId = getPara("area_id");
        String regionId = getPara("region_id");
        String productId = getPara("product_id");
        String dealerId = getPara("dealer_id");
        String businessId = getPara("business_id");
        User loginUser = getLoginUser();
        //获取数据

        Map map = new HashMap();
        String[] split = productId.split(",");
        for (String product_id : split) {
            Product product = Product.dao.findById(product_id);
            QueryUtil queryUtil = getQueryUtil(FirstDealerInventoryData.class);
            List<FirstDealerInventoryData> list = ExportService.me.getData(queryUtil, loginUser, isSelf, areaId, regionId, product_id, dealerId, businessId);
            map.put(product.getName(), list);
        }
        ExportService.me.exportData(response, map);
        renderNull();
    }


}
