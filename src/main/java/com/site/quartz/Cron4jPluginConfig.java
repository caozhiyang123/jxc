package com.site.quartz;

import com.jfinal.kit.PropKit;

public class Cron4jPluginConfig {

    private Cron4jPluginConfig(){}

    public static CustomCron4jPlugin getCron4jPlugin() {
        return SingletonInstance.cron4jPlugin;
    }

    public static void addTask(String cron, Runnable task) {
        SingletonInstance.cron4jPlugin.addTask(cron, task);
    }

    private static class SingletonInstance {
        static CustomCron4jPlugin cron4jPlugin = new CustomCron4jPlugin(PropKit.use("properties/application.properties"), "cron4j");

    }

}