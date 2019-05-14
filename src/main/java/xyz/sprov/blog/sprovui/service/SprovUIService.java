package xyz.sprov.blog.sprovui.service;

import org.apache.commons.io.FileUtils;
import spark.utils.IOUtils;
import xyz.sprov.blog.sprovui.exception.SprovUIException;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.Context;
import xyz.sprov.blog.sprovui.util.ExecUtil;
import xyz.sprov.blog.sprovui.util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SprovUIService {

    private static final String GITHUB_LAST_RELEASE_URL = "https://github.com/sprov065/sprov-ui/releases/latest";

    private String currentVersion = Config.currentVersion();

    private String lastVersion = currentVersion;

    private Pattern urlVersionPattern = Pattern.compile("https://github\\.com/[^/]+/[^/]+/releases/tag/(.+)");

    private static final String JAR_DIR = "/usr/local/sprov-ui/";

    public SprovUIService() {
        Context.threadService.scheduleAtFixedRate(new GetLastVersionRunnable(), 5, 29, TimeUnit.MINUTES);
    }

    private String getLastVersion() throws Exception {
        String url = HttpUtil.getRealUrl(GITHUB_LAST_RELEASE_URL);
        Matcher matcher = urlVersionPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IOException("获取最新版本失败");
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    private String getLastDownloadUrl() {
        return "https://github.com/sprov065/sprov-ui/releases/download/" + lastVersion + "/sprov-ui-" + lastVersion + ".jar";
    }

    private void refreshLastVersion() throws Exception {
        String lastVersion = getLastVersion();
        if (currentVersion.equals(lastVersion)) {
            return;
        }
        this.lastVersion = lastVersion;
    }

    public boolean isLastVersion() {
        return currentVersion.equalsIgnoreCase(lastVersion);
    }

    public String lastVersion() {
        return lastVersion;
    }

    public void update() throws IOException {
        if (currentVersion.equals(lastVersion)) {
            throw new SprovUIException("已经是最新版本了");
        }

        // 下载新版本软件包
        String downloadUrl = getLastDownloadUrl();
        byte[] bytes;
        try (InputStream in = HttpUtil.download(downloadUrl)) {
            bytes = IOUtils.toByteArray(in);
        } catch (Exception e) {
            throw new IOException("下载软件包失败：" + e.getMessage());
        }

        // 备份旧版本软件包
        File oldJar = new File(JAR_DIR + "sprov-ui.jar");
        File copiedOldJar = new File(JAR_DIR + "sprov-ui-" + currentVersion + ".jar");
        FileUtils.deleteQuietly(copiedOldJar);
        try {
            FileUtils.copyFile(oldJar, copiedOldJar);
        } catch (Exception e) {
            FileUtils.deleteQuietly(copiedOldJar);
            throw new IOException("备份旧软件包失败：" + e.getMessage());
        }

        // 写入新版本软件包至目录
        File newJar = new File(JAR_DIR + "sprov-ui-" + lastVersion + ".jar");
        FileUtils.deleteQuietly(newJar);
        try {
            FileUtils.writeByteArrayToFile(newJar, bytes);
        } catch (Exception e) {
            FileUtils.deleteQuietly(newJar);
            FileUtils.deleteQuietly(copiedOldJar);
            throw new IOException("下载软件包失败：" + e.getMessage());
        }

        // 新版本替换旧版本
        try {
            FileUtils.deleteQuietly(oldJar);
            if (newJar.renameTo(oldJar)) {
                FileUtils.deleteQuietly(copiedOldJar);
            } else {
                throw new IOException();
            }
        } catch (Exception e) {
            FileUtils.deleteQuietly(oldJar);
            copiedOldJar.renameTo(oldJar);
            throw new IOException("严重错误！！！新版本软件包替换旧版本软件包失败，请手动升级！！！");
        }

        currentVersion = lastVersion;
    }

    public void restart() throws IOException, InterruptedException {
        ExecUtil.execForStatus("systemctl restart sprov-ui");
    }

    private class GetLastVersionRunnable implements Runnable {

        @Override
        public void run() {
            try {
                refreshLastVersion();
            } catch (Exception e) {
                System.err.println("检测 sprov-ui 最新版本失败：" + e.getMessage());
            }
        }
    }

}
