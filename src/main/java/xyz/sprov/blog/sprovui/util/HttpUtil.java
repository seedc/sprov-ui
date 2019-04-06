package xyz.sprov.blog.sprovui.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HttpUtil {

    public static String getRealUrl(String url) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            conn.getResponseCode();
            return conn.getURL().toString();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static InputStream download(String url) throws IOException {
        URL u = new URL(url);
        return u.openStream();
    }

    public static void post(String url, Map<String, Object> data) throws IOException {
        HttpURLConnection conn = null;
        URL u = new URL(url);
        try {
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            conn.connect();

            StringBuilder content = new StringBuilder();
            data.forEach((key, value) -> {
                try {
                    content.append(key)
                            .append('=')
                            .append(URLEncoder.encode(String.valueOf(value), "UTF-8"))
                            .append('&');
                } catch (UnsupportedEncodingException ignore) {}
            });
            if (content.length() > 0) {
                content.setLength(content.length() - 1);
            }
            try (OutputStream out = conn.getOutputStream()) {
                out.write(content.toString().getBytes());
                out.flush();
            }
            conn.getInputStream().close();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
