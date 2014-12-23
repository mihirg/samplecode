package in.gore;

import java.util.ArrayList;
import java.util.List;

public class Record {
	
	private String name;
	private List<Record> children;
	private List<Attribute> attributes;
	private List<Field> fields;
	private String type;
	private ElementType elementType;
	private ContentModel contentModel;
	private boolean isRootElement;
	
	public boolean isRootElement() {
		return isRootElement;
	}

	public void setRootElement(boolean isRootElement) {
		this.isRootElement = isRootElement;
	}

	public ContentModel getContentModel() {
		return contentModel;
	}

	public void setContentModel(ContentModel contentModel) {
		this.contentModel = contentModel;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public void setElementType(ElementType elementType) {
		this.elementType = elementType;
	}

	public Record(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<Record> getChildren() {
		if (children == null)
			children = new ArrayList<Record>();
		return children;
	}

	public List<Attribute> getAttributes() {
		if (attributes == null)
			attributes = new ArrayList<Attribute>();
		return attributes;
	}
	
	public List<Field> getFields() {
		if (fields == null)
			fields = new ArrayList<Field>();
		return fields;
	}

}
