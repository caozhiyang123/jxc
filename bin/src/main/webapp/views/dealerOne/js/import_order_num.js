var formSelects = layui.formSelects;
var ctx = $("#ctx").val();
var month = $("#month").val();
var l_month = $("#l_month").val();
var ll_month = $("#ll_month").val();
var canEdit = $("#canEdit").val();


layui.use('upload', function () {
    var upload = layui.upload;
    var loadingIndex;
    var uploadInst = upload.render({
        elem: '#upload_order_num' //绑定元素
        , url: ctx + '/dealerOne/importOrderData' //上传接口
        , accept: 'file'
        , exts: 'xlsx|xls'
        , data: {}
        , before: function (input) {
            $("#err_order").html("无错误信息!");
            loadingIndex = showLoading();
        }
        , done: function (res) {
            hideLoading(loadingIndex);
            var err = res.err;
            if (err.length > 0) {
                var e_m = "";
                $.each(err, function (index, value) {
                    e_m += value + "<br/>"
                });
                $("#err_order").html(e_m);
            } else {
                $("#err_order").html("<span style='color: green'>保存成功</span>");
                showTips("上传成功", 800, function () {
                    location.reload();
                })
            }
        }
        , error: function (data) {
            hideLoading(loadingIndex);
            showTips("出错了：" + data.message);
        }
    });
});
