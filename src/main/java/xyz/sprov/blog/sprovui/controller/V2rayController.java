package xyz.sprov.blog.sprovui.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.service.ExtraConfigService;
import xyz.sprov.blog.sprovui.service.V2rayConfigService;
import xyz.sprov.blog.sprovui.service.V2rayService;
import xyz.sprov.blog.sprovui.util.Context;

import java.util.Iterator;
import java.util.Map;

//@Controller
//@RequestMapping("v2ray")
public class V2rayController {

//    @Autowired
    private V2rayService service = Context.v2rayService;

//    @Autowired
    private V2rayConfigService configService = Context.v2rayConfigService;

    private ExtraConfigService extraConfigService = Context.extraConfigService;

    /**
     * v2ray状态
     * 0：运行
     * 1：未运行
     * 2：未安装
     */
//    @ResponseBody
//    @GetMapping("status")
    public Msg status() {
        try {
            int status = service.status();
            switch (status) {
                case 0:
                    return new Msg(true, "运行中", status);
                case 1:
                    return new Msg(false, "未运行", status);
                default:
                    return new Msg(false, "未安装", status);
            }
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "获取v2ray状态发生错误，请稍后重试");
        }
    }

    /**
     * 安装v2ray
     */
//    @ResponseBody
//    @PostMapping("install")
    public Msg install() {
        try {
            if (service.isInstalled()) {
                return new Msg(false, "v2ray已安装，不能再次安装，你可以选择升级v2ray");
            } else if (service.install()) {
                return new Msg(true, "安装成功，请手动启动v2ray");
            }
            return new Msg(false, "安装失败，未知原因");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "获取v2ray状态发生错误，请稍后重试");
        }
    }

    /**
     * 卸载v2ray
     *
     * 暂时不写
     */
//    @ResponseBody
//    @PostMapping("uninstall")
//    public Msg uninstall() {
//        return new Msg(false);
//    }

    /**
     * 升级v2ray
     */
//    @ResponseBody
//    @PostMapping("update")
    public Msg update() {
        try {
            if (service.update()) {
                return new Msg(true, "升级成功，请手动重启v2ray生效");
            }
            return new Msg(false, "升级失败，未知原因");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "获取v2ray状态发生错误，请稍后重试");
        }
    }

    /**
     * 启动v2ray
     */
//    @ResponseBody
//    @PostMapping("start")
    public Msg start() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray尚未安装，请先安装");
            } else if (service.start()) {
                return new Msg(true, "操作成功，如配置有误可能会导致重启失败");
            }
            return new Msg(false, "启动失败，请使用systemctl status v2ray -l命令查看失败原因");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "启动出现错误：" + e.getMessage());
        }
    }

//    @ResponseBody
//    @PostMapping("restart")
    public Msg restart() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray尚未安装，请先安装");
            }
            service.restart();
            return new Msg(true, "操作成功，将在数秒后重启，如配置有误可能会导致重启失败");
            //return new Msg(false, "重启失败，请使用systemctl status v2ray -l命令查看失败原因");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "重启出现错误：" + e.getMessage());
        }
    }

    /**
     * 关闭v2ray
     */
//    @ResponseBody
//    @PostMapping("stop")
    public Msg stop() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray 尚未安装，请先安装");
            }
            service.stop();
            return new Msg(true, "操作成功，将在数秒后关闭 v2ray");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "关闭出现错误：" + e.getMessage());
        }
    }

    /**
     * 获取完整的配置文件内容
     */
//    @ResponseBody
//    @PostMapping("config")
    public Msg config() {
        try {
            JSONObject config = configService.getConfig();
            JSONArray inbounds = config.getJSONArray("inbounds");
            Iterator<Object> iterator = inbounds.iterator();
            Map<String, JSONObject> tagInboundMap = extraConfigService.getTagInboundMap();
            while (iterator.hasNext()) {
                JSONObject inbound = (JSONObject) iterator.next();
                inbound.put("enable", true);
                String tag = inbound.getString("tag");
                if (StringUtils.isEmpty(tag)) {
                    continue;
                } else if ("api".equals(tag)) {
                    iterator.remove();
                }
                JSONObject extraInbound = tagInboundMap.get(tag);
                if (extraInbound != null) {
                    inbound.putAll(extraInbound);
                }
            }
            JSONArray disabledInbounds = extraConfigService.getDisabledInbounds();
            for (Object obj : disabledInbounds) {
                ((JSONObject) obj).put("enable", false);
            }
            inbounds.addAll(disabledInbounds);
            return new Msg(true, config.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            return new Msg(false, "获取配置文件内容失败：" + e.getMessage());
        }
    }

}
