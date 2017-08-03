package configs.cloud.client.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import configs.cloud.client.CloudConfigClient;
import configs.cloud.client.entity.Config;
import configs.cloud.client.service.CacheService;

public class HazelCastService implements CacheService {

	private static final Logger logger = Logger.getLogger(CloudConfigClient.class);	
	private static final String CONFIG_CACHE = "config_cache_";
	private static final String ENV_KEY_SEPARATOR = ":";
	
	private com.hazelcast.config.Config cfg = null;
	private HazelcastInstance instance = null;
	
	public HazelCastService() {
		
		logger.debug("Initializing Hazel Cast Service...");		
		
		cfg = new com.hazelcast.config.Config();				
		instance = Hazelcast.newHazelcastInstance(cfg);
		
		logger.debug("Hazel Cast Service initializied.");	
	}

	public Map<String, Config> getCache(String name) {

		logger.debug("Getting cache : " + name);
		
		if(cfg.getMapConfig(name) == null) {
			
			MapConfig mapConfig = new MapConfig();			
			mapConfig.setName(name);			
			mapConfig.setTimeToLiveSeconds(20);
			mapConfig.setEvictionPolicy(EvictionPolicy.LFU);			
	        cfg.getMapConfigs().put(name, mapConfig);
		}	    
		
		Map<String, Config> cacheMap = instance.getMap(name);
		return cacheMap;		
	}

	@Override
	public List<Config> getConfigListFromCache(Integer datasetId) {

		logger.debug("Getting config from cache. Dataset Id : " + datasetId);
		
		Map<String, Config> cacheMap = getCache(CONFIG_CACHE + datasetId);		
		List<Config> configs = (List<Config>) cacheMap.values();
		
		if(configs.size() > 0) {
			return configs;
		}
		
		return null;
	}

	@Override
	public List<Config> getConfigListFromCache(Integer datasetId, String env) {
		
		Map<String, Config> cacheMap = getCache(CONFIG_CACHE + datasetId);		
		List<Config> configs = new ArrayList<>();
		
		Iterator<String> keys = cacheMap.keySet().iterator();
		if(keys.hasNext()) {
			String key = keys.next();
			if(key.startsWith(env + ":")) {
				configs.add(cacheMap.get(key));
			}
		}
		
		if(configs.size() > 0){
			return configs;
		}		
		return null;
	}

	@Override
	public Config getConfigFromCache(Integer datasetId, String env, String key) {
		
		Map<String, Config> cacheMap = getCache(CONFIG_CACHE + datasetId);
		return cacheMap.get(env + ENV_KEY_SEPARATOR + key);
	}

	@Override
	public void storeConfigToCache(Integer datasetId, List<Config> configs) {
		Map<String, Config> cacheMap = getCache(CONFIG_CACHE + datasetId);		
		for (Config config : configs) {
			if (null != cfg) {
				addConfigToCache(cacheMap, config);
			}
		}
	}

	@Override
	public void storeConfigToCache(Integer datasetId, String env, Config config) {
		
		Map<String, Config> cacheMap = getCache(CONFIG_CACHE + datasetId);
		addConfigToCache(cacheMap, config);
	}

	private void addConfigToCache(Map<String, Config> cacheMap, Config config) {
		
		logger.debug(" Dataset :" + config.getDataset().getDatasetid() + " key : " + 
				config.getEnv().getSname() + ENV_KEY_SEPARATOR + config.getKey());
		cacheMap.put(config.getEnv().getSname() + ENV_KEY_SEPARATOR + config.getKey(), config);
	} 
}
