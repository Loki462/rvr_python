package ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sforce.ws.ConnectionException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import login.Login;
import retriever.DataRetriever;
import retriever.PartnerRetriever;
import retriever.ToolingRetriever;
import utilities.ConfigDifferenceCalculator;
import utilities.DateUtil;
import utilities.Excel_Generator;
import utilities.JavaDBManager;
import utilities.SF_ORG_Analytics;
import utilities.XML_Generator;

public class HomePage implements Initializable{
	@FXML private TabPane SFDCComparisonTabPane;
	@FXML private Tab LatestConfigComparisonTab;
	@FXML private Tab HistoricalComparisonTab;
	@FXML private Tab QuickViewTab;
	
	@FXML private TextField LCT_TextShowCurrentConfigDate;
	@FXML private DatePicker LCT_DPSelectLastConfigFromDate_First;
	@FXML private DatePicker LCT_DPSelectLastConfigToDate_First;
	@FXML private ComboBox<String> LCT_SelectFinalConfigFromDate_First;
	@FXML private Button LCT_BtnGetLatestOrgConfigurations;
	@FXML private Button LCT_BtnConfirm_First;
	@FXML private Button LCT_BtnCompareOrgConfigurations;
	@FXML private Label HCT_ErrorLabel;
	@FXML private Label LCT_ErrorLabel;
	
	@FXML private DatePicker HCT_DPSelectLastConfigFromDate_First;
	@FXML private DatePicker HCT_DPSelectLastConfigToDate_First;
	@FXML private ComboBox<String> HCT_SelectFinalConfigFromDate_First;
	@FXML private DatePicker HCT_DPSelectLastConfigFromDate_Second;
	@FXML private DatePicker HCT_DPSelectLastConfigToDate_Second;
	@FXML private ComboBox<String> HCT_SelectFinalConfigFromDate_Second;
	@FXML private Button HCT_BtnConfirm_Second;
	@FXML private Button HCT_BtnConfirm_First;
	@FXML private Button HCT_BtnCompareOrgConfigurations;
	
	@FXML private TextField QVT_EnterNDays;
	@FXML private Button QVT_BtnCompareOrgConfigurations;

	@FXML private TextField TextOrganizationName;
	@FXML private TextField TextOrganizationInstance;
	@FXML private TextField TextOrganizationID;
	@FXML private TextField TextOrganizationEdition;
	@FXML private TextField TextLoginUserName;
	@FXML private TextField TextOrgPrimaryContact;
	@FXML private TextField TextLoginUserEmailID;
	@FXML private TextField TextLoginUserProfileID;
	@FXML private TextField TextSandboxFlag;
	@FXML private TextField TextConnectionAPIEndPointURL;
	@FXML private TextField TextShowCurrentConfigDate;

	@FXML private DatePicker DPSelectLastConfigFromDate_First;
	@FXML private Tooltip TTSelectLastConfigFromDate_First;
	@FXML private DatePicker DPSelectLastConfigToDate_First;
	@FXML private Tooltip TTSelectLastConfigToDate_First;
	
	@FXML private DatePicker DPSelectLastConfigFromDate_Second;
	@FXML private Tooltip TTSelectLastConfigFromDate_Second;
	@FXML private DatePicker DPSelectLastConfigToDate_Second;
	@FXML private Tooltip TTSelectLastConfigToDate_Second;
	
	@FXML private ProgressIndicator PIProgressIndicator;
	
	@FXML private BarChart<String,Number> finalOrgComponentsBarChart;
	
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
	
	@FXML private TextArea ApexBody;
	@FXML private TextArea ValidationRules;
	@FXML private TextArea TriggerList;
	@FXML private Label LblSFDCImpactedObjects;
	
	@FXML private Label LblOrganizationInformation;
	@FXML private Label LblOrganizationName;
	@FXML private Label LblOrganizationInstance;
	@FXML private Label LblOrganizationID;
	@FXML private Label LblOrganizationEdition;
	@FXML private Label LblLoginUserInfo;
	@FXML private Label LblLoginUserName;
	@FXML private Label LblLoginUserRoleID;
	@FXML private Label LblLoginUserEmailID;
	@FXML private Label LblLoginUserProfileID;
	@FXML private Label LblConnectionInformation;
	@FXML private Label LblConnectionAPIVersion;
	@FXML private Label LblConnectionAPIEndPointURL;
	@FXML private Label LblDateSelectionSection;
	@FXML private Label LblCurrentConfigDate;
	@FXML private Label LblChooseLastConfigToDate;
	@FXML private Label LblChooseLastConfigFromDate;

	@FXML private Button BtnRefresh;
	@FXML private Button BtnExit;
	@FXML private Button BtnBack;
	@FXML private Button BtnClearDB;
	@FXML private Button BtnGetLatestOrgConfigurations;
	@FXML private Button BtnCompareOrgConfigurations;
	@FXML private Button BtnClickForMoreDetails;
	

	@FXML private TextField ExcelFilePath;
	
	private Logger logger = Logger.getLogger(PartnerRetriever.class.getCanonicalName());	
	private String UserID, Password, URL, OrgID; // Find a way to update org id 
	
	private String txtSelectedProfile;	
	private DataRetriever dataRetriever = new DataRetriever();	
	private ToolingRetriever tr; 
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	private String configDateTimeToken = new String();
	private ArrayList<String> totalCountList = new ArrayList<String>();	
	private ArrayList<String> metadataTypeList = new ArrayList<String>();
	private String configWithDate;
	public TabPane tabpane = new TabPane(); 
	public static String strDateTime = DateUtil.getCurrentDateTime();
	Alert dateSelectionAlert = new Alert(AlertType.INFORMATION);
	Alert SFDCImpactedObjectsAlert = new Alert(Alert.AlertType.INFORMATION);

	public void setTab() {
		
	}
	/* 
	 * Initialize UI. Start background task to update database with latest ORG 
	 * and User related information from SFDC ORG.
	 * Run background task to update UI elements
	 * @author Debdatta Porya 
	 */
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {
		
		logger.log(Level.INFO,"Home Page update started.");				
		try {
			tr = new ToolingRetriever(ui.ToolsLogin.sfUserID, ui.ToolsLogin.sfPassword);
			OrgID = tr.getOrgId();	
		
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
        	
        //Update application data with latest data from database
		Task<Integer> uiUpdateTask = new Task<Integer>() {
			@Override
			public Integer call() throws Exception {			
				populateHomePageUI();
				return 1;
			}				
		};
		
//		ProgressBar uiUpdateBar = new ProgressBar();
//		uiUpdateBar.progressProperty().bind(uiUpdateTask.progressProperty());
		
		Thread uiUpdate = new Thread(uiUpdateTask);
		uiUpdate.setDaemon(true);
		uiUpdate.start();							
	}
	
	/*
	 * Launch home screen
	 * @author Parantap Samajdar
	 */
	public void launchHome(Stage homeStage) throws ConnectionException {	
		Stage localStage = homeStage;
		UserID = ui.ToolsLogin.sfUserID; 
		Password = ui.ToolsLogin.sfPassword;
		URL = ui.ToolsLogin.sfURL;
//        Parent root; 
          
		try {
			//Deb Comment : Below line is for displaying HomePage Layout
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(HomePage.class.getResource("HomePage.fxml"));
			 
			Parent root = loader.load();
	        //root.setStyle("-fx-background-image: url('SFDCOhana.jpg')");
			Scene homeScene = new Scene(root); 
			homeScene.getStylesheets().add(getClass().getResource("Login.css").toExternalForm());
			localStage.setScene(homeScene);	 
	        // Set display size
			localStage.setWidth(1000);
			localStage.setHeight(800);
			localStage.show();
//			// Save user id to database after successful login if user selected remember option 
//	        if(ui.ToolsLogin.rememberUser) {
//				System.out.println("Update user table with remember user check");
//	        	jdbc.updateUserTable(UserID, Password, URL, OrgID);
//	        }

	        logger.log(Level.INFO,"Home screen loaded successfully");	        
		} catch (IOException e) {			
			logger.log(Level.INFO,"Error loading home screen " + e.getMessage());
		}		 
    }
	
	/*
	 * Update all fields on home screen.
	 * @author Debdatta Porya
	 */
	public void populateHomePageUI() throws ConnectionException {
		com.sforce.soap.partner.LoginResult loginResult = Login.getHomePageUiComponent(ui.ToolsLogin.sfUserID,ui.ToolsLogin.sfPassword,ui.ToolsLogin.sfURL);
		PartnerRetriever sfPartner = new PartnerRetriever(ui.ToolsLogin.sfUserID,ui.ToolsLogin.sfPassword,ui.ToolsLogin.sfURL);
		ArrayList<String> arrOrgDetails = sfPartner.getOrganizationInformation(loginResult.getUserInfo().getOrganizationId());
		System.out.println(sfPartner.getObjectsCount());
		System.out.println(sfPartner.getOrganizationInformation(loginResult.getUserInfo().getOrganizationId()));
		TextOrganizationName.setText(loginResult.getUserInfo().getOrganizationName());
		TextOrganizationInstance.setText(arrOrgDetails.get(1));
		TextOrganizationID.setText(loginResult.getUserInfo().getOrganizationId());
		TextOrganizationEdition.setText(arrOrgDetails.get(4));
		TextLoginUserName.setText(loginResult.getUserInfo().getUserName());
		TextLoginUserEmailID.setText(loginResult.getUserInfo().getUserEmail());
		TextOrgPrimaryContact.setText(arrOrgDetails.get(5));
		TextLoginUserProfileID.setText(loginResult.getUserInfo().getProfileId());
		String isSandbox = Boolean.toString(loginResult.getSandbox());
		TextSandboxFlag.setText(isSandbox);
		TextConnectionAPIEndPointURL.setText(loginResult.getMetadataServerUrl());
		//SF_ORG_Analytics sf = new SF_ORG_Analytics(ui.ToolsLogin.sfUserID, ui.ToolsLogin.sfPassword, ui.ToolsLogin.sfURL);
		System.out.println("Current Time stamp : "+SF_ORG_Analytics.timeStamp);
		LCT_TextShowCurrentConfigDate.setText(SF_ORG_Analytics.timeStamp);
		/*
		 * DPSelectLastConfigFromDate_First.setShowWeekNumbers(true);
		 * DPSelectLastConfigToDate_First.setShowWeekNumbers(true);
		 * DPSelectLastConfigFromDate_First.setValue(LocalDate.of(0, null, 0));
		 * DPSelectLastConfigToDate_First.setValue(LocalDate.of(0, null, 0));
		 * DPSelectLastConfigFromDate_Second.setShowWeekNumbers(true);
		 * DPSelectLastConfigToDate_Second.setShowWeekNumbers(true);
		 * DPSelectLastConfigFromDate_Second.setValue(LocalDate.of(0, null, 0));
		 * DPSelectLastConfigToDate_Second.setValue(LocalDate.of(0, null, 0));
		 */
		
	}

	/*
	 * Handle all button click events from home screen.
	 * @author Debdatta Porya
	 */
	@FXML
	public void handleButtonAction(ActionEvent event) throws Exception {

		//Refresh button - fetch latest org information		
		BtnClickForMoreDetails.setOnAction(new EventHandler<ActionEvent>() {			
			public void handle(ActionEvent ae) {
				try {
					//Stage newOrgStage = new Stage();
					//OrgSummaryPage newOrgSummary = new OrgSummaryPage(TextShowCurrentConfigDate.getText(), SelectFinalConfigFromDate.getValue());	
					//newOrgSummary.launchOrgSummaryHome(newOrgStage);

					Excel_Generator eg = new Excel_Generator(strDateTime);
					ResultSet rsBUTTONSRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYBUTTONS", "LAYOUTID,NAME");
					eg.export("BUTTONS_SUMMARY_",rsBUTTONSRESULTSVIEW);
					ResultSet rsFIELDSRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYFIELDS", "LAYOUTID,LAYOUTTYPE,LABEL");
					eg.export("FIELDS_SUMMARY_",rsFIELDSRESULTSVIEW);
					ResultSet rsMETADATACOMPONENTRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYMETADATACOMPONENT", "METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE");
					eg.export("METADATACOMPONENTS_SUMMARY_",rsMETADATACOMPONENTRESULTSVIEW);
					ResultSet rsPROFILESRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYPROFILES", "PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE");
					eg.export("PROFILES_SUMMARY_",rsPROFILESRESULTSVIEW);
					ResultSet rsRECORDTYPESRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYRECORDTYPES", "OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME");
					eg.export("RECORDTYPES_SUMMARY_",rsRECORDTYPESRESULTSVIEW);
					ResultSet rsRELATEDLISTSRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYRELATEDLISTS", "LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,BUTTONNAME,BUTTONLABEL");
					eg.export("RELATEDLISTS_SUMMARY_",rsRELATEDLISTSRESULTSVIEW);
					ResultSet rsTRIGGERSRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYTRIGGERS", "TRIGGERNAME,OBJECTNAME,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE");
					eg.export("TRIGGERS_SUMMARY_",rsTRIGGERSRESULTSVIEW); 
					ResultSet rsVALIDATIONRULESRESULTSVIEW = jdbc.getResultSetWithDistinctRow("SUMMARYVALIDATIONRULES", "ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE");
					eg.export("VALIDATIONRULES_SUMMARY_",rsVALIDATIONRULESRESULTSVIEW); 
					ResultSet rsWEBLINKSResultsView = jdbc.getResultSetWithDistinctRow("SUMMARYWEBLINKS", "LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE");
					eg.export("WEBLINKS_SUMMARY_",rsWEBLINKSResultsView); 
					ResultSet rsIA_TO_DOTNEXT_MAPPINGView = jdbc.getResultSetWithDistinctRow("IA_TO_DOTNEXT_MAPPING", "ORG_ID,TIMESTAMP,OBJECTNAME,LAYOUTID,LAYOUTNAME,BUTTONS,FIELDS,RELATEDLISTS,RELATEDLISTSCOLUMNS,RELATEDLISTSBUTTONS,RECORDTYPES,TRIGGERS,VALIDATIONRULES");
					eg.export("IA_TO_DOTNEXT_MAPPING_SUMMARY_",rsIA_TO_DOTNEXT_MAPPINGView); 
					eg.replaceExcelCellWithGivenValue("IA_TO_DOTNEXT_MAPPING_SUMMARY__REPORT", "null");
					eg.JsonGenerator();
					ExcelFilePath.setText(eg.resultDirPath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				logger.log(Level.INFO, "SFDC Org Analysis completed. ");										

			}
		});
		
//******************DO NOT MODIFY This Code	**********************************************************************
		//Button - Get Latest Org Configuration - To fetch latest org information		
		LCT_BtnGetLatestOrgConfigurations.setOnAction(new EventHandler<ActionEvent>() {			
			public void handle(ActionEvent ae) {				 
				Task<Integer> sfUpdateTask = new Task<Integer>() {
					@Override
					public Integer call() throws Exception {				
						SF_ORG_Analytics sf = new SF_ORG_Analytics(ui.ToolsLogin.sfUserID, ui.ToolsLogin.sfPassword, ui.ToolsLogin.sfURL);						
						return 1;
					}					
				};
				Thread sfUpdate = new Thread(sfUpdateTask);				
				sfUpdate.setDaemon(true);			
				sfUpdate.start();
			}
		});
		
			
		LCT_BtnConfirm_First.setOnAction(new EventHandler<ActionEvent>() {			
			public void handle(ActionEvent ae) {				 
				Task<Integer> sfUpdateConfigDropDown_First = new Task<Integer>() {
					@Override
					public Integer call() throws Exception {	
						int count_First = 0;
						//**********************************************************************************
						//*******Below Lines of code to select First Config date from Dropdown**************
						//**********************************************************************************
						LCT_SelectFinalConfigFromDate_First.getItems().clear();
						System.out.println("Date Pick From Value for First Config Date Selection: "+LCT_DPSelectLastConfigFromDate_First.getValue());
						System.out.println("Date Pick To Value for First Config Date Selection: "+LCT_DPSelectLastConfigToDate_First.getValue());		
						String strToConfigDate_First = String.valueOf(LCT_DPSelectLastConfigToDate_First.getValue());
						strToConfigDate_First = DataRetriever.stringToDateConversion(strToConfigDate_First);
						Date dtToConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(strToConfigDate_First);
						dtToConfigDate_First = DataRetriever.addDays(dtToConfigDate_First, 1);
						System.out.println(dtToConfigDate_First);
						
						String strFromConfigDate_First = String.valueOf(LCT_DPSelectLastConfigFromDate_First.getValue());
						strFromConfigDate_First = DataRetriever.stringToDateConversion(strFromConfigDate_First);
						Date dtFromConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(strFromConfigDate_First);
						dtFromConfigDate_First = DataRetriever.addDays(dtFromConfigDate_First, -1);
						System.out.println(dtFromConfigDate_First);
						
						PriorityQueue<String> configDateList_First = DataRetriever.ConfigurationDateList;
						//logger.log(Level.INFO, "SFDC Org Analysis completed. Current configuration dates list : + " + configDateList);						
						for(String dates_First : configDateList_First) {

							String StrDates_First = DataRetriever.stringToDateConversion(dates_First);
							System.out.println(StrDates_First);
							Date dtWithConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(StrDates_First);
							System.out.println("Date comparison with :"+dtWithConfigDate_First);
							if(dtWithConfigDate_First.after(dtFromConfigDate_First) && dtWithConfigDate_First.before(dtToConfigDate_First)) {
								System.out.println(dates_First);
								LCT_SelectFinalConfigFromDate_First.getItems().add(dates_First);
								count_First = count_First + 1;
							}
						}
						System.out.println("Total Date Count :"+count_First);
						if(count_First==0) {
							LCT_SelectFinalConfigFromDate_First.getItems().add("No data Found");
							System.out.println("Total Date Count :"+count_First);
							Alert noDatesAvailableAlert = new Alert(Alert.AlertType.WARNING);
							noDatesAvailableAlert.setTitle("No date found");
							noDatesAvailableAlert.setHeaderText("No Data found based on your input: ");
							noDatesAvailableAlert.setContentText("There is no existing dates found between "+dtToConfigDate_First+" and "+dtFromConfigDate_First+" in database .");
							noDatesAvailableAlert.setResizable(true);
					    	//alert.getDialogPane().setPrefSize(480, 320);
							noDatesAvailableAlert.showAndWait();
						}
						
						return 1;
					}					
				};
				Thread sfUpdateConfigDD_First = new Thread(sfUpdateConfigDropDown_First);				
				sfUpdateConfigDD_First.setDaemon(true);			
				sfUpdateConfigDD_First.start();
			}
		});
		
		HCT_BtnConfirm_First.setOnAction(new EventHandler<ActionEvent>() {			
			public void handle(ActionEvent ae) {				 
				Task<Integer> sfUpdateConfigDropDown_First = new Task<Integer>() {
					@Override
					public Integer call() throws Exception {	
						int count_First = 0;
						//**********************************************************************************
						//*******Below Lines of code to select First Config date from Dropdown**************
						//**********************************************************************************
						HCT_SelectFinalConfigFromDate_First.getItems().clear();
						System.out.println("Date Pick From Value for First Config Date Selection: "+HCT_DPSelectLastConfigFromDate_First.getValue());
						System.out.println("Date Pick To Value for First Config Date Selection: "+HCT_DPSelectLastConfigToDate_First.getValue());		
						String strToConfigDate_First = String.valueOf(HCT_DPSelectLastConfigToDate_First.getValue());
						strToConfigDate_First = DataRetriever.stringToDateConversion(strToConfigDate_First);
						Date dtToConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(strToConfigDate_First);
						dtToConfigDate_First = DataRetriever.addDays(dtToConfigDate_First, 1);
						System.out.println(dtToConfigDate_First);
						
						String strFromConfigDate_First = String.valueOf(HCT_DPSelectLastConfigFromDate_First.getValue());
						strFromConfigDate_First = DataRetriever.stringToDateConversion(strFromConfigDate_First);
						Date dtFromConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(strFromConfigDate_First);
						dtFromConfigDate_First = DataRetriever.addDays(dtFromConfigDate_First, -1);
						System.out.println(dtFromConfigDate_First);
						
						PriorityQueue<String> configDateList_First = DataRetriever.ConfigurationDateList;
						//logger.log(Level.INFO, "SFDC Org Analysis completed. Current configuration dates list : + " + configDateList);						
						for(String dates_First : configDateList_First) {

							String StrDates_First = DataRetriever.stringToDateConversion(dates_First);
							System.out.println(StrDates_First);
							Date dtWithConfigDate_First = new SimpleDateFormat("dd/MM/yyyy").parse(StrDates_First);
							System.out.println("Date comparison with :"+dtWithConfigDate_First);
							if(dtWithConfigDate_First.after(dtFromConfigDate_First) && dtWithConfigDate_First.before(dtToConfigDate_First)) {
								System.out.println(dates_First);
								HCT_SelectFinalConfigFromDate_First.getItems().add(dates_First);
								count_First = count_First + 1;
							}
						}
						System.out.println("Total Date Count :"+count_First);
						if(count_First==0) {
							HCT_SelectFinalConfigFromDate_First.getItems().add("No data Found");
							System.out.println("Total Date Count :"+count_First);
							Alert noDatesAvailableAlert = new Alert(Alert.AlertType.WARNING);
							noDatesAvailableAlert.setTitle("No date found");
							noDatesAvailableAlert.setHeaderText("No Data found based on your input: ");
							noDatesAvailableAlert.setContentText("There is no existing dates found between "+dtToConfigDate_First+" and "+dtFromConfigDate_First+" in database .");
							noDatesAvailableAlert.setResizable(true);
					    	//alert.getDialogPane().setPrefSize(480, 320);
							noDatesAvailableAlert.showAndWait();
						}
						
						return 1;
					}					
				};
				Thread sfUpdateConfigDD_First = new Thread(sfUpdateConfigDropDown_First);				
				sfUpdateConfigDD_First.setDaemon(true);			
				sfUpdateConfigDD_First.start();
			}
		});
			
		HCT_BtnConfirm_Second.setOnAction(new EventHandler<ActionEvent>() {			
			public void handle(ActionEvent ae) {				 
				Task<Integer> sfUpdateConfigDropDown_Second = new Task<Integer>() {
					@Override
					public Integer call() throws Exception {
						//***********************************************************************************
						//*******Below Lines of code to select Second Config date from Dropdown**************
						//***********************************************************************************
						int count_Second = 0;
						HCT_SelectFinalConfigFromDate_Second.getItems().clear();
						System.out.println("Date Pick From Value for Second Config Date Selection: "+HCT_DPSelectLastConfigFromDate_Second.getValue());
						System.out.println("Date Pick To Value for Second Config Date Selection: "+HCT_DPSelectLastConfigToDate_Second.getValue());		
						String strToConfigDate_Second = String.valueOf(HCT_DPSelectLastConfigToDate_Second.getValue());
						strToConfigDate_Second = DataRetriever.stringToDateConversion(strToConfigDate_Second);
						Date dtToConfigDate_Second = new SimpleDateFormat("dd/MM/yyyy").parse(strToConfigDate_Second);
						dtToConfigDate_Second = DataRetriever.addDays(dtToConfigDate_Second, 1);
						System.out.println(dtToConfigDate_Second);
						
						String strFromConfigDate_Second = String.valueOf(HCT_DPSelectLastConfigFromDate_Second.getValue());
						strFromConfigDate_Second = DataRetriever.stringToDateConversion(strFromConfigDate_Second);
						Date dtFromConfigDate_Second = new SimpleDateFormat("dd/MM/yyyy").parse(strFromConfigDate_Second);
						dtFromConfigDate_Second = DataRetriever.addDays(dtFromConfigDate_Second, -1);
						System.out.println(dtFromConfigDate_Second);
						
						PriorityQueue<String> configDateList_Second = DataRetriever.ConfigurationDateList;
						//logger.log(Level.INFO, "SFDC Org Analysis completed. Current configuration dates list : + " + configDateList);						
						for(String dates_Second : configDateList_Second) {

							String StrDates_Second = DataRetriever.stringToDateConversion(dates_Second);
							System.out.println(StrDates_Second);
							Date dtWithConfigDate_Second = new SimpleDateFormat("dd/MM/yyyy").parse(StrDates_Second);
							System.out.println("Date comparison with :"+dtWithConfigDate_Second);
							if(dtWithConfigDate_Second.after(dtFromConfigDate_Second) && dtWithConfigDate_Second.before(dtToConfigDate_Second)) {
								System.out.println(dates_Second);
								HCT_SelectFinalConfigFromDate_Second.getItems().add(dates_Second);
								count_Second = count_Second + 1;
							}
						}
						System.out.println("Total Date Count :"+count_Second);
						if(count_Second == 0) {
							HCT_SelectFinalConfigFromDate_Second.getItems().add("No data Found");
							System.out.println("Total Date Count :"+count_Second);
							Alert noDatesAvailableAlert = new Alert(Alert.AlertType.WARNING);
							noDatesAvailableAlert.setTitle("No date found");
							noDatesAvailableAlert.setHeaderText("No Data found based on your input: ");
							noDatesAvailableAlert.setContentText("There is no existing dates found between "+dtToConfigDate_Second+" and "+dtFromConfigDate_Second+" in database .");
							noDatesAvailableAlert.setResizable(true);
					    	//alert.getDialogPane().setPrefSize(480, 320);
							noDatesAvailableAlert.showAndWait();
						}
						return 1;
					}					
				};
				Thread sfUpdateConfigDD_Second = new Thread(sfUpdateConfigDropDown_Second);				
				sfUpdateConfigDD_Second.setDaemon(true);			
				sfUpdateConfigDD_Second.start();
			}
		});
		//Button - Compare Salesforce Components - To fetch latest changes in Org		
		LCT_BtnCompareOrgConfigurations.setOnAction(new EventHandler<ActionEvent>() {	
			public void handle(ActionEvent ae) {
				Task<Integer> sfUpdateBarChart = new Task<Integer>() {

					@Override
					public Integer call() throws Exception {	
						String configToDate = null;
						if(LCT_TextShowCurrentConfigDate.getText().length() > 1) {
							configToDate = LCT_TextShowCurrentConfigDate.getText();
						}
						Date dtTokenConfigToDate = DataRetriever.stringToGivenDateFormatConversion(configToDate,"MM/dd/yyyy hh:mm:ss");
						System.out.println("Within Compare org Config With function : "+configToDate+" ---- "+dtTokenConfigToDate);
						
						if(LCT_SelectFinalConfigFromDate_First.getValue().length() > 1) {
							configWithDate = LCT_SelectFinalConfigFromDate_First.getValue();
						}
						
						Date dtTokenConfigWithDate = DataRetriever.stringToGivenDateFormatConversion(configWithDate,"MM/dd/yyyy hh:mm:ss");
						System.out.println("Within Compare org Config With function : "+configWithDate+" ---- "+dtTokenConfigWithDate);
						
						XML_Generator xg = new XML_Generator();
						xg.setStrLastConfigRunDate(configToDate);
						xg.setStrCurrentConfigDate(configWithDate);
						ConfigDifferenceCalculator cdc = new ConfigDifferenceCalculator(configWithDate, configToDate, TextOrganizationID.getText());						
						cdc.getConfigDifference();

						/*
						 * ArrayList<String> arrImpactedObjects = cdc.getImpactedObjects(); String
						 * strImpactedObject = ""; for(String arrImpactedObject : arrImpactedObjects ) {
						 * if(arrImpactedObject!=null) { strImpactedObject =
						 * strImpactedObject+", "+arrImpactedObject; } }
						 * xg.setArrImpactedObjects(arrImpactedObjects);
						 */
						
						return 1;
					}					
				};
				Thread sfBarChart = new Thread(sfUpdateBarChart);				
				sfBarChart.setDaemon(true);			
				sfBarChart.start();
			}
		});
		
		HCT_BtnCompareOrgConfigurations.setOnAction(new EventHandler<ActionEvent>() {	
			public void handle(ActionEvent ae) {
				Task<Integer> sfUpdateBarChart = new Task<Integer>() {

					@Override
					public Integer call() {	
						try {
							if(HCT_SelectFinalConfigFromDate_First.getValue().length() > 1) {
								configWithDate = HCT_SelectFinalConfigFromDate_First.getValue();
							}
							Date dtTokenConfigWithDate = DataRetriever.stringToGivenDateFormatConversion(configWithDate,"MM/dd/yyyy hh:mm:ss");
							System.out.println("Within Compare org Config With function : "+configWithDate);
							
							String configToDate = null;
							if(HCT_SelectFinalConfigFromDate_First.getValue().length() > 1) {
								configToDate = HCT_SelectFinalConfigFromDate_Second.getValue();
							}
							Date dtTokenConfigToDate = DataRetriever.stringToGivenDateFormatConversion(configToDate,"MM/dd/yyyy hh:mm:ss");
							System.out.println("Within Compare org Config With function : "+configToDate);
							
							if(dtTokenConfigWithDate.before(dtTokenConfigToDate)) {
								XML_Generator xg = new XML_Generator();
								xg.setStrCurrentConfigDate(configWithDate);
								xg.setStrLastConfigRunDate(configToDate);
								ConfigDifferenceCalculator cdc = new ConfigDifferenceCalculator(configWithDate, configToDate, TextOrganizationID.getText());	
								cdc.getConfigDifference(); 
								//cdc.getSummaryButtonDetails();
								//cdc.getSummaryFieldsDetails();
							}else {
								logger.log(Level.WARNING,"Selected First Config date(i.e Config With Date) should always be earlier than Second Config date(i.e Config To Date). "
								+"E.g - First Config Date = 1st Jan 2019 & Second Config Date = 31st Jan 2019");
								//HCT_ErrorLabel.setText("Selected First Config date(i.e Config With Date) should always be earlier than Second Config date(i.e Config To Date). E.g - First Config Date = 1st Jan 2019 & Second Config Date = 31st Jan 2019"); 
							}
						}catch(Exception e) {
							logger.log(Level.WARNING,e.getStackTrace().toString());
						}
						
						return 1;
					}					
				};
				Thread sfBarChart = new Thread(sfUpdateBarChart);				
				sfBarChart.setDaemon(true);			
				sfBarChart.start();
			}
		});
		
		QVT_BtnCompareOrgConfigurations.setOnAction(new EventHandler<ActionEvent>() {	
			public void handle(ActionEvent ae) {
				Task<Integer> sfUpdateBarChart = new Task<Integer>() {
					int last_N_Days = 0;
					@Override
					public Integer call() throws Exception {	
						if(Integer.valueOf(QVT_EnterNDays.getText()) > 0 ) {
							last_N_Days = Integer.valueOf(QVT_EnterNDays.getText()) ;
						}
						System.out.println("No. of Days entered by User : "+last_N_Days);
						
						ArrayList<ArrayList<String>> changedComponentList = tr.getMetadataChanges(last_N_Days);
						
						String strDateTime = DateUtil.getCurrentDateTime();
						Excel_Generator eg = new Excel_Generator(strDateTime);
			        	String resultDirPath = System.getProperty("user.dir") + "\\Results\\Last_"+last_N_Days+"Days_LogReport\\"+strDateTime;
			        	System.out.println("Log Report File Path :"+resultDirPath);
			        	File fileDir = new File(resultDirPath);
			        	if(!fileDir.exists()) {
			        		fileDir.mkdirs();
			        	}
			        	FileWriter writer = new FileWriter(fileDir+"\\output.txt");
			        	for(ArrayList<String> fileInputLists : changedComponentList) {
			        			writer.write(fileInputLists + System.lineSeparator()+"\n");
			        	}
			        	writer.close();
						ExcelFilePath.setText(resultDirPath);
						
						return 1;
					}					
				};
				Thread sfBarChart = new Thread(sfUpdateBarChart);				
				sfBarChart.setDaemon(true);			
				sfBarChart.start();
			}
		});
//****************************************************************************************************************		
		//Back button - To go back to Login Page
		BtnBack.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {
//
//				
//				logger.log(Level.INFO,"Go back to Login screen");	
			}					
		});
		BtnClearDB.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {										
//				ui.ToolsLogin newLogin = new ui.ToolsLogin();		
//				newLogin.launchUI();
				jdbc.dropView("BUTTONSResultsView");
				jdbc.dropView("BUTTONSWithDateView");
				jdbc.dropView("BUTTONSToDateView");
				jdbc.deleteTableData("SUMMARYBUTTONS");
				jdbc.dropView("FIELDSResultsView");
				jdbc.dropView("FIELDSWithDateView");
				jdbc.dropView("FIELDSToDateView");
				jdbc.deleteTableData("SUMMARYFIELDS");
				jdbc.dropView("METADATACOMPONENTPrimaryView");
				jdbc.dropView("METADATACOMPONENTSecondaryResultsView");
				jdbc.dropView("METADATACOMPONENTToDateView");
				jdbc.dropView("METADATACOMPONENTWithDateView");
				jdbc.deleteTableData("SUMMARYMETADATACOMPONENT");
				jdbc.dropView("PROFILESPrimaryResultsView");
				jdbc.dropView("PROFILESSecondaryResultsView");
				jdbc.dropView("PROFILESToDateView");
				jdbc.dropView("PROFILESWithDateView");
				jdbc.deleteTableData("SUMMARYPROFILES");
				jdbc.dropView("RECORDTYPESResultsView");
				jdbc.dropView("RECORDTYPESToDateView");
				jdbc.dropView("RECORDTYPESWithDateView");
				jdbc.deleteTableData("SUMMARYRECORDTYPES");
				jdbc.dropView("RELATEDLISTSResultsView");
				jdbc.dropView("RELATEDLISTSToDateView");
				jdbc.dropView("RELATEDLISTSWithDateView");
				jdbc.deleteTableData("SUMMARYRELATEDLISTS");
				jdbc.dropView("TRIGGERPrimaryResultsView");
				jdbc.dropView("TRIGGERSecondaryResultsView");
				jdbc.dropView("TRIGGERSToDateView");
				jdbc.dropView("TRIGGERSWithDateView");
				jdbc.deleteTableData("SUMMARYTRIGGERS");
				jdbc.dropView("VALIDATIONRULEPrimaryResultsView");
				jdbc.dropView("VALIDATIONRULESSecondaryResultsView");
				jdbc.dropView("VALIDATIONRULESToDateView");
				jdbc.dropView("VALIDATIONRULESWithDateView");
				jdbc.deleteTableData("SUMMARYVALIDATIONRULES");
				jdbc.dropView("WEBLINKPrimaryResultsView");
				jdbc.dropView("WEBLINKSecondaryResultsView");
				jdbc.dropView("WEBLINKSToDateView");
				jdbc.dropView("WEBLINKSWithDateView");
				jdbc.deleteTableData("SUMMARYWEBLINKS");
				jdbc.deleteTableData("IA_TO_DOTNEXT_MAPPING");
				
				logger.log(Level.INFO,"Go back to Login screen");	
			}					
		});	
		//Exit button - close program
		BtnExit.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {										
					logger.log(Level.INFO,"Existing from home screen");					
					Platform.exit();
					System.exit(0);
				}					
		});		
	}
	
}
