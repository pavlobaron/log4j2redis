package org.pbit.log4j2redis;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

public class RedisAppender extends AppenderSkeleton {
    private Jedis jedis;
    private String host = "localhost";
    private int port = 6379;

    private Map<String, String> messages;
    private String localHostName;
    private String processName;
    private int MSetMax = 100;

    public void activateOptions() {
        super.activateOptions();

        jedis = new Jedis(host, port);
        messages = new ConcurrentHashMap<String, String>();
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            localHostName = "localhost";
        }

        processName = ManagementFactory.getRuntimeMXBean().getName();

        new Timer().schedule(new TimerTask() {
            public void run() {
                // long begin = System.nanoTime();

                Entry<String, String> message;

                int i = 0;
                int msz = messages.size();
                int sz = msz < MSetMax ? msz : MSetMax;

                byte[][] kv = new byte[sz * 2][];
                for (Iterator<Entry<String, String>> it = messages.entrySet().iterator(); it.hasNext();) {
                    message = it.next();
                    kv[i] = SafeEncoder.encode(message.getKey());
                    kv[i + 1] = SafeEncoder.encode(message.getValue());
                    i += 2;

                    it.remove();

                    if (i == sz * 2) {
                        jedis.mset(kv);
                        msz -= sz;
                        if (msz > 0) {
                            sz = msz < MSetMax ? msz : MSetMax;
                            kv = new byte[sz * 2][];
                            i = 0;
                        }
                    }
                }

                // long expendHere = System.nanoTime() - begin;
                // System.out.println("Expend here: " + expendHere + " ns");
            }
        }, 1000, 1000);

        /*
         left here for benchmark comparison, should be deleted when it's proven that the
         version above indeed is x1.5 or even 2.0 faster
         
         new Timer().schedule(new TimerTask() {
            public void run() {
                long begin = System.nanoTime();
                
                Entry<String, String> message;
        
                for (Iterator<Entry<String, String>> it = messages.entrySet().iterator(); it.hasNext();) {
                    message = it.next();
                    jedis.set(message.getKey(), message.getValue());
                    it.remove();
                }
                
                long expendHere = System.nanoTime() - begin;
                
                System.out.println("Expend here: " + expendHere + " ns");
            }
        }, 5000, 5000);
        */
    }

    protected void append(LoggingEvent event) {
        try {
            StringBuilder id = new StringBuilder(localHostName);
            id.append(" - ");
            id.append(processName);
            id.append(" - ");
            id.append(Thread.currentThread().getId());
            id.append(" - ");
            id.append(now());
            id.append(" - ");
            id.append(event.getLevel());
            id.append(" - ");
            id.append(UUID.randomUUID());

            messages.put(id.toString(), event.getRenderedMessage());
        } catch (Exception e) {
            // what to do? ignore? send back error - from log???
        }
    }

    public void close() {
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = Integer.valueOf(port);
    }

    public void setMsetmax(String m) {
        this.MSetMax = Integer.valueOf(m);
    }

    public boolean requiresLayout() {
        return false;
    }

    private String now() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        return dateFormat.format(new Date());
    }
}