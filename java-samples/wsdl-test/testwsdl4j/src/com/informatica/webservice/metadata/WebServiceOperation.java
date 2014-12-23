package com.informatica.webservice.metadata;

public class WebServiceOperation {
	
	protected String operationName;
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	protected String soapAction;
	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	protected String messageExchange;
	
	public String getMessageExchange() {
		return messageExchange;
	}

	public void setMessageExchange(String messageExchange) {
		this.messageExchange = messageExchange;
	}

	public WebServiceOperation() {	
		
	}
	


}
