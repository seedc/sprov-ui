package xyz.sprov.blog.sprovui.util;

import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.bean.InboundTraffic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class V2ctlUtil {

    private static final Pattern octPattern = Pattern.compile("\\\\(\\d{3})");
    private static final Pattern inboundPattern = Pattern.compile("stat:\\s*<\\s*name:\\s*\"inbound>>>(?<tag>[^>]+)>>>traffic>>>(?<type>up|down)link\"(\\s*value:\\s*(?<value>\\d+))?");

    private static String[] getCommands(int port, String service, String method, String pattern, boolean reset) {
        return new String[] {
                "sh", "-c",
                String.format("/usr/bin/v2ray/v2ctl api --server=127.0.0.1:%d %s.%s 'pattern: \"%s\" reset: %b'", port, service, method, pattern, reset)
        };
    }

    public static Map<String, InboundTraffic> getInboundTraffics(boolean reset) throws IOException, InterruptedException {
        int port = Config.apiPort();
        if (port <= 0) {
            throw new RuntimeException("错误：未开启 v2ray api");
        }
        String[] commands = getCommands(port, "StatsService", "QueryStats", "", reset);
        String result = ExecUtil.execForResult(10, TimeUnit.SECONDS, commands);
        Matcher matcher = inboundPattern.matcher(result);
        Map<String, InboundTraffic> inboundTrafficMap = new HashMap<>();
        while (matcher.find()) {
            String tag = matcher.group("tag");
            tag = toUTF_8(tag);
            String type = matcher.group("type");
            String valueStr = matcher.group("value");
            long value;
            if (StringUtils.isEmpty(valueStr)) {
                value = 0;
            } else {
                value = Long.parseLong(valueStr);
            }
            InboundTraffic inboundTraffic = inboundTrafficMap.get(tag);
            if (inboundTraffic == null) {
                inboundTraffic = new InboundTraffic();
                inboundTraffic.setTag(tag);
                inboundTrafficMap.put(tag, inboundTraffic);
            }
            if ("up".equals(type)) {
                inboundTraffic.setUplink(value);
            } else {
                inboundTraffic.setDownlink(value);
            }
        }
        return inboundTrafficMap;
    }

    private static String toUTF_8(String octStr) throws IOException {
        Matcher matcher = octPattern.matcher(octStr);
        if (matcher.find()) {
            char[] chArr = octStr.toCharArray();
            int i = 0;
            int length = octStr.length();
            StringBuilder builder = new StringBuilder();
            do {
                int start = matcher.start();
                if (i < length) {
                    if (i != start) {
                        builder.append(chArr, i, start - i);
                    }
                }
                int a = Integer.parseInt(matcher.group(1), 8);
                int b = -1, c = -1;
                if (matcher.find()) {
                    b = Integer.parseInt(matcher.group(1), 8);
                }
                if (matcher.find()) {
                    c = Integer.parseInt(matcher.group(1), 8);
                }
                if (b == -1 || c == -1) {
                    throw new IOException("tag 格式错误：" + octStr);
                }
                builder.append(new String(new byte[]{(byte) a, (byte) b, (byte) c}));
                i = matcher.end();
            } while (matcher.find());

            if (i < length) {
                builder.append(chArr, i, length - i);
            }
            return builder.toString();
        } else {
            return octStr;
        }
    }

}
