package com.site.quartz;

import com.jfinal.plugin.cron4j.ITask;


public class SaveHistoryTask implements ITask {
    @Override
    public void stop() {

    }

    @Override
    public void run() {
//        ChangeToHistoryService.me.saveSecondLevelData();
    }
}
