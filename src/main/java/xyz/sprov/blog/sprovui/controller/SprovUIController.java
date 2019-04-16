package xyz.sprov.blog.sprovui.controller;

import spark.Route;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.SprovUIException;
import xyz.sprov.blog.sprovui.service.SprovUIService;
import xyz.sprov.blog.sprovui.util.Context;

public class SprovUIController {

    private SprovUIService service = Context.sprovUIService;

    public Route isLastVersion() {
        return (request, response) -> new Msg(true, service.lastVersion(), service.getCurrentVersion());
    }

    public Route update() {
        return (request, response) -> {
            try {
                service.update();
                return new Msg(true, "面板升级成功，请重启面板");
            } catch (SprovUIException e) {
                return new Msg(true, e.getMessage());
            } catch (Exception e) {
                return new Msg(false, "面板升级失败：" + e.getMessage());
            }
        };
    }

    public Route restart() {
        return (request, response) -> {
            try {
                service.restart();
                return new Msg(true, "操作成功，请在几秒后刷新页面");
            } catch (Exception e) {
                return new Msg(false, "重启失败：" + e.getMessage());
            }
        };
    }
}
