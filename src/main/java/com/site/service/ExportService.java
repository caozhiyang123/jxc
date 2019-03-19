package com.site.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.site.base.BaseService;
import com.site.core.model.FirstDealerInventoryData;
import com.site.core.model.User;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.DateUtils;
import com.site.utils.PoiUtils;
import com.site.utils.QueryUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExportService extends BaseService {
    public static ExportService me = new ExportService();

    /**
     * 导入自己的数据，只需要判断角色权限即可
     */
    public void exportData(HttpServletResponse response, Map<String, List<FirstDealerInventoryData>> map) {
        System.out.println("daochuyunxing start");
        OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
        String month = openTimeStep.getYearAndMonth() + "";
        String lastMonth = DateUtils.getYearAndMonth(month, "yyyyMM", -1);
        String lastLastMonth = DateUtils.getYearAndMonth(month, "yyyyMM", -2);
        System.out.println("daochuyunxing data" + JSON.toJSONString(map));

        //导出的文件名称
        String fileName = "进销存数据" + System.currentTimeMillis() + ".xls";
        //excel列名
        String colNames = "大区,省份,商务经理,商业公司名称,产品名称," + lastLastMonth + "月实际库存," + lastMonth + "月预估进货," + lastMonth + "月实际进货," + lastMonth + "月销售预估," + lastMonth + "月份实际销售," + lastMonth + "月实际销售-总部上传," + lastMonth + "月理论库存," + lastMonth + "月实际库存," + lastMonth + "月库存天数," + lastMonth + "月库存差异原因," + month + "月系统分配采购," + month + "月进货预估," + month + "月销售预估," + month + "月库存," + month + "月库存天数," + month + "本月已下单数," + month + "下单数差异";
        //对应的取什么名称
        String columnModelNames = "area_name,regin_name,business_manager_name,dealer_name,product_name,ssy_sj_kc,sy_yg_jhl,sy_jhl,sy_yg_xsl,sy_sj_xsl,sy_sj_xsl_zb,sy_ll_kc,sy_sj_kc,sy_kcts,cyyy,by_xtfp_cgl,by_yg_jhl,by_yg_xsl,by_kc,by_kcts,order_num,diff_order_num";
        //需要合并的字段名称是否需要显示
        Map mergeMap = new ImmutableMap.Builder<String, String>().
                put("area_name", "dealer_id").
                put("regin_name", "dealer_id").
                put("business_manager_name", "dealer_id")
                .build();
        try {
            PoiUtils.exportMergeData(response, FirstDealerInventoryData.class, map, mergeMap, fileName, colNames, columnModelNames, new String[]{});
        } catch (IOException e) {
            System.out.println("出错了，错误信息：" + e.getMessage());
        }
        System.out.println("daochuyunxing end");
    }

    public List<FirstDealerInventoryData> getData(QueryUtil queryUtil, User user, Boolean isSelf, String areaId, String regionId, String productId, String dealerId, String businessId) {
        String roleName = user.getRoleName();
        String sort = "c.id,d.id,g.id";
        String order = "desc,desc,desc,desc";
        if (roleName.equalsIgnoreCase("大区经理")) {
            if (!isSelf) {
                return DealerOneService.me.getList(queryUtil, user, productId, areaId, regionId, dealerId, businessId, sort, order, isSelf).getData();
            }
        }
        return DealerOneService.me.getList(queryUtil, user, productId, areaId, regionId, dealerId, businessId, sort, order, true).getData();
    }
}
