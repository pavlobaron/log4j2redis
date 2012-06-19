package test.org.pbit.log4j2redis;

import org.apache.log4j.Logger;

public class Log4j2RedisTest {
    
    public static class LogThread extends Thread {
        Logger log = Logger.getLogger("LogThread");
        
        public void run() {
            for (long i = 0; i < 10000; i++) {
                log.warn("whatever " + i);
            }
        }
    }
    
    static Logger log = Logger.getLogger("LogMainThread");
    
    public static void main(String[] args) throws Exception {
        Log4j2RedisTest.LogThread t = new Log4j2RedisTest.LogThread();
        t.start();
        
        for (long i = 0; i < 10000; i++) {
            log.error("that's me " + i);
        }
    }
}
