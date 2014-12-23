package in.gore;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.Output;
import org.eclipse.wst.wsdl.Input;
import org.eclipse.wst.wsdl.Part;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
import org.eclipse.wst.wsdl.util.WSDLResourceImpl;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
public class Test {

	public static void main(String args[]) {
		
        try {      	
        	DateFormat sap = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        	Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("0027-08-12 04:04:04");        	
        	Date sapDt = sap.parse("0027-08-12 04:04:04");
        	long time = sapDt.getTime();
        	java.sql.Timestamp ts3 = new java.sql.Timestamp(time);
//			XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar("2012-01-19T21:00:00.000-08:00");
//			XMLGregorianCalendar cal1 = DatatypeFactory.newInstance().newXMLGregorianCalendar("2012-01-20T05:00:00.000Z");
//			System.out.println(cal.toGregorianCalendar().getTimeInMillis());
//			System.out.println(cal1.toGregorianCalendar().getTimeInMillis());
//			java.sql.Timestamp ts = new java.sql.Timestamp(cal.toGregorianCalendar().getTimeInMillis());
//			java.sql.Timestamp ts1 = new java.sql.Timestamp(cal1.toGregorianCalendar().getTimeInMillis());
//			System.out.println(cal.toGregorianCalendar().getTime().getTime());
//			System.out.println(ts.getTime());
//			System.out.println(ts.getTime());
			XMLGregorianCalendar cal2 = DatatypeFactory.newInstance().newXMLGregorianCalendar("2012-11-27T12:03:12.000Z");
			System.out.println(cal2.toGregorianCalendar().getTimeInMillis());
			DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			//utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dt = utcFormat.parse("2012-11-27T12:03:12.000Z");
			Date tn = utcFormat.parse("9999-12-31T00:00:00.000Z");
			System.out.println(tn.getTime());
			System.out.println(dt.getTime());
			System.out.println(dt.getTime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//URI fileURI = URI.createURI("http://infacloudsample.appspot.com/SampleDataService.wsdl");
		URI fileURI = URI.createURI("https://secure.eloqua.com/API/1.2/Service.svc?wsdl");
		WSDLResourceFactoryImpl factImpl = new WSDLResourceFactoryImpl();
		WSDLResourceImpl wsdlResource = (WSDLResourceImpl)factImpl.createResource(fileURI);
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
		wsdlResource.basicSetResourceSet(new ResourceSetImpl(), null);
		Definition loadedDefinition = null;
		
		try {
			wsdlResource.load(null);
			loadedDefinition = wsdlResource.getDefinition();
			Iterator<PortType> portTypes = loadedDefinition.getEPortTypes().iterator();
			while (portTypes.hasNext()) {
				PortType portType = (PortType)portTypes.next();
				System.out.println("PorType: " + portType.getQName().getLocalPart());
				Iterator<Operation> operations = portType.getOperations().iterator();
				while (operations.hasNext()) {
					Operation operation = operations.next();
					System.out.println("Operation Name: " + operation.getName());
					operation.getEExtensibilityElements();
					Input ip = (Input)operation.getEInput();
					Iterator<Part> parts = ip.getEMessage().getEParts().iterator();
					
					while (parts.hasNext()) {
						Part part = parts.next();
						EClass eclass = part.eClass();
					
						XSDElementDeclaration elem = part.getElementDeclaration();
						XSDTypeDefinition type = elem.getType();
						if (type instanceof XSDComplexTypeDefinition) {
							XSDComplexTypeDefinition complex = (XSDComplexTypeDefinition)type;
							XSDContentTypeCategory category = complex.getContentTypeCategory();
							if (category == XSDContentTypeCategory.SIMPLE_LITERAL) {
								
							} else if (category == XSDContentTypeCategory.EMPTY_LITERAL) {
								
							} else if (category == XSDContentTypeCategory.ELEMENT_ONLY_LITERAL) {
								
							} else if (category == XSDContentTypeCategory.MIXED_LITERAL) {
								
							}
								
							XSDComplexTypeContent contentType = complex.getContentType();
							contentType.toString();
						}
						Element conc = elem.getElement();
						Document document = conc.getOwnerDocument();
						DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
						LSSerializer serializer = domImplLS.createLSSerializer();
						String str = serializer.writeToString(conc);
						
						if (elem != null)
							System.out.println(elem.toString());
						
						XSDTypeDefinition xsdDef = part.getTypeDefinition();
						if (xsdDef != null)
							System.out.println(xsdDef.toString());
					}
					Output op = (Output)operation.getOutput();
					
					Iterator<Part> opPart = op.getEMessage().getEParts().iterator();
					while (opPart.hasNext()) {
						Part part = opPart.next();
						EClass eclass = part.eClass();
						XSDElementDeclaration elem = part.getElementDeclaration();
						Element conc = elem.getElement();
						Document document = conc.getOwnerDocument();
						DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
						LSSerializer serializer = domImplLS.createLSSerializer();
						String str = serializer.writeToString(conc);
						
						if (elem != null)
							System.out.println(elem.toString());

					}
					
				}
				
			}
			
		} catch (Exception exp) {
			exp.printStackTrace();
			
		}
	}
}
