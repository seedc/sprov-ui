package xyz.sprov.blog.sprovui.controller;

import com.alibaba.fastjson.JSONObject;
import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.V2rayConfigException;
import xyz.sprov.blog.sprovui.service.ExtraConfigService;
import xyz.sprov.blog.sprovui.service.V2rayConfigService;
import xyz.sprov.blog.sprovui.service.V2rayService;
import xyz.sprov.blog.sprovui.util.Context;

import java.io.IOException;

//@Controller
//@RequestMapping("v2ray/inbound")
public class InboundsController {

//    @Autowired
    private V2rayConfigService configService = Context.v2rayConfigService;

    private ExtraConfigService extraConfigService = Context.extraConfigService;

    private V2rayService v2rayService = Context.v2rayService;

    private JSONObject getInbound(String listen,
                                  int port,
                                  String protocol,
                                  String settings,
                                  String streamSettings,
                                  String remark) {
        JSONObject inbound = new JSONObject();
        inbound.put("listen", listen);
        inbound.put("port", port);
        inbound.put("protocol", protocol);
        inbound.put("remark", remark);
        try {
            inbound.put("settings", JSONObject.parseObject(settings));
        } catch (Exception e) {
            throw new V2rayConfigException("协议配置JSON格式错误：" + e.getMessage());
        }
        try {
            inbound.put("streamSettings", JSONObject.parseObject(streamSettings));
        } catch (Exception e) {
            throw new V2rayConfigException("传输配置JSON格式错误：" + e.getMessage());
        }
        return inbound;
    }

    /**
     * 添加一个入站协议
     *
     * @param settings 一个JSON字符串
     * @param streamSettings 一个JSON字符串
     */
//    @ResponseBody
//    @PostMapping("add")
    public Msg add(String listen,
                   int port,
                   String protocol,
                   String settings,
                   String streamSettings,
                   String remark) {
        JSONObject inbound;
        try {
            inbound = getInbound(listen, port, protocol, settings, streamSettings, remark);
        } catch (V2rayConfigException e) {
            return new Msg(false, e.getMessage());
        }
        try {
            inbound.put("streamSettings", JSONObject.parseObject(streamSettings));
        } catch (Exception e) {
            return new Msg(false, "传输配置JSON格式错误：" + e.getMessage());
        }
        try {
            configService.addInbound(inbound);
            return new Msg(true, "修改配置文件成功，需重启v2ray生效");
        } catch (V2rayConfigException e) {
            return new Msg(false, e.getMessage());
        } catch (IOException e) {
            return new Msg(false, "读取或写入配置文件失败");
        }
    }

//    @ResponseBody
//    @PostMapping("edit")
    public Msg edit(String listen,
                    int oldPort,
                    int port,
                    String protocol,
                    String settings,
                    String streamSettings,
                    String remark,
                    String tag) {
        JSONObject inbound;
        try {
            inbound = getInbound(listen, port, protocol, settings, streamSettings, remark);
            if (!StringUtils.isEmpty(tag)) {
                inbound.put("tag", tag);
            }
            inbound.put("oldPort", oldPort);
        } catch (V2rayConfigException e) {
            return new Msg(false, e.getMessage());
        }
        try {
            configService.editInbound(inbound);
            return new Msg(true, "修改配置文件成功，需重启v2ray生效");
        } catch (V2rayConfigException e) {
            return new Msg(false, e.getMessage());
        } catch (IOException e) {
            return new Msg(false, "读取或写入配置文件失败");
        }
    }

//    @ResponseBody
//    @PostMapping("del")
    public Msg del(int port) {
        try {
            configService.delInbound(port);
            return new Msg(true, "删除成功，需重启v2ray生效");
        } catch (Exception e) {
            e.printStackTrace();
            return new Msg(false, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 开启 inbound 流量统计
     */
    public Msg openTraffic(int port) {
        try {
            JSONObject inbound = configService.getInbound(port);
            if (inbound == null) {
                return new Msg(false, "此账号不存在");
            }
            inbound.put("tag", "inbound-" + port);
            configService.editInbound(inbound);
            return new Msg(true, "操作成功，需重启v2ray生效");
        } catch (Exception e) {
            e.printStackTrace();
            return new Msg(false, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 重置端口流量
     */
    public Msg resetTraffic(int port) {
        try {
            JSONObject inbound = configService.getInbound(port);
            if (inbound == null) {
                return new Msg(false, "找不到端口为" + port + "的账号");
            }
            String tag = inbound.getString("tag");
            if (StringUtils.isEmpty(tag)) {
                return new Msg(false, "端口为" + port + "的账号没有tag标识");
            }
            extraConfigService.resetTraffic(tag);
            return new Msg(true, "操作成功，此功能无需重启");
        } catch (Exception e) {
            e.printStackTrace();
            return new Msg(false, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 重置所有流量
     */
    public Msg resetAllTraffic() {
        try {
            extraConfigService.resetAllTraffic();
            return new Msg(true, "操作成功，此功能无需重启");
        } catch (Exception e) {
            e.printStackTrace();
            return new Msg(false, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 启用 inbound
     */
    public Msg enable(int port) {
        JSONObject inbound;
        try {
            // 从删除 disabled 列表删除 inbound
            inbound = extraConfigService.delDisabledInbound(port);
            if (inbound == null) {
                return new Msg(false, "端口为 " + port + " 的 inbound 不存在");
            }
        } catch (Exception e) {
            return new Msg(false, "操作失败：" + e.getMessage());
        }
        try {
            // 添加流量配置文件
            String tag = inbound.getString("tag");
            if (!StringUtils.isEmpty(tag)) {
                Long down = inbound.getLong("downlink");
                Long up = inbound.getLong("uplink");
                extraConfigService.addInbound(tag, down, up);
            }
        } catch (Exception e) {
            try {
                extraConfigService.addDisabledInbound(inbound);
            } catch (Exception ignore) {}
            return new Msg(false, "操作失败：" + e.getMessage());
        }
        try {
            // 添加 v2ray 配置文件
            inbound.remove("downlink");
            inbound.remove("uplink");
            configService.addInbound(inbound, false);
        } catch (Exception e) {
            try {
                extraConfigService.addDisabledInbound(inbound);
            } catch (Exception ignore) {}
            return new Msg(false, "操作失败：" + e.getMessage());
        }
        try {
            v2rayService.restart();
            return new Msg(true, "操作成功，已自动重启 v2ray");
        } catch (Exception e) {
            return new Msg(false, "操作成功，但是重启 v2ray 失败，请手动重启");
        }
    }

    /**
     * 禁用 inbound
     */
    public Msg disable(int port) {
        JSONObject inbound;
        try {
            // 获取 inbound
            inbound = configService.getInbound(port);
            if (inbound == null) {
                return new Msg(false, "端口为 " + port + " 的 inbound 不存在");
            }
            String tag = inbound.getString("tag");
            JSONObject extraInbound = extraConfigService.getInboundByTag(tag);
            if (extraInbound != null) {
                inbound.putAll(extraInbound);
            }
        } catch (Exception e) {
            return new Msg(false, "操作失败：" + e.getMessage());
        }
        try {
            // 从 v2ray 配置文件中删除
            configService.delInbound(port);
        } catch (Exception e) {
            return new Msg(false, "操作失败：" + e.getMessage());
        }

        try {
            // 添加进 disabled 列表
            extraConfigService.addDisabledInbound(inbound);
        } catch (Exception e) {
            try {
                inbound.remove("downlink");
                inbound.remove("uplink");
                configService.addInbound(inbound, false);
            } catch (Exception ignore) {}
            return new Msg(false, "操作失败：" + e.getMessage());
        }

        try {
            v2rayService.restart();
            return new Msg(true, "操作成功，已自动重启 v2ray");
        } catch (Exception e) {
            return new Msg(false, "操作成功，但是重启 v2ray 失败，请手动重启");
        }
    }

    /**
     * 删除已禁用的账号
     */
    public Msg delDisabled(int port) {
        try {
            if (extraConfigService.delDisabledInbound(port) == null) {
                return new Msg(false, "端口 " + port + " 不存在");
            }
            return new Msg(true, "删除成功，此操作无需重启");
        } catch (IOException e) {
            return new Msg(false, "删除失败：" + e.getMessage());
        }
    }



}
