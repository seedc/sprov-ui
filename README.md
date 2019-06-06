# sprov-ui

一个支持多协议多用户的 v2ray Web 面板

# 详细教程

https://blog.sprov.xyz/2019/02/09/sprov-ui/

# 支持的功能

- https 访问面板
- 系统运行状态监控
- 多协议、多用户管理
- 禁用、启用单个账号
- 支持设置监听的 IP（多 IP 服务器下）
- 流量统计（支持所有协议）

## 支持的 v2ray 协议

- vmess（v2ray 特色）
- shadowsocks（经典 ss）
- mtproto（Telegram 专用）
- dokodemo-door（端口转发）
- socks（socks 4、socks 4a、socks 5）
- http（http 代理）

## 支持的 vmess 传输配置

- tcp
- kcp + 伪装
- ws + 伪装 + tls
- http/2 + 伪装 + tls

# 运行截图

![1.png](1.png)
![2.png](2.png)

# 支持的系统

> 务必使用纯净版的系统，建议在 256MB 内存及以上的 vps 搭建，低内存情况下可能运作不良

- CentOS 7（推荐）
- Ubuntu 16
- Ubuntu 18
- Debian 8
- Debian 9

# 一键安装&升级面板

> 以下两条命令皆可，两者是一样的，只需要运行一个，如果其中一个有错误，可以运行另外一个。

> 请务必使用 root 用户运行！

```
wget -O /usr/bin/sprov-ui -N --no-check-certificate https://download.o2oyc.com/seedc/sprov-ui/blob/sprov-ui.sh && chmod +x /usr/bin/sprov-ui && sprov-ui

```
或
```
wget -O /usr/bin/sprov-ui -N --no-check-certificate https://raw.githubusercontent.com/seedc/sprov-ui/master/install.sh && chmod +x /usr/bin/sprov-ui && sprov-ui

```
# 常见问题

### sprov-ui 启动失败：Address already in use

这个问题是因为面板的监听端口被占用了，换个端口即可。

### sprov-ui 启动失败：port out of range:xxxx

面板监听的端口超出正常范围，正常范围是 1-65535，换个端口即可。

### sprov-ui 启动失败：Invalid keystore format

证书有问题，需要 jks 格式的证书，文章下面有配置教程。

### sprov-ui 启动失败：Keystore was tampered with, or password was incorrect

jks 证书密码错误，如果忘记密码了可以重新生成一个。

### vmess 协议的账号连不上，其它的账号都连得上，端口也是通的

这是因为你的服务器时间和本地时间相差过大，vmess 协议要求服务器的 UTC 时间和本地 UTC 时间相差不超过 90 秒，服务器与本地的时区不一样没关系，但是分钟数要相同，请自行修改服务器时间。

### 所有账号都连不上，或者刚刚添加/修改的账号连不上

添加、删除、修改账号之后都需要重启 v2ray 才会生效新的配置，点击网页上的【重启】按钮即可，不是【重启面板】。还有确保你的端口是通的，防火墙都放行了。

### 开启 v2ray api 失败：xxxx

这个错误的原因一般就是你的 v2ray 配置文件格式过老了，v2ray 的 v4.1 版本开始启用了新的配置文件格式，本面板只支持 v4.1 版本之后的配置文件。

通用解决方法：

- 先备份好你的 v2ray 节点信息
- 删除 /etc/v2ray/config.json 文件
- 重新使用此命令安装 v2ray：bash <(curl -L -s https://install.direct/go.sh) -f
- 重启面板

访问网页出现：Bad response. The server or forwarder response doesn’t look like HTTP.
开启 https 后不能使用 http:// 访问，请使用 https:// 访问，且必须使用域名访问，不能使用 ip。

# 高级操作

### 使用域名

首先你需要一个域名，并将域名解析到你 vps 的 IP，直接使用域名加端口号登录面板即可，无需其它配置。

### sprov-ui 面板配置文件

面板配置文件在 /etc/sprov-ui/ 文件夹下，包含两个文件，一个是 sprov-ui.conf，一个是 v2ray-extra-config.json。

#### sprov-ui.conf

```
port=80                       # 面板监听端口
username=sprov                # 用户名
password=blog.sprov.xyz       # 密码
keystoreFile=                 # jks 证书文件路径，v3.0.0+
keystorePass=                 # jks 证书密码，v3.0.0+
maxWrongPassCount=5           # 密码最大错误次数，达到此次数则封禁 IP，默认为 5，v3.0.0+
loginTitle=xxxx               # 登录页标题，可自定义，留空则没有标题，v3.1.0+
loginFooter=xxxx              # 登录页页脚，可自定义，支持 html 标签，留空则没有页脚，v3.1.0+
```

此文件配置了 sprov-ui 的端口、用户名、密码等等，可以自行修改，修改后需要重启面板生效，留空则使用默认配置。配置错误会导致面板启动失败，例如：port=111111（非法端口号）。

### v2ray-extra-config.json

```
{
    "disabled-inbounds": [],
    "inbounds": []
}
```

此文件为 v2ray 配置文件的扩展，为一个 json 文件，包含两个属性：inbounds、disabled-inbounds。

inbounds 为一个数组，包含若干个 inbound，主要记录流量数据，每个 inbound 的格式如下：

```
{
    "tag": "",      // tag 标识，不能为空
    "downlink": 0,  // 下行流量，单位 Byte
    "uplink: 0      // 上行流量，单位 Byte
}
```

disabled-inbounds 为一个数组，包含若干个 inbound，记录被禁用的 inbound，每个 inbound 都是一个完整的 v2ray inbound，并且还包含流量数据。

### 启用面板 https 访问

sprov-ui v3.0.0 及以上版本支持

开启 https 后不能再使用 http:// 访问面板，请使用 https:// 访问面板，且必须使用域名访问，不能使用 ip

首先我们申请的证书常见的是 .crt / .pem 等格式，密钥常见的格式是 .key，这些证书不能直接配置在面板里，需要将证书和密钥转换成 .jks 格式的证书，以下是转换教程。

将 crt / pem 证书转换为 jks 格式的证书

在转换 jks 证书后，上传 jks 证书至你的服务器，并在面板配置文件里添加如下配置，并重启面板：

```
keystoreFile=/path/to/xxx.com.jks        # jks 证书文件绝对路径
keystorePass=yourpassword                # jks 证书密码，如果没有设置密码则留空
```

不会上传文件到服务器？看这篇文章：MobaXterm – 一个强大的全能终端

### 面板服务器迁移

面板的服务器迁移很简单，首先需要备份面板配置文件和 v2ray 配置文件，分别是：/etc/sprov-ui/ 文件夹下所有文件，/etc/v2ray/config.json，如何备份请自行解决。

然后在新服务器上重新安装面板，之前备份的文件覆盖掉现有的，最后启动或重启面板即可。

# Telegram 群组

https://t.me/sprov_blog

# Telegram 频道

https://t.me/sprov_channel
