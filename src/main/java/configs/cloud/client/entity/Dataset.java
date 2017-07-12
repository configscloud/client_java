package configs.cloud.client.entity;

import java.io.Serializable;

public class Dataset implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long datasetid;
	
	private String name;
	
	public Long getDatasetid() {
		return datasetid;
	}

	public void setDatasetid(Long datasetid) {
		this.datasetid = datasetid;
	}

	public String getDatasetname() {
		return name;
	}

	public void setDatasetname(String name) {
		this.name = name;
	}
	
	public Dataset(){}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datasetid == null) ? 0 : datasetid.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Dataset other = (Dataset) obj;
		if (datasetid == null) {
			if (other.datasetid != null)
				return false;
		} else if (!datasetid.equals(other.datasetid))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	
}