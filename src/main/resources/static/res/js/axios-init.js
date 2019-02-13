axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

axios.interceptors.request.use(
    config => {
        config.data = Qs.stringify(config.data, {
            arrayFormat: 'repeat'
        });
        return config;
    },
    error => Promise.reject(error)
);

function commonSuccess(response, callback) {
    let data = response.data;
    // if (data && typeof data === 'object') {
    //     if (data.success === true) {
    //         Vue.prototype.$message({
    //             message: data.msg,
    //             type: 'success'
    //         });
    //     } else if (data.success === false) {
    //         Vue.prototype.$message({
    //             message: data.msg,
    //             type: 'error'
    //         });
    //     }
    // }
    execute(callback, data, response);
}

function commonError(e, callback) {
    console.log(e);
    execute(callback, e);
}

/**
 * POST 请求
 */
window.post = options => {
    axios.post(options.url, options.data)
        .then(response => commonSuccess(response, options.success))
        .catch(e => commonError(e, options.error));
};

/**
 * GET 请求
 */
window.get = options => {
    axios.get(options.url, options.data)
        .then(response => commonSuccess(response, options.success))
        .catch(e => commonError(e, options.error));
};