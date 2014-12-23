package in.gore.tools;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import com.ibm.wsdl.xml.WSDLReaderImpl;

public class WSDLParser {
	
	private WSDLReader reader;
	private Definition definition;
	
	public static WSDLParser parse(URL wsdlUrl) {
		return new WSDLParser(wsdlUrl);
	}
	
	private WSDLParser(URL wsdlUrl) {
		reader = new WSDLReaderImpl();
        reader.setFeature("javax.wsdl.verbose", false);
        try {
			definition = reader.readWSDL("https://secure.eloqua.com/API/1.2/Service.svc?wsdl");
		} catch (WSDLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<QName> getBindingNames() {
		return new ArrayList<QName>(definition.getAllBindings().keySet());
	}

}
