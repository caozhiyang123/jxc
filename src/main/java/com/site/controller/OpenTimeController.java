package com.site.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.OpenTimeManage;
import com.site.core.model.common.OpenTimeStep;
import com.site.service.RoleConfigService;
import com.site.utils.DateUtils;

import java.util.Date;

public class OpenTimeController extends BaseController {

    /**
     * 暂时全当成是日期
     */
    @Before(Tx.class)
    public void save() {
        OpenTimeManage oldOpenTime = OpenTimeManage.dao.findFirst("select * from open_time_manage where status =? and level = 1", "1");
        OpenTimeManage oldOpenTime2 = OpenTimeManage.dao.findFirst("select * from open_time_manage where status =? and level = 2", "1");

        //之前的规则置为失效
        int update = Db.update("update open_time_manage set status =? ", "0");


        OpenTimeManage openTime = getModel(OpenTimeManage.class, "", true);
        openTime.setFirstLockStartDay(openTime.getFirstEndDay() + 1);
        openTime.setSecondStartDay(openTime.getFirstLockEndDay() + 1);
        openTime.setSecondLockStartDay(openTime.getSecondEndDay() + 1);

        OpenTimeManage openTime2 = getModel(OpenTimeManage.class, "o", true);
        openTime2.setFirstLockStartDay(openTime2.getFirstEndDay() + 1);

        if (openTime.getFirstLockStartDay() < openTime.getFirstEndDay()) {
            throw new ExceptionForJson("第一个锁定期的开始时间要大于第一个开放期的结束时间");
        }

        if (openTime.getFirstLockEndDay() < openTime.getFirstLockStartDay()) {
            throw new ExceptionForJson("第一个锁定期结束时间不能小于开始时间");
        }

        if (openTime.getSecondStartDay() < openTime.getFirstLockEndDay()) {
            throw new ExceptionForJson("第二个开放期的开始时间要大于第一个锁定期的结束时间");
        }

        if (openTime.getSecondEndDay() < openTime.getSecondStartDay()) {
            throw new ExceptionForJson("第二个开放期结束时间不能小于开始时间");
        }

        if (openTime.getSecondLockStartDay() < openTime.getSecondEndDay()) {
            throw new ExceptionForJson("第二个锁定期的开始时间要大于第二个开放期的结束时间");
        }

        openTime.setFirstStartDay(oldOpenTime.getFirstStartDay());
        openTime.setSecondLockEndDay(oldOpenTime.getSecondLockEndDay());
        openTime.setCreateTime(new Date());
        openTime.setCreateUserId(getLoginUser().getId());
        openTime.setUpdateTime(new Date());
        openTime.setUpdateUserId(getLoginUser().getId());
        openTime.setStatus("1");
        openTime.setLevel(1);
        openTime.setLastMonth(oldOpenTime.getLastMonth());
        openTime.save();

//        ================================二级商的锁定期设定==================================
        openTime2.setFirstStartDay(1);
        openTime2.setCreateTime(new Date());
        openTime2.setCreateUserId(getLoginUser().getId());
        openTime2.setUpdateTime(new Date());
        openTime2.setUpdateUserId(getLoginUser().getId());
        openTime2.setStatus("1");
        openTime2.setLevel(2);
        openTime2.setLastMonth(oldOpenTime2.getLastMonth());
        openTime2.setFirstStartDay(oldOpenTime2.getFirstStartDay());
        openTime2.setFirstLockEndDay(oldOpenTime2.getFirstLockEndDay());
        openTime2.save();

        renderJson(result);
    }

    public void getOpenTime() {
        OpenTimeManage openTime = OpenTimeManage.dao.findFirst("select * from open_time_manage where level=1 and  status = '1' ");
        OpenTimeManage openTime2 = OpenTimeManage.dao.findFirst("select * from open_time_manage where level=2 and status = '1' ");

        //系统最后一次计算时间
        Integer lastMonth = openTime.getLastMonth();
        //当前月份
        int thisYearAndMonth = DateUtils.getYearAndMonth(0);

        if (lastMonth.equals(thisYearAndMonth)) {
            //最后结算时间是不是当月最后一天，如果是，取当月，不是取上月
            Integer secondLockEndDay = openTime.getSecondLockEndDay();
            int totalDaysOfLastMonth = DateUtils.getNumberOfDays(lastMonth + "", "yyyyMM");
            if (secondLockEndDay == totalDaysOfLastMonth) {
                lastMonth = new Integer(DateUtils.getMonth(lastMonth + "", "yyyyMMdd", 1));
            }
            openTime.put("first_start_day", (lastMonth + "").substring(4, 6) + "月" + openTime.getFirstStartDay());
            openTime2.put("first_start_day", (lastMonth + "").substring(4, 6) + "月" + openTime2.getFirstStartDay());
        } else {
            openTime.put("first_start_day", (openTime.getLastMonth() + "").substring(4, 6) + "月" + openTime.getFirstStartDay());
            openTime2.put("first_start_day", (openTime2.getLastMonth() + "").substring(4, 6) + "月" + openTime2.getFirstStartDay());
        }

        String mm = DateUtils.getNowDate("MM");

        if (openTime.getSecondLockEndDay() > openTime.getFirstEndDay()) {
            //这就说明本月就已经计算结束，第一个锁定期这些时间要加一个月
            OpenTimeStep openTimeStep = RoleConfigService.me.getCurrentOpenTimeStep(1);
            Integer yearAndMonth = openTimeStep.getYearAndMonth();
            if (!yearAndMonth.toString().equals(DateUtils.getNowDate("yyyyMM"))) {
                mm = DateUtils.getMonth(1) + "";
                if (mm.length() == 1) {
                    mm = "0" + mm;
                }
            }
        }
        setAttr("openTime", openTime);
        setAttr("openTime2", openTime2);
        setAttr("thisMonth", mm);
        render("index.html");
    }


    public void update() {
        OpenTimeManage openTime = OpenTimeManage.dao.findFirst("select * from open_time_manage where level=1 and  status = '1' ");
        OpenTimeManage openTime2 = OpenTimeManage.dao.findFirst("select * from open_time_manage where level=2 and  status = '1' ");
        setAttr("openTime", openTime);
        setAttr("openTime2", openTime2);
        render("update.html");
    }

}
