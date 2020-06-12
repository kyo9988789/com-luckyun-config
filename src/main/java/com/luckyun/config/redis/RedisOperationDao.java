package com.luckyun.config.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @author yj
 *
 */
public interface RedisOperationDao 
{
	/**
	 * 写入缓存
	 * @param key 键
	 * @param value 值
	 * @return 返回是否redis set是否成功
	 */
	boolean set(String key, String value);
	
	/**
     * 将对象写入缓存
     * @param key 键
     * @param t 内容对象
     * @return redis set的对象数据
     */
    <T> boolean set(String key ,T t);
	
	/**
	 * 将对象写入缓存
	 * @param key 键
	 * @param t 内容对象
	 * @param expire 过期时间
	 * @return redis set的对象数据
	 */
	<T> boolean set(String key ,T t,Long expire);
    
	/**
	 * 获取缓存内容
	 * @param key 键
 	 * @return 获取对应的key的存储内容
	 */
    String get(String key);
    
    /**
     * 获取内容对象
     * @param key 键
     * @param clazz 获取的内容对象类型
     * @return 返回存储的实体对象
     */
    <T> T get(String key,Class<T> clazz);
    
    /**
     * 获取相关的key
     * @param pattern 匹配的字符串
     * @return
     */
    Set<String> getKeys(String pattern);
    
    /**
     *模糊匹配 删除keys
     * @param pattern
     */
    void deleteByPattern(String pattern);
      
    /**
     * 给缓存key添加过期时间
     * @param key 键
     * @param expire 过期时间
     * @return 添加key的过期时间是否成功
     */
    boolean expire(String key,long expire);  
    /**
     * 将数组数据放入缓存中
     * @param key 键
     * @param list 内容数组
     * @return 是否成功将数组数据导入缓存中
     */
    <T> boolean setList(String key ,List<T> list);  
    /**
     * 获取内容转数组
     * @param key 键
     * @param clz 数组对象类型
     * @return 获取缓存的数组数据
     */
    <T> List<T> getList(String key,Class<T> clz);  
    /**
     * 将内容放入redis 列表list的头部添加字符串元素
     * @param key 键
     * @param obj 内容
     * @return 对应缓存的数组的长度
     */
    long lpush(String key,String obj);  
     /**
      * 在key对应 list 的尾部添加字符串元素
      * @param key
      * @param obj
      * @return 对应缓存的数组的长度
      */
    long rpush(String key,String obj);  
    /**
     * 取出redis list的内容
     * @param key 键
     * @return 获取缓存移除的第一个元素
     */
    String lpop(String key);
    
    /**
     * 获取对应的key数组所有的数据
     * @param key
     * @param clz
     * @return 获取缓存数组的所有数据
     */
    <T> List<T> lrangeAll(String key,Class<T> clz);
    
    /**
     * 删除key
     * @param key redis键名
     */
    void del(String key);

    void hdel(String key,String... field);
    
    /**
     * redis hash操作
     * @param key 键
     * @param datas map类型的数据
     * @return
     */
    public String hMset(String key,Map<String,String> datas);
    
    /**
     * redis hash添加数据
     * @param key 键
     * @param field hash内部键
     * @param data 数据
     * @return 是否添加成功
     */
    public boolean hSet(String key,String field, String data);
    
    /**
     * redis 获取hash值数据
     * @param key 键
     * @param field 多个field参数
     * @param clazz 插入的对象
     * @return 对象集合
     */
    public <T> List<T> hMgetList(String key,Class<T> clazz,String... fields);
    
    /**
     * 获取hash里面的对象数据
     * @param key 键
     * @param clazz 返回的对象
     * @param field hash键
     * @return 当前的field的结果集
     */
    public <T> T hMget(String key,Class<T> clazz,String field);
    
    /**
     * 获取hash里面的对象数据
     * @param key 键
     * @param clazz 返回的对象
     * @param field hash键
     * @return 当前的field的结果集
     */
    public String hMget(String key,String field);
    /**
     * redis 获取所有的key-value的内容
     * @param key 返回的对象
     * @param clazz 返回的对象
     * @return 整个hash的结果集
     */
    public <T> Map<String,T> hMgetAll(String key,Class<T> clazz);
    
    /**
     * redis 获取所有的key-value的内容
     * @param key 返回的对象
     * @param clazz 返回的对象
     * @return 整个hash的结果集
     */
    public Map<String,Object> hMgetAll(String key);
    
    /**
     * 获取原子自增值
     * @param key 只能添加int值得key
     * @return 原子自增后的值
     */
    Long getIncrNum(String key);
    
    /**
     * 原子性获取hash结构中的对应的field的值
     * @param key 键
     * @param field
     * @return 原子添加后的值
     */
    Long getHIncrByNum(String key,String field);
    
    /**
     * 判断key是否存在
     * @param key key值
     * @return 是否存在
     */
    boolean existKey(String key);
    
    /**
     * 获取key的过期时间,以秒为单位
     * @param key key键值
     * @return 以秒为单位的时常
     */
    Long getExpireTime(String key);
    
    /**
     * 根据key的正则找到对应的所有key列表
     * @param pattern 主键符合的正则数据
     * @return key的集合
     */
    List<String> getPatternKeys(String pattern);
}
