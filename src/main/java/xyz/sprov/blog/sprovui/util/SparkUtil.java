package xyz.sprov.blog.sprovui.util;

import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import spark.ModelAndView;
import spark.Request;
import spark.Route;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class SparkUtil {

    private static final ClassLoaderTemplateResolver templateResolver;
    private static final ThymeleafTemplateEngine templateEngine;

    static {
        templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);

        templateEngine = new ThymeleafTemplateEngine(templateResolver);
    }

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
        return templateEngine.render(new ModelAndView(model, path));
    }

    public static Route view(String path) {
        return (request, response) -> render(path);
    }

}
