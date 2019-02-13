Vue.prototype.loading = function (msg) {
    return this.$loading({
        lock: true,
        text: msg ? msg : '操作中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
    });
};

function addProperty(obj) {
    if (obj === undefined) {
        obj = { center: true };
    } else if (typeof obj === 'object') {
        obj.center = true;
    }
    return obj;
}

Vue.prototype.alert = function (message, title, options) {
    title = addProperty(title);
    options = addProperty(options);
    return this.$alert(message, title, options);
};

Vue.prototype.confirm = function (message, title, options) {
    title = addProperty(title);
    options = addProperty(options);
    options.type = isEmpty(options.type) ? 'warning' : options.type;
    return this.$confirm(message, title, options);
};

Vue.prototype.prompt = function (message, title, options) {
    title = addProperty(title);
    options = addProperty(options);
    return this.$prompt(message, title, options);
};

Vue.prototype.validate = function (form, func) {
    this.$refs[form].validate(valid => {
        if (valid) {
            execute(func);
        }
    })
};

Vue.prototype.resetFields = function (form) {
    this.$refs[form] && this.$refs[form].resetFields();
};

Vue.prototype.sizeFormat = window.sizeFormat;

Vue.prototype.loading = function () {
    this.fullscreenLoading = true;
};

Vue.prototype.closeLoading = function () {
    this.fullscreenLoading = false;
};

Vue.prototype.message = Vue.prototype.$message;
Vue.prototype.success = Vue.prototype.$message.success;
Vue.prototype.error = Vue.prototype.$message.error;
Vue.prototype.info = Vue.prototype.$message.info;