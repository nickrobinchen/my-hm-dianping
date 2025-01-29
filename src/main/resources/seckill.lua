--
local voucherId = ARGV[1]

local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- judge if stock availble
if (tonumber(redis.call('get', stockKey)) <= 0) then
    return 1
end

-- judge if user has ordered already SISMEMBER
if(redis.call('sismember', orderKey, userId) == 1) then
    return 2
end

redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)
return 0