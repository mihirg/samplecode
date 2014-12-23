package in.gore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;

public class XSDTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getRecordList("c://test.xsd");
	}
	
	public static List<Record> getRecordList(String fileName) {
		
		XMLSchemaLoader loader = new XMLSchemaLoader();
		XSModel model = loader.loadURI(new File(fileName).toURI().toString());
		XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
		List<Record> recordList = new ArrayList<Record>();
		for (int i=0; i < map.getLength(); i++ ) {
			XSObject o = map.item(i);
			if (o instanceof XSElementDecl ) {
				XSElementDecl ele = (XSElementDecl) o;
				Record root = new Record(ele.getName());
				root.setRootElement(true);
				handleElement(ele, root);
				recordList.add(root);
			}			
		}
		return recordList;
	}
	
	private static void unwrapModelGroup(XSModelGroup modelGroup, Record root) {
		XSObjectList list = modelGroup.getParticles();
		for (int i = 0; i< list.getLength(); i++) {
			XSParticle particle = (XSParticle)list.item(i);
			XSTerm term = particle.getTerm();
			if (term instanceof XSModelGroup) {
				unwrapModelGroup((XSModelGroup)term, root);
				return;			
			} else if (term instanceof XSElementDeclaration) {
				XSElementDeclaration elem = (XSElementDeclaration)term;
				XSTypeDefinition type = elem.getTypeDefinition();
				if (type instanceof XSSimpleTypeDefinition) {
					XSSimpleTypeDefinition simple = (XSSimpleTypeDefinition) type;
					Field f = new Field(elem.getName(), simple.getName());
					root.getFields().add(f);					
				} else {
					assert(type instanceof XSComplexTypeDefinition);
					XSComplexTypeDefinition complex = (XSComplexTypeDefinition)type;
					short contentType = complex.getContentType();
					if (contentType == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {
						// TODO: Since this is a complex type, this may have attributes defined on it.
						// this code block does not handle this.
						XSTypeDefinition baseType = complex.getBaseType();
						Field f = new Field(elem.getName(), baseType.getName());
						root.getFields().add(f);						
					} else if (contentType == XSComplexTypeDefinition.CONTENTTYPE_ELEMENT) {
						// This indicates that this element has child elements in it. So create a new Record for this
						// and add as child of root.
						Record child = new Record(elem.getName());
						root.getChildren().add(child);
						handleElement(elem, child);						
					} else 
						assert(false); // any other type not yet handled.

				}
			}
		}
	}
	
	private static void handleAttributesForElements(XSObjectList attrList, Record root) {
		for (int i = 0; i < attrList.getLength(); i++) {
			XSObject obj = attrList.item(i);
			if (obj instanceof XSAttributeUse) {
				XSAttributeUse attrUse = (XSAttributeUse)obj;
				XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
				String attrName = attrDecl.getName();
				String attrType = attrDecl.getTypeDefinition().getName();
				Attribute attr = new Attribute(attrName, attrType);
				root.getAttributes().add(attr);
			}
		}		
	}
	
	private static void handleElement(XSElementDeclaration elem, Record root) {
		XSTypeDefinition type = elem.getTypeDefinition();
		if (type instanceof XSComplexTypeDefinition) {
			root.setElementType(ElementType.Complex);
			XSComplexTypeDefinition complex = (XSComplexTypeDefinition) type;
			// get the attributes for element
			XSObjectList attrList = complex.getAttributeUses();
			handleAttributesForElements(attrList,root);
			short contentType = complex.getContentType();
			switch (contentType) {
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
				root.setContentModel(ContentModel.Simple);

				// TODO: This case should not be hit. The only possible scenario if the root element of the XSD has Complex types with 
				// simple content models in the root. This is not yet handled.
				// Complex type with simple content. In this case the element can be expanded as member variable
				// of the class where the element's name is the variable name and its type its dataype.
				XSTypeDefinition baseType = complex.getBaseType();
				root.setType(baseType.getName());
				//System.out.println(elem.getName() + ":" +baseType.getNamespace() + ":" + baseType.getName());
				//System.out.println("<" +elem.getName()+">"+"</" + elem.getName()+ ">" );
				break;
				
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
				root.setContentModel(ContentModel.Complex);
				// When content type is element, the input element is composed of further elements.
				//System.out.println("Element: " + elem.getName());
				System.out.println("<" + elem.getName()+">");
				XSParticle part = complex.getParticle();
				XSTerm ter = part.getTerm();
				if (ter instanceof XSModelGroup){
					// this code is based on http://www.javadocexamples.com/java_source/org/jboss/ws/tools/helpers/ReturnTypeUnwrapper.java.html
					XSModelGroup modGrp = (XSModelGroup)ter;
					unwrapModelGroup(modGrp, root);
				}
				System.out.println("</" + elem.getName()+">");
				break;
			default:							
			}
		} else {
			root.setContentModel(ContentModel.Simple);
			root.setElementType(ElementType.Simple);
			// TODO: Check why this block gets hit. This may be getting hit the root element of the XSD is a simple element. We currently
			// to not handle this.			
			// simple type definition can be expanded inline, similar to complex type with simple content. The element can become member variable and its
			// type information can be used to infer the datatype.
			XSSimpleTypeDefinition simple = (XSSimpleTypeDefinition) type;
			root.setType(simple.getName());
			//System.out.println(elem.getName() + ":" +simple.getNamespace() + ":" +simple.getName());
			//System.out.println("<" +elem.getName() + ">" + "</" +elem.getName() + ">");			
		}		
	}

}
