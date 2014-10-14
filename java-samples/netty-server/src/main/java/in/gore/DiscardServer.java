package in.gore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

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
				 pipeline.addLast("handler", new HttpServerHandler());
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
		 
	 }

}
