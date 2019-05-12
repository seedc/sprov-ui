package xyz.sprov.blog.sprovui.route;

import spark.Request;
import spark.Route;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.controller.InboundsController;
import xyz.sprov.blog.sprovui.util.Context;

import static spark.Spark.halt;

public class InboundsRoute {

    private InboundsController controller = Context.inboundsController;

    private Msg addOrEdit(Request request, String action) {
        Integer port = request.queryMap("port").integerValue();
        if (port == null) {
            halt(404);
            return null;
        }
        String protocol = request.queryParams("protocol");
        String listen = request.queryParams("listen");
        String settings = request.queryParams("settings");
        String streamSettings = request.queryParams("streamSettings");
        String remark = request.queryParams("remark");
        if ("add".equals(action)) {
            return controller.add(listen, port, protocol, settings, streamSettings, remark);
        } else if ("edit".equals(action)) {
            String tag = request.queryParams("tag");
            int oldPort = request.queryMap("oldPort").integerValue();
            return controller.edit(listen, oldPort, port, protocol, settings, streamSettings, remark, tag);
        }
        throw new IllegalArgumentException("Unknown action: " + action);
    }

    public Route add() {
        return (request, response) -> addOrEdit(request, "add");
    }

    public Route edit() {
        return (request, response) -> addOrEdit(request, "edit");
    }

    public Route del() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.del(port);
        };
    }

    public Route openTraffic() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.openTraffic(port);
        };
    }

    public Route resetTraffic() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.resetTraffic(port);
        };
    }

    public Route resetAllTraffic() {
        return (request, response) -> controller.resetAllTraffic();
    }

    public Route enable() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.enable(port);
        };
    }

    public Route disable() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.disable(port);
        };
    }

    public Route delDisabled() {
        return (request, response) -> {
            Integer port = request.queryMap("port").integerValue();
            if (port == null) {
                halt(404);
                return null;
            }
            return controller.delDisabled(port);
        };
    }
}
