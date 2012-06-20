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

public class RedisAppender extends AppenderSkeleton {
    
    private Jedis jedis;
    private String host = "localhost";
    private int port = 6379;
    
    private Map<String, String> messages;
    private String localHostName;
    private String processName;
    
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
				Entry<String, String> message;
				
				for (Iterator<Entry<String, String>> it = messages.entrySet().iterator(); it.hasNext();) {
					message = it.next();
					jedis.set(message.getKey(), message.getValue());
					it.remove();
				}
			}
		}, 1000, 1000);
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
            //what to do? ignore? send back error - from log???
        }
    }

    public void close() {
    }
    
    public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean requiresLayout() {
    	return false;
    }
    
    private String now() {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        return dateFormat.format(new Date());
    }
}
