package configs.cloud.client;

public final class Constant {

	
	public static final String X_AUTH_TOKEN = "X-Auth-Token";
	public static final String ACCEPT = "application/json";
	public static final String DATASETID = "datasetid";
	public static final String ENV_SHORTNAME = "envsname";
	public static final String KEY = "key";
	public static final String VALUE= "value";
	public static final String IS_ENABLED = "isenabled";
	public static final String SEARCH = "search";
	public static final String IQK = "iqk";
	
	public static final String GET_ALL_CONFIGS = "/api/configs/{datasetid}";
	public static final String GET_ALL_CONFIGS_FOR_ENV = "/api/configs/{datasetid}/{envsname}";
	public static final String UPDATE_VALUE_FOR_CONFIG_KEY = "/api/configs/{datasetid}/{envsname}/{key}";	
	public static final String GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY = "/api/configs/{datasetid}/{envsname}/{key}";
	public static final String GET_ALL_DATASET = "/api/dataset/all";
	public static final String GET_DATASET_BY_DATASET = "/api/dataset/{datasetid}";
	public static final String GET_ALL_ENV = "/api/env";
	public static final String GET_ENV_BY_ENV = "/api/env/{envsname}";
	public static final String UPDATE_CONFIG_ENABLED_STATUS_FOR_ENV = "/api/configs/{datasetid}/{envsname}/{key}/{isenabled}";
	public static final String CONFIG_BY_RSQL_SEARCH="/api/configs/parser";
	
	
	public static final String EHCACHE_GET_ALL_CONFIGS = "EHCACHE_GET_ALL_CONFIGS";
	public static final String EHCACHE_GET_ALL_CONFIGS_FOR_ENV = "EHCACHE_GET_ALL_CONFIGS_FOR_ENV";
	public static final String EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY = "EHCACHE_GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY";
	public static final String EHCACHE_GET_ALL_DATASET = "EHCACHE_GET_ALL_DATASET";
	public static final String EHCACHE_GET_DATASET_BY_DATASET = "EHCACHE_GET_DATASET_BY_DATASET";
	public static final String EHCACHE_GET_ALL_ENV = "EHCACHE_GET_ALL_ENV";
	public static final String EHCACHE_GET_ENV_BY_ENV = "EHCACHE_GET_ENV_BY_ENV";
	public static final String EHCACHE_CONFIG_BY_RSQL_SEARCH="EHCACHE_CONFIG_BY_RSQL_SEARCH";
	public static final String EHCACHE_GET_CONFIGS = "EHCACHE_ALL_CONFIGS";
	
	
	

}