package xyz.sprov.blog.sprovui.bean;

public class InboundTraffic {

    private String tag;
    private long downlink;
    private long uplink;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getDownlink() {
        return downlink;
    }

    public void setDownlink(long downlink) {
        this.downlink = downlink;
    }

    public long getUplink() {
        return uplink;
    }

    public void setUplink(long uplink) {
        this.uplink = uplink;
    }

    @Override
    public String toString() {
        return "InboundTraffic{" +
                "tag='" + tag + '\'' +
                ", downlink=" + downlink +
                ", uplink=" + uplink +
                '}';
    }
}
