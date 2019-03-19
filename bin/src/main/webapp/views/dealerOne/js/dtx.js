var formSelects = layui.formSelects;
var ctx = $("#ctx").val();
var month = $("#month").val();
var l_month = $("#l_month").val();
var ll_month = $("#ll_month").val();
var canEdit_dtx = $("#canEdit_dtx").val();
var area_code = $("#area_code").val();
var next_month = $("#next_month").val();
var role_name = $("#role_name").val();

var tableConfig_dtx = {
    elem: '#one_level_dtx',
    id: 'one_level_dtx_table_id',
    url: ctx + "/dealerOne/list",
    cols: [[
        {field: 'dealer_name', title: '商业公司名称', width: 250, fixed: "left"}
        , {field: 'area_name', title: '大区', width: 90}
        , {field: 'regin_name', title: '省份', width: 90}
        , {field: 'business_manager_name', title: '商务经理', width: 90}
        , {field: 'ssy_sj_kc', title: ll_month + '月实际库存', width: 120, align: 'right'}
        , {field: 'sy_yg_jhl', title: l_month + '月预估进货', width: 120, align: 'right'}
        , {field: 'sy_jhl', title: l_month + '月实际进货', width: 120, align: 'right'}
        , {field: 'sy_yg_xsl', title: l_month + '月销售预估', width: 120, align: 'right'}
        , {field: 'sy_sj_xsl', title: l_month + '月实际销售', width: 120, align: 'right'}
        , {field: 'sy_ll_kc', title: l_month + '月理论库存', width: 120, align: 'right'}
        , {field: 'sy_sj_kc', title: l_month + '月实际库存', width: 120, align: 'right'}
        , {field: 'sy_kcts', title: l_month + '月库存天数', width: 120, align: 'right'}
        , {field: 'cyyy', title: l_month + '月库存差异原因', width: 150}
        , {field: 'by_xtfp_cgl', title: month + '月系统分配采购', width: 150, align: 'right'}
        , {field: 'xy_xtfp_cgl', title: next_month + '月系统分配采购', width: 150, align: 'right', align: 'right'}
        , {field: 'by_jhl', title: month + '月实际进货', width: 120, align: 'right'}
        , {field: 'by_yg_jhl', title: month + '月进货预估', width: 120, align: 'right'}
        , {field: 'by_yg_xsl', title: month + '月销售预估', width: 120, align: 'right'}
        , {field: 'by_kc', title: month + '月实际库存', width: 120, align: 'right'}
        , {field: 'by_kcts', title: month + '月库存天数', width: 120, align: 'right'}
        , {field: 'order_num', title: month + '月下单数', width: 110, align: 'right'}
        , {field: 'diff_order_num', title: month + '月下单数差异', width: 130, align: 'right'}
    ]],
    loading: true,
    width: $(window).width() - $(".layui-side").width() - 50,
    height: 'full-380',
    where: {}
}

var layui_table_dtx;
layui.use(['form', 'layedit', 'table'], function () {
    var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
    layui_table_dtx = layui.table;
    var table_dtx;
    setProductSelect_dtx(function () {
        tableConfig_dtx.where.product_id = layui.formSelects.value('product_select_dtx', 'valStr');
        tableConfig_dtx.where.is_self = "false";  //这里是去取大区经理顶替的商务经理要填写的数据.
        table_dtx = renderTable(tableConfig_dtx, canEdit_dtx, function (res, curr, count) {
            var tableData = res;
            $.each($("#dtx_table_div .layui-table-main tr[data-index]"), function (i, dom) {
                //第一个锁定期，总部导入数据和之前商务经理填写的数据如果有差异，这里需要标红
                var datum = tableData.data[i];
                var flagStatus = datum.flag_status;
                if (flagStatus != undefined && flagStatus == 3) {
                    $(dom).find("td[data-field=sy_ll_kc]").css("color", "#FF5722");
                }

                var sy_ll_kc = $(dom).find("td[data-field=sy_ll_kc] div").html();
                var sy_sj_kc = $(dom).find("td[data-field=sy_sj_kc] div").html();
                var cyyy = $(dom).find("td[data-field=cyyy] div").html();

                var dealer_name = $(dom).find("td[data-field=dealer_name] div").html();

                //提示填写差异原因
                if (sy_sj_kc != null && sy_sj_kc != "" && sy_ll_kc != sy_sj_kc && cyyy.length == 0) {
                    if ("总计数据" != dealer_name && canEdit_dtx != undefined && canEdit_dtx != "") {
                        setEditTdErrorStyle2(datum, "cyyy");
                    }
                }

                if ("总计数据" == dealer_name) {
                    //统计的上一行清空
                    $($(dom).prev().find("td")[0]).attr("colspan", 20);
                    deleteDom($($(dom).prev().find("td")[0]), 19);

                    //统计行需要锁定，不允许有点击事件
                    $(dom).find("td").each(function (index, item) {
                        $(item).removeAttr("data-edit");
                    });

                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").attr("colspan", 5);
                    $(dom).find("td[data-field=area_name] div").css("margin-left", "40%");
                }
            })
        });
    });
    //监听表格编辑事件
    layui_table_dtx.on('edit(one_level_dtx_filter)', function (obj) { //注：edit是固定事件名，test是table原始容器的属性 lay-filter="对应的值"
        var row_data = obj.data;
        var update_data = obj.value;
        var column = obj.field;
        if (column != 'cyyy') {
            update_data = update_data == "" ? "0" : update_data;
            if (!is_number2(update_data)) {
                showTips("请填写正确的数字");
                setEditTdErrorStyle2(row_data, column);
                return;
            } else {
                clearEditTdErrorStyle2(row_data, column)
            }
        }

        if (column == 'cyyy' && update_data.length > 0) {
            update_data = "'" + update_data + "'";
            clearEditTdErrorStyle2(row_data, "cyyy");
        }

        //开始清求后段保存编辑的数据
        $.getRemoteDate(ctx + "/dealerOne/commitBusinessData", {
            id: row_data.id,
            dealer_id: row_data.dealer_id,
            product_id: row_data.product_id,
            column: column,
            val: update_data
        }, function (data) {
            if (data.data != undefined && data.data != '') {
                var oldDate = data.data.old_date;
                //差异原因
                var cyyy_reason = row_data.cyyy == null ? "" : row_data.cyyy;
                //这就说明上月理论库存被计算出来了，这时候要和实际库存做比较，如果差距>1则需要将差异原因变成红色data-field="cyyy"

                //如果计算到了上月理论库存，则直接更新掉这个值
                //如果计算到了上月理论库存，则直接更新掉这个值
                if (data.data.sy_ll_kc.length > 0) {
                    $("#dtx_table_div .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_ll_kc] div").html(parseInt(data.data.sy_ll_kc) + "");
                    if (new Number(data.data.sy_ll_kc) != row_data.sy_sj_kc && cyyy_reason.length == 0) {
                        setEditTdErrorStyle2(row_data, "cyyy");
                    } else {
                        clearEditTdErrorStyle2(row_data, "cyyy");
                    }
                    row_data.sy_ll_kc = data.data.sy_ll_kc;
                }

                //上月库存天数联动
                if (data.data.stock_day.length > 0) {
                    $("#dtx_table_div .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_kcts] div").html(parseFloat(data.data.stock_day) + "");
                }

                //上月实际库存
                if (data.data.sy_sj_kc.length > 0) {
                    $("#dtx_table_div .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_sj_kc] div").html(parseInt(data.data.sy_sj_kc) + "");
                    var actual_stock_quantity = 0;
                    if (oldDate != undefined && oldDate != null) {
                        if (oldDate.actual_stock_quantity != undefined && oldDate.actual_stock_quantity != null) {
                            actual_stock_quantity = oldDate.actual_stock_quantity;
                        }
                    }
                    if (column != "sy_sj_kc") {
                        genTotal("sy_sj_kc", new Number(data.data.sy_sj_kc) - new Number(actual_stock_quantity));
                    }
                    genTotal("sy_ll_kc", new Number(data.data.sy_sj_kc) - new Number(actual_stock_quantity));
                }

                //本月库存联动
                if (data.data.by_kc.length > 0) {
                    $("#dtx_table_div .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=by_kc] div").html(parseInt(data.data.by_kc) + "");
                    var actual_stock_quantity = 0;
                    if (oldDate != undefined && oldDate != null) {
                        if (oldDate.actual_stock_quantity != undefined && oldDate.actual_stock_quantity != null) {
                            actual_stock_quantity = oldDate.actual_stock_quantity;
                        }
                    }
                    if (column != "by_kc") {
                        genTotal("by_kc", new Number(data.data.by_kc) - new Number(actual_stock_quantity));
                    }
                }

                //本月库存天数联动
                if (data.data.by_kc_ts.length > 0) {
                    $("#dtx_table_div .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=by_kcts] div").html(parseFloat(data.data.by_kc_ts) + "");
                }


                //这里看上月理论库存和上月实际库存是否一样，不一样，则看差异原因是否填写，没填写则飙红
                if (row_data.sy_ll_kc != null && row_data.sy_sj_kc != null && row_data.sy_ll_kc != row_data.sy_sj_kc && cyyy_reason.length == 0) {
                    setEditTdErrorStyle2(row_data, "cyyy");
                } else {
                    clearEditTdErrorStyle2(row_data, "cyyy");
                }

                //被修改的列的汇总
                if ("cyyy" != column) {
                    genTotal(column, new Number(update_data) - new Number(data.data.old_value));
                }
            }
        }, function (err) {
        })
    });
});

//初始化函数
$(function () {
    if ("大区经理" == role_name) {
        setRegionSelect_dtx(area_code);
        //初始加载商务经理的下拉列表
        // setBusinessManagerSelect_dtx();
        //初始加载经销商信息
        setDealerSelect_dtx();
    }
})

//搜索按钮事件
function searchEvent_dtx() {
    var product_id = layui.formSelects.value('product_select_dtx', 'valStr');
    var area_id = formSelects.value('area_select_dtx', 'valStr');
    var region_id = formSelects.value('region_select_dtx', 'valStr');
    var business_id = formSelects.value('business_select_dtx', 'valStr');
    var dealer_id = formSelects.value('dealer_select_dtx', 'valStr');
    tableConfig_dtx.where.product_id = product_id;
    tableConfig_dtx.where.area_id = area_id;
    tableConfig_dtx.where.region_id = region_id;
    tableConfig_dtx.where.business_id = business_id;
    tableConfig_dtx.where.dealer_id = dealer_id;
    reloadTable('one_level_dtx_table_id', tableConfig_dtx)
}


//加载省份下拉宽，保持和大区联动
function setRegionSelect_dtx(area) {
    if (area.length != 0) {
        formSelects.data('region_select_dtx', "server", {
            url: ctx + '/dealerOne/getRegionList?area_ids=' + area + '&is_self=false&level=1',
            keyName: 'name',
            keyVal: "id"
        });
    } else {
        formSelects.data('region_select_dtx', 'local', {
            arr: []
        });
    }
}

//加载经销商下拉宽
function setDealerSelect_dtx() {
    //赋值大区下拉宽
    formSelects.data('dealer_select_dtx', "server", {
        url: ctx + '/dealerOne/getDealerList?level=1&isSelf=false',
        keyName: 'name',
        keyVal: "id"
    });
}

//赋值产品下拉宽
function setProductSelect_dtx(afterRender) {
    formSelects.data('product_select_dtx', "server", {
        url: ctx + '/dealerOne/getProductList?isSelf=false',
        keyName: 'name',
        keyVal: "id",
        beforeSuccess: function (id, url, searchVal, result) {
            result = result.data;
            $.each(result, function (index, item) {
                item.name && (item.name = item.name.split('').join(''))
            });
            if (result.length == 0) {
                return result;
            }
            layui.formSelects.value('product_select_dtx', [result[0].id]);
            $("#product_name_lable_dtx").html(result[0].name);
            return result;
        },
        success: function (id, url, val, result) {
            if (typeof afterRender != 'undefined' && typeof afterRender == 'function') {
                afterRender();
            }
        }, error: function (id, url, searchVal, err) {           //使用远程方式的error回调
            console.log(err);   //err对象
        },
    });
}

//赋值商务经理下拉宽
function setBusinessManagerSelect_dtx() {
    formSelects.data('business_select_dtx', "server", {
        url: ctx + '/dealerOne/getBusinessManagerListForDtx',
        keyName: 'name',
        keyVal: "id",
        beforeSuccess: function (id, url, searchVal, result) {
            result = result.data;
            $.each(result, function (index, item) {
                item.name && (item.name = item.name.split('').join(''))
            });
            if (result.length == 0) {
                return result;
            }
            return result;
        }
    });
}

//监听产品下拉事件
layui.formSelects.on('product_select_dtx', function (id, vals, val, isAdd, isDisabled) {
    var value = layui.formSelects.value('product_select_dtx', 'val');
    if (isDisabled) {
        if (val.val == value[0]) {
            showTips("最少选择一个，不能清空", 800, function () {
                layui.formSelects.value('product_select_dtx', value);
                return false;
            })
        }
    }
    $("#product_name_lable_dtx").html(val.name);
});


function export_dtx_sj() {
    var product_id = layui.formSelects.value('product_select_dtx', 'valStr');
    var area_id = formSelects.value('area_select_dtx', 'valStr');
    var region_id = formSelects.value('region_select_dtx', 'valStr');
    var business_id = formSelects.value('business_select_dtx', 'valStr');
    var dealer_id = formSelects.value('dealer_select_dtx', 'valStr');
    var url = ctx + "/exportFile/exportFistLevelData?"
        + "product_id=" + product_id
        + "&area_id=" + area_id
        + "&region_id=" + region_id
        + "&business_id=" + business_id
        + "&dealer_id=" + dealer_id
        + "&isSelf=false";
    window.location.href = url;
}

/**
 * 需要填写差异数据时设置单元格错误样式
 */
function setEditTdErrorStyle2(data, field, addClass) {
    var index = data.LAY_TABLE_INDEX;
    if (index >= 0 && field) {
        var elem = $("#dtx_table_div").find("tr[data-index='" + index + "']").find("td[data-field='" + field + "']");
        elem.addClass("data-error");
        if (addClass) {
            elem.addClass(addClass);
        }

        //TODO 已经提前修改好，到时候放开注释即可,不需要删除底下未注释的行
        if (field == "diff_cause" || field == "cyyy" || field == "sy_sj_kc" || field == "by_kc") {
            var input = elem.find(".custom-layui-input");
            if (input.length > 0) {
                input.remove();
            }
            var value = data[field] ? data[field] : "";
            if (field == "by_kc") {
                elem.append("<button class='layui-input layui-table-edit custom-layui-input' style='border-color:#FF5722;text-align: left;'>" + value + "</button>");
                return;
            }
            elem.append("<input class='layui-input layui-table-edit custom-layui-input' style='border-color:#FF5722;' value='" + value + "'>");
        }

        //TODO 到时记得删除
        // if (field == "diff_cause" || field == "cyyy") {
        //     elem.append("<input class='layui-input layui-table-edit custom-layui-input' style='border-color:#FF5722;'>");
        // }
    }
}

function deleteDom(dom, num) {
    for (var i = 0; i < num; i++) {
        dom.next().remove();
    }
}

/**
 * 清除差异数据单元格错误样式
 */
function clearEditTdErrorStyle2(data, field, removeClass) {
    var index = data.LAY_TABLE_INDEX;
    if (index >= 0 && field) {
        var elem = $("#dtx_table_div").find("tr[data-index='" + index + "']").find("td[data-field='" + field + "']");
        elem.removeClass("data-error");
        if (removeClass) {
            elem.removeClass(removeClass);
        }
        var input = elem.find(".custom-layui-input");
        if (input.length > 0) {
            input.remove();
        }
    }
}

function showErrorCyyy2() {
    var ss = $("#dtx_table_div .layui-table-main").find(".data-error");
    if (ss.length > 0) {
        showConfirm("<div style='color:#FF5722'>您有实际库存与理论库存差异解释未填或数据格式不正确，请留意红框标出部分！</div>");
    } else {
        showConfirm("<div style='color:#5FB878'>保存成功</div>");
    }
}

function is_number2(val) {
    var r = /^\-?[0-9][0-9]*$/;　　//判断是否为整数
    return r.test(val);
}