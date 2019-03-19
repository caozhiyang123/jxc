var formSelects = layui.formSelects;
var ctx = $("#ctx").val();
var month = $("#month").val();
var l_month = $("#l_month").val();
var ll_month = $("#ll_month").val();
var next_month = $("#next_month").val();
var canEdit = $("#canEdit").val();
var role_name = $("#role_name").val();

var areaSelectedBak = [];
var regionSelectedBak = [];
var businessUserSelectedBak = [];
var dealerSelectedBak = [];

var areaSelectedObj = {};
var regionSelectedObj = {};
var businessUserSelectedObj = {};
var dealerSelectedObj = {};

var tableConfig = {
    elem: '#oneLevel',
    url: ctx + '/dealerOne/list',
    id: 'one_level_ytj_table_id',
    cols: [[
        {field: 'dealer_name', title: '商业公司名称', width: 300, fixed: "left"}
        , {field: 'area_name', title: '大区', width: 80}
        , {field: 'regin_name', title: '省份', width: 80}
        , {field: 'business_manager_name', title: '商务经理', width: 100}
        , {field: 'product_name', title: '商品名称', width: 100}
        , {field: 'ssy_sj_kc', title: ll_month + '月库存', width: 90, align: "right"}
        , {field: 'sy_yg_jhl', title: l_month + '月预估进货', width: 120, align: "right"}
        , {field: 'sy_jhl', title: l_month + '月进货', width: 110, align: "right"}
        , {field: 'sy_yg_xsl', title: l_month + '月销售预估', width: 130, align: "right"}
        , {field: 'sy_sj_xsl', title: l_month + '月实际销售', width: 130, align: "right"}
        , {field: 'sy_sj_xsl_zb', title: l_month + '月实际销售-总部上传', width: 200, align: "right"}
        , {field: 'sy_ll_kc', title: l_month + '月理论库存', width: 120, align: "right"}
        , {field: 'sy_sj_kc', title: l_month + '月实际库存', width: 120, align: "right"}
        , {field: 'sy_kcts', title: l_month + '月库存天数', width: 120, align: "right"}
        , {field: 'cyyy', title: l_month + '月库存差异原因', width: 150}
        , {field: 'by_xtfp_cgl', title: month + '月系统分配采购', width: 150, align: "right"}
        , {field: 'xy_xtfp_cgl', title: next_month + '月系统分配采购', width: 150, align: "right"}
        , {field: 'by_yg_jhl', title: month + '月进货预估', width: 120, align: "right"}
        , {field: 'by_yg_xsl', title: month + '月销售预估', width: 120, align: "right"}
        , {field: 'by_kc', title: month + '月库存', width: 100, align: "right"}
        , {field: 'by_kcts', title: month + '月库存天数', width: 120, align: "right"}
        , {field: 'qly_xs_pjz', title:'近6月月均销售', width: 120, align: "right"}
        , {field: 'order_num', title: '本月已下单数', width: 130, align: "right"}
        , {field: 'diff_order_num', title: '下单数差异', width: 100, align: "right"}
    ]],
    loading: true,
    width: $(window).width() - $(".layui-side").width() - 50,
    height: 'full-350',
    where: {}
}

layui.use(['form', 'layedit', 'table', 'element'], function () {
    var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
    var table = layui.table;
    var element = layui.element;
    var table_ytj;
    var refreshCount = 0;
    element.on('tab(docDemoTabBrief)', function (data) {
        //初始加载常品信息
        if (refreshCount == 1) {
            return;
        }
        searchEvent_ytj();
        refreshCount = 1;
    });

    setProductSelect(function () {
        tableConfig.where.product_id = layui.formSelects.value('product_select', 'valStr');
        table_ytj = renderTable(tableConfig, canEdit, function (res, curr, count) {
            $("#div_table .layui-table-main").css("width", $(window).width() - $(".layui-side").width() - 50);
            var tableData = res;
            $.each($("#div_table .layui-table-main tr[data-index]"), function (i, dom) {
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
                    if ("总计数据" != dealer_name && canEdit != undefined && canEdit != "") {
                        setEditTdErrorStyle(datum, "cyyy");
                    }
                }

                if ("总计数据" == dealer_name) {
                    //统计的上一行清空
                    $($(dom).prev().find("td")[0]).attr("colspan", 20);
                    deleteDom_ytx($($(dom).prev().find("td")[0]), 19);

                    //统计行需要锁定，不允许有点击事件
                    $(dom).find("td").each(function (index, item) {
                        $(item).removeAttr("data-edit");
                    });

                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").next().remove();
                    $(dom).find("td[data-field=area_name]").attr("colspan", 4);
                    $(dom).find("td[data-field=area_name] div").css("margin-left", "40%");
                }
            })
        });
    });

    //监听表格编辑事件
    table.on('edit(oneLevel_filter)', function (obj) { //注：edit是固定事件名，test是table原始容器的属性 lay-filter="对应的值"
        var row_data = obj.data;
        var update_data = obj.value;
        var column = obj.field;
        if (column != 'cyyy') {
            update_data = update_data == "" ? "0" : update_data;
            if (!is_number(update_data)) {
                setEditTdErrorStyle(row_data, column);
                return;
            } else {
                clearEditTdErrorStyle(row_data, column)
            }
        }

        if (column == 'cyyy' && update_data.length > 0) {
            update_data = "'" + update_data + "'";
            clearEditTdErrorStyle(row_data, "cyyy");
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
                //差异原因
                var cyyy_reason = row_data.cyyy == null ? "" : row_data.cyyy;
                //这就说明上月理论库存被计算出来了，这时候要和实际库存做比较，如果差距>1则需要将差异原因变成红色data-field="cyyy"

                if (data.data.flagStatus != undefined && data.data.flagStatus == 3) {
                    var elm = $("#div_table .layui-table-main").find("tr[data-index='" + row_data.LAY_TABLE_INDEX + "']").find("td[data-field='sy_ll_kc'] div");
                    elm.css("color", "#FF5722");
                }

                //如果计算到了上月理论库存，则直接更新掉这个值
                if (data.data.sy_ll_kc.length > 0) {
                    row_data.sy_ll_kc = data.data.sy_ll_kc;
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_ll_kc] div").html(parseInt(data.data.sy_ll_kc) + "");
                    genTotal("sy_ll_kc", new Number(update_data) - new Number(data.data.old_date.theory_stock_quantity));
                    if (new Number(data.data.sy_ll_kc) != row_data.sy_sj_kc && cyyy_reason.length == 0) {
                        setEditTdErrorStyle(row_data, "cyyy");
                    } else {
                        clearEditTdErrorStyle(row_data, "cyyy");
                    }
                }
                //上月库存天数联动
                if (data.data.stock_day.length > 0) {
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_kcts] div").html(parseFloat(data.data.stock_day) + "");
                }
                
                //下单差异数联动
                if (data.data.diff_order_num.length > 0) {
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=diff_order_num] div").html(parseFloat(data.data.diff_order_num) + "");
                    genTotal("diff_order_num", new Number(data.data.diff_order_num) - new Number(data.data.old_date.diff_order_num));
                }               

                //上月实际库存
                if (data.data.sy_sj_kc.length > 0) {
                    row_data.sy_sj_kc = data.data.sy_sj_kc;
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=sy_sj_kc] div").html(parseInt(data.data.sy_sj_kc) + "");
                    genTotal("sy_sj_kc", new Number(update_data) - new Number(data.data.old_date.actual_stock_quantity));
                }

                //本月库存联动
                if (data.data.by_kc.length > 0) {
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=by_kc] div").html(parseInt(data.data.by_kc) + "");
                    genTotal("by_kc", new Number(data.data.by_kc) - new Number(data.data.old_date.actual_stock_quantity));
                }

                //本月库存天数联动
                if (data.data.by_kc_ts.length > 0) {
                    $("#div_table .layui-table-main tr[data-index = " + row_data.LAY_TABLE_INDEX + "] td[data-field=by_kcts] div").html(parseFloat(data.data.by_kc_ts) + "");
                }

                //这里看上月理论库存和上月实际库存是否一样，不一样，则看差异原因是否填写，没填写则飙红
                if (row_data.sy_ll_kc != null && row_data.sy_sj_kc != null && row_data.sy_ll_kc != row_data.sy_sj_kc && cyyy_reason.length == 0) {
                    setEditTdErrorStyle(row_data, "cyyy");
                } else {
                    clearEditTdErrorStyle(row_data, "cyyy");
                }

                //联动汇总
                if ("cyyy" != column) {
                    genTotal(column, new Number(update_data) - new Number(data.data.old_value));
                }
            }
        }, function (err) {
        }, false)
    });
});


//初始化函数
$(function () {
    if ("总部" == role_name) {
        //初始加载大区信息
        setAreaSelect();
        //初始加载经销商信息
        setDealerSelect("","","");
        //初始加载商务经理的下拉列表
        setBusinessManagerSelect_ytj("","","");
    }
    if ("大区经理" == role_name) {
        setRegionSelect("");
        //初始加载商务经理的下拉列表
        setBusinessManagerSelect_ytj("","","");
        //初始加载经销商信息
        setDealerSelect("","","");
    }
    if ("商务经理" == role_name) {
        setRegionSelect("");
        //初始加载经销商信息
        setDealerSelect("","","");
    }

})

//搜索按钮事件
function searchEvent_ytj() {
    var product_id = layui.formSelects.value('product_select', 'valStr');
    var area_id = formSelects.value('area_select', 'valStr');
    var region_id = formSelects.value('region_select', 'valStr');
    var business_id = formSelects.value('business_select', 'valStr');
    var dealer_id = formSelects.value('dealer_select', 'valStr');
    tableConfig.where.product_id = product_id;
    tableConfig.where.area_id = area_id;
    tableConfig.where.region_id = region_id;
    tableConfig.where.business_id = business_id;
    tableConfig.where.dealer_id = dealer_id;
    reloadTable('one_level_ytj_table_id', tableConfig)
}

//加载区域信息
function setAreaSelect() {
    //赋值大区下拉宽
    formSelects.data('area_select', "server", {
        url: ctx + '/dealerOne/getAreaList',
        keyName: 'name',
        keyVal: "id"
    });
    formSelects.btns('area_select', []);
}

//加载经销商下拉宽
function setDealerSelect(area,regionIds,businessIds) {
    // formSelects.btns('dealer_select', []);
    //赋值大区下拉宽
    formSelects.data('dealer_select', "server", {
        url: ctx + '/dealerOne/getDealerList?level=1&area_ids=' + area + '&region_ids=' + regionIds + '&business_ids=' + businessIds,
        keyName: 'name',
        keyVal: "id",
        success: function (id, url, searchVal, result) {
        	if (result)
        	{
                result = result.data;
                dealerSelectedBak = [];
                
                $.each(result, function (index, item) {
                    if (dealerSelectedObj[item.id])
                    {
                    	dealerSelectedBak.push(item.id);
                    }
                });
                
                layui.formSelects.value('dealer_select', dealerSelectedBak);    
                
                dealerSelectedObj = {};
                $.each(dealerSelectedBak, function (i, ele) {
                	dealerSelectedObj[ele] = ele;
                });
        	}
        }
    });
}

//加载省份下拉宽，保持和大区联动
function setRegionSelect(area) {
    formSelects.data('region_select', "server", {
        url: ctx + '/dealerOne/getRegionList?area_ids=' + area,
        keyName: 'name',
        keyVal: "id",
        success: function (id, url, searchVal, result) {
        	if (result)
        	{
                result = result.data;
                regionSelectedBak = [];
                
                $.each(result, function (index, item) {
                    if (regionSelectedObj[item.id])
                    {
                    	regionSelectedBak.push(item.id);
                    }
                });
                
                layui.formSelects.value('region_select', regionSelectedBak);    
                
                regionSelectedObj = {};
                $.each(regionSelectedBak, function (i, ele) {
                	regionSelectedObj[ele] = ele;
                });
        	}
        }
    });
}

//赋值产品下拉宽
function setProductSelect(afterRender) {
    formSelects.data('product_select', "server", {
        url: ctx + '/dealerOne/getProductList?isSelf=true',
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
            layui.formSelects.value('product_select', [result[0].id]);
            $("#product_name_lable").html(result[0].name);
            return result;
        },
        success: function (id, url, val, result) {
            if (typeof afterRender != 'undefined' && typeof afterRender == 'function') {
                afterRender();
            }
        },
    });
}

//赋值商务经理下拉宽
function setBusinessManagerSelect_ytj(area,regionIds,dealerIds) {
    // formSelects.btns('business_select', []);
    formSelects.data('business_select', "server", {
        url: ctx + '/dealerOne/getBusinessManagerList?area_ids=' + area + '&region_ids=' + regionIds + '&dealer_ids=' + dealerIds,
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
        },
        success: function (id, url, searchVal, result) {
            if (result)
            {
                result = result.data;
                businessUserSelectedBak = [];
                
                $.each(result, function (index, item) {
                    if (businessUserSelectedObj[item.id])
                    {
                    	businessUserSelectedBak.push(item.id);
                    }
                });
                
                layui.formSelects.value('business_select', businessUserSelectedBak);    
                
                businessUserSelectedObj = {};
                $.each(businessUserSelectedBak, function (i, ele) {
                	businessUserSelectedObj[ele] = ele;
                });
            }
        }
    });
}

//大区下拉事件监听，去取省份的值
formSelects.on('area_select', function (id, vals, val, isAdd, isDisabled) {
    if (val == undefined) {
        return false;
    }
    var area = "";
    if (isAdd) {
        $.each(vals, function (index, val) {
            area = area + val.val + ",";
            areaSelectedObj[val.val] = val.val;
            areaSelectedBak.push(val.val);
        })
        area = area + val.val;
        areaSelectedObj[val.val] = val.val;
        areaSelectedBak.push(val.val);
    } else {
        $.each(vals, function (index, thisVal) {
            if (thisVal.val != val.val) {
                area = area + thisVal.val + ",";
                areaSelectedObj[thisVal.val] = thisVal.val;
                areaSelectedBak.push(thisVal.val);
            }
            else
            {
                areaSelectedObj[thisVal.val] = null;
                areaSelectedBak.splice($.inArray(thisVal.val,areaSelectedBak),1);
            }
        })
    }
    //  console.log("area:" + area);
    // 省份
    setRegionSelect(area);
    
    // 商务经理
    setBusinessManagerSelect_ytj(area,regionSelectedBak.join(","),dealerSelectedBak.join(","));
    
    // 经销商信息
    setDealerSelect(areaSelectedBak.join(","),regionSelectedBak.join(","),businessUserSelectedBak.join(","));
});

//大区下拉事件监听，去取省份的值
formSelects.on('region_select', function (id, vals, val, isAdd, isDisabled) {
    if (val == undefined) {
        return false;
    }
    var region = "";
    if (isAdd) {
        $.each(vals, function (index, val) {
        	region = region + val.val + ",";
            regionSelectedObj[val.val] = val.val;
            regionSelectedBak.push(val.val);
        })
        region = region + val.val;
        regionSelectedObj[val.val] = val.val;
        regionSelectedBak.push(val.val);
    } else {
        $.each(vals, function (index, thisVal) {
            if (thisVal.val != val.val) {
            	region = region + thisVal.val + ",";
            	regionSelectedObj[thisVal.val] = thisVal.val;
            	regionSelectedBak.push(thisVal.val);
            }
            else
            {
            	regionSelectedObj[thisVal.val] = null;
            	regionSelectedBak.splice($.inArray(thisVal.val,regionSelectedBak),1);
            }
        })
    }
    
    // 商务经理
    setBusinessManagerSelect_ytj(areaSelectedBak.join(","),region,dealerSelectedBak.join(","));
    
    // 经销商信息
    setDealerSelect(areaSelectedBak.join(","),regionSelectedBak.join(","),businessUserSelectedBak.join(","));
});

formSelects.on('dealer_select', function (id, vals, val, isAdd, isDisabled) {
    if (val == undefined) {
        return false;
    }   
    
    if (isAdd) {
        $.each(vals, function (index, val) {
            dealerSelectedObj[val.val] = val.val;
            dealerSelectedBak.push(val.val);
        });
        dealerSelectedObj[val.val] = val.val;
        dealerSelectedBak.push(val.val);
    } else {
        $.each(vals, function (index, thisVal) {
            if (thisVal.val != val.val) {
                dealerSelectedObj[thisVal.val] = thisVal.val;
                dealerSelectedBak.push(thisVal.val);
            }            
            else
            {
            	dealerSelectedObj[thisVal.val] = null;
            	dealerSelectedBak.splice($.inArray(thisVal.val,dealerSelectedBak),1);
            }
        });
    }
    
    setBusinessManagerSelect_ytj(areaSelectedBak.join(","),regionSelectedBak.join(","),dealerSelectedBak.join(","));
});   

formSelects.on('business_select', function (id, vals, val, isAdd, isDisabled) {
    
    if (val == undefined) {
        return false;
    }   
    
    if (isAdd) {
        $.each(vals, function (index, val) {
        	businessUserSelectedObj[val.val] = val.val;
            businessUserSelectedBak.push(val.val);
        });
        businessUserSelectedObj[val.val] = val.val;
        businessUserSelectedBak.push(val.val);
    } else {
        $.each(vals, function (index, thisVal) {
            if (thisVal.val != val.val) {
            	businessUserSelectedObj[thisVal.val] = thisVal.val;
                businessUserSelectedBak.push(thisVal.val);
            }            
            else
            {
            	businessUserSelectedObj[thisVal.val] = null;
            	businessUserSelectedBak.splice($.inArray(thisVal.val,businessUserSelectedBak),1);
            }
        });
    }

    //初始加载经销商信息
    setDealerSelect(areaSelectedBak.join(","),regionSelectedBak.join(","),businessUserSelectedBak.join(","));
}); 

//监听产品下拉事件
layui.formSelects.on('product_select', function (id, vals, val, isAdd, isDisabled) {
    var value = layui.formSelects.value('product_select', 'val');
    $("#product_name_lable").html(val.name);
    if (isDisabled) {
        if (val.val == value[0]) {
            showTips("最少选择一个，不能清空", 800, function () {
                layui.formSelects.value('product_select', value);
                return false;
            })
        }
    }
    $("#product_name_lable").html(val.name);
});

function export_ytx_sj() {
    var product_id = layui.formSelects.value('product_select', 'valStr');
    var area_id = formSelects.value('area_select', 'valStr');
    var region_id = formSelects.value('region_select', 'valStr');
    var business_id = formSelects.value('business_select', 'valStr');
    var dealer_id = formSelects.value('dealer_select', 'valStr');
    var url = ctx + "/exportFile/exportFistLevelData?"
        + "product_id=" + product_id
        + "&area_id=" + area_id
        + "&region_id=" + region_id
        + "&business_id=" + business_id
        + "&dealer_id=" + dealer_id;
    window.location.href = url;
}

/**
 * 需要填写差异数据时设置单元格错误样式
 */
function setEditTdErrorStyle(data, field, addClass) {
    var index = data.LAY_TABLE_INDEX;
    if (index >= 0 && field) {
        var elem = $("#div_table .layui-table-main").find("tr[data-index='" + index + "']").find("td[data-field='" + field + "']");
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

/**
 * 清除差异数据单元格错误样式
 */
function clearEditTdErrorStyle(data, field, removeClass) {
    var index = data.LAY_TABLE_INDEX;
    if (index >= 0 && field) {
        var elem = $("#div_table .layui-table-main").find("tr[data-index='" + index + "']").find("td[data-field='" + field + "']");
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

function deleteDom_ytx(dom, num) {
    for (var i = 0; i < num; i++) {
        dom.next().remove();
    }
}

function showErrorCyyy() {
    var ss = $("#div_table .layui-table-main").find(".data-error");
    if (ss.length > 0) {
        showConfirm("<div style='color:#FF5722'>您有实际库存与理论库存差异解释未填或数据格式不正确，请留意红框标出部分！</div>");
    } else {
        showConfirm("<div style='color:#5FB878'>保存成功</div>");
    }
}

//总计行联动
function genTotal(cloum_name, val) {
    var last_row = $("#div_table .layui-table-main tr").eq(-1);
    var div = last_row.find("td[data-field=" + cloum_name + "] div");
    var old_num = div.html();
    var number = new Number(old_num) + new Number(val);
    div.html(number);

    genStockDayNum();
}

function genStockDayNum() {
    var product_id = layui.formSelects.value('product_select', 'valStr');
    var area_id = formSelects.value('area_select', 'valStr');
    var region_id = formSelects.value('region_select', 'valStr');
    var business_id = formSelects.value('business_select', 'valStr');
    var dealer_id = formSelects.value('dealer_select', 'valStr');

    $.getRemoteDate(ctx + "/dealerOne/getStockDayForTotal", {
        product_id: product_id,
        area_id: area_id,
        region_id: region_id,
        business_id: business_id,
        dealer_id: dealer_id
    }, function (data) {
        if (data.data == null) {
            return;
        }
        var last_row = $("#div_table .layui-table-main tr").eq(-1);
        if (data.data.sy_kcts.length > 0) {
            var div = last_row.find("td[data-field=sy_kcts] div");
            div.html(data.data.sy_kcts);
        }
        if (data.data.by_kcts.length > 0) {
            var div = last_row.find("td[data-field=by_kcts] div");
            div.html(data.data.by_kcts);
        }

    }, function (error) {

    })

}

function is_number(val) {
    var r = /^\+?[0-9][0-9]*$/;　　//判断是否为正整数
    return r.test(val);
}