package com.site.service;

import com.google.common.collect.ImmutableMap;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.site.base.BaseService;
import com.site.core.model.*;
import com.site.core.model.common.OpenTimeStep;
import com.site.utils.ConvertUtil;
import com.site.utils.DateUtils;
import com.site.utils.ToolFunction;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoleConfigService extends BaseService {
    public static RoleConfigService me = new RoleConfigService();

    //商务经理的操作权限，权限-经销商等级-第几个开放期
    static Map<String, String> BusinessManagerRule = new ImmutableMap.Builder<String, String>()
            .put("商务经理-1-1", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("商务经理-1-2", "库存差异原因(cyyy)")
            .put("商务经理-1-3", "当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("商务经理-2-1", "上月进货(last_month_actual_purchase_quantity),上月实际销售(last_month_actual_sales_quantity),上月实际库存(last_month_actual_stock_quantity),当月进货预估(plan_purchase_quantity),当月销售预估(plan_sales_quantity),库存差异原因(diff_cause)")
            .put("商务经理-2-2", "库存差异原因(diff_cause)")
            .build();

    //大区经理的操作权限，权限-经销商等级-第几个开放期
    static Map<String, String> AreaManagerRule = new ImmutableMap.Builder<String, String>()
            .put("大区经理-1-1", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("大区经理-1-2", "库存差异原因(cyyy)")
            .put("大区经理-1-3", "当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("大区经理-2-1", "上月进货(last_month_actual_purchase_quantity),上月实际销售(last_month_actual_sales_quantity),上月实际库存(last_month_actual_stock_quantity),当月进货预估(plan_purchase_quantity),当月销售预估(plan_sales_quantity),库存差异原因(diff_cause)")
            .put("大区经理-2-2", "库存差异原因(diff_cause)")
            .build();

    //总部的操作权限，权限-经销商等级-第几个开放期
    static Map<String, String> BossRule = new ImmutableMap.Builder<String, String>()
            .put("总部-1-1", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际销售-总部填写(sy_sj_xsl_zb),上月销售量预估(sy_yg_xsl),上月进货量预估(sy_yg_jhl),本月系统分配采购量(by_xtfp_cgl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("总部-1-2", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际销售-总部填写(sy_sj_xsl_zb),上月销售量预估(sy_yg_xsl),上月进货量预估(sy_yg_jhl),本月系统分配采购量(by_xtfp_cgl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("总部-1-3", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际销售-总部填写(sy_sj_xsl_zb),上月销售量预估(sy_yg_xsl),上月进货量预估(sy_yg_jhl),本月系统分配采购量(by_xtfp_cgl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("总部-1-4", "上月进货(sy_jhl),上月实际销售(sy_sj_xsl),上月实际销售-总部填写(sy_sj_xsl_zb),上月销售量预估(sy_yg_xsl),上月进货量预估(sy_yg_jhl),本月系统分配采购量(by_xtfp_cgl),上月实际库存(sy_sj_kc),当月进货预估(by_yg_jhl),当月销售预估(by_yg_xsl),库存差异原因(cyyy)")
            .put("总部-2-1", "上月进货(last_month_actual_purchase_quantity),上月实际销售(last_month_actual_sales_quantity),上月总部上传实际销售(last_month_actual_sales_quantity_head_office),上月实际库存(last_month_actual_stock_quantity),当月进货预估(plan_purchase_quantity),当月销售预估(plan_sales_quantity),库存差异原因(diff_cause)")
            .put("总部-2-2", "上月进货(last_month_actual_purchase_quantity),上月实际销售(last_month_actual_sales_quantity),上月总部上传实际销售(last_month_actual_sales_quantity_head_office),上月实际库存(last_month_actual_stock_quantity),当月进货预估(plan_purchase_quantity),当月销售预估(plan_sales_quantity),库存差异原因(diff_cause)")
            //.put("总部-2-2", "上月预估进货(last_month_plan_purchase_quantity),上月进货(last_month_actual_purchase_quantity),上月销售预估(last_month_plan_sales_quantity),上月实际销售(last_month_actual_sales_quantity),上月理论库存(last_month_theory_stock_quantity),上月实际库存(last_month_actual_stock_quantity),上月库存天数(last_month_inventory_day),当月进货预估(plan_purchase_quantity),当月销售预估(plan_sales_quantity),当月库存(actual_stock_quantity),当月库存天数(inventory_day),库存差异原因(diff_cause)")
            .build();

    /**
     * 获取商务经理在不同的开放期可操纵的数据字段
     *
     * @param roleName    权限名称
     * @param dealerLevel T1经销商还是T2经销商
     * @return
     */
    public String getCanEditColumn(String roleName, int dealerLevel) {
        OpenTimeStep currentOpenTimeStep = getCurrentOpenTimeStep(dealerLevel);
        Integer step = currentOpenTimeStep.getStep();
        Integer descDay = currentOpenTimeStep.getDesc();
        step = step == null ? 0 : step;
        descDay = descDay == null ? 0 : descDay;

        if ("商务经理".equalsIgnoreCase(roleName)) {
            if ((1 == step || 3 == step) && descDay != 1) {
                return BusinessManagerRule.get(roleName + "-" + dealerLevel + "-" + step);
            }
            if (step != 4) {
                return BusinessManagerRule.get(roleName + "-" + dealerLevel + "-" + 2);
            }
        }
        if ("大区经理".equalsIgnoreCase(roleName)) {
            return AreaManagerRule.get(roleName + "-" + dealerLevel + "-" + step);
        }
        if ("总部".equalsIgnoreCase(roleName) && descDay > 0) {
            return BossRule.get(roleName + "-" + dealerLevel + "-" + step);
        }
        return "";
    }

    /**
     * 不同角色下的下拉能看到的大区信息
     */
    public List<Area> getAreaListByUser(Long userId) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List<Area> re = new ArrayList();
        if ("总部".equalsIgnoreCase(roleName)) {
            re = Area.dao.find("SELECT id,name from area where is_delete='0'");
        }

        if ("大区经理".equalsIgnoreCase(roleName)) {
            Area first = Area.dao.findFirst("SELECT b.name, b.id from user a LEFT JOIN area b on a.area_id =b.id where a.id =? and b.is_delete='0'", userId);
            re.add(first);
        }


        if ("商务经理".equalsIgnoreCase(roleName)) {
            Area first = Area.dao.findFirst("SELECT  b.name,b.id from user a LEFT JOIN area b on a.area_id =b.id  where a.id =? and b.is_delete='0'", userId);
            re.add(first);
        }
        return re;
    }

    /**
     * 不同角色下的下拉能看到的所有大区信息
     */
    public List<Area> getAllAreaListByUser(Long userId) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List<Area> re = new ArrayList();
        if ("总部".equalsIgnoreCase(roleName)) {
            re = Area.dao.find("SELECT id,name from area");
        }

        if ("大区经理".equalsIgnoreCase(roleName)) {
            Area first = Area.dao.findFirst("SELECT b.name, b.id from user a LEFT JOIN area b on a.area_id =b.id where a.id =?", userId);
            re.add(first);
        }


        if ("商务经理".equalsIgnoreCase(roleName)) {
            Area first = Area.dao.findFirst("SELECT  b.name,b.id from user a LEFT JOIN area b on a.area_id =b.id  where a.id =?", userId);
            re.add(first);
        }
        return re;
    }

    /**
     * 不同角色下的下拉能看到的省份的列表
     */
    public List getRegionList(List<String> areaIds, Long userId) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List re = new ArrayList();

        if ("总部".equalsIgnoreCase(roleName)) {
            if (areaIds.isEmpty()) {
                re = Region.dao.find("SELECT name,id from region where is_delete='0'");
            } else {
                StringBuilder ids = new StringBuilder("(");
                areaIds.forEach(k -> {
                    ids.append("'").append(k).append("'").append(",");
                });
                String string = ids.toString();
                String substring = string.substring(0, string.length() - 1);
                substring = substring + (")");
                re = Region.dao.find("SELECT name,id from region where area_id in  " + substring + " and is_delete='0'");
            }
        }

        if ("大区经理".equalsIgnoreCase(roleName)) {
            //找出这货是那个大区的
            Area first = Area.dao.findFirst("SELECT b.name, b.id from user  a LEFT JOIN area b on a.area_id =b.id  where a.id =?", userId);
            re = Region.dao.find("SELECT name,id from region where area_id = ? and is_delete='0'", first.getId());
        }


        if ("商务经理".equalsIgnoreCase(roleName)) {
            Region first = Region.dao.findFirst("SELECT b.id ,b.name from dealer a left join region b on a.region_id=b.id where business_manager_user_id =? and b.is_delete='0'", userId);
            re.add(first);
        }
        return re;
    }

    public List getRegionListForDtx(List<String> areaIds, Long userId, int level) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List re = new ArrayList();

        if ("大区经理".equalsIgnoreCase(roleName)) {
            List<Long> list = UserService.me.getVacancyBMUserId(userId, true);
            String join = StringUtils.join(list, ",");
            re = Region.dao.find("SELECT b.* FROM dealer a LEFT JOIN region b ON a.region_id = b.id WHERE business_manager_user_id IN (?) and  a.`level` =?  GROUP BY b.id", join, level);
        }

        return re;
    }

    /**
     * 不同角色下的下拉能看到的所有省份的列表
     */
    public List getAllRegionList(List<String> areaIds, Long userId) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List re = new ArrayList();

        if ("总部".equalsIgnoreCase(roleName)) {
            if (areaIds.isEmpty()) {
                re = Region.dao.find("SELECT name,id from region");
            } else {
                StringBuilder ids = new StringBuilder("(");
                areaIds.forEach(k -> {
                    ids.append("'").append(k).append("'").append(",");
                });
                String string = ids.toString();
                String substring = string.substring(0, string.length() - 1);
                substring = substring + (")");
                re = Region.dao.find("SELECT name,id from region where area_id in  " + substring + "");
            }
        }

        if ("大区经理".equalsIgnoreCase(roleName)) {
            //找出这货是那个大区的
            Area first = Area.dao.findFirst("SELECT b.name, b.id from user  a LEFT JOIN area b on a.area_id =b.id  where a.id =?", userId);
            re = Region.dao.find("SELECT name,id from region where area_id = ?", first.getId());
        }
     //   1e8fb35221c8eb856c96746c8e4ed844a64bd81f

        if ("商务经理".equalsIgnoreCase(roleName)) {
            re = Region.dao.find("SELECT b.id ,b.name from dealer a left join region b on a.region_id =b.id where business_manager_user_id =?  GROUP BY b.id ", userId);
        }
        return re;
    }

    /**
     * 根据不同角色获取下面商务经理
     *
     * @return
     */
    public List<User> getBusinessManager(Long userId) {
        User user = User.dao.findById(userId);
        List<User> users = new ArrayList<>();
        String roleName = user.getRoleName();
        if ("总部".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and is_delete ='0' and enable ='0' ");
        }
        if ("大区经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and parent_id = ?  and  enable ='0' ", userId);
            if (!users.isEmpty()) {
                Iterator<User> it = users.iterator();
                while (it.hasNext()) {
                    User x = it.next();
                    List<Dealer> dealers = Dealer.dao.find("select * from dealer where business_manager_user_id =?", x.getId());
                    if (dealers.size() == 0) {
                        it.remove();
                    }
                }
            }
        }

        if ("商务经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and id = ? and is_delete ='0' and enable ='0' ", userId);
        }
        return users;
    }
    
    /**
     * 根据不同角色获取下面商务经理
     *
     * @return
     */
    public List<User> getBusinessManagerByAreaIds(List<String> areaIds,List<String> regionIds,List<String> dealerIds,Long userId) {
        User user = User.dao.findById(userId);
        List<User> users = new ArrayList<>();
        String roleName = user.getRoleName();
        if ("总部".equalsIgnoreCase(roleName)) {
        	String querySql = "select * from user where role_name ='商务经理' and is_delete ='0' and enable ='0' ";
			
        	if (!areaIds.isEmpty())
        	{
        		querySql = querySql + "and area_id in (" + StringUtils.join(areaIds, ",") + ") ";
        	}
        	
        	if (!regionIds.isEmpty() || !dealerIds.isEmpty())
        	{
        		String dealerSql = "and id in (SELECT business_manager_user_id FROM dealer WHERE is_delete = '0' ";
        		
        		if (!regionIds.isEmpty())
        		{
        			dealerSql = dealerSql + "and region_id in (" + StringUtils.join(regionIds, ",") + ") ";
        		}
        		
        		if (!dealerIds.isEmpty())
        		{
        			dealerSql = dealerSql + "and id in (" + StringUtils.join(dealerIds, ",") + ") ";
        		}
        		
        		dealerSql = dealerSql + ") ";
        		
        		querySql = querySql + dealerSql;
        	}
        	
        	users = ConvertUtil.toUserList(Db.find(querySql));
        	
        	
        }
        if ("大区经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and parent_id = ?  and  enable ='0' ", userId);
            if (!users.isEmpty()) {
                Iterator<User> it = users.iterator();
                while (it.hasNext()) {
                    User x = it.next();
                    List<Dealer> dealers = Dealer.dao.find("select * from dealer where business_manager_user_id =?", x.getId());
                    if (dealers.size() == 0) {
                        it.remove();
                    }
                }
            }
        }

        if ("商务经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and id = ? and is_delete ='0' and enable ='0' ", userId);
        }
        return users;
    }    

    /**
     * 根据角色获取所有的商务经理,无论是否删除
     *
     * @return
     */
    public List<User> getAllBusinessManager(List<String> areaIds,List<String> regionIds,List<String> dealerIds, Long userId) {
        User user = User.dao.findById(userId);
        List<User> users = new ArrayList<>();
        String roleName = user.getRoleName();
        if ("总部".equalsIgnoreCase(roleName)) {
   	        String querySql = "select * from user where role_name ='商务经理' ";
			
        	if (!areaIds.isEmpty())
        	{
        		querySql = querySql + "and area_id in (" + StringUtils.join(areaIds, ",") + ") ";
        	}
        	
        	if (!regionIds.isEmpty() || !dealerIds.isEmpty())
        	{
        		String dealerSql = "and id in (SELECT business_manager_user_id FROM dealer WHERE is_delete = '0' ";
        		
        		if (!regionIds.isEmpty())
        		{
        			dealerSql = dealerSql + "and region_id in (" + StringUtils.join(regionIds, ",") + ") ";
        		}
        		
        		if (!dealerIds.isEmpty())
        		{
        			dealerSql = dealerSql + "and id in (" + StringUtils.join(dealerIds, ",") + ") ";
        		}
        		
        		dealerSql = dealerSql + ") ";
        		
        		querySql = querySql + dealerSql;
        	}
        	
        	users = ConvertUtil.toUserList(Db.find(querySql));
        }
        if ("大区经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and parent_id = ?", userId);
        }
        if ("商务经理".equalsIgnoreCase(roleName)) {
            users = User.dao.find("select * from user where role_name ='商务经理' and id = ?", userId);
        }
        return users;
    }

    /**
     * 不同角色获取不同的经销商
     */
    public List<Dealer> getDealerList(List<String> areaIds,List<String> regionIds,List<String> businessIds, Long userId, String level, boolean isSelf) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List<Dealer> re = new ArrayList<Dealer>();
        boolean isLevel = StringUtils.isNotBlank(level);
        if ("总部".equalsIgnoreCase(roleName)) {
            if (isLevel) {
            	String querySql = "SELECT id,name FROM dealer WHERE LEVEL =" +level+ " AND is_delete = '0' ";
            			
            	if (!areaIds.isEmpty())
            	{
            		querySql = querySql + "and area_id in (" + StringUtils.join(areaIds, ",") + ") ";
            	}
            	
            	if (!regionIds.isEmpty())
            	{
            		querySql = querySql + "and region_id in (" + StringUtils.join(regionIds, ",") + ") ";
            	}
            	
            	if (!businessIds.isEmpty())
            	{
            		querySql = querySql + "and business_manager_user_id in (" + StringUtils.join(businessIds, ",") + ") ";
            	}
            	
            	 re = ConvertUtil.toDealerList(Db.find(querySql));
                
            } else {
                re = Dealer.dao.find("select id,name from dealer where is_delete='0'");
            }
        }
        if ("大区经理".equalsIgnoreCase(roleName)) {
            if (isLevel) {
                if (isSelf) {
                    re = Dealer.dao.find("SELECT id,name from dealer where area_manager_user_id = ? and level=? and is_delete='0'", userId, level);
                } else {
                    List<Long> vacancyBMUserId = UserService.me.getVacancyBMUserId(userId, true);
                    String join = StringUtils.join(vacancyBMUserId, ",");
                    re = Dealer.dao.find("select id,name from dealer where is_delete ='0' and `level` =? and area_manager_user_id =? and business_manager_user_id in (?) ", level, userId, join);
                }
            } else {
                re = Dealer.dao.find("SELECT id,name from dealer where area_manager_user_id = ? and is_delete='0'", userId);
            }
        }
        if ("商务经理".equalsIgnoreCase(roleName)) {
            if (isLevel) {
                re = Dealer.dao.find("SELECT id,name from dealer where business_manager_user_id = ? and level=? and is_delete='0'", userId, level);
            } else {
                re = Dealer.dao.find("SELECT id,name from dealer where business_manager_user_id = ? and is_delete='0'", userId);
            }
        }
        
        return re;
    }

    /**
     * 根据角色获取所有的经销商,无论是否删除
     */
    public List<Dealer> getAllDealerList(List<String> areaIds,List<String> regionIds,List<String> businessIds, Long userId, String level) {
        User user = User.dao.findById(userId);
        String roleName = user.getRoleName();
        List<Dealer> re = new ArrayList<Dealer>();
        level = ToolFunction.getLevel(level);
        boolean isLevel = StringUtils.isNotBlank(level);
        if ("总部".equalsIgnoreCase(roleName)) {
        	String querySql = "SELECT id,name FROM dealer WHERE 1 = 1 ";
			
        	if (isLevel)
        	{
        		querySql = querySql + "and level = " + level + " ";
        	}
        	
        	if (!areaIds.isEmpty())
        	{
        		querySql = querySql + "and area_id in (" + StringUtils.join(areaIds, ",") + ") ";
        	}
        	
        	if (!regionIds.isEmpty())
        	{
        		querySql = querySql + "and region_id in (" + StringUtils.join(regionIds, ",") + ") ";
        	}
        	
        	if (!businessIds.isEmpty())
        	{
        		querySql = querySql + "and business_manager_user_id in (" + StringUtils.join(businessIds, ",") + ") ";
        	}
        	
        	re = ConvertUtil.toDealerList(Db.find(querySql));        	
        }

        if ("大区经理".equalsIgnoreCase(roleName)) {
            if (isLevel) {
                re = Dealer.dao.find("SELECT id,name from dealer where area_manager_user_id = ? and level=?", userId, level);
            } else {
                re = Dealer.dao.find("SELECT id,name from dealer where area_manager_user_id = ?", userId);
            }
        }
        if ("商务经理".equalsIgnoreCase(roleName)) {
            if (isLevel) {
                re = Dealer.dao.find("SELECT id,name from dealer where business_manager_user_id = ? and level=?", userId, level);
            } else {
                re = Dealer.dao.find("SELECT id,name from dealer where business_manager_user_id = ?", userId);
            }
        }
        return re;
    }

    /**
     * 当前在哪一个开放期
     * 1：第一个开放期
     * 2：第一个结束期
     * 3：第二个开发期
     * 4：第二个结束期
     *
     * @return
     */
    public OpenTimeStep getCurrentOpenTimeStep(int level) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        OpenTimeManage time = OpenTimeManage.dao.findFirst("select * from open_time_manage where `status` ='1' and level =?", level);

        String s = DateUtils.getYearAndMonth(0) + "" + (day < 10 ? "0" + day : day);
        if (time.getLevel() == 1) {
            Integer lastMonth = time.getLastMonth();
            Integer secondLockEndDay = time.getSecondLockEndDay();
            String lDate = lastMonth + "" + secondLockEndDay;
            String yyyyMMdd = DateUtils.getNowDate("yyyyMMdd");
            OpenTimeStep oneLevelStep = getOneLevelStep(time, new Integer(s));
            if (lDate.equalsIgnoreCase(yyyyMMdd)) {
                oneLevelStep.setProductPurposeStatus(1);
            } else if (time.getSecondLockEndDay() == null) {
                oneLevelStep.setProductPurposeStatus(1);
            } else {
                oneLevelStep.setProductPurposeStatus(0);
            }
            return oneLevelStep;
        } else {
            Integer lastMonth = time.getLastMonth();
            Integer secondLockEndDay = time.getSecondLockEndDay();
            String lDate = lastMonth + "" + secondLockEndDay;
            String yyyyMMdd = DateUtils.getNowDate("yyyyMMdd");
            OpenTimeStep twoLevelStep = getTwoLevelStep(time, new Integer(s));
            if (lDate.equalsIgnoreCase(yyyyMMdd)) {
                twoLevelStep.setDesc(-1);
            }
            return twoLevelStep;
        }
    }

    private OpenTimeStep getTwoLevelStep(OpenTimeManage time, int day) {
        Integer thisMonth = Integer.parseInt(DateUtils.getNowDate("yyyyMM"));

        //上次计算结束的那一刻年月
        Integer calculateLastMonth = time.getLastMonth();
        /**
         * 如果结算时间的月份和当前的月份一样
         * 那么就要比较第一个锁定期的结束时间和第一个开放期结束时间做比较
         * 1：第一个锁定期结束时间 > 第一个开放期结束时间 ，这就说明，当月就完成了所有的计算，那么，下个月的开放期就得从现在开始，而不是等到下个月
         * 2：第一个锁定期结束时间 < 第一个开放期结束时间 , 这就说明，就是本月
         */
        if (calculateLastMonth.equals(thisMonth) && (time.getFirstLockEndDay() > time.getFirstLockStartDay())) {
            thisMonth = DateUtils.getYearAndMonth(1);
        }

        //第一个开放期的结束，第一个锁定期的开始
        Integer s_1_1 = new Integer(thisMonth + "" + (time.getFirstEndDay() < 10 ? "0" + time.getFirstEndDay() : time.getFirstEndDay()));

        //第一个锁定期的结束，第一个开放期的开始
        Integer s_1_2 = new Integer(calculateLastMonth + "" + (time.getFirstLockEndDay() < 10 ? "0" + time.getFirstLockEndDay() : time.getFirstLockEndDay()));

        List<Integer> list = genDefaultList(s_1_2, s_1_1);

        List step1 = new ArrayList();
        step1.addAll(list.subList(0, list.indexOf(s_1_1) + 1));
        if (step1.contains(day)) {
            return new OpenTimeStep(1, step1.size() - step1.indexOf(day), step1.indexOf(day) + 1, thisMonth);
        }

        //第一个锁定期
        Date date = DateUtils.formateDate((s_1_1 + 1) + "", "yyyyMMdd");
        int diffDays = DateUtils.differentDays(date, new Date()) + 1;
        return new OpenTimeStep(2, 1, diffDays, thisMonth);
    }

    private OpenTimeStep getOneLevelStep(OpenTimeManage time, int day) {
        Integer yyyyMM = Integer.parseInt(DateUtils.getNowDate("yyyyMM"));
        if (time.getFirstStartDay() == null) {
            //这里就说明是是在系统计算期间，所有角色不能进行任何的操作
            return new OpenTimeStep(4, -1, -1, yyyyMM);
        }
        //上次计算结束的那一刻年月
        Integer lastMonth = time.getLastMonth();
        Integer timeLastMonth = lastMonth;
        if (timeLastMonth == null) {
            return new OpenTimeStep(4, -1, -1, yyyyMM);
        }

        //如果计算结束时间和当前时间处在同一个月，且开始时间和当前时间一致的情况下，这就代表这这是第二个锁定期的最后一天
        Integer secondLockEndDay = time.getSecondLockEndDay();
        String thisDay = DateUtils.getDate(new Date(), "yyyyMMdd");
        String todayDate = thisDay.substring(6, 8).startsWith("0") ? thisDay.substring(7, 8) : thisDay.substring(6, 8);
        String thisMonth = DateUtils.getDate(new Date(), "yyyyMM");
        if (new Integer(thisMonth).equals(time.getLastMonth()) && todayDate.equalsIgnoreCase(time.getSecondLockEndDay().toString())) {
            return new OpenTimeStep(4, -1, -1, yyyyMM);
        }
        int month = DateUtils.getYearAndMonth(0);

        /**
         * 如果结算时间的月份和当前的月份一样
         * 那么就要比较第二个锁定期的结束时间和第二个开放期结束时间做比较
         * 1：第二个锁定期结束时间 > 第二个开放期结束时间 ，这就说明，当月就完成了所有的计算，那么，下个月的开放期就得从现在开始，而不是等到下个月
         * 2：第二个锁定期结束时间 < 第二个开放期结束时间 , 这就说明，就是本月
         */
        if (timeLastMonth.equals(month) && (secondLockEndDay > time.getSecondEndDay())) {
            month = DateUtils.getYearAndMonth(1);
        }

        //第一个开放期的结束，第一个锁定期的开始
        Integer s_1_2 = 0;
        //如果第一个锁定期时间大于上一个周期中第二个锁定期的结束时间，那么就需要将时间变更为上月
        if (time.getFirstLockEndDay() > time.getSecondLockEndDay()) {
            s_1_2 = new Integer(lastMonth + "" + (time.getFirstEndDay() < 10 ? "0" + time.getFirstEndDay() : time.getFirstEndDay()));
        } else {
            s_1_2 = new Integer(month + "" + (time.getFirstEndDay() < 10 ? "0" + time.getFirstEndDay() : time.getFirstEndDay()));
        }

        //第一个锁定期的结束，第二个开放期的开始
        Integer s_2_1 = new Integer(month + "" + (time.getFirstLockEndDay() < 10 ? "0" + time.getFirstLockEndDay() : time.getFirstLockEndDay()));

        //第二个开放期的结束，第二个锁定期的开始
        Integer s_2_2 = new Integer(month + "" + (time.getSecondEndDay() < 10 ? "0" + time.getSecondEndDay() : time.getSecondEndDay()));

        //第二个锁定期的结束，第一个开放期的开始
        Integer s_1_1 = new Integer(timeLastMonth + "" + (secondLockEndDay < 10 ? "0" + secondLockEndDay : secondLockEndDay));

        List<Integer> list = genDefaultList(s_1_1, s_2_2 + 1);

        List step1 = new ArrayList();
        int start = 0;
        int end = list.indexOf(s_1_2) + 1;
        step1.addAll(list.subList(0, end));
        if (step1.contains(day)) {
            return new OpenTimeStep(1, step1.size() - step1.indexOf(day), step1.indexOf(day) + 1, month);
        }

        List step2 = new ArrayList();
        start = list.indexOf(s_1_2) + 1;
        end = list.indexOf(s_2_1) + 1;
        step2.addAll(list.subList(start, end));
        if (step2.contains(day)) {
            return new OpenTimeStep(2, step2.size() - step2.indexOf(day), step2.indexOf(day) + 1, month);
        }

        List step3 = new ArrayList();
        start = list.indexOf(s_2_1) + 1;
        end = list.indexOf(s_2_2) + 1;
        step3.addAll(list.subList(start, end));
        if (step3.contains(day)) {
            return new OpenTimeStep(3, step3.size() - step3.indexOf(day), step3.indexOf(day) + 1, month);
        }

        //到了这就肯定是第二个锁定期
        return new OpenTimeStep(4, 1, 1, month);
    }

    //20180528-20180617 或者是20180601-20180617
    private List<Integer> genDefaultList(int startDay, int endDay) {
        Integer currentMonth = DateUtils.getYearAndMonth(0);
        Integer startDayMonth = new Integer((startDay + "").substring(0, 6));
        Integer endDayMonth = new Integer((endDay + "").substring(0, 6));
        Integer star = new Integer((startDay + "").substring(6, 8));
        Integer end = new Integer((endDay + "").substring(6, 8));

        List<Integer> list = new ArrayList<>();
        if (currentMonth.equals(endDayMonth) && currentMonth.equals(startDayMonth)) {
            //本月
            for (int i = star + 1; i <= end; i++) {
                list.add(new Integer(currentMonth + "" + (i < 10 ? "0" + i : i)));
            }
        } else if (currentMonth.compareTo(endDayMonth) < 0) {
            //下一个月
            int totalDay = DateUtils.getNumberOfDays(currentMonth + "", "yyyyMM");
            for (int i = star + 1; i <= totalDay; i++) {
                list.add(new Integer(currentMonth + "" + (i < 10 ? "0" + i : i)));
            }
            for (int i = 1; i <= end; i++) {
                int yearAndMonth2 = DateUtils.getYearAndMonth(1);
                list.add(new Integer(yearAndMonth2 + "" + (i < 10 ? "0" + i : i)));
            }
        } else {
            //上一个月
            int yearAndMonthLast = DateUtils.getYearAndMonth(-1);
            int totalDay = DateUtils.getNumberOfDays(yearAndMonthLast + "", "yyyyMM");
            for (int i = star + 1; i <= totalDay; i++) {
                list.add(new Integer(yearAndMonthLast + "" + (i < 10 ? "0" + i : i)));
            }
            for (int i = 1; i <= end; i++) {
                list.add(new Integer(currentMonth + "" + (i < 10 ? "0" + i : i)));
            }
        }

        return list;
    }

    public static void main(String[] args) {
    }

}
