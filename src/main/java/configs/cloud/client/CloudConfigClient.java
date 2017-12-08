package configs.cloud.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.log4j.Logger;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import configs.cloud.client.entity.Config;
import configs.cloud.client.entity.Dataset;
import configs.cloud.client.entity.Env;
import configs.cloud.client.entity.EnvWrapper;
import configs.cloud.client.enums.CacheProvider;
import configs.cloud.client.exceptions.ContextNotFoundException;
import configs.cloud.client.exceptions.NotFoundException;
import configs.cloud.client.factory.CacheFactory;
import configs.cloud.client.service.CacheService;
import configs.cloud.client.util.ClientUtilities;

/**
 * 
 * @author Pushkar
 *
 */

public class CloudConfigClient {

	private static final Logger logger = Logger.getLogger(CloudConfigClient.class);

	private String apiKey;
	private String url;
	private boolean isCached = false;
	private String currentEnvironment;
	private Integer currentDataset = 0;
	private CacheService cacheService;
	private CacheProvider cacheProvider = CacheProvider.EHCACHE;

	/**
	 * 
	 * @param apiKey Api Key
	 * @param url Api endpoint
	 * @param isCached if cache enabled or disabled. <br>
	 * 					By default disabled
	 * @param cprovider CacheProvider name - Choose from EHCACHE | HAZELCAST
	 */
	public CloudConfigClient(String apiKey, String url, boolean isCached, CacheProvider cprovider) {

		super();
		logger.debug("Initializing cloud config client...");

		this.apiKey = apiKey;
		this.url = url;
		this.isCached = isCached;
		this.cacheProvider = cprovider;
		if (this.isCached) {

			if (this.cacheProvider == null) {
				this.cacheProvider = CacheProvider.EHCACHE;
			}

			cacheService = CacheFactory.getCacheService(cacheProvider);
			logger.debug("Cache is enabled. Creating Cache cache manager..");
			logger.debug("Cache cache manager created.");
		}

		logger.debug("Cloud config client initialized successfully.");
	}

	/**
	 * 
	 * @param apiKey Api Key
	 * @param url  Api endpoint
	 * @param isCached if cache enabled or disabled. By default disabled
	 * @param dataset  Current dataset
	 * @param environment Current environment
	 * @param cprovider CacheProvider name - Choose from EHCACHE | HAZELCAST          
	 */
	public CloudConfigClient(String apiKey, String url, boolean isCached, Integer dataset, String environment,
			CacheProvider cprovider) {

		super();
		logger.debug("Initializing cloud config client...");

		this.apiKey = apiKey;
		this.url = url;
		this.isCached = isCached;
		this.currentDataset = dataset;
		this.currentEnvironment = environment;
		this.cacheProvider = cprovider;
		if (this.isCached) {

			if (this.cacheProvider == null) {
				this.cacheProvider = CacheProvider.EHCACHE;
			}

			cacheService = CacheFactory.getCacheService(cacheProvider);
			logger.debug("Cache is enabled. Creating cache manager..");
			logger.debug("Cache manager created.");
		}

		logger.debug("Cloud config client initialized successfully.");
	}

	/**
	 * Returns Environment Short name under context
	 * 
	 * @return Environment Shortname
	 */
	public String getCurrentEnvironment() {
		return currentEnvironment;
	}

	/**
	 * Returns Dataset Id under context
	 * 
	 * @return Dataset Id
	 */
	public Integer getCurrentDataset() {
		return currentDataset;
	}

	/**
	 * Sets Default Datasetid and Environment ShortName in the context for all
	 * further API calls
	 * 
	 * @param datasetId DatasetId
	 * @param environment Environment Short name
	 */
	public void setClientDefaults(Integer datasetId, String environment) {
		this.currentDataset = datasetId;
		this.currentEnvironment = environment;
	}

	/**
	 * Gets all Configurations in the Default Dataset. Uses Default Dataset as
	 * <br>
	 * set by user. Before calling this function setClientDefaults(Integer <br>
	 * dataset, String environment ) should be called, otherwise an Exception
	 * <br>
	 * will be thrown.
	 * 
	 * @return {@link List}&lt;{@link Config}&gt; List of Configs
	 * @throws ContextNotFoundException Thrown when current dataset is not known
	 * @throws Exception Exception Any other exception
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
	 * @param datasetId Dataset Id
	 * @return {@link List}&lt;{@link Config}&gt; List of Config objects
	 * @throws Exception Exception
	 */
	public List<Config> getConfigs(Integer datasetId) throws Exception {

		if (datasetId == 0) {
			throw new ContextNotFoundException("Cannot identify current Dataset.");
		}

		List<Config> configs = new ArrayList<>();
		if (isCached) {
			configs = cacheService.getConfigListFromCache(datasetId);
		}

		// Note : For datasets having size 0, always hit url
		if (configs == null || configs.size() == 0) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS, apiKey);

			cacheService.storeConfigToCache(datasetId, configs);
		}

		return configs;
	}

	/**
	 * Gets all Configurations in the requested Dataset and Environment.
	 * 
	 * @param datasetId Dataset Id
	 * @param sname Environment Short name
	 * @return {@link List}&lt;{@link Config}&gt; List of Config Objects 
	 * @throws Exception Exception
	 */
	public List<Config> getConfigs(Integer datasetId, String sname) throws Exception {

		if (datasetId == 0) {
			throw new ContextNotFoundException("Cannot identify current Dataset.");
		}

		List<Config> configs = new ArrayList<>();
		if (isCached) {
			configs = cacheService.getConfigListFromCache(datasetId, sname);
		}

		// Note : For datasets having size 0, always hit url
		if (configs == null || configs.size() == 0) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			parameters.put(Constant.ENV_SHORTNAME, sname);
			configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS_FOR_ENV, apiKey);

			cacheService.storeConfigToCache(datasetId, configs);
		}

		return configs;
	}

	/**
	 * Get all configs specific to a key.<br>
	 * Use the setClientDefaults function to set the environment and dataset
	 * defaults
	 * 
	 * @param key Key 
	 * @return String Value Value
	 * @throws Exception Exception 
	 */
	public String getConfigValue(String key) throws Exception {

		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()) {
			throw new NotFoundException("Key Cannot be Null");

		}

		Config config = null;
		if (isCached) {
			config = cacheService.getConfigFromCache(currentDataset, currentEnvironment, key);
		}

		if (config == null) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
			parameters.put(Constant.KEY, key);

			List<Config> configs = ClientUtilities.getConfigCall(parameters, url,
					Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY, apiKey);

			if (configs != null && configs.size() > 0) {
				config = configs.get(0);
				cacheService.storeConfigToCache(currentDataset, currentEnvironment, config);
			}
		}

		return ((config != null) ? config.getValue() : null);
	}

	/**
	 * Get all configs specific to a key<br>
	 * Use the setClientDefaults function to set the environment and dataset
	 * defaults.<br>
	 * if key doesnt exist, then null is returned
	 * 
	 * @param key Key 
	 * @return Config Config Object
	 * @throws Exception Any Exception
	 */
	public Config getConfig(String key) throws Exception {

		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()) {
			throw new NotFoundException("Key Cannot be Null");
		}

		Config config = null;
		if (isCached) {
			config = cacheService.getConfigFromCache(currentDataset, currentEnvironment, key);
		}

		if (config == null) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
			parameters.put(Constant.KEY, key);

			List<Config> configs = ClientUtilities.getConfigCall(parameters, url,
					Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY, apiKey);

			if (configs != null && configs.size() > 0) {
				config = configs.get(0);
				cacheService.storeConfigToCache(currentDataset, currentEnvironment, config);
			}
		}

		return config;
	}

	/**
	 * Get the Config object for a key specific to another environment. <br>
	 * This environment value will override the environment value set in the
	 * setClientDefaults() <br>
	 * for the same dataset.<br>
	 * <br>
	 * 
	 * If Key doesnt exist for this environment, then - null - is returned.
	 * 
	 * @param envsname Environment Short name
	 * @param key Key
	 * @return Config Config object
	 * @throws Exception Any other Exception 
	 */
	public Config getConfig(String envsname, String key) throws Exception {

		if (currentDataset == 0 || envsname.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()) {
			throw new NotFoundException("Key Cannot be Null");
		}

		Config config = null;
		if (isCached) {
			config = cacheService.getConfigFromCache(currentDataset, envsname, key);
		}

		if (config == null) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			parameters.put(Constant.KEY, key);

			List<Config> configs = ClientUtilities.getConfigCall(parameters, url,
					Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY, apiKey);
			if (configs.size() > 0) {
				config = configs.get(0);
				cacheService.storeConfigToCache(currentDataset, envsname, config);
			}
		}

		return config;
	}

	/**
	 * Get All configurations for a given environment.<br>
	 * This environment value will override the environment value set in the
	 * setClientDefaults() <br>
	 * for the same dataset.
	 * 
	 * @param envsname Environment Short name
	 * @return {@link List}&lt;{@link Config}&gt; List of all Configurations
	 * @throws Exception Any other Exception 
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
		if (isCached) {
			configs = cacheService.getConfigListFromCache(currentDataset, envsname);
		}

		if (configs == null || configs.size() == 0) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
			parameters.put(Constant.ENV_SHORTNAME, envsname);

			configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS_FOR_ENV, apiKey);
			cacheService.storeConfigToCache(currentDataset, configs);
		}

		return configs;
	}

	/**
	 * Search for a set of configurations using a RSQL.<br>
	 * Use setClientDefaults() to set applicable environment and dataset.<br>
	 * Refer documentation for more details of the RSQL
	 * 
	 * @param searchQuery Search Query
	 * @return {@link List}&lt;{@link Config}&gt; List of Config Objects 
	 * @throws Exception Any other Exception 
	 */
	public List<Config> searchConfigs(String searchQuery) throws Exception {

		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		searchQuery = "key=="+searchQuery;
		
		List<Config> configs = ClientUtilities.searchConfigCall(searchQuery,String.valueOf(currentDataset.longValue()),currentEnvironment, false, url, Constant.CONFIG_BY_RSQL_SEARCH,
				apiKey);
		return configs;

	}

	/**
	 * Search for a set of configurations using a RSQL - with iqk - feature<br>
	 * iqk - or "ignore query key" allows for keys to be returned without the
	 * key in the query. <br>
	 * for e.g. if the query is for returning all keys which match the query =
	 * myapp.module.*, <br>
	 * like myapp.module.address.streetname, myapp.module.address.addresline1
	 * etc, will return keys<br>
	 * without the prefix - mayapp.module. In other words, the output keyset
	 * will be <br>
	 * address.streetname, address.addressline1 etc.<br>
	 * <br>
	 * 
	 * This flag allows to have same keys for e.g. name, address etc, to be used
	 * across multiple contexts.<br>
	 * Refer documentaiton for more information. <br>
	 * <br>
	 * 
	 * 
	 * Use setClientDefaults() to set applicable environment and dataset.<br>
	 * Refer documentation for more details of the RSQL<br>
	 * 
	 * @param searchQuery Search query. Refer documentation for more information on forming queries
	 * @param iqk Ignore Query Key, Ignores the query key part from the keys out of results. <br>
	 * 				This has been designed for accomodating the KeyPrefix, which allows to have duplicate<br>
	 * 				keys with separate prefixes. <br>
	 * 				for e.g. if template.ldap.*, when searched with key==template.*, returns only keys ldap.* <br>
	 * @return {@link List}&lt;{@link Config}&gt;  List of Config Objects 
	 * @throws Exception Any other Exception 
	 */ 
	public List<Config> searchConfigs(String searchQuery, boolean iqk) throws Exception {
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		
		searchQuery = "key=="+searchQuery;
		List<Config> configs = ClientUtilities.searchConfigCall(searchQuery,String.valueOf(currentDataset.longValue()),currentEnvironment, iqk, url, Constant.CONFIG_BY_RSQL_SEARCH,
				apiKey);
		return configs;
	}

	/**
	 * Updates the config with the value.<br>
	 * 
	 * @param key Key
	 * @param value Value
	 * @return boolean  Updated Status, as either true or false
	 * @throws Exception Any other Exception 
	 */
	public boolean updateConfig(String key, String value) throws Exception {

		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()) {
			throw new NotFoundException("Key Cannot be Null");
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
		parameters.put(Constant.KEY, key);

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(Constant.VALUE, value);

		Integer response = ClientUtilities.updateConfigCall(parameters, queryParams, url,
				Constant.UPDATE_VALUE_FOR_CONFIG_KEY, apiKey);
		boolean updateStatus = false;
		if (response == 200 || response == 201) {
			updateStatus = true;
		}

		return updateStatus;
	}

	/**
	 * Updates the config with isEnabled status. <br>
	 * Do note that the isEnabled status cannot be updated with the value of the
	 * config.
	 * 
	 * @param key Key
	 * @param isenabled isEnabled - true or false
	 * @return boolean - Update Status
	 * @throws Exception Any other Exception 
	 */
	public boolean updateConfig(String key, Character isenabled) throws Exception {

		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()) {
			throw new NotFoundException("Key Cannot be Null");
		} else if (String.valueOf(isenabled).equals("Y") == false && String.valueOf(isenabled).equals("N") == false) {
			throw new NotFoundException("isEnabled should be either of Y or N");
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, currentEnvironment);
		parameters.put(Constant.KEY, key);
		parameters.put(Constant.IS_ENABLED, String.valueOf(isenabled));

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		Integer response = ClientUtilities.updateConfigCall(parameters, queryParams, url,
				Constant.UPDATE_CONFIG_ENABLED_STATUS_FOR_ENV, apiKey);
		boolean updateStatus = false;
		if (response == 200 || response == 201) {
			updateStatus = true;
		}

		return updateStatus;

	}

	/**
	 * Returns list of all Datasets allocated to the user.<br>
	 * 
	 * @return {@link List}&lt;{@link Dataset}&gt; List of Dataset objects 
	 * @throws Exception Any other Exception 
	 */
	public List<Dataset> getDatasets() throws Exception {

		Map<String, String> parameters = new HashMap<>();
		List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_ALL_DATASET, apiKey);
		return datasets;
	}

	/**
	 * Returns a specific dataset with id provided. If a dataset with the id
	 * doenst exist, <br>
	 * null is returned.
	 * 
	 * @param datasetId Dataset Id
	 * @return Dataset  Returns the dataset context
	 * @throws Exception Any other Exception 
	 */
	public Dataset getDataset(Long datasetId) throws Exception {

		if (datasetId == 0) {
			throw new ContextNotFoundException("Invalid Dataset id");
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(datasetId));

		List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_DATASET_BY_DATASET,
				apiKey);

		if (datasets.size() > 0) {
			return datasets.get(0);
		}

		throw new ContextNotFoundException("Invalid Dataset id");
	}

	/**
	 * Returns the list of Environments available.<br>
	 * 
	 * @return {@link List}&lt;{@link Env}&gt; List of Environments for Current
	 *         user
	 * @throws Exception Any other Exception 
	 */
	public List<Env> getEnvironments() throws Exception {
		Map<String, String> parameters = new HashMap<>();
		EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ALL_ENV, apiKey, true);
		return envWrapper.getEnv();
	}

	/**
	 * Returns an environment with short name provided.<br>
	 * If the environment is not available with name provided, null is returned.
	 * 
	 * @param shortname Environment Short name
	 * @return Env Environment details for given shortname
	 * @throws Exception Any other Exception 
	 */
	public Env getEnvironment(String shortname) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.ENV_SHORTNAME, shortname);
		EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ENV_BY_ENV, apiKey, false);
		return (envWrapper.getEnv()).get(0);
	}

}