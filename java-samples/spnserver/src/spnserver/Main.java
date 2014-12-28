/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spnserver;

/**
 *
 * @author mgore
 */

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

import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.security.jgss.GSSUtil;
import java.util.UUID;
import javax.xml.soap.*;

public class Main {

    private Subject subject;
    
    public static void main(String[] args) {

        System.setProperty( "sun.security.krb5.debug", "true");
        System.setProperty( "java.security.auth.login.config", "./jaas.conf");
        System.setProperty("java.security.krb5.conf", "c:/krb5.conf");
        System.setProperty("sun.security.spnego.debug", "true");
        String username = "crm_web";
        String password = "Inf@1234";

        Main client = new Main();

        // Login to the KDC.
        try {
            client.login( username, password);
        }
        catch (LoginException exp) {
            exp.printStackTrace();
        }


    }


    public boolean login(String username, String password) throws LoginException {
        LoginContext loginCtx = new LoginContext("Server", new LoginCallbackHandler(username, password));
        loginCtx.login();
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
        byte[] token = null;
        byte[] tokenForPeer = null;
        byte[] tokenForEndpoint = new byte[0];

        String endpointSPN = null;
        GSSManager manager = GSSManager.getInstance();
        GSSContext context = null;
        GSSCredential clientCred = null;
        GSSCredential myCred = null;
        Oid krb5MechOid = new Oid("1.2.840.113554.1.2.2");
        //Oid krb5MechOid = new Oid("1.2.840.113554.1.2.2.3");
        Oid spnegoMechOid = new Oid("1.3.6.1.5.5.2");
        //first obtain it's own credentials...
        Oid krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1");
        GSSName serverName = manager.createName("crm_web@TEST.LOCAL", GSSName.NT_USER_NAME);

        Oid nulloid = null;
        myCred = manager.createCredential(null, GSSCredential.DEFAULT_LIFETIME, spnegoMechOid, GSSCredential.ACCEPT_ONLY);
        //...and create a context for this credentials...
        context = manager.createContext(myCred);

        //...then use that context to authenticate the calling peer by reading his
        //spnego token
        //String authorization = "YIGCBgYrBgEFBQKgeDB2oDAwLgYJKoZIgvcSAQICBgkqhkiG9xIBAgIGCisGAQQBgjcCAh4GCisGAQQBgjcCAgqiQgRAYD4GCiqGSIb3EgECAgMEADAuoAMCAQWhAwIBEKIUMBKgAwIBAaELMAkbB0NSTV9XRUKjDBsKdGVzdC5sb2NhbA==";
        //String authorization = "oYIF0zCCBc+gAwoBAaKCBcYEggXCYIIFvgYJKoZIhvcSAQICAQBuggWtMIIFqaADAgEFoQMCAQ6iBwMFACAAAACjggSiYYIEnjCCBJqgAwIBBaEMGwpURVNULkxPQ0FMoikwJ6ADAgECoSAwHhsEaHR0cBsWaXAtMGE3NGNkOWYudGVzdC5sb2NhbKOCBFgwggRUoAMCARKhAwIBAaKCBEYEggRCptYGf+AMPQ7c2kV7lhqdn/gQV7/tal/y0WQV/jAiY6vW0lcqSJNxnyonuEmgdF4GzzV3fvjTFPvuFK8neQWsUqD4Opd4KIglbo/8ypoaCctTBXPDxyaWsayFU/RFGbjNDiRDoCld7KvCGaDcMUbnvy/aKXG1HsusZbtJ+pR8tw9iwlD3sWauoC5HiqMDuOOJtykUGvYdja+fZrwREb0m5vKU7Ow98MITOCiAlpn1XDW6132ihQR1hKJQuxoWGW7mbbHpVIMTRrEJBNZKpSCM98braCKKCUVD+xodGwtO1GUJmhPRNl0ZNa42NeIwFOiTN9jUJ/hDlzjoZB4/6t2yY0hN2HiB4DedFywxTEoHQi5L0CD2UCMpaATyPZkeQsNNxoD+uvxXCt6quud/JRYHXygBM1F182MBYgS1cuJZH6g39RZ0Z2HUvDEXMb0iV/NzSOPpu8hkDIwVhIlsfwd+qJ82MRMML+WjNny8GzlPKDGAiX9P7gQPlabUo0xowqR9DIV4se3HVsgjm4RuyhM9RqUwlbjKZIbhdMpw6UiBZstj4vDIHJ/2tC6xYw18OGoRf9rRiNYTDsN9dUCdloLuFGNjHkawl9JnGkdho92y2Nl44KN5TtrwgBvSc4BSnX8iUERnnkl8dxd0LifLHizseP5lLEejt+bToJbKpUmYAS/iukQmGuxrkazT1N4MerZJLZbgRHaSEyoOawQjasksNGMSttoMLuoeMA8WR2ImyHFec7BWDyYbJKMC1dtjo8YT4J5ESu9lCHDh+OxVm/34gfhRKMYQw/7nOVxsHL4v0h+tyXxeeRaB9d4IltCQ69tdkAoxMc0UtWx0p4+xbuHRSzLRT8d2jTvovQinaTmYgTeLAGjYLzEsrIHmvT3JYn+a49A0IskdI6YAzcXA1Aemqs4GEldNmbyIxfCyQ6uTxszq1c7TjIxgELbwFT6YOy5I3IOU5CufeWfTTxKTm+0JtOwKRL8fDk297GznBSnnooPfGxQUeXSNrq5qfc3UUS9RhJocKGZh5+IX0Len4ieWhqdy+nsfthDg+N6BPnSjom9Mp5YtQI/qrx5ClLsBm3UH/c/vGMHFyMNy01xGlDmU4JVEERe184tIerzVEybmSm8vyZHYGlzgk3goZphU/X3XYWzDirf+TreEHPWST94VU0JGfSmo1amnhRGVygUkl7s8ovt4TFJwT//kc+jM8zXddHZBGv/Crzy4JQxZtRHrg2MLhpTRPTNmIvIeBYkKG+548/FFiAM0hqG1EhAq3HPlmyNykzz23Xkn5jMZwGPShD99OYiDbenaEKfgmU524Z+twi/WdZ1WL1M0MNAT9SR0KqnZwFGORlJxJ1l0/4hRsH7wztw1NV13oZmKWcishgQOAPVaw9OrmEh9uzH1pUFGP60wp0V3nviRqelrVHgVNZSca2FG8iow3wrSisANhuKcDKSB7TCB6qADAgESooHiBIHfrnIaVj24oMdBBJGt313B8iQqKPOeVbldlskkM4e+eaHC2e9Nrxc0rgw8dU/sdaJwP6hPU70gRNKfp9GnD9ys9lKS+IlJEMm1nZ1qvXs47JPCFdUNCI/ePAFcNkAVg+UNi+bXeDg7OK542tN23GKiMLUoYAGbOltNNT02lkadqcS4a8GeXuVlXuMr3xIlWZ162hHJkyXWxB9ucswPrI2M4fCNZYsoUhZYHcmdrBfv1O0TdwkkEYmMsnW9Xvk8ZbF1DNx/dGt3OG4pYKl/KZK/ZrUjM+NXuXUZMX8pGIuAFA==";

//        String authorization = "YIIF7gYGKwYBBQUCoIIF4jCCBd6gMDAuBgkqhkiC9xIBAgIGCSqGSIb3EgECAgYKKwYBBAGCNwICHgYKKwYBBAGCNwICCqKCBagEggWkYIIFoAYJKoZIhvcSAQICAQBuggWPMIIFi6ADAgEFoQMCAQ6iBwMFACAAAACjggSBYYIEfTCCBHmgAwIBBaEMGwpURVNULkxPQ0FMohQwEqADAgEBoQswCRsHQ1JNX1dFQqOCBEwwggRIoAMCARehAwIBAqKCBDoEggQ2IeRQ4G6iq39J2iI5ds5zo53ORIRvY01JNAk45p4KWeGGpyNOByIIDnbDubU6Q7CR/sBTMEERx75Rlg1mXIIWQ/JX6dEC8/z2w3HjhKfhFeLUIDXOQlaaB+TIs7edof6AZ3Yy/owDwhICgKejn+PEv7DmWvbd9z6XWdV2AxDx4kiYBAFzU0AmQLj82qhh2xKuNt0K+gJjrtP3H4oYL7XJ0AOxFGIzFJYC5eLX95/Phyp/8e+bPRvlOZXEVaDX6jb4N9/xQUsgPh+ZBR+YUxSrlWAsM698bhTeAdQyeh80dzya4hpWo3pOlSay1LX/kdpbnUJJC/xZmQrnjdQDiIv5QPnBWXB3smRYynxWDJKGpiqOLw8im8mXFIfj9Wb8vUBZXgScbHw414jmVeaNJCI6U64NOC4ZqCuxriPcylAykapTIwrIyuzyCTUt+mOOZWUQ1vsa6D8ZyMYvmpzEQdr6cyDDMmcj028E6bH/66YroHbpT4OF2vH9NZLCxA0K4i9YcRN8n1HBZJLH8msozoe5G6D4anRptSHlB89qhatwDpew2V47b4dyCY/WZDJsszRftjh2x+1iIm+jI1CxaonUdJ9Nh+3skMdVc/cncItaKqr3TeELDNMuNGaIlSLaEnC4ur5gi4zrTX+hV5KI/n+Z2rUATUa9jeqPHVSpxjjg6LXoCIedp1YGc8QY+02BDRONXDsYHasIsTb0OyFNdAmuOFLDW4EPIy6jYM5wae3/xWI5KyOeLOVOkzGXcwUnlmPJfKNwyCWG7VfGSJRX8/u+Gbu69U/BFe7/nd5N/zEX+AYfwp4L4UXs23jC7sXgaCMLB5QljEmr2H9CjumVWMtLFg6Z2WwcxsudMsyZbQKAUC8mSaSZqAcaRjZvKC/Apk4g6tPe4oL78lOHY6Qm6B9REf+cDaFPakagdw1buNPq/+fbhnxBWrq9BkJguh8M1Ft2mK9NHzyZILHfYHFpyvRVWIdD5GX4VPNriOJXIGtQ23jBim0PzCr/enebzjKhkwn9MGFSy7Oy4PFvWoFx2k0qe5FzG5fNng2mXMR6o5y7sQxWC593JKNoyYEI/8cQfFaxo8X+/8hDF6TbCCM6mlXRiluz3vbLouTQYbmiF/z6z2mTWKBBt0iDPmwf1ApKw72ckBNlCd0tw3zxoFmqUAkpq2ZbnTF7Df7ex0sQMg/IrIhQ5gaA7c9KGRnztNkRXz7xJLGOxoIDSKbbu/D3X0AxHohuLsPlfRXDCNme2REDPbMvfzbG+FphGEnCNk2DCcoXyD5CCrVo+782fnJXpyktOWUpCh6+uhdUuK8napT3S5bx64VqQ8BU77gTrDHiraxcqFbl1pifcR2FcSrPwCudzrHtPlCtdZUhkr/bs52y0hOr2cArWINhww5JyuHFniFtcWNXvgmt+l3rr/x5hG6Af3B+KfzzT6SB8DCB7aADAgEXooHlBIHi8/krCt70DgMGOThf9YM+ah8vi2nzEtOhz95q7+xlAaBpFY0n2ACGT0STN/UbMPS0TZg9dwco3nO5LwoSL/utgRYLCl/3kOShiQ3Sh750dziThbt6YltjIx/wy13dXvfYydEUa/6XGOlFisaz68ioeC8Iw4EbWpHCyqJmEjQCNRhrXaBaYFMU9ZVVHYbg/EhhjpurhWJ9zmA6KoaecTHzZAcY1cUavcsNMnzks17NuK8oHF3VhC+23/USzOL62IExhWL3kb4x3Wo0P/7YtZ4Ktg4h4cNIdP5Qny0INriYP5rlFw==";
        String authorization = "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";
        token = Base64.decode(authorization);
        tokenForPeer = context.acceptSecContext(token, 0, token.length);

        if (!context.isEstablished())
            return;
        if (tokenForPeer != null) {
            System.out.println("there is a token to send back to the peer, but I leave this out for now");
        }

        System.out.println("Context Established! ");
        System.out.println("Client principal is " + context.getSrcName());
        System.out.println("Server principal is " + context.getTargName());

        
    }

}
