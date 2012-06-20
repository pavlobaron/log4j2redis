log4j2redis
===========

log4j appender that writes straight to Redis.

(the 42 looking thingy in the name is intended, yes)

## Configuration

This appender writes to a Redis store. Here is an example configuration:

    log4j.rootLogger=DEBUG, REDIS
    log4j.appender.REDIS=org.pbit.log4j2redis.RedisAppender
    log4j.appender.REDIS.host=localhost
    log4j.appender.REDIS.port=6379
    log4j.appender.REDIS.msetmax=100

**Host** and **Port** are optional properties, so if they are not set it will use the standard **localhost** and **6379**.
**msetmax** (default 100) is the number of messages to be sent in one batch MSET command.

## Message Format

Every **log message** will be written behind a key of this format:

    hostname - process_name_on_host - thread_id_there - timestamp - log_level - unique UUID

That way, it should be possible to uniquely collect log messages from any host while writing
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
Pavlo Baron (original version)

Leandro Silva

## Contribution

There still is a black hole in the code with a central map - I'll get back to it later.

Feedback and contribution are welcome.
