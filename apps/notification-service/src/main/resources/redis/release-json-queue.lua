if redis.call('EXISTS', KEYS[3]) == 0 then
    redis.call('ZREM', KEYS[4], ARGV[1])
    return 0
end

if redis.call('EXISTS', KEYS[2]) == 0 then
    local value = redis.call('GET', KEYS[3])
    redis.call('SET', KEYS[2], value, 'PX', ARGV[3])
end

redis.call('DEL', KEYS[3])
redis.call('ZREM', KEYS[4], ARGV[1])
redis.call('ZADD', KEYS[1], 'LT', ARGV[2], ARGV[1])
return 1
