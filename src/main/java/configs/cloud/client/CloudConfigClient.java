package configs.cloud.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

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


public class CloudConfigClient {

	private String apiKey;
	private String url;
	private boolean isEhcache;
	private boolean isEhcacheResult=true;

	private String currentEnvironment;
	private Integer currentDataset = 0;
	
	CacheManager cm = CacheManager.getInstance();
	Cache cache = cm.getCache("cloud_cache");
	public CloudConfigClient(String apiKey, String url, boolean isEhcache) {
		super();
		this.apiKey = apiKey;
		this.url = url;
		this.isEhcache = isEhcache;
	}
	public CloudConfigClient(String apiKey, String url,boolean isEhcache, Integer dataset, String environment) {
		super();
		this.apiKey = apiKey;
		this.url = url;
		this.isEhcache = isEhcache;
		this.currentDataset = dataset;
		this.currentEnvironment = environment;
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
	 * Gets all Configurations in the Default Dataset. Uses Default Dataset as
	 * set by user. Before calling this function setClientDefaults(Integer
	 * dataset, String environment ) should be called, otherwise an Exception
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
	@SuppressWarnings("unchecked")
	public List<Config> getConfigs(Integer datasetId) throws Exception {
		
		if (datasetId == 0) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset.");
		}
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(datasetId));
		
		Element ele = cache.get(Constant.EHCACHE_GET_CONFIGS+datasetId);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_CONFIGS+datasetId)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS, apiKey);
			cache.put(new Element(Constant.EHCACHE_GET_CONFIGS+datasetId,configs));
			ele = cache.get(Constant.EHCACHE_GET_CONFIGS+datasetId);
			return configs;
		}
		else {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS, apiKey);	
			return configs;					
		}
		
	}


	/**
	 * Get all configs specific to a key
	 * Use the setClientDefaults function to set the environment and dataset defaults
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Config> getConfigs(String key) throws Exception {
		
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
		
		Element ele = cache.get(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
					apiKey);			cache.put(new Element(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key,configs));
			ele = cache.get(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key);
			return configs;
		}
		else {				
		List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
				apiKey);
		
		return configs;
		}

	}

	/**
	 * Get the value of a key specific to another environment. 
	 * This environment value will override the environment value set in the setClientDefaults() 
	 * for the same dataset 
	 * 
	 * @param envsname
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Config> getConfigs(String envsname, String key) throws Exception {
		
		if (currentDataset == 0 || envsname.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (key.isEmpty()){
			throw new NotFoundException(
					"Key Cannot be Null");
		
		}
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, envsname);
		parameters.put(Constant.KEY, key);

		Element ele = cache.get(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key+envsname);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key+envsname)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
					apiKey);
			cache.put(new Element(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key+envsname,configs));
			ele = cache.get(Constant.EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY+key+envsname);
			return configs;
		}
		else {

		List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY,
				apiKey);

		return configs;
		}

	}
	
	/**
	 * Get All configurations for a given environment.
	 * This environment value will override the environment value set in the setClientDefaults() 
	 * for the same dataset 
	 * 
	 * @param envsname
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Config> getConfigsForEnv(String envsname) throws Exception {
		
		if (currentDataset == 0) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		} else if (envsname.isEmpty()) {
			throw new ContextNotFoundException(
					"Environment cannot be null. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		

		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(currentDataset));
		parameters.put(Constant.ENV_SHORTNAME, envsname);

		Element ele = cache.get(Constant.EHCACHE_GET_ALL_CONFIGS_FOR_ENV+envsname);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_ALL_CONFIGS_FOR_ENV+envsname)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS_FOR_ENV, apiKey);
			cache.put(new Element(Constant.EHCACHE_GET_ALL_CONFIGS_FOR_ENV+envsname,configs));
			ele = cache.get(Constant.EHCACHE_GET_ALL_CONFIGS_FOR_ENV+envsname);
			return configs;
		}
		else {
			List<Config> configs = ClientUtilities.getConfigCall(parameters, url, Constant.GET_ALL_CONFIGS_FOR_ENV, apiKey);

		return configs;
		}
	}

	/**
	 * Search for a set of configurations using a RSQL.
	 * Use setClientDefaults() to set applicable environment and dataset.
	 * Refer documentation for more details of the RSQL
	 * 
	 * @param searchQuery
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Config> searchConfig(String searchQuery) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		
		Element ele = cache.get(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {	
			List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, false, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
			cache.put(new Element(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery,configs));
			ele = cache.get(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery);
			return configs;
		}
		else {
			List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, false, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
		return configs;
		
		}
	}
	
	
	/**
	 * Search for a set of configurations using a RSQL - with iqk - feature
	 * iqk - or "ignore query key" allows for keys to be returned without the key in the query. 
	 * for e.g. if the query is for returning all keys which match the query = myapp.module.*, 
	 * like myapp.module.address.streetname, myapp.module.address.addresline1 etc, will return keys
	 * without the prefix - mayapp.module. In other words, the output keyset will be 
	 * address.streetname, address.addressline1 etc.
	 * 
	 * This flag allows to have same keys for e.g. name, address etc, to be used across multiple contexts.
	 * Refer documentaiton for more information. 
	 * 
	 * 
	 * Use setClientDefaults() to set applicable environment and dataset.
	 * Refer documentation for more details of the RSQL
	 * 
	 * @param searchQuery
	 * @param iqk
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Config> searchConfig(String searchQuery, boolean iqk) throws Exception {
		
		if (currentDataset == 0 || currentEnvironment.isEmpty()) {
			throw new ContextNotFoundException(
					"Cannot identify current Dataset or Environment. Recommendation: Call setClientDefaults to set current dataset and environment.");
		}
		
		Element ele = cache.get(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery+iqk);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery+iqk)){

				return (List<Config>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, iqk, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
			cache.put(new Element(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery+iqk,configs));
			ele = cache.get(Constant.EHCACHE_CONFIG_BY_RSQL_SEARCH+searchQuery+iqk);
			return configs;
		}
		else {
		
		List<Config> configs = ClientUtilities.searchConfigCall(searchQuery, iqk, url, Constant.CONFIG_BY_RSQL_SEARCH, apiKey);
		return configs;
		}

	}
	
	/**
	 * Updates the config with the value.
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
	 * Updates the config with isEnabled status. 
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
		} else if (String.valueOf(isenabled).equals("Y") == false || String.valueOf(isenabled).equals("N") == false){
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
	 * Returns lits of all Datasets allocated to the user.
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Dataset> getDatasets() throws Exception {

		Map<String, String> parameters = new HashMap<>();
		
		Element ele = cache.get(Constant.EHCACHE_GET_ALL_DATASET);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_ALL_DATASET)){

				return (List<Dataset>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
	
			List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_ALL_DATASET,
					apiKey);			
			cache.put(new Element(Constant.EHCACHE_GET_ALL_DATASET,datasets));
			ele = cache.get(Constant.EHCACHE_GET_ALL_DATASET);
			return datasets;
		}
		else {
		
		List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_ALL_DATASET,
				apiKey);

		return datasets;
		}
	}
	
	/**
	 * Returns a specific dataset with id provided. If a dataset with the id doenst exist, 
	 * null is returned.
	 * 
	 * @param datasetId
	 * @return
	 * @throws Exception
	 */
	public Dataset getDataset(Integer datasetId) throws Exception {
		
		if (datasetId == 0) {
			throw new ContextNotFoundException(
					"Invalid Dataset id");
		}
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.DATASETID, String.valueOf(datasetId));
		Element ele = cache.get(Constant.EHCACHE_GET_DATASET_BY_DATASET+datasetId);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_DATASET_BY_DATASET+datasetId)){

				return (Dataset) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			
			List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_DATASET_BY_DATASET,
					apiKey);			
			cache.put(new Element(Constant.EHCACHE_GET_DATASET_BY_DATASET+datasetId,datasets));
			ele = cache.get(Constant.EHCACHE_GET_DATASET_BY_DATASET+datasetId);
			return datasets.get(0);
		}
		else {				
			List<Dataset> datasets = ClientUtilities.getDatasetCall(parameters, url, Constant.GET_DATASET_BY_DATASET,
					apiKey);
			return datasets.get(0);
		}
	}


	/**
	 * Returns the list of Environments available.
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Env> getEnvironments() throws Exception {

		Map<String, String> parameters = new HashMap<>();
		Element ele = cache.get(Constant.EHCACHE_GET_ALL_ENV);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_ALL_ENV)){

				return (List<Env>) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			
			EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ALL_ENV,
					apiKey, true);		
			cache.put(new Element(Constant.EHCACHE_GET_ALL_ENV,envWrapper.getEnv()));
			ele = cache.get(Constant.EHCACHE_GET_ALL_ENV);
			return envWrapper.getEnv();
		}
		else {		
		
			EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ALL_ENV,
					apiKey, true);
			return envWrapper.getEnv();
		}
	}
	
	/**
	 * Returns an environment with short name provided.
	 * If the environment is not available with name provided, null is returned.
	 * 
	 * @param sname
	 * @return
	 * @throws Exception
	 */
	public Env getEnvironment(String sname) throws Exception {
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constant.ENV_SHORTNAME, sname);
		
		Element ele = cache.get(Constant.EHCACHE_GET_ENV_BY_ENV+sname);
		if(isEhcache){
			if(cache.isKeyInCache(Constant.EHCACHE_GET_ENV_BY_ENV+sname)){

				return (Env) ele.getObjectValue();
			}
			else {
				isEhcacheResult=false;
			}
		}		
		
		if(isEhcache && !isEhcacheResult ) {
			EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ENV_BY_ENV,
					apiKey, false);		
			cache.put(new Element(Constant.EHCACHE_GET_ENV_BY_ENV+sname,(envWrapper.getEnv()).get(0)));
			ele = cache.get(Constant.EHCACHE_GET_ENV_BY_ENV+sname);
			return (envWrapper.getEnv()).get(0);
		}
		else {		
		
		EnvWrapper envWrapper = ClientUtilities.getEnvCall(parameters, url, Constant.GET_ENV_BY_ENV,
				apiKey, false);

		return (envWrapper.getEnv()).get(0);
		}
	} 

}