log4j2redis
===========

log4j appender that writes straight to Redis.

(the 42 looking thingy in the name is intended, yes)

This appender writes to a Redis store. Here is an example configuration:

log4j.rootLogger=DEBUG, REDIS
log4j.appender.REDIS=org.pbit.log4j2redis.RedisAppender
log4j.appender.REDIS.host = localhost

Port isn't supported yet, it will write to the standard 6379 port.

Every log message will be written behind a key of this format:

hostname - process_name_on_host - thread_id_there - timestamp - log_level - unique UUID

That way, it should be possible to uniquely collect log messages from any host while writing
to one single Redis node. Redis can of course be configured to have persisting slaves while
the one and only target node just writes into memory (attention: after its restart the data on
slaves might get lost, so back it up - it's just append-only logs). Of course, a more complex
Redis topology can be implemented and used, but therefore, consult Redis documentation.

There still is a black hole in the code with a central map - I'll get back to it later. And I also
need to elaborate on batching events instead of sending every single one over the network.

Feedback and contribution are welcome.
