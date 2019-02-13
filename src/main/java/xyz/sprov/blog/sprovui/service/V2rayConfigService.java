package xyz.sprov.blog.sprovui.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.sprov.blog.sprovui.venum.Protocol;
import xyz.sprov.blog.sprovui.exception.V2rayConfigException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class V2rayConfigService {

    @Value("${v2ray.config-location}")
    private String configLocation;

    private final Pattern uuidPattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /**
     * 获取v2ray的配置文件内容，以字符串形式
     */
    public String config() throws IOException {
        return FileUtils.readFileToString(new File(configLocation), "UTF-8");
    }

    /**
     * 获取v2ray的配置文件内容，以JSON形式
     */
    public JSONObject getConfig() throws IOException {
        return JSONObject.parseObject(config());
    }

    /**
     * 将内容覆盖写入v2ray配置文件中
     */
    public void writeConfig(JSONObject config) throws IOException {
        String jsonStr = JSON.toJSONString(config, true);
        FileUtils.write(new File(configLocation), jsonStr, "UTF-8");
    }

    /**
     * 获取 inbounds json
     */
    private JSONArray getInbounds(JSONObject config) {
        JSONArray inbounds = config.getJSONArray("inbounds");
        if (inbounds == null) {
            inbounds = new JSONArray();
            config.put("inbounds", inbounds);
        }
        return inbounds;
    }

    private JSONArray getVmessUsers(JSONObject inbound) {
        String protocol = inbound.getString("protocol");
        if (!Protocol.VMESS.getValue().equals(protocol)) {
            return null;
        }
        JSONObject settings = inbound.getJSONObject("settings");
        if (settings == null) {
            settings = new JSONObject();
            inbound.put("settings", settings);
        }
        JSONArray clients = settings.getJSONArray("clients");
        if (clients == null) {
            clients = new JSONArray();
            settings.put("clients", clients);
        }
        return clients;
    }

    private void checkPort(Integer port) {
        if (port == null) {
            throw new V2rayConfigException("必须要填端口");
        } else if (port < 1 || port > 65535) {
            throw new V2rayConfigException("端口必须介于1-65535之间");
        }
    }

    private void checkUUID(String uuid) {
        if (!uuidPattern.matcher(uuid).matches()) {
            throw new V2rayConfigException("用户id必须是一个合法的UUID，请使用工具生成");
        }
    }

    /**
     * 添加一个入站协议
     */
    public void addInbound(JSONObject inbound) throws IOException {
        if (inbound == null) {
            throw new V2rayConfigException("配置不能为空");
        }
        Integer port = inbound.getInteger("port");
        checkPort(port);
        String protocol = inbound.getString("protocol");
        if (!Protocol.checkProtocol(protocol)) {
            throw new V2rayConfigException("协议填写错误");
        }
        JSONObject config = getConfig();
        JSONArray inbounds = getInbounds(config);
        for (Object inb : inbounds) {
            JSONObject in = (JSONObject) inb;
            if (in.getIntValue("port") == port) {
                throw new V2rayConfigException("端口已使用，请填写其它端口");
            }
        }
        inbounds.add(inbound);
        writeConfig(config);
    }

    /**
     * 修改一个 inbound 协议
     */
    public void editInbound(JSONObject inbound) throws IOException {
        int port = inbound.getIntValue("port");
        JSONObject config = getConfig();
        JSONArray inbounds = getInbounds(config);
        for (Object inb : inbounds) {
            JSONObject in = (JSONObject) inb;
            if (in.getIntValue("port") == port) {
                for (Map.Entry<String, Object> entry : inbound.entrySet()) {
                    in.put(entry.getKey(), entry.getValue());
                }
                writeConfig(config);
                return;
            }
        }
        throw new V2rayConfigException("没有此端口");
    }

    /**
     * 删除一个 inbound 协议
     */
    public void delInbound(int port) throws IOException {
        JSONObject config = getConfig();
        JSONArray inbounds = getInbounds(config);
        boolean removed = inbounds.removeIf(inb -> {
            JSONObject in = (JSONObject) inb;
            return in.getIntValue("port") == port;
        });
        if (removed) {
            writeConfig(config);
        }
    }

    /**
     * 添加一个vmess用户（单端口多用户）
     * {
     *     "id": "UUID",
     *     "alterId": xx
     * }
     */
    public void addVmessUser(JSONObject client) throws IOException {
        Integer port = client.getInteger("port");
        checkPort(port);
        String uuid = client.getString("id");
        checkUUID(uuid);
        JSONObject config = getConfig();
        JSONArray inbounds = getInbounds(config);
        for (Object inb : inbounds) {
            JSONObject in = (JSONObject) inb;
            int p = in.getIntValue("port");
            if (p == port) {
                JSONArray clients = getVmessUsers(in);
                if (clients == null) {
                    throw new V2rayConfigException("此端口使用的不是vmess协议");
                }
                for (Object c : clients) {
                    JSONObject cl = (JSONObject) c;
                    String id = cl.getString("id");
                    if (id.equals(uuid)) {
                        throw new V2rayConfigException("此用户id已在使用，请重新生成");
                    }
                }
                clients.add(client);
                writeConfig(config);
                return;
            }
        }

        //如果找不到相同的端口就新添加一个
        JSONObject inbound = new JSONObject();
        inbound.put("port", port);
        inbound.put("protocol", Protocol.VMESS.getValue());
        JSONObject settings = new JSONObject();
        JSONArray clients = new JSONArray();
        clients.add(client);
        settings.put("clients", clients);
        inbound.put("settings", settings);
        addInbound(inbound);
    }

}
