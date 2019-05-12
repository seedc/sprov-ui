const defaultInbound = {
    listen: '0.0.0.0',
    settings: {},
    streamSettings: {
        network: 'tcp',
        security: 'none',
        tcpSettings: {
            header: {
                type: 'none'
            }
        },
        kcpSettings: {
            uplinkCapacity: 5,
            downlinkCapacity: 20,
            header: {
                type: 'none'
            }
        },
        wsSettings: {
            path: '/',
            headers: {}
        },
        httpSettings: {
            host: [],
            path: '/'
        },
        tlsSettings: {
            serverName: '',
            certificates: [{certificateFile: '', keyFile: ''}],
        },
    },
    tag: '',
    remark: '',
    downlink: 0,
    uplink: 0,
    searched: true,
    enable: true
};

const defaultVmessSettings = {
    clients: []
};

let formRules = {
    port: [
        { type: 'integer', min: 1, max: 65535, required: true, message: '范围1-65535且为整数', trigger: 'blur' },
    ]
};

let vmessRules = {
    id: [
        { required: true, message: '必须填写一个 UUID', trigger: 'blur' }
    ],
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
        { required: true, message: '请输入一个IP或域名', trigger: 'blur' }
    ],
    port: [
        { type: 'integer', min: 1, max: 65535, required: true, message: '范围1-65535且为整数', trigger: 'blur' },
    ]
};

let kcpRules = {
    uplinkCapacity: [
        { type: 'integer', min: 0, required: true, message: '请输入一个非负整数', trigger: 'blur' }
    ],
    downlinkCapacity: [
        { type: 'integer', min: 0, required: true, message: '请输入一个非负整数', trigger: 'blur' }
    ]
};

let tlsRules = {
    certFile: [
        { required: true, message: '必须输入证书文件路径', trigger: 'blur' }
    ],
    keyFile: [
        { required: true, message: '必须输入密钥文件路径', trigger: 'blur' }
    ]
};

let wsRules = {
    path: [
        { required: true, message: '必须填入一个路径', trigger: 'blur' }
    ]
};

const http2Rules = {
    path: [
        { required: true, message: '必须填入一个路径', trigger: 'blur' }
    ]
};

const forms = ['inForm', 'vmessForm', 'ssForm', 'dokoForm', 'kcpForm', 'wsForm', 'http2Form', 'tlsForm'];

let app = new Vue({
    el: '#app',
    data: {
        loading: false,
        btnLoad: false,
        ip: location.hostname,
        protocols: ['shadowsocks', 'dokodemo-door', 'mtproto', 'socks', 'http'],
        config: {},
        search: '',
        inbounds: [],
        inDL: { visible: false, mode: 'add' },
        qrCodeDL: { visible: false, qrcode: null },
        formRules: formRules,
        vmessRules: vmessRules,
        ssRules: ssRules,
        dokoRules: dokoRules,
        kcpRules: kcpRules,
        wsRules: wsRules,
        http2Rules: http2Rules,
        tlsRules: tlsRules,
        form: {},
        vmess: {},
        ss: {},
        doko: {},
        mt: {},
        socks: {},
        http: {},
        stream: {},
        kcp: {},
        ws: {},
        http2: {},
        tls: {}
    },
    methods: {
        getConfig: function () {
            this.loading = true;
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
                    this.loading = false;
                },
                error: e => {
                    this.message({
                        message: e,
                        type: 'error'
                    });
                    this.loading = false;
                }
            });
        },
        openAdd: function () {
            this.form = {
                protocol: 'vmess',
                listen: '0.0.0.0',
                port: randomIntRange(10000, 60000),
                remark: ''
            };
            this.vmess = {
                id: randomUUID(),
                alterId: 64,
                // security: 'auto'
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
            this.mt = {
                secret: randomMTSecret()
            };
            this.socks = {
                auth: 'password',
                accounts: [{ user: '', pass: '' }],
                udp: false,
                ip: '127.0.0.1'
            };
            this.http = {
                accounts: [{ user: '', pass: '' }]
            };
            this.stream = {
                tls: false,
                security: 'none',
                network: 'tcp'
            };
            this.kcp = {
                type: 'none',
                uplinkCapacity: 5,
                downlinkCapacity: 20
            };
            this.ws = {
                path: '/',
                headers: [
                    { name: '', value: '' }
                ]
            };
            this.http2 = {
                path: '/',
                hosts: []
            };
            this.tls = {
                serverName: '',
                certFile: '',
                keyFile: ''
            };
            this.inDL.mode = 'add';
            this.inDL.visible = true;
        },
        openEdit: function(inbound, client) {
            this.form = {
                protocol: inbound.protocol,
                listen: inbound.listen,
                port: inbound.port,
                oldPort: inbound.port,
                remark: inbound.remark
            };
            if (inbound.protocol === 'vmess') {
                this.vmess = {
                    id: client.id,
                    alterId: client.alterId,
                };
                let streamSettings = inbound.streamSettings;
                this.stream = {
                    tls: streamSettings.security === 'tls',
                    security: streamSettings.security,
                    network: streamSettings.network
                };
                let kcpSettings = streamSettings.kcpSettings;
                this.kcp = {
                    type: kcpSettings.header.type,
                    uplinkCapacity: kcpSettings.uplinkCapacity,
                    downlinkCapacity: kcpSettings.downlinkCapacity
                };
                let wsSettings = streamSettings.wsSettings;
                let headers = [];
                for (let name in wsSettings.headers) {
                    headers.push({ name: name, value: wsSettings.headers[name] });
                }
                if (headers.length === 0) {
                    headers.push({ name: '', value: '' });
                }
                this.ws = {
                    path: wsSettings.path,
                    headers: headers
                };
                let httpSettings = streamSettings.httpSettings;
                this.http2 = {
                    path: httpSettings.path,
                    hosts: clone(httpSettings.host)
                };
                let tlsSettings = streamSettings.tlsSettings;
                this.tls = {
                    serverName: tlsSettings.serverName,
                    certFile: tlsSettings.certificates[0].certificateFile,
                    keyFile: tlsSettings.certificates[0].keyFile
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
            } else if (inbound.protocol === 'mtproto') {
                this.mt = {
                    secret: inbound.settings.users[0].secret
                };
            } else if (inbound.protocol === 'socks') {
                const settings = inbound.settings;
                this.socks = {
                    auth: settings.auth,
                    accounts: isEmpty(settings.accounts) ? [] : deepClone(settings.accounts),
                    udp: isEmpty(settings.udp) ? false : settings.udp,
                    ip: isEmpty(settings.ip) ? '127.0.0.1' : settings.ip
                };
            } else if (inbound.protocol === 'http') {
                const settings = inbound.settings;
                this.http = {
                    accounts: isEmpty(settings.accounts) ? [] : deepClone(settings.accounts)
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
                let kcp = this.kcp;
                settings = {
                    clients: [{
                        id: vmess.id,
                        alterId: vmess.alterId
                    }]
                };
                streamSettings = {
                    security: stream.security,
                    network: stream.network
                };
                if (stream.network === 'kcp') {
                    streamSettings.kcpSettings = {
                        uplinkCapacity: kcp.uplinkCapacity,
                        downlinkCapacity: kcp.downlinkCapacity,
                        header: {
                            type: kcp.type
                        }
                    }
                } else if (stream.network === 'ws') {
                    let ws = this.ws;
                    let headers = {};
                    for (let i in ws.headers) {
                        let header = ws.headers[i];
                        if (!isEmpty(header.name)) {
                            headers[header.name] = header.value;
                        }
                    }
                    streamSettings.wsSettings = {
                        path: ws.path,
                        headers: headers
                    };
                } else if (stream.network === 'http') {
                    let http2 = this.http2;
                    let host = [];
                    for (let i = 0; i < http2.hosts.length; ++i) {
                        if (!isEmpty(http2.hosts[i])) {
                            host.push(http2.hosts[i]);
                        }
                    }
                    streamSettings.httpSettings = {
                        host: host,
                        path: http2.path
                    };
                    stream.security = 'tls';
                }
                if (stream.security === 'tls') {
                    const tls = this.tls;
                    streamSettings.tlsSettings = {
                        serverName: tls.serverName,
                        certificates: [{
                            certificateFile: tls.certFile,
                            keyFile: tls.keyFile
                        }]
                    };
                }
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
            } else if (form.protocol === 'mtproto') {
                let mt = this.mt;
                settings = {
                    users: [{
                        secret: mt.secret
                    }]
                };
            } else if (form.protocol === 'socks') {
                const socks = deepClone(this.socks);
                const accounts = [];
                for (let i = 0; i < socks.accounts.length; ++i) {
                    if (!isEmpty(socks.accounts[i].user)) {
                        accounts.push(socks.accounts[i]);
                    }
                }
                socks.accounts = accounts;
                settings = socks;
            } else if (form.protocol === 'http') {
                const http = deepClone(this.http);
                const accounts = [];
                for (let i = 0; i < http.accounts.length; ++i) {
                    if (!isEmpty(http.accounts[i].user)) {
                        accounts.push(http.accounts[i]);
                    }
                }
                http.accounts = accounts;
                settings = http;
            }
            return {
                listen: isEmpty(form.listen) ? '0.0.0.0' : form.listen,
                port: form.port,
                protocol: form.protocol,
                remark: form.remark,
                settings: JSON.stringify(settings),
                streamSettings: JSON.stringify(streamSettings)
            };
        },
        clearValidates: function() {
            for (let i in forms) {
                if (this.$refs[forms[i]]) {
                    this.$refs[forms[i]].clearValidate();
                }
            }
        },
        validateForms: function(callback) {
            const promises = [];
            for (let i = 0; i < forms.length; ++i) {
                let form = this.$refs[forms[i]];
                if (form) {
                    let promise = form.validate();
                    promises.push(promise);
                }
            }
            Promise.all(promises)
                .then(_ => {
                    execute(callback, true);
                })
                .catch(_ => {
                    execute(callback, false);
                });
        },
        addOrEdit: function(form) {
            if (this.inDL.mode === 'add') {
                this.addInbound(form);
            } else {
                this.editInbound(form);
            }
        },
        addInbound: function (form) {
            this.validateForms(valid => {
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
            this.validateForms(valid => {
                if (valid) {
                    let oldPort = form.oldPort;
                    form = this.getInbound(form);
                    form.oldPort = oldPort;
                    this.submit('/v2ray/inbound/edit', form, this.inDL);
                } else {
                    this.$message({
                        message: '配置填写有误，请检查错误信息',
                        type: 'error'
                    });
                }
            });
        },
        openTraffic: function(inbound) {
            this.submit('/v2ray/inbound/openTraffic', { port: inbound.port });
        },
        resetTraffic: function(inbound) {
            this.confirm('确定要重置该账号的流量吗？（不可恢复）', '')
                .then(() => this.submit('/v2ray/inbound/resetTraffic', { port: inbound.port }));
        },
        resetAllTraffic: function() {
            this.confirm('确定要重置所有账号的流量吗？（不可恢复）', '')
                .then(() => this.submit('/v2ray/inbound/resetAllTraffic'));
        },
        restart: function() {
            this.confirm('确定要重启 v2ray 吗？', '')
                .then(() => this.submit('/v2ray/restart'));
        },
        stop: function() {
            this.confirm('确定要关闭 v2ray 吗？', '')
                .then(() => this.submit('/v2ray/stop'));
        },
        enable: function(inbound) {
            this.confirm('确定要启用账号吗？', '')
                .then(() => this.submit('/v2ray/inbound/enable', { port: inbound.port }));
        },
        disable: function(inbound) {
            this.confirm('确定要禁用账号吗？', '')
                .then(() => this.submit('/v2ray/inbound/disable', { port: inbound.port }));
        },
        delDisabled: function(inbound) {
            this.confirm('确定要删除账号吗？', '')
                .then(() => this.submit('/v2ray/inbound/delDisabled', { port: inbound.port }));
        },
        submit: function (url, form, dl) {
            this.btnLoad = true;
            if (form === undefined || form === null) {
                form = {};
            }
            const loading = this.$loading({
                lock: true,
                text: '操作中...',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
            post({
                url: url,
                data: form,
                success: data => {
                    loading.close();
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
                    loading.close();
                    this.btnLoad = false;
                    this.$message({
                        message: '发生网络错误，请检查网络连接，或请关闭代理软件再尝试',
                        type: 'error'
                    });
                }
            });
        },
        setInbounds: function (inbounds) {
            for (let i in inbounds) {
                this.setDefaultInbound(inbounds[i]);
            }
            this.inbounds = inbounds;
        },
        setDefaultInbound: function (inbound) {
            this.setDefaultIfNone(inbound, defaultInbound);
            switch (inbound.protocol) {
                case 'vmess': this.setDefaultIfNone(inbound.settings, defaultVmessSettings); break;
            }
        },
        setDefaultIfNone: function (obj, defaultObj) {
            for (let i in defaultObj) {
                if (obj[i] === undefined) {
                    obj[i] = defaultObj[i];
                } else if (typeof obj[i] === 'object') {
                    this.setDefaultIfNone(obj[i], defaultObj[i]);
                }
            }
        },
        showHeaders: function (obj) {
            let html = '<div>';
            for (let i in obj) {
                html += '<p>' + i + " : " + obj[i] + '</p>';
            }
            html += '</div>';
            this.$alert(html, '', {
                center: true,
                dangerouslyUseHTMLString: true
            });
        },
        showHosts: function(obj) {
            let html = '<div>';
            for (let i in obj) {
                html += '<p>' + obj[i] + '</p>';
            }
            html += '</div>';
            this.$alert(html, '', {
                center: true,
                dangerouslyUseHTMLString: true
            });
        },
        showQrCode: function(str) {
            this.qrCodeDL.visible = true;
            this.$nextTick(() => {
                if (this.qrCodeDL.qrcode === null) {
                    this.qrCodeDL.qrcode = new QRious({
                        element: document.querySelector('#qrCode'),
                        size: 260,
                        value: str
                    });
                } else {
                    this.qrCodeDL.qrcode.value = str;
                }
            });
        },
        vmessLink: function (inbound, client) {
            let network = inbound.streamSettings.network;
            let type = 'none';
            let host = '';
            let path = '';
            if (network === 'tcp') {
                let tcpSettings = inbound.streamSettings.tcpSettings;
                let header = tcpSettings.header;
                type = header.type;
                if (header.request) {
                    let request = header.request;
                    path = this.arrToString(request.path);
                    if (header.headers) {
                        host = this.arrToString(propIgnoreCase(header.headers, 'host'));
                    }
                }
            } else if (network === 'kcp') {
                let kcpSettings = inbound.streamSettings.kcpSettings;
                let header = kcpSettings.header;
                type = header.type;
            } else if (network === 'ws') {
                let wsSettings = inbound.streamSettings.wsSettings;
                path = wsSettings.path;
                host = propIgnoreCase(wsSettings.headers, 'host') ? propIgnoreCase(wsSettings.headers, 'host') : '';
            } else if (network === 'http') {
                network = 'h2';
                path = inbound.streamSettings.httpSettings.path;
                host = this.arrToString(inbound.streamSettings.httpSettings.host);
            }
            let obj = {
                v: '2',
                ps: inbound.remark,
                add: inbound.listen === '0.0.0.0' ? this.ip : inbound.listen,
                port: inbound.port,
                id: client.id,
                aid: client.alterId,
                net: network,
                type: type,
                host: host,
                path: path,
                tls: inbound.streamSettings.security === 'tls' ? 'tls' : ''
            };
            return 'vmess://' + Base64.encode(JSON.stringify(obj));
        },
        ssLink: function(inbound) {
            let settings = inbound.settings;
            return 'ss://' + safeBase64(settings.method + ':' + settings.password + '@' + (inbound.listen === '0.0.0.0' ? this.ip : inbound.listen) + ':' + inbound.port);
        },
        mtLink: function(inbound) {
            let settings = inbound.settings;
            let user = settings.users[0];
            return 'https://t.me/proxy?server=' + (inbound.listen === '0.0.0.0' ? this.ip : inbound.listen) + '&port=' + inbound.port + '&secret=' + user.secret;
        },
        arrToString: function (arr, split) {
            if (isEmpty(split)) {
                split = ',';
            }
            let str = '';
            if (arr) {
                for (let i = 0; i < arr.length; ++i) {
                    str += arr[i] + split;
                }
                if (str.length > 0) {
                    str = str.substring(0, str.length - split.length);
                }
            }
            return str;
        },
        deepSearch: function (obj, value) {
            if (typeof obj === 'string') {
                return obj.indexOf(value) >= 0;
            } else if (typeof obj === 'number') {
                return obj.toString().indexOf(value) >= 0;
            } else {
                for (let k in obj) {
                    if (this.deepSearch(obj[k], value)) {
                        return true;
                    }
                }
                return false;
            }
        }
    },
    watch: {
        'stream.network': function (network) {
            if (network === 'http') {
                this.stream.tls = true;
            }
        },
        'config': function (config) {
            this.setInbounds(config.inbounds);
        },
        'stream.tls': function (tls) {
            this.stream.security = tls ? 'tls' : 'none';
        },
        'search': function (search) {
            let inbounds = this.inbounds;
            if (search !== '') {
                for (let i = 0; i < inbounds.length; ++i) {
                    inbounds[i].searched = this.deepSearch(inbounds[i], search);
                }
            } else {
                for (let i = 0; i < inbounds.length; ++i) {
                    inbounds[i].searched = true;``
                }
            }
        }
    },
    computed: {
        'total': function () {
            let up = 0, down = 0;
            let inbounds = this.inbounds;
            for (let i = 0; i < inbounds.length; ++i) {
                up += inbounds[i].uplink;
                down += inbounds[i].downlink;
            }
            return {
                uplink: up,
                downlink: down
            }
        }
    },
    mounted: function () {
        this.getConfig();
    }
});