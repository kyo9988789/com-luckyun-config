package com.luckyun.config.redis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * redis操作辅助实现类
 * @author yangj080
 *
 */
@Service
public class RedisOperationDaoImpl implements RedisOperationDao{

	@Autowired
    private RedisTemplate<String, ?> redisTemplate;
	
	@Override
	public boolean set(String key, String value) 
	{
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.set(serializer.serialize(key), serializer.serialize(value));  
                return true;  
            }  
        });
		return result;
	}
	
	@Override
    public <T> boolean set(String key, T t) {
        final String value = JSON.toJSONString(t);
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.set(serializer.serialize(key),serializer.serialize(value));  
                return true;  
            }  
        });
        return result;
    }

	@Override
	public <T> boolean set(String key, T t, Long expire) {
		final String value = JSON.toJSONString(t);
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {  
            @Override  
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                connection.setEx(serializer.serialize(key),expire,serializer.serialize(value));  
                return true;  
            }  
        });
		return result;
	}

	@Override
	public String get(String key) {
		String result = redisTemplate.execute(new RedisCallback<String>() {  
            @Override  
            public String doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                byte[] value =  connection.get(serializer.serialize(key));  
                return serializer.deserialize(value);  
            }
        });
		return result;
	}

	@Override
	public <T> T get(String key, Class<T> clazz) {
		String result = redisTemplate.execute(new RedisCallback<String>() {  
            @Override  
            public String doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                byte[] value =  connection.get(serializer.serialize(key));  
                return serializer.deserialize(value);  
            }  
        });
		return JSONObject.parseObject(result, clazz);
	}
	
	@Override
	public Set<String> getKeys(String pattern){
	    return redisTemplate.keys(pattern);
	}
	
	@Override
	public void deleteByPattern(String pattern) {
	    Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
	}

	@Override
	public boolean expire(String key, long expire) {
		return redisTemplate.expire(key, expire, TimeUnit.SECONDS);
	}

	@Override
	public <T> boolean setList(String key, List<T> list) {
		String value = JSON.toJSONString(list);
		return set(key, value);
	}

	@Override
	public <T> List<T> getList(String key, Class<T> clz) {
		String json = get(key);  
		 if(json!=null){  
           List<T> list = JSON.parseArray(json, clz);  
           return list;  
		 }  
		return null;
	}

	@Override
	public long lpush(String key, String obj) {
		final String value = obj;
        long result = redisTemplate.execute(new RedisCallback<Long>() {  
            @Override  
            public Long doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                long count = connection.lPush(serializer.serialize(key), serializer.serialize(value));  
                return count;  
            }  
        });
		return result;
	}

	@Override
	public long rpush(String key, String obj) {
		final String value = obj;
        long result = redisTemplate.execute(new RedisCallback<Long>() {  
            @Override  
            public Long doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                long count = connection.rPush(serializer.serialize(key), serializer.serialize(value));  
                return count;  
            }  
        });  
        return result;
	}

	@Override
	public String lpop(String key) {
		String result = redisTemplate.execute(new RedisCallback<String>() {  
            @Override  
            public String doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                byte[] res =  connection.lPop(serializer.serialize(key));  
                return serializer.deserialize(res);  
            }  
        });  
        return result;
	}

	@Override
	public <T> List<T> lrangeAll(String key,Class<T> clz) {
		String result = redisTemplate.execute(new RedisCallback<String>() {  
            @Override  
            public String doInRedis(RedisConnection connection) throws DataAccessException {  
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                List<byte[]> res =  connection.lRange(serializer.serialize(key),0,-1);
                List<String> strings = new ArrayList<>();
                for(byte[] sres : res) {
                	strings.add(serializer.deserialize(sres));
                }
                return JSON.toJSONString(strings);  
            }  
        });
		return JSON.parseArray(result, clz);
	}

	@Override
	public void del(String key) {
		redisTemplate.execute(new RedisCallback<String>() {  
            @Override  
            public String doInRedis(RedisConnection connection) throws DataAccessException {  
            	RedisSerializer<String> serializer = redisTemplate.getStringSerializer();  
                Long istate = connection.del(serializer.serialize(key));
                return istate.toString();  
            }  
        });
	}


	@Override
	public String hMset(String key, Map<String, String> datas) {
		final RedisSerializer<String> serializer = redisTemplate.getStringSerializer(); 
		final Map<byte[], byte[]> mapBytes = new LinkedHashMap<byte[], byte[]>();
		for (Map.Entry<String, String> entry : datas.entrySet()) {
			byte[] bKey = entry.getKey().getBytes();
			byte[] bValues = serializer.serialize(entry.getValue());
			mapBytes.put(bKey, bValues);
		}
		String result = redisTemplate.execute(new RedisCallback<String>() {

			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				
				connection.hMSet(serializer.serialize(key), mapBytes);
				return "ok";
			}
			
		});
		return result;
	}

	@Override
	public <T> List<T> hMgetList(String key, Class<T> clazz, String... fields) {
		List<T> result = redisTemplate.execute(new RedisCallback<List<T>>() {

			@Override
			public List<T> doInRedis(RedisConnection connection) throws DataAccessException {
				// TODO Auto-generated method stub
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer(); 
				byte[][] byteDatas = new byte[fields.length][];
				int i = 0;
				for(String str : fields) {
					byteDatas[i] = serializer.serialize(str);
					i++;
				}
				List<byte[]> bs = connection.hMGet(serializer.serialize(key), byteDatas);
				List<T> list = new ArrayList<T>();
				for(byte[] by : bs) {
					String rs = new String(by);
					T t = JSONObject.parseObject(rs, clazz);
					list.add(t);
				}
				return list;
			}
		});
		return result;
	}

	@Override
	public <T> T hMget(String key, Class<T> clazz, String field) {
		T result = redisTemplate.execute(new RedisCallback<T>() {

			@Override
			public T doInRedis(RedisConnection connection) throws DataAccessException {
				// TODO Auto-generated method stub
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
				byte[] bytes = connection.hGet(serializer.serialize(key), serializer.serialize(field));
				String json = serializer.deserialize(bytes);
				System.out.println(clazz.getTypeName());
				return JSONObject.parseObject(json, clazz);
			}
		});
		return result;
	}
	
	@Override
	public String hMget(String key,String field) {
		String result = redisTemplate.execute(new RedisCallback<String>() {

			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				// TODO Auto-generated method stub
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
				byte[] bytes = connection.hGet(serializer.serialize(key), serializer.serialize(field));
				String json = serializer.deserialize(bytes);
				return json;
			}
		});
		return result;
	}

	@Override
	public <T> Map<String, T> hMgetAll(String key, Class<T> clazz) {
		Map<String,T> maps = redisTemplate.execute(new RedisCallback<Map<String,T>>() {

			@Override
			public Map<String,T> doInRedis(RedisConnection connection) throws DataAccessException {
				// TODO Auto-generated method stub
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer(); 
				Map<byte[], byte[]> maps= connection.hGetAll(serializer.serialize(key));
				Map<String,T> mapss = new LinkedHashMap<>();
				for(Map.Entry<byte[], byte[]> entry : maps.entrySet()) {
					String key = serializer.deserialize(entry.getKey());
					String values = serializer.deserialize(entry.getValue());
					mapss.put(key, JSONObject.parseObject(values, clazz));
				}
				return mapss;
			}
		});
		return maps;
	}
	
	@Override
	public Map<String, Object> hMgetAll(String key) {
		Map<String,Object> maps = redisTemplate.execute(new RedisCallback<Map<String,Object>>() {

			@Override
			public Map<String,Object> doInRedis(RedisConnection connection) throws DataAccessException {
				// TODO Auto-generated method stub
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer(); 
				Map<byte[], byte[]> maps= connection.hGetAll(serializer.serialize(key));
				Map<String,Object> mapss = new LinkedHashMap<>();
				for(Map.Entry<byte[], byte[]> entry : maps.entrySet()) {
					String key = serializer.deserialize(entry.getKey());
					String values = serializer.deserialize(entry.getValue());
					mapss.put(key, values);
				}
				return mapss;
			}
		});
		return maps;
	}

	@Override
	public boolean hSet(String key, String field, String data) {
		Boolean flag = redisTemplate.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hSet(key.getBytes(), field.getBytes(), data.getBytes());
			}
		});
		return flag;
	}

	/**
	 * 原子性自增数字
	 */
	@Override
	public Long getIncrNum(String key) {
		return redisTemplate.execute(new RedisCallback<Long>() {

			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.incr(key.getBytes());
			}
			
		});
	}
	
	@Override
	public Long getHIncrByNum(String key, String field) {
		return redisTemplate.execute(new RedisCallback<Long>() {

			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hIncrBy(key.getBytes(), field.getBytes(), 1L);
			}
		});
	}

	@Override
	public boolean existKey(String key) {
		boolean flag = redisTemplate.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				
				return connection.exists(key.getBytes());
			}
		});
		return flag;
	}

	@Override
	public Long getExpireTime(String key) {
		return redisTemplate.execute(new RedisCallback<Long>() {

			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.ttl(key.getBytes());
			}
			
		});
	}

	@Override
	public void hdel(String key, String... field) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<String> getPatternKeys(String patternKey) {
		return redisTemplate.execute(new RedisCallback<List<String>>() {

			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
				Set<byte[]> content = connection.keys(patternKey.getBytes());
				RedisSerializer<String> serializer = redisTemplate.getStringSerializer(); 
				List<String> result = new ArrayList<>();
				for(byte[] bs : content) {
					result.add(serializer.deserialize(bs));
				}
				return result;
			}
		});
	}
}
