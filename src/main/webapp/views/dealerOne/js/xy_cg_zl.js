var ctx = $("#ctx").val();
var product_purpose_status = $("#product_purpose_status").val();


var product_data = [];
var tableConfig_product_total = {
    elem: '#product_total',
    id: "product_total_id",
    url: ctx + '/purchaseTotal/index',
    cols: [[
        {field: 'id', title: 'ID', width: 50}
        , {field: 'name', title: '名称', width: 100}
        // , {field: 'num', title: '下月采购总量', width: 170, edit: 'text'}
        , {field: 'num', title: '下月采购总量', width: 170}
    ]],
    loading: true,
    width: $(window).width() - $(".layui-side").width(),
    height: 'full-250',
    where: {
        product_id: ""
    }
}

layui.use(['form', 'layedit', 'table'], function () {
    var table = layui.table;
    var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
    if (product_purpose_status != undefined && product_purpose_status == 0) {
        renderTable(tableConfig_product_total, "num", function (res, curr, count) {
            done_render(res, curr, count);
        })
    } else {
        renderTable(tableConfig_product_total, "", function (res, curr, count) {
            done_render(res, curr, count);
        })
    }
    table.on('edit(product_total_filter)', function (obj) { //注：edit是固定事件名，test是table原始容器的属性 lay-filter="对应的值"
        var flag = true;
        $.each(product_data, function (i, v) {
            if (v.id == obj.data.id) {
                v.num = obj.data.num;
                flag = false;
            }
        })
        if (flag) {
            var pro = {}
            pro.id = obj.data.id;
            pro.num = obj.data.num;
            product_data.push(pro);
        }
    });
});

function commitProductTotal() {
    if (product_data.length == 0) {
        showTips("请添加数据后再添加");
        return;
    }
    $.postJson(ctx + "/purchaseTotal/savePurchaseTotal", product_data, function (data) {
        var ss_index = layer.open({
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
            id: 'LAY_layuipro' //设定一个id，防止重复弹出
            ,
            btn: ['确定开始计算', '我还会继续填写']
            ,
            btnAlign: 'c'
            ,
            moveType: 1 //拖拽模式，0或者1
            ,
            content: '<div style="padding: 20px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 500;">' +
            '是否已经填写完下月采购量总量数据?<br/>' +
            '<span style="color: green">是：</span>点击【确定开始计算】，系统将开始计算计算当月库存、库存天数、下月逻辑分配采购量！<span style="color: red">警告：系统计算结束，将会把T1商和T2商的上月数据全部封存，请自行调配好时间</span><br>' +
            '<span style="color: red">否：</span>点击【我还会继续填写】，系统将不会自动计算</div>'
            ,
            success: function (layero) {
            },
            yes: function (index, layero) {
                layer.close(ss_index);
                $.getRemoteDate(ctx + "/purchaseTotal/startCalculate", {}, function () {
                    showTips("系统已经开始进入计算期，请勿再次改动下月采购量总量数据", 800, function () {
                        location.reload();
                    });
                })
            }
        });
    }, function (err) {
        showTips("保存失败")
    })
}

function done_render(res, curr, count) {
    product_data = res.data;
}