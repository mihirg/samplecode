package in.gore;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReceiveCallback extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
		
		String code = req.getParameter("code");
		code = URLDecoder.decode(code, "UTF-8");
		
		URL url = new URL("https://login.salesforce.com/services/oauth2/token");
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("grant_type", "authorization_code");
		params.put("code",code);
		params.put("client_id",StaticConstants.client_id);
		params.put("client_secret",StaticConstants.client_secret);
		params.put("redirect_uri", StaticConstants.redirect_uri);
		
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		
		try {
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setRequestProperty("charset", "utf-8");
			OutputStream httpOut = conn.getOutputStream();
			httpOut.write(postDataBytes);
			httpOut.flush();
			httpOut.close();
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			
			StringBuffer output = new StringBuffer();
			int c;
			while ((c = in.read()) > 0)
				output.append((char)c);
			
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(output.toString());
			out.flush();			
		} catch (Exception exp) {
			response.setContentType("application/text");
			PrintWriter out = response.getWriter();
			out.print(exp.toString());
			out.flush();
		}
		
	}
}
