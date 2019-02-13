#!/bin/bash

red='\033[0;31m'
green='\033[0;32m'
yellow='\033[0;33m'
plain='\033[0m'

cur_dir=$(pwd)

# check root
[[ $EUID -ne 0 ]] && echo -e "${red}error:${plain} This script must be run as root!!！\n" && exit 1

[[ -d "/proc/vz" ]] && echo -e "${yellow}warning:${plain} Your VPS is based on OpenVZ, which is not support bbr.\n"

# check os
if [[ -f /etc/redhat-release ]]; then
    release="centos"
elif cat /etc/issue | grep -Eqi "debian"; then
    release="debian"
elif cat /etc/issue | grep -Eqi "ubuntu"; then
    release="ubuntu"
elif cat /etc/issue | grep -Eqi "centos|red hat|redhat"; then
    release="centos"
elif cat /proc/version | grep -Eqi "debian"; then
    release="debian"
elif cat /proc/version | grep -Eqi "ubuntu"; then
    release="ubuntu"
elif cat /proc/version | grep -Eqi "centos|red hat|redhat"; then
    release="centos"
else
    echo -e "${red}OS is not supported, please contact the author!!!${plain}\n" && exit 1
fi

os_version=""

# os version
if [[ -f /etc/os-release ]]; then
    os_version=$(awk -F'[= ."]' '/VERSION_ID/{print $3}' /etc/os-release)
fi
if [[ -z "$os_version" && -f /etc/lsb-release ]]; then
    os_version=$(awk -F'[= ."]+' '/DISTRIB_RELEASE/{print $2}' /etc/lsb-release)
fi

if [[ x"${release}" == x"centos" ]]; then
    if [[ ${os_version} -le 6 ]]; then
        echo -e "${red}OS is not supported, please use CentOS 7 or higher versions!!!${plain}" && exit 1
    fi
elif [[ x"${release}" == x"ubuntu" ]]; then
    if [[ ${os_version} -lt 16 ]]; then
        echo -e "${red}OS is not supported, please use Ubuntu 16 or higher versions!!!${plain}\n" && exit 1
    fi
elif [[ x"${release}" == x"debian" ]]; then
    if [[ ${os_version} -lt 8 ]]; then
        echo -e "${red}OS is not supported, please use Debian 8 or higher versions!!!${plain}\n" && exit 1
    fi
fi

install_java() {
    if [[ x"${release}" == x"centos" ]]; then
        yum install java-1.8.0-openjdk curl -y
    elif [[ x"${release}" == x"debian" ]]; then
        apt-get install java-1.8.0-openjdk curl -y
    elif [[ x"${release}" == x"ubuntu" ]]; then
        apt-get install openjdk-8-jre-headless curl -y
    fi
}

install_v2ray() {
    bash <(curl -L -s https://install.direct/go.sh) -f
}

close_firewall() {
    if [[ x"${release}" == x"centos" || x"${release}" == x"debian" ]]; then
        systemctl stop firewalld
        systemctl disable firewalld
    elif [[ x"${release}" == x"ubuntu" ]]; then
        ufw disable
    fi
}

install_sprov-ui() {
    if [[ ! -f /usr/local/sprov-ui ]]; then
        mkdir /usr/local/sprov-ui
    fi
    wget -O /usr/local/sprov-ui/sprov-ui.war https://github.com/sprov065/sprov-ui/releases/download/v1.0.0-beta/sprov-ui-1.0.0.war
    read -p "请输入面板监听端口[默认80]：" port
    read -p "请输入面板登录用户名[默认sprov]：" user
    read -p "请输入面板登录密码[默认blog.sprov.xyz]：" pwd
    if [[ -z "${port}" ]]; then
        port=80
    fi
    if [[ -z "${user}" ]]; then
        user="sprov"
    fi
    if [[ -z "${pwd}" ]]; then
        pwd="blog.sprov.xyz"
    fi
    echo "[Unit]" > /etc/systemd/system/sprov-ui.service
    echo "Description=sprov-ui Service" >> /etc/systemd/system/sprov-ui.service
    echo "After=network.target" >> /etc/systemd/system/sprov-ui.service
    echo "Wants=network.target" >> /etc/systemd/system/sprov-ui.service
    echo "" >> /etc/systemd/system/sprov-ui.service
    echo "[Service]" >> /etc/systemd/system/sprov-ui.service
    echo "Type=simple" >> /etc/systemd/system/sprov-ui.service
    if [[ x"${release}" == x"debian" ]]; then
        java_cmd="/usr/share/java"
    else
        java_cmd="/usr/bin/java"
    fi
    echo "ExecStart=${java_cmd} -jar /usr/local/sprov-ui/sprov-ui.war --server.port=${port} --user.username=${user} --user.password=${pwd}" >> /etc/systemd/system/sprov-ui.service
    echo "" >> /etc/systemd/system/sprov-ui.service
    echo "[Install]" >> /etc/systemd/system/sprov-ui.service
    echo "WantedBy=multi-user.target" >> /etc/systemd/system/sprov-ui.service
    systemctl daemon-reload
    echo -e "${green}v2ray面板安装成功${plain}\n"
    echo -e "面板监听端口（不是v2ray端口）：${green}${port}${plain}"
    echo -e "面板登录用户名：${green}${user}${plain}"
    echo -e "面板登录密码：${green}${pwd}${plain}"
    echo -e "开启面板：systemctl start sprov-ui"
    echo -e "关闭面板：systemctl stop sprov-ui"
    echo -e "重启面板：systemctl restart sprov-ui"
}

echo "开始安装"
install_java
install_v2ray
close_firewall
install_sprov-ui