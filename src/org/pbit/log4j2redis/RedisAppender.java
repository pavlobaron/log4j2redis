package org.pbit.log4j2redis;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAppender extends AppenderSkeleton {
    
    protected JedisPool pool;
    protected Map<Long, Jedis> map = new ConcurrentHashMap<Long, Jedis>();
    protected String localHostName;
    protected String processName;
    
    @Override
    protected void append(LoggingEvent event) {
        //unclear: how about batching events to reduce network gossip?
        try {
            StringBuilder id = new StringBuilder(getLocalHostName());
            id.append(" - ");
            id.append(getProcessName());
            id.append(" - ");
            id.append(Thread.currentThread().getId());
            id.append(" - ");
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            id.append(dateFormat.format(date));
            id.append(" - ");
            id.append(event.getLevel().toString());
            id.append(" - ");
            id.append(UUID.randomUUID().toString());
            getJedis().set(id.toString(), event.getRenderedMessage());
        } catch (Exception e) {
            //what to do? ignore? send back error - from log???
        }
    }

    public void close() {
    }
    
    public boolean requiresLayout() {
        return false;
    }
    
    public void setHost(String host) {
        pool = new JedisPool(host);
    }
    
    protected String getLocalHostName() throws Exception {
        if (localHostName == null) {
            localHostName = InetAddress.getLocalHost().getHostName();
        }

        return localHostName;
    }
    
    protected String getProcessName() {
        if (processName == null) {
            processName = ManagementFactory.getRuntimeMXBean().getName();
        }

        return processName;
    }
    
    protected Jedis getJedis() {
        //here, the connection pool must be cleaned,
        //searching for non-existent threads and removing them from the map.
        //otherwise, the map can really get pretty full.
        //maybe this should be done in a parallel thread to immediately return from
        //this call (oh, how I miss asynchronous message passing here...).
        
        Jedis j = map.get(Thread.currentThread().getId());
        if (j == null) {
            if (pool == null) {
                setHost("localhost");
            }
            
            j = pool.getResource();
        }
        
        map.put(Thread.currentThread().getId(), j);
        
        return j;
    }
}
