package com.informatica.webservices.utils;

import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

public class Test {

	/**
	 * @param args
	 * @throws WSDLException 
	 */
	public static void main(String[] args) throws WSDLException {
		//WSDLWrapper wrapper = new WSDLWrapper("https://secure.eloqua.com/API/1.2/Service.svc?wsdl");
		WSDLWrapper wrapper = new WSDLWrapper("https://webservices.na1.beta.netsuite.com/wsdl/v2014_2_0/netsuite.wsdl");
		List<QName> services = wrapper.getServices();
/*		
		for (QName service : services) {
			List<String> operations = wrapper.getWSOperations(service);
			
			for (String operation : operations) {
				wrapper.getWSOperation(operation);
			}
		}
*/
	}

}
