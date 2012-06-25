/**
* This file is part of log4j2redis
*
* Copyright (c) 2012 by Pavlo Baron (pb at pbit dot org)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*

* @author Pavlo Baron <pb at pbit dot org>
* @author Landro Silva
* @copyright 2012 Pavlo Baron
**/

package org.pbit.log4j2redis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

public class RedisAppender extends AppenderSkeleton {
    // Log4J properties
    private String host = "localhost";
    private int port = 6379;
    private int msetmax = 100;

    // Redis connection and messages buffer
    private Jedis jedis;
    private Map<String, String> messages;

    public void activateOptions() {
        super.activateOptions();

        jedis = new Jedis(host, port);
        messages = new ConcurrentHashMap<String, String>();

        new Timer().schedule(new TimerTask() {
            public void run() {
                // long begin = System.nanoTime();

                Entry<String, String> message;

                int currentMessagesCount = messages.size();
                int bucketSize = currentMessagesCount < msetmax ? currentMessagesCount : msetmax;
                byte[][] bucket = new byte[bucketSize * 2][];

                int messageIndex = 0;
                
                for (Iterator<Entry<String, String>> it = messages.entrySet().iterator(); it.hasNext();) {
                    message = it.next();
                    it.remove();

                    // [k1, v1, k2, v2, ..., kN, vN]
                    bucket[messageIndex] = SafeEncoder.encode(message.getKey());
                    bucket[messageIndex + 1] = SafeEncoder.encode(message.getValue());
                    messageIndex += 2;

                    if (messageIndex == bucketSize * 2) {
                        jedis.mset(bucket);
                        
                        currentMessagesCount -= bucketSize;
                        
                        if (currentMessagesCount == 0) {
                        	// get out the loop and wait 1/2 second
                        	break;
                        } else {
                        	bucketSize = currentMessagesCount < msetmax ? currentMessagesCount : msetmax;
                            bucket = new byte[bucketSize * 2][];
                            
                            messageIndex = 0;
                        }
                    }
                }

                // long expendHere = System.nanoTime() - begin;
                // System.out.println("Expend here: " + expendHere + " ns");
            }
        }, 500, 500);
    }

    protected void append(LoggingEvent event) {
        try {
            messages.put(this.layout.format(event), event.getRenderedMessage());
        } catch (Exception e) {
            // what to do? ignore? send back error - from log???
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

    public void setMsetmax(int msetmax) {
        this.msetmax = msetmax;
    }

    public boolean requiresLayout() {
        return true;
    }
}