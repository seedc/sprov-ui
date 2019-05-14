package xyz.sprov.blog.sprovui;

import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.SparkUtil;

import java.io.File;

import static spark.Spark.*;
import static xyz.sprov.blog.sprovui.util.Context.*;

public class SprovUISparkApp {

    static {
        System.setProperty("file.encoding", "UTF-8");
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int port  = Config.getPort();
        port(port);

        threadPool(4, 1, 30000);

        initExceptionHandler(e -> {
            System.err.println("sprov-ui 启动失败：" + e.getMessage());
            System.exit(1);
        });

//        exception(Exception.class, (e, request, response) -> System.out.println(e.getMessage()));

        configSSL();

        staticFiles.location("/static");
        staticFiles.expireTime(3600 * 24 * 30 * 6);

        String basePath = Config.basePath();
        if (StringUtils.isBlank(basePath)) {
            createPath();
        } else {
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            while (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            Config.setBasePath(basePath);
            System.out.println("basePath = " + basePath);
            path(basePath, SprovUISparkApp::createPath);
        }

        awaitInitialization();
        long end = System.currentTimeMillis();
        System.out.println("sprov-ui 启动成功，耗时 " + (end - start) + " ms，面板监听端口为 " + port);
    }

    private static void configSSL() {
        String keystoreFile = Config.keystoreFile();
        if (!StringUtils.isEmpty(keystoreFile)) {
            if (!new File(keystoreFile).exists()) {
                throw new RuntimeException("keystoreFile - " + keystoreFile + " 不存在");
            } else {
                secure(keystoreFile, Config.keystorePass(), null, null);
            }
        }
    }

    private static void createPath() {

        before("", encodingFilter);
        before("/*", encodingFilter);

        get("", baseRoute.index());
        get("/", baseRoute.index());
        post("/login", baseRoute.login(), jsonTransformer);
        get("/logout", baseRoute.logout(), jsonTransformer);
        get("/logout/", baseRoute.logout(), jsonTransformer);

        get("/robots.txt", baseRoute.robots());

        path("/v2ray", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            get("", SparkUtil.view("/v2ray/index"));
            get("/", SparkUtil.view("/v2ray/index"));
            get("/accounts", SparkUtil.view("/v2ray/accounts"));
            get("/accounts/", SparkUtil.view("/v2ray/accounts"));
            get("/clients", SparkUtil.view("/v2ray/clients"));
            get("/clients/", SparkUtil.view("/v2ray/clients"));

            post("/status", v2rayRoute.status(), jsonTransformer);
            post("/start", v2rayRoute.start(), jsonTransformer);
            post("/restart", v2rayRoute.restart(), jsonTransformer);
            post("/stop", v2rayRoute.stop(), jsonTransformer);
            post("/config", v2rayRoute.config(), jsonTransformer);

            path("/inbound", () -> {
                post("/add", inboundsRoute.add(), jsonTransformer);
                post("/edit", inboundsRoute.edit(), jsonTransformer);
                post("/del", inboundsRoute.del(), jsonTransformer);

                post("/openTraffic", inboundsRoute.openTraffic(), jsonTransformer);
                post("/resetTraffic", inboundsRoute.resetTraffic(), jsonTransformer);
                post("/resetAllTraffic", inboundsRoute.resetAllTraffic(), jsonTransformer);

                post("/enable", inboundsRoute.enable(), jsonTransformer);
                post("/disable", inboundsRoute.disable(), jsonTransformer);
                post("/delDisabled", inboundsRoute.delDisabled(), jsonTransformer);
            });
        });

        path("/server", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            post("/status", serverRoute.status(), jsonTransformer);
        });

        path("/sprov-ui", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            post("/isLastVersion", sprovUIController.isLastVersion(), jsonTransformer);
            post("/update", sprovUIController.update(), jsonTransformer);
            post("/restart", sprovUIController.restart(), jsonTransformer);
        });
    }

}
