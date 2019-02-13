package xyz.sprov.blog.sprovui.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.util.ServletUtil;
import xyz.sprov.blog.sprovui.util.SessionContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (SessionContainer.getUser(request) == null) {
            if (ServletUtil.isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print(new Msg(false, "您的登录时效已过，请重新登录"));
            } else {
                response.sendRedirect("/");
            }
            return false;
        }
        return true;
    }
}
