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
* @copyright 2012 Pavlo Baron */

package test.org.pbit.log4j2redis;

import org.apache.log4j.Logger;

public class Log4j2RedisTest {
    
    public static class LogThread extends Thread {
        Logger log = Logger.getLogger("LogThread");
        public void run() {
            for (long i = 0; i < 10000; i++)
                log.warn("whatever " + i);
        }
    }
    
    static Logger log = Logger.getLogger("LogMainThread");
    
    public static void main(String[] args) {
        for (int i = 1; i <= 9; i++) {
            new Log4j2RedisTest.LogThread().start();
        }
        
        for (long i = 0; i < 10000; i++) {
            log.error("that's me " + i);
        }
    }
}
