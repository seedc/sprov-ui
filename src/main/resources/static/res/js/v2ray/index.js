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
        defaultActive: location.pathname + (!location.pathname.endsWith('/') ? '/' : ''),
        tableData: tableData,
        freshInterval: 1.5
    },
    methods: {
        menuSelect: function (index) { console.log(index); location.href = index; },
        freshStatus: function () {
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
                    }
                },
                error: e => {
                    console.log(e);
                    this.error('发生错误，状态刷新失败，30秒后重新尝试');
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
        }
    },
    mounted: function () {
        this.freshStatus();
        // this.setTableData(newTableData);
    }
});