package com.site.utils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by WSQ on 2016/6/25.
 */
public class QueryUtil<T> {
    private int pageNumber;                //当前页码
    private int pageSize;                //每页显示行数
    private String sort;                //排序字段
    private String order;               //排序方式
    private String defaultOrder;        //默认排序
    private String searchValue;            //快速搜索内容
    private String[] searchColunm;        //快速搜索字段
    private String groupColunm;            // 分组条件
    private String sqlSelect = "select * ";
    private String sqlSelectExecute = "select * ";
    private String sqlExceptSelect = "";
    private String sqlExceptSelectExecute = "";
    private String condSql = "";           //自动化拼接的SQL
    private List<QueryParam> queryParams = new ArrayList<>();
    private LinkedList<Object> paramValue = new LinkedList<>();    //查询条件的值
    private String dateBase = "manage_arp";
    private Model<?> model;
    private Page<?> page;
    private String appendSql = "";
    private String executeSql = "";         //执行的sql

    private class QueryParam {
        private String cond;
        private String key;
        private String operate;
        private Object value;

        public String getKey() {
            return key;
        }

        public QueryParam setKey(String key) {
            this.key = key;
            return this;
        }

        public String getOperate() {
            return operate;
        }

        public QueryParam setOperate(String operate) {
            this.operate = operate;
            return this;
        }

        public Object getValue() {
            return value;
        }

        public QueryParam setValue(String value) {
            this.value = value;
            return this;
        }

        public String getCond() {
            return cond;
        }

        public QueryParam setCond(String cond) {
            this.cond = cond;
            return this;
        }
    }

    public class SearchResult implements Serializable {
        private int code;
        private List<T> data;
        private int count;
        private int totalPage;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public List getData() {
            return data;
        }

        public void setData(List data) {
            this.data = data;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public void addQueryParam(String key, String operate, Object value) {
        this.page = null;
        QueryParam param = new QueryParam().setKey(key).setOperate(operate).setValue(ToolFunction.me.objToString(value));
        this.queryParams.add(param);
    }

    /**
     * @param key
     * @param operate
     * @param value
     * @param cond    是否使用where还是and拼接此条件 ； EX：where、and
     */
    public void addQueryParam(String key, String operate, Object value, String cond) {
        this.page = null;
        QueryParam param = new QueryParam().setKey(key).setOperate(operate).setCond(cond).setValue(ToolFunction.me.objToString(value));
        this.queryParams.add(param);
    }

    public void addQueryParam(String key, String operate, Object value, Boolean overWrite) {
        this.page = null;
        this.paramValue = new LinkedList<Object>();
        if (overWrite) {
            Boolean exist = false;
            for (QueryParam queryParam : queryParams) {
                if (queryParam.key.equals(key.trim()) && queryParam.operate.equals(operate)) {
                    exist = true;
                    queryParam.setValue(ToolFunction.me.objToString(value));
                }
            }
            if (!exist) {
                QueryParam param = new QueryParam().setKey(key).setOperate(operate).setValue(ToolFunction.me.objToString(value));
                this.queryParams.add(param);
            }
        } else {
            QueryParam param = new QueryParam().setKey(key).setOperate(operate).setValue(ToolFunction.me.objToString(value));
            this.queryParams.add(param);
        }
    }

    public void appendSql(String sql) {
        StringBuffer sbf = new StringBuffer(appendSql);
        sbf.append(" ");
        sbf.append(sql);
        sbf.append(" ");
        appendSql = sbf.toString();
    }


    public QueryUtil() {
    }

    public QueryUtil(String dateBase) {
        if (StrKit.notBlank(dateBase))
            this.dateBase = dateBase;
    }

    public QueryUtil(Model<?> m) {
        if (m != null)
            this.model = m;
    }

    public QueryUtil(Class<? extends Model> clazz) {
        if (clazz != null) {
            try {
                this.model = clazz.newInstance();
            } catch (InstantiationException e) {
                System.out.println("初始化queryUtil失败：" + e.getMessage());
            } catch (IllegalAccessException e) {
                System.out.println("初始化queryUtil失败：" + e.getMessage());
            }
        }
    }

    public Map<String, Object> getResult() {
        Map<String, Object> map = new HashMap<>();
        try {
            Page<?> page = result();
            if (page == null) {
                map.put("code", 1);
                map.put("message", "没有查询到数据");
            } else {
                map.put("code", 0);
                map.put("data", page.getList());
                map.put("count", page.getTotalRow());
                map.put("totalPage", page.getTotalPage());
                map.put("message", "");
            }
        } catch (Exception e) {
            map.put("code", 1);
            map.put("message", e.getMessage());
        }
        return map;
    }

    public SearchResult getSearchResult() {
        SearchResult searchResult = new SearchResult();
        try {
            Page<?> page = result();
            if (page == null) {
                searchResult.setCode(1);
                searchResult.setMessage("没有查询到数据");
            } else {
                searchResult.setCode(0);
                searchResult.setData(page.getList());
                searchResult.setCount(page.getTotalRow());
                searchResult.setTotalPage(page.getTotalPage());
                searchResult.setMessage("");
            }
        } catch (Exception e) {
            searchResult.setCode(1);
            searchResult.setMessage(e.getMessage());
        }
        return searchResult;
    }

    public Page<?> result() {
        if (page != null) {
            return page;
        }
        this.generateSqlExceptSelect();
        if (getSqlExceptSelectExecute().toLowerCase().contains(" group by")) {
            setSqlExceptSelectExecute(" from (" + getSqlSelect() + " " + getSqlExceptSelectExecute() + ") ttt");
            setSqlSelect("select *  ");
        }
        System.out.println("执行的SQL：" + getSqlSelectExecute() + " " + getSqlExceptSelectExecute());
        if (paramValue != null && paramValue.size() > 0) {
            if (model == null) {
                page = Db.use(this.dateBase).paginate(getPageNumber(), getPageSize(), getSqlSelectExecute(), getSqlExceptSelectExecute(), paramValue.toArray());
            } else {
                page = this.model.paginate(getPageNumber(), getPageSize(), getSqlSelectExecute(), getSqlExceptSelectExecute(), paramValue.toArray());
            }
        } else {
            if (model == null) {
                page = Db.use(this.dateBase).paginate(getPageNumber(), getPageSize(), getSqlSelectExecute(), getSqlExceptSelectExecute());
            } else {
                page = this.model.paginate(getPageNumber(), getPageSize(), getSqlSelectExecute(), getSqlExceptSelectExecute());
            }
        }
        return page;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String[] getSearchColunm() {
        return searchColunm;
    }

    public void setSearchColunm(String[] searchColunm) {
        this.searchColunm = searchColunm;
    }

    public String getGroupColunm() {
        return groupColunm;
    }

    public void setGroupColunm(String groupColunm) {
        this.groupColunm = groupColunm;
    }

    public String getSqlSelect() {
        return sqlSelect;
    }

    public Page<?> getPage() {
        return page;
    }

    public void setPage(Page<?> page) {
        this.page = page;
    }

    public void setExecuteSql(String executeSql) {
        this.executeSql = executeSql;
    }

    public void setParamValue(LinkedList<Object> paramValue) {
        this.paramValue = paramValue;
    }

    public String getExecuteSql() {
        if (StringUtils.isBlank(executeSql)) {
            this.generateSqlExceptSelect();
            executeSql = getSqlSelectExecute() + " " + getSqlExceptSelectExecute();
        }
        return executeSql;
    }

    public void setSqlSelect(String sqlSelect) {
        this.sqlSelectExecute = sqlSelect;
        this.sqlSelect = sqlSelect;
    }

    private void generateSqlExceptSelect() {
        StringBuffer old_sql = new StringBuffer(this.sqlExceptSelect);
        setCondSql("");
        StringBuffer sql = new StringBuffer(this.condSql);
        if (old_sql.toString().toLowerCase().indexOf("where") < 0) {
            sql.append(" where 1=1");
        }
        for (int i = 0; i < queryParams.size(); i++) {
            QueryParam param = queryParams.get(i);
            if (param.getKey() != null && !"".equals(param.getKey())
                    && param.getValue() != null && !"".equals(param.getValue())) {
                if (param.getCond() != null && !StrKit.isBlank(param.getCond())) {
                    sql.append(" " + param.getCond() + " ");
                } else {
                    sql.append(" and ");
                }
                sql.append(param.getKey());
                //运算符
                if (param.getOperate() == null) {
                    sql.append(" = ?");
                    paramValue.add(param.getValue());
                } else {
                    switch (param.getOperate()) {
                        case "like":
                            sql.append(" like ?");
                            paramValue.add("%" + param.getValue() + "%");
                            break;
                        case "in":
                            sql.append(" in (");
                            //防止注入构建SQL
                            String[] vals = ToolFunction.me.objToString(param.getValue()).split(",");
                            for (int j = 0; j < vals.length; j++) {
                                if (j > 0) sql.append(",");
                                sql.append("'");
                                String val = vals[j].replace("'", "''");
                                sql.append(val);
                                sql.append("'");
                            }
                            sql.append(")");
                            break;
                        case "not in":
                            sql.append(" not in (");
                            //防止注入构建SQL
                            String[] vals_n = ToolFunction.me.objToString(param.getValue()).split(",");
                            for (int j = 0; j < vals_n.length; j++) {
                                if (j > 0) sql.append(",");
                                sql.append("'");
                                String val = vals_n[j].replace("'", "''");
                                sql.append(val);
                                sql.append("'");
                            }
                            sql.append(")");
                            break;
                        case "%like":
                            sql.append(" like ?");
                            paramValue.add("%" + param.getValue());
                            break;
                        case "like%":
                            sql.append(" like ?");
                            paramValue.add(param.getValue() + "%");
                            break;
                        case "%like%":
                            sql.append(" like ?");
                            paramValue.add("%" + param.getValue() + "%");
                            break;
                        case "is":
                            sql.append(" is  null");
                            break;
                        case "is not":
                            sql.append(" is  not null");
                            break;
                        default:
                            sql.append(" " + param.getOperate() + " ?");
                            paramValue.add(param.getValue());
                    }
                }
            }
        }
        //快速搜索SQL
        sql.append(getSearchSql());
        if (StrKit.notBlank(appendSql)) {
            sql.append(appendSql);
        }
        //构建分组SQL
        if (groupColunm != null && !"".equals(groupColunm)) {
            sql.append(" group by ");
            sql.append(groupColunm);
        }
        //构建排序SQL
        if (StrKit.notBlank(order)) {
            sql.append(" order by ");
            String[] columns = this.sort.split(",");
            String[] modes = this.order.split(",");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]);
                sql.append(" ");
                sql.append(modes[i]);
                if (i != columns.length - 1) {
                    sql.append(",");
                }
                if (i >= modes.length) {
                    break;
                }
            }
            if (StrKit.notBlank(defaultOrder)) {
                sql.append(",");
                sql.append(defaultOrder);
            }
        } else {
            if (StrKit.notBlank(defaultOrder)) {
                sql.append(" order by ");
                sql.append(defaultOrder);
            }
        }
        setCondSql(sql.toString());
        setSqlExceptSelectExecute(getSqlExceptSelect() + " " + getCondSql());
    }

    //通过查询条件和排序条件重新构建SQL尾段
    public String getSearchSql() {
        StringBuffer sql = new StringBuffer("");
        if (this.searchValue != null && !"".equals(searchValue) && this.searchColunm.length > 0) {
            sql.append(" and (");
            for (int i = 0; i < this.searchColunm.length; i++) {
                if (i > 0) sql.append(" or ");
                sql.append(searchColunm[i]);
                sql.append(" like ?");
                paramValue.add("%" + searchValue + "%");
            }
            sql.append(")");
        }
        return sql.toString();
    }

    public String getSqlExceptSelect() {
        return sqlExceptSelect;
    }

    public void setSqlExceptSelect(String sqlExceptSelect) {
        this.sqlExceptSelect = sqlExceptSelect;
    }

    public String getCondSql() {
        return condSql;
    }

    public void setCondSql(String condSql) {
        this.condSql = condSql;
    }

    public String getSqlSelectExecute() {
        return sqlSelectExecute;
    }

    public void setSqlSelectExecute(String sqlSelectExecute) {
        this.sqlSelectExecute = sqlSelectExecute;
    }

    public String getSqlExceptSelectExecute() {
        return sqlExceptSelectExecute;
    }

    public void setSqlExceptSelectExecute(String sqlExceptSelectExecute) {
        this.sqlExceptSelectExecute = sqlExceptSelectExecute;
    }

    public Object[] getParamValue() {
        return paramValue.toArray();
    }

    public QueryUtil setQueryUtilDef() {
        //获取当前页码，默认1
        this.setPageNumber(1);
        //获取每页多少条数据，默认10
        this.setPageSize(10);
        this.setSqlSelect("select * ");
        return this;
    }
}
