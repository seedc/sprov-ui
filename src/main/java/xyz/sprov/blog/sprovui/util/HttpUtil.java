package xyz.sprov.blog.sprovui.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
