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
* @copyright 2012 Pavlo Baron
**/

package org.pbit.log4j2redis;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

public class RedisPatternParser extends PatternParser {

    static final int PROCESS_CONVERTER = 1104;
    static final int HOST_CONVERTER = 1105;
    static final int UUID_CONVERTER = 1106;
    
    private static String localHostName;
    private static String processName;
    
    private static class RedisPatternConverter extends PatternConverter {
        int type;

        RedisPatternConverter(FormattingInfo formattingInfo, int type) {
            super(formattingInfo);
            this.type = type;
        }

        public String convert(LoggingEvent event) {
            switch (type) {
                case PROCESS_CONVERTER:
                    return processName;
                case HOST_CONVERTER:
                    return localHostName;
                case UUID_CONVERTER:
                    return UUID.randomUUID().toString();
                default:
                    return null;
            }
        }
    }
    
    public RedisPatternParser(String s) {
        super(s);
        
        if (RedisPatternParser.processName == null) {
            try {
                RedisPatternParser.localHostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                RedisPatternParser.localHostName = "localhost";
            }
    
            RedisPatternParser.processName = ManagementFactory.getRuntimeMXBean().getName();
        }
    }
    
    @Override
    protected void finalizeConverter(char c) {
        PatternConverter pc = null;
        switch(c) {
            case 'P':
                pc = new RedisPatternConverter(formattingInfo, PROCESS_CONVERTER);
                currentLiteral.setLength(0);
                addConverter(pc);
                break;
            case 'H':
                pc = new RedisPatternConverter(formattingInfo, HOST_CONVERTER);
                currentLiteral.setLength(0);
                addConverter(pc);
                break;
            case 'U':
                pc = new RedisPatternConverter(formattingInfo, UUID_CONVERTER);
                currentLiteral.setLength(0);
                addConverter(pc);
                break;
            default:
                super.finalizeConverter(c);
        }
    }
}
