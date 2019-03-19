jQuery.extend({
    getRemoteDate: function (url, para, calbck, errCalbck, showLoadingFlag) {
        var flag = true;
        flag = (showLoadingFlag != undefined && showLoadingFlag != null && showLoadingFlag == false);
        var loading_i;
        if (!flag) {
            loading_i = showLoading();
        }
        $.ajax({
            type: "get",
            url: url,
            data: para,
            async: false,
            error: function (request) {
                hideLoading(loading_i)
                alert("Connection error");
            },
            success: function (data) {
                hideLoading(loading_i)
                if (data.code == '0') {
                    calbck(data);
                } else if (data.code == '8') {
                    //跳转到登录页面
                    showTips("登录超时，即将跳转到登录页", 2000, function () {
                        parent.location.reload();//完成刷新
                    })
                } else {
                    errCalbck(data);
                }
            }
        });
    },
    postJson: function (url, para, calbck, errCalbck) {
        $.ajax({
            type: "POST",
            url: url,
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(para),
            dataType: "json",
            success: function (data) {
                if (data.code == '0') {
                    calbck(data);
                } else if (data.code == '8') {
                    //跳转到登录页面
                    showTips("登录超时，即将跳转到登录页", 2000, function () {
                        parent.location.reload();//完成刷新
                    })
                } else {
                    errCalbck(data);
                }
            },
            error: function (message) {
                $("#request-process-patent").html("提交数据失败！");
                errCalbck(message);
            }
        });
    },
    postForm: function (url, para, calbck, errCalbck) {
        $.ajax({
            type: "POST",
            url: url,
            data: para,
            dataType: "json",
            success: function (data) {
                if (data.code == '0') {
                    calbck(data);
                } else if (data.code == '8') {
                    //跳转到登录页面
                    showTips("登录超时，即将跳转到登录页", 2000, function () {
                        parent.location.reload();//完成刷新
                    })
                } else {
                    errCalbck(data);
                }
            },
            error: function (message) {
                $("#request-process-patent").html("提交数据失败！");
            }
        });
    },

    /**
     *
     * @param form_id
     * @param rules
     * @param message
     * @returns {*|jQuery}
     *  {
                user_name: {
                    required: true
                },
            }
     *{
                user_name: {
                    required: '用户名必填',
                },
            }
     *
     */
    validateForm: function (form_id, rules, message) {
        $.validator.setDefaults({ignore: ":hidden:not(select)"});
        return $("#" + form_id + "").validate({
            rules: rules,
            messages: message,
            errorPlacement: function (error, element) {
                error.appendTo(element.parent());
            }
        }).form();
    }
});
$.fn.extend({
    initForm: function (options) {
        //默认参数
        var defaults = {
            jsonValue: "",
            isDebug: false   //是否需要调试，这个用于开发阶段，发布阶段请将设置为false，默认为false,true将会把name value打印出来
        }
        //设置参数
        var setting = $.extend({}, defaults, options);
        var form = this;
        jsonValue = setting.jsonValue;
        //如果传入的json字符串，将转为json对象
        if ($.type(setting.jsonValue) === "string") {
            jsonValue = $.parseJSON(jsonValue);
        }
        //如果传入的json对象为空，则不做任何操作
        if (!$.isEmptyObject(jsonValue)) {
            var debugInfo = "";
            $.each(jsonValue, function (key, value) {
                //是否开启调试，开启将会把name value打印出来
                if (setting.isDebug) {
                    console.log("name:" + key + "; value:" + value);
                    debugInfo += "name:" + key + "; value:" + value + " || ";
                }
                var formField = form.find("[name='" + key + "']");
                if ($.type(formField[0]) === "undefined") {
                    if (setting.isDebug) {
                        console.log("can not find name:[" + key + "] in form!!!");    //没找到指定name的表单
                    }
                } else {
                    var fieldTagName = formField[0].tagName.toLowerCase();
                    if (fieldTagName == "input") {
                        if (formField.attr("type") == "radio") {
                            $("input:radio[name='" + key + "'][value='" + value + "']").attr("checked", "checked");
                        } else if (formField.attr("type") == "checkbox") {
                            $.each(value, function (key_, value_) {
                                $("input:checkbox[name='" + key + "'][value='" + value_.id + "']").attr("checked", "");
                            })
                        } else {
                            formField.val(value);
                        }
                    } else if (fieldTagName == "select") {
                        //do something special
                        formField.val(value);
                    } else if (fieldTagName == "textarea") {
                        //do something special
                        formField.val(value);
                    } else {
                        formField.val(value);
                    }
                }
            })
            if (setting.isDebug) {
                console.log(debugInfo);
            }
        }
        return form;    //返回对象，提供链式操作
    },

    loadPage: function (url, pra, callback) {
        var loading_i = showLoading();
        $(this).load(url, pra, function (response, status, xhr) {
            hideLoading(loading_i)
            callback(response, status, xhr);
        });
    }
});

function showLoading(msg) {
    if (msg == undefined || msg.length == 0)
        msg = '<div style="text-align: center;padding-top: 20px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;">加载中...请稍等<br/><br/></div>'
    // msg = '<div style="text-align: center;padding-top: 20px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;">你知道吗?<br/>努力 ≠ 玩命<br/>正在玩命干活中...请稍等<br/><br/></div>'

    top_la_index = top.layer.open({
        type: 1
        ,
        title: false //不显示标题栏
        ,
        closeBtn: false
        ,
        area: '300px;'
        ,
        shade: 0.8
        ,
        id: 'LAY_layuipro_12133' //设定一个id，防止重复弹出
        ,
        zIndex: layer.zIndex
        ,
        btnAlign: 'c'
        ,
        moveType: 0 //拖拽模式，0或者1
        ,
        content: msg
    });
    return top_la_index;
}

function hideLoading(index) {
    if (index != undefined && index != null && index != '') {
        top.layer.close(index);
    }
}

/**
 * 格式化日期
 * @param fmt
 * @returns {*}
 * @constructor
 */
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};


/**
 *js中更改日期
 * y年， m月， d日， h小时， n分钟，s秒
 */
Date.prototype.add = function (part, value) {
    value *= 1;
    if (isNaN(value)) {
        value = 0;
    }
    switch (part) {
        case "y":
            this.setFullYear(this.getFullYear() + value);
            break;
        case "m":
            this.setMonth(this.getMonth() + value);
            break;
        case "d":
            this.setDate(this.getDate() + value);
            break;
        case "h":
            this.setHours(this.getHours() + value);
            break;
        case "n":
            this.setMinutes(this.getMinutes() + value);
            break;
        case "s":
            this.setSeconds(this.getSeconds() + value);
            break;
        default:
    }
    return new Date(this);
};