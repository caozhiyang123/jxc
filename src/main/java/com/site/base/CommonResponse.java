package com.site.base;

/**
 * Created by Administrator on 2016/3/23.
 */
public class CommonResponse<T> extends BaseCommonResponse {

    private Integer total;      //如果是数组信息，返回数组的总条数

    private T data;             //数据主体内容


    /**
     * 创建一个不包含数据主体的返回对象
     *
     * @return
     */
    public static BaseCommonResponse createCommonResponseWithNone() {
        return new BaseCommonResponse();
    }

    public void CommonResponse(Integer total, T data) {
        this.total = total;
        this.data = data;
    }

    public CommonResponse() {
    }

    public CommonResponse(Integer code) {
        this.setCode(code);
    }

    public static CommonResponse createCommonResponseWithOK() {
        return new CommonResponse(ErrorCode.SUCCESS);
    }

    public static CommonResponse createCommonResponseWithERROR() {
        return new CommonResponse(ErrorCode.FAILED);
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
