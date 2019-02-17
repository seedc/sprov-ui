package xyz.sprov.blog.sprovui.route;

import spark.Route;
import xyz.sprov.blog.sprovui.controller.V2rayController;
import xyz.sprov.blog.sprovui.util.Context;

public class V2rayRoute {

    private V2rayController controller = Context.v2rayController;

    public Route status() {
        return (request, response) -> controller.status();
    }

    public Route start() {
        return (request, response) -> controller.start();
    }

    public Route stop() {
        return (request, response) -> controller.stop();
    }

    public Route restart() {
        return (request, response) -> controller.restart();
    }

    public Route config() {
        return (request, response) -> controller.config();
    }
}
