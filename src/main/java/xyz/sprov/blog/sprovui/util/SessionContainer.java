package xyz.sprov.blog.sprovui.util;

import org.springframework.ui.ModelMap;
import xyz.sprov.blog.sprovui.bean.User;

import javax.servlet.http.HttpServletRequest;

public class SessionContainer {

    public static final String LOGIN_USER = "LOGIN_USER";

    private SessionContainer() {}

    public static User getUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(LOGIN_USER);
    }

    public static void setUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute(LOGIN_USER, user);
    }

    public static User getUser(ModelMap modelMap) {
        return (User) modelMap.get(LOGIN_USER);
    }

    public static void setUser(ModelMap modelMap, User user) {
        modelMap.addAttribute(LOGIN_USER, user);
    }

    private static void set(ModelMap modelMap, String key, Object value) {
        if (value == null) {
            modelMap.remove(key);
        } else {
            modelMap.addAttribute(key, value);
        }
    }

}
