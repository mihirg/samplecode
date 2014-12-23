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

@Deprecated
/**
 * This class is old code. Refer to <code>NewTreeModel</code>
 */
public class TreeModel implements IModel {
	private Definition definition;
	public static Map<QName, Record> recordMap = new HashMap<QName, Record>();
	private List<String> stdNamespaces;
	private String targetNamespace;
	
	public TreeModel() {
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
        		EList<XSDDiagnostic> dglist = sch.getAllDiagnostics();
        		handleSchema(sch);	        		
        	}
        }

        return ret;		
	}
	
	private void handleSchema(XSDSchema schema) {
		targetNamespace = schema.getTargetNamespace();
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
	private boolean isStandardNamespace(String namespace) {
		Iterator<String> iter = stdNamespaces.iterator();
		while (iter.hasNext()) {
			String str = iter.next();
			if (str.equals(namespace))
				return true;
		}
		return false;
	}
	private void handleElement(XSDElementDeclaration element, Record rootRecord) {
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

	private void handleParticle(XSDParticle particle, Record rootRecord) {
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
				String namespace = type.getTargetNamespace();
				if (isStandardNamespace(namespace) || this.targetNamespace.equals(namespace)) {
					// Since this a simple field, it will have basic datatypes, so this can be a field of a record.
					type.getTargetNamespace();
					Field f = new Field(element.getName(), type.getName());
					rootRecord.getFields().add(f);
				} else {
					Record child = new Record(element.getName());
					child.setNamespace(element.getTargetNamespace());
					child.setResolved(false);
					recordMap.put(child.getQName(), child);
					rootRecord.getChildren().add(child);
					handleElement(element, child);					
				}
				
			} else if (type instanceof XSDComplexTypeDefinition) {
				Record child = new Record(element.getName());
				child.setNamespace(element.getTargetNamespace());
				recordMap.put(child.getQName(), child);
				rootRecord.getChildren().add(child);
				handleElement(element, child);				
			}
		}
			
		
	}

}
