let formRules = {
    port: [
        { type: 'integer', min: 1, max: 65535, required: true, message: '范围1-65535且为整数', trigger: 'blur' },
    ]
};

let vmessRules = {
    alterId: [
        { type: 'integer', min: 0, max: 65535, required: true, message: '范围0-65535且为整数', trigger: 'blur' }
    ]
};

let ssRules = {
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
};

let dokoRules = {
    address: [
        { len: 3, required: true, message: '请输入一个IP或域名', trigger: 'blur' }
    ],
    port: [
        { type: 'integer', min: 1, max: 65535, required: true, message: '范围1-65535且为整数', trigger: 'blur' },
    ]
};

let app = new Vue({
    el: '#app',
    data: {
        btnLoad: false,
        defaultActive: location.pathname + (!location.pathname.endsWith('/') ? '/' : ''),
        ip: location.hostname,
        config: {},
        inbounds: [],
        inDL: { visible: false, mode: 'add' },
        formRules: formRules,
        vmessRules: vmessRules,
        ssRules: ssRules,
        dokoRules: dokoRules,
        form: {},
        vmess: {},
        ss: {},
        doko: {},
        stream: {}
    },
    methods: {
        menuSelect: function (index) { location.href = index; },
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
                port: randomIntRange(10000, 60000),
                tag: ''
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
            this.doko = {
                address: '',
                port: '',
                network: 'tcp,udp'
            };
            this.stream = {
                network: 'tcp'
            };
            this.inDL.mode = 'add';
            this.inDL.visible = true;
        },
        openEdit: function(inbound, client) {
            this.form = {
                protocol: inbound.protocol,
                port: inbound.port,
                tag: inbound.tag ? inbound.tag : ''
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
                    network: inbound.settings.network ? inbound.settings.network : 'tcp'
                };
            } else if (inbound.protocol === 'dokodemo-door') {
                this.doko = {
                    address: inbound.settings.address,
                    port: inbound.settings.port,
                    network: inbound.settings.network ? inbound.settings.network : 'tcp'
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
            } else if (form.protocol === 'dokodemo-door') {
                let doko = this.doko;
                settings = {
                    address: doko.address,
                    port: doko.port,
                    network: doko.network
                };
            }
            return {
                port: form.port,
                protocol: form.protocol,
                tag: form.tag,
                settings: JSON.stringify(settings),
                streamSettings: JSON.stringify(streamSettings)
            };
        },
        clearValidates: function() {
            if (this.$refs['inForm']) {
                this.$refs['inForm'].clearValidate();
            }
            if (this.$refs['vmessForm']) {
                this.$refs['vmessForm'].clearValidate();
            }
            if (this.$refs['ssForm']) {
                this.$refs['ssForm'].clearValidate();
            }
            if (this.$refs['dokoForm']) {
                this.$refs['dokoForm'].clearValidate();
            }
        },
        validateForms: function(form, callback) {
            this.$refs['inForm'].validate(valid => {
                if (valid) {
                    if (form.protocol === 'vmess') {
                        this.$refs['vmessForm'].validate(callback);
                    } else if (form.protocol === 'shadowsocks') {
                        this.$refs['ssForm'].validate(callback);
                    } else if (form.protocol === 'dokodemo-door') {
                        this.$refs['dokoForm'].validate(callback);
                    }
                } else {
                    execute(callback, valid);
                }
            });
        },
        addInbound: function (form) {
            this.validateForms(form, valid => {
                if (valid) {
                    this.submit('/v2ray/inbound/add', this.getInbound(form), this.inDL);
                } else {
                    this.$message({
                        message: '配置填写有误，请检查错误信息',
                        type: 'error'
                    });
                }
            });
        },
        editInbound: function (form) {
            this.validateForms(form, valid => {
                if (valid) {
                    this.submit('/v2ray/inbound/edit', this.getInbound(form), this.inDL);
                } else {
                    this.$message({
                        message: '配置填写有误，请检查错误信息',
                        type: 'error'
                    });
                }
            });
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