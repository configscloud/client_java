package configs.cloud.client.entity;

import java.io.Serializable;

public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long  configid;
	
	private String key;
	
	private String value;
	
	private String ispassword;	
	
	private String isenabled;
	
	private int version;
		
	private Env env;
		
	private Dataset dataset;

	public Long getConfigid() {
		return configid;
	}

	public void setConfigid(Long configid) {
		this.configid = configid;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Env getEnv() {
		return env;
	}

	public void setEnv(Env env) {
		this.env = env;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getIspassword() {
		return ispassword;
	}

	public void setIspassword(String ispassword) {
		this.ispassword = ispassword;
	}

	public String getIsenabled() {
		return isenabled;
	}

	public void setIsenabled(String isenabled) {
		this.isenabled = isenabled;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configid == null) ? 0 : configid.hashCode());
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + ((env == null) ? 0 : env.hashCode());
		result = prime * result + ((isenabled == null) ? 0 : isenabled.hashCode());
		result = prime * result + ((ispassword == null) ? 0 : ispassword.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Config other = (Config) obj;
		if (configid == null) {
			if (other.configid != null)
				return false;
		} else if (!configid.equals(other.configid))
			return false;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (env == null) {
			if (other.env != null)
				return false;
		} else if (!env.equals(other.env))
			return false;
		if (isenabled == null) {
			if (other.isenabled != null)
				return false;
		} else if (!isenabled.equals(other.isenabled))
			return false;
		if (ispassword == null) {
			if (other.ispassword != null)
				return false;
		} else if (!ispassword.equals(other.ispassword))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	
	public String toString() {
        return "" + configid + ":" + key + ":" + value + ":" +
        		ispassword + ":" + isenabled + ":" + version + ":" +
        		env.getEnvid() + ":" +  dataset.getDatasetid();
    }
}
