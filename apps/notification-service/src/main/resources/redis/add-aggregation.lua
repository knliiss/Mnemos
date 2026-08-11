if redis.call('EXISTS', KEYS[1]) == 1 then
    return 0
end

local sourceId = ARGV[1]
local item = ARGV[2]
local flushAt = ARGV[3]
local acceptedTtl = ARGV[4]
local bucketTtl = ARGV[5]

for i = 3, #KEYS do
    local userId = ARGV[i + 3]

    redis.call('HSETNX', KEYS[i], sourceId, item)
    redis.call('PEXPIRE', KEYS[i], bucketTtl)
    redis.call('ZADD', KEYS[2], 'LT', flushAt, userId)
end

redis.call('SET', KEYS[1], '1', 'PX', acceptedTtl)
return 1
