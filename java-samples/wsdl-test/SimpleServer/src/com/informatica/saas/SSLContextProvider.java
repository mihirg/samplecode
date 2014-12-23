package com.informatica.saas;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
/**
 * 
 * This is based on the SecureChatSSLContextFactory from Netty tutorial. The differences are as follows:
 * <ul>
 * <li> The tutorial refers to a key store in a byte array form, and does not really explain how to generate it.
 * <li> This example acutally works with a generated key store. Use the following steps to generate the mystore.jks file which is used in code below
 * <ul>
 * <li> keytool -genkey -alias keyAlias -keyalg RSA -keypass changeit -storepass changeit -keystore  mystore.jks
 * </ul>
 * <li> keytool -export -alias keyAlias -file mycert.cer -keystore mystore.jks -storepass changeit
 *</ul>
 */
public class SSLContextProvider {

	private static SSLContext context = null;
	
	public static SSLContext getInstance() {
		synchronized (SSLContextProvider.class) {
			if (context != null)
				return context;
			
			try {
				context = SSLContext.getInstance("TLSv1");
	    		InputStream is = new FileInputStream(new File("mystore.jks"));
	    		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    		ks.load(is, "changeit".toCharArray());
	    		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    		kmf.init(ks, "changeit".toCharArray());
	    		
	    		context.init(kmf.getKeyManagers(), null, null);

				
			} catch (Exception exp) {
				context = null;
			}
			finally {
				return context;
			}
		}
	}
}
