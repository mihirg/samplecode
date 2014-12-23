package com.informatica.webservice.metadata;

public class WSOperationDescriptor {

	private String serviceName;
	private String serviceNamespace;
	protected String operationName;	
	protected String soapAction;
	protected String messageExchange;	
	protected String bindingStyle;
	protected String bindingUse;
	private WSOperationInputDescriptor input;
	
	
	public WSOperationInputDescriptor getInput() {
		return input;
	}

	public void setInput(WSOperationInputDescriptor input) {
		this.input = input;
	}

	public String getBindingUse() {
		return bindingUse;
	}

	public void setBindingUse(String bindingUse) {
		this.bindingUse = bindingUse;
	}

	public String getBindingStyle() {
		return bindingStyle;
	}

	public void setBindingStyle(String style) {
		this.bindingStyle = style;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	
	public String getMessageExchange() {
		return messageExchange;
	}

	public void setMessageExchange(String messageExchange) {
		this.messageExchange = messageExchange;
	}

	
	
	public WSOperationDescriptor() {
		
	}

}
