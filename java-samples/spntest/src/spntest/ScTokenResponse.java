/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spntest;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.MessageProp;

/**
 *
 * @author agunta
 */
public class ScTokenResponse {

    protected SOAPElement securityTokenContext;
    protected String binaryExchange;
    protected String cipherValue;
    protected String sctId;
    protected String sctIdentifier;
    protected SOAPElement sctReference;
    protected SOAPMessage rstResponse;

    public void setMessage1(SOAPMessage message) {
        rstResponse = message;
    }
    
    public void setMessage(SOAPMessage message) {
        rstResponse = message;

        // Process the response message for binary exchange, proof token, security context token
        processMessage();
    }

    public String getBinaryExchange() {
        return binaryExchange;
    }

    public String getBinaryToken() {

        String value = null;
        try
        {
            SOAPBody soapBody = rstResponse.getSOAPBody();
            Iterator iter = soapBody.getChildElements();

            while(iter.hasNext()) {
                SOAPElement element = (SOAPElement)iter.next();
                if (element.getLocalName().equals("RequestSecurityTokenResponse"))
                {
                    Iterator childList = element.getChildElements();
                    while (childList.hasNext()) {
                        SOAPElement child = (SOAPElement)childList.next();
                        if (child.getLocalName().equals("BinaryExchange")) {
                            value = child.getValue();
                            break;
                        }
                    }
                }                
            }
        }
        catch (SOAPException exp) {
            exp.printStackTrace();
        }

        return value;
    }

    public String getCipherValue() {
        return cipherValue;
//        String value = null;
//        try
//        {
//            SOAPBody soapBody = rstResponse.getSOAPBody();
//            SOAPElement soapElement = (SOAPElement) soapBody.getChildElements().next();
//
//
//            soapElement = (SOAPElement) soapElement.getChildElements().next();
//            Iterator it = (Iterator) soapElement.getChildElements();
//            while (it.hasNext())
//            {
//                SOAPElement bodyElement = (SOAPElement) it.next();
//
//                if (bodyElement.getLocalName().equals("RequestedProofToken"))
//                {
//                    // Get encrypted key
//                    bodyElement = (SOAPBodyElement) bodyElement.getChildElements().next();
//                    Iterator it1 = bodyElement.getChildElements();
//                    while(it1.hasNext())
//                    {
//                         bodyElement = (SOAPElement)it1.next();
//                         if (bodyElement.getLocalName().equals("CipherData"))
//                        {
//                            bodyElement = (SOAPElement)bodyElement.getChildElements().next();
//                            value = bodyElement.getValue();
//                            break;
//                        }
//                    }
//                }
//            }
//
//        }
//        catch (SOAPException ex)
//        {
//            ex.printStackTrace(System.out);
//        }
//
//
//        return value;

    }

    public String getSctId() {
        return sctId;
    }

    public String getSctIdentifier() {
        return sctIdentifier;
    }

    public SOAPElement getSctReference() {
        return sctReference;
    }

    protected void processMessage() {
        try {
            SOAPBody soapBody = rstResponse.getSOAPBody();
            String output = SoapUtils.soapMessageToString(rstResponse);
            System.out.println(output);

            SOAPElement soapElement = (SOAPElement) soapBody.getChildElements().next();

            soapElement = (SOAPElement) soapElement.getChildElements().next();
            Iterator it = (Iterator) soapElement.getChildElements();
            while (it.hasNext()) {
                SOAPElement bodyElement = (SOAPElement) it.next();

                if (bodyElement.getLocalName().equals("RequestedSecurityToken")) {
                    bodyElement = (SOAPBodyElement) bodyElement.getChildElements().next();

                    // Remove this node from the tree
                    bodyElement.detachNode();
                    securityTokenContext = (SOAPBodyElement) bodyElement.cloneNode(true);

                    // set Id
                    QName attrName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "u");
                    sctId = securityTokenContext.getAttributeValue(attrName);

                    // set Identifier
                    SOAPElement identifier = (SOAPElement) securityTokenContext.getChildElements().next();
                    sctIdentifier = identifier.getValue();
                } else if (bodyElement.getLocalName().equals("RequestedAttachedReference")) {
                    bodyElement = (SOAPBodyElement) bodyElement.getChildElements().next();

                    // Remove this node from the tree
                    bodyElement.detachNode();
                    sctReference = (SOAPBodyElement) bodyElement.cloneNode(true);
                } else if (bodyElement.getLocalName().equals("BinaryExchange")) {
                    binaryExchange = bodyElement.getValue();
                }
                if (bodyElement.getLocalName().equals("RequestedProofToken")) {
                    // Get encrypted key
                    bodyElement = (SOAPBodyElement) bodyElement.getChildElements().next();
                    Iterator it1 = bodyElement.getChildElements();
                    while (it1.hasNext()) {
                        bodyElement = (SOAPElement) it1.next();
                        if (bodyElement.getLocalName().equals("CipherData")) {
                            bodyElement = (SOAPElement) bodyElement.getChildElements().next();
                            cipherValue = bodyElement.getValue();
                        }
                    }
                }
            }

        } catch (SOAPException ex) {
            ex.printStackTrace(System.out);
        }

    }
}
