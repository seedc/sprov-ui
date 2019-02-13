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

echo "$release $os_version"

install_java() {
    if [[ x"${release}" == x"centos" ]]; then
        yum install java-1.8.0-openjdk git curl -y
    elif [[ x"${release}" == x"ubuntu" || x"${release}" == x"debian" ]]; then
        apt-get install java-1.8.0-openjdk git curl -y
    fi
}

install_v2ray() {
    bash <(curl -L -s https://install.direct/go.sh) -f
}

install_sprov-ui() {
    return 1
}

echo "开始安装"
install_java
install_v2ray
install_sprov-ui
echo -e "${green}v2ray面板安装成功${plain}\n"