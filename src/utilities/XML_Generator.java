package utilities;
import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sforce.soap.partner.LoginResult;

import login.Login;
import retriever.PartnerRetriever;
import ui.ToolsLogin;

public class XML_Generator{
	private static JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();	
	static DateUtil date = new DateUtil();
	private static final Logger logger = Logger.getLogger(XML_Generator.class.getName());
	private String resultPath, fileName;
	private static File file;
	private static File fileDir;	
 	private static String strCurrentConfigDate = null;
 	public static String strLastConfigRunDate = null;
 	public static ArrayList<String> arrImpactedObjects = null;
 	//*******************************getter and Setter Method
	public static String getStrCurrentConfigDate() {
		return strCurrentConfigDate;
	}

	public static void setStrCurrentConfigDate(String strCurrentConfigDate) {
		XML_Generator.strCurrentConfigDate = strCurrentConfigDate;
	}
	public static String getstrLastConfigRunDate() {
		return strLastConfigRunDate;
	}

	public static void setStrLastConfigRunDate(String strLastConfigRunDate) {
		XML_Generator.strLastConfigRunDate = strLastConfigRunDate;
	}
	public static ArrayList<String> getArrImpactedObjects() {
		return arrImpactedObjects;
	}

	public static void setArrImpactedObjects(ArrayList<String> arrImpactedObjects) {
		XML_Generator.arrImpactedObjects = arrImpactedObjects;
	}
	/******************************************************************************************************************************************************
	 * Converting XML file to HTML Report
	 * Input : XML File Location , XSLT File Location
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public static void convertXMLToHTML(String resultDirPath, Source xml, Source xslt) {
		StringWriter sw = new StringWriter();

		try {
			FileWriter fw = new FileWriter(resultDirPath+"\\Salesforce_Quality_Analyzer_"+date.getCurrentDateTime()+".html");
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer trasform = tFactory.newTransformer(xslt);
			trasform.transform(xml, new StreamResult(sw));
			fw.write(sw.toString());
			fw.close();

			System.out.println("Final HTML generated successfully at "+System.getProperty("user.dir") + "\\Results");

		} catch (IOException | TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	/******************************************************************************************************************************************************
	 * Create XML file with Input data
	 * Input : XML File Location
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	
	public static void createXML(String resultXMLPath) throws Exception {
		
		System.out.println("XMML File Path within CreateXML FIle:"+resultXMLPath);
		file = new File(resultXMLPath);
		if(file.exists()) {
			System.out.println("File already exist");
		}
		
		LoginResult loginResult = Login.getHomePageUiComponent(ui.ToolsLogin.sfUserID,ui.ToolsLogin.sfPassword,ui.ToolsLogin.sfURL);
		
		strCurrentConfigDate = getStrCurrentConfigDate();
	 	strLastConfigRunDate = getstrLastConfigRunDate();
	 	ArrayList<String> strObjectArray = getArrImpactedObjects();
	 	System.out.println("strObjectArray within CreateXML FIle:"+strObjectArray);
	 			//new String[]{"ApexTrigger", "ApexComponent", "Accounts", "Contacts","Opportunities","Lead"};
	 	PartnerRetriever sfPartner = new PartnerRetriever(ui.ToolsLogin.sfUserID,ui.ToolsLogin.sfPassword,ui.ToolsLogin.sfURL);
		ArrayList<String> arrOrgDetails = sfPartner.getOrganizationInformation(loginResult.getUserInfo().getOrganizationId());
		ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
		ArrayList<String> strComponentArray = twoLists.get(0);
		ArrayList<String> strTotalCountArray = twoLists.get(1);
	 	String strUserName = loginResult.getUserInfo().getUserName();
	 	String strEmailID = loginResult.getUserInfo().getUserEmail();
	 	String strProfileName = loginResult.getUserInfo().getProfileId();
	 	String strSandbox = String.valueOf(loginResult.getSandbox());
	 	String strAPIVersion = "44.0";
	 	String strOrgName = loginResult.getUserInfo().getOrganizationName();
	 	String strOrgID = loginResult.getUserInfo().getOrganizationId();
	 	String strOrgInstance = arrOrgDetails.get(1);
	 	String strOrgEdition = arrOrgDetails.get(4);
	 	String strOrgPrimaryContact = arrOrgDetails.get(5);
	 	String strMetadataType = "",strMetadataName = "",strMetadataID = "",strCreatedDate = "",strLastModifieDate = "";
 	
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();	 
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // root element
        Element root = document.createElement("root");
        document.appendChild(root);

        // executionDateList element
        Element executionDateList = document.createElement("ExecutionDateList");
        root.appendChild(executionDateList);            
        // executionDateInfo element
        Element executionDateInfo = document.createElement("ExecutionDateInfo");
        executionDateList.appendChild(executionDateInfo);
        // currentConfigDate element
        Element currentConfigDate = document.createElement("CurrentConfigDate");
        currentConfigDate.appendChild(document.createTextNode(getStrCurrentConfigDate()));
        executionDateInfo.appendChild(currentConfigDate);
        // lastConfigRunDate element
        Element lastConfigRunDate = document.createElement("lastConfigRunDate");
        lastConfigRunDate.appendChild(document.createTextNode(strLastConfigRunDate));
        executionDateInfo.appendChild(lastConfigRunDate);

        // objectDetailsList element
        Element objectDetailsList = document.createElement("ObjectDetailsList");
        root.appendChild(objectDetailsList);            
        for(int i=0; i<strObjectArray.size(); i++) {
            // ObjectDetailInfo element
            Element objectDetailInfo = document.createElement("ObjectDetailInfo");
            objectDetailsList.appendChild(objectDetailInfo);
            // ObjectNames element
            Element objectNames = document.createElement("ObjectNames");
            objectNames.appendChild(document.createTextNode(strObjectArray.get(i)));
            objectDetailInfo.appendChild(objectNames);
        }
        
        // HighLevelLoginInfoList element
        Element highLevelLoginInfoList = document.createElement("HighLevelLoginInfoList");
        root.appendChild(highLevelLoginInfoList);            
        // HighLevelLoginInfo element
        Element highLevelLoginInfo = document.createElement("HighLevelLoginInfo");
        highLevelLoginInfoList.appendChild(highLevelLoginInfo);
        // userName element
        Element userName = document.createElement("userName");
        userName.appendChild(document.createTextNode(strUserName));
        highLevelLoginInfo.appendChild(userName);
        // emailID element
        Element emailID = document.createElement("emailID");
        emailID.appendChild(document.createTextNode(strEmailID));
        highLevelLoginInfo.appendChild(emailID);
        // profileName element
        Element profileName = document.createElement("profileName");
        profileName.appendChild(document.createTextNode(strProfileName));
        highLevelLoginInfo.appendChild(profileName);
        // sandbox element
        Element sandbox = document.createElement("sandbox");
        sandbox.appendChild(document.createTextNode(strSandbox));
        highLevelLoginInfo.appendChild(sandbox);
        // profileName element
        Element APIVersion = document.createElement("APIVersion");
        APIVersion.appendChild(document.createTextNode(strAPIVersion));
        highLevelLoginInfo.appendChild(APIVersion);
        
        // HighLevelOrgInfoList element
        Element highLevelOrgInfoList = document.createElement("HighLevelOrgInfoList");
        root.appendChild(highLevelOrgInfoList);            
        // HighLevelOrgInfo element
        Element highLevelOrgInfo = document.createElement("HighLevelOrgInfo");
        highLevelOrgInfoList.appendChild(highLevelOrgInfo);
        // OrgName element
        Element OrgName = document.createElement("OrgName");
        OrgName.appendChild(document.createTextNode(strOrgName));
        highLevelOrgInfo.appendChild(OrgName);
        // OrgID element
        Element OrgID = document.createElement("OrgID");
        OrgID.appendChild(document.createTextNode(strOrgID));
        highLevelOrgInfo.appendChild(OrgID);
        // OrgInstance element
        Element OrgInstance = document.createElement("OrgInstance");
        OrgInstance.appendChild(document.createTextNode(strOrgInstance));
        highLevelOrgInfo.appendChild(OrgInstance);
        // OrgEdition element
        Element OrgEdition = document.createElement("OrgEdition");
        OrgEdition.appendChild(document.createTextNode(strOrgEdition));
        highLevelOrgInfo.appendChild(OrgEdition);
        // OrgPrimaryContact element
        Element OrgPrimaryContact = document.createElement("OrgPrimaryContact");
        OrgPrimaryContact.appendChild(document.createTextNode(strOrgPrimaryContact));
        highLevelOrgInfo.appendChild(OrgPrimaryContact);
        
        // HighLevelComparisonList element
        Element highLevelComparisonList = document.createElement("HighLevelComparisonList");
        root.appendChild(highLevelComparisonList);            
        for(int i=0; i<strComponentArray.size(); i++) {
            // HighLevelComparison element
            Element highLevelComparison = document.createElement("HighLevelComparison");
            highLevelComparisonList.appendChild(highLevelComparison);
            // ComponentName element
            Element ComponentName = document.createElement("ComponentName");
            ComponentName.appendChild(document.createTextNode(strComponentArray.get(i)));
            highLevelComparison.appendChild(ComponentName);
            // ComponentCount element
            Element ComponentCount = document.createElement("ComponentCount");
            ComponentCount.appendChild(document.createTextNode(strTotalCountArray.get(i)));
            highLevelComparison.appendChild(ComponentCount);
        }
        // DetailComparisonList element
        ArrayList<ArrayList<String>> twoLists1 = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
        ArrayList<String>metadataTypeList = twoLists1.get(0);
        ArrayList<String>totalCountList = twoLists1.get(1);
        System.out.println(metadataTypeList);
        System.out.println(totalCountList);
        for(int i=0;i<metadataTypeList.size();i++) {
        	String strMetadataType1 = metadataTypeList.get(i);
        	String charMetadataType = Character.toString(strMetadataType1.charAt(0));
        	if(charMetadataType.matches("[a-eA-E]")) {
        		System.out.println("Metadatatype name with [a-eA-E] : "+strMetadataType1);
        		ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType1);
        		// DetailComparisonList element
        			Element detailComparisonList = document.createElement("DetailComparisonList");
        			root.appendChild(detailComparisonList);            
        			for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
        				// detailComparisonInfo element
        				Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
        				detailComparisonList.appendChild(detailComparisonInfo);
        				// metadataType element
        				Element metadataType = document.createElement("metadataType");
        				metadataType.appendChild(document.createTextNode(arrSFDCResults.getMetadataType()));
        				detailComparisonInfo.appendChild(metadataType);
        				// metadataName element
        				Element metadataName = document.createElement("metadataName");
        				metadataName.appendChild(document.createTextNode(arrSFDCResults.getMetadataName()));
        				detailComparisonInfo.appendChild(metadataName);
        				// metadataID element
        				Element metadataID = document.createElement("metadataID");
        				metadataID.appendChild(document.createTextNode(arrSFDCResults.getID()));
        				detailComparisonInfo.appendChild(metadataID);
        				// createdDate element
        				Element createdDate = document.createElement("createdDate");
        				createdDate.appendChild(document.createTextNode(arrSFDCResults.getCreatedDate()));
        				detailComparisonInfo.appendChild(createdDate);
        				// lastModifieDate element
        				Element lastModifieDate = document.createElement("lastModifieDate");
        				lastModifieDate.appendChild(document.createTextNode(arrSFDCResults.getLastModifiedDate()));
        				detailComparisonInfo.appendChild(lastModifieDate);
        			}
        	}else if(charMetadataType.matches("[f-iF-I]")){
        		System.out.println("Metadatatype name with [f-iF-I] : "+strMetadataType1);
        		ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType1);
        		// DetailComparisonList element
        			Element detailComparisonList = document.createElement("DetailComparisonList");
        			root.appendChild(detailComparisonList);            
        			for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
        				// detailComparisonInfo element
        				Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
        				detailComparisonList.appendChild(detailComparisonInfo);
        				// metadataType element
        				Element metadataType = document.createElement("metadataType");
        				metadataType.appendChild(document.createTextNode(arrSFDCResults.getMetadataType()));
        				detailComparisonInfo.appendChild(metadataType);
        				// metadataName element
        				Element metadataName = document.createElement("metadataName");
        				metadataName.appendChild(document.createTextNode(arrSFDCResults.getMetadataName()));
        				detailComparisonInfo.appendChild(metadataName);
        				// metadataID element
        				Element metadataID = document.createElement("metadataID");
        				metadataID.appendChild(document.createTextNode(arrSFDCResults.getID()));
        				detailComparisonInfo.appendChild(metadataID);
        				// createdDate element
        				Element createdDate = document.createElement("createdDate");
        				createdDate.appendChild(document.createTextNode(arrSFDCResults.getCreatedDate()));
        				detailComparisonInfo.appendChild(createdDate);
        				// lastModifieDate element
        				Element lastModifieDate = document.createElement("lastModifieDate");
        				lastModifieDate.appendChild(document.createTextNode(arrSFDCResults.getLastModifiedDate()));
        				detailComparisonInfo.appendChild(lastModifieDate);
        			}
        	}else if(charMetadataType.matches("[j-nJ-N]")){
        		System.out.println("Metadatatype name with [j-nJ-N] : "+strMetadataType1);
        		ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType1);
        			// DetailComparisonList element
        			Element detailComparisonList = document.createElement("DetailComparisonList");
        			root.appendChild(detailComparisonList);            
        			for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
        				// detailComparisonInfo element
        				Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
        				detailComparisonList.appendChild(detailComparisonInfo);
        				// metadataType element
        				Element metadataType = document.createElement("metadataType");
        				metadataType.appendChild(document.createTextNode(arrSFDCResults.getMetadataType()));
        				detailComparisonInfo.appendChild(metadataType);
        				// metadataName element
        				Element metadataName = document.createElement("metadataName");
        				metadataName.appendChild(document.createTextNode(arrSFDCResults.getMetadataName()));
        				detailComparisonInfo.appendChild(metadataName);
        				// metadataID element
        				Element metadataID = document.createElement("metadataID");
        				metadataID.appendChild(document.createTextNode(arrSFDCResults.getID()));
        				detailComparisonInfo.appendChild(metadataID);
        				// createdDate element
        				Element createdDate = document.createElement("createdDate");
        				createdDate.appendChild(document.createTextNode(arrSFDCResults.getCreatedDate()));
        				detailComparisonInfo.appendChild(createdDate);
        				// lastModifieDate element
        				Element lastModifieDate = document.createElement("lastModifieDate");
        				lastModifieDate.appendChild(document.createTextNode(arrSFDCResults.getLastModifiedDate()));
        				detailComparisonInfo.appendChild(lastModifieDate);
        			}
        	}else if(charMetadataType.matches("[o-sO-S]")){
        		System.out.println("Metadatatype name with [o-sO-S] : "+strMetadataType1);
        		ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType1);
        			// DetailComparisonList element
        			Element detailComparisonList = document.createElement("DetailComparisonList");
        			root.appendChild(detailComparisonList);            
        			for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
        				// detailComparisonInfo element
        				Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
        				detailComparisonList.appendChild(detailComparisonInfo);
        				// metadataType element
        				Element metadataType = document.createElement("metadataType");
        				metadataType.appendChild(document.createTextNode(arrSFDCResults.getMetadataType()));
        				detailComparisonInfo.appendChild(metadataType);
        				// metadataName element
        				Element metadataName = document.createElement("metadataName");
        				metadataName.appendChild(document.createTextNode(arrSFDCResults.getMetadataName()));
        				detailComparisonInfo.appendChild(metadataName);
        				// metadataID element
        				Element metadataID = document.createElement("metadataID");
        				metadataID.appendChild(document.createTextNode(arrSFDCResults.getID()));
        				detailComparisonInfo.appendChild(metadataID);
        				// createdDate element
        				Element createdDate = document.createElement("createdDate");
        				createdDate.appendChild(document.createTextNode(arrSFDCResults.getCreatedDate()));
        				detailComparisonInfo.appendChild(createdDate);
        				// lastModifieDate element
        				Element lastModifieDate = document.createElement("lastModifieDate");
        				lastModifieDate.appendChild(document.createTextNode(arrSFDCResults.getLastModifiedDate()));
        				detailComparisonInfo.appendChild(lastModifieDate);
        			}
        	}else{
        		System.out.println("Metadatatype name with [t-zT-Z] : "+strMetadataType1);
        		ArrayList<retriever.SFDCComparisonRetriever> arrSFDCResultsList = jdbc.fetchSFDCComparisonResults(strMetadataType1);
        		// DetailComparisonList element
        		Element detailComparisonList = document.createElement("DetailComparisonList");
        		root.appendChild(detailComparisonList);            
        		for(retriever.SFDCComparisonRetriever arrSFDCResults : arrSFDCResultsList) {
        			// detailComparisonInfo element
        			Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
        			detailComparisonList.appendChild(detailComparisonInfo);
        			// metadataType element
        			Element metadataType = document.createElement("metadataType");
        			metadataType.appendChild(document.createTextNode(arrSFDCResults.getMetadataType()));
        			detailComparisonInfo.appendChild(metadataType);
        			// metadataName element
        			Element metadataName = document.createElement("metadataName");
        			metadataName.appendChild(document.createTextNode(arrSFDCResults.getMetadataName()));
        			detailComparisonInfo.appendChild(metadataName);
        			// metadataID element
        			Element metadataID = document.createElement("metadataID");
        			metadataID.appendChild(document.createTextNode(arrSFDCResults.getID()));
        			detailComparisonInfo.appendChild(metadataID);
        			// createdDate element
        			Element createdDate = document.createElement("createdDate");
        			createdDate.appendChild(document.createTextNode(arrSFDCResults.getCreatedDate()));
        			detailComparisonInfo.appendChild(createdDate);
        			// lastModifieDate element
        			Element lastModifieDate = document.createElement("lastModifieDate");
        			lastModifieDate.appendChild(document.createTextNode(arrSFDCResults.getLastModifiedDate()));
        			detailComparisonInfo.appendChild(lastModifieDate);
        		}
        	}
        }
//**********************************************************************************************        
//        Element detailComparisonList = document.createElement("DetailComparisonList");
//        root.appendChild(detailComparisonList);            
//        for(int i=0; i<strComponentArray.size(); i++) {
//            // detailComparisonInfo element
//            Element detailComparisonInfo = document.createElement("DetailComparisonInfo");
//            detailComparisonList.appendChild(detailComparisonInfo);
//            // metadataType element
//            Element metadataType = document.createElement("metadataType");
//            metadataType.appendChild(document.createTextNode(strMetadataType));
//            detailComparisonInfo.appendChild(metadataType);
//            // metadataName element
//            Element metadataName = document.createElement("metadataName");
//            metadataName.appendChild(document.createTextNode(strMetadataName));
//            detailComparisonInfo.appendChild(metadataName);
//            // metadataID element
//            Element metadataID = document.createElement("metadataID");
//            metadataID.appendChild(document.createTextNode(strMetadataID));
//            detailComparisonInfo.appendChild(metadataID);
//            // createdDate element
//            Element createdDate = document.createElement("createdDate");
//            createdDate.appendChild(document.createTextNode(strCreatedDate));
//            detailComparisonInfo.appendChild(createdDate);
//            // lastModifieDate element
//            Element lastModifieDate = document.createElement("lastModifieDate");
//            lastModifieDate.appendChild(document.createTextNode(strLastModifieDate));
//            detailComparisonInfo.appendChild(lastModifieDate);
//        }
//**************************************************************************************************
        // create the xml file to transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(resultXMLPath));

        transformer.transform(domSource, streamResult);

	}
	
	/******************************************************************************************
	 * OLD methods created by Parantap Samajdar
	 *******************************************************************************************/
	public static void createXML_OLD(Document doc, File file) throws TransformerException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}
	
	public void tmpObjectDetail(String nodeName, String nodeValue ) {
     	Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd-hh-mm-ss");
		String fileName = "C://" + "Objects" + ".txt";		
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
		    writer.write("<" + nodeName + ">");
		    writer.write(nodeValue);
		} catch (IOException ex) {
		  ex.printStackTrace();
		} 
	}	
	
	public void writeFile(String FileName, StringBuffer sb) {
		BufferedWriter writer = null;
        try {          
            File logFile = new File(FileName);            
            System.out.println(logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.append(sb);
            System.out.println("Output file generated successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {                
                writer.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
	
	public void writeFile(String FileName, String sb) {
		BufferedWriter writer = null;
        try {          
            File logFile = new File(FileName);            
            System.out.println(logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.append(sb);
            System.out.println("Output file generated successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {                
                writer.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }



}

/*
 * //	public static String strLoginUserName = jdbc.getValue("UserName", "Select UserName from USERS order by ID desc fetch first row only");
//	public static String strLoginPassword = jdbc.getValue("Password", "Select Password from USERS order by ID desc fetch first row only");
//	public static String strLoginURL = jdbc.getValue("UserName", "Select URL from USERS order by ID desc fetch first row only");;
//
//	public static LoginResult loginResult = Login.getHomePageUiComponent(strLoginUserName,strLoginPassword,strLoginURL);
//	
// 	static String strCurrentConfigDate = "11/03/2019";
// 	static String strLastConfigRunDate = "11/11/2019";
// 	static String[] strObjectArray = new String[]{"ApexTrigger", "ApexComponent", "Accounts", "Contacts","Opportunities","Lead"};
// 	//static String[] strComponentArray = new String[]{"ApexTrigger", "ApexComponent", "CustomObject","CustomField","CustomPermission", "PageLayout","ApexClass","ApexPage"};
// 	//static int[] strTotalCountArray = new int[]{15, 10, 5,10,10, 4, 5, 3};
//	static ArrayList<ArrayList<String>> twoLists = jdbc.fetchTotalConfigCount();
//	static ArrayList<String> strComponentArray = twoLists.get(0);
//	static ArrayList<String> strTotalCountArray = twoLists.get(1);
// 	static String strUserName = loginResult.getUserInfo().getUserName();
// 	static String strEmailID = loginResult.getUserInfo().getUserEmail();
// 	static String strProfileName = "System Admin";
// 	static String strSandbox = String.valueOf(loginResult.getSandbox());
// 	static String strAPIVersion = "44.0";
// 	static String strOrgName = loginResult.getUserInfo().getOrganizationName();
// 	static String strOrgID = loginResult.getUserInfo().getOrganizationId();
// 	static String strOrgInstance = "4000090000";
// 	static String strOrgEdition = "Developer Sandbox";
// 	static String strOrgPrimaryContact = loginResult.getUserInfo().getUserFullName();
// 	static String strMetadataType = "",strMetadataName = "",strMetadataID = "",strCreatedDate = "",strLastModifieDate = "";

 * */
