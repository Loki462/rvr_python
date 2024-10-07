package login;
//import com.sforce.soap.enterprise.EnterpriseConnection;
//import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class Login {
	
	public static MetadataConnection login(String USERNAME, String PASSWORD, String URL) throws ConnectionException {			
		LoginResult loginResult = loginToSalesforce(USERNAME,PASSWORD,URL);	
		return createMetadataConnection(loginResult);
		
//		System.out.println("Metadata Server URL : "+loginResult.getMetadataServerUrl());
//		System.out.println("Metadata Session ID : "+loginResult.getSessionId());
//		System.out.println("Server URL : "+loginResult.getServerUrl());
//		System.out.println("Metadata User Info : "+loginResult.getUserInfo());
//		System.out.println("Metadata User ID : "+loginResult.getUserId());
	}

	private static MetadataConnection createMetadataConnection(final LoginResult loginResult) throws ConnectionException {
		final ConnectorConfig config = new ConnectorConfig();
		config.setServiceEndpoint(loginResult.getMetadataServerUrl());
		config.setSessionId(loginResult.getSessionId());

		return new MetadataConnection(config);
	}
		
	private static LoginResult loginToSalesforce(final String USERNAME, final String PASSWORD, final String loginURL) throws ConnectionException {
		final ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint(loginURL);
		config.setServiceEndpoint(loginURL);
		config.setManualLogin(true);
		
		return (new PartnerConnection(config)).login(USERNAME, PASSWORD);
	}
	
	public static LoginResult getHomePageUiComponent(String USERNAME, String PASSWORD, String URL) throws ConnectionException {
		return loginToSalesforce(USERNAME, PASSWORD, URL);
	}

}