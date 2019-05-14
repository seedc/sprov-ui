#!/bin/bash

#=================================================
#   System Required: CentOS 7+ / Debian 8+ / Ubuntu 16+
#   Description: install or update sprov-ui
#   Author: sprov
#   Blog: https://blog.sprov.xyz
#   Github - sprov-ui: https://github.com/sprov065/sprov-ui
#=================================================

#       ┏┓    ┏┓  + +
#      ┏┛┻━━━━┛┻┓ + +
#      ┃        ┃ + +
#      ┃   ━    ┃ + + + + +
#     ████━████ ┃ + + + + +
#      ┃        ┃ +
#      ┃   ┻    ┃
#      ┃        ┃ + +
#      ┗━┓    ┏━┛
#        ┃    ┃
#        ┃    ┃ + + + +
#        ┃    ┃ Codes are far away from bugs with the animal protecting
#        ┃    ┃ + 神兽保佑,代码无bug　　
#        ┃    ┃
#        ┃    ┃ +
#        ┃    ┗━━━┓ + +
#        ┃        ┣┓
#        ┃       ┏┛
#        ┗┓┓┏━┳┓┏┛+ + + +
#         ┃┫┫ ┃┫┫
#         ┗┻┛ ┗┻┛+ + + +

red='\033[0;31m'
green='\033[0;32m'
yellow='\033[0;33m'
plain='\033[0m'

cur_dir=$(pwd)

conf_dir="/etc/sprov-ui/"
conf_path="${conf_dir}sprov-ui.conf"

# check root
[[ $EUID -ne 0 ]] && echo -e "${red}错误：${plain} 必须使用root用户运行此脚本！\n" && exit 1

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
    echo -e "${red}未检测到系统版本，请联系脚本作者！${plain}\n" && exit 1
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
        echo -e "${red}请使用 CentOS 7 或更高版本的系统！${plain}\n" && exit 1
    fi
elif [[ x"${release}" == x"ubuntu" ]]; then
    if [[ ${os_version} -lt 16 ]]; then
        echo -e "${red}请使用 Ubuntu 16 或更高版本的系统！${plain}\n" && exit 1
    fi
elif [[ x"${release}" == x"debian" ]]; then
    if [[ ${os_version} -lt 8 ]]; then
        echo -e "${red}请使用 Debian 8 或更高版本的系统！${plain}\n" && exit 1
    fi
fi

install_base() {
    command -v bc >/dev/null 2>&1 || yum install bc -y || apt install bc -y
    command -v curl >/dev/null 2>&1 || yum install curl -y || apt install curl -y
}

install_java() {
    if [[ -f /usr/bin/java ]]; then
        install_base
        java_version=`/usr/bin/java -version 2>&1 | awk -F '\"' 'NR==1{print $2}' | awk -F '.' '{OFS="."; print $1,$2;}'`
        require_version=1.8
        is_ok=`echo "$java_version>=$require_version" | bc`
        if [[ is_ok -eq 1 ]]; then
	    echo -e "${green}已检测到1.8及以上版本的java，无需重复安装${plain}"
	else
	    echo -e "错误：${red}/usr/bin/java${red}的版本低于1.8，请安装大于等于1.8版本的java${plain}"
        echo -e "尝试更新系统可能可以解决该问题："
	    echo -e "CentOS: ${green}yum update${plain}"
        echo -e "Debian / Ubuntu: ${green}apt-get update && apt-get upgrade${plain}"
        exit -1
	fi
    elif [[ x"${release}" == x"centos" ]]; then
        yum install java-1.8.0-openjdk -y
    elif [[ x"${release}" == x"debian" || x"${release}" == x"ubuntu" ]]; then
        apt install default-jre -y
    fi
    if [[ $? -ne 0 ]]; then
        echo -e "${red}Java环境安装失败，请检查错误信息${plain}"
        echo -e "尝试更新系统可能可以解决该问题"
        echo -e "CentOS: ${green}yum update${plain}"
        echo -e "Debian / Ubuntu: ${green}apt-get update && apt-get upgrade${plain}"
        echo -e ""
        echo -e "Debian / Ubuntu 也可以尝试用以下命令安装 java 环境，若安装 java 成功，那么重新运行安装面板即可:"
        echo -e "1. ${green}apt-get install openjdk-11-jre-headless -y${plain}"
        echo -e "2. ${green}apt-get install openjdk-8-jre-headless -y${plain}"
        exit 1
    fi
}

install_v2ray() {
    echo -e "${green}开始安装or升级v2ray${plain}"
    bash <(curl -L -s https://install.direct/go.sh) -f
    if [[ $? -ne 0 ]]; then
        echo -e "${red}v2ray安装或升级失败，请检查错误信息${plain}"
        exit 1
    fi
    systemctl enable v2ray
    systemctl start v2ray
}

close_firewall() {
    if [[ x"${release}" == x"centos" ]]; then
        systemctl stop firewalld
        systemctl disable firewalld
    elif [[ x"${release}" == x"ubuntu" ]]; then
        ufw disable
    elif [[ x"${release}" == x"debian" ]]; then
        iptables -P INPUT ACCEPT
        iptables -P OUTPUT ACCEPT
        iptables -P FORWARD ACCEPT
        iptables -F
    fi
}

port=80
user="sprov"
pwd="blog.sprov.xyz"

init_config() {
    if [[ ! -e "${conf_dir}" ]]; then
        mkdir ${conf_dir}
    fi
    if [[ ! -f ${conf_path} ]]; then
        echo "port=${port}" >> ${conf_path}
        echo "username=${user}" >> ${conf_path}
        echo "password=${pwd}" >> ${conf_path}
        echo "keystoreFile=" >> ${conf_path}
        echo "keystorePass=" >> ${conf_path}
    else
        sed -i "s/^port=.*/port=${port}/" ${conf_path}
        sed -i "s/^username=.*/username=${user}/" ${conf_path}
        sed -i "s/^password=.*/password=${pwd}/" ${conf_path}
    fi

    echo ""
    echo -e "面板监听端口（不是v2ray端口）：${green}${port}${plain}"
    echo -e "面板登录用户名：${green}${user}${plain}"
    echo -e "面板登录密码：${green}${pwd}${plain}"
}

init_service() {
    echo "[Unit]" > /etc/systemd/system/sprov-ui.service
    echo "Description=sprov-ui Service" >> /etc/systemd/system/sprov-ui.service
    echo "After=network.target" >> /etc/systemd/system/sprov-ui.service
    echo "Wants=network.target" >> /etc/systemd/system/sprov-ui.service
    echo "" >> /etc/systemd/system/sprov-ui.service
    echo "[Service]" >> /etc/systemd/system/sprov-ui.service
    echo "Type=simple" >> /etc/systemd/system/sprov-ui.service
    java_cmd="/usr/bin/java"
    echo "ExecStart=${java_cmd} -jar /usr/local/sprov-ui/sprov-ui.jar" >> /etc/systemd/system/sprov-ui.service
    echo "" >> /etc/systemd/system/sprov-ui.service
    echo "[Install]" >> /etc/systemd/system/sprov-ui.service
    echo "WantedBy=multi-user.target" >> /etc/systemd/system/sprov-ui.service
    systemctl daemon-reload
}

set_systemd() {
    init_service
    reset="y"
    first="y"
    if [[ -f "${conf_path}" ]]; then
        read -p "是否重新设置面板端口、用户名和密码[默认n]：" reset
        first="n"
    fi
    if [[ x"$reset" == x"y" || x"$reset" == x"Y" ]]; then
        read -p "请输入面板监听端口[默认${port}]：" port
        read -p "请输入面板登录用户名[默认${user}]：" user
        read -p "请输入面板登录密码[默认${pwd}]：" pwd
        if [[ -z "${port}" ]]; then
            port=80
        fi
        if [[ -z "${user}" ]]; then
            user="sprov"
        fi
        if [[ -z "${pwd}" ]]; then
            pwd="blog.sprov.xyz"
        fi
        init_config
        if [[ x"${first}" == x"n" ]]; then
            echo ""
            echo -e "${green}设置了新的端口、用户名和密码后记得重启面板${plain}"
        fi
    fi
}

install_sprov-ui() {
    if [[ ! -e "/usr/local/sprov-ui" ]]; then
        mkdir /usr/local/sprov-ui
    fi
    if [[ -f "/usr/local/sprov-ui/sprov-ui.war" ]]; then
        rm /usr/local/sprov-ui/sprov-ui.war -f
    fi
    last_version=$(curl --silent "https://api.github.com/repos/sprov065/sprov-ui/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
    echo -e "检测到sprov-ui最新版本：${last_version}，开始下载核心文件"
    wget -N --no-check-certificate -O /usr/local/sprov-ui/sprov-ui.jar https://github.com/sprov065/sprov-ui/releases/download/${last_version}/sprov-ui-${last_version}.jar
    if [[ $? -ne 0 ]]; then
        echo -e "${red}下载sprov-ui核心文件失败，请确保你的服务器能够下载Github的文件，如果多次安装失败，请参考手动安装教程${plain}"
        exit 1
    fi
    set_systemd
    echo -e ""
    echo -e "${green}sprov-ui 面板安装成功${plain}\n"
    echo -e ""
    echo -e "sprov-ui 管理脚本使用方法: "
    echo -e "------------------------------------------"
    echo -e "sprov-ui              - 显示管理菜单 (功能更多)"
    echo -e "sprov-ui start        - 启动 sprov-ui 面板"
    echo -e "sprov-ui stop         - 停止 sprov-ui 面板"
    echo -e "sprov-ui restart      - 重启 sprov-ui 面板"
    echo -e "sprov-ui status       - 查看 sprov-ui 状态"
    echo -e "sprov-ui enable       - 设置 sprov-ui 开机自启"
    echo -e "sprov-ui disable      - 取消 sprov-ui 开启自启"
    echo -e "sprov-ui log          - 查看 sprov-ui 日志"
    echo -e "sprov-ui update       - 更新 sprov-ui 面板"
    echo -e "sprov-ui install      - 安装 sprov-ui 面板"
    echo -e "sprov-ui uninstall    - 卸载 sprov-ui 面板"
    echo -e "------------------------------------------"
    echo -e ""
    echo -e "若未下载管理脚本，使用以下命令下载管理脚本:"
    echo -e "wget -O /usr/bin/sprov-ui -N --no-check-certificate https://github.com/sprov065/sprov-ui/raw/master/sprov-ui.sh && chmod +x /usr/bin/sprov-ui"
    echo -e ""
    echo -e "若未安装 bbr 等加速工具，推荐使用以下命令一键安装 bbr："
    echo -e "wget --no-check-certificate https://github.com/sprov065/blog/raw/master/bbr.sh && bash bbr.sh"
    echo -e ""
}

echo "开始安装"
install_java
install_v2ray
close_firewall
install_sprov-ui
