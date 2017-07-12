package configs.cloud.client.entity;

import java.io.Serializable;
import java.util.Collection;

public class EnvReturnFormat implements Serializable{

	private static final long serialVersionUID = 1L;
	private Collection<Env> env;

	public Collection<Env> getEnv() {
		return env;
	}

	public void setEnv(Collection<Env> env) {
		this.env = env;
	}
}