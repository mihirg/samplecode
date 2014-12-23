package com.informatica.saas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.apache.http.Header;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;


public class DiscardServer {
	 public static void startHttp(String[] args) throws Exception {
		 ChannelFactory factory =
			 new NioServerSocketChannelFactory(
					 Executors.newCachedThreadPool(),
					 Executors.newCachedThreadPool());
		 
		 ServerBootstrap bootstrap = new ServerBootstrap(factory);

		 bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			 public ChannelPipeline getPipeline() {
				 ChannelPipeline pipeline = Channels.pipeline();
				 pipeline.addLast("decoder", new HttpRequestDecoder());
				 pipeline.addLast("encoder", new HttpResponseEncoder());
				 pipeline.addLast("handler", new DiscardServerHandler());
				 return pipeline;
			 }
		 });
		 
		 //bootstrap.setOption("child.tcpNoDelay", true);
		 //bootstrap.setOption("child.keepAlive", true);
		 bootstrap.bind(new InetSocketAddress(8080));
	 }

	 
	 public static void main(String[] args) throws Exception {
		 ChannelFactory factory =
			 new NioServerSocketChannelFactory(
					 Executors.newCachedThreadPool(),
					 Executors.newCachedThreadPool());
		 
		 ServerBootstrap bootstrap = new ServerBootstrap(factory);

		 bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			 public ChannelPipeline getPipeline() {
				 ChannelPipeline pipeline = Channels.pipeline();
				 SSLEngine sslEngine = SSLContextProvider.getInstance().createSSLEngine();
				 sslEngine.setUseClientMode(false);
				 pipeline.addLast("ssl", new SslHandler(sslEngine));
				 pipeline.addLast("decoder", new HttpRequestDecoder());
				 pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				 pipeline.addLast("encoder", new HttpResponseEncoder());
				 pipeline.addLast("deflator", new HttpContentCompressor());
				 pipeline.addLast("handler", new SSLServerHandler());
				 return pipeline;
			 }
		 });
		 
		 //bootstrap.setOption("child.tcpNoDelay", true);
		 //bootstrap.setOption("child.keepAlive", true);
		 bootstrap.bind(new InetSocketAddress(8080));
		 
		 Runnable r = new Runnable() {

			@Override
			public void run() {
				 // salesforce code
				 String url = "https://login.salesforce.com/services/oauth2/token";
				 HttpPost post = new HttpPost(url);
				 List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				 nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
				 nvps.add(new BasicNameValuePair("code", "aPrxA6bDRmdSUjwh0EYdfEnmwpfFG__bur7WXzp_0WXfEEs.6XA8WkELzLx9NIrBRdU3QiK8QQ=="));
				 nvps.add(new BasicNameValuePair("client_id", "3MVG9y6x0357HledXnULB3O1c28yX_0c7g.Do8krazBuo6N5pJIk63juEAjdiIWxEaKEHVqPeIRda.NtICO2N"));
				 nvps.add(new BasicNameValuePair("client_secret", "324026928215762764"));
				 nvps.add(new BasicNameValuePair("redirect_uri", "https://localhost:8080/salesforce"));
				 try {
					 System.out.println(URLEncodedUtils.format(nvps, HTTP.UTF_8));
					 UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
					 entity.setContentType("application/x-www-form-urlencoded");
					post.setEntity(entity);
					
					//post.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
					System.out.println(post.toString());
					HttpClient cl = new DefaultHttpClient();
					HttpResponse resp = cl.execute(post);
				      BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				      String line = "";
				      while ((line = rd.readLine()) != null) {
				        System.out.println(line);
				      }
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				 
				
			}
			 
		 };
		 
		 //new Thread(r).start();
	 }

}
