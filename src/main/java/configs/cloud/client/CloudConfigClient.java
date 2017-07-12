package configs.cloud.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import configs.cloud.client.entity.Config;
import configs.cloud.client.entity.Dataset;
import configs.cloud.client.entity.Env;
import configs.cloud.client.entity.EnvReturnFormat;
import configs.cloud.client.exceptions.ForbiddenException;
import configs.cloud.client.exceptions.NotFoundException;
import configs.cloud.client.exceptions.UnAuthorizedException;

public class CloudConfigClient {
	
	private Logger logger = LoggerFactory.getLogger(CloudConfigClient.class);

	private String apiKey;
	private String url;

	public CloudConfigClient(String apiKey, String url) {
		super();
		this.apiKey = apiKey;
		this.url = url;
	}

	public List<Config> getAllConfiguration(Integer datasetId) {

		List<Config> configs = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_ALL_CONFIGS, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configs = Arrays.asList(config);

		} catch (Exception e) {
			logger.error("Error occurred while getting configuration.", e);
		}
		return configs;
	}

	public List<Config> getAllConfigurationForEnvironment(Integer datasetId, String envsname) {

		List<Config> configs = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			parameters.put(Constant.ENV_SHORTNAME, envsname);

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			WebResource.Builder webResource = client
					.resource(
							replaceParametersOnURL(url + Constant.GET_ALL_CONFIGS_FOR_ENV, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);

			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configs = Arrays.asList(config);

		} catch (Exception e) {
			logger.error("Error occurred while getting configuration for environment.", e);
		}

		return configs;
	}

	public List<Config> getConfigurationByDatasetAndEnvironmentAndKey(Integer datasetId, String envsname, String key) {

		List<Config> configs = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			parameters.put(Constant.KEY, key);

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_CONFIGS_BY_DATASET_AND_ENV_AND_KEY, parameters)
							.toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configs = Arrays.asList(config);

		} catch (Exception e) {
			logger.error("Error occurred while getting configuration by dataset,environment and key.", e);
		}
		return configs;
	}

	public void updateConfigKey(Integer datasetId, String envsname, String key, String value) {

		try {
			Client client = Client.create();

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			parameters.put(Constant.KEY, key);

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(Constant.VALUE, value);

			WebResource.Builder webResource = client.resource(
					replaceParametersOnURL(url + Constant.UPDATE_VALUE_FOR_CONFIG_KEY, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).put(ClientResponse.class);

			if (response.getStatus() == 200 || response.getStatus() == 201) {
				 response.getStatus();
			} else if (response.getStatus() == 401) {
				throw new UnAuthorizedException("UnAuthorized");
			} else if (response.getStatus() == 403) {
				throw new ForbiddenException("Access denied");
			} else if (response.getStatus() == 404) {
				throw new NotFoundException("Resource requested was not found on the server");
			} else {
				throw new RuntimeException("Internal server error ");
			}

		} catch (Exception e) {
			logger.error("Error occurred while updating configuration key.", e);
		}

	}

	public void updateConfigEnabledStatusForEnv(Integer datasetId, String envsname, String key, String isenabled) {

		try {
			Client client = Client.create();

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));
			parameters.put(Constant.ENV_SHORTNAME, envsname);
			parameters.put(Constant.KEY, key);
			parameters.put(Constant.IS_ENABLED, isenabled);

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.UPDATE_CONFIG_ENABLED_STATUS_FOR_ENV, parameters)
							.toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).put(ClientResponse.class);

			if (response.getStatus() == 200 || response.getStatus() == 201) {
				 response.getStatus();
			} else if (response.getStatus() == 401) {
				throw new UnAuthorizedException("UnAuthorized");
			} else if (response.getStatus() == 403) {
				throw new ForbiddenException("Access denied");
			} else if (response.getStatus() == 404) {
				throw new NotFoundException("Resource requested was not found on the server");
			} else {
				throw new RuntimeException("Internal server error ");
			}

		} catch (Exception e) {
			logger.error("Error occurred while updating configuration status.", e);
		}

	}

	public List<Dataset> getAllDataset() {

		List<Dataset> datasets = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_ALL_DATASET, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);

			Dataset dataset[] = (Dataset[]) parseResponse(response, Dataset[].class);
			if (dataset.length > 0)
				datasets = Arrays.asList(dataset);

		} catch (Exception e) {
			logger.error("Error occurred while getting datasets.", e);
		}

		return datasets;
	}

	public List<Dataset> getDatasetByDatasetId(Integer datasetId) {

		List<Dataset> datasets = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.DATASETID, String.valueOf(datasetId));

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_DATASET_BY_DATASET, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			Dataset dataset[] = (Dataset[]) parseResponse(response, Dataset[].class);

			if (dataset.length > 0)
				datasets = Arrays.asList(dataset);

		} catch (Exception e) {
			logger.error("Error occurred while getting dataset by id.", e);
		}
		return datasets;
	}

	public EnvReturnFormat getAllEnvironment() {

		EnvReturnFormat environments = new EnvReturnFormat();
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_ALL_ENV, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			environments = (EnvReturnFormat) parseResponse(response, EnvReturnFormat.class);

		} catch (Exception e) {
			logger.error("Error occurred while getting environment.", e);
		}

		return environments;
	}

	public Env getEnvironmentByShortName(String sname) {

		Env environment = new Env();
		try {
			Client client = Client.create();

			Map<String, String> parameters = new HashMap<>();
			parameters.put(Constant.ENV_SHORTNAME, sname);

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.GET_ENV_BY_ENV, parameters).toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			environment = (Env) parseResponse(response, Env.class);

		} catch (Exception e) {
			logger.error("Error occurred while getting environment by shortname.", e);
		}
		return environment;
	}
	
	public List<Config> getConfigByRSQLSearch(String search) {


		List<Config> configs = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();

			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(Constant.SEARCH, search);
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.CONFIG_BY_RSQL_SEARCH, parameters)
							.toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configs = Arrays.asList(config);

		} catch (Exception e) {
			logger.error("Error occurred while getting configuration", e);
		}
		return configs;
	
		
	}
	
	public List<Config> getConfigByRSQLSearch(String search,String iqk) {


		List<Config> configs = new ArrayList<>(0);
		try {
			Client client = Client.create();
			Map<String, String> parameters = new HashMap<>();
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(Constant.SEARCH, search);
			queryParams.add(Constant.IQK, iqk);
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(url + Constant.CONFIG_BY_RSQL_SEARCH, parameters)
							.toASCIIString())
					.queryParams(queryParams).header(Constant.X_AUTH_TOKEN, apiKey);

			ClientResponse response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configs = Arrays.asList(config);

		} catch (Exception e) {
			logger.error("Error occurred while getting configuration", e);
		}
		return configs;
	
		
	}

	private Object parseResponse(ClientResponse response, Class<?> clazz) throws Exception {

		if (response.getStatus() == 200) {

			ObjectMapper mapper = new ObjectMapper();
			String output = response.getEntity(String.class);
			return mapper.readValue(output, clazz);

		} else if (response.getStatus() == 401) {
			throw new UnAuthorizedException("UnAuthorized");
		} else if (response.getStatus() == 403) {
			throw new ForbiddenException("Access denied");
		} else if (response.getStatus() == 404) {
			throw new NotFoundException("Resource requested was not found on the server");
		} else {
			throw new RuntimeException("Internal server error");
		}
	}

	private URI replaceParametersOnURL(String template, Map<String, String> parameters) {
		UriBuilder builder = UriBuilder.fromPath(template);
		URI output = builder.buildFromMap(parameters);
		return output;
	}
}