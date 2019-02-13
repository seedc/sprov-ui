package xyz.sprov.blog.sprovui.bean;

public class Status {

    private String name;
    private String value;
    private String tag = "";
    private String color = "";

    public Status(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Status(String name, String value, String tag, String color) {
        this.name = name;
        this.value = value;
        this.tag = tag;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
