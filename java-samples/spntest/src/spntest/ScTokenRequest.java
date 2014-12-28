/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spntest;

import java.io.ByteArrayInputStream;
import javax.xml.soap.*;
import java.text.*;
import java.util.*;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author agunta
 */
public class ScTokenRequest
{

     public static SOAPMessage getRSTSoapMessage(final String b64Token, final String serviceUrl, String context)
     {
         // Generate unique identifiers for msdId and contex
         String msgId = UUID.randomUUID().toString();
         if (context == null)
             context = UUID.randomUUID().toString();

         // SOAP template for SP negotiation protocol using WS-Trust
         String soapTemplate1 =
                  "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">"
                +        "<s:Header>"
                +                "<a:Action s:mustUnderstand=\"1\">"
                +                        "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue"
                +                "</a:Action>"
                +                "<a:MessageID>uuid:"
                +                      msgId
                +                "</a:MessageID>"
                +                "<a:ReplyTo>"
                +                        "<a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>"
                +                "</a:ReplyTo>"
                +                "<a:To s:mustUnderstand=\"1\">"
                +                        serviceUrl
                +                "</a:To>"
                +        "</s:Header>"
                +        "<s:Body>"
                +                "<t:RequestSecurityToken Context=\"uuid:" + context + "\" xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">"
                +                        "<t:TokenType>http://schemas.xmlsoap.org/ws/2005/02/sc/sct</t:TokenType>"
                +                        "<t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType>"
                +                        "<t:KeySize>256</t:KeySize>"
                +                        "<t:BinaryExchange ValueType=\"http://schemas.xmlsoap.org/ws/2005/02/trust/spnego\" EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">"
                +                                b64Token
                +                        "</t:BinaryExchange>"
                +                "</t:RequestSecurityToken>"
                +        "</s:Body>"
                +"</s:Envelope>";


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
            byte[] buffer                           = soapTemplate1.getBytes();
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
     public static SOAPMessage getRSTSoapMessageSts(final String stsUrl, final String serviceUrl, String userName, String password)
     {
         // Generate unique identifiers for msdId and contex
         String msgId = UUID.randomUUID().toString();

         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

         // Created time - now
         Date now = new Date();

         String createdTime = dateFormat.format(now);

         // Expires - after 5 minutes
         now.setMinutes(now.getMinutes() + 5);
         String expiresTime = dateFormat.format(now);


         // SOAP template for SP negotiation protocol using WS-Trust
         String soapTemplate1 =
                  "<s:Envelope xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">"
                +        "<s:Header>"
                +                "<a:Action s:mustUnderstand=\"1\">"
                +                        "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue"
                +                "</a:Action>"
                +                "<a:MessageID>uuid:"
                +                      msgId
                +                "</a:MessageID>"
                +                "<a:ReplyTo>"
                +                        "<a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>"
                +                "</a:ReplyTo>"
                +                "<a:To s:mustUnderstand=\"1\">"
                +                        stsUrl
                +                "</a:To>"
                +                "<o:Security s:mustUnderstand=\"1\" xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
                +                       "<u:Timestamp u:Id=\"_0\">"
                +                           "<u:Created>" + createdTime + "</u:Created>"
                +                           "<u:Expires>" + expiresTime + "</u:Expires>"
                +                       "</u:Timestamp>"
                +                       "<o:UsernameToken u:Id=\"uuid-a94e03b8-4f86-4057-a3a6-4e7df39ed38b-5\">"
                +                           "<o:Username>" + userName + "</o:Username>"
                +                           "<o:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + password + "</o:Password>"
                +                       "</o:UsernameToken>"
                +               "</o:Security>"
                +        "</s:Header>"
                +        "<s:Body>"
                +                "<trust:RequestSecurityToken xmlns:trust=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">"
                +                   "<wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">"
                +                       "<a:EndpointReference>"
                +                           "<a:Address>" + serviceUrl + "</a:Address>"
                +                       "</a:EndpointReference>"
                +                   "</wsp:AppliesTo>"
                +                   "<trust:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</trust:RequestType>"
                +               "</trust:RequestSecurityToken>"
                +        "</s:Body>"
                +"</s:Envelope>";


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
            byte[] buffer                           = soapTemplate1.getBytes();
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


}
