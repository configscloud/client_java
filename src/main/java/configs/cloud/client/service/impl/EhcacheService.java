package configs.cloud.client.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import configs.cloud.client.CloudConfigClient;
import configs.cloud.client.entity.Config;
import configs.cloud.client.service.CacheService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class EhcacheService implements CacheService {
	
	private static final Logger logger = Logger.getLogger(CloudConfigClient.class);
	private static final String CONFIG_CACHE = "config_cache_";
	private static final String ENV_KEY_SEPARATOR = ":";

	private CacheManager cm = null;
	
	public EhcacheService() {
		cm = CacheManager.getInstance();
	}

	/**
	 * Get cache with particular name. Create one if not available and return.
	 * 
	 * @param name Cache Name
	 * @return Cache Cache 
	 */

	public Cache getCache(String name) {

		logger.debug("Getting cache : " + name);
		Cache cache = cm.getCache(name);

		if (null == cache) {

			logger.debug("Cache not found. Creating new cache : " + name);

			CacheConfiguration cacheConfiguration = new CacheConfiguration();
			cacheConfiguration.setName(name);
			cacheConfiguration.setMaxEntriesLocalHeap(1000);
			cacheConfiguration.timeToIdleSeconds(1000);
			cacheConfiguration.timeToLiveSeconds(1000);

			cache = new Cache(cacheConfiguration);
			logger.debug("New cache created : " + name);

			cm.addCache(cache);
			logger.debug("New cache added to cache manager.");
		}

		return cache;
	}

	/**
	 * Retrieves Configs list from the cache 
	 * 
	 * @param datasetId Dataset id
	 * @return {@link List}&lt;{@link Config}&gt; List of Config objects
	 */
	@Override
	public List<Config> getConfigListFromCache(Integer datasetId) {

		logger.debug("Getting config from cache. Dataset Id : " + datasetId);

		Cache cache = getCache(CONFIG_CACHE + datasetId);
		if (null != cache) {

			List<Config> configs = new ArrayList<>();
			Iterator<Object> it = cache.getKeys().iterator();

			while (it.hasNext()) {
				Object key = it.next();
				Element element = cache.get(key);
				configs.add((Config) element.getObjectValue());
			}
			return configs;
		}

		return null;
	}

	/**
	 * Retrieves Configs list from the cache 
	 * 
	 * @param datasetId Dataset id
	 * @param env Environment name
	 * @return {@link List}&lt;{@link Config}&gt; List of Config objects
	 */
	@Override
	public List<Config> getConfigListFromCache(Integer datasetId, String env) {

		logger.debug("Getting config from cache. Dataset Id : " + datasetId + " Env : " + env);

		Cache cache = getCache(CONFIG_CACHE + datasetId);
		if (null != cache) {

			List<Config> configs = new ArrayList<>();
			Iterator<Object> it = cache.getKeys().iterator();

			while (it.hasNext()) {

				Object key = it.next();
				if (((String) key).startsWith(env + ":")) {

					Element element = cache.get(key);
					configs.add((Config) element.getObjectValue());
				}
			}
			return configs;
		}

		return null;
	}

	/**
	 * Get Config Object from cache
	 * 
	 * @param datasetId DatasetId 
	 * @param env		Environment short name 
	 * @param key	Key
	 * @return Config Config object
	 */
	@Override
	public Config getConfigFromCache(Integer datasetId, String env, String key) {

		logger.debug("Getting config from cache. Dataset Id : " + datasetId + " Env : " + env + " Key : " + key);

		Cache cache = getCache(CONFIG_CACHE + datasetId);
		if (null != cache) {

			Element element = cache.get(env + ENV_KEY_SEPARATOR + key);
			if (element != null) {
				return (Config) element.getObjectValue();
			}
		}

		return null;
	}

	/**
	 * Stores Config object to Cache
	 * 
	 * @param datasetId Dataset id
	 * @param configs List of Config objects to store
	 */
	@Override
	public void storeConfigToCache(Integer datasetId, List<Config> configs) {

		Cache cache = getCache(CONFIG_CACHE + datasetId);
		for (Config config : configs) {
			if (null != cache) {
				cache.put(getElement(config));
			}
		}
	}

	/**
	 * Stores config to Cache
	 * 
	 * @param datasetId Dataset id
	 * @param env Environment Short name
	 * @param config Config object 
	 */
	@Override
	public void storeConfigToCache(Integer datasetId, String env, Config config) {
		Cache cache = getCache(CONFIG_CACHE + datasetId);
		if (null != cache) {
			cache.put(getElement(config));
		}
	}

	private Element getElement(Config config) {
		logger.debug(" Dataset :" + config.getDataset().getDatasetid() + " key : " + config.getEnv().getSname() + ENV_KEY_SEPARATOR + config.getKey());
		return new Element(config.getEnv().getSname() + ENV_KEY_SEPARATOR + config.getKey(), config);
	}

}
