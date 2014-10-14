package in.gore;

import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class SSLServerHandler extends SimpleChannelHandler {
	
	private String consumerKey = "3MVG9y6x0357HledXnULB3O1c28yX_0c7g.Do8krazBuo6N5pJIk63juEAjdiIWxEaKEHVqPeIRda.NtICO2N";
	private String consumerSecret = "324026928215762764";
	   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
	    	HttpRequest req = (HttpRequest)e.getMessage();
	    	System.out.println(req.getMethod().toString() + " " + req.getUri());
	    	ChannelBuffer content = req.getContent();
	    	 for (Entry<String, String> h: req.getHeaders()) {
	    		 System.out.println(h.getKey() + " = " + h.getValue());
			 }
	    	System.out.println(content.toString(CharsetUtil.UTF_8));
	    	System.out.flush();
	    	
	   		String resp = new String("Message Received");
	   		//buf.writeBytes(resp.getBytes());
	   		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	   		response.setContent(ChannelBuffers.copiedBuffer(resp, CharsetUtil.UTF_8));
	   	    response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

	   		ChannelFuture future = e.getChannel().write(response);
	   		future.addListener(ChannelFutureListener.CLOSE);
	    }

	   
	    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
	        e.getCause().printStackTrace();
	        
	        Channel ch = e.getChannel();
	        ch.close();
	    }


}
