package xyz.sprov.blog.sprovui.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadService extends ScheduledThreadPoolExecutor {

    public ThreadService() {
        super(1);
    }

}
