local score = redis.call('ZSCORE', KEYS[1], ARGV[1])
if not score or tonumber(score) > tonumber(ARGV[2]) then
    return 0
end

if redis.call('EXISTS', KEYS[2]) == 0 then
    redis.call('ZREM', KEYS[1], ARGV[1])
    return 0
end

if redis.call('RENAMENX', KEYS[2], KEYS[3]) == 0 then
    return 0
end

redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('ZADD', KEYS[4], ARGV[3], KEYS[3])
redis.call('PEXPIRE', KEYS[3], ARGV[4])
return 1
