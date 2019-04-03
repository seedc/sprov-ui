package xyz.sprov.blog.sprovui.util;

import xyz.sprov.blog.sprovui.entity.Exec;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ExecUtil {

    private ExecUtil() {}

    public static String execForResult(String ... commands) throws IOException, InterruptedException {
        Exec exec = new Exec(commands);
        return exec.waitForResult();
    }

    public static String execForResult(String command) throws IOException, InterruptedException {
        Exec exec = new Exec(command);
        return exec.waitForResult();
    }

    public static String execForResult(String command, long timeout, TimeUnit timeUnit) throws IOException, InterruptedException {
        Exec exec = new Exec(command);
        return exec.waitForResult(timeout, timeUnit);
    }

    public static String execForResult(long timeout, TimeUnit timeUnit, String... commands) throws IOException, InterruptedException {
        Exec exec = new Exec(commands);
        return exec.waitForResult(timeout, timeUnit);
    }

    public static int execForStatus(String command) throws IOException, InterruptedException {
        Exec exec = new Exec(command);
        return exec.waitFor();
    }

    public static int execForStatus(String command, long timeout, TimeUnit timeUnit) throws IOException, InterruptedException {
        Exec exec = new Exec(command);
        if (exec.waitFor(timeout, timeUnit)) {
            return exec.getExitValue();
        }
        throw new IOException("wait timeout：" + command);
    }

}
