package com.site.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseController;
import com.site.base.ExceptionForJson;
import com.site.core.model.User;
import com.site.definition.Constants;
import com.site.service.UserService;
import com.site.utils.ExcelUtil;
import com.site.utils.Md5Utils;
import com.site.utils.QueryUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 用户 管理
 * 描述：
 */
public class UserController extends BaseController {

    private static final Log log = Log.getLog(NoticeController.class);

    static UserService srv = UserService.me;

    /**
     * 列表
     * /user/list
     */
    public void list() {
        QueryUtil queryUtil = getQueryUtil(User.class);
        queryUtil.setSqlSelect("SELECT a.*, b. NAME AS create_user_name,c. NAME AS update_user_name");
        queryUtil.setSqlExceptSelect("FROM user a LEFT JOIN `user` b ON a.create_user_id = b.id LEFT JOIN `user` c ON a.update_user_id = c.id ");
        queryUtil.addQueryParam("a.is_delete", "=", "0");
        queryUtil.setSort("create_time");
        queryUtil.setOrder("DESC");
        queryUtil.setSearchColunm(new String[]{"a.name", "a.employee_number"});

        if (!StrKit.isBlank(getPara("enable")))
            queryUtil.addQueryParam("a.enable", "=", getPara("enable"));

        QueryUtil.SearchResult searchResult = queryUtil.getSearchResult();
        List<User> users = searchResult.getData();
        if (users != null) {
            users.forEach(user -> {

                user.put("status_name", Constants.ActiveState.getNameByValue(user.getStatus()));

                user.put("enable_name", Constants.EnableState.getNameByValue(user.getEnable()));

            });
        }

        renderJson(searchResult);
    }

    /**
     * 准备添加
     * /user/add
     */
    public void add() {
        setAttr("downloadUrl", getServerPath());
        render("add.html");
    }

    /**
     * 保存或更新
     * /user/save
     */
    @Before(Tx.class)
    public void save() {
        User user = getModel(User.class, "");
        if (user.getId() != null && user.getId() > 0) {
            user.setUpdateTime(new Date());
            user.setUpdateUserId(getLoginUser().getId());
            srv.update(user);
        } else {
            user.setCreateTime(new Date());
            user.setCreateUserId(getLoginUser().getId());
            srv.save(user);
        }

        renderJson(result);
    }

    /**
     * 准备更新
     * /user/edit
     */
    public void edit() {
        User user = srv.findById(getParaToInt("id"));
        setAttr("user", user);
        render("add.html");
    }

    /**
     * 删除
     * /user/delete
     */
    @Before(Tx.class)
    public void delete() {
        srv.delete(getParaToInt("id"));
        renderJson(result);
    }

    /**
     * 启用/禁用
     * /user/delete
     */
    @Before(Tx.class)
    public void enable() {
        User user = srv.findById(getParaToInt("id"));
        String enable = user.getEnable();
        user.setEnable(Constants.EnableState.getReverseValue(enable));
        user.setUpdateUserId(getLoginUser().getId());
        srv.update(user);
        renderJson(result);
    }

    /**
     * 文件上传
     * /user/import
     */
    @Before(Tx.class)
    public void importFile() {
        UploadFile file = getFile();
        JSONArray jsonArray = null;
        try {
            jsonArray = ExcelUtil.readExcel(file, new String[]{"name", "username", "password", "employee_number", "email", "role_name", "status"});
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.getFile().delete();
        }

        if (jsonArray == null || jsonArray.size() == 0) {
            throw new ExceptionForJson("文件数据异常，请修改后重试");
        }
        JSONObject validate = validateExcelData(jsonArray);

        Map result = new HashMap<>();
        String warnMessage = validate.getString("warnMessage");
        if (!validate.getBoolean("validate")) {
            result.put("code", "2");
            result.put("message", "上传失败");
            result.put("warnMessage", warnMessage);
            result.put("errorMessage", validate.getString("errorMessage"));
            renderJson(result);
            return;
        }

        JSONObject employeeMap = validate.getJSONObject("employeeMap");
        Set<String> employeeIds = employeeMap.keySet();

        long userId = getLoginUser().getId();
        employeeIds.forEach(employeeId -> {
            JSONObject jsonObject = employeeMap.getJSONObject(employeeId);
            User user = new User();

            String name = jsonObject.getString("name");
            if (StringUtils.isNotBlank(name)) {
                user.setName(name);
            }

            String username = jsonObject.getString("username");
            if (StringUtils.isNotBlank(username)) {
                user.setUsername(username);
            }

            String password = jsonObject.getString("password");
            if (StringUtils.isNotBlank(password)) {
                String md5PassWord = Md5Utils.toHex(password);
                user.setPassword(md5PassWord);
            }

            String employee_number = jsonObject.getString("employee_number");
            if (StringUtils.isNotBlank(employee_number)) {
                user.setEmployeeNumber(employee_number);
            }

            String email = jsonObject.getString("email");
            if (StringUtils.isNotBlank(email)) {
                user.setEmail(email);
            }

            String roleName = jsonObject.getString("role_name");
            if (StringUtils.isNotBlank(roleName)) {
                user.setRoleName(roleName);
            }

            String status = jsonObject.getString("status");
            if (StringUtils.isNotBlank(status)) {
                user.setStatus(status);
            }

            if (jsonObject.getBoolean("update")) {
                user.setUpdateUserId(userId);
                user.setId(jsonObject.getLong("user_id"));
                user.setIsDelete("0");
                user.update();
            } else {
                user.setCreateTime(new Date());
                user.setCreateUserId(userId);
                user.save();
            }
        });


        String code = "0";
        if (StringUtils.isNotBlank(warnMessage)) {
            code = "1";
        }

        result.put("code", code);
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("warnMessage", warnMessage);
        result.put("errorMessage", "");
        file.getFile().delete();
        renderJson(result);
    }

    /**
     * 验证数据是否正确并获取员工号
     *
     * @param jsonArray
     * @return
     */
    private JSONObject validateExcelData(JSONArray jsonArray) {
        JSONObject result = new JSONObject();
        result.put("validate", true);
        JSONObject employeeMap = new JSONObject();
        StringBuilder warnMessage = new StringBuilder();
        StringBuilder errorMessage = new StringBuilder();
        Set<String> usernames = new HashSet<>();
        Set<String> employIds = new HashSet<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            StringBuilder warnMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();

            String status = jsonObject.getString("status");
            if ("活跃".equals(status)) {
                jsonObject.put("status", 0);
            } else if ("不活跃".equals(status)) {
                jsonObject.put("status", 1);
            } else {
                warnMsg.append("活跃信息错误，系统将自动置为活跃;");
                jsonObject.put("status", 0);
            }

            String employeeId = jsonObject.getString("employee_number");
            String username = jsonObject.getString("username");
            if (StringUtils.isNotBlank(username)) {
                if (usernames.contains(username)) {
                    result.put("validate", false);
                    errorMsg.append("Excel中用户名 " + username + " 重复，建议使用用户姓名拼音+数字 避免重复;");
                } else {
                    usernames.add(username);
                    User user = User.dao.findFirst("select username,employee_number from user where username=?", username);
                    if (user != null && !user.getEmployeeNumber().equals(employeeId)) {
                        result.put("validate", false);
                        errorMsg.append("用户名重复，建议使用用户姓名拼音+数字 避免重复;");
                    }
                }
            }

            String roleName = jsonObject.getString("role_name");
            if (!"总部".equals(roleName) && !"大区经理".equals(roleName) && !"商务经理".equals(roleName)) {
                result.put("validate", false);
                errorMsg.append("角色信息错误，只能填写总部、大区经理、商务经理;");
            }

            if (StringUtils.isBlank(employeeId)) {
                result.put("validate", false);
                errorMsg.append("员工号为必填选项;");
            } else {
                if (employIds.contains(employeeId)) {
                    result.put("validate", false);
                    errorMsg.append("Excel中员工号 " + username + " 重复，请修改后重新上传;");
                } else {
                    employIds.add(employeeId);
                    User user = User.dao.findFirst("select employee_number,id from user where employee_number=?", employeeId);
                    if (user == null) {
                        jsonObject.put("update", false);
                    } else {
                        jsonObject.put("update", true);
                        jsonObject.put("user_id", user.getId());
                    }
                }
                employeeMap.put(employeeId, jsonObject);
            }

            if (StringUtils.isNotBlank(warnMsg)) {
                warnMessage.append("第" + (i + 2) + "行：" + warnMsg + "<br/>");
            }

            if (StringUtils.isNotBlank(errorMsg)) {
                errorMessage.append("第" + (i + 2) + "行：" + errorMsg + "<br/>");
            }

        }
        result.put("errorMessage", errorMessage.toString());
        result.put("warnMessage", warnMessage.toString());
        result.put("employeeMap", employeeMap);
        return result;
    }

    @Before(Tx.class)
    public void resetPasswordForIndex() {
        String old_password = getPara("old_password");
        String new_password = getPara("new_password");
        User user = getLoginUser();
        if (!user.getPassword().equals(Md5Utils.toHex(old_password))) {
            result.addError("旧密码不正确，请重新输入");
            renderJson(result);
            return;
        }
        user.setPassword(Md5Utils.toHex(new_password));
        user.update();
        setSessionAttr("user", user);
        renderJson(result);
    }

}