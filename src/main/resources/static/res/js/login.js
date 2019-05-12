let app = new Vue({
    el: '#app',
    data: {
        buttonLoading: false,
        user: {}
    },
    methods: {
        login: function () {
            this.buttonLoading = true;
            post({
                url: '/login',
                data: this.user,
                success: data => {
                    this.buttonLoading = false;
                    if (data.success) {
                        location.href = basePath + "/v2ray/";
                    } else {
                        this.error(data.msg);
                    }
                },
                error: () => {
                    this.buttonLoading = false;
                    this.error('发生网络错误，请检查网络连接');
                }
            });
        }
    }
});