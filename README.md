# How to use client config code?

#### Documentation
JavaDocs API Documentation : https://configs.cloud/clients/apidocs/

#### Initialisation 
Create the constructor CloudConfigClient and pass the argument 'REST API token' and base url "https://app.configs.cloud".

	CloudConfigClient c = new CloudConfigClient("ZXY4c2tsMk8wYm9jMFRGZ2RyRWoy", "https://app.configs.cloud");
	
(You can access following  methods using variable of CloudConfigClient object)

#### Methods	
To retrieve list of configs for particular dataset call  following method and pass the argument 'datasetid'.

	List<Config> configs = c.getAllConfiguration(2);

To retrieve list of configs for particular dataset and environment call the following method and pass the argument 'datsetid' and 'short name of enviornment'.

	List<Config> configs = c.getAllConfigurationForEnvironment(2,"dev");
	
To update the config call the following method and pass the argument 'datasetid','environment short name','config key' and 'value'

	c.updateConfigKey(2, "dev", "sonar.projectKey", "update from sdk");
	
To retrieve the config detail by dataset,environment and config key call the following method and pass the argument 'datasetid', 'environment short name' and 'config key'.

	List<Config> configs = c.getConfigurationByDatasetAndEnvironmentAndKey(2,"dev", "sonar.projectKey");
	
To enable or disable the particular config call the following method and pass the argument 'datasetid', 'environment short name', 'config key','Y' for enable or 'N' for disable.

	c.updateConfigEnabledStatusForEnv(2,"dev", "sonar.projectKey","Y");
	
To retrieve the list of datasets use the following method.

	List<Dataset> datasets = c.getAllDataset();
	
To retrieve the detail of dataset for particular dataset call the following method and pass the argument 'datasetid'.

	List<Dataset> datasets = c.getDatasetByDatasetId(2);
	
To retrieve the list of environments use the following method.

	EnvReturnFormat envs = c.getAllEnvironment();
	
To retrieve the particular environment use the following method and pass the argument 'environment short name'.
	 
	 Env env = c.getEnvironmentByShortName("all");
	
To retrieve the configs list by RSQL parser search use the following method and pass the argument 'search option' and optional argument iqk value either 'y' or 'n'. if you use iqk==y it will give you the list of configs with  following result for key

In this case if keys are 
com.configs.cloud.x.name=yyy
com.configs.cloud.x.addressline1=yyy
com.configs.cloud.x.addressline2=yyy

outcome should be -

name=yyyy
addressline1=yyy
addressline2=yyyy

	 List<Config> cd = c.getConfigByRSQLSearch("configid==2,key==com.configs.cloud.x.*;iqk==y");
	 

for	

	List<Config> cd = c.getConfigByRSQLSearch("configid==2,key==com.configs.cloud.x.*;iqk==n");

outcome should be -	
com.configs.cloud.x.name=yyy
com.configs.cloud.x.addressline1=yyy
com.configs.cloud.x.addressline2=yyy

for

	List<Config> cd = c.getConfigByRSQLSearch("configid==2,key==com.configs.cloud.x.*");
	
outcome should be -	
com.configs.cloud.x.name=yyy
com.configs.cloud.x.addressline1=yyy
com.configs.cloud.x.addressline2=yyy

following are some example for calling this method

		List<Config> cd = c.getConfigByRSQLSearch("configid==2,
		key==com.configs.cloud.x.*");
		
		List<Config> cd =c.getConfigByRSQLSearch("configid==2,
		envid==1,key==com.configs.cloud.x.*");
		
		List<Config> cd = c.getConfigByRSQLSearch("configid==2,
		envid==1,key==com.configs");
	
	
	


