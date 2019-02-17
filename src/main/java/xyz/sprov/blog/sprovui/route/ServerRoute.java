package xyz.sprov.blog.sprovui.route;

import spark.Route;
import xyz.sprov.blog.sprovui.controller.ServerController;
import xyz.sprov.blog.sprovui.util.Context;

public class ServerRoute {

    private ServerController controller = Context.serverController;

    public Route status() {
        return (request, response) -> controller.status(request.raw());
    }

}
