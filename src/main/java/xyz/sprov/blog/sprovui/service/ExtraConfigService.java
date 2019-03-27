package xyz.sprov.blog.sprovui.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExtraConfigService {

    private String configPath = "/etc/sprov-ui/v2ray-extra-config.json";

    private JSONObject config;

    public ExtraConfigService() {
        File file = new File(configPath);
        if (!file.exists() || !file.isFile()) {
            try {
                FileUtils.deleteQuietly(file);
                config = JSONObject.parseObject("{'inbounds': []}");
                writeConfig(config);
            } catch (Exception e) {
                System.err.println("创建配置文件 /etc/sprov-ui/v2ray-extra-config.json 失败：" + e.getMessage());
                System.exit(-1);
            }
        } else {
            try {
                config = readConfig();
            } catch (Exception e) {
                System.err.println("读取配置文件 /etc/sprov-ui/v2ray-extra-config.json 失败：" + e.getMessage());
                System.exit(-1);
            }
        }
    }

    private JSONObject readConfig() throws IOException {
        return JSONObject.parseObject(FileUtils.readFileToString(new File(configPath), "UTF-8"));
    }

    private void writeConfig(JSONObject config) throws IOException {
        String str = JSON.toJSONString(config, true);
        FileUtils.write(new File(configPath), str, "UTF-8");
    }

    public JSONArray getInbounds() {
        return config.getJSONArray("inbounds");
    }

    public Map<Integer, JSONObject> getPortsTraffic() {
        JSONArray inbounds = getInbounds();
        Map<Integer, JSONObject> map = new HashMap<>();
        for (Object in : inbounds) {
            JSONObject inbound = (JSONObject) in;
            Integer port = inbound.getInteger("port");
            if (port != null) {
                map.put(port, inbound);
            }
        }
        return map;
    }

}
