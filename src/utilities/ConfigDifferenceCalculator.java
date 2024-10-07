package utilities;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.xml.xpath.*;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sforce.ws.ConnectionException;

import retriever.DataRetriever;
import retriever.MetadataRetriever;
import retriever.PartnerRetriever;
import retriever.ToolingRetriever;
import ui.HomePage;

public class ConfigDifferenceCalculator {	
	private static final Logger logger = Logger.getLogger(ConfigDifferenceCalculator.class.getName());
//	public static String OrgID = null;	
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	private static DateUtil currentDateTime = new DateUtil();
	public String timeStamp = currentDateTime.getCurrentDateTime();
	PreferenceCollection prefCol = new PreferenceCollection();
	String retrieveComparisonQuery = null;
	String retrieveSecondaryComparisonQuery = null;
	private static String configToDate, configWithDate, orgID;	
	public ArrayList<String> impactedSFDCObjects = null;
	String login_ID = jdbc.getValue("USERNAME", "Select USERNAME From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	String Password = jdbc.getValue("PASSWORD", "Select PASSWORD From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	String URL = jdbc.getValue("URL", "Select URL From USERS Order By ID Desc FETCH FIRST ROW ONLY");
	//public retriever.MetadataRetriever sfMetadata = new MetadataRetriever(login_ID, Password, URL);
	ArrayList<String> onlyObjectList = new ArrayList<String>();

	//private ToolingRetriever sfTooling = new ToolingRetriever(login_ID, Password);
	//SF_ORG_Analytics sfOrgAnalysis = new SF_ORG_Analytics(login_ID, Password, URL);

	public ConfigDifferenceCalculator( String strConfigWithDate,String strConfigToDate, String strOrgID) {
		ConfigDifferenceCalculator.setConfigWithDate(strConfigWithDate);
		ConfigDifferenceCalculator.setConfigToDate(strConfigToDate); 
		ConfigDifferenceCalculator.setOrgID(strOrgID);
	}
	
	/**
	 * @return the orgID
	 */
	public static String getOrgID() {
		return orgID;
	}

	/**
	 * @param orgID the orgID to set
	 */
	public static void setOrgID(String orgID) {
		ConfigDifferenceCalculator.orgID = orgID;
	}

	/**
	 * @return the configWithDate
	 */
	public static String getConfigWithDate() {
		return configWithDate;
	}

	/**
	 * @param configWithDate the configWithDate to set
	 */
	public static void setConfigWithDate(String configWithDate) {
		ConfigDifferenceCalculator.configWithDate = configWithDate;
	}

	/**
	 * @return the configToDate
	 */
	public static String getConfigToDate() {
		return configToDate;
	}

	/**
	 * @param configToDate the configToDate to set
	 */
	public static void setConfigToDate(String configToDate) {
		ConfigDifferenceCalculator.configToDate = configToDate;
	}
	/**
	 * @throws ParseException 
	 * @purpose Comparing Buttons related changes between configToDate and configWithDate
	 */
	public void getButtonsConfigDiff() throws ParseException {
		
		jdbc.createViewWithConfigWithDate("BUTTONSWithDateView", "BUTTONS", getOrgID(), getConfigWithDate(), false);
		jdbc.createViewWithConfigToDate("BUTTONSToDateView", "BUTTONS", getOrgID(), getConfigToDate(), false);
		retrieveComparisonQuery = "CREATE VIEW BUTTONSResultsView AS (SELECT LAYOUTID,NAME,LABEL FROM BUTTONSToDateView EXCEPT SELECT LAYOUTID,NAME,LABEL FROM BUTTONSWithDateView) UNION ALL (SELECT LAYOUTID,NAME,LABEL FROM BUTTONSWithDateView EXCEPT SELECT LAYOUTID,NAME,LABEL FROM BUTTONSToDateView)";
		jdbc.createSFDCComparisonResultsView(retrieveComparisonQuery);
		jdbc.copyTableData("SUMMARYBUTTONS", "BUTTONSResultsView", "LAYOUTID,NAME,LABEL");
	}
	/**
	 * @throws ParseException 
	 * @purpose Comparing Fields related changes between configToDate and configWithDate
	 */
	public void getFieldsConfigDiff() throws ParseException {
		jdbc.createViewWithConfigWithDate("FIELDSWithDateView", "FIELDS", getOrgID(), getConfigWithDate(), false);
		jdbc.createViewWithConfigToDate("FIELDSToDateView", "FIELDS", getOrgID(), getConfigToDate(), false);
		retrieveComparisonQuery = "CREATE VIEW FIELDSResultsView AS (SELECT LAYOUTID,LAYOUTTYPE,APINAME,LABEL,SECTIONHEADING FROM FIELDSToDateView EXCEPT SELECT LAYOUTID,LAYOUTTYPE,APINAME,LABEL,SECTIONHEADING FROM FIELDSWithDateView) UNION ALL (SELECT LAYOUTID,LAYOUTTYPE,APINAME,LABEL,SECTIONHEADING FROM FIELDSWithDateView EXCEPT SELECT LAYOUTID,LAYOUTTYPE,APINAME,LABEL,SECTIONHEADING FROM FIELDSToDateView)";
		jdbc.createSFDCComparisonResultsView(retrieveComparisonQuery);
		jdbc.copyTableData("SUMMARYFIELDS", "FIELDSResultsView", "LAYOUTID,LAYOUTTYPE,APINAME,LABEL,SECTIONHEADING");
	} 
	
	public void getMetadataConfigDiff() throws ParseException {
		//Comparing Metadata Component related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("METADATACOMPONENTWithDateView", "METADATACOMPONENT", getOrgID(), getConfigWithDate(), true);
		jdbc.createViewWithConfigToDate("METADATACOMPONENTToDateView", "METADATACOMPONENT", getOrgID(), getConfigToDate(), true);
		//retrieveSecondaryComparisonQuery = "CREATE VIEW METADATACOMPONENTSecondaryResultsView AS (SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM METADATACOMPONENTToDateView EXCEPT SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM METADATACOMPONENTWithDateView) UNION ALL (SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM METADATACOMPONENTWithDateView EXCEPT SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM METADATACOMPONENTToDateView)";
		//jdbc.createSFDCComparisonResultsView(retrieveSecondaryComparisonQuery);
		DataRetriever.stringToGivenDateFormatConversion(getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		DataRetriever.stringToGivenDateFormatConversion(getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		jdbc.updateFinalResult("SUMMARYMETADATACOMPONENT" , "METADATACOMPONENTWithDateView", "METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE");
		jdbc.updateFinalResult("SUMMARYMETADATACOMPONENT" , "METADATACOMPONENTToDateView", "METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE");
		//retrieveSecondaryComparisonQuery = null;
	}
	
	public void getProfilesConfigDiff() throws ParseException {
		//Comparing Profiles related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("PROFILESWithDateView", "PROFILES", getOrgID(), getConfigWithDate(), true);
		jdbc.createViewWithConfigToDate("PROFILESToDateView", "PROFILES", getOrgID(), getConfigToDate(), true);
		
		//retrieveSecondaryComparisonQuery = "CREATE VIEW PROFILESSecondaryResultsView AS (SELECT PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE FROM PROFILESToDateView EXCEPT SELECT PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE FROM PROFILESWithDateView) UNION ALL (SELECT PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE FROM PROFILESWithDateView EXCEPT SELECT PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE FROM PROFILESToDateView)";
		//jdbc.createSFDCComparisonResultsView(retrieveSecondaryComparisonQuery);
		DataRetriever.stringToGivenDateFormatConversion(getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		DataRetriever.stringToGivenDateFormatConversion(getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		jdbc.updateFinalResult("SUMMARYPROFILES" , "PROFILESWithDateView", "PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE");
		jdbc.updateFinalResult("SUMMARYPROFILES" , "PROFILESToDateView", "PROFILE_NAME,USER_COUNT,CREATEDDATE,LASTMODIFIEDDATE");
		//retrieveSecondaryComparisonQuery = null;
	}
	
	public void getRecordTypesConfigDiff() throws ParseException {
		//Comparing Record Types related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("RECORDTYPESWithDateView", "RECORDTYPES", getOrgID(), getConfigWithDate(), false);
		jdbc.createViewWithConfigToDate("RECORDTYPESToDateView", "RECORDTYPES", getOrgID(), getConfigToDate(), false);
		retrieveComparisonQuery = "CREATE VIEW RECORDTYPESResultsView AS (SELECT OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME FROM RECORDTYPESToDateView EXCEPT SELECT OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME FROM RECORDTYPESWithDateView) UNION ALL (SELECT OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME FROM RECORDTYPESWithDateView EXCEPT SELECT OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME FROM RECORDTYPESToDateView)";
		jdbc.createSFDCComparisonResultsView(retrieveComparisonQuery);
		jdbc.copyTableData("SUMMARYRECORDTYPES", "RECORDTYPESResultsView", "OBJECTNAME,LAYOUTID,RECORDTYPEID,LAYOUTNAME");
	}
	
	public void getRelatedListsConfigDiff() throws ParseException {
		//Comparing Related Lists related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("RELATEDLISTSWithDateView", "RELATEDLISTS", getOrgID(), getConfigWithDate(), false);
		jdbc.createViewWithConfigToDate("RELATEDLISTSToDateView", "RELATEDLISTS", getOrgID(), getConfigToDate(), false);
		retrieveComparisonQuery = "CREATE VIEW RELATEDLISTSResultsView AS (SELECT LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,COLUMNLOOKUP,BUTTONNAME,BUTTONLABEL FROM RELATEDLISTSToDateView EXCEPT SELECT LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,COLUMNLOOKUP,BUTTONNAME,BUTTONLABEL FROM RELATEDLISTSWithDateView) UNION ALL (SELECT LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,COLUMNLOOKUP,BUTTONNAME,BUTTONLABEL FROM RELATEDLISTSWithDateView EXCEPT SELECT LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,COLUMNLOOKUP,BUTTONNAME,BUTTONLABEL FROM RELATEDLISTSToDateView)";
		jdbc.createSFDCComparisonResultsView(retrieveComparisonQuery);
		jdbc.copyTableData("SUMMARYRELATEDLISTS", "RELATEDLISTSResultsView", "LAYOUTID,NAME,LABEL,COLUMNNAME,COLUMNFIELD,COLUMNLOOKUP,BUTTONNAME,BUTTONLABEL");
	}
	
	public void getTriggersConfigDiff() throws ParseException {
		//Comparing Triggers related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("TRIGGERSWithDateView", "TRIGGERS", getOrgID(), getConfigWithDate(), true);
		jdbc.createViewWithConfigToDate("TRIGGERSToDateView", "TRIGGERS", getOrgID(), getConfigToDate(), true);

		//retrieveSecondaryComparisonQuery = "CREATE VIEW TRIGGERSecondaryResultsView AS (SELECT TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE FROM TRIGGERSToDateView EXCEPT SELECT TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE FROM TRIGGERSWithDateView) UNION ALL (SELECT TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE FROM TRIGGERSWithDateView EXCEPT SELECT TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE FROM TRIGGERSToDateView)"; 
		//jdbc.createSFDCComparisonResultsView(retrieveSecondaryComparisonQuery);
		DataRetriever.stringToGivenDateFormatConversion(getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		DataRetriever.stringToGivenDateFormatConversion(getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		jdbc.updateFinalResult("SUMMARYTRIGGERS" , "TRIGGERSWithDateView", "TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE");
		jdbc.updateFinalResult("SUMMARYTRIGGERS" , "TRIGGERSToDateView", "TRIGGERNAME,OBJECTNAME,TRIGGERBODY,TRIGGERSTATUS,CREATEDDATE,LASTMODIFIEDDATE");
		//retrieveSecondaryComparisonQuery = null;
	}
	
	public void getValidationRulesConfigDiff() throws ParseException {
		//Comparing Validation Rules related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("VALIDATIONRULESWithDateView", "VALIDATIONRULES", getOrgID(), getConfigWithDate(), true);
		jdbc.createViewWithConfigToDate("VALIDATIONRULESToDateView", "VALIDATIONRULES", getOrgID(), getConfigToDate(), true);
		
		//retrieveSecondaryComparisonQuery = "CREATE VIEW VALIDATIONRULESSecondaryResultsView AS (SELECT ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE FROM VALIDATIONRULESToDateView EXCEPT SELECT ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE FROM VALIDATIONRULESWithDateView) UNION ALL (SELECT ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE FROM VALIDATIONRULESWithDateView EXCEPT SELECT ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE FROM VALIDATIONRULESToDateView)";
		//jdbc.createSFDCComparisonResultsView(retrieveSecondaryComparisonQuery);
		DataRetriever.stringToGivenDateFormatConversion(getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		DataRetriever.stringToGivenDateFormatConversion(getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		jdbc.updateFinalResult("SUMMARYVALIDATIONRULES" , "VALIDATIONRULESWithDateView", "ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE");
		jdbc.updateFinalResult("SUMMARYVALIDATIONRULES" , "VALIDATIONRULESToDateView", "ACTIVE,ERRORDISPLAYFIELD,ERRORMESSAGE,ERRORFORMULA,ID,VALIDATIONNAME,OBJECTNAME,CREATEDDATE,LASTMODIFIEDDATE");
		//retrieveSecondaryComparisonQuery = null;
	}
	
	public void getWebLinksConfigDiff() throws ParseException {
		//Comparing Validation Rules related changes between configToDate and configWithDate
		jdbc.createViewWithConfigWithDate("WEBLINKSWithDateView", "WEBLINKS", getOrgID(), getConfigWithDate(), true);
		jdbc.createViewWithConfigToDate("WEBLINKSToDateView", "WEBLINKS", getOrgID(), getConfigToDate(), true);
		
		//retrieveSecondaryComparisonQuery = "CREATE VIEW WEBLINKSecondaryResultsView AS (SELECT LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE FROM WEBLINKSToDateView EXCEPT SELECT LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE FROM WEBLINKSWithDateView) UNION ALL (SELECT LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE FROM WEBLINKSWithDateView EXCEPT SELECT LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE FROM WEBLINKSToDateView)";
		//jdbc.createSFDCComparisonResultsView(retrieveSecondaryComparisonQuery);
		DataRetriever.stringToGivenDateFormatConversion(getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		DataRetriever.stringToGivenDateFormatConversion(getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		jdbc.updateFinalResult("SUMMARYWEBLINKS" , "WEBLINKSWithDateView", "LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE");
		jdbc.updateFinalResult("SUMMARYWEBLINKS" , "WEBLINKSToDateView", "LINKNAME,LINKDESCRIPTION,MASTERLABEL,URL,CREATEDDATE,LASTMODIFIEDDATE");
		//retrieveSecondaryComparisonQuery = null;
	}
	
	public void getConfigDifference() {
		try {
			logger.log(Level.INFO,"Starting Org comparison between "+getConfigToDate()+" and "+getConfigWithDate()+" for OrgID : "+getOrgID());
			long startTime = System.nanoTime();
				//**************Below lines of Code is for Performing the Config Difference********************************//
				getButtonsConfigDiff(); 
				getFieldsConfigDiff(); 
				getMetadataConfigDiff();
				getProfilesConfigDiff(); 
				getRecordTypesConfigDiff();
				getRelatedListsConfigDiff(); 
				getTriggersConfigDiff();
				getValidationRulesConfigDiff(); 
				getWebLinksConfigDiff();
				//******Below Lines of Code is for Mapping between Dot Next Framework and Impact Analyzer Tool*************//
				getSummaryButtonDetails();
				getSummaryFieldsDetails(); 
				getSummaryRecordTypeDetails();
				getSummaryTriggerDetails();
				getSummaryValidationRulesDetails();
				getSummaryRelatedListsDetails();
				
			Long endTime = System.nanoTime();

			//jdbc.updateSFDCORGTable(OrgID,timeStamp,ProfileList,ObjectList);
			logger.log(Level.INFO, "Time taken to execute SFDC Org Analysis : " + (endTime - startTime)/1000000000 + " Second.");			
		//} catch (ConnectionException | SQLException e) {
		} catch (Exception e) {	
			e.printStackTrace();
			logger.log(Level.SEVERE,"Failed to retrieve SFDC org information");
		}
		
	}
	
	public void getSummaryButtonDetails() throws SQLException{
		//System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrLayoutIDs = jdbc.getValues("LAYOUTID", "Select Count(*) as TotalLayouts, LAYOUTID From SUMMARYBUTTONS Group By LAYOUTID");
		//System.out.println("Unique Layouts in Button Summary Table : "+arrLayoutIDs);
		for(int i=0;i<arrLayoutIDs.size();i++) {
			ArrayList<String> subButtonNames = jdbc.getValues("LABEL", "Select LABEL From SUMMARYBUTTONS Where LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
			ArrayList<String> arrButtonNames = new ArrayList<String>();
			for(int j=0;j<subButtonNames.size();j++) {
				String str1 = subButtonNames.get(j);
				String str2 = str1.replaceAll("\'", "");
				arrButtonNames.add(str2);
			}
			String strLayoutName = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(i)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
			//System.out.println("Button names for Layout : "+strLayoutName+" in Button Summary Table are : "+arrButtonNames);
			String objectName = "";
			String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, BUTTONS";
			String strColumnValues;
			if(strLayoutName == "null" || strLayoutName == null) {
				strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+null+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrButtonNames+"'";
			}else {
				objectName = strLayoutName.split("-")[0];
				strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+objectName+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrButtonNames+"'";
			}
			jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
		}
	}
	
	public void getSummaryFieldsDetails() throws Exception{
		//System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrLayoutIDs = jdbc.getValues("LAYOUTID", "Select Count(*) as TotalLayouts, LAYOUTID From SUMMARYFIELDS Group By LAYOUTID");
		//System.out.println("Unique Layouts in Fields Summary Table : "+arrLayoutIDs);
		for(int i=0;i<arrLayoutIDs.size();i++) {
			ArrayList<String> subFieldNames = jdbc.getValues("LABEL", "Select LABEL From SUMMARYFIELDS Where LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
			ArrayList<String> arrFieldNames = new ArrayList<String>();
			for(int j=0;j<subFieldNames.size();j++) {
				String str1 = subFieldNames.get(j);
				String str2 = str1.replaceAll("\'", "");
				arrFieldNames.add(str2);
			}
			String strLayoutName = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(i)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
			//System.out.println("Field names for Layout : "+strLayoutName+" in Field Summary Table are : "+arrFieldNames);

			//Final Updated in IA_TO_DOTNEXT_MAPPING Table
			String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, FIELDS";
			String strColumnValues = "";
			ArrayList<String> arrFinalUpdate = new ArrayList<String>();
			if(strLayoutName == null) {
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+null+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				//System.out.println("Final Update Object Name : "+null+" with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, "null", arrLayoutIDs.get(i), "FIELDS", ""+arrFieldNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+null+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrFieldNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			}else {
				String objectName = strLayoutName.split("-")[0];
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+objectName+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				//System.out.println("Final Update Object Name : "+objectName+" with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, objectName, arrLayoutIDs.get(i), "FIELDS", ""+arrFieldNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+objectName+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrFieldNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			}
		}
	} 
	
	public void getSummaryRecordTypeDetails() throws Exception{
		System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrObjectNames = jdbc.getValues("OBJECTNAME", "Select Count(*) as TotalCount, LAYOUTID, OBJECTNAME From SUMMARYRECORDTYPES Group By LAYOUTID,OBJECTNAME");
		ArrayList<String> arrLayoutIDs = jdbc.getValues("LAYOUTID", "Select Count(*) as TotalCount, LAYOUTID, OBJECTNAME From SUMMARYRECORDTYPES Group By LAYOUTID,OBJECTNAME");
		System.out.println("Unique Object names in Record Types Summary Table : "+arrObjectNames);
		for(int i=0;i<arrObjectNames.size();i++) {
			ArrayList<String> arrRecordTypeNames = jdbc.getValues("LAYOUTNAME", "Select LAYOUTNAME From SUMMARYRECORDTYPES Where OBJECTNAME = '"+arrObjectNames.get(i)+"'");
			String strLayoutName = "";
			String strLayoutID = "";
			ArrayList<String> arrFinalUpdate = new ArrayList<String>();
			String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, RECORDTYPES";
			String strColumnValues = "";
			
			if((arrLayoutIDs.get(i) == "null") || (arrLayoutIDs.get(i) == "NA") || (arrLayoutIDs.get(i) == "")){
				strLayoutName = null;
				strLayoutID = null;
				System.out.println("Record Type Names for Object : "+arrObjectNames.get(i)+" in Record Types Summary Table are : "+arrRecordTypeNames);
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+timeStamp+"' And OBJECTNAME = '"+arrObjectNames.get(i)+"' And LAYOUTID = "+strLayoutID+"");
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, arrObjectNames.get(i), "null", "RECORDTYPES", ""+arrRecordTypeNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+arrObjectNames.get(i)+"', '"+null+"', '"+strLayoutName+"', '"+arrRecordTypeNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			} else {
				strLayoutName = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(i)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
				System.out.println("Record Type Names for Object : "+arrObjectNames.get(i)+" in Record Types Summary Table are : "+arrRecordTypeNames);
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+timeStamp+"' And OBJECTNAME = '"+arrObjectNames.get(i)+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, arrObjectNames.get(i), arrLayoutIDs.get(i), "RECORDTYPES", ""+arrRecordTypeNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+arrObjectNames.get(i)+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrRecordTypeNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			}
			//String objectName = strLayoutName.split("-")[0];
			//Final Updated in IA_TO_DOTNEXT_MAPPING Table
			
		}
	} 
	
	public void getSummaryRelatedListsDetails() throws Exception{
		//System.out.println("Time Stamp : "+HomePage.strDateTime);
			ArrayList<String> arrLayoutIDs = jdbc.getValues("LAYOUTID", "Select Count(*) as TotalLayouts, LAYOUTID, LABEL From SUMMARYRELATEDLISTS Group By LAYOUTID,LABEL");
			//System.out.println("Unique Layouts in Fields Summary Table : "+arrLayoutIDs);
			for(int i=0;i<arrLayoutIDs.size();i++) {
				if(arrLayoutIDs.get(i) != "null") {
					ArrayList<String> subRelatedListNames = jdbc.getValues("LABEL", "Select LABEL From SUMMARYRELATEDLISTS Where LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
					HashSet<String> hSetString = new HashSet<String>(subRelatedListNames);
					ArrayList<String> arrRelatedListNames = new ArrayList<String>();
					for(String strReplace : hSetString) {
						arrRelatedListNames.add(strReplace);
					}
					String strLayoutName = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(i)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
					//System.out.println("Field names for Layout : "+strLayoutName+" in Field Summary Table are : "+arrFieldNames);
	
					//Final Updated in IA_TO_DOTNEXT_MAPPING Table
					String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, FIELDS";
					String strColumnValues = "";
					ArrayList<String> arrFinalUpdate = new ArrayList<String>();
					if(strLayoutName == "null" || strLayoutName == null) {
						arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+null+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
						//System.out.println("Final Update Object Name : "+null+" with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
						if(arrFinalUpdate.size() > 0) {
							jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, "null", arrLayoutIDs.get(i), "RELATEDLISTS", ""+arrRelatedListNames);
						} else {
							strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+null+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrRelatedListNames+"'";
							jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
						}
					}else {
						String objectName = strLayoutName.split("-")[0];
						arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+objectName+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
						//System.out.println("Final Update Object Name : "+objectName+" with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
						if(arrFinalUpdate.size() > 0) {
							jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, objectName, arrLayoutIDs.get(i), "RELATEDLISTS", ""+arrRelatedListNames);
						} else {
							strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+objectName+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrRelatedListNames+"'";
							jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
						}
					}
				}
			}
	}

	
	public void getSummaryTriggerDetails() throws Exception{
		System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrObjectNames = jdbc.getValues("OBJECTNAME", "Select Count(*) as TotalCount, OBJECTNAME From SUMMARYTRIGGERS Group By OBJECTNAME");
		System.out.println("Unique Object names in Triggers Summary Table : "+arrObjectNames);
		for(int i=0;i<arrObjectNames.size();i++) {
			ArrayList<String> arrTriggersNames = jdbc.getValues("TRIGGERNAME", "Select TRIGGERNAME From SUMMARYTRIGGERS Where OBJECTNAME = '"+arrObjectNames.get(i)+"'");
			System.out.println("Trigger Names for Object : "+arrObjectNames.get(i)+" in Triggers Summary Table are : "+arrTriggersNames);
			String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, TRIGGERS";
			String strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+arrObjectNames.get(i)+"', 'null', 'null', '"+arrTriggersNames+"'";
			jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
		}
	} 
	
	public void getSummaryValidationRulesDetails() throws Exception{
		System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrObjectNames = jdbc.getValues("OBJECTNAME", "Select Count(*) as TotalCount, OBJECTNAME From SUMMARYVALIDATIONRULES Group By OBJECTNAME");
		System.out.println("Unique Object names in Validation Rules Summary Table : "+arrObjectNames);
		for(int i=0;i<arrObjectNames.size();i++) {
			ArrayList<String> arrValidationNames = jdbc.getValues("VALIDATIONNAME", "Select VALIDATIONNAME From SUMMARYVALIDATIONRULES Where OBJECTNAME = '"+arrObjectNames.get(i)+"'");
			System.out.println("Validation Rules Names for Object : "+arrObjectNames.get(i)+" in Validation Rules Summary Table are : "+arrValidationNames);
			//Final Updated in IA_TO_DOTNEXT_MAPPING Table
			ArrayList<String> arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+arrObjectNames.get(i)+"' And LAYOUTID = 'null'");
			if(arrFinalUpdate.size() > 0) {
				jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, arrObjectNames.get(i), "null", "VALIDATIONRULES", ""+arrValidationNames);
			} else {
				String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, VALIDATIONRULES";
				String strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+arrObjectNames.get(i)+"', 'null', 'null', '"+arrValidationNames+"'";
				jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
			}
		}
	} 
	
	public void getSummaryRelatedListsDetails_WIP() throws Exception{
		System.out.println("Time Stamp : "+HomePage.strDateTime);
		ArrayList<String> arrLayoutIDs = jdbc.getValues("LAYOUTID", "Select Count(*) as TotalLayouts, LAYOUTID,LABEL From SUMMARYRELATEDLISTS Group By LAYOUTID,LABEL");
		System.out.println("Unique Layouts in Related Lists Summary Table : "+arrLayoutIDs);
		//Below Lines of Code for Updating RelatedList Columns
		for(int i=0;i<arrLayoutIDs.size();i++) {
			ArrayList<String> subColumnNames = jdbc.getValues("COLUMNNAME", "Select COLUMNNAME From SUMMARYRELATEDLISTS Where LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
			ArrayList<String> arrColumnNames = new ArrayList<String>();
			for(int j=0;j<subColumnNames.size();j++) {
				if(subColumnNames.get(j) != " ") {
					String str1 = subColumnNames.get(j);
					String str2 = str1.replaceAll("\'", "");
					arrColumnNames.add(str2);
				}
			}
			String strLayoutName = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(i)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
			System.out.println("Related List Column names for Layout : "+strLayoutName+" in Summary Table are : "+arrColumnNames);
			//Final Updated in IA_TO_DOTNEXT_MAPPING Table
			String strColumnNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, FIELDS";
			String strColumnValues = "";
			ArrayList<String> arrFinalUpdate = new ArrayList<String>();
			if(strLayoutName == "null") {
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+null+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				System.out.println("Final Update Object Name : '"+null+"' with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, "null", arrLayoutIDs.get(i), "RELATEDLISTSCOLUMNS", ""+arrColumnNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+null+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrColumnNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			}else {
				String objectName = strLayoutName.split("-")[0];
				arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+objectName+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				System.out.println("Final Update Object Name : "+objectName+" with total return count "+arrFinalUpdate.size()+" and values : "+arrFinalUpdate);
				if(arrFinalUpdate.size() > 0) {
					jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, objectName, arrLayoutIDs.get(i), "RELATEDLISTSCOLUMNS", ""+arrColumnNames);
				} else {
					strColumnValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+objectName+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrColumnNames+"'";
					jdbc.insertIntoIADotNextTable(strColumnNames, strColumnValues);
				}
			}
			
			//Below Lines of Code for Updating RelatedList Buttons
			for(int k=0;k<arrLayoutIDs.size();k++) {
				ArrayList<String> subButtonsNames = jdbc.getValues("BUTTONLABEL", "Select BUTTONLABEL From SUMMARYRELATEDLISTS Where LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
				ArrayList<String> arrButtonsNames = new ArrayList<String>();
				for(int l=0;l<subButtonsNames.size();l++) {
					if(subButtonsNames.get(l) != " ") {
						String str1 = subButtonsNames.get(l);
						String str2 = str1.replaceAll("\'", "");
						arrButtonsNames.add(str2);
					}
				}
				String strLayoutNameB = jdbc.getValue("METADATANAME", "Select METADATANAME From METADATACOMPONENT Where METADATATYPE = 'Layout' And ID = '"+arrLayoutIDs.get(k)+"' And ORG_ID = '"+getOrgID()+"' Fetch First 1 Rows Only");
				System.out.println("Related List Column names for Layout : "+strLayoutNameB+" in Summary Table are : "+arrButtonsNames);
				//Final Updated in IA_TO_DOTNEXT_MAPPING Table
				String strButtonNames = "ORG_ID, TIMESTAMP, OBJECTNAME, LAYOUTID, LAYOUTNAME, FIELDS";
				String strButtonValues = "";
				ArrayList<String> arrFinalUpdateB = new ArrayList<String>();
				if(strLayoutNameB == "null") {
					arrFinalUpdateB = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+null+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
					System.out.println("Final Update Object Name : '"+null+"' with total return count "+arrFinalUpdateB.size()+" and values : "+arrFinalUpdateB);
					if(arrFinalUpdateB.size() > 0) {
						jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, "null", arrLayoutIDs.get(i), "RELATEDLISTSBUTTONS", ""+arrButtonsNames);
					} else {
						strButtonValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+null+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrButtonsNames+"'";
						jdbc.insertIntoIADotNextTable(strButtonNames, strButtonValues);
					}
				}else {
					String objectNameB = strLayoutName.split("-")[0];
					arrFinalUpdate = jdbc.getValues("OBJECTNAME", "Select OBJECTNAME From IA_TO_DOTNEXT_MAPPING Where ORG_ID = '"+getOrgID()+"' And TIMESTAMP = '"+HomePage.strDateTime+"' And OBJECTNAME = '"+objectNameB+"' And LAYOUTID = '"+arrLayoutIDs.get(i)+"'");
					System.out.println("Final Update Object Name : "+objectNameB+" with total return count "+arrFinalUpdateB.size()+" and values : "+arrFinalUpdateB);
					if(arrFinalUpdate.size() > 0) {
						jdbc.updateIADotNextTable(getOrgID(), HomePage.strDateTime, objectNameB, arrLayoutIDs.get(i), "RELATEDLISTSBUTTONS", ""+arrButtonsNames);
					} else {
						strButtonValues = "'"+getOrgID()+"', '"+HomePage.strDateTime+"', '"+objectNameB+"', '"+arrLayoutIDs.get(i)+"', '"+strLayoutName+"', '"+arrButtonsNames+"'";
						jdbc.insertIntoIADotNextTable(strButtonNames, strButtonValues);
					}
				}
			}
		}
	} 
	
	public ArrayList<String> getImpactedObjects() throws Exception{
		ArrayList<String> customObjectNameList = null;
		//Retrieve the Total count per metadata component 
		ArrayList<ArrayList<String>> customObjectsLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount,METADATANAME From SUMMARYMETADATACOMPONENT Group By METADATATYPE Having METADATATYPE = 'CustomObject'");
		if(customObjectsLists.get(1).size()>0) {
			customObjectNameList = customObjectsLists.get(1);
			System.out.println("Custom Object List from Metadata View : "+customObjectNameList);
		}
     	//Retrieve the Total count for group of buttons listed under same Layout 
		ArrayList<ArrayList<String>> buttonLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID From BUTTONSResultsView Group By LAYOUTID");
     	ArrayList<String> layoutBIDList = buttonLists.get(0);    	
     	ArrayList<String> layoutBNameList = new ArrayList<String>();
     	for(int i=0;i<layoutBIDList.size();i++) {
     		String strLayoutBID = layoutBIDList.get(i);
     		String strLayoutBQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strLayoutBID+"'";
     		layoutBNameList.add(jdbc.getValue("METADATANAME", strLayoutBQuery));
     	}
     	System.out.println("Buttons List from View : "+layoutBNameList);
     	
     	//Retrieve the Total count for group of fields listed under same Layout 
     	ArrayList<ArrayList<String>> fieldsLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID From FIELDSResultsView Group By LAYOUTID");
     	ArrayList<String> layoutFIDList = fieldsLists.get(0);
     	ArrayList<String> layoutFNameList = new ArrayList<String>();
     	for(int i=0;i<layoutFIDList.size();i++) {
     		String strLayoutFID = layoutFIDList.get(i);
     		String strLayoutFQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strLayoutFID+"'";
     		layoutFNameList.add(jdbc.getValue("METADATANAME", strLayoutFQuery));
     	}
     	System.out.println("Fields List from View : "+layoutFNameList);
     	
     	//Retrieve the Total count for group of Related Lists under same Layout 
     	ArrayList<ArrayList<String>> relatedListLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID , LABEL From RELATEDLISTSResultsView Group By LAYOUTID,LABEL");
     	ArrayList<String> relatedListIDList = relatedListLists.get(0);
     	ArrayList<String> relatedListNameList = new ArrayList<String>();
     	for(int i=0;i<relatedListIDList.size();i++) {
     		String strRelatedLayoutID = relatedListIDList.get(i);
     		String strRelatedLayoutQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strRelatedLayoutID+"'";
     		relatedListNameList.add(jdbc.getValue("METADATANAME", strRelatedLayoutQuery));
     	}
     	System.out.println("Related List from View : "+relatedListNameList);
     	
     	//Retrieve the Total count for Triggers group by Object 
     	ArrayList<ArrayList<String>> triggerLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, OBJECTNAME From TRIGGERSRESULTSVIEW Group By OBJECTNAME");
     	ArrayList<String> triggerObjectList = triggerLists.get(0);
     	System.out.println("Triggers List from View : "+triggerObjectList);
     	
     	//Retrieve the Total count for Triggers group by Object 
     	ArrayList<ArrayList<String>> validationRuleLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, OBJECTNAME From VALIDATIONRULESRESULTSVIEW Group By OBJECTNAME");
     	ArrayList<String> validationRuleObjectList = validationRuleLists.get(0);
     	System.out.println("Validation Rules List from View"+validationRuleObjectList);
     	
     	//Removing Duplicates from Arraylist - layoutBNameList,layoutFNameList,relatedListNameList
     	Set<String> listWithoutDuplicates = null;
     	ArrayList<String> combinedList = null;
     	listWithoutDuplicates =new LinkedHashSet<>(layoutBNameList);
     	listWithoutDuplicates.addAll(layoutFNameList);
     	combinedList = new ArrayList<>(listWithoutDuplicates);
     	listWithoutDuplicates = new LinkedHashSet<>(combinedList);
     	listWithoutDuplicates.addAll(relatedListNameList);
     	combinedList = new ArrayList<>(listWithoutDuplicates);
     	System.out.println("Combined List One : "+combinedList);
     	//Removing Duplicates from Arraylist - layoutBNameList,layoutFNameList,relatedListNameList
     	Set<String> listWithoutDuplicates2 = null;
     	ArrayList<String> combinedList2 = null;
     	listWithoutDuplicates2 = new LinkedHashSet<>(customObjectNameList);
     	listWithoutDuplicates2.addAll(triggerObjectList);
     	combinedList2 = new ArrayList<>(listWithoutDuplicates2);
     	listWithoutDuplicates2 = new LinkedHashSet<>(combinedList2);
     	listWithoutDuplicates2.addAll(validationRuleObjectList);
     	combinedList2 = new ArrayList<>(listWithoutDuplicates2);
     	System.out.println("Combined List two: "+combinedList2);
     	
		PartnerRetriever sfPartner = new PartnerRetriever(login_ID, Password, URL);
		ArrayList<String> SFDCObjectList = sfPartner.getObjectsList();
		System.out.println("SFDC Object List : "+SFDCObjectList);
		String strListItem = null;	
		String[] strArray = null;
		for(String combinedItem : combinedList) {
			strListItem = combinedItem;
			if(strListItem!=null) {
				strArray = strListItem.split("-");
				System.out.println("Before Splitting :"+strListItem+" :: "+strArray[0]);
				onlyObjectList.add(strArray[0]);
			}
			//onlyObjectList.add(i, strArray[0]);	
		}
		System.out.println("SFDC Objects from Layout name :"+onlyObjectList);
		//Removing Duplicates from Arraylist - onlyObjectList,combinedList2
     	Set<String> listWithoutDuplicates3 = null;
     	ArrayList<String> combinedList3 = null;
     	listWithoutDuplicates3 = new LinkedHashSet<>(onlyObjectList);
     	listWithoutDuplicates3.addAll(combinedList2);
     	combinedList3 = new ArrayList<>(listWithoutDuplicates3);
     	//Getting Impacted SFDC Objects from combinedList3 and SFDCObjectList
     	impactedSFDCObjects = SFDCObjectList;
     	impactedSFDCObjects.retainAll(combinedList3);
     	
     	System.out.println("Impacted Objects Only :"+impactedSFDCObjects);
     	return impactedSFDCObjects;
		
	}
	

	/************************************************************************************************
     *To retrieve Total count per Component Level
     *@author: Debdatta Porya
     *input parameter : N/A
     ************************************************************************************************/
     public void getTotalConfigCount() {
    	 //Retrieve the Total count per metadata component 
    	ArrayList<ArrayList<String>> metadataLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, METADATATYPE From METADATACOMPONENTResultsView Group By METADATATYPE");
     	ArrayList<String> metadataTypeList = metadataLists.get(0);
     	ArrayList<String> metadataTypeCountArray = metadataLists.get(1);
     	System.out.println(metadataTypeList);
     	System.out.println(metadataTypeCountArray);
     	//Retrieve the Total count for group of buttons listed under same Layout 
     	ArrayList<ArrayList<String>> buttonLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID From BUTTONSResultsView Group By LAYOUTID");
     	ArrayList<String> layoutBIDList = buttonLists.get(0);
     	ArrayList<String> layoutBCountArray = buttonLists.get(1);
     	ArrayList<String> layoutBNameList = new ArrayList<String>();
     	for(int i=0;i<layoutBIDList.size();i++) {
     		String strLayoutBID = layoutBIDList.get(i);
     		String strLayoutBQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strLayoutBID+"'";
     		layoutBNameList.add(jdbc.getValue("METADATANAME", strLayoutBQuery));
     	}
     	System.out.println(layoutBNameList);
     	System.out.println(layoutBCountArray);
     	//Retrieve the Total count for group of fields listed under same Layout 
     	ArrayList<ArrayList<String>> fieldsLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID From FIELDSResultsView Group By LAYOUTID");
     	ArrayList<String> layoutFIDList = fieldsLists.get(0);
     	ArrayList<String> layoutFCountArray = fieldsLists.get(1);
     	ArrayList<String> layoutFNameList = new ArrayList<String>();
     	for(int i=0;i<layoutFIDList.size();i++) {
     		String strLayoutFID = layoutFIDList.get(i);
     		String strLayoutFQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strLayoutFID+"'";
     		layoutFNameList.add(jdbc.getValue("METADATANAME", strLayoutFQuery));
     	}
     	System.out.println(layoutFNameList);
     	System.out.println(layoutFCountArray);
     	//Retrieve the Total count for group of Related Lists under same Layout 
     	ArrayList<ArrayList<String>> relatedListLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, LAYOUTID , LABEL From RELATEDLISTSResultsView Group By LAYOUTID,LABEL");
     	ArrayList<String> relatedListIDList = relatedListLists.get(0);
     	ArrayList<String> relatedListCountArray = relatedListLists.get(1);
     	ArrayList<String> relatedListNameList = new ArrayList<String>();
     	for(int i=0;i<relatedListIDList.size();i++) {
     		String strRelatedLayoutID = relatedListIDList.get(i);
     		String strRelatedLayoutQuery = "Select METADATANAME From METADATACOMPONENTRESULTSVIEW Where METADATATYPE = 'Layout' AND ID='"+strRelatedLayoutID+"'";
     		relatedListNameList.add(jdbc.getValue("METADATANAME", strRelatedLayoutQuery));
     	}
     	System.out.println(relatedListNameList);
     	System.out.println(relatedListCountArray);
     	//Retrieve the Total count for Triggers group by Object 
     	ArrayList<ArrayList<String>> triggerLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, OBJECTNAME From TRIGGERSRESULTSVIEW Group By OBJECTNAME");
     	ArrayList<String> triggerObjectList = triggerLists.get(0);
     	ArrayList<String> triggerCountArray = triggerLists.get(1);
     	System.out.println(triggerObjectList);
     	System.out.println(triggerCountArray);
     	//Retrieve the Total count for Triggers group by Object 
     	ArrayList<ArrayList<String>> validationRuleLists = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount, OBJECTNAME From VALIDATIONRULESRESULTSVIEW Group By OBJECTNAME");
     	ArrayList<String> validationRuleObjectList = validationRuleLists.get(0);
     	ArrayList<String> validationRuleCountArray = validationRuleLists.get(1);
     	System.out.println(validationRuleObjectList);
     	System.out.println(validationRuleCountArray);
     	
     }		 
	
	//Store org metadata in XMLformat
	private void saveMetadata() {
		
	}
	
	
}
