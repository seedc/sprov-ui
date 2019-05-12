package xyz.sprov.blog.sprovui.venum;

import spark.utils.StringUtils;

public enum Protocol {

    DOKODEMO_DOOR("dokodemo-door"), // 端口转发
    VMESS("vmess"), // VMess协议
    MT_PROTO("mtproto"), // Telegram代理
    SHADOWSOCKS("shadowsocks"), // shadowsocks
    SOCKS("socks"), // socks 代理
    HTTP("http"); // http 代理

    private static String[] protocolNames;

    static {
        Protocol[] protocols = Protocol.values();
        protocolNames = new String[protocols.length];
        for (int i = 0; i < protocolNames.length; ++i) {
            protocolNames[i] = protocols[i].protocol;
        }
    }

    private String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    public String getValue() {
        return protocol;
    }

    public static boolean checkProtocol(String protocol) {
        if (StringUtils.isBlank(protocol)) {
            return false;
        }
        for (String name : protocolNames) {
            if (name.equals(protocol)) {
                return true;
            }
        }
        return false;
    }
}
