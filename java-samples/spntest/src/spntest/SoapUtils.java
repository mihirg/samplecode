/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spntest;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import com.sun.org.apache.xml.internal.security.encryption.*;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.SAXException;

import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;


/**
 *
 * @author agunta
 */
public class SoapUtils
{
     public static void encryptElement(SecretKey key, SOAPMessage message, SOAPElement element, String refId, Boolean encryptContent) throws XMLEncryptionException, SOAPException, Exception
     {
         XMLCipher cipher  = XMLCipher.getInstance(XMLCipher.AES_256);
         cipher.init(XMLCipher.ENCRYPT_MODE, key);

         Document root = (Document)message.getSOAPPart();

         // Add KeyInfo element to the EncryptedData
         EncryptedData encryptedDataElement = cipher.getEncryptedData();
         com.sun.org.apache.xml.internal.security.keys.KeyInfo keyInfo = new com.sun.org.apache.xml.internal.security.keys.KeyInfo(root);
         encryptedDataElement.setKeyInfo(keyInfo);
         encryptedDataElement.setId(refId);

         SOAPElement kiElement = (SOAPElement) keyInfo.getElement();

         if (kiElement != null)
         {
             SOAPElement stRef = kiElement.addChildElement("SecurityTokenReference", "o", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
             SOAPElement ref = stRef.addChildElement("Reference", "o", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
             ref.addAttribute(new QName("ValueType"), "http://schemas.xmlsoap.org/ws/2005/02/sc/dk");
             ref.addAttribute(new QName("URI"), "#_dk1");
         }

         // Encrypt
         cipher.doFinal(root, element, encryptContent);

     }


     public static void addTimestamp(SOAPElement security) throws SOAPException
     {
        // Add timestamp element to the security node
        SOAPElement timestamp = security.addChildElement("Timestamp", "u", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

        // Add Id to the time stamp which will be used as a reference for digital signature
        timestamp.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "u"), "_0");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Created time - now
       // Calendar now = Calendar.getInstance();
        Date now = new Date();

        String createdTime = dateFormat.format(now);

        // Expires - after 5 minutes
        //now.add(Calendar.MINUTE, 5);
        now.setMinutes(now.getMinutes() + 5);
        String expiresTime = dateFormat.format(now);


        SOAPElement created = timestamp.addChildElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Created", "u"));
        created.addTextNode(createdTime);
        SOAPElement expires = timestamp.addChildElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Expires", "u"));
        expires.addTextNode(expiresTime);

    }
     public static SOAPHeaderElement addSecurityNode(SOAPHeader header) throws SOAPException
     {
         // Add security element to the SOAP header
         SOAPHeaderElement security = header.addHeaderElement(
                 new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                 "Security",
                 "o"));

         return security;
    }

     public static void setId(SOAPElement element, String id) throws SOAPException
     {
         element.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "u"), id);
     }

     public static void generateSignature(Node securityNode, SecretKey signKey, SOAPElement keyRef, ArrayList<String> references) throws Exception
     {
        try
        {
            if ((references == null) || (references.isEmpty()))
            {
                throw new IllegalArgumentException("references list in empty");
            }

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
            DigestMethod digestMethod = factory.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null);

            TransformParameterSpec transformSpec = null;
            Transform transform = factory.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", transformSpec);

            ArrayList transformList = new ArrayList();
            transformList.add(transform);

            ArrayList referenceList = new ArrayList();
            for (String ref:references)
            {
                javax.xml.crypto.dsig.Reference reference = factory.newReference(ref, digestMethod, transformList, null, null);

                referenceList.add(reference);
            }

            CanonicalizationMethod cm = factory.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#", (C14NMethodParameterSpec) null);

            SignatureMethod sm = factory.newSignatureMethod("http://www.w3.org/2000/09/xmldsig#hmac-sha1", null);


            SignedInfo signedInfo = factory.newSignedInfo(cm, sm, referenceList);


            DOMSignContext signContext = new DOMSignContext(signKey, securityNode);

            KeyInfoFactory keyFactory = KeyInfoFactory.getInstance();
            DOMStructure domKeyInfo = new DOMStructure(keyRef);
            KeyInfo keyInfo = keyFactory.newKeyInfo(Collections.singletonList(domKeyInfo));
            XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(signContext);

        }
        catch (Exception ex)
        {
            throw( new Exception("Failed to generate digital signature: " + ex.getMessage()));
        }
     }


     public static SOAPMessage stringToSoapMessage(String strMessage )
     {
        SOAPMessage message = null;
        try
        {
            // Create SoapMessage
            MessageFactory msgFactory       = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            message                                  = msgFactory.createMessage();
            MimeHeaders hdrs = message.getMimeHeaders();
            hdrs.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
            hdrs.addHeader("Expect", "100-continue");
            hdrs.addHeader("Accept-Encoding", "gzip,deflate");
            hdrs.addHeader("Connection", "Keep-Alive");

            SOAPPart soapPart             = message.getSOAPPart();

            // Load the SOAP text into a stream source
            byte[] buffer                           = strMessage.getBytes();
            ByteArrayInputStream stream     = new ByteArrayInputStream(buffer);
            StreamSource source                = new StreamSource(stream);

            // Set contents of message
            soapPart.setContent(source);

            message.saveChanges();

        }
        catch (SOAPException  e)
        {
            System.out.println("SOAPException : " + e);
        }

        return message;

     }

    public static String soapMessageToString (SOAPMessage msg)
    {
        try
        {
            Writer out = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(msg.getSOAPPart().getContent(), new StreamResult(out));
            return out.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        return "";
    }

    public static SOAPMessage formatSoapMessage (SOAPMessage msg)
    {
         SOAPMessage message = null;
         String strMsg = null;
        try
        {
            Writer out = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(msg.getSOAPPart().getContent(), new StreamResult(out));
            strMsg =  out.toString();

            // Create SoapMessage
            MessageFactory msgFactory       = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            message                            = msgFactory.createMessage();
            MimeHeaders hdrs = message.getMimeHeaders();
            hdrs.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
            //hdrs.addHeader("Expect", "100-continue");
            //hdrs.addHeader("Accept-Encoding", "gzip,deflate");
            hdrs.addHeader("Connection", "Keep-Alive");



            SOAPPart soapPart             = message.getSOAPPart();

            // Load the SOAP text into a stream source
            byte[] buffer                           = strMsg.getBytes();
            ByteArrayInputStream stream     = new ByteArrayInputStream(buffer);
            StreamSource source                = new StreamSource(stream);

            // Set contents of message
            soapPart.setContent(source);

            message.saveChanges();


        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        return message;
    }

     public Document toDocument(SOAPMessage soapMsg)   throws TransformerConfigurationException, TransformerException, SOAPException, IOException
     {
        Source src = soapMsg.getSOAPPart().getContent();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform(src, result);
        return (Document)result.getNode();
      }

     public static void checkConnection(String serviceUrl, final int timeout) throws IOException
     {
          URL url = new URL(null,
                      serviceUrl ,
                    new URLStreamHandler() { // Anonymous (inline) class
                    @Override
                    protected URLConnection openConnection(URL url) throws IOException {
                    URL clone_url = new URL(url.toString());
                    HttpURLConnection clone_urlconnection = (HttpURLConnection) clone_url.openConnection();
                    // TimeOut settings
                    clone_urlconnection.setConnectTimeout(timeout);
                    clone_urlconnection.setReadTimeout(timeout);
                    return(clone_urlconnection);
                    }
                });

            URLConnection openConnection = url.openConnection();
            InputStream openStream = url.openStream();
     }

     public static SOAPElement stringToSOAPElement(SOAPHeader header, String xmlText)
     {
        try
        {
            // Load the XML text into a DOM Document
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            InputStream stream  = new ByteArrayInputStream(xmlText.getBytes());
            Document doc = builderFactory.newDocumentBuilder().parse(stream);

            // Use SAAJ to convert Document to SOAPElement
            // Create SoapMessage
            MessageFactory msgFactory = MessageFactory.newInstance();
            SOAPMessage    message    = msgFactory.createMessage();
            SOAPBody       soapBody   = message.getSOAPBody();
            SOAPHeader       soapHeader   = message.getSOAPHeader();



            // This returns the SOAPBodyElement
            // that contains ONLY the Payload
            return soapBody.addDocument(doc);

        } catch (SOAPException  e) {
            System.out.println("SOAPException : " + e);
            return null;

        } catch (IOException  e) {
            System.out.println("IOException : " + e);
            return null;

        } catch (ParserConfigurationException  e) {
            System.out.println("ParserConfigurationException : " + e);
            return null;

        } catch (SAXException  e) {
            System.out.println("SAXException : " + e);
            return null;

        }
    }
}
