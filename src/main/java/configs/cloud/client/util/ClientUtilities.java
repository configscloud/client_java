package configs.cloud.client.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import configs.cloud.client.Constant;
import configs.cloud.client.entity.Config;
import configs.cloud.client.entity.Dataset;
import configs.cloud.client.entity.Env;
import configs.cloud.client.entity.EnvWrapper;
import configs.cloud.client.exceptions.ForbiddenException;
import configs.cloud.client.exceptions.NotFoundException;
import configs.cloud.client.exceptions.UnAuthorizedException;

public class ClientUtilities {
	
	private static final Logger logger = Logger.getLogger(ClientUtilities.class);

	public static List<Config> getConfigCall(Map<String, String> parameters, String url, String queryApi,
			String apiKey) throws Exception {
		List<Config> configsList = new ArrayList<>(0);
		try {

			// add query params = empty
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			// call Server
			ClientResponse response = makeGetCall(url + queryApi, parameters, queryParams, apiKey);

			// Format response as Config Objects List
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configsList = Arrays.asList(config);

		} catch (Exception e) {
			throw e;
		}
		return configsList;
	}

	public static List<Config> searchConfigCall(String searchQuery, boolean iqkFlag, String url, String queryApi,
			String apiKey) throws Exception {
		List<Config> configsList = new ArrayList<>(0);
		Map<String, String> parameters = new HashMap<>();

		try {

			// add query params
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add(Constant.SEARCH, searchQuery);
			if (iqkFlag) 
				queryParams.add(Constant.IQK, "Y");

			// Call server
			ClientResponse response = makeGetCall(url + queryApi, parameters, queryParams, apiKey);

			// Format response as Config Objects List
			Config config[] = (Config[]) parseResponse(response, Config[].class);
			if (config.length > 0)
				configsList = Arrays.asList(config);

		} catch (Exception e) {
			throw e;
		}
		return configsList;
	}
	
	public static Integer updateConfigCall(Map<String, String> parameters, MultivaluedMap<String, String> queryParams, String url, String queryApi,
			String apiKey) throws Exception {
		
		ClientResponse response = null;
		try {

			// Call server
			response = makePutCall(url + queryApi, parameters, queryParams, apiKey);
			
		} catch (Exception e) {
			throw e;
		}
		return response.getStatus();
	}
	
	public static List<Dataset> getDatasetCall(Map<String, String> parameters, String url, String queryApi,
			String apiKey) throws Exception {
		List<Dataset> datasets = new ArrayList<>(0);
		try {

			// add query params = empty
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			// call Server
			ClientResponse response = makeGetCall(url + queryApi, parameters, queryParams, apiKey);

			// Format response as Config Objects List
			Dataset[] dataset = (Dataset[]) parseResponse(response, Dataset[].class);
			if (dataset.length > 0)
				datasets = Arrays.asList(dataset);

		} catch (Exception e) {
			throw e;
		}
		return datasets;
	}

	public static EnvWrapper getEnvCall(Map<String, String> parameters, String url, String queryApi,
			String apiKey, boolean getWrapper) throws Exception {
		
		EnvWrapper envWrapper =  new EnvWrapper();
		Env environment = new Env();
		try {

			// add query params = empty
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

			// call Server
			ClientResponse response = makeGetCall(url + queryApi, parameters, queryParams, apiKey);

			if (getWrapper) {
				envWrapper = (EnvWrapper) parseResponse(response, EnvWrapper.class);
			} else {
				environment = (Env) parseResponse(response, Env.class);
				List<Env> envList = (new ArrayList<Env>(1));
				envList.add(environment);
				envWrapper.setEnv(envList);
			}
			
		} catch (Exception e) {
			throw e;
		}
		return envWrapper;
	}

	
	/** ============================= **/
	/**        Private Methods        **/
	/** ============================= **/
	
	
	private static ClientResponse makeGetCall(String urlToCall, Map<String, String> parameters,
			MultivaluedMap<String, String> queryParams, String apiKey) throws Exception {

		ClientResponse response = null;
		try {
			Client client = Client.create();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(urlToCall, parameters).toASCIIString()).queryParams(queryParams)
					.header(Constant.X_AUTH_TOKEN, apiKey);

			response = webResource.accept(Constant.ACCEPT).get(ClientResponse.class);

		} catch (Exception e) {
			throw e;
		}
		return response;
	}
	
	private static ClientResponse makePutCall(String urlToCall, Map<String, String> parameters,
			MultivaluedMap<String, String> queryParams, String apiKey) throws Exception {

		ClientResponse response = null;
		try {
			Client client = Client.create();
			WebResource.Builder webResource = client
					.resource(replaceParametersOnURL(urlToCall, parameters).toASCIIString())
					.queryParams(queryParams)
					.header(Constant.X_AUTH_TOKEN, apiKey);

			response = webResource.accept(Constant.ACCEPT).put(ClientResponse.class);
			response = verifyResponse(response); 
			

		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	private static Object parseResponse(ClientResponse response, Class<?> clazz) throws Exception {

		// check for any errors 
		response = verifyResponse(response);
		
		ObjectMapper mapper = new ObjectMapper();
		String output = response.getEntity(String.class);
		return mapper.readValue(output, clazz);
	}

	/**
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static ClientResponse verifyResponse(ClientResponse response) throws Exception {
		
		logger.debug("Server response status : "  + response.getStatus());		
		
		if (response.getStatus() == 200) {
			return response;
			
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
	
	private static URI replaceParametersOnURL(String template, Map<String, String> parameters) {
		UriBuilder builder = UriBuilder.fromPath(template);
		URI output = builder.buildFromMap(parameters);
		return output;
	}
}
