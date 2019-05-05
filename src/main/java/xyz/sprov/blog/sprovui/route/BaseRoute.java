package xyz.sprov.blog.sprovui.route;

import org.apache.commons.lang3.StringUtils;
import spark.Route;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.bean.User;
import xyz.sprov.blog.sprovui.service.SecureService;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.Context;
import xyz.sprov.blog.sprovui.util.SessionUtil;
import xyz.sprov.blog.sprovui.util.SparkUtil;

public class BaseRoute {

    private SecureService secureService = Context.secureService;

    private String username = Config.username();

    private String password = Config.password();

    public Route index() {
        return (request, response) -> {
            if (SessionUtil.getUser(request) != null) {
                response.redirect("/v2ray/");
            }
            return SparkUtil.render("/index");
        };
    }

    public Route login() {
        return (request, response) -> {
            String user = request.queryParams("username");
            String pwd = request.queryParams("password");
            if (StringUtils.isEmpty(user) || StringUtils.isEmpty(pwd)) {
                return new Msg(false, "用户名和密码不能为空");
            } else if (secureService.isBlackList(request)) {
                return new Msg(false, "用户名或密码错误");
            } else if (username.equals(user) && password.equals(pwd)) {
                SessionUtil.setUser(request, new User(user, pwd));
                return new Msg(true, "登录成功");
            }
            return new Msg(false, "用户名或密码错误");
        };
    }

    public Route logout() {
        return (request, response) -> {
            SessionUtil.removeUser(request);
            response.redirect("/");
            return "";
        };
    }

    public Route robots() {
        return (request, response) -> {
            response.type("text/plain");
            return "User-agent: *\n" + "Disallow: /";
        };
    }

}
