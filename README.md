log4j2redis
===========

log4j appender that writes straight to Redis.

The appender works with log4j 1.x series and is currently not supporting the newer log4j 2.x series. 

The code is released under Apache License 2.0.

## Configuration

This appender writes to a Redis store. Here is an example configuration:

    log4j.rootLogger=DEBUG, REDIS
    log4j.appender.REDIS=org.pbit.log4j2redis.RedisAppender
    log4j.appender.REDIS.host=localhost
    log4j.appender.REDIS.port=6379
    log4j.appender.REDIS.msetmax=100
    log4j.appender.REDIS.layout=org.pbit.log4j2redis.RedisPatternLayout
    log4j.appender.REDIS.layout.ConversionPattern=%H - %P - %t - %d - %p - %U

Where:

* **host** and **port** are optional properties, so if they are not set it will use the standard **localhost** and **6379**
* **msetmax** is the number of messages to be sent in one batch MSET command, which defaults to **100**
* RedisPatternLayout extends the standard PatternLayout with %U (UUID), %P (process name) and %H (host) The pattern is used
to build the Redis key, while the simply rendered log message will be the value behind it.

It's recommended to use %U, %P and %H. That way, it should be possible to uniquely collect log messages from any host while writing
to one single Redis node. Redis can of course be configured to have persisting slaves while
the one and only target node just writes into memory (attention: after its restart the data on
slaves might get lost, so back it up - it's just append-only logs). Of course, a more complex
Redis topology can be implemented and used, but therefore, consult Redis documentation.

## Message Writing

Every **log message** will be first stored in memory and after asynchronously sent to Redis. Thus
network latency doesn't impact log writing - unless, of course, that message writing is too
fast and network is too slow, what might throw an "out of memory"; but it would be the worst
case ever.

## Developers

* Pavlo Baron
* Leandro Silva

## Contribution

There still is a black hole in the code with a central map - I'll get back to it later.

Feedback and contribution are welcome.
