package xyz.sprov.blog.sprovui.entity;

import org.apache.commons.io.IOUtils;
import spark.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class Exec {

    private String command;

    private String[] commands;

    private Process process;

    public Exec(String command) throws IOException {
        if (StringUtils.isBlank(command)) {
            throw new IllegalArgumentException("command can not be blank");
        }
        this.command = command;
        process = Runtime.getRuntime().exec(command);
    }

    public Exec(String ... commands) throws IOException {
        if (commands.length == 0) {
            throw new IllegalArgumentException("command can not be blank");
        }
        this.commands = commands;
        process = Runtime.getRuntime().exec(commands);
    }

    public boolean isStarted() {
        return process != null;
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    public boolean isDestroy() {
        return process != null && !process.isAlive();
    }

    public InputStream getInputStream() {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        return process.getInputStream();
    }

    public String getResult() throws IOException {
        InputStream in = getInputStream();
        return IOUtils.toString(in);
    }

    public String waitForResult() throws InterruptedException, IOException {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        process.waitFor();
        InputStream in = getInputStream();
        return IOUtils.toString(in);
    }

    public String waitForResult(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        if (process.waitFor(timeout, timeUnit)) {
            InputStream in = getInputStream();
            return IOUtils.toString(in);
        }
        throw new IOException("wait timeout");
    }

    public int getExitValue() {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        return process.exitValue();
    }

    public int waitFor() throws InterruptedException {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        return process.waitFor();
    }

    public boolean waitFor(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (!isStarted()) {
            throw new RuntimeException("command has not started");
        }
        return process.waitFor(timeout, timeUnit);
    }

    public void destroy() {
        process.destroy();
    }

}
