/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package soapmessageposter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.soap.Detail;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

//saaj.jar
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import javax.xml.soap.SOAPBody;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author mgore
 */
public class Main {

    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TransformerConfigurationException, TransformerException {

        try {
            //com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump = true;
            //com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump=true;
            //com.sun.xml.ws.transport.http.HttpAdapter.dump=true;
            //com.sun.xml.internal.ws.transport.http.HttpAdapter.dump = true;
            //com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump = true;

            Main myMain = new Main();
            LogManager.getLogManager().readConfiguration(myMain.getClass().getResourceAsStream("/soapmessageposter/log.properties"));
            Logger log = Logger.getLogger("Test");

            
            InputStream is = myMain.getClass().getResourceAsStream("/soapmessageposter/input.xml");
            InputStreamReader isRead = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(isRead);
            StringBuilder sb = new StringBuilder();
            String str = null;
            while((str = bufReader.readLine()) != null) {
                sb.append(str);
            }
            String soapText = sb.toString();

            // Create SoapMessage
            MessageFactory msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage message = msgFactory.createMessage();
            MimeHeaders hdrs = message.getMimeHeaders();
            hdrs.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
            hdrs.addHeader("Expect", "100-continue");
            hdrs.addHeader("Accept-Encoding", "gzip,deflate");
            hdrs.addHeader("Connection", "Keep-Alive");

            SOAPPart soapPart = message.getSOAPPart();

            // Load the SOAP text into a stream source
            byte[] buffer = soapText.getBytes();
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            StreamSource source = new StreamSource(stream);

            // Set contents of message
            soapPart.setContent(source);

            message.saveChanges();

            // -- DONE
            SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
            SOAPConnection conn = factory.createConnection();
            //log.addHandler(new ConsoleHandler());
            //log.setLevel(Level.FINE);
            Level level = log.getLevel();
            log.info("fine message");
            Logger myLog = Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
            Level level1 = myLog.getLevel();
            myLog.addHandler(new ConsoleHandler());
            myLog.setLevel(Level.FINEST);
            level1 = myLog.getLevel();
            boolean loggable = myLog.isLoggable(Level.FINEST);
            SOAPMessage retVal = conn.call(message, "https://disco.crm4.dynamics.com/XRMServices/2011/Discovery.svc");

            Writer out = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(retVal.getSOAPPart().getContent(), new StreamResult(out));
            System.out.println(out.toString());
            //Detail detail = retVal.getSOAPBody().getFault().getDetail();
            //detail.getValue();

        } catch (SOAPException  e) {
            System.out.println("SOAPException : " + e);

        } catch (IOException  e) {
            System.out.println("IOException : " + e);
        }
    }



}
