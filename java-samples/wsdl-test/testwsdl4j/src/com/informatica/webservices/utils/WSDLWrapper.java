package com.informatica.webservices.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.wsdl.extensions.soap.SOAPBinding;


import com.ibm.wsdl.factory.WSDLFactoryImpl;
import com.informatica.webservice.metadata.WSOperationDescriptor;
import com.informatica.webservice.metadata.WSOperationInputDescriptor;
import com.informatica.webservice.metadata.WebServiceOperation;

public class WSDLWrapper {
	
	private Definition definition;
	
	public WSDLWrapper(String url) throws WSDLException {
		assert(url != null);
		assert(url.length() > 0);
		loadWSDL(url);		
	}
	
	private void loadWSDL(String url) throws WSDLException {
		WSDLFactory factory = new WSDLFactoryImpl();
		WSDLReader reader = factory.newWSDLReader();
		definition = reader.readWSDL(url);
	}
	
	/**
	 * TODO: This is not yet implemented. This method should identify if the wsdl is SOAP 1.1 or SOAP 1.2. Also need to understand differences.
	 * 
	 */
	public void getProtocolHandler() {
		Map services = (Map)definition.getServices();
		for ( Iterator<Service> serviceIter = services.values().iterator(); serviceIter.hasNext();) {
			Service service = serviceIter.next();
			Map ports = service.getPorts();
			
			for (Iterator<Port> portIter = ports.values().iterator(); portIter.hasNext();) {
				Port port = portIter.next();
				Binding binding = port.getBinding();
				List<ExtensibilityElement> extElements = binding.getExtensibilityElements();
				System.out.println("*** Printing Extensibility Elements for Binding ***");
				for (ExtensibilityElement element : extElements) {
					System.out.println(element.getElementType());
				}
				
				System.out.println("*** Printing Binding Operations ***");
				List<BindingOperation> bindOps = binding.getBindingOperations();
				for (BindingOperation bindOp : bindOps) {
					System.out.println(bindOp.getName());
					List<ExtensibilityElement> bindOpExtElements = bindOp.getExtensibilityElements();
					System.out.println("*** Printing Extensibility Elements for Binding ***");
					for (ExtensibilityElement element : bindOpExtElements) {
						System.out.println(element.getElementType());
					}

				}
			}
		}
		
	}
	
	public List<QName> getServices() {
		List<QName> serviceNames = new ArrayList<QName>();
		Map services = (Map)definition.getServices();
		for ( Iterator<Service> serviceIter = services.values().iterator(); serviceIter.hasNext();) {
			Service service = serviceIter.next();
			serviceNames.add(service.getQName());
		}
		
		return serviceNames;
		
	}
	
	
	public List<String> getWSOperations() {
		List<String> opNames = new ArrayList<String>();
		Map services = (Map)definition.getServices();
		for ( Iterator<Service> serviceIter = services.values().iterator(); serviceIter.hasNext();) {
			Service service = serviceIter.next();
			Map ports = service.getPorts();
			
			for (Iterator<Port> portIter = ports.values().iterator(); portIter.hasNext();) {
				Port port = portIter.next();
				Binding binding = port.getBinding();
				List<BindingOperation> bindOps = binding.getBindingOperations();
				for (BindingOperation bindOp : bindOps) {
					opNames.add(bindOp.getName());
				}
			}
		}
		
		return opNames;
	}
	
	public WebServiceOperation getWSOperation(QName serviceName, String operationName) {
		Service service = definition.getService(serviceName);
		Map ports = service.getPorts();
		
		for (Iterator<Port> portIter = ports.values().iterator(); portIter.hasNext();) {
			Port port = portIter.next();
			Binding binding = port.getBinding();
			List<BindingOperation> bindOps = binding.getBindingOperations();
			for (BindingOperation bindOp : bindOps) {
				if (operationName.equals(bindOp.getName())) {
					WebServiceOperation op = new WebServiceOperation();
					op.setOperationName(operationName);
					List<ExtensibilityElement> bindOpExtElements = bindOp.getExtensibilityElements();
					for (ExtensibilityElement element : bindOpExtElements) {
						if (element instanceof SOAPOperation) {
							SOAPOperation soapOp = (SOAPOperation)element;
							op.setSoapAction(soapOp.getSoapActionURI());								
						}
					}						
					return op;						
				}
			}
		}
		return null;
	}
	
	
	public WSOperationDescriptor describeWSOperation(QName serviceName, String operationName) {
		

		Service service = definition.getService(serviceName);
		Map ports = service.getPorts();
		for (Iterator<Port> portIter = ports.values().iterator(); portIter.hasNext();) {
			Port port = portIter.next();
			Binding binding = port.getBinding();
			List<BindingOperation> bindOps = binding.getBindingOperations();
			for (BindingOperation bindOp : bindOps) {
				if (operationName.equals(bindOp.getName())) {
					WSOperationDescriptor wsDesc = new WSOperationDescriptor();
					wsDesc.setServiceName(serviceName.getLocalPart());
					wsDesc.setServiceNamespace(definition.getTargetNamespace());
					wsDesc.setOperationName(operationName);
					List<ExtensibilityElement> bindOpExtElements = bindOp.getExtensibilityElements();
					for (ExtensibilityElement element : bindOpExtElements) {
						if (element instanceof SOAPOperation) {
							SOAPOperation soapOp = (SOAPOperation)element;
							wsDesc.setSoapAction(soapOp.getSoapActionURI());								
						} 
					}
					
					List<ElementExtensible> bindingExtList = binding.getExtensibilityElements();
					for (ElementExtensible eleExt : bindingExtList) {
						if (eleExt instanceof SOAPBinding) {
							SOAPBinding sbind = (SOAPBinding)eleExt;
							wsDesc.setBindingStyle(sbind.getStyle());
						}
					}
					
					BindingInput bindInput = bindOp.getBindingInput();
					List<ElementExtensible> bindInputExt = bindInput.getExtensibilityElements();
					for (ElementExtensible ex : bindInputExt) {
						if (ex instanceof SOAPBody) {
							SOAPBody body = (SOAPBody)ex;
							wsDesc.setBindingUse(body.getUse());
						}
					}
					
					Operation op = bindOp.getOperation();
					Message ipMsg = op.getInput().getMessage();
					WSOperationInputDescriptor inDesc = new WSOperationInputDescriptor();
					inDesc.setMessageName(ipMsg.getQName().getLocalPart());
					inDesc.setMessageNamespace(ipMsg.getQName().getNamespaceURI());
					List<Part> ipParts = ipMsg.getOrderedParts(null);
					
					
					return wsDesc;
											
				}
			}
		}
		return null;
		
	}

}
