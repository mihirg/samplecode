package com.informatica.saas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;


		 
public class SelfSignedCertificateGeneration {
    public static void oldain(String[] args) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException{
    		InputStream is = new FileInputStream(new File("mycert.cer"));
    		CertificateFactory factory = CertificateFactory.getInstance("X.509");
    		X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
    		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    		ks.load(null, "changeit".toCharArray());
    		ks.setCertificateEntry("mihirtest", cert);
    		 // Set up key manager factory to use our key store
            String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null) {
                algorithm = "SunX509";
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, "changeit".toCharArray());

    		System.out.println(ks.getCertificate("mihirtest"));
    		System.out.println(cert);
	    }

    
    public static void main(String[] args) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException{
		InputStream is = new FileInputStream(new File("mystore.jks"));
//		CertificateFactory factory = CertificateFactory.getInstance("X.509");
//		X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(is, "changeit".toCharArray());
//		ks.setCertificateEntry("mihirtest", cert);
		 // Set up key manager factory to use our key store
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(ks, "changeit".toCharArray());

//		System.out.println(ks.getCertificate("mihirtest"));
//		System.out.println(cert);
    }


}


