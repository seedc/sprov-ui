package xyz.sprov.blog.sprovui.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ServletUtil {

    public static String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        int port = request.getServerPort();
        if (port == 80 || port == 443) {
            return request.getScheme() + "://" + request.getServerName() + path;
        } else {
            return request.getScheme() + "://" + request.getServerName() + ":" + port + path;
        }
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            parameterMap.put(name, request.getParameter(name));
        }
        return parameterMap;
    }

}
