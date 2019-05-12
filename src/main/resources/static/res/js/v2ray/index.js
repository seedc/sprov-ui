let labels = [
    'v2ray状态', '已运行', 'IP',
    'CPU', '内存', '硬盘', '负载',
    '网络 ↑ | ↓', '流量 ↑ | ↓'
];
let names = [
    'v2rayStatus', 'uptime', 'ip',
    'cpu', 'mem', 'hardDisk', 'loads',
    'netSpeed', 'traffic'
];

let tableData = [];
for (let i in labels) {
    tableData.push({
        label: labels[i],
        name: names[i],
        value: '获取中...'
    });
}

let newTableData = [{"name":"v2rayStatus","value":"运行中","tag":"tag","color":"success"},{"name":"uptime","value":"0 秒","tag":"","color":""},{"name":"ip","value":"localhost","tag":"","color":""},{"name":"cpu","value":"0.00","tag":"progress","color":"rgb(103, 194, 58)"},{"name":"mem","value":"0.00","tag":"progress","color":"rgb(103, 194, 58)"},{"name":"hardDisk","value":"35.98","tag":"progress","color":"rgb(103, 194, 58)"},{"name":"loads","value":"0.0|0.0|0.0","tag":"","color":""},{"name":"netSpeed","value":"0 B/S|0 B/S","tag":"","color":""},{"name":"traffic","value":"0 B|0 B","tag":"","color":""}];

let app = new Vue({
    el: '#app',
    data: {
        tableData: tableData,
        freshInterval: 1,
        lastVersion: '',
        currentVersion: '',
        isLastVersion: true,
        refresh: true
    },
    methods: {
        freshStatus: function () {
            if (!this.refresh) {
                setTimeout('app.freshStatus()', this.freshInterval * 1000);
                return;
            }
            post({
                url: '/server/status',
                success: data => {
                    if (data.success) {
                        this.setTableData(data.obj);
                        setTimeout('app.freshStatus()', this.freshInterval * 1000);
                    } else {
                        this.message({
                            message: data.msg,
                            type: 'warning'
                        });
                        setTimeout('app.freshStatus()', 30 * 1000);
                    }
                },
                error: e => {
                    console.log(e);
                    this.message({
                        message: '状态刷新失败，请检查网络连接，30秒后重试',
                        type: 'error'
                    });
                    setTimeout('app.freshStatus()', 30 * 1000);
                }
            });
        },
        setTableData: function (newData) {
            let oldData = this.tableData;
            for (let i in newData) {
                if (isEmpty(newData[i].name) || newData[i].name === 'null') {
                    continue;
                }
                for (let j in oldData) {
                    if (newData[i].name === oldData[j].name) {
                        oldData[j].value = newData[i].value;
                        oldData[j].tag = newData[i].tag;
                        oldData[j].color = newData[i].color;
                    }
                }
            }
        },
        checkUpdate: function () {
            post({
                url: '/sprov-ui/isLastVersion',
                success: data => {
                    if (data.success === true) {
                        this.isLastVersion = data.msg === data.obj;
                        this.lastVersion = data.msg;
                        this.currentVersion = data.obj;
                    }
                }
            });
        },
        restart: function () {
            this.confirm('确定要重启吗？', '')
                .then(this.restartPanel);
        },
        restartPanel: function() {
            const loading = this.$loading({
                lock: true,
                text: '重启面板中...',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
            this.refresh = false;
            post({
                url: '/sprov-ui/restart',
                success: data => {
                    if (data.success) {
                        this.message({
                            message: '重启成功，将在 5 秒后刷新页面',
                            type: 'success',
                            duration: 5000
                        });
                        setTimeout(() => {
                            location.reload();
                        }, 5000);
                    } else {
                        this.message({
                            message: data.msg,
                            type: 'error'
                        });
                    }
                    loading.close();
                    this.refresh = true;
                },
                error: e => {
                    this.message({
                        message: '网络错误，请检查网络连接',
                        type: 'error'
                    });
                    loading.close();
                    this.refresh = true;
                }
            });
        },
        update: function () {
            this.confirm('确定要升级面板吗？', '')
                .then(this.updatePanel);
        },
        updatePanel: function () {
            this.message({
                message: '请耐心等待，升级可能需要花费几分钟',
                type: 'warning'
            });
            const loading = this.$loading({
                lock: true,
                text: '面板升级中...',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
            this.refresh = false;
            post({
                url: '/sprov-ui/update',
                success: data => {
                    if (data.success) {
                        this.message({
                            message: data.msg,
                            type: 'success'
                        });
                        this.restartPanel();
                    } else {
                        this.message({
                            message: data.msg,
                            type: 'error'
                        });
                    }
                    loading.close();
                    this.refresh = true;
                },
                error: e => {
                    this.message({
                        message: '网络错误，请检查网络连接',
                        type: 'error'
                    });
                    loading.close();
                    this.refresh = true;
                }
            });
        }
    },
    mounted: function () {
        this.freshStatus();
        this.checkUpdate();
        // this.setTableData(newTableData);
    }
});