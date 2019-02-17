package xyz.sprov.blog.sprovui.util;

import spark.ModelAndView;
import spark.Request;
import spark.Route;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class SparkUtil {

    private SparkUtil() {}

    public static boolean isAjax(Request request) {
        return "XMLHttpRequest".equals(request.headers("X-Requested-With"));
    }

    public static String render(String path) {
        return render(new HashMap<>(), path);
    }

    public static String render(Map<String, Object> model, String path) {
        if (model == null) {
            model = new HashMap<>();
        }
        return new ThymeleafTemplateEngine().render(new ModelAndView(model, path));
    }

    public static Route view(String path) {
        return (request, response) -> render(path);
    }

}
