package com.site.quartz;

import com.jfinal.kit.Prop;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.cron4j.ITask;
import it.sauronsoftware.cron4j.ProcessTask;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomCron4jPlugin implements IPlugin {

    private List<TaskInfo> taskInfoList;
    public static final String defaultConfigName = "cron4j";

    public CustomCron4jPlugin() {
        this.taskInfoList = new ArrayList();
    }

    public CustomCron4jPlugin(String configFile) {
        this(new Prop(configFile), "cron4j");
    }

    public CustomCron4jPlugin(Prop configProp) {
        this(configProp, "cron4j");
    }

    public CustomCron4jPlugin(String configFile, String configName) {
        this(new Prop(configFile), configName);
    }

    public CustomCron4jPlugin(Prop configProp, String configName) {
        this.taskInfoList = new ArrayList();

        try {
            this.addTask(configProp, configName);
        } catch (RuntimeException var4) {
            throw var4;
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    private void addTask(Prop configProp, String configName) throws Exception {
        String configNameValue = configProp.get(configName);
        if (StrKit.isBlank(configNameValue)) {
            throw new IllegalArgumentException("The value of configName: " + configName + " can not be blank.");
        } else {
            String[] taskNameArray = configNameValue.trim().split(",");
            String[] var5 = taskNameArray;
            int var6 = taskNameArray.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String taskName = var5[var7];
                if (StrKit.isBlank(taskName)) {
                    throw new IllegalArgumentException("taskName can not be blank.");
                }

                taskName = taskName.trim();
                String taskCron = configProp.get(taskName + ".cron");
                if (StrKit.isBlank(taskCron)) {
                    throw new IllegalArgumentException(taskName + ".cron not found.");
                }

                taskCron = taskCron.trim();
                String taskClass = configProp.get(taskName + ".class");
                if (StrKit.isBlank(taskClass)) {
                    throw new IllegalArgumentException(taskName + ".class not found.");
                }

                taskClass = taskClass.trim();
                Object taskObj = Class.forName(taskClass).newInstance();
                if (!(taskObj instanceof Runnable) && !(taskObj instanceof Task)) {
                    throw new IllegalArgumentException("Task 必须是 Runnable、ITask、ProcessTask 或者 Task 类型");
                }

                boolean taskDaemon = configProp.getBoolean(taskName + ".daemon", true).booleanValue();
                boolean taskEnable = configProp.getBoolean(taskName + ".enable", true).booleanValue();
                this.taskInfoList.add(new CustomCron4jPlugin.TaskInfo(taskCron, taskObj, taskDaemon, taskEnable));
            }

        }
    }

    public CustomCron4jPlugin addTask(String cron, Runnable task, boolean daemon, boolean enable) {
        this.taskInfoList.add(new CustomCron4jPlugin.TaskInfo(cron, task, daemon, enable));
        return this;
    }

    public CustomCron4jPlugin addTask(String cron, Runnable task, boolean daemon) {
        return this.addTask(cron, task, daemon, true);
    }

    public CustomCron4jPlugin addTask(String cron, Runnable task) {
        return this.addTask(cron, task, true, true);
    }

    public CustomCron4jPlugin addTask(String cron, ProcessTask processTask, boolean daemon, boolean enable) {
        this.taskInfoList.add(new CustomCron4jPlugin.TaskInfo(cron, processTask, daemon, enable));
        return this;
    }

    public CustomCron4jPlugin addTask(String cron, ProcessTask processTask, boolean daemon) {
        return this.addTask(cron, processTask, daemon, true);
    }

    public CustomCron4jPlugin addTask(String cron, ProcessTask processTask) {
        return this.addTask(cron, processTask, true, true);
    }

    public CustomCron4jPlugin addTask(String cron, Task task, boolean daemon, boolean enable) {
        this.taskInfoList.add(new CustomCron4jPlugin.TaskInfo(cron, task, daemon, enable));
        return this;
    }

    public CustomCron4jPlugin addTask(String cron, Task task, boolean daemon) {
        return this.addTask(cron, task, daemon, true);
    }

    public CustomCron4jPlugin addTask(String cron, Task task) {
        return this.addTask(cron, task, true, true);
    }

    public boolean start() {
        Iterator var1 = this.taskInfoList.iterator();

        CustomCron4jPlugin.TaskInfo taskInfo;
        while(var1.hasNext()) {
            taskInfo = (CustomCron4jPlugin.TaskInfo)var1.next();
            taskInfo.schedule();
        }

        var1 = this.taskInfoList.iterator();

        while(var1.hasNext()) {
            taskInfo = (CustomCron4jPlugin.TaskInfo)var1.next();
            taskInfo.start();
        }

        return true;
    }

    public boolean stop() {
        Iterator var1 = this.taskInfoList.iterator();

        while(var1.hasNext()) {
            CustomCron4jPlugin.TaskInfo taskInfo = (CustomCron4jPlugin.TaskInfo)var1.next();
            taskInfo.stop();
        }

        return true;
    }


    public boolean startTheLastOne() {
        int index = this.taskInfoList.size();
        CustomCron4jPlugin.TaskInfo taskInfo = this.taskInfoList.get(index - 1);
        taskInfo.schedule();
        taskInfo.start();
        return true;
    }

    private static class TaskInfo {
        Scheduler scheduler;
        String cron;
        Object task;
        boolean daemon;
        boolean enable;

        TaskInfo(String cron, Object task, boolean daemon, boolean enable) {
            if (StrKit.isBlank(cron)) {
                throw new IllegalArgumentException("cron 不能为空.");
            } else if (task == null) {
                throw new IllegalArgumentException("task 不能为 null.");
            } else {
                this.cron = cron.trim();
                this.task = task;
                this.daemon = daemon;
                this.enable = enable;
            }
        }

        void schedule() {
            if (this.enable) {
                this.scheduler = new Scheduler();
                if (this.task instanceof Runnable) {
                    this.scheduler.schedule(this.cron, (Runnable)this.task);
                } else {
                    if (!(this.task instanceof Task)) {
                        this.scheduler = null;
                        throw new IllegalStateException("Task 必须是 Runnable、ITask、ProcessTask 或者 Task 类型");
                    }

                    this.scheduler.schedule(this.cron, (Task)this.task);
                }

                this.scheduler.setDaemon(this.daemon);
            }

        }

        void start() {
            if (this.enable) {
                this.scheduler.start();
            }

        }

        void stop() {
            if (this.enable) {
                if (this.task instanceof ITask) {
                    ((ITask)this.task).stop();
                }

                this.scheduler.stop();
            }

        }
    }
}
