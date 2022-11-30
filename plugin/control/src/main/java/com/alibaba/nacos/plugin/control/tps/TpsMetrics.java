package com.alibaba.nacos.plugin.control.tps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TpsMetrics {
    
    private String pointName;
    
    private String type;
    
    private long timeStamp;
    
    private TimeUnit period;
    
    private Counter counter;
    
    public TpsMetrics(String pointName, String type, long timeStamp, TimeUnit period) {
        this.pointName = pointName;
        this.type = type;
        this.timeStamp = timeStamp;
        this.period = period;
        
    }
    
    @Override
    public String toString() {
        return "TpsMetrics{" + "pointName='" + pointName + '\'' + ", type='" + type + '\'' + ", timeStamp=" + timeStamp
                + ", period=" + period + ", counter=" + counter + '}';
    }
    
    public String getTimeFormatOfSecond(long timeStamp) {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeStamp));
        return format;
    }
    
    public String getMsg() {
        
        return String.join("|", pointName, type, period.name(), getTimeFormatOfSecond(timeStamp),
                String.valueOf(counter.passCount), String.valueOf(counter.deniedCount));
    }
    
    public String getPointName() {
        return pointName;
    }
    
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    public TimeUnit getPeriod() {
        return period;
    }
    
    public void setPeriod(TimeUnit period) {
        this.period = period;
    }
    
    public Counter getCounter() {
        return counter;
    }
    
    public void setCounter(Counter counter) {
        this.counter = counter;
    }
    
    public static class Counter {
        
        private long passCount;
        
        private long deniedCount;
        
        public Counter(long passCount, long deniedCount) {
            this.passCount = passCount;
            this.deniedCount = deniedCount;
        }
        
        public long getPassCount() {
            return passCount;
        }
        
        public void setPassCount(long passCount) {
            this.passCount = passCount;
        }
        
        public long getDeniedCount() {
            return deniedCount;
        }
        
        public void setDeniedCount(long deniedCount) {
            this.deniedCount = deniedCount;
        }
        
        @Override
        public String toString() {
            return "{" + "passCount=" + passCount + ", deniedCount=" + deniedCount + '}';
        }
        
        public String getSimpleLog() {
            return String.join("|", String.valueOf(passCount), String.valueOf(deniedCount));
        }
    }
}
