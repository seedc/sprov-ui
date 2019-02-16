let app = new Vue({
    el: '#app',
    data: {
        btnLoad: false,
        defaultActive: location.pathname + (!location.pathname.endsWith('/') ? '/' : ''),
        ip: location.hostname,
        config: {},
        inbounds: [],
        inDL: { visible: false, mode: 'add' },
        form: {},
        vmess: {},
        ss: {},
        stream: {}
    },
    methods: {
        menuSelect: function (index) { console.log(index); location.href = index; },
        getConfig: function () {
            post({
                url: '/v2ray/config',
                success: data => {
                    if (data.success) {
                        this.config = JSON.parse(data.msg);
                    } else {
                        this.message({
                            message: data.msg,
                            type: 'error'
                        });
                    }
                },
                error: e => {
                    this.message({
                        message: e,
                        type: 'error'
                    });
                }
            });
        },
        openAdd: function () {
            this.form = {
                protocol: 'vmess',
                port: randomIntRange(10000, 60000)
            };
            this.vmess = {
                id: randomUUID(),
                alterId: 64,
                security: 'auto'
            };
            this.ss = {
                method: 'aes-256-gcm',
                password: randomSeq(10),
                network: 'tcp,udp'
            };
            this.stream = {
                network: 'tcp'
            };
            this.inDL.mode = 'add';
            this.inDL.visible = true;
        },
        // TODO
        openEdit: function(inbound, client) {
            this.form = {
                protocol: inbound.protocol,
                port: inbound.port
            };
            if (inbound.protocol === 'vmess') {
                this.vmess = {
                    id: client.id,
                    alterId: client.alterId,
                    security: client.security ? client.security : 'auto'
                };
                this.stream = {
                    network: inbound.streamSettings && inbound.streamSettings.network ? inbound.streamSettings.network : 'tcp'
                };
            } else if (inbound.protocol === 'shadowsocks') {
                this.ss = {
                    method: inbound.settings.method,
                    password: inbound.settings.password,
                    network: inbound.settings.network ? inbound.settings.network : 'tcp',
                };
            }
            this.inDL.mode = 'edit';
            this.inDL.visible = true;
        },
        del: function(inbound) {
            this.confirm('确定要删除吗？', '删除账号')
                .then(() => {
                    this.submit('/v2ray/inbound/del', {
                        port: inbound.port
                    });
                });
        },
        getInbound: function(form) {
            let settings;
            let streamSettings = {};
            if (form.protocol === 'vmess') {
                let vmess = this.vmess;
                let stream = this.stream;
                settings = {
                    clients: [{
                        id: vmess.id,
                        alterId: vmess.alterId,
                        security: vmess.security
                    }]
                };
                streamSettings = {
                    network: stream.network
                };
            } else if (form.protocol === 'shadowsocks') {
                let ss = this.ss;
                settings = {
                    method: ss.method,
                    password: ss.password,
                    network: ss.network
                };
            }
            return {
                port: form.port,
                protocol: form.protocol,
                settings: JSON.stringify(settings),
                streamSettings: JSON.stringify(streamSettings)
            };
        },
        addInbound: function (form) {
            this.submit('/v2ray/inbound/add', this.getInbound(form), this.inDL);
        },
        editInbound: function (form) {
            this.submit('/v2ray/inbound/edit', this.getInbound(form), this.inDL);
        },
        restart: function() {
            this.confirm('确定要重启吗？', '')
                .then(() => {
                    this.submit('/v2ray/restart');
                });
        },
        stop: function() {
            this.confirm('确定要关闭吗？', '')
                .then(() => {
                    this.submit('/v2ray/stop');
                });
        },
        submit: function (url, form, dl) {
            this.btnLoad = true;
            post({
                url: url,
                data: form,
                success: data => {
                    this.btnLoad = false;
                    if (data.success && dl) {
                        dl.visible = false;
                    }
                    let type = 'success';
                    this.getConfig();
                    if (!data.success) {
                        type = 'error';
                    }
                    this.$message({
                        message: data.msg,
                        type: type
                    });
                },
                error: e => {
                    this.btnLoad = false;
                    this.$message({
                        message: e,
                        type: 'error'
                    });
                }
            });
        }
    },
    watch: {
        'config': function (config) {
            this.inbounds = config.inbounds;
        }
    },
    mounted: function () {
        this.getConfig();
    }
});