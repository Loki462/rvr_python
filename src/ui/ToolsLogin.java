package ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import retriever.PartnerRetriever;
import retriever.ToolingRetriever;
import utilities.JavaDBManager;
import utilities.SF_ORG_Analytics;
public class ToolsLogin extends Application {
	
	@FXML private TextField UserID;
	@FXML private PasswordField Password;	
	@FXML private ComboBox<Text> EnvironmentSelection;
	@FXML private Button Exit;
	@FXML private Button Login;
	@FXML private CheckBox Remember;
	
	private Logger logger = Logger.getLogger(PartnerRetriever.class.getCanonicalName());
		
	Stage localStage;
	Parent root;
	Scene homeScene ; 
	private String txtUserID, txtPassword, URL;	 
	public static String sfUserID, sfPassword, sfURL;
	public static boolean rememberUser;
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	private ToolingRetriever tr ; 

    
	public void launchUI() {
        launch();
    }
	
	public void start(Stage stage) throws Exception {
		localStage = stage;
		root = FXMLLoader.load(getClass().getResource("Login.fxml"));

        localStage.setTitle("Salesforce Quality Enforcer");
        Scene scene = new Scene(root); 
        scene.getStylesheets().add(getClass().getResource("Login.css").toExternalForm());
        localStage.setScene(scene);    
        localStage.setWidth(800);
        localStage.setHeight(600);
        localStage.show();        
    }
	
	public static String getUserName() {
		return sfUserID;
	}
		
	public static String getPassword() {
		return sfPassword;
	}
	
	public static String getURL() {
		return sfURL;
	}
	public void handleButtonAction(ActionEvent event) {		
		String eventSource = event.getSource().toString();
		System.out.println("Login Page buttons : "+eventSource);
		String strClickedButtonName = eventSource.split("'")[1]; 
		switch(strClickedButtonName) {
		case "Exit" :			
			logger.log(Level.INFO,"Existing Application");			
			Platform.exit();
			System.exit(0);
		case "Login" :			
			//For testing purpose only - to be removed before deployment///////////////////////////////////
			//UserID.setText("Use your Salesforce username");
	        //Password.setText("This will be a concatenated string of Password and Security token");	
//			UserID.setText("sayantest@cts.com");
//			Password.setText("salessales33C6NHgyTeP3ffESDf8m8FHwtOM");
			UserID.setText("lokesh+admin3@salesforce.com");
			Password.setText("LokiHarsha@11130PvRmU8bmVerybW76WJoNOngX");
	        txtUserID = UserID.getText();
			txtPassword = Password.getText();
			sfUserID = txtUserID;
			sfPassword = txtPassword;		
			//String txtEnvSelection = "Sandbox";	
			String txtEnvSelection = "Production";	
			if(txtEnvSelection.equals("Sandbox")) {
				URL = "https://test.salesforce.com/services/Soap/u/44.0";
				System.out.println("Sandbox is selected...");
			} else {
				System.out.println("Production is selected...");
				URL = "https://login.salesforce.com/services/Soap/u/44.0";				
			}
			sfURL = URL; 
			try {				
				try {
					System.out.println("User name : "+sfUserID+" , Password : "+sfPassword);
					System.out.println("Login Url : "+URL);
					//com.sforce.soap.partner.LoginResult mcLoginResult = login.Login.loginToSalesforce(sfUserID, sfPassword, sfURL);
					
					System.out.println("txtUserID:"+txtUserID);
					System.out.println("txtPassword:"+txtPassword);
					System.out.println("URL:"+URL);
					
					login.Login.login(txtUserID, txtPassword, URL);
					ToolingRetriever tr = new ToolingRetriever(ui.ToolsLogin.sfUserID, ui.ToolsLogin.sfPassword);	
					if(Remember.isSelected()) {
						rememberUser = true;
							//Save user id to database after successful login if user selected remember option 
							System.out.println("Update user table with remember user check");
				        	jdbc.insertUserTable(sfUserID, sfPassword, sfURL, tr.getOrgId(),SF_ORG_Analytics.timeStamp);
				        }
					jdbc.insertUserTable(sfUserID, sfPassword, sfURL, tr.getOrgId(),SF_ORG_Analytics.timeStamp);

					if(tr.getOrgId().length() > 0) {
						Stage homeStage = new Stage();
						HomePage newHome = new HomePage();				
						newHome.launchHome(homeStage);	
					}
				}
				catch(Exception ex) {
					logger.log(Level.INFO,"Error logging into Salesforce - please check user id, password and security token");
					//ex.printStackTrace();
					Alert loginErrorAlert = new Alert(AlertType.ERROR);
					loginErrorAlert.setTitle("Login Error");
					loginErrorAlert.setHeaderText("Login Error : ");
					loginErrorAlert.setContentText("Error while logging into Salesforce = 'Invalid username, password, security token; or user locked out.'");
			    	loginErrorAlert.setResizable(true);
			    	//alert.getDialogPane().setPrefSize(480, 320);
			    	loginErrorAlert.showAndWait();
				}
				
				

			}
			catch(Exception e) {
				logger.log(Level.INFO,"Error loading Home Screen");
				e.printStackTrace();
			}
		}
	}
}

//UserID.setText("cognizant.qeacrm@sfdc.com");
//Password.setText("Cognizant@04");
//UserID.setText("parantap.samajdar@voya.com.eb.accp");
//Password.setText("Cognizant@027N8htPuAga9RJKTUIVhuY1YU");
/////////////////////////////////////////////////////////////	
