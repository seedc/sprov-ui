package xyz.sprov.blog.sprovui.util;

import spark.Request;
import xyz.sprov.blog.sprovui.bean.User;

public class SessionUtil {

    private static final String USER_LOGIN = "USER_LOGIN";

    private SessionUtil() {}

    public static User getUser(Request request) {
        return request.session().attribute(USER_LOGIN);
    }

    public static void setUser(Request request, User user) {
        request.session().attribute(USER_LOGIN, user);
    }

    public static void removeUser(Request request) {
        request.session().removeAttribute(USER_LOGIN);
    }

}
