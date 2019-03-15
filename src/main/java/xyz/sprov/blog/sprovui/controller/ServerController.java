package xyz.sprov.blog.sprovui.controller;

import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.service.ServerService;
import xyz.sprov.blog.sprovui.util.Context;

import javax.servlet.http.HttpServletRequest;

//@Controller
//@RequestMapping("server")
public class ServerController {

//    @Autowired
    private ServerService service = Context.serverService;

//    @ResponseBody
//    @PostMapping("status")
    public Msg status(HttpServletRequest request) {
        try {
            return new Msg(true, service.statuses(request));
        } catch (V2rayException e) {
            return new Msg(false, "刷新状态失败：" + e.getMessage(), e.getObject());
        } catch (Exception e) {
            return new Msg(false, "刷新状态失败：" + e.getMessage());
        }
    }

}
