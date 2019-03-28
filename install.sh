#!/bin/bash

red='\033[0;31m'
green='\033[0;32m'
yellow='\033[0;33m'
plain='\033[0m'

cur_dir=$(pwd)

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

install_java() {
	if [[ -f /usr/bin/java ]]; then
		java_version=`/usr/bin/java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
		if [[ "${java_version}" > "1.8" ]]; then
			echo -e "${green}已检测到1.8及以上版本的java，无需重复安装${plain}"
		else
			echo -e "错误：${green}/usr/bin/java${red}的版本低于1.8，请安装大于等于1.8版本的java${plain}"
			exit -1
		fi
    elif [[ x"${release}" == x"centos" ]]; then
        yum install java-1.8.0-openjdk curl -y
    elif [[ x"${release}" == x"debian" || x"${release}" == x"ubuntu" ]]; then
        apt install default-jre curl -y
    fi
    if [[ $? -ne 0 ]]; then
        echo -e "${red}Java环境安装失败，请检查错误信息${plain}"
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
    if [[ ! -e "/etc/sprov-ui" ]]; then
        mkdir /etc/sprov-ui
    fi
    echo "port=${port}" > /etc/sprov-ui/sprov-ui.conf
    echo "username=${user}" >> /etc/sprov-ui/sprov-ui.conf
    echo "password=${pwd}" >> /etc/sprov-ui/sprov-ui.conf

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
    if [[ -f "/etc/sprov-ui/sprov-ui.conf" ]]; then
        read -p "是否重新设置面板端口、用户名和密码[y/n]：" reset
        first="n"
    fi
    if [[ x"$reset" == x"y" || x"$reset" == x"Y" ]]; then
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
    echo ""
    echo -e "${green}sprov-ui面板安装成功${plain}\n"
    echo ""
    echo -e "开启面板：systemctl start sprov-ui"
    echo -e "关闭面板：systemctl stop sprov-ui"
    echo -e "重启面板：systemctl restart sprov-ui"
    echo -e "运行状态：systemctl status sprov-ui"
    echo -e "开机启动：systemctl enable sprov-ui"
    echo -e "取消开机启动：systemctl disable sprov-ui"
    echo ""
    echo -e "若启动面板失败，请使用以下命令手动启动检查问题所在："
    echo -e "/usr/bin/java -jar /usr/local/sprov-ui/sprov-ui.jar"
    echo ""
    echo -e "若未安装bbr等加速工具，推荐使用以下命令一键安装bbr："
    echo -e "wget --no-check-certificate https://github.com/sprov065/blog/raw/master/bbr.sh && bash bbr.sh"
}

echo "开始安装"
install_java
install_v2ray
close_firewall
install_sprov-ui
