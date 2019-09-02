package top.wboost.common.cache.redis;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.ScriptingCommands;
import top.wboost.common.cache.DataCache;

public interface MyJedisCommands extends DataCache, JedisCommands, BinaryJedisCommands, ScriptingCommands {

}
