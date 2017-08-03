package configs.cloud.client.service;

import java.util.List;
import configs.cloud.client.entity.Config;

public interface CacheService {
		
	public List<Config> getConfigListFromCache(Integer datasetId);
	
	public List<Config> getConfigListFromCache(Integer datasetId, String env);
	
	public Config getConfigFromCache(Integer datasetId, String env, String key);
	
	public void storeConfigToCache(Integer datasetId, List<Config> configs);
	
	public void storeConfigToCache(Integer datasetId, String env, Config config);

}
