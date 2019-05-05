package xyz.sprov.blog.sprovui.util;

import xyz.sprov.blog.sprovui.controller.InboundsController;
import xyz.sprov.blog.sprovui.controller.ServerController;
import xyz.sprov.blog.sprovui.controller.SprovUIController;
import xyz.sprov.blog.sprovui.controller.V2rayController;
import xyz.sprov.blog.sprovui.filter.EncodingFilter;
import xyz.sprov.blog.sprovui.filter.LoginFilter;
import xyz.sprov.blog.sprovui.route.BaseRoute;
import xyz.sprov.blog.sprovui.route.InboundsRoute;
import xyz.sprov.blog.sprovui.route.ServerRoute;
import xyz.sprov.blog.sprovui.route.V2rayRoute;
import xyz.sprov.blog.sprovui.service.*;
import xyz.sprov.blog.sprovui.transformer.JsonTransformer;

public class Context {

    public static final LoginFilter loginFilter = new LoginFilter();
    public static final EncodingFilter encodingFilter = new EncodingFilter();

    public static final JsonTransformer jsonTransformer = new JsonTransformer();

    public static final ThreadService threadService = new ThreadService();
    public static final ExtraConfigService extraConfigService = new ExtraConfigService();
    public static final V2rayService v2rayService = new V2rayService();
    public static final V2rayConfigService v2rayConfigService = new V2rayConfigService();
    public static final ServerService serverService = new ServerService();
    public static final SprovUIService sprovUIService = new SprovUIService();
    public static final ReportService reportService = new ReportService();
    public static final SecureService secureService = new SecureService();

    public static final V2rayController v2rayController = new V2rayController();
    public static final InboundsController inboundsController = new InboundsController();
    public static final ServerController serverController = new ServerController();
    public static final SprovUIController sprovUIController = new SprovUIController();

    public static final BaseRoute baseRoute = new BaseRoute();
    public static final V2rayRoute v2rayRoute = new V2rayRoute();
    public static final ServerRoute serverRoute = new ServerRoute();
    public static final InboundsRoute inboundsRoute = new InboundsRoute();


}
