package in.gore.wsdl.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class Record {
	
	private String name;
	private List<Record> children;
	private List<Field> fields;
	private String type;
	private String namespace;
	private boolean isResolved = true;
	
	public boolean isResolved() {
		return isResolved;
	}

	public void setResolved(boolean isResolved) {
		this.isResolved = isResolved;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Record(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public List<Record> getChildren() {
		if (children == null)
			children = new ArrayList<Record>();
		return children;
	}
	public List<Field> getFields() {
		if (fields == null)
			fields = new ArrayList<Field>();
		return fields;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public QName getQName() {
		return new QName(namespace,name);
	}
	

}
