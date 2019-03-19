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
        elem: '#test1' //绑定元素
        , url: ctx + '/dealerOne/importData' //上传接口
        , accept: 'file'
        , exts: 'xlsx|xls'
        , data: {store_id: $("#store_id").val()}
        , before: function (input) {
            $("#err").html("无错误信息!");
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
                $("#err").html(e_m);
            } else {
                $("#err").html("<span style='color: green'>保存成功</span>");
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


function hideAllTips() {
    $("#warn-div").hide();
    $("#error-div").hide();
    $("#success-div").hide();
    clearAllTips();
}

function clearAllTips() {
    $("#success-message").html("");
    $("#warn-message").html("");
    $("#error-message").html("");
}

function showSuccess(message, code) {
    var color = code == "2" ? "#CC3333" : "#5FB878";
    $("#success-div").css("color", color);
    $("#success-div").show();
}

function showWarn() {
    $("#warn-div").show();
}

function showError() {
    $("#error-div").show();
}

