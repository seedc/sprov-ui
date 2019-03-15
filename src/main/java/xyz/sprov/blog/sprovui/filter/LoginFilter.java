package xyz.sprov.blog.sprovui.filter;

import spark.Filter;
import spark.Request;
import spark.Response;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.util.SessionUtil;
import xyz.sprov.blog.sprovui.util.SparkUtil;

import static spark.Spark.halt;

public class LoginFilter implements Filter {

    @Override
    public void handle(Request request, Response response) {
        if (SessionUtil.getUser(request) == null) {
            if (SparkUtil.isAjax(request)) {
                response.type("text/json");
                halt(new Msg(false, "您的登录时效已过，请重新登录").toString());
            } else {
                response.redirect("/");
                halt();
            }
        }
    }
}
