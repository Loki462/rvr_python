package ui;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.sforce.ws.ConnectionException;

//import com.sforce.ws.ConnectionException;

import javafx.scene.chart.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;

import javafx.geometry.Side;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import retriever.MetadataTypeList.ApexComponentChange;
import retriever.MetadataTypeList.BusinessLogicChanges;
import retriever.MetadataTypeList.CustomSettingsChange;
import retriever.MetadataTypeList.SecurityChanges;
import retriever.DataRetriever;
import retriever.MetadataRetriever;
import retriever.PartnerRetriever;
//import retriever.SFDCComparisonRetriever;
import retriever.SFDCComparisonRetriever;
import retriever.ToolingRetriever;
import utilities.ConfigDifferenceCalculator;
import utilities.DateUtil;
import utilities.JavaDBManager;
import utilities.XML_Generator;

public class OrgSummaryPage implements Initializable{

	Stage primaryStage ; 
	private Logger logger = Logger.getLogger(PartnerRetriever.class.getCanonicalName());	
	private static JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	static DateUtil date = new DateUtil();
	String login_ID = jdbc.getValue("USERNAME", "Select USERNAME From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	String Password = jdbc.getValue("PASSWORD", "Select PASSWORD From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	String URL = jdbc.getValue("URL", "Select URL From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	private DataRetriever dataRetriever = new DataRetriever();	

	private MetadataRetriever sfMetadata;
	private PartnerRetriever sfPartner;

	private String configDateTimeToken = new String();
	String strConfigToDate = null, strConfigWithDate = null;
	private ArrayList<String> totalCountList = new ArrayList<String>();	
	private ArrayList<String> metadataTypeList = new ArrayList<String>();
	HomePage hmPage = new HomePage();

	public String impSelectedObject = null;
	//public retriever.MetadataRetriever sfMetadata = new MetadataRetriever(login_ID, Password, URL);
	//********************************All Java FXML Components Initialization

	public Label lblActiveUserCount = new Label("Active User Count");
	public Label impActiveUserCount = new Label();
	public Label lblAvailableObjectCount = new Label("Available Object Count : ");
	public Label impAvailableObjectCount = new Label();
	public Label lblAvailableVFPageCount = new Label("Available VisualForce Page Count : ");
	public Label impAvailableVFPageCount = new Label();
	public Label lblAssignedApps = new Label("Assigned Apps : ");
	public Label impAssignedAppsNames = new Label();
	public Label lblObjectAccess = new Label("Object Access : ");
	public Label impObjectAccessNames = new Label();
	public Label lblVFPageAccess = new Label("VisualForce Page Access : ");
	public Label impVFPageAccessNames = new Label();
	public Label lblCustomPermission = new Label("Custom Permission : ");
	public Label impCustomPermission = new Label();
	//=================================================================
	public Label impObjectNames = new Label("Impacted Objects ->");
	public ListView impObjectNamesText = new ListView();
	public Label lblFieldCount = new Label("Fields Count");
	public Label impFieldsCount = new Label();
	Hyperlink impFieldsCountlink = new Hyperlink();
	public Label lblRelatedListsCount = new Label("Related Lists Count : ");
	public Label impRelatedListsCount = new Label();
	Hyperlink impRelatedListsCountlink = new Hyperlink();
	public Label lblButtonsCount = new Label("Buttons Count : ");
	public Label impButtonsCount = new Label();
	Hyperlink impButtonsCountlink = new Hyperlink();
	public Label lblValidationRules = new Label("Impacted Validation Rules & Triggers : ");
	public TextArea impValidationRules = new TextArea();
	public Label lblTriggers = new Label("Triggers : ");
	public TextArea impTriggers = new TextArea();
	//===================================================================
	public Label impPermissionSets = new Label("Impacted Permission Sets ->");
	public ListView impPermissionSetLists = new ListView();
	public Label impAssignementRules = new Label("Impacted Assignment Rules ->");
	public ListView impAssignementRulesLists = new ListView();
	public Label impSharingRules = new Label("Impacted Sharing Rules ->");
	public ListView impSharingRulesLists = new ListView();
	public Label impProfileNames = new Label("Impacted Profiles ->");
	public ListView impProfileNamesText = new ListView();
	//===================================================================
	public Label lblApexClass = new Label("Apex Classes ->");
	public ListView impApexClassLists = new ListView();
	public Label lblApexPages = new Label("Apex Pages ->");
	public ListView impApexPageLists = new ListView();
	public Label lblApexTriggers = new Label("Apex Triggers ->");
	public ListView impApexTriggers = new ListView();
	public Label lblApexCompCode = new Label("Apex Components ->");
	public ListView impApexCompCode = new ListView();
	//====================================================================

	//private ToolingRetriever sfTooling = new ToolingRetriever(login_ID, Password);
	//SF_ORG_Analytics sfOrgAnalysis = new SF_ORG_Analytics(login_ID, Password, URL);

	//TextShowCurrentConfigDate.getText(),SelectFinalConfigFromDate.getValue()
	public OrgSummaryPage(String ConfigToDate, String ConfigWithDate) {
		// TODO Auto-generated constructor stub
		this.strConfigToDate = ConfigToDate;
		this.strConfigWithDate = ConfigWithDate;
	}

	/* 
	 * Initialize UI. Start background task to update database with latest ORG 
	 * and User related information from SFDC ORG.
	 * Run background task to update UI elements
	 * @author Debdatta Porya 
	 */
	public void initialize(java.net.URL arg0, ResourceBundle arg1) {				 
	        //Update application data with latest data from database
			Task<Integer> uiUpdateTask = new Task<Integer>() {
				@Override
				public Integer call() throws Exception {	
					logger.log(Level.INFO,"Org Summary Screen update started.");	
					//populateOrgDetailsUI();

					return 1;
					}
		};
		
		Thread uiUpdate = new Thread(uiUpdateTask);
		uiUpdate.setDaemon(true);
		uiUpdate.start();	
	}

//*************************New code as per Dhivya's Request. This is to add Vertical and Horizontal Scroll bar
	/*
	 * Launch home screen
	 * @author Debdatta Porya
	 */

	public void launchOrgSummaryHome(Stage stage) throws Exception {	
		// Use a border pane as the root for scene
		int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
	    int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
	    
		primaryStage = stage;
        BorderPane border1 = new BorderPane();
        Pane rootPane = new Pane(border1);
        rootPane.getStyleClass().add("pane");
        ScrollPane scroller = new ScrollPane(rootPane);
//        scroller.setFitToWidth(true);
        
        HBox hbox = addHBox();
        border1.setTop(hbox);
//    	ScrollPane scroll = new ScrollPane();
//      scroll.setContent(addVBox());
//      border.setLeft(scroll);
        border1.setLeft(addLeftVBox());
        System.out.println("Config To Date : "+strConfigToDate);
        System.out.println("Config With Date : "+strConfigWithDate);
// Choose either a TilePane or FlowPane for right region and comment out the
// one you aren't using        
        border1.setRight(addRightVBox());
//      border.setRight(addTilePane());
        
// To see only the grid in the center, comment out the following statement
// If both setCenter() calls are executed, the anchor pane from the second
// call replaces the grid from the first call        
        border1.setCenter(addVBoxCentrePane());
        
        // Responsive Design
        int sceneWidth = 0;
        int sceneHeight = 0;
        if (screenWidth <= 800 && screenHeight <= 600) {
            sceneWidth = 700;
            sceneHeight = 500;
        } else if (screenWidth <= 1280 && screenHeight <= 768) {
            sceneWidth = 1100;
            sceneHeight = 650;
        } else if (screenWidth <= 1920 && screenHeight <= 1080) {
            sceneWidth = 1600;
            sceneHeight = 900;
        }
        
        Scene scene1 = new Scene(new BorderPane(scroller,null,null,null,null),sceneWidth,sceneHeight);
        scene1.getStylesheets().add(getClass().getResource("Login.css").toExternalForm());
//		primaryStage.setWidth(sceneWidth);
//		primaryStage.setHeight(sceneHeight);
        primaryStage.setScene(scene1);
        primaryStage.setTitle("SFDC Quality Analyzer tool - Details View ");
//      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//      int width = gd.getDisplayMode().getWidth();
//      int height = gd.getDisplayMode().getHeight();
        
        String errorMessage = "Primary Scene Height : "+scene1.getHeight();
        errorMessage = errorMessage+" Primary Scene Width : "+scene1.getWidth();
        primaryStage.show();
        errorMessage = errorMessage+" Primary Stage Height : "+primaryStage.getHeight();
        errorMessage = errorMessage+" Primary Stage Width : "+primaryStage.getWidth();
		logger.log(Level.INFO,"Org Summary Screen update completed."+errorMessage);
    }
//*************************************************************************************************
/*
 * Creates an HBox with two buttons for the top region
 * @Author : Debdatta Porya
 */
    
    private HBox addHBox() {

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 20, 15, 20));
        hbox.setSpacing(20);   // Gap between nodes
        //hbox.setStyle("-fx-background-color: #336699;");

        Button buttonGenerateHTML = new Button("Generate HTML Summary Report");
        buttonGenerateHTML.setMinSize(200, 20);

        Button buttonProjectedUsecase = new Button("Generate Use Cases");
        buttonProjectedUsecase.setMinSize(200, 20);
        
        Button buttonCITS = new Button("Launch CITS Automation Tool");
        buttonCITS.setMinSize(200, 20);
        
        Button buttonExit = new Button("Exit");
        buttonExit.setMinSize(100, 20);
        
        hbox.getChildren().addAll(buttonGenerateHTML, buttonProjectedUsecase, buttonCITS, buttonExit);
        
		//Generate Use Case button - create use cases from the org
        buttonGenerateHTML.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {
				String strDateTime = DateUtil.getCurrentDateTime();
				String resultDirPath = System.getProperty("user.dir") + "\\Results\\Html_Reports\\" + strDateTime;
				String resultXMLPath = resultDirPath + "\\XML_" + strDateTime + ".xml";
				System.out.println("XMML File Path :"+resultXMLPath);
				String resultHTMLPath = resultDirPath + "\\Salesforce_Quality_Analyzer_"+ strDateTime + ".html";
				File fileDir = new File(resultDirPath);
				if(!fileDir.exists()) {
					fileDir.mkdirs();
				}
				XML_Generator xmlGenerator = new XML_Generator();
				Source xml = new StreamSource(new File(resultXMLPath));
				Source xslt = new StreamSource(System.getProperty("user.dir") + "\\Results\\xslGrid_Jquery.xsl");
				
				try {
					xmlGenerator.createXML(resultXMLPath);
					xmlGenerator.convertXMLToHTML(resultDirPath, xml, xslt);
					logger.log(Level.INFO,"HTML Reports generation successfully completed.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}					
		});
		
		//Generate Report button - create org report in HTML format
        buttonProjectedUsecase.setOnAction(new EventHandler<ActionEvent>() {				
				public void handle(ActionEvent ae) {								
					try {
						logger.log(Level.INFO,"Creating use cases");
						//jdbc.generateSummary();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}					
			});
		//Back button - To go back to Login Page
    	buttonCITS.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {										
				ui.ToolsLogin newLogin = new ui.ToolsLogin();		
				newLogin.launchUI();
				logger.log(Level.INFO,"launch CITS Automation tool");	
			}					
		});
		//Exit button - close program
    	buttonExit.setOnAction(new EventHandler<ActionEvent>() {				
			public void handle(ActionEvent ae) {										
					logger.log(Level.INFO,"Existing from screen");					
					Platform.exit();
					System.exit(0);
				}					
		});	
        return hbox;
    }
    /*
     * Creates a VBox with a list of total count and SFDC Component name in left region
     * @Author : Debdatta Porya
     */
        private VBox addRightVBox() throws Exception {

    		String selectedDate = jdbc.getValue("TimeStamp", "Select DISTINCT(TIMESTAMP) from METADATACOMPONENTWithDateView");    
    	    //System.out.println("Within method : populateOrgDetailUI - selectedDate : "+selectedDate);
    	    configDateTimeToken = selectedDate;
    	    //System.out.println("Within method : populateOrgDetailUI - configDateTimeToken : "+configDateTimeToken);
    		ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
    		metadataTypeList = twoLists.get(0);
    		totalCountList = twoLists.get(1);
    		System.out.println(metadataTypeList);
    		System.out.println(totalCountList);
    		String strOrgID = jdbc.getValue("Org_ID", "Select Org_ID from Users Order By ID Desc FETCH FIRST ROW ONLY");
    		ConfigDifferenceCalculator cdc = new ConfigDifferenceCalculator(strConfigToDate,strConfigWithDate,strOrgID);
    		System.out.println("Constructor parameters :strConfigToDate : "+strConfigToDate+",strConfigWithDate :"+strConfigWithDate+",strOrgID:"+strOrgID);
            VBox vbox = new VBox();
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(100);
            
            RowConstraints row1 = new RowConstraints();
            row1.setPercentHeight(30);
            RowConstraints row2 = new RowConstraints();
            row2.setPercentHeight(70);
            vbox.setPadding(new Insets(10)); // Set all sides to 10
            vbox.setSpacing(8);  // Gap between nodes
            //vbox.setStyle("-fx-background-color: DAE6F3;");

//          VBox.setMargin(options, new Insets(0, 0, 0, 8));
            vbox.getChildren().addAll(addFlowPane());
            
            return vbox;
        }   
/*
 * Creates a VBox with a list of total count and SFDC Component name in left region
 * @Author : Debdatta Porya
 */
    private VBox addLeftVBox() {

		String selectedDate = jdbc.getValue("TimeStamp", "Select DISTINCT(TIMESTAMP) from METADATACOMPONENTWithDateView");    
	    //System.out.println("Within method : populateOrgDetailUI - selectedDate : "+selectedDate);
	    configDateTimeToken = selectedDate;
	    //System.out.println("Within method : populateOrgDetailUI - configDateTimeToken : "+configDateTimeToken);
		ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
		metadataTypeList = twoLists.get(0);
		totalCountList = twoLists.get(1);
		System.out.println(metadataTypeList);
		System.out.println(totalCountList);
		
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10)); // Set all sides to 10
        vbox.setSpacing(8);  // Gap between nodes
        //vbox.setStyle("-fx-background-color: DAE6F3;");

        Label strlabel = new Label("High Level Comparison Result :");
        strlabel.setFont(new Font("Arial", 20));
        strlabel.setStyle("-fx-text-fill: white");
        //        title1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        vbox.getChildren().add(strlabel);

        for (int i=0; i<metadataTypeList.size(); i++) {
        	String strMetadataTypeList = "";
        	ArrayList<String> arrMetadataTypeList = jdbc.getValues("METADATANAME", "Select METADATANAME,METADATATYPE From METADATACOMPONENTResultsView Where METADATATYPE = '"+metadataTypeList.get(i)+"'");
        	List<String> listDistinct = arrMetadataTypeList.stream().distinct().collect(Collectors.toList());
        	System.out.println("Difference B/w Before Distinct-----------"+arrMetadataTypeList.size()+" and After Distinct------------"+listDistinct.size());
        	for(int j=0; j<listDistinct.size();j++) {
        		strMetadataTypeList = listDistinct.get(j)+""+System.getProperty("line.separator")+""+strMetadataTypeList;
        	}
        	final String strMetadataNameList = strMetadataTypeList;
        	Label options = new Label(); // {new TextField(metadataTypeList[i])};
        	//Commented on 02-07-2020 to show distinct impacted component count
        	//options.setText(metadataTypeList.get(i)+" : "+totalCountList.get(i));
        	//New line of code to show Distinct impacted Component count
        	options.setText(metadataTypeList.get(i)+" : "+listDistinct.size());
        	String strMetadataType = metadataTypeList.get(i);
        	Hyperlink impmetadataComponentCountlink = new Hyperlink();
        	impmetadataComponentCountlink.setVisited(false);
        	impmetadataComponentCountlink.setText(options.getText());

        	impmetadataComponentCountlink.setOnAction(new EventHandler<ActionEvent>() {	 
			    @Override
			    public void handle(ActionEvent event) {
			    	Alert alertmetadataComponentCount = new Alert(AlertType.INFORMATION);
			    	alertmetadataComponentCount.setTitle("Impacted Metadata Components Details");
			    	alertmetadataComponentCount.setHeaderText("Below are the Impacted Components for "+strMetadataType+" Metadata type.");
			    	alertmetadataComponentCount.setContentText(strMetadataNameList);
			    	alertmetadataComponentCount.setResizable(true);
			    	//alert.getDialogPane().setPrefSize(480, 320);

			    	alertmetadataComponentCount.showAndWait();
			    }
			});
        	//options.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            // Add offset to left side to indent from title
            VBox.setMargin(impmetadataComponentCountlink, new Insets(0, 0, 0, 8));
            vbox.getChildren().addAll(impmetadataComponentCountlink);
        }
        
        return vbox;
    }

/*
 * Creates a grid for the center region with four columns and three rows
 */
    private VBox addVBoxCentrePane() throws ConnectionException {
		String selectedDate = jdbc.getValue("TimeStamp", "Select DISTINCT(TIMESTAMP) from METADATACOMPONENTWithDateView");    
	    configDateTimeToken = selectedDate;
		ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
		metadataTypeList = twoLists.get(0);
		totalCountList = twoLists.get(1);
		System.out.println(metadataTypeList);
		System.out.println(totalCountList);
		
		VBox gridVBox = new VBox();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(100);
        
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);

        gridVBox.setPadding(new Insets(0, 10, 0, 10));
        gridVBox.setPrefSize(600.0, 600.0);
        //Pie chart#1 - Filling out first box	    
        ObservableList<PieChart.Data> pieChartData1 = FXCollections.observableArrayList();

        for (int i=0; i<metadataTypeList.size(); i++) {
        	List<String> listDistinct = metadataTypeList.stream().distinct().collect(Collectors.toList());
	        for(CustomSettingsChange myVar : CustomSettingsChange.values()) {	
	        	if(myVar.toString().equalsIgnoreCase(metadataTypeList.get(i))) {
	        		//System.out.println("Inside Piechart 1: "+myVar.toString());
	        		pieChartData1.add(new javafx.scene.chart.PieChart.Data(metadataTypeList.get(i), listDistinct.size()));
	        				//Integer.parseInt(totalCountList.get(i)))); // {new TextField(metadataTypeList[i])};
	        	// Add offset to left side to indent from title
	        	}
	        }
        }
        
        final PieChart chart1 = new PieChart(pieChartData1);
        chart1.setMinSize(60.0, 60.0);
        chart1.setLabelsVisible(false);
        chart1.setLegendVisible(true);
        chart1.setLegendSide(Side.RIGHT);
        chart1.setTitle("Overall Impact Analysis in Pie chart");
        chart1.setStyle("-fx-font-size: 10px;-fx-font-weight: bold;");

        GridPane.setConstraints( chart1, 0, 0);
        //****Adding Chart again - Debdatta 23rd June 2020
        gridVBox.getChildren().addAll(chart1,addCentreFlowPane());
        
        return gridVBox;
    }
    /*********************************************************************************************
     * @purpose : Creates a horizontal flow pane with information with 5 columns
     * @author: Debdatta Porya
     * @throws ConnectionException 
     *********************************************************************************************/
        private FlowPane addCentreFlowPane() throws ConnectionException {
            String[] imageNames = new String[]{"Objects -> ", 
            		"Security -> ",
            		"Apex Components -> "};
            /*"Profiles -> ", */
            TitledPane[] tps = new TitledPane[imageNames.length];

            FlowPane flow = new FlowPane();
            flow.setPadding(new Insets(10, 10, 10, 10));
            flow.setVgap(5);
            flow.setHgap(5);
            flow.setPrefWrapLength(700); // preferred width allows for two columns
            //flow.setStyle("-fx-background-color: DAE6F3;");

            Label strlabel = new Label("SFDC Impacted Component Details");
            strlabel.setFont(new Font("Arial", 20));
            strlabel.setStyle("-fx-text-fill: white");
            // --- Accordion
            final Accordion accordion = new Accordion ();                

			//impFieldsCountlink.setText(impFieldsCount);
//			impFieldsCountlink.setOnMouseClicked(new EventHandler<MouseEvent>() {
//			    @Override
//			    public void handle(MouseEvent e) {
//			        System.out.println("This link is clicked");
//			    }
//			});
            tps[0] = new TitledPane(imageNames[0],addFirstBorderPane()); 
            tps[1] = new TitledPane(imageNames[1],addSecondBorderPane());
            tps[2] = new TitledPane(imageNames[2],addThirdBorderPane());
            //tps[3] = new TitledPane(imageNames[3],addFourthBorderPane());
            accordion.getPanes().addAll(tps[0],tps[1],tps[2]);  
            //accordion.setExpandedPane(tps[0]);
            
            VBox vbox = new VBox();
//            Text title = new Text("SFDC Impacted Component Details ->");
//            title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            //title.setStyle("-fx-text-fill: white");
            //vbox.getChildren().addAll(title,resultTable);
            vbox.setSpacing(10.00);
            vbox.getChildren().addAll(strlabel,accordion); 
            flow.getChildren().add(vbox);

            ScrollPane sp1 = new ScrollPane(flow);
            sp1.setContent(flow);

            return flow;
        } 
        
/**************************************************************************************************/
        public AnchorPane addFourthBorderPane() {

        	AnchorPane fourthAnchorPane = new AnchorPane();

            //Label impProfileNames = new Label("Impacted Profiles ->");
            impProfileNames.setLayoutX(14.0);
            impProfileNames.setLayoutY(14.0);
            //ListView impProfileNamesText = new ListView();
            impProfileNamesText.setLayoutX(14.0);
            impProfileNamesText.setLayoutY(42.0);
            impProfileNamesText.setPrefSize(380.0, 540.0);
            
            Text lblActiveUserCount = new Text("Active User Count");
            lblActiveUserCount.setLayoutX(438.0);
            lblActiveUserCount.setLayoutY(48.0);
            Label impActiveUserCount = new Label();
            impActiveUserCount.setLayoutX(631.0);
            impActiveUserCount.setLayoutY(48.0);
            Label lblAvailableObjectCount = new Label("Available Object Count : ");
            lblAvailableObjectCount.setLayoutX(438.0);
            lblAvailableObjectCount.setLayoutY(80.0);
            Label impAvailableObjectCount = new Label();
            impAvailableObjectCount.setLayoutX(670.0);
            impAvailableObjectCount.setLayoutY(48.0);
            Label lblAvailableVFPageCount = new Label("Available VisualForce Page Count : ");
            lblAvailableVFPageCount.setLayoutX(438.0);
            lblAvailableVFPageCount.setLayoutY(109.0);
            Label impAvailableVFPageCount = new Label();
            impAvailableVFPageCount.setLayoutX(670.0);
            impAvailableVFPageCount.setLayoutY(109.0);
            Label lblAssignedApps = new Label("Assigned Apps : ");
            lblAssignedApps.setLayoutX(438.0);
            lblAssignedApps.setLayoutY(164.0);
            Label impAssignedAppsNames = new Label();
            impAssignedAppsNames.setLayoutX(572.0);
            impAssignedAppsNames.setLayoutY(136.0);
            Label lblObjectAccess = new Label("Object Access : ");
            lblObjectAccess.setLayoutX(441.0);
            lblObjectAccess.setLayoutY(236.0);
            Label impObjectAccessNames = new Label();
            impObjectAccessNames.setLayoutX(573.0);
            impObjectAccessNames.setLayoutY(232.0);
            Label lblVFPageAccess = new Label("VisualForce Page Access : ");
            lblVFPageAccess.setLayoutX(436.0);
            lblVFPageAccess.setLayoutY(364.0);
            Label impVFPageAccessNames = new Label();
            impVFPageAccessNames.setLayoutX(573.0);
            impVFPageAccessNames.setLayoutY(351.0);
            Label lblCustomPermission = new Label("Custom Permission : ");
            lblCustomPermission.setLayoutX(429.0);
            lblCustomPermission.setLayoutY(486.0);
            Label impCustomPermission = new Label();
            impCustomPermission.setLayoutX(572.0);
            impCustomPermission.setLayoutY(448.0);
            fourthAnchorPane.getChildren().addAll(impProfileNames,impProfileNamesText,lblActiveUserCount,impActiveUserCount,
            		lblAvailableObjectCount,impAvailableObjectCount,lblAvailableVFPageCount,impAvailableVFPageCount,
          		  lblAssignedApps,impAssignedAppsNames,lblObjectAccess,impObjectAccessNames,
          		  lblVFPageAccess,impVFPageAccessNames,lblCustomPermission,impCustomPermission);
  
            //firstAnchorPane.setStyle("-fx-text-fill: black");
            fourthAnchorPane.getStyleClass().add("root");
            return fourthAnchorPane;
        }
/***************************************************************************************************/
        public AnchorPane addFirstBorderPane() {
        	AnchorPane firstAnchorPane = new AnchorPane();

            //Label impObjectNames = new Label("Impacted Objects ->");
            impObjectNames.setLayoutX(23.0);
            impObjectNames.setLayoutY(18.0);
            //ListView impObjectNamesText = new ListView();
            impObjectNamesText.setLayoutX(14.0);
            impObjectNamesText.setLayoutY(42.0);
            impObjectNamesText.setPrefSize(360.0, 540.0);
            
            //Label lblFieldCount = new Label("Fields Count");
            lblFieldCount.setLayoutX(438.0);
            lblFieldCount.setLayoutY(48.0);
            //Label impFieldsCount = new Label();
            impFieldsCount.setLayoutX(625.0);
            impFieldsCount.setLayoutY(48.0);
            //Label lblRelatedListsCount = new Label("Related Lists Count : ");
            lblRelatedListsCount.setLayoutX(438.0);
            lblRelatedListsCount.setLayoutY(80.0);
            //Label impRelatedListsCount = new Label();
            impRelatedListsCount.setLayoutX(625.0);
            impRelatedListsCount.setLayoutY(80.0);
            //Label lblButtonsCount = new Label("Buttons Count : ");
            lblButtonsCount.setLayoutX(438.0);
            lblButtonsCount.setLayoutY(109.0);
            //Label impButtonsCount = new Label();
            impButtonsCount.setLayoutX(625.0);
            impButtonsCount.setLayoutY(109.0);
            //Label lblValidationRules = new Label("Validation Rules : ");
            lblValidationRules.setLayoutX(438.0);
            lblValidationRules.setLayoutY(164.0);
            //TextArea impValidationRules = new TextArea();
            impValidationRules.setLayoutX(438.0);
            impValidationRules.setLayoutY(194.0);
            impValidationRules.prefWidth(550.0);
            impValidationRules.prefHeight(159.0);
            //Label lblTriggers = new Label("Triggers : ");
            lblTriggers.setLayoutX(438.0);
            lblTriggers.setLayoutY(236.0);
            //TextArea impTriggers = new TextArea();
            impTriggers.setLayoutX(438.0);
            impTriggers.setLayoutY(400.0);
            impTriggers.prefWidth(550.0);
            impTriggers.prefHeight(159.0);
            
            ArrayList<String> objectnameList = new ArrayList<String>();			
			objectnameList = jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'CustomObject'");	
			impObjectNamesText.getItems().addAll(objectnameList);
			impObjectNamesText.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				impSelectedObject = String.valueOf(dataRetriever.getFieldCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
				System.out.println("Impacted Selected Object : "+impSelectedObject);
				impFieldsCount.setText(String.valueOf(dataRetriever.getFieldCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				impFieldsCountlink.setText(String.valueOf(dataRetriever.getFieldCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				System.out.println("Impacted Selected Field Count Links : "+impFieldsCountlink.getText());
				impFieldsCountlink.setVisited(false);
				impFieldsCountlink.setLayoutX(625.0);
				impFieldsCountlink.setLayoutY(48.0);
				impFieldsCountlink.setOnAction(new EventHandler<ActionEvent>() {	 
				    @Override
				    public void handle(ActionEvent event) {
				    	Alert alertField = new Alert(AlertType.INFORMATION);
				    	alertField.setTitle("Impacted Components Details");
				    	alertField.setHeaderText("Below are the Impacted Field for "+impObjectNamesText.getSelectionModel().getSelectedItem().toString()+" Object :");
				    	alertField.setContentText(dataRetriever.getImpactedFieldPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
				    	alertField.setResizable(true);
				    	//alert.getDialogPane().setPrefSize(480, 320);

				    	alertField.showAndWait();
				    }
				});
//				System.out.println("Total Impacted Fields#1 : "+String.valueOf(dataRetriever.getFieldCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				impRelatedListsCount.setText(String.valueOf(dataRetriever.getRelatedListCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				impRelatedListsCountlink.setText(String.valueOf(dataRetriever.getRelatedListCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				System.out.println("Impacted Selected Field Count Links : "+impButtonsCountlink.getText());
				impRelatedListsCountlink.setVisited(false);
				impRelatedListsCountlink.setLayoutX(625.0);
				impRelatedListsCountlink.setLayoutY(80.0);
				impRelatedListsCountlink.setOnAction(new EventHandler<ActionEvent>() {	 
				    @Override
				    public void handle(ActionEvent event) {
				    	Alert alertRelatedList = new Alert(AlertType.INFORMATION);
				    	alertRelatedList.setTitle("Impacted Components Details");
				    	alertRelatedList.setHeaderText("See below for the Impacted Related List for "+impObjectNamesText.getSelectionModel().getSelectedItem().toString()+" Object :");
				    	alertRelatedList.setContentText(dataRetriever.getImpactedRelatedListPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
				    	alertRelatedList.setResizable(true);
				    	//alert.getDialogPane().setPrefSize(480, 320);

				    	alertRelatedList.showAndWait();
				    }
				});
				
				impButtonsCount.setText(String.valueOf(dataRetriever.getButtonsCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				impButtonsCountlink.setText(String.valueOf(dataRetriever.getButtonsCountPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString())));
				System.out.println("Impacted Selected Field Count Links : "+impButtonsCountlink.getText());
				impButtonsCountlink.setVisited(false);
				impButtonsCountlink.setLayoutX(625.0);
				impButtonsCountlink.setLayoutY(109.0);
				impButtonsCountlink.setOnAction(new EventHandler<ActionEvent>() {	 
				    @Override
				    public void handle(ActionEvent event) {
				    	Alert alertButton = new Alert(AlertType.INFORMATION);
				    	alertButton.setTitle("Impacted Components Details");
				    	alertButton.setHeaderText("Below are the Impacted Buttons per Page Layout for Object : "+impObjectNamesText.getSelectionModel().getSelectedItem().toString());
				    	alertButton.setContentText(dataRetriever.getImpactedButtonsPerObject(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
				    	alertButton.setResizable(true);
				    	//alert.getDialogPane().setPrefSize(480, 320);

				    	alertButton.showAndWait();
				    }
				});
				impValidationRules.setText(jdbc.fetchValidationRulesList(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
				impTriggers.setText(jdbc.fetchTriggers(impObjectNamesText.getSelectionModel().getSelectedItem().toString()));
			}					
			});

			firstAnchorPane.getChildren().addAll(impObjectNames,impObjectNamesText,lblFieldCount,impFieldsCountlink,/*impFieldsCount,*/lblRelatedListsCount,impRelatedListsCountlink,
            		lblButtonsCount,impButtonsCountlink,lblValidationRules,impValidationRules,
            		impTriggers);
            

            //firstAnchorPane.setStyle("-fx-text-fill: black");
            firstAnchorPane.getStyleClass().add("root");
            return firstAnchorPane;
        }
/***************************************************************************************************/
		public AnchorPane addSecondBorderPane() {
		    AnchorPane secondAnchorPane = new AnchorPane();

		    //Label impPermissionSets = new Label("Impacted Permission Sets ->");
		    impPermissionSets.setLayoutX(33.0);
		    impPermissionSets.setLayoutY(14.0);
            //ListView impPermissionSetLists = new ListView();
            impPermissionSetLists.setLayoutX(33.0);
            impPermissionSetLists.setLayoutY(41.0);
            impPermissionSetLists.setPrefSize(425.0, 150.0);
            //Label impAssignementRules = new Label("Impacted Assignment Rules ->");
            impAssignementRules.setLayoutX(33.0);
            impAssignementRules.setLayoutY(192.0);
            //ListView impAssignementRulesLists = new ListView();
            impAssignementRulesLists.setLayoutX(33.0);
            impAssignementRulesLists.setLayoutY(220.0);
            impAssignementRulesLists.setPrefSize(425.0, 150.0);
            //Label impSharingRules = new Label("Impacted Sharing Rules ->");
            impSharingRules.setLayoutX(33.0);
            impSharingRules.setLayoutY(377.0);
            //ListView impSharingRulesLists = new ListView();
            impSharingRulesLists.setLayoutX(33.0);
            impSharingRulesLists.setLayoutY(402.0);
            impSharingRulesLists.setPrefSize(425.0, 150.0);
		    
            //Label impProfileNames = new Label("Impacted Profiles ->");
            impProfileNames.setLayoutX(496.0);
            impProfileNames.setLayoutY(14.0);
            //ListView impProfileNamesText = new ListView();
            impProfileNamesText.setLayoutX(496.0);
            impProfileNamesText.setLayoutY(36.0);
            impProfileNamesText.setPrefSize(475.0, 540.0);
            
            //System.out.println("Impacted Profiles : "+dataRetriever.getProfileNameList("Profile_Name","Select * From PROFILESRESULTSVIEW"));
			impProfileNamesText.getItems().addAll(dataRetriever.getProfileNameList("Profile_Name","Select * From PROFILESRESULTSVIEW"));
			impPermissionSetLists.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'PermissionSet'"));
		    impAssignementRulesLists.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'AssignmentRules'"));
			impSharingRulesLists.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'SharingRules'"));

			secondAnchorPane.getChildren().addAll(impPermissionSets,impPermissionSetLists,impAssignementRules,impAssignementRulesLists,
            		impSharingRules,impSharingRulesLists,impProfileNames,impProfileNamesText);
			secondAnchorPane.getStyleClass().add("root");
            //secondAnchorPane.setStyle("-fx-text-fill: black");
		    return secondAnchorPane;
		}
/**
 * @throws ConnectionException *************************************************************************************************/
		public AnchorPane addThirdBorderPane() throws ConnectionException {
			ToolingRetriever tr=new ToolingRetriever(login_ID,Password);
			AnchorPane thirdAnchorPane = new AnchorPane();
			
			//Label lblApexClass = new Label("Apex Classes ->");
			lblApexClass.setLayoutX(23.0);
			lblApexClass.setLayoutY(14.0);
            //ListView impApexClassLists = new ListView();
            impApexClassLists.setLayoutX(23.0);
            impApexClassLists.setLayoutY(41.0);
            impApexClassLists.setPrefSize(430.0, 150.0);
            
            //Label lblApexPages = new Label("Apex Pages ->");
            lblApexPages.setLayoutX(23.0);
            lblApexPages.setLayoutY(192.0);
            //ListView impApexPageLists = new ListView();
            impApexPageLists.setLayoutX(23.0);
            impApexPageLists.setLayoutY(219.0);
            impApexPageLists.setPrefSize(430.0, 184.0);
            
            //Label lblApexTriggers = new Label("Apex Triggers ->");
            lblApexTriggers.setLayoutX(23.0);
            lblApexTriggers.setLayoutY(415.0);
            //ListView impApexTriggers = new ListView();
            impApexTriggers.setLayoutX(23.0);
            impApexTriggers.setLayoutY(446.0);
            impApexTriggers.setPrefSize(430.0, 150.0);
            
            //Label lblApexCompCode = new Label("Apex Components ->");
            lblApexCompCode.setLayoutX(475.0);
            lblApexCompCode.setLayoutY(14.0);
            //ListView impApexCompCode = new ListView();
            impApexCompCode.setLayoutX(480.0);
            impApexCompCode.setLayoutY(37.0);
            impApexCompCode.setPrefSize(475.0, 560.0);
            
            impApexClassLists.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'ApexClass'"));			
            impApexPageLists.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'ApexPage'"));
            impApexTriggers.getItems().addAll(jdbc.getValues("TRIGGERNAME","Select * From TRIGGERSRESULTSVIEW"));
            impApexCompCode.getItems().addAll(jdbc.getValues("METADATANAME", "Select * From METADATACOMPONENTRESULTSVIEW where METADATATYPE = 'ApexComponent'"));
		    
            thirdAnchorPane.getChildren().addAll(lblApexClass,impApexClassLists,lblApexPages,impApexPageLists,
            		lblApexTriggers,impApexTriggers,lblApexCompCode,impApexCompCode);
            thirdAnchorPane.getStyleClass().add("root");
            //thirdAnchorPane.setStyle("-fx-text-fill: black");

		    return thirdAnchorPane;
		}
/***************************************************************************************************
 * @purpose : Creates a horizontal flow pane with Metadata level information with 5 columns
 * @author: Debdatta Porya
 ***************************************************************************************************/
    private FlowPane addFlowPane() {
        String[] imageNames = new String[]{"Metadatatype name with [A-E] : ", 
        		"Metadatatype name with [F-I] : ", 
        		"Metadatatype name with [J-N] : ",
        		"Metadatatype name with [O-S] : ",
        		"Metadatatype name with [T-Z] : "};
        TitledPane[] tps = new TitledPane[imageNames.length];

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(20, 20, 20, 20));
        flow.setVgap(10);
        flow.setHgap(10);
        flow.setPrefSize(550.00, 750.00);
        //flow.setPrefWrapLength(700); // preferred width allows for two columns
        //flow.setStyle("-fx-background-color: DAE6F3;");

        final Label strlabel = new Label("Detailed Level Comparison Results");
        strlabel.setFont(new Font("Arial", 20));
   
        // --- Accordion
        final Accordion accordion = new Accordion ();                 
        
        //Table#1
        TableView<SFDCComparisonRetriever> detailTable1 = new TableView<SFDCComparisonRetriever>();
	    TableView<SFDCComparisonRetriever> detailTable2 = new TableView<SFDCComparisonRetriever>();
	    TableView<SFDCComparisonRetriever> detailTable3 = new TableView<SFDCComparisonRetriever>();
	    TableView<SFDCComparisonRetriever> detailTable4 = new TableView<SFDCComparisonRetriever>();
	    TableView<SFDCComparisonRetriever> detailTable5 = new TableView<SFDCComparisonRetriever>();
	    
	    ObservableList<SFDCComparisonRetriever> data1 = FXCollections.observableArrayList();
	    ObservableList<SFDCComparisonRetriever> data2 = FXCollections.observableArrayList();
	    ObservableList<SFDCComparisonRetriever> data3 = FXCollections.observableArrayList();
	    ObservableList<SFDCComparisonRetriever> data4 = FXCollections.observableArrayList();
	    ObservableList<SFDCComparisonRetriever> data5 = FXCollections.observableArrayList();
	    
	    ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
		metadataTypeList = twoLists.get(0);
		totalCountList = twoLists.get(1);
		//System.out.println(metadataTypeList);
		//System.out.println(totalCountList);
		for(int i=0;i<metadataTypeList.size();i++) {
			String strMetadataType = metadataTypeList.get(i);
			String charMetadataType = Character.toString(strMetadataType.charAt(0));
			if(charMetadataType.matches("[a-eA-E]")) {
				//System.out.println("Metadatatype name with [a-eA-E] : "+strMetadataType);
				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
					//System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
					data1.add(new SFDCComparisonRetriever(arrSFDCResults.getMetadataType(), arrSFDCResults.getMetadataName(), arrSFDCResults.getID() , arrSFDCResults.getCreatedDate() , arrSFDCResults.getLastModifiedDate()));
				}
			}else if(charMetadataType.matches("[f-iF-I]")){
				//System.out.println("Metadatatype name with [f-iF-I] : "+strMetadataType);
				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
					//System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
					data2.add(new SFDCComparisonRetriever(arrSFDCResults.getMetadataType(), arrSFDCResults.getMetadataName(), arrSFDCResults.getID() , arrSFDCResults.getCreatedDate() , arrSFDCResults.getLastModifiedDate()));
					
				}
			}else if(charMetadataType.matches("[j-nJ-N]")){
				//System.out.println("Metadatatype name with [j-nJ-N] : "+strMetadataType);
				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
					//System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
					data3.add(new SFDCComparisonRetriever(arrSFDCResults.getMetadataType(), arrSFDCResults.getMetadataName(), arrSFDCResults.getID() , arrSFDCResults.getCreatedDate() , arrSFDCResults.getLastModifiedDate()));
					
				}
			}else if(charMetadataType.matches("[o-sO-S]")){
				//System.out.println("Metadatatype name with [o-sO-S] : "+strMetadataType);
				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
					//System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
					data4.add(new SFDCComparisonRetriever(arrSFDCResults.getMetadataType(), arrSFDCResults.getMetadataName(), arrSFDCResults.getID() , arrSFDCResults.getCreatedDate() , arrSFDCResults.getLastModifiedDate()));
					
				}
			}else{
				//System.out.println("Metadatatype name with [t-zT-Z] : "+strMetadataType);
				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
					//System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
					data5.add(new SFDCComparisonRetriever(arrSFDCResults.getMetadataType(), arrSFDCResults.getMetadataName(), arrSFDCResults.getID() , arrSFDCResults.getCreatedDate() , arrSFDCResults.getLastModifiedDate()));	
				}
			}
		}
	    detailTable1 = addTableData(detailTable1,data1); 
		detailTable2 = addTableData(detailTable2,data2); 
	    detailTable3 = addTableData(detailTable3,data3);  	    
	    detailTable4 = addTableData(detailTable4,data4);
	    detailTable5 = addTableData(detailTable5,data5);
        
        tps[0] = new TitledPane(imageNames[0],detailTable1); 
        tps[1] = new TitledPane(imageNames[1],detailTable2);
        tps[2] = new TitledPane(imageNames[2],detailTable3);
        tps[3] = new TitledPane(imageNames[3],detailTable4);
        tps[4] = new TitledPane(imageNames[4],detailTable5);
        accordion.getPanes().addAll(tps[0],tps[1],tps[2],tps[3],tps[4]);  
        accordion.setExpandedPane(tps[0]);
        
        VBox vbox = new VBox();
//        Text title = new Text("Detailed Level Comparison Results");
//        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        //vbox.getChildren().addAll(title,resultTable);
        vbox.setSpacing(10.00);
        vbox.getChildren().addAll(strlabel,accordion);     
        flow.getChildren().add(vbox);
        return flow;
    }
/*
 * Creates a horizontal (default) tile pane with eight icons in four rows
 */
    private TilePane addTilePane() {
        
        TilePane tile = new TilePane();
        tile.setPadding(new Insets(5, 0, 5, 0));
        tile.setVgap(4);
        tile.setHgap(4);
        tile.setPrefColumns(2);

        ImageView pages[] = new ImageView[8];

        return tile;
    }
    /* *********************************************************************************************
     * @Purpose : This method is to populate Metadata table
     * @Author : Debdatta Porya
     ***********************************************************************************************
     */
    public TableView<ui.OrgSummaryPage.SFDCComparisonRetriever> addTableData(TableView<SFDCComparisonRetriever> resultTable,ObservableList<SFDCComparisonRetriever> resultTableData) {

        TableColumn<SFDCComparisonRetriever, String> mdTypeCol = new TableColumn<SFDCComparisonRetriever, String>("Type");
        mdTypeCol.setMinWidth(100.00);
        mdTypeCol.setCellValueFactory(new PropertyValueFactory<SFDCComparisonRetriever, String>("metadataType"));

        TableColumn<SFDCComparisonRetriever, String> mdNameCol = new TableColumn<SFDCComparisonRetriever, String>("Name");
        mdNameCol.setMinWidth(100.00);
        mdNameCol.setCellValueFactory(new PropertyValueFactory<SFDCComparisonRetriever, String>("metadataName"));

        TableColumn<SFDCComparisonRetriever, String> idCol = new TableColumn<SFDCComparisonRetriever, String>("ID");
        idCol.setMinWidth(100.00);
        idCol.setCellValueFactory(new PropertyValueFactory<SFDCComparisonRetriever, String>("id"));
        
        TableColumn<SFDCComparisonRetriever, String> createdDateCol = new TableColumn<SFDCComparisonRetriever, String>("Created DateTime");
        createdDateCol.setMinWidth(100.00);
        createdDateCol.setCellValueFactory(new PropertyValueFactory<SFDCComparisonRetriever, String>("createdDate"));
        
        TableColumn<SFDCComparisonRetriever, String> lastModDateCol = new TableColumn<SFDCComparisonRetriever, String>("Last Modified DateTime");
        lastModDateCol.setMinWidth(100.00);
        lastModDateCol.setCellValueFactory(new PropertyValueFactory<SFDCComparisonRetriever, String>("lastModifiedDate"));
        System.out.println("Within addTableData "+resultTableData);
        
        resultTable.setItems(resultTableData);
        resultTable.getColumns().addAll(mdTypeCol, mdNameCol, idCol, createdDateCol,lastModDateCol);

        return resultTable;
    }
	/************************************************************************************************
     *To retrieve the impacted object names from metadata details 
     *@author: Debdatta Porya
     *input parameter : 
     *************************************************************************************************/
     public static void getImpactedObjectNames() {
     	ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
     	ArrayList<String> metadataTypeList = twoLists.get(0);
  		System.out.println(metadataTypeList);
  		for(int i=0;i<metadataTypeList.size();i++) {
  			String strMetadataType = metadataTypeList.get(i);
  				ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType);
  				for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
  					System.out.println(arrSFDCResults.getMetadataType()+"::"+arrSFDCResults.getMetadataName()+"::"+arrSFDCResults.getID()+"::"+arrSFDCResults.getCreatedDate()+"::"+arrSFDCResults.getLastModifiedDate());
  					}
  				}
     }
     
//	/****************************************************************************************************
//	 * Creates an anchor pane using the provided grid and an HBox with buttons
//	 * @param gridPane Grid to anchor to the top of the anchor pane
//	 ****************************************************************************************************/
//    private AnchorPane addAnchorPane(GridPane gridPane) {
//
//        AnchorPane anchorpane = new AnchorPane();   
//        anchorpane.getChildren().addAll(gridPane);
//
//        return anchorpane;
//    }
    
    //************************************************************************************************   
	public static class SFDCComparisonRetriever {
	    	private SimpleStringProperty metadataType;
	    	private SimpleStringProperty metadataName;
	    	private SimpleStringProperty id;
	    	private SimpleStringProperty createdDate; 
	    	private SimpleStringProperty lastModifiedDate;
	
	       //private static final Logger logger = Logger.getLogger(SFDCComparisonRetriever.class.getName());
	        //private final SimpleStringProperty strMetadataType;
	        public SFDCComparisonRetriever(String strMetadataType,String strMetadataName ,String strID,String strCreatedDate ,String strLastModifiedDate){
	        	this.metadataType = new SimpleStringProperty(strMetadataType) ;
	        	this.metadataName = new SimpleStringProperty(strMetadataName) ;
	        	this.id = new SimpleStringProperty(strID) ;
	        	this.createdDate = new SimpleStringProperty(strCreatedDate) ;
	        	this.lastModifiedDate = new SimpleStringProperty(strLastModifiedDate) ;
	        }
	        
	        public void setMetadataType(String strMetadataType) {
	        	metadataType.set(strMetadataType) ;
	        }
	        public void setMetadataName(String strMetadataName) {
	        	metadataName.set(strMetadataName);
	        }
	        public void setID(String strID) {
	        	id.set(strID) ;
	        }
	        public void setCreatedDate(String strCreatedDate) {
	        	createdDate.set(strCreatedDate);
	        }
	        public void setLastModifiedDate(String strLastModifiedDate) {
	        	lastModifiedDate.set(strLastModifiedDate);
	        }
	        public String getMetadataType() {
	        	return metadataType.get();
	        }
	        public String getMetadataName() {
	        	return metadataName.get();
	        }
	        public String getID() {
	        	return id.get();
	        }
	        public String getCreatedDate() {
	        	return createdDate.get();
	        }
	        public String getLastModifiedDate() {
	        	return lastModifiedDate.get();
	        }
	    }
}
///****************************************************************************************************************************
// * Populate UI elements using data stored in database 
// * @author Parantap Samajdar & Debdatta Porya
// ***************************************************************************************************************************/
//public void populateUI() throws ConnectionException {
//	try {
////			PriorityQueue<String> configDateList = DataRetriever.ConfigurationDateList;
////			for(String dates : configDateList) {
////				ConfigFromDate.getItems().addAll(dates);					
////			}
//			//Update date list in picklist field  
//			ConfigFromDate.setOnAction((event) -> {					
//			    String selectedDate = ConfigFromDate.getSelectionModel().getSelectedItem();		    
//			    configDateTimeToken = dataRetriever.dateToTokenConversion(selectedDate);
//			System.out.println("Impacted Profiles : "+dataRetriever.getProfileNameList("Profile_Name","Select * From PROFILESTODATEVIEW"));
//			impProfileNamesText.getItems().addAll(dataRetriever.getProfileNameList("Profile_Name","Select * From PROFILESTODATEVIEW"));
//		    //ProfileCount.setText(" " + dataRetriever.getMetadataCount(OrgID, configDateTimeToken,"Profile") + " ");
//			impObjectNamesText.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"CustomObject"));
////		    impObjectAccessNames.setText(" " + dataRetriever.getMetadataCount(OrgID, configDateTimeToken,"CustomObject") + " ");
//		    impPermissionSetLists.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"PermissionSet"));
//		    impAssignementRulesLists.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"AssignmentRules"));
//		    impSharingRulesLists.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"SharingRules"));
//		    impApexClassLists.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"ApexClass"));			
//		    impApexPageLists.getItems().addAll(dataRetriever.getMetadataNameList(OrgID, configDateTimeToken,"ApexPage"));
//		    impApexTriggers.getItems().addAll(dataRetriever.getTriggerNameList(OrgID, configDateTimeToken));
////			});		
//			//Return active user count for profile selected 
//			impProfileNamesText.setOnMouseClicked(new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent event) {
//					try {
//						PartnerRetriever pr = new PartnerRetriever(ui.Login.sfUserID, ui.Login.sfPassword, ui.Login.sfURL);							
//						impActiveUserCount .setText(Integer.toString(pr.getUsersCount(impProfileNamesText.getSelectionModel().getSelectedItem())));
//					} catch (ConnectionException e) {							
//						e.printStackTrace();
//					}												
//				}					
//			});
//			//Return validation rules list for selected object
//			impObjectNamesText.setOnMouseClicked(new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent event) {
//					impValidationRules.setText(jdbc.fetchValidationRulesList(OrgID, configDateTimeToken, impObjectNamesText.getSelectionModel().getSelectedItem()));
//					impTriggers.setText(jdbc.fetchTriggers(OrgID, configDateTimeToken, impObjectNamesText.getSelectionModel().getSelectedItem()));
//				}					
//			});
//			// Display Apex Class body
//			impApexClassLists.setOnMouseClicked(new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent event) {
//					try {
//						//ToolingRetriever tr = new ToolingRetriever(ui.Login.sfUserID, ui.Login.sfPassword);							
//						ApexBody.setText(tr.getApexClassBody(ApexClass.getSelectionModel().getSelectedItem()));
//					} catch (ConnectionException e) {							
//						e.printStackTrace();
//					}												
//				}					
//			});
//			// Display Apex Trigger body
//			impApexTriggers.setOnMouseClicked(new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent event) {
//					try {
//						//ToolingRetriever tr = new ToolingRetriever(ui.Login.sfUserID, ui.Login.sfPassword);							
//						ApexBody.setText(tr.getApexTriggerBody(ApexTrigger.getSelectionModel().getSelectedItem()));
//					} catch (ConnectionException e) {							
//						e.printStackTrace();
//					}												
//				}					
//			});
//			logger.log(Level.INFO,"UI update complete.");
//	} catch(Exception e) {
//		e.printStackTrace();
//	}		
//		
//	}

//**********************************************************************************************
/*
//Pie chart#2
ObservableList<PieChart.Data> pieChartData2 = FXCollections.observableArrayList();

for (int i=0; i<metadataTypeList.size(); i++) {
	for(ApexComponentChange myVar : ApexComponentChange.values()) {	
    	if(myVar.toString().equalsIgnoreCase(metadataTypeList.get(i))) {
    		//System.out.println("Inside Piechart 2: "+myVar.toString());
    		pieChartData2.add(new javafx.scene.chart.PieChart.Data(metadataTypeList.get(i), Integer.parseInt(totalCountList.get(i)))); // {new TextField(metadataTypeList[i])};
    	// Add offset to left side to indent from title
    	}
    }
}
final PieChart chart2 = new PieChart(pieChartData2);
chart2.setMinSize(60.0, 60.0);
chart2.setLabelsVisible(false);
chart2.setLegendVisible(true);
chart2.setLegendSide(Side.BOTTOM);
chart2.setTitle("Apex Component Changes");
chart2.setStyle("-fx-font-size: 10px;-fx-font-weight: bold;-fx-text-fill:grey;");
GridPane.setConstraints( chart2, 0, 1);

//Pie chart#3	
ObservableList<PieChart.Data> pieChartData3 = FXCollections.observableArrayList();

for (int i=0; i<metadataTypeList.size(); i++) {
	for(BusinessLogicChanges myVar : BusinessLogicChanges.values()) {	
    	if(myVar.toString().equalsIgnoreCase(metadataTypeList.get(i))) {
    		//System.out.println("Inside Piechart 3: "+myVar.toString());
    		pieChartData3.add(new javafx.scene.chart.PieChart.Data(metadataTypeList.get(i), Integer.parseInt(totalCountList.get(i)))); // {new TextField(metadataTypeList[i])};
    	// Add offset to left side to indent from title
    	}
    }
}
final PieChart chart3 = new PieChart(pieChartData3);
chart3.setMinSize(60.0, 60.0);
chart3.setLabelsVisible(false);
chart3.setLegendVisible(true);
chart3.setLegendSide(Side.BOTTOM);
chart3.setTitle("Business Logic Changes");
chart3.setStyle("-fx-font-size: 10px;-fx-font-weight: bold;-fx-text-fill:grey;");
GridPane.setConstraints( chart3, 1, 0);

//Pie chart#4
ObservableList<PieChart.Data> pieChartData4 = FXCollections.observableArrayList();

for (int i=0; i<metadataTypeList.size(); i++) {
	for(SecurityChanges myVar : SecurityChanges.values()) {	
    	if(myVar.toString().equalsIgnoreCase(metadataTypeList.get(i))) {
    		//System.out.println("Inside Piechart 4: "+myVar.toString());
    		pieChartData4.add(new javafx.scene.chart.PieChart.Data(metadataTypeList.get(i), Integer.parseInt(totalCountList.get(i)))); // {new TextField(metadataTypeList[i])};
    	// Add offset to left side to indent from title
    	}
    }
}
final PieChart chart4 = new PieChart(pieChartData4);
chart4.setMinSize(60.0, 60.0);
chart4.setLabelsVisible(false);
chart4.setLegendVisible(true);
chart4.setLegendSide(Side.BOTTOM);
chart4.setTitle("Security Level Changes");
chart4.setStyle("-fx-font-size: 10px;-fx-font-weight: bold;-fx-text-fill:grey;");
GridPane.setConstraints( chart4, 1, 1);
*/
//grid.getChildren().addAll(chart1,chart2,chart3,chart4);

//********************************************************************************************************************************************************************
//[DP] : Commenting the line of code from 170 to 233 . Check the new line of code , which has been included to add Vertical and Horizontal Scroll Bar	
//********************************************************************************************************************************************************************
/*	
	 * Launch home screen
	 * @author Debdatta Porya
	 

	public void launchOrgSummaryHome(Stage stage) throws Exception {	
		// Use a border pane as the root for scene
		int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
	    int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
		primaryStage = stage;
     BorderPane border1 = new BorderPane();
     Pane rootPane = new Pane();

//     scroller.setFitToWidth(true);
     
     HBox hbox = addHBox();
     border1.setTop(hbox);
// 	ScrollPane scroll = new ScrollPane();
//   scroll.setContent(addVBox());
//   border.setLeft(scroll);
     border1.setLeft(addLeftVBox());
     System.out.println("Config To Date : "+strConfigToDate);
     System.out.println("Config With Date : "+strConfigWithDate);
//Choose either a TilePane or FlowPane for right region and comment out the
//one you aren't using        
     border1.setRight(addRightVBox());
//   border.setRight(addTilePane());
     
//To see only the grid in the center, comment out the following statement
//If both setCenter() calls are executed, the anchor pane from the second
//call replaces the grid from the first call        
     border1.setCenter(addVBoxCentrePane());
     
     // Responsive Design
     int sceneWidth = 0;
     int sceneHeight = 0;
     if (screenWidth <= 800 && screenHeight <= 600) {
         sceneWidth = 700;
         sceneHeight = 500;
     } else if (screenWidth <= 1280 && screenHeight <= 768) {
         sceneWidth = 1100;
         sceneHeight = 650;
     } else if (screenWidth <= 1920 && screenHeight <= 1080) {
         sceneWidth = 1600;
         sceneHeight = 900;
     }
     
     Scene scene1 = new Scene(border1,sceneWidth,sceneHeight);
     scene1.getStylesheets().add(getClass().getResource("Login.css").toExternalForm());
		primaryStage.setWidth(sceneWidth);
		primaryStage.setHeight(sceneHeight);
     primaryStage.setScene(scene1);
     primaryStage.setTitle("SFDC Quality Analyzer tool - Details View ");
//   GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//   int width = gd.getDisplayMode().getWidth();
//   int height = gd.getDisplayMode().getHeight();
     
     String errorMessage = "Primary Scene Height : "+scene1.getHeight();
     errorMessage = errorMessage+" Primary Scene Width : "+scene1.getWidth();
     primaryStage.show();
     errorMessage = errorMessage+" Primary Stage Height : "+primaryStage.getHeight();
     errorMessage = errorMessage+" Primary Stage Width : "+primaryStage.getWidth();
		logger.log(Level.INFO,"Org Summary Screen update completed."+errorMessage);
 }*/