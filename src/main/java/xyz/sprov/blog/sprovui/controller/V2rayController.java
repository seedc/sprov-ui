package xyz.sprov.blog.sprovui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.service.V2rayConfigService;
import xyz.sprov.blog.sprovui.service.V2rayService;

@Controller
@RequestMapping("v2ray")
public class V2rayController {

    @Autowired
    private V2rayService service;

    @Autowired
    private V2rayConfigService configService;

    @GetMapping("")
    public String index() {
        return "v2ray/index";
    }

    @GetMapping("accounts")
    public String accounts() {
        return "v2ray/accounts";
    }

    @GetMapping("clients")
    public String clients() { return "v2ray/clients"; }

    /**
     * v2ray状态
     * 0：运行
     * 1：未运行
     * 2：未安装
     */
    @ResponseBody
    @GetMapping("status")
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
    @ResponseBody
    @PostMapping("install")
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
    @ResponseBody
    @PostMapping("update")
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
    @ResponseBody
    @PostMapping("start")
    public Msg start() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray尚未安装，请先安装");
            } else if (service.start()) {
                return new Msg(true, "启动成功");
            }
            return new Msg(false, "启动失败，原因未知");
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "启动出现错误：" + e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("restart")
    public Msg restart() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray尚未安装，请先安装");
            } else if (service.restart()) {
                return new Msg(true, "重启成功");
            }
            return new Msg(false, "重启失败，原因未知");
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
    @ResponseBody
    @PostMapping("stop")
    public Msg stop() {
        try {
            if (!service.isInstalled()) {
                return new Msg(false, "v2ray尚未安装，请先安装");
            } else if (service.stop()) {
                return new Msg(true, "关闭成功");
            }
            return new Msg(false, "关闭失败，原因未知");
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
    @ResponseBody
    @PostMapping("config")
    public Msg config() {
        try {
            return new Msg(true, configService.config());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Msg(false, "获取配置文件内容失败：" + e.getMessage());
        }
    }

}
