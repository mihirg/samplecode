package in.gore;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InitiateOAuth extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
		//String client_id="3MVG9y6x0357HledXnULB3O1c295AcEdCsFVjmKwt_wsTJ8WHnkRBxNixuslhzDuBI8dZK78KoP6LNRVjzSoJ";
		StringBuffer buf = new StringBuffer();
		buf.append("https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=");
		buf.append(StaticConstants.client_id);
		buf.append("&redirect_uri=");
		buf.append(URLEncoder.encode(StaticConstants.redirect_uri, "UTF-8"));
		//response.sendRedirect("https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9y6x0357HledXnULB3O1c28yX_0c7g.Do8krazBuo6N5pJIk63juEAjdiIWxEaKEHVqPeIRda.NtICO2N&redirect_uri=https%3A%2F%2Flocalhost%3A8080%2Fsalesforce");
		response.sendRedirect(buf.toString());
	}
}
