if redis.call('EXISTS', KEYS[1]) == 0 then
    redis.call('ZREM', KEYS[2], ARGV[1])
    return 0
end

local candidateCount = (#KEYS - 2) / 2
for i = 0, candidateCount - 1 do
    local keyOffset = 3 + (i * 2)
    local argOffset = 2 + (i * 4)
    local candidateKey = KEYS[keyOffset]
    local laneKey = KEYS[keyOffset + 1]
    local candidate = ARGV[argOffset]
    local readyAt = ARGV[argOffset + 1]
    local candidateId = ARGV[argOffset + 2]
    local candidateTtl = ARGV[argOffset + 3]

    if redis.call('EXISTS', candidateKey) == 0 then
        redis.call('SET', candidateKey, candidate, 'PX', candidateTtl, 'NX')
    end
    redis.call('ZADD', laneKey, 'NX', readyAt, candidateId)
end

redis.call('DEL', KEYS[1])
redis.call('ZREM', KEYS[2], ARGV[1])
return 1
