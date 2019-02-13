package xyz.sprov.blog.sprovui.exception;

public class V2rayException extends RuntimeException {

    private Object object;

    public V2rayException() {}

    public V2rayException(String msg) {
        super(msg);
    }

    public V2rayException(String msg, Object object) {
        super(msg);
        this.object = object;
    }

    public V2rayException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public V2rayException(Throwable cause) {
        super(cause);
    }

    public V2rayException(Throwable cause, Object object) {
        super(cause);
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
