package io.github.hqqich.cache;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hqqich on 2022/8/11 is 15:15.
 *
 * @date 2022/8/11
 */
public class CacheManager {

	private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

	/**
	 * 缓存项最大数量
	 */
	private static final long GUAVA_CACHE_SIZE = 10000;

	/**
	 * 缓存时间
	 */
	private static final long GUAVA_CACHE_TIME = 5;

	/**
	 * 缓存操作对象,这就是缓存对象,数据就放在这
	 */
	private static LoadingCache<String, String> GLOBAL_CACHE = null;

	static {


		try {
			GLOBAL_CACHE = loadCache(new CacheLoader<String, String>() {
				@Override
				public String load(String s) throws Exception {
					return null;
				}
			});
		} catch (Exception e) {
			log.info("初始化Guava Cache出错" + e);
		}
	}

	/**
	 * 全局缓存设置
	 * <p>
	 * 缓存项最大数量：100000
	 * 缓存有效时间（天）：10
	 *
	 * @param cacheLoader
	 * @return
	 * @throws Exception
	 */
	private static LoadingCache<String, String> loadCache(CacheLoader<String, String> cacheLoader) throws Exception {
		LoadingCache<String, String> cache = CacheBuilder.newBuilder()
				//缓存池大小，在缓存项接近该大小时， Guava开始回收旧的缓存项
				.maximumSize(GUAVA_CACHE_SIZE)
				//设置时间对象没有被读/写访问则对象从内存中删除(在另外的线程里面不定期维护)
				.expireAfterAccess(GUAVA_CACHE_TIME, TimeUnit.MINUTES)
				// 设置缓存在写入之后 设定时间 后失效
				.expireAfterWrite(GUAVA_CACHE_TIME, TimeUnit.MINUTES)
				//移除监听器,缓存项被移除时会触发
				.removalListener(new RemovalListener<String, String>() {
					@Override
					public void onRemoval(RemovalNotification<String, String> rn) {
						//逻辑操作
						//ystem.out.println("移除了键");
						// 如果是超时时间到了
						if (rn.getCause().equals(RemovalCause.EXPIRED)) {
							log.info("超时删除");
						}
					}
				})
				//开启Guava Cache的统计功能
				.recordStats()
				.build(cacheLoader);
		return cache;
	}

	/**
	 * 设置缓存值
	 * 注: 若已有该key值，则会先移除(会触发removalListener移除监听器)，再添加
	 *
	 * @param key
	 * @param value
	 */
	public static void put(String key, String value) {
		try {
			GLOBAL_CACHE.put(key, value);
		} catch (Exception e) {
			log.info("设置缓存值出错" + e);
		}
	}

	/**
	 * 批量设置缓存值
	 *
	 * @param map
	 */
	public static void putAll(Map<? extends String, ? extends String> map) {
		try {
			GLOBAL_CACHE.putAll(map);
		} catch (Exception e) {
			log.info("批量设置缓存值出错" + e);
		}
	}

	/**
	 * 获取缓存值
	 * 注：如果键不存在值，将调用CacheLoader的load方法加载新值到该键中
	 *
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		String token = "";
		try {
			token = GLOBAL_CACHE.get(key);
		} catch (Exception e) {
			log.info("获取缓存值出错" + e);
		}
		return token;
	}

	public static String getHigh(String key) {
		String token = "";
		try {
			GLOBAL_CACHE.get(key, new Callable<String>() {
				@Override
				public String call() throws Exception {
					// 读源，也就是从数据库中读取
					// ...
					String value = "SQL DATA RETURN";
					// 写回缓存
					GLOBAL_CACHE.put(key, value);
					return value;
				}
			});
		} catch (Exception e) {
			log.info("获取缓存值出错" + e);
		}
		return token;
	}

	/**
	 * 移除缓存
	 *
	 * @param key
	 */
	public static void remove(String key) {
		try {
			GLOBAL_CACHE.invalidate(key);
		} catch (Exception e) {
			log.info("移除缓存出错" + e);
		}
	}

	/**
	 * 批量移除缓存
	 *
	 * @param keys
	 */
	public static void removeAll(Iterable<String> keys) {
		try {
			GLOBAL_CACHE.invalidateAll(keys);
		} catch (Exception e) {
			log.info("批量移除缓存出错" + e);
		}
	}

	/**
	 * 清空所有缓存
	 */
	public static void removeAll() {
		try {
			GLOBAL_CACHE.invalidateAll();
		} catch (Exception e) {
			log.info("清空所有缓存出错" + e);
		}
	}

	/**
	 * 获取缓存项数量
	 *
	 * @return
	 */
	public static long size() {
		long size = 0;
		try {
			size = GLOBAL_CACHE.size();
		} catch (Exception e) {
			log.info("获取缓存项数量出错" + e);
		}
		return size;
	}

	/**
	 * 显示缓存中的所有内容
	 *
	 * @return
	 * @author hqqich
	 * @date 2022/8/25 19:28
	 **/
	public static Set<Entry<String, String>> display() {
		// 下面是遍历值
		//Iterator<Map.Entry<String, String>> iterator = GLOBAL_CACHE.asMap().entrySet().iterator();
		//while (iterator.hasNext()) {
		//	//logger.info(iterator.next().toString());
		//}
		return GLOBAL_CACHE.asMap().entrySet();
	}

	public static List<Entry<String, String>> displayList() {
		// 下面是遍历值
		//Iterator<Map.Entry<String, String>> iterator = GLOBAL_CACHE.asMap().entrySet().iterator();
		//while (iterator.hasNext()) {
		//	//logger.info(iterator.next().toString());
		//}

		return new ArrayList<>(GLOBAL_CACHE.asMap().entrySet());
	}
}
