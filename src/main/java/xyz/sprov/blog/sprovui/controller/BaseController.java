package xyz.sprov.blog.sprovui.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.bean.User;
import xyz.sprov.blog.sprovui.util.SessionContainer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Controller
@SessionAttributes(SessionContainer.LOGIN_USER)
public class BaseController {

    @Value("${user.username}")
    private String username;
    @Value("${user.password}")
    private String password;

    @PostConstruct
    public void init() {
        username = StringUtils.trim(username);
        password = StringUtils.trim(password);
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            System.err.println("用户名和密码不能为空");
            System.exit(-1);
        }
    }

    @GetMapping({"", "/"})
    public String index(HttpServletRequest request) {
        if (SessionContainer.getUser(request) != null) {
            return "redirect:/v2ray/";
        }
        return "index";
    }

    @ResponseBody
    @PostMapping("login")
    public Msg login(User user, HttpServletRequest request) {
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            return new Msg(false, "用户名和密码不能为空");
        } else if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
            SessionContainer.setUser(request, user);
            return new Msg(true, "登录成功");
        }
        return new Msg(false, "用户名或密码错误");
    }

}
