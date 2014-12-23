package in.gore.wsdl.model;

import in.gore.WsdlTest;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDDiagnostic;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.impl.XSDSchemaImpl;
import org.eclipse.xsd.util.XSDParser;
import org.w3c.dom.Element;

import com.ibm.wsdl.xml.WSDLReaderImpl;

public class NewTreeModel implements IModel {
	private Definition definition;
	public static Map<QName, Record> recordMap = new HashMap<QName, Record>();
	private List<String> stdNamespaces;
	private Map<String, XSDSchema> namespaceSchemaMap;
	
	public NewTreeModel() {
		
	}
	
	public void initialize(String url) {
		namespaceSchemaMap = new HashMap<String, XSDSchema>();
		stdNamespaces = new ArrayList<String>();
		stdNamespaces.add("http://www.w3.org/1999/XMLSchema");
		stdNamespaces.add("http://www.w3.org/2000/10/XMLSchema");
		stdNamespaces.add("http://www.w3.org/2001/XMLSchema");
		WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", true);
        try {
			definition = reader.readWSDL("https://secure.eloqua.com/API/1.2/Service.svc?wsdl");
        	//definition = reader.readWSDL("https://webservices.netsuite.com/wsdl/v2010_2_0/netsuite.wsdl");
        	//definition = reader.readWSDL("http://ip-10-62-135-75.ec2.internal:8002/webservices/SOAProvider/plsql/hz_cust_account_v2pub/?wsdl");
		} catch (WSDLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Binding> getAllBinding() {
        Map bindings = definition.getAllBindings();
        List<Binding> ret  = new ArrayList<Binding>(bindings.values());
        Types types = definition.getTypes();
        Iterator iter1 = types.getExtensibilityElements().iterator();
        int schemaCount = 0;
        while (iter1.hasNext()) {
        	ExtensibilityElement ext = (ExtensibilityElement)iter1.next();
        	if (ext instanceof Schema) {
        		
        		Schema s = (Schema)ext;
        		Map imports = s.getImports();
        		List<Vector> importList = new ArrayList<Vector>(imports.values());
        		Iterator<Vector> importIter = importList.iterator();
        		while (importIter.hasNext()) {
         			Vector<SchemaImport> imp = importIter.next();
         			for (SchemaImport simp : imp) {         				
         				String url = simp.getSchemaLocationURI();
         				XSDParser parse = new XSDParser();
         				parse.parse(url);
            			XSDSchema impSchema = parse.getSchema();
            			handleSchema(impSchema);

         			}
           		}
        		
        		// This is based on http://www.eclipse.org/forums/index.php/t/20211/
        		// the code below takes care of inlined schemas.
        		Element ele = s.getElement();
        		XSDSchema sch = XSDSchemaImpl.createSchema(ele);
        		sch.updateElement();
        		sch.update();
        		namespaceSchemaMap.put(sch.getTargetNamespace(),sch);
//        		handleSchema(sch);	        		
        	}
        }

        return ret;		
	}
	
	private void handleSchema(XSDSchema schema) {
		EList<XSDElementDeclaration> elements = schema.getElementDeclarations();
		Iterator<XSDElementDeclaration> iter = elements.iterator();
		while (iter.hasNext()) {
			XSDElementDeclaration element = (XSDElementDeclaration)iter.next();
			Record record = new Record(element.getName());
			record.setNamespace(element.getTargetNamespace());
			recordMap.put(record.getQName(), record);
			handleElement(schema, element, record);
			System.out.println(record.getName());
			
		}
	}
	private boolean isStandardNamespace(String namespace) {
		Iterator<String> iter = stdNamespaces.iterator();
		while (iter.hasNext()) {
			String str = iter.next();
			if (str.equals(namespace))
				return true;
		}
		return false;
	}
	private void handleElement(XSDSchema rootSchema, XSDElementDeclaration element, Record rootRecord) {
		System.out.println("Element Name: " + element.getName());
		XSDTypeDefinition type = element.getTypeDefinition();
		if (!type.getTargetNamespace().equals(rootSchema.getTargetNamespace())) {
			XSDSchema targetSchema = this.getSchema(type.getTargetNamespace());
			type = this.getTypeFromSchema(targetSchema, type.getName());
			expandType(targetSchema,rootRecord,type);
		}
		else 
			expandType(rootSchema, rootRecord, type);	
	}

	/**
	 * @param rootSchema The XSDSchema element corresponding to the rootRecord
	 * @param particle
	 * @param rootRecord The root Record object.
	 */
	private void handleParticle(XSDSchema rootSchema, XSDParticle particle, Record rootRecord) {
		if (particle.getTerm() instanceof XSDModelGroup) {
			List<XSDParticle> partList = ((XSDModelGroup)particle.getTerm()).getParticles();
			Iterator<XSDParticle> iter = partList.iterator();
			while (iter.hasNext()) {
				XSDParticle childParticle = (XSDParticle)iter.next();
				handleParticle(rootSchema, childParticle, rootRecord);
			}
		} else if (particle.getTerm() instanceof XSDElementDeclaration) {
			XSDElementDeclaration element = (XSDElementDeclaration)particle.getTerm();
			XSDTypeDefinition type = element.getTypeDefinition();
			if (type instanceof XSDSimpleTypeDefinition) {
				String typeNamespace = type.getTargetNamespace();
				String rootSchemaTargetNamespace = rootSchema.getTargetNamespace();
				// if this is a simple type and it belongs to same namespace as the rootRecord, then expand it to a field.
				if (isStandardNamespace(typeNamespace) || rootSchemaTargetNamespace.equals(typeNamespace)) {
					// Since this a simple field, it will have basic datatypes, so this can be a field of a record.
					type.getTargetNamespace();
					Field f = new Field(element.getName(), type.getName());
					rootRecord.getFields().add(f);
				} else {
					// We have a simple element but in belongs to different namespace.
					XSDSchema newSchema = this.getSchema(typeNamespace);
					XSDTypeDefinition newType = this.getTypeFromSchema(newSchema, type.getName());
					Record child = new Record(element.getName());
					child.setNamespace(element.getTargetNamespace());
					recordMap.put(child.getQName(), child);
					rootRecord.getChildren().add(child);
					//handleElement(newSchema, element, child);
					expandType(newSchema,child,newType);
				}
				
			} else if (type instanceof XSDComplexTypeDefinition) {
				XSDSchema schema = type.getTargetNamespace().equals(rootSchema.getTargetNamespace()) ? rootSchema : getSchema(type.getTargetNamespace());
				Record child = new Record(element.getName());
				child.setNamespace(element.getTargetNamespace());
				recordMap.put(child.getQName(), child);
				rootRecord.getChildren().add(child);
				handleElement(schema,element, child);				
			}
		}		
	}
	
	private void expandType(XSDSchema schema, Record rootRecord, XSDTypeDefinition type) {
		if (type instanceof XSDSimpleTypeDefinition) {
			XSDSimpleTypeDefinition simple = (XSDSimpleTypeDefinition)type;
			rootRecord.setType(simple.getName());
			System.out.println("Simple Type: " + simple.getName());
		} else if (type instanceof XSDComplexTypeDefinition) {
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
					handleParticle(schema, particle, rootRecord);
					
				}
			} else if (category == XSDContentTypeCategory.MIXED_LITERAL) {
			} else if (category == XSDContentTypeCategory.SIMPLE_LITERAL) {
				XSDSimpleTypeDefinition simple = (XSDSimpleTypeDefinition)complex.getContentType();
				System.out.println(simple.getName());
			}
	
		}
	}
	
	private XSDSchema getSchema(String targetNamespace) {
		// TODO: Check for standard namespaces.
		XSDSchema sch = namespaceSchemaMap.get(targetNamespace);
		return sch;
	}
	
	private Record getRecordForElement(XSDSchema sch, QName elementName) {		
		Record record = null;
		List<XSDElementDeclaration> eleList = sch.getElementDeclarations();
		for (XSDElementDeclaration element : eleList) {
			if (element.getName().equals(elementName.getLocalPart()) && 
				element.getTargetNamespace().equals(elementName.getNamespaceURI())) {
				record = new Record(element.getName());
				record.setNamespace(element.getTargetNamespace());
				recordMap.put(record.getQName(), record);
				handleElement(sch, element, record);				
				break;
			}
		}
		return record;
	}
	
	public Record getRecordForElement(QName elementName) {
		Record rec = recordMap.get(elementName);
		if (rec == null) {
			// TODO: Handle standard namespaces
			XSDSchema sch = getSchema(elementName.getNamespaceURI());
			if (sch != null) {
				return getRecordForElement(sch,elementName);
			}
		}
		return rec;
	}
	
	private XSDTypeDefinition getTypeFromSchema(XSDSchema schema, String typeName) {
		List<XSDTypeDefinition> typeList = schema.getTypeDefinitions();
		for (XSDTypeDefinition type : typeList) {
			if (type.getName().equals(typeName)) {
				return type;
			}
		}
		return null;
	}

}
