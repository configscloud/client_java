package configs.cloud.client.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class EnvWrapper implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<Env> env;

	public List<Env> getEnv() {
		return env;
	}

	public void setEnv(List<Env> env) {
		this.env = env;
	}
}