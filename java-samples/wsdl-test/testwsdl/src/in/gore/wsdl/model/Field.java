package in.gore.wsdl.model;

public class Field {
	private String name;
	private String namespace;
	private String dataType;

	public Field(String name, String dataType) {
		this.name = name;
		this.dataType = dataType;
	}

	public String getName() {
		return name;
	}

	public String getDataType() {
		return dataType;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
