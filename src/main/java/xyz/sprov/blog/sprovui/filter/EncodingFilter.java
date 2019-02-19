package xyz.sprov.blog.sprovui.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

public class EncodingFilter implements Filter {
    @Override
    public void handle(Request request, Response response) throws Exception {
        request.raw().setCharacterEncoding("UTF-8");
        response.raw().setCharacterEncoding("UTF-8");
    }
}
