if redis.call('EXISTS', KEYS[1]) == 1 then
    return 0
end

redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[5])
redis.call('ZADD', KEYS[3], 'NX', ARGV[3], ARGV[1])
redis.call('SET', KEYS[1], '1', 'PX', ARGV[4])
return 1
