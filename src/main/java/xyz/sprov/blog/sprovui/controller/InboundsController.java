package xyz.sprov.blog.sprovui.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.V2rayConfigException;
import xyz.sprov.blog.sprovui.service.V2rayConfigService;
import xyz.sprov.blog.sprovui.util.Context;

import java.io.IOException;

//@Controller
//@RequestMapping("v2ray/inbound")
public class InboundsController {

//    @Autowired
    private V2rayConfigService configService = Context.v2rayConfigService;

    private JSONObject getInbound(int port,
                                  String protocol,
                                  String settings,
                                  String streamSettings,
                                  String tag) {
        JSONObject inbound = new JSONObject();
        inbound.put("port", port);
        inbound.put("protocol", protocol);
        inbound.put("tag", tag);
//        if (!StringUtils.isBlank(tag)) {
//        }
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
    public Msg add(int port,
                   String protocol,
                   String settings,
                   String streamSettings,
                   String tag) {
        JSONObject inbound;
        try {
            inbound = getInbound(port, protocol, settings, streamSettings, tag);
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
    public Msg edit(int port,
                    String protocol,
                    String settings,
                    String streamSettings,
                    String tag) {
        JSONObject inbound;
        try {
            inbound = getInbound(port, protocol, settings, streamSettings, tag);
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
            return new Msg(false, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 添加一个VMess用户
     */
//    @ResponseBody
//    @PostMapping("vmess/add")
    public Msg vmessAdd(int port, String id, int alterId, String secure) {
        JSONObject client = new JSONObject();
        client.put("port", port);
        client.put("id", id);
        client.put("alterId", alterId);
        client.put("secure", secure);
        try {
            configService.addVmessUser(client);
            return new Msg(true, "修改配置文件成功，需重启v2ray生效");
        } catch (V2rayConfigException e) {
            return new Msg(false, e.getMessage());
        } catch (IOException e) {
            return new Msg(false, "读取或写入配置文件失败");
        }
    }

    /**
     * 删除一个VMess入站协议
     */
//    @ResponseBody
//    @PostMapping("vmess/del")
    public Msg vmessDel(int port, String uuid) {
        return new Msg(false);
    }

    /**
     * 删除一个ss入站协议
     */
//    @ResponseBody
//    @PostMapping("ss/del")
    public Msg ssDel(int port) {
        return new Msg(false);
    }

    /**
     * 删除一个tg代理入站协议
     */
//    @ResponseBody
//    @PostMapping("mtproto/del")
    public Msg mtprotoDel() {
        return new Msg(false);
    }

}
