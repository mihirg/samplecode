package in.gore;

import in.gore.wsdl.model.Field;
import in.gore.wsdl.model.Record;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.impl.XSDSchemaImpl;
import org.w3c.dom.Element;

import com.ibm.wsdl.xml.WSDLReaderImpl;

public class WsdlTest {
	
	private static Map<QName, Record> recordMap = new HashMap<QName, Record>();

	private static String getWsdlSchemaURI(String wsdlDocURIStr, int count)	{
		
		String wsdlSchemaUri = new StringBuilder().append("ROOT_WSDL_").append(count).append(".xsd").toString();
		if (wsdlDocURIStr == null || wsdlDocURIStr.trim().isEmpty()) {
			return wsdlSchemaUri;
	    }
	    try {
	    	return new URI(wsdlDocURIStr).resolve(wsdlSchemaUri).toString();
	    }
	    catch (URISyntaxException e) {
	    }
	    return wsdlSchemaUri;
    }
	
	private static void handleParticle(XSDParticle particle, Record rootRecord) {
		if (particle.getTerm() instanceof XSDModelGroup) {
			List<XSDParticle> partList = ((XSDModelGroup)particle.getTerm()).getParticles();
			Iterator<XSDParticle> iter = partList.iterator();
			while (iter.hasNext()) {
				XSDParticle childParticle = (XSDParticle)iter.next();
				handleParticle(childParticle, rootRecord);
			}
		} else if (particle.getTerm() instanceof XSDElementDeclaration) {
			XSDElementDeclaration element = (XSDElementDeclaration)particle.getTerm();
			XSDTypeDefinition type = element.getTypeDefinition();
			if (type instanceof XSDSimpleTypeDefinition) {
				// Since this a simple field, it will have basic datatypes, so this can be a field of a record.
				Field f = new Field(element.getName(), type.getName());
				rootRecord.getFields().add(f);
			} else if (type instanceof XSDComplexTypeDefinition) {
				Record child = new Record(element.getName());
				child.setNamespace(element.getTargetNamespace());
				recordMap.put(child.getQName(), child);
				rootRecord.getChildren().add(child);
				handleElement(element, child);				
			}
		}
			
		
	}
	
	private static List<XSDParticle> getChildParticles(XSDParticle particle){
		if (particle.getTerm() instanceof XSDModelGroup) {
			return ((XSDModelGroup)particle.getTerm()).getParticles();
		} else if (particle.getTerm() instanceof XSDElementDeclaration) {
			XSDElementDeclaration element = (XSDElementDeclaration)particle.getTerm();
			System.out.println(element.getName());
			XSDTypeDefinition typeDefinition = element.getTypeDefinition();
			System.out.println(typeDefinition.getName());
			if(typeDefinition instanceof XSDComplexTypeDefinition){ 
				List<XSDParticle> children = new ArrayList<XSDParticle>();
				XSDComplexTypeDefinition complexTypeDefinition = (XSDComplexTypeDefinition)typeDefinition;
				XSDParticle currentParticle = (XSDParticle)complexTypeDefinition.getContent();
				if (currentParticle == null) {
					currentParticle = (XSDParticle)complexTypeDefinition.getContentType();
				}
				
				if(currentParticle != null){
					XSDModelGroup modelGroup = (XSDModelGroup)currentParticle.getTerm();
					EList<XSDParticle> particles = modelGroup.getParticles();
					if (particles.isEmpty()) {
						particles = modelGroup.getContents();
					} 

					// For each attribute
					for (int j=0; j<particles.size(); j++){
						XSDParticle p = particles.get(j);
						children.add(p);
					}
				}
				return children;
			}
		}
		return Collections.emptyList();
	}
	private static void handleSchema(XSDSchema schema) {
		EList<XSDElementDeclaration> elements = schema.getElementDeclarations();
		Iterator<XSDElementDeclaration> iter = elements.iterator();
		while (iter.hasNext()) {
			XSDElementDeclaration element = (XSDElementDeclaration)iter.next();
			Record record = new Record(element.getName());
			record.setNamespace(element.getTargetNamespace());
			recordMap.put(record.getQName(), record);
			handleElement(element, record);
			System.out.println(record.getName());
			
		}
	}
	
	private static void handleElement(XSDElementDeclaration element, Record rootRecord) {
		System.out.println("Element Name: " + element.getName());
		XSDTypeDefinition type = element.getTypeDefinition();
		if (type instanceof XSDSimpleTypeDefinition) {
			XSDSimpleTypeDefinition simple = (XSDSimpleTypeDefinition)type;
			rootRecord.setType(simple.getName());
			System.out.println("Simple Type: " + simple.getName());
		} else  if (type instanceof XSDComplexTypeDefinition) {
			XSDComplexTypeDefinition complex = (XSDComplexTypeDefinition)type;
			rootRecord.setType(complex.getName());
			System.out.println("Complex Type: " + complex.getName());
			XSDContentTypeCategory category = complex.getContentTypeCategory();
			if (category == XSDContentTypeCategory.ELEMENT_ONLY_LITERAL) {
			} else if (category == XSDContentTypeCategory.EMPTY_LITERAL) {
				XSDComplexTypeContent contentType = complex.getContent();
				if (contentType instanceof XSDSimpleTypeDefinition) {
					XSDSimpleTypeDefinition simple = (XSDSimpleTypeDefinition) contentType;
					System.out.println(simple.getName());
				} else if (contentType instanceof XSDComplexTypeDefinition) {
					XSDComplexTypeDefinition complexType = (XSDComplexTypeDefinition)contentType;
					System.out.println(complexType.getName());
				} else if (contentType instanceof XSDParticle) {
					XSDParticle particle = (XSDParticle)contentType;
					handleParticle(particle, rootRecord);
					
				}
			} else if (category == XSDContentTypeCategory.MIXED_LITERAL) {
			} else if (category == XSDContentTypeCategory.SIMPLE_LITERAL) {
				XSDSimpleTypeDefinition simple = (XSDSimpleTypeDefinition)complex.getContentType();
				System.out.println(simple.getName());
			}
		}
	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WSDLReader reader = new WSDLReaderImpl();
	        reader.setFeature("javax.wsdl.verbose", false);
	        Definition definition = reader.readWSDL("https://secure.eloqua.com/API/1.2/Service.svc?wsdl");
	        //Definition definition = reader.readWSDL("http://ip-10-62-135-75.ec2.internal:8002/webservices/SOAProvider/plsql/hz_cust_account_v2pub/?wsdl");
	        List defExtEle = definition.getExtensibilityElements();
	        Types types = definition.getTypes();
	        Iterator iter1 = types.getExtensibilityElements().iterator();
	        int schemaCount = 0;
			ResourceSet set = new ResourceSetImpl();
	        while (iter1.hasNext()) {
	        	ExtensibilityElement ext = (ExtensibilityElement)iter1.next();
	        	if (ext instanceof Schema) {
	        		Schema s = (Schema)ext;
	        		String rootSchemaLocation = WsdlTest.getWsdlSchemaURI(definition.getDocumentBaseURI(), schemaCount++);
	        		Element ele = s.getElement();
	        		XSDSchema sch = XSDSchemaImpl.createSchema(ele);
	        		sch.updateElement();
	        		sch.update();
	        		//set.getResources().add(sch.eResource());
	        		handleSchema(sch);	        		
	        	}
	        }
	        Map bindings = definition.getAllBindings();
	        Iterator iter = bindings.values().iterator();
	        while (iter.hasNext()) {
	        	Binding bind = (Binding)iter.next();
	        	List opList = bind.getBindingOperations();
	        	Iterator listIter = opList.iterator();
	        	while (listIter.hasNext()) {
	        		BindingOperation bindop = (BindingOperation)listIter.next();
	        		System.out.println(bindop.getName());
	        		Operation op = bindop.getOperation();
	        		Part part = op.getInput().getMessage().getPart("parameters");
	        		QName tname = part.getElementName();
	        		Record ipRecord = recordMap.get(tname);
	        		generateXML(ipRecord);
	        		
	        		BindingInput input = bindop.getBindingInput();
	        		BindingOutput output = bindop.getBindingOutput();
	        		System.out.println(bindop.getName());
	        	}
	        	System.out.println(bind.toString());        
	        	
	        }
	        
	        
			
		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}
	
	private static void generateXML(Record ipRecord) {
		
		System.out.println("<" + ipRecord.getName() + ">");
		if (ipRecord.getFields().size() > 0) {
			Iterator<Field> fields = ipRecord.getFields().iterator();
			while(fields.hasNext()) {
				Field f = fields.next();
				System.out.println("<" + f.getName() + ">");
				System.out.println("</" + f.getName() + ">");
			}
		}
		Iterator<Record> children = ipRecord.getChildren().iterator();
		while (children.hasNext()) {
			Record child = children.next();
			generateXML(child);
		}
		System.out.println("</" + ipRecord.getName() + ">");
		
	}

}
