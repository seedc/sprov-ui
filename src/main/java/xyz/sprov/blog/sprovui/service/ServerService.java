package xyz.sprov.blog.sprovui.service;

import org.apache.commons.io.FileUtils;
import xyz.sprov.blog.sprovui.bean.Status;
import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.util.Context;
import xyz.sprov.blog.sprovui.util.DateUtil;
import xyz.sprov.blog.sprovui.util.ExecUtil;
import xyz.sprov.blog.sprovui.util.FileUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;

//@Service
public class ServerService {

    private long lastAccess = System.currentTimeMillis();

//    @Autowired
    private V2rayService v2rayService = Context.v2rayService;

    private GetSystemInfoThread thread = new GetSystemInfoThread();

    public ServerService() {
        Context.threadService.scheduleAtFixedRate(thread, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 获取系统所有状态
     */
    public Collection<Status> statuses(HttpServletRequest request) throws V2rayException {
        lastAccess = System.currentTimeMillis();
        List<Status> statuses = new ArrayList<>(9);
        statuses.add(v2rayStatus());
        statuses.add(uptime());
        statuses.add(ip(request));
        statuses.add(cpu());
        statuses.add(mem());
        statuses.add(disk());
        statuses.add(loads());
        statuses.add(netSpeed());
        statuses.add(traffic());
        return statuses;
    }

    public Status v2rayStatus() {
        int status = thread.v2rayStatus;
        String value, color;
        switch (status) {
            case 0:
                value = "运行中";
                color = "success";
                break;
            case 1:
                value = "未运行";
                color = "info";
                break;
            case 2:
                value = "未安装";
                color = "danger";
                break;
            default:
                value = "获取失败";
                color = "warning";
        }
        return new Status("v2rayStatus", value, "tag", color);
    }

    public Status uptime() {
        return new Status("uptime", DateUtil.formatSecond(thread.uptime));
    }

    public Status ip(HttpServletRequest request) {
        return new Status("ip", request.getServerName());
    }

    public Status cpu() {
        double rate = thread.cpuRate;
        String color = "#67C23A";
        if (rate >= 90) {
            color = "#F56C6C";
        } else if (rate >= 80) {
            color = "#E6A23C";
        }
        return new Status("cpu", String.format("%.2f", rate), "progress", color);
    }

    public Status mem() {
        double rate = thread.memRate;
        String color = "#67C23A";
        if (rate >= 90) {
            color = "#F56C6C";
        } else if (rate >= 80) {
            color = "#E6A23C";
        }
        return new Status("mem", String.format("%.2f", rate), "progress", color);
    }

    public Status disk() {
        double rate = thread.hardDiskRate;
        String color = "#67C23A";
        if (rate >= 90) {
            color = "#F56C6C";
        } else if (rate >= 80) {
            color = "#E6A23C";
        }
        return new Status("hardDisk", String.format("%.2f", rate), "progress", color);
    }

    public Status loads() {
        double[] loads = thread.loads;
        return new Status("loads", String.format("%.2f | %.2f | %.2f", loads[0], loads[1], loads[2]));
    }

    public Status netSpeed() {
        long[] netSpeed = thread.netSpeed;
        return new Status("netSpeed",
                FileUtil.formatSize(netSpeed[0]) + "/S | " + FileUtil.formatSize(netSpeed[1]) + "/S");
    }

    public Status traffic() {
        long[] traffic = thread.lastTraffic;
        return new Status("traffic",
                FileUtil.formatSize(traffic[0]) + " | " + FileUtil.formatSize(traffic[1]));
    }

    private class GetSystemInfoThread implements Runnable {

        private Pattern numPattern = Pattern.compile("\\d+");

        private Pattern uptimePattern = Pattern.compile("load average:\\s+(?<load1>\\d+(\\.\\d+)?),\\s+(?<load5>\\d+(\\.\\d+)?),\\s+(?<load15>\\d+(\\.\\d+)?)");

        /**
         * [空闲时间，总时间]
         */
        private long[] lastCpuTime = new long[2];

        /**
         * [上传流量，下载流量，记录时间（毫秒）]（字节）
         */
        private long[] lastTraffic = new long[3];

        /**
         * v2ray状态
         * 0：运行
         * 1：未运行
         * 2：未安装
         */
        private int v2rayStatus = -1;

        /**
         * 系统已运行时间（秒）
         */
        private long uptime;

        /**
         * 实时CPU使用率（%）
         */
        private double cpuRate;

        /**
         * 实时内存使用率（%）
         */
        private double memRate;

        /**
         * 磁盘使用率（%）
         */
        private double hardDiskRate;

        /**
         * 系统负载，[1分钟，5分钟，15分钟]
         */
        private double[] loads = new double[3];

        /**
         * 实时网速，[上传速度，下载速度]（字节/秒）
         */
        private long[] netSpeed = new long[2];

        /**
         * 获取v2ray运行状态
         */
        public void v2rayStatus() throws IOException, InterruptedException {
            v2rayStatus = v2rayService.status();
        }

        /**
         * 获取系统运行时间
         */
        public void uptime() throws IOException {
            String str = FileUtils.readFileToString(new File("/proc/uptime"), "UTF-8");
            uptime = (long) Double.parseDouble(str.split(" ")[0]);
        }

        /**
         * 当前CPU时间
         *
         * [空闲时间，总时间]
         */
        private long[] getCpuTime() throws IOException {
            String line;
            try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
                line = reader.readLine();
            }
            long total = 0;
            long idle = 0;
            String[] numbers = line.split("\\s+");
            for (int i = 1; i < numbers.length; ++i) {
                long number = Long.parseLong(numbers[i]);
                total += number;
                if (i == 4) {
                    idle = number;
                }
            }
            return new long[]{ idle, total };
        }

        /**
         * 计算cpu使用率
         */
        public void computeCpuRate() throws IOException {
            long[] cpuTime = getCpuTime();
            long total = cpuTime[1] - lastCpuTime[1];
            long idle = cpuTime[0] - lastCpuTime[0];
            lastCpuTime = cpuTime;
            cpuRate = 100.0 * (total - idle) / total;
        }

        /**
         * 计算内存使用率
         */
        public void computeMemory() throws IOException {
            long total = -1;
            long free = -1;
            long buffers = -1;
            long cached = -1;
            try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (total != -1 && free != -1 && buffers != -1 && cached != -1) {
                        break;
                    }
                    line = line.toLowerCase();
                    if (line.startsWith("memtotal:")) {
                        total = getNumber(line);
                    } else if (line.startsWith("memfree:")) {
                        free = getNumber(line);
                    } else if (line.startsWith("buffers:")) {
                        buffers = getNumber(line);
                    } else if (line.startsWith("cached:")) {
                        cached = getNumber(line);
                    }
                }
            }
            long used = total - free - buffers - cached;
            memRate = 100.0 * used / total;
        }

        /**
         * 计算硬盘使用率
         */
        public void computeHardDisk() {
            File file = new File("/");
            long total = file.getTotalSpace();
            long free = file.getFreeSpace();
            hardDiskRate = 100.0 * (total - free) / total;
        }

        /**
         * 获取CPU负载
         */
        public void loads() throws IOException, InterruptedException {
            String str = ExecUtil.execForResult("uptime");
            Matcher matcher = uptimePattern.matcher(str);
            if (matcher.find()) {
                loads[0] = Double.parseDouble(matcher.group("load1"));
                loads[1] = Double.parseDouble(matcher.group("load5"));
                loads[2] = Double.parseDouble(matcher.group("load15"));
            }
        }

        /**
         * 获取流量（字节）
         *
         * [总上传流量，总下载流量]
         */
        private long[] getTraffic() throws IOException {
            List<String> lines = FileUtils.readLines(new File("/proc/net/dev"), "UTF-8");
            long up = 0;
            long down = 0;
            for (String line : lines) {
                line = line.trim();
                if ((line.startsWith("eth")
                        || line.startsWith("en")
                        || line.startsWith("wlan")
                        || line.startsWith("venet"))
                        && line.contains(":")) {
                    List<Long> numbers = getNumbers(line.substring(line.indexOf(':')));
                    down += numbers.get(0);
                    up += numbers.get(8);
                }
            }
            return new long[]{ up, down, System.currentTimeMillis() };
        }

        /**
         * 计算网速
         */
        public void networkSpeed() throws IOException {
            long[] traffic = getTraffic();
            long time = traffic[2] - lastTraffic[2];
            long up = traffic[0] - lastTraffic[0];
            long down = traffic[1] - lastTraffic[1];
            netSpeed[0] = (long) (1.0 * up / time * 1000);
            netSpeed[1] = (long) (1.0 * down / time * 1000);
            lastTraffic = traffic;
        }

        private long getNumber(String str) {
            return getNumber(str, -1);
        }

        private long getNumber(String str, long defaultValue) {
            Matcher matcher = numPattern.matcher(str);
            if (matcher.find()) {
                return Long.parseLong(matcher.group());
            }
            return defaultValue;
        }

        private List<Long> getNumbers(String str) {
            List<Long> numbers = new ArrayList<>();
            Matcher matcher = numPattern.matcher(str);
            while (matcher.find()) {
                numbers.add(Long.valueOf(matcher.group()));
            }
            return numbers;
        }

        @Override
        public void run() {
            long curTime = System.currentTimeMillis();
            if (curTime - lastAccess > 60000) {
                return;
            }
            try {
                v2rayStatus();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                uptime();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                computeCpuRate();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                computeMemory();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                computeHardDisk();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                loads();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
            try {
                networkSpeed();
            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(e.getMessage());
            }
        }
    }

}
