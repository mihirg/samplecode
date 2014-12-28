/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spntest;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import java.net.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.misc.BASE64Encoder;
import sun.security.jgss.ProviderList;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.UUID;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.xml.soap.*;

/**
 *
 * @author mgore
 */
public class Main {

    private static Oid krb5Oid;
    private Subject subject;
    private byte[] serviceTicket;
    private String serverPrinciple;
    private static String discoveryUrl;


        public static boolean compareURI(URI input, URI output) {

        if (input.getScheme().compareTo(output.getScheme()) != 0)
            return false;

        if (input.getHost().compareTo(output.getHost()) != 0)
            return false;

        if (input.getPath().compareTo(output.getPath()) != 0)
            return false;

        int ipPort = input.getPort();
        int opPort = output.getPort();        

        if (ipPort == opPort)
            return true;

        // If scheme is http, and one url is explicitly specifying 80 as the port while other is not
        // the URLs are acutally okay as far as our connector is concerned.
        // If scheme is https and one url is explicitly speficying 443 as portwhile the other is not,
        // then the URLs are actually okay as far as our connectors are concerned.

        if (input.getScheme().compareToIgnoreCase("http") == 0)
        {
            if ((ipPort == 80 && opPort == -1) || (ipPort == -1 && opPort == 80))
                return true;
            else
                return false;
        }
        else if (input.getScheme().compareToIgnoreCase("https") == 0) {
            if ((ipPort == 443 && opPort == -1) || (ipPort == -1 && opPort == 443))
                return true;
            else
                return false;
        }

        return false;
    }
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] a) throws Exception {
        System.setProperty( "sun.security.krb5.debug", "true");
        System.setProperty( "java.security.auth.login.config", "./jaas.conf");
        System.setProperty("java.security.krb5.conf", "c:/krb5.conf");
        System.setProperty("sun.security.spnego.debug", "true");

        String username = "Administrator";
        String password = "Inf@1234";
        Main.discoveryUrl = "http://ip-0a74cd9f.test.local:5555/XRMServices/2011/Discovery.svc";
        //Main.discoveryUrl = "http://ip-0AF88EFF.testinf.com:5555/XRMServices/2011/Discovery.svc";
        
        //ProviderList plist = new ProviderList(0,true);
        //Oid krb5User2User = new Oid("1.2.840.113554.1.2.2.3");
        //plist.getMechFactory(krb5User2User);
        // Oid mechanism = use Kerberos V5 as the security mechanism.
        krb5Oid = new Oid( "1.2.840.113554.1.2.2");
        Main client = new Main();
        
        URL url1 = new URL("https://dnb.crm.dynamics.com:443/XRMServices/2011/Discovery.svc");
        URL url2 = new URL("https://dnb.crm.dynamics.com/XRMServices/2011/Discovery.svc");
        Main.compareURI(url1.toURI(), url2.toURI());
        
        // Login to the KDC.
        try {
            client.login( username, password);
        }
        catch (LoginException exp) {
            exp.printStackTrace();
        }
        
    }

    public boolean login(String username, String password) throws LoginException {
        LoginContext loginCtx = new LoginContext("Client", new LoginCallbackHandler(username, password));
        loginCtx.login();
        // get the Configuration
	
	Configuration  config = (Configuration)java.security.AccessController.doPrivileged
		(new java.security.PrivilegedAction() {
		public Object run() {
		    return Configuration.getConfiguration();
		}
	    });
	

	// get the LoginModules configured for this application
	AppConfigurationEntry[] entries = config.getAppConfigurationEntry("Client");
        if (entries.length == 0)
            return false;
        assert(entries.length == 1);
        //this.serverPrinciple = (String)entries[0].getOptions().get("serviceprincipal");
        this.subject = loginCtx.getSubject();

        Subject.doAs(subject, new PrivilegedAction() {

            public Object run() {
                try {
                    initializeContext();
                    return null;
                }
                catch (GSSException gsexp) {
                    gsexp.printStackTrace();
                    System.out.println(gsexp.getMajor());

                }
                catch (Exception exp) {
                    exp.printStackTrace();
                }
                return null;
            }
        });
        return true;

    }

    public void initializeContext() throws GSSException, SOAPException, Base64DecodingException {
        GSSManager manager = GSSManager.getInstance();
        Oid spnegoOid = new Oid("1.3.6.1.5.5.2");
        Oid krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1");
        //Oid krb5PrincipalNameType = new Oid("1.2.840.48018.1.2.2");
        //GSSName serverName = manager.createName("HOST/IP-0A74CD9F.test.local", krb5PrincipalNameType);
        GSSName serverName = manager.createName("crm_web@TEST.LOCAL", krb5PrincipalNameType);
        //GSSName serverName = manager.createName("krbtgt/TEST.LOCAL", krb5PrincipalNameType);
        //GSSName serverName = manager.createName("host/ip-0AF88EFF.test.local", krb5PrincipalNameType);

        GSSName userGssName = manager.createName("user1@TEST.LOCAL", GSSName.NT_USER_NAME);
        String str = System.getProperty("java.security.auth.login.config.Client.serviceprincipal");


        // Acquire credentials for the user
        GSSCredential userCreds = manager.createCredential(userGssName, GSSCredential.DEFAULT_LIFETIME, spnegoOid, GSSCredential.INITIATE_ONLY);

        // Instantiate GSS context with the service principal and user credentials
        GSSContext gssContext = manager.createContext(serverName, spnegoOid, userCreds, GSSContext.DEFAULT_LIFETIME);
        //GSSContext gssContext = manager.createContext(userCreds);

        // Set GSS context attributes: Mutual authentication, confidentiality, integrity and delegation
        gssContext.requestMutualAuth(true);
        gssContext.requestConf(true);
        gssContext.requestInteg(true);
        gssContext.requestCredDeleg(true);

        // Generate the initial token: input token is ignored on the first call
        //String wcfToken = "TlRMTVNTUAABAAAAt4IY4gAAAAAAAAAAAAAAAAAAAAAGAbAdAAAADw==";
        //String wcfToken = "YIGCBgYrBgEFBQKgeDB2oDAwLgYJKoZIgvcSAQICBgkqhkiG9xIBAgIGCisGAQQBgjcCAh4GCisGAQQBgjcCAgqiQgRAYD4GCiqGSIb3EgECAgMEADAuoAMCAQWhAwIBEKIUMBKgAwIBAaELMAkbB0NSTV9XRUKjDBsKdGVzdC5sb2NhbA==";
        //byte[] token = Base64.decode(wcfToken);
        byte[] token = new byte[0];
        token = gssContext.initSecContext(token, 0, token.length);

        String context = UUID.randomUUID().toString();

        while (!gssContext.isEstablished()) {
            //String b64Token = "YIGCBgYrBgEFBQKgeDB2oDAwLgYJKoZIgvcSAQICBgkqhkiG9xIBAgIGCisGAQQBgjcCAh4GCisGAQQBgjcCAgqiQgRAYD4GCiqGSIb3EgECAgMEADAuoAMCAQWhAwIBEKIUMBKgAwIBAaELMAkbB0NSTV9XRUKjDBsKdGVzdC5sb2NhbA==";
            String b64Token = Base64.encode(token);
            System.out.println(b64Token);
            SOAPMessage request = ScTokenRequest.getRSTSoapMessage(b64Token, Main.discoveryUrl, context);
            SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
            SOAPConnection conn = factory.createConnection();
            SOAPMessage response = conn.call(request, Main.discoveryUrl);
            String val = SoapUtils.soapMessageToString(response);
            System.out.println(val);
            ScTokenResponse resp = new ScTokenResponse();
            resp.setMessage1(response);
            String tempToken = resp.getBinaryToken();
            //String tempToken = "oYIEPjCCBDqgAwoBAaELBgkqhkiC9xIBAgKiggQkBIIEIGCCBBwGCiqGSIb3EgECAgMEATCCBAqgAwIBBaEDAgERooID/GGCA/gwggP0oAMCAQWhDBsKVEVTVC5MT0NBTKIfMB2gAwIBAqEWMBQbBmtyYnRndBsKVEVTVC5MT0NBTKOCA7wwggO4oAMCARKhAwIBAqKCA6oEggOm2kNtDhUTlpQv5gDBwWiWmu+Q6DIO1rV/GytQiBdH+1/ju/9J7goF/XXfd/wep5x5qDQW4/nvZWRl9Y4q0Fp9Y9Gwhxwu25s3OcEzJC8NcoaClxP8LC2uEpOjW55TvWfZIuB89A9Qq227GFeLGMVAI1fz3oDO6tkDA84rSLVqNhdcPagRtYb+0BWNNUqeglh/D6PWfYI8ZuRVsoQR1ZWVXlsHnPPn4vIUprVO2NIxPRdRis1Kdst72LK2w9B0RzdhXBi+dpRdkkd2GcokZTHHIzx9tVydvGTzXFriuYN1u+zrApo++7YUw4HKdvjV6YOv2K6oLUzrTAps8LhuWqNJUibgNDCI+syWua4zQVR8iubIS4TwOkUWaTsLJnTHWrb/CeD/ui3LwZyHtBO1XlYRMdmXD1J9G4nYMXfNbYMdK0KHcYoKmcC74KdigwC3FnDjYD/0iUGtRwZyEN00o9fGcg6X807cevOZNtKHxrK5plBmp/qTAG/8t22xLCFYc9PawKbSnlDyqBgHFv2OEvj6SBsBT2GSKspbxpTegnlvow+KgHF5NrVHaRdkky08GwUbsABAh5lqZ5oYcb4rXNUW0ECweC/PT/H/SndA6i0df6kTkRwNx8sbbJnUYmIZ98daWD/Dt2NjDBq3EqadWxxtm2rSUf+FI5O8W0YRWLQS8JEjyd3xC/X6aMf+S43Y3poIiUUxRZ2LwkoIfRNsLgD80MSHsir1rztwNs7fwiNXF7Jcn+TUgSl2r5DIsUUGwb8ptncM+0L7IE1McICxp5S1VxoLVhuX8jJfO11emNCudcAKAWsRfVvGYO1ePeEOKwtz0jSTCredd9TALe1vNjRSLJxUeOcukCVW8cjLBoifmnNPAAt0xx61SfssGl0FGGWVk7besSB9dBliqqjPBYVBQdKZtFvaOdCMGZuAd+l5l+ZVZ+kyd0dIn05PNWrQsKSuCtZbVgXfp71jnfHfXbFGVk2ODf/MWqvF9UYj8Uh2Ko/zF5wqRNuV8PeHpRBa1UvRD7UONPs8QSfGHjE32EGb88KxAWKdAdK+nu8npbf+4OtjLr1ozpFNY5v/YQpoV7uODYrrMqfl/jZQUx3UiJS4WMhDW6MSOnTbD6EjWN1X64Cm6XiisvdM8M4QDl6h6PJQDcmLJSufeutTfJ6kw4gmY6I/BxZJqD9Zi6vXq3dAh6PcjKUI8hioZoPv682YnsxmUeX5gcBKTaYgGXiNyBV+vH1mH9ROsw==";
            token = Base64.decode(tempToken);
            token = gssContext.initSecContext(token, 0, token.length);
        }
    }

}
