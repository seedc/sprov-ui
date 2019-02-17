package xyz.sprov.blog.sprovui.controller;

import org.apache.commons.lang3.StringUtils;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.bean.User;
//import xyz.sprov.blog.sprovui.util.SessionContainer;

import javax.servlet.http.HttpServletRequest;

//@Controller
//@SessionAttributes(SessionContainer.LOGIN_USER)
//@Deprecated
//public class BaseController {
//
////    @Value("${user.username}")
//    private String username;
////    @Value("${user.password}")
//    private String password;
//
////    @PostConstruct
//    public void init() {
//        username = StringUtils.trim(username);
//        password = StringUtils.trim(password);
//        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
//            System.err.println("用户名和密码不能为空");
//            System.exit(-1);
//        }
//    }
//
////    @GetMapping({"", "/"})
//    public String index(HttpServletRequest request) {
//        if (SessionContainer.getUser(request) != null) {
//            return "redirect:/v2ray/";
//        }
//        return "index";
//    }
//
////    @ResponseBody
////    @PostMapping("login")
//    public Msg login(User user, HttpServletRequest request) {
//        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
//            return new Msg(false, "用户名和密码不能为空");
//        } else if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
//            SessionContainer.setUser(request, user);
//            return new Msg(true, "登录成功");
//        }
//        return new Msg(false, "用户名或密码错误");
//    }
//
//}
