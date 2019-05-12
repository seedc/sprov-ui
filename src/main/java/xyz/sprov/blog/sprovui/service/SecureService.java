package xyz.sprov.blog.sprovui.service;

import spark.Request;
import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SecureService {

    private final Map<String, Integer> wrongPassCountMap = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private int maxWrongPassCount = Config.maxWrongPassCount();

    public SecureService() {
        Context.threadService.scheduleAtFixedRate(new SecurityJob(), 29, 29, TimeUnit.MINUTES);
    }

    public boolean isBlackList(Request request) {
        String ip = request.ip();
        int count = getCount(ip);
        putCount(ip, count + 1);
        return count >= maxWrongPassCount;
    }

    private int getCount(String ip) {
        Integer count;
        try {
            readLock.lock();
            count = wrongPassCountMap.get(ip);
        } finally {
            readLock.unlock();
        }
        if (count == null) {
            return 0;
        }
        return count;
    }

    private void putCount(String ip, int count) {
        try {
            writeLock.lock();
            wrongPassCountMap.put(ip, count);
        } finally {
            writeLock.unlock();
        }
    }

    private class SecurityJob implements Runnable {

        @Override
        public void run() {
            try {
                writeLock.lock();
                Iterator<Map.Entry<String, Integer>> iterator = wrongPassCountMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Integer> entry = iterator.next();
                    int count = entry.getValue();
                    if (count == 1) {
                        iterator.remove();
                    } else {
                        entry.setValue(count - 1);
                    }
                }
            } finally {
                writeLock.unlock();
            }
            System.gc();
        }
    }

}
