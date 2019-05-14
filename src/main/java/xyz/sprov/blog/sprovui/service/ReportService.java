package xyz.sprov.blog.sprovui.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.Context;
import xyz.sprov.blog.sprovui.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReportService {

    private static final String REPORT_URL = "https://blog.sprov.xyz/sprov-ui/report";

    private V2rayConfigService v2rayConfigService = Context.v2rayConfigService;

    public ReportService() {
        Context.threadService.scheduleAtFixedRate(new ReportThread(), 30, 30, TimeUnit.MINUTES);
    }

    private class ReportThread implements Runnable {

        @Override
        public void run() {
            try {
                JSONObject config = v2rayConfigService.getConfig();
                JSONArray inbounds = config.getJSONArray("inbounds");
                Map<String, Object> map = new HashMap<>();
                for (Object obj : inbounds) {
                    JSONObject inbound = (JSONObject) obj;
                    String protocol = inbound.getString("protocol");
                    if (StringUtils.isEmpty(protocol)) {
                        continue;
                    }
                    int n = (Integer) map.getOrDefault(protocol, 0);
                    map.put(protocol, n + 1);
                    map.put("version", Config.currentVersion());
                }
                HttpUtil.post(REPORT_URL, map);
            } catch (Exception ignore) {}
        }
    }

}
