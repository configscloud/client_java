package configs.cloud.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import configs.cloud.client.entity.Config;
import configs.cloud.client.entity.Dataset;
import configs.cloud.client.entity.Env;
import configs.cloud.client.entity.EnvWrapper;
import configs.cloud.client.exceptions.ContextNotFoundException;
import configs.cloud.client.exceptions.NotFoundException;
import configs.cloud.client.util.ClientUtilities;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * 
 * @author Pushkar
 *
 */
public class CloudConfigClient {
	
	private static final Logger logger = Logger.getLogger(CloudConfigClient.class);	
	private static final String CONFIG_CACHE = "config_cache_";
	private static final String ENV_KEY_SEPARATOR = ":";
	
	private String apiKey;
	private String url;
	private boolean isCached = false;				
	private String currentEnvironment;
	private Integer currentDataset = 0;	
	private CacheManager cm = null;	
	
	/**
	 * 
	 * @param apiKey Api Key
	 * @param url Api endpoint
	 * @param isCached if cache enabled or disabled. By default disabled
	 */
	public CloudConfigClient(String apiKey, String url, boolean isCached) {
		
		super();		
		logger.debug("Initializing cloud config client...");					
		
		this.apiKey = apiKey;
		this.url = url;				
		this.isCached = isCached;
		
		if(this.isCached) {		
			logger.debug("Cache is enabled. Creating Cache cache manager..");				
			cm = CacheManager.getInstance();
			logger.debug("Cache cache manager created.");
		}
		
		logger.debug("Cloud config client initialized successfully.");
	}
	
	/**
	 * 
	 * @param apiKey Api Key
	 * @param url Api endpoint
	 * @param isCached if cache enabled or disabled. By default disabled
	 * @param dataset Current dataset
	 * @param environment Current environment
	 */
	public CloudConfigClient(String apiKey, String url,boolean isCached, Integer dataset, String environment) {
		
		super();
		logger.debug("Initializing cloud config client...");
		
		this.apiKey = apiKey;
		this.url = url;
		this.isCached = isCached;
		this.currentDataset = dataset;
		this.currentEnvironment = environment;
		
		if(this.isCached) {
			logger.debug("Cache is enabled. Creating cache manager..");				
			cm = CacheManager.getInstance();
			logger.debug("Cache manager created.");
		}
		
		logger.debug("Cloud config client initialized successfully.");
	}

	public String getCurrentEnvironment() {
		return currentEnvironment;
	}

	public Integer getCurrentDataset() {
		return currentDataset;
	}

	public void setClientDefaults(Integer dataset, String environment) {
		this.currentDataset = dataset;
		this.currentEnvironment = environment;
	}

	/**
	 * Get cache with particular name. Create one if not available and return.
	 * @param name
	 * @return
	 */
	private Cache getCache(String name) {
		
		logger.debug("Cache enabeld : " + this.isCached);
		
		if(isCached) {	
			
			logger.debug("Getting cache : " + name);
			Cache cache = cm.getCache(name);
			
			if(null == cache) {
				
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
		
		return null;
	}
	
	/**
	 * Gets all Configurations in the Default Dataset. Uses Default Dataset as <br/>
	 * set by user. Before calling this function setClientDefaults(Integer <br/>
	 * dataset, String environment ) should be called, otherwise an Exception <br/>
	 * will be thrown.
	 * 
	 * @return
	 * @throws ConfigsClientException
	 */
	public List<Config> getConfigs() throws ContextNotFoundException, Exception {
		if (currentDataset == 0) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		return getConfigs(currentDataset);
	}

	/**
	 * Gets all Configurations in the requested Dataset.
	 * 
	 * @param datasetId
	 * @return
	 */
	public List<Config> getConfigs(Integer datasetId) throws Exception {
		
		if (datasetId == 0) {
			throw new ContextNotFoundException("Cannot identify current Dataset.");
		}		
						
		List<Config> configs = new ArrayList<>();
		if(isCached) {
			configs = getConfigListFromCache(datasetId);			
		}
		
		// Note : For datasets having size 0, always hit url
		if(configs == null || configs.size() == 0) {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS,
					apiKey);
			
			storeConfigToCache(datasetId, configs);
		}			
		
		return configs;				
	}

	/**
	 * Get all configs specific to a key.<br/>
	 * Use the setClientDefaults function to set the environment and dataset defaults
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */	
    public String getConfigValue(String key) throws Exception {
    	
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException("Key Cannot be Null");
		
		}
								
		Config config = null;
		if(isCached) {			
			config = getConfigFromCache(currentDataset, currentEnvironment, key);			
		}
		
		if(config == null) {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
			parameters.put(Constant.KEY, key);
			
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
					apiKey);
			
			if(configs != null && configs.size() > 0){				
				config = configs.get(0);
				storeConfigToCache(currentDataset, currentEnvironment, config);
			}
		}
		
		return ((config != null) ? config.getValue() : null);		
	}
	
	/**
	 * Get all configs specific to a key<br/>
	 * Use the setClientDefaults function to set the environment and dataset defaults.<br/>
	 * if key doesnt exist, then null is returned
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Config getConfig(String key) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException(
					"Key Cannot be Null");
		}		
		
		Config config = null;
		if(isCached) {
			config = getConfigFromCache(currentDataset, currentEnvironment, key);
		}
		
		if(config == null) {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
			parameters.put(Constant.KEY, key);
	
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
					apiKey);
			
			if(configs != null && configs.size() > 0){				
				config = configs.get(0);
				storeConfigToCache(currentDataset, currentEnvironment, config);
			}		
		}
		
		return config;
	}
	/**
	 * Get the Config object for a key specific to another environment. <br/>
	 * This environment value will override the environment value set in the setClientDefaults() <br/>
	 * for the same dataset.<br/><br/>
	 * 
	 * If Key doesnt exist for this environment, then - null - is returned.
	 * 
	 * @param envsname
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Config getConfig(String envsname, String key) throws Exception {
				
		if (currentDataset == 0 || envsname.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException("Key Cannot be Null");		
		}
		
		Config config = null;
		if(isCached) {
			config = getConfigFromCache(currentDataset, envsname, key);
		}
		
		if(config == null) {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			parameters.put(Constant.KEY, key);
			
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
					apiKey);
			if (configs.size() > 0) {
				config = configs.get(0);
				storeConfigToCache(currentDataset, envsname, config);
			}
		}
		
		return config;
	}
	
	/**
	 * Get All configurations for a given environment.<br/>
	 * This environment value will override the environment value set in the setClientDefaults() <br/>
	 * for the same dataset.
	 * 
	 * @param envsname
	 * @return
	 * @throws Exception
	 */
	public List<Config> getConfigs(String envsname) throws Exception {
		
		if (currentDataset == 0) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (envsname.isEmpty()) {
			throw new ContextNotFoundException(
					"Environment cannot be null. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		
		List<Config> configs = new ArrayList<>();		
		if(isCached) {
			configs = getConfigListFromCache(currentDataset, envsname);
		}
		
		if(configs == null || configs.size() == 0) {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			
			configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS_FOR_ENV, apiKey);		
			storeConfigToCache(currentDataset, configs);
		}
		
		return configs;
	}

	/**
	 * Search for a set of configurations using a RSQL.<br/>
	 * Use setClientDefaults() to set applicable environment and dataset.<br/>
	 * Refer documentation for more details of the RSQL
	 * 
	 * @param searchQuery
	 * @return
	 * @throws Exception
	 */
	public List<Config> searchConfigs(String searchQuery) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}			
		
		List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, false, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
		return configs;
		
	}
	
	
	/**
	 * Search for a set of configurations using a RSQL - with iqk - feature<br/>
	 * iqk - or "ignore query key" allows for keys to be returned without the key in the query. <br/>
	 * for e.g. if the query is for returning all keys which match the query = myapp.module.*, <br/>
	 * like myapp.module.address.streetname, myapp.module.address.addresline1 etc, will return keys<br/>
	 * without the prefix - mayapp.module. In other words, the output keyset will be <br/>
	 * address.streetname, address.addressline1 etc.<br/><br/>
	 * 
	 * This flag allows to have same keys for e.g. name, address etc, to be used across multiple contexts.<br/>
	 * Refer documentaiton for more information. <br/><br/>
	 * 
	 * 
	 * Use setClientDefaults() to set applicable environment and dataset.<br/>
	 * Refer documentation for more details of the RSQL<br/>
	 * 
	 * @param searchQuery
	 * @param iqk
	 * @return
	 * @throws Exception
	 */
	public List<Config> searchConfigs(String searchQuery, boolean iqk) throws Exception {		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}						
		List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, iqk, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
		return configs;
	}
	
	/**
	 * Updates the config with the value.<br/>
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean updateConfig(String key, String value) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException(
					"Key Cannot be Null");
		}
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
		parameters.put(Constant.KEY, key);

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(Constant.VALUE, value);
		
		Integer response = ClientUtilities.updateConfigCall(parameters, queryParams, url, Constant.UPDATE_VALUE_FOR_CONFIG_KEY, apiKey);
		boolean updateStatus = false;
		if (response == 200 || response == 201){
			updateStatus = true;
		}
		
		return updateStatus;
	}
	
	/**
	 * Updates the config with isEnabled status. <br/>
	 * Do note that the isEnabled status cannot be updated with the value of the config.
	 * 
	 * @param key
	 * @param isenabled
	 * @return
	 * @throws Exception
	 */
	public boolean updateConfig(String key, Character isenabled) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException(
					"Key Cannot be Null");
		} else if (String.valueOf(isenabled).equals("Y") == false && String.valueOf(isenabled).equals("N") == false){
			throw new NotFoundException(
					"isEnabled should be either of Y or N");
		}
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
		parameters.put(Constant.KEY, key);
		parameters.put(Constant.IS_ENABLED, String.valueOf(isenabled));

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		
		Integer response = ClientUtilities.updateConfigCall(parameters, queryParams, url, Constant.UPDATE_CONFIG_ENABLED_STATUS_FOR_ENV, apiKey);
		boolean updateStatus = false;
		if (response == 200 || response == 201){
			updateStatus = true;
		}
		
		return updateStatus;

	}

	/**
	 * Returns list of all Datasets allocated to the user.<br/>
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Dataset> getDatasets() throws Exception {

		Map<String, String> parameters = new HashMap<>();
		List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_ALL_DATASET,
				apiKey);
		return datasets;
	}	
	
	/**
	 * Returns a specific dataset with id provided. If a dataset with the id doenst exist, <br/>
	 * null is returned.
	 * 
	 * @param datasetId
	 * @return
	 * @throws Exception
	 */
	public Dataset getDataset(Long datasetId) throws Exception {
		
		if (datasetId == 0) {
			throw new ContextNotFoundException(
					"Invalid Dataset id");
		}
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(datasetId));
		
		List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_DATASET_BY_DATASET,
				apiKey);
		
		if(datasets.size() > 0) {
			return datasets.get(0);
		}
		
		throw new ContextNotFoundException(
				"Invalid Dataset id");
	}
	

	/**
	 * Returns the list of Environments available.<br/>
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Env> getEnvironments() throws Exception {
		Map<String, String> parameters = new HashMap<>();
			EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ALL_ENV,
					apiKey, true);
			return envWrapper.getEnv();
	}
	
	/**
	 * Returns an environment with short name provided.<br/>
	 * If the environment is not available with name provided, null is returned.
	 * 
	 * @param sname
	 * @return
	 * @throws Exception
	 */
	public Env getEnvironment(String sname) throws Exception {		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.ENV_SHORTNAME, sname);				
		EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ENV_BY_ENV,
				apiKey, false);
		return (envWrapper.getEnv()).get(0);
	}	
	
	
	/** ================= **/
	/** PRIVATE METHODS   **/
	/** ================= **/
	
	/**
	 * 
	 * @return
	 */
	private List<Config> getConfigListFromCache(Integer datasetId) {
		
		logger.debug("Getting config from cache. Dataset Id : " + datasetId);
		
		Cache cache = getCache(CONFIG_CACHE + datasetId);		
		if(null != cache) {				
		
			List<Config> configs = new ArrayList<>();			
			Iterator<Object> it = cache.getKeys().iterator();
			
			while(it.hasNext()) {
				Object key = it.next();
				Element element = cache.get(key);
				configs.add( (Config) element.getObjectValue());
			}		
			return configs;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	private List<Config> getConfigListFromCache(Integer datasetId, String env) {
		
		logger.debug("Getting config from cache. Dataset Id : " + datasetId + " Env : " + env);
		
		Cache cache = getCache(CONFIG_CACHE + datasetId);		
		if(null != cache) {		
			
			List<Config> configs = new ArrayList<>();			
			Iterator<Object> it = cache.getKeys().iterator();
			
			while(it.hasNext()) {
				
				Object key = it.next();				
				if( ((String)key).startsWith(env + ":")) {	
					
					Element element = cache.get(key);
					configs.add( (Config) element.getObjectValue());
				}
			}		
			return configs;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param datasetId
	 * @param env
	 * @param key
	 * @return
	 */
	private Config getConfigFromCache(Integer datasetId, String env, String key) {
		
		logger.debug("Getting config from cache. Dataset Id : " + datasetId + " Env : " + env + " Key : " + key);
		
		Cache cache = getCache(CONFIG_CACHE + datasetId);		
		if(null != cache) {			
					
			Element element = cache.get(env + ENV_KEY_SEPARATOR + key);
			if(element != null) {
				return (Config) element.getObjectValue();
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param datasetId
	 * @param configs
	 */
	private void storeConfigToCache(Integer datasetId, List<Config> configs) {
		
		Cache cache = getCache(CONFIG_CACHE + datasetId);
		for(Config config : configs) {			
			if(null != cache) {
				cache.put(getElement(config));
			}
		}
	}
	
	/**
	 * 
	 * @param config
	 */
	private void storeConfigToCache(Integer datasetId, String env, Config config) {
		Cache cache = getCache(CONFIG_CACHE + datasetId);
		if(null != cache) {
			cache.put(getElement(config));			
		}		
	}
	
	private Element getElement(Config config) {
		return new Element(config.getEnv().getSname() + ENV_KEY_SEPARATOR + config.getDataset().getDatasetid(), config);
	} 
}