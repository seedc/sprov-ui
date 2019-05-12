package xyz.sprov.blog.sprovui.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import spark.utils.StringUtils;
import xyz.sprov.blog.sprovui.exception.V2rayConfigException;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.Context;
import xyz.sprov.blog.sprovui.venum.Protocol;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;

//@Service
public class V2rayConfigService {

    private V2rayService v2rayService = Context.v2rayService;

    private ThreadService threadService = Context.threadService;

//    @Value("${v2ray.config-location}")
    private String configLocation = "/etc/v2ray/config.json";

    private final Pattern uuidPattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public V2rayConfigService() {
        try {
            openV2rayApi();
        } catch (Exception e) {
            System.err.println("开启 v2ray api 失败：" + e.getMessage());
            System.exit(-1);
        }
    }

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
    private void writeConfig(JSONObject config) throws IOException {
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

    public JSONObject getInbound(int port) throws IOException {
        JSONArray inbounds = getInbounds(getConfig());
        for (Object obj : inbounds) {
            JSONObject inbound = (JSONObject) obj;
            int p = inbound.getIntValue("port");
            if (p == port) {
                return inbound;
            }
        }
        return null;
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
     *
     * @param renameTag 是否自动生成 tag
     */
    public void addInbound(JSONObject inbound, boolean renameTag) throws IOException {
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
        String tag = (String) inbound.getOrDefault("tag", "");
        if (Protocol.MT_PROTO.getValue().equals(protocol)) {
            if (renameTag || StringUtils.isEmpty(tag)) {
                tag = "tg-in-" + port;
                inbound.put("tag", tag);
            }
            addMTOutboundAndRoute(config, tag);
        } else if (renameTag) {
            inbound.put("tag", "inbound-" + port);
        }
        JSONArray inbounds = getInbounds(config);
        for (Object inb : inbounds) {
            JSONObject in = (JSONObject) inb;
            if (in.getIntValue("port") == port) {
                throw new V2rayConfigException("端口 " + port + " 已使用，请填写其它端口");
            }
        }
        inbounds.add(inbound);
        writeConfig(config);
    }

    /**
     * 添加一个入站协议
     */
    public void addInbound(JSONObject inbound) throws IOException {
        addInbound(inbound, true);
    }

    private void addMTOutboundAndRoute(JSONObject config, String tag) {
        addMTOutbound(config);
        addOrDelMTRoute(config, tag, "add");
    }

    private void addMTOutbound(JSONObject config) {
        JSONArray outbounds = config.getJSONArray("outbounds");
        if (outbounds == null) {
            outbounds = JSONArray.parseArray("[{'protocol': 'freedom','settings': {}}]");
            config.put("outbounds", outbounds);
        }
        for (Object obj : outbounds) {
            JSONObject outbound = (JSONObject) obj;
            if ("tg-out".equals(outbound.getString("tag"))) {
                return;
            }
        }
        JSONObject mtOutbound = JSONObject.parseObject("{'tag': 'tg-out', 'protocol': 'mtproto', 'settings': {}}");
        outbounds.add(mtOutbound);
    }

    private void addOrDelMTRoute(JSONObject config, String tag, String action) {
        JSONObject routing = getRouting(config);
        JSONArray rules = routing.getJSONArray("rules");
        JSONObject tgRule = null;
        for (Object obj : rules) {
            JSONObject rule = (JSONObject) obj;
            if ("tg-out".equals(rule.getString("outboundTag"))) {
                tgRule = rule;
                break;
            }
        }
        if (tgRule == null) {
            tgRule = JSONObject.parseObject("{'type': 'field', 'inboundTag': [], 'outboundTag': 'tg-out'}");
            rules.add(tgRule);
        }
        JSONArray inboundTag = tgRule.getJSONArray("inboundTag");
        if (inboundTag == null) {
            inboundTag = JSONArray.parseArray("[]");
            tgRule.put("inboundTag", inboundTag);
        }
        if ("add".equals(action)) {
            inboundTag.add(tag);
        } else if ("del".equals(action)) {
            inboundTag.remove(tag);
            if (inboundTag.size() == 0) {
                rules.removeIf(obj -> {
                    JSONObject rule = (JSONObject) obj;
                    return "tg-out".equals(rule.getString("outboundTag"));
                });
            }
        }
    }

    /**
     * 修改一个 inbound 协议
     */
    public void editInbound(JSONObject inbound) throws IOException {
        Integer oldPort = inbound.getInteger("oldPort");
        int newPort = inbound.getIntValue("port");
        if (oldPort == null) {
            oldPort = newPort;
        }
        JSONObject config = getConfig();
        JSONArray inbounds = getInbounds(config);
        if (oldPort != newPort) {
            for (Object inb : inbounds) {
                JSONObject in = (JSONObject) inb;
                if (in.getIntValue("port") == newPort) {
                    throw new V2rayConfigException("端口 " + newPort + " 已使用，请填写其它端口");
                }
            }
        }
        inbound.remove("oldPort");
        for (Object inb : inbounds) {
            JSONObject in = (JSONObject) inb;
            if (in.getIntValue("port") == oldPort) {
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
        boolean removed = inbounds.removeIf(obj -> {
            JSONObject in = (JSONObject) obj;
            if (in.getIntValue("port") == port) {
                if (Protocol.MT_PROTO.getValue().equals(in.getString("protocol"))) {
                    addOrDelMTRoute(config, in.getString("tag"), "del");
                }
                return true;
            }
            return false;
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

    public void openV2rayApi() throws IOException {
        JSONObject config = getConfig();
        config.put("api", JSONObject.parseObject("{" +
                "    'tag': 'api'," +
                "    'services': [" +
                "      'HandlerService'," +
                "      'LoggerService'," +
                "      'StatsService'" +
                "    ]" +
                "  }"));
        config.put("stats", new JSONObject());
        JSONObject policy = getPolicy(config);
        JSONObject system = policy.getJSONObject("system");
        if (system == null) {
            system = new JSONObject();
            policy.put("system", system);
        }
        system.put("statsInboundUplink", true);
        system.put("statsInboundDownlink", true);
        JSONArray inbounds = getInbounds(config);
        int apiPort;
        Random random = new Random();
        do {
            apiPort = random.nextInt(50000) + 10000;
        } while (containsPort(inbounds, apiPort));
        removeTag(inbounds, "api");
        inbounds.add(JSONObject.parseObject("{" +
                "    'listen': '127.0.0.1'," +
                "    'port': " + apiPort + "," +
                "    'protocol': 'dokodemo-door'," +
                "    'settings': {" +
                "      'address': '127.0.0.1'" +
                "    }," +
                "    'tag': 'api'" +
                "  }"));
        Config.setApiPort(apiPort);
        JSONObject routing = getRouting(config);
        JSONArray rules = routing.getJSONArray("rules");
        if (rules == null) {
            rules = new JSONArray();
            routing.put("rules", rules);
        }
        rules.removeIf(o -> {
            JSONObject rule = (JSONObject) o;
            return "api".equals(rule.getString("outboundTag"));
        });
        rules.add(0, JSONObject.parseObject("{" +
                "      'type': 'field'," +
                "      'inboundTag': ['api']," +
                "      'outboundTag': 'api'" +
                "    }"));
        writeConfig(config);

        threadService.schedule(() -> {
            try {
                v2rayService.restart();
            } catch (Exception e) {
                System.err.println("v2ray 重启失败：" + e.getMessage());
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void removeTag(JSONArray inbounds, String tag) {
        inbounds.removeIf(o -> {
            JSONObject inbound = (JSONObject) o;
            return tag.equals(inbound.getString("tag"));
        });
    }

    private JSONObject getRouting(JSONObject config) {
        JSONObject routing = config.getJSONObject("routing");
        if (routing == null) {
            routing = JSONObject.parseObject("{" +
                    "    'domainStrategy': 'IPIfNonMatch'," +
                    "    'rules': []" +
                    "  }");
            config.put("routing", routing);
        }
        return routing;
    }

    private JSONObject getPolicy(JSONObject config) {
        JSONObject policy = config.getJSONObject("policy");
        if (policy == null) {
            policy = new JSONObject();
            config.put("policy", policy);
        }
        return policy;
    }

    private boolean containsPort(JSONArray inbounds, int port) {
        for (Object obj : inbounds) {
            JSONObject inbound = (JSONObject) obj;
            if (port == inbound.getIntValue("port")) {
                return true;
            }
        }
        return false;
    }

}
