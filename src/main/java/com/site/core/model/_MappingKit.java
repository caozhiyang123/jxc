package com.site.core.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("area", "id", Area.class);
		arp.addMapping("dealer", "id", Dealer.class);
		arp.addMapping("dealer_inventory_data_history", "id", DealerInventoryDataHistory.class);
		arp.addMapping("dealer_inventory_data_version", "id", DealerInventoryDataVersion.class);
		arp.addMapping("dealer_inventory_data_version_list", "id", DealerInventoryDataVersionList.class);
		arp.addMapping("dealer_product", "id", DealerProduct.class);
		arp.addMapping("first_dealer_inventory_data", "id", FirstDealerInventoryData.class);
		arp.addMapping("job", "id", Job.class);
		arp.addMapping("notice", "id", Notice.class);
		arp.addMapping("open_time_manage", "id", OpenTimeManage.class);
		arp.addMapping("product", "id", Product.class);
		arp.addMapping("purchase_total_product", "id", PurchaseTotalProduct.class);
		arp.addMapping("region", "id", Region.class);
		arp.addMapping("secondary_dealer_inventory_data", "id", SecondaryDealerInventoryData.class);
		arp.addMapping("send_email_list", "id", SendEmailList.class);
		arp.addMapping("test", "id", Test.class);
		arp.addMapping("user", "id", User.class);
	}
}

