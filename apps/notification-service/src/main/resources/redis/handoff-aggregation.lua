if redis.call('EXISTS', KEYS[1]) == 0 then
    redis.call('ZREM', KEYS[2], KEYS[1])
    return 0
end

local created = false
if redis.call('EXISTS', KEYS[3]) == 0 then
    local result = redis.call('SET', KEYS[3], ARGV[1], 'PX', ARGV[4], 'NX')
    created = result ~= false
end

local laneResult = redis.pcall('ZADD', KEYS[4], 'NX', ARGV[2], ARGV[3])
if type(laneResult) == 'table' and laneResult.err then
    if created then redis.call('DEL', KEYS[3]) end
    return redis.error_reply(laneResult.err)
end

redis.call('DEL', KEYS[1])
redis.call('ZREM', KEYS[2], KEYS[1])
return 1
