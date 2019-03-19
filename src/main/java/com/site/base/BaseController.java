package com.site.base;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;
import com.site.core.model.User;
import com.site.utils.QueryUtil;
import com.site.utils.ToolFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class BaseController extends Controller {
    public Logger log;
    protected Result result = new Result();
    protected static String IMGURl_PREFIX = PropKit.get("qn_img_prefix");
    public static String SERVER_PATH;

    public BaseController() {
        Field[] fields = this.getClass().getDeclaredFields();
        this.log = Logger.getLogger(this.getClass());
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class clazz = field.getType();
            if (BaseService.class.isAssignableFrom(clazz) && clazz != BaseService.class) {
                try {
                    field.setAccessible(true);
                    field.set(this, BaseService.getInstance(clazz, this));
                } catch (IllegalAccessException e) {
                    System.out.println("自动注入失败" + e.getMessage());
                }
            }
        }
    }

    public void index() {
        render("update.html");
    }

    protected int getIntPara(String para) {
        return getIntPara(para, 0);
    }

    public User getLoginUser() {
        User user = getSessionAttr("user");
        if (user == null) {
            throw new ExceptionForLogin("需要登录才能访问的页面");
        }
        return user;
    }

    protected int getIntPara(String para, int def) {
        try {
            return Integer.parseInt(getPara(para));
        } catch (Exception e) {
            return def;
        }
    }

    protected QueryUtil getQueryUtil() {
        return getQueryUtil("");
    }

    protected QueryUtil getQueryUtil(Model<?> model) {
        QueryUtil queryUtil = new QueryUtil(model);
        setQueryUtilDef(queryUtil);
        return queryUtil;
    }

    protected QueryUtil getQueryUtil(Class<? extends Model> clazz) {
        QueryUtil queryUtil = new QueryUtil(clazz);
        setQueryUtilDef(queryUtil);
        return queryUtil;
    }

    protected QueryUtil getQueryUtil(String dateBaseConfig) {
        QueryUtil queryUtil = new QueryUtil(dateBaseConfig);
        setQueryUtilDef(queryUtil);
        return queryUtil;
    }

    private void setQueryUtilDef(QueryUtil queryUtil) {
        //获取当前页码，默认1
        if (StrKit.notBlank(getPara("page"))) {
            queryUtil.setPageNumber(getIntPara("page", 1));
        } else {
            queryUtil.setPageNumber(1);
        }
        //获取每页多少条数据，默认10
        if (StrKit.notBlank(getPara("limit"))) {
            queryUtil.setPageSize(getIntPara("limit", 10));
        } else {
            queryUtil.setPageSize(10);
        }
        //获取排序
        queryUtil.setSort(getPara("sort"));
        queryUtil.setOrder(getPara("order"));
        //获取快速查询内容
        queryUtil.setSearchValue(getPara("search"));

        queryUtil.setSqlSelect("select * ");
    }

    protected Model<?> getModel(Model<?> model) {
        Map<String, String[]> parasMap = getParaMap();
        try {
            for (Map.Entry<String, String[]> entry : parasMap.entrySet()) {
                String paraName = entry.getKey();

                Table table = TableMapping.me().getTable(model.getClass());
                Class<?> colType = table.getColumnType(paraName);

                String[] paraValueArray = entry.getValue();
                String paraValue = (paraValueArray != null && paraValueArray.length > 0) ? paraValueArray[0] : null;

                Object value = paraValue != null ? ToolFunction.me.convert(colType, paraValue) : null;
                model.set(paraName, value);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return model;
    }

    protected Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        Enumeration<String> v = getParaNames();
        while (v.hasMoreElements()) {
            String paraName = (String) v.nextElement();
            map.put(paraName, getPara(paraName));
        }
        return map;
    }

    /**
     * 初始化服务器路径
     * @return http://localhost:8080/
     */
    public void initServerPath() {
        if (SERVER_PATH != null) {
            return;
        }

        String downloadPath = PropKit.get("download.url");

        if (StringUtils.isBlank(downloadPath)) {
            //获取http和https
            String requestUrl = getRequest().getRequestURL().toString();
            String httpPath = requestUrl.substring(0, requestUrl.indexOf("//") + 2);
            downloadPath = httpPath + getRequest().getServerName() + ":" + getRequest().getServerPort() + "/";
        }
        log.info("初始化服务器URL为：" + downloadPath);
        SERVER_PATH = downloadPath;
    }

    /**
     * 获取服务器路径
     * @return http://localhost:8080/
     */
    public String getServerPath() {
        return SERVER_PATH;
    }

//    protected Result uploadImage(String key) {
//        UploadFile uploadFile = null;
//        Result result = new Result();
//        try {
//            uploadFile = getFile(key, "image", 500 * 1024);
//            String fileName = uploadFile.getFileName();
//            if (!uploadFile.getContentType().contains("image")) {
//                result.addError("文件类型不正确");
//                FileKit.delete(uploadFile.getFile());
//                return result;
//            }
//            Random random = new Random();
//            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//            String randomName = df.format(new Date()) + random.nextInt(100);
//            String hz = "." + uploadFile.getContentType().split("/")[1];
//            if (".jpeg".equals(hz)) {
//                hz = ".jpg";
//            }
//            // 上传FTP
//            String ftpFileName = randomName + hz;
//            FtpUtil.upload(C.config.get("图片FTP路径_会员系统"), uploadFile.getUploadPath() + File.separator + fileName, ftpFileName);
//            // 图片访问路径
//            String image = C.config.get("图片FTP路径_会员系统") + ftpFileName;
//
//            FileKit.delete(uploadFile.getFile());
//            result.setData(image);
//        } catch (Exception e) {
//            result.addError(e.getMessage());
//            if (uploadFile != null) {
//                FileKit.delete(uploadFile.getFile());
//            }
//        }
//        return result;
//    }
}
