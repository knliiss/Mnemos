if redis.call('EXISTS', KEYS[2]) == 0 then
    redis.call('ZREM', KEYS[4], KEYS[2])
    return 0
end

local entries = redis.call('HGETALL', KEYS[2])
for i = 1, #entries, 2 do
    redis.call('HSETNX', KEYS[3], entries[i], entries[i + 1])
end

redis.call('DEL', KEYS[2])
redis.call('ZREM', KEYS[4], KEYS[2])
redis.call('PEXPIRE', KEYS[3], ARGV[3])
redis.call('ZADD', KEYS[1], 'LT', ARGV[2], ARGV[1])
return 1
