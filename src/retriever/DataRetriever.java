/**
 * 
 */
package retriever;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import utilities.JavaDBManager;

/**
 * @author n691581
 *
 */
public class DataRetriever {

	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	//public static ArrayList<String> ConfigurationDateList = new ArrayList<String>();	
	public static PriorityQueue<String> ConfigurationDateList = new PriorityQueue<String>();
	
	/******************************************************************************************************************************************************
	 * @return object instance for this singleton class
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public DataRetriever() {
		ConfigurationDateList = getConfigurationDateList();	
		//System.out.println("Inside Data retriever : "+ConfigurationDateList);
	}
	
	/******************************************************************************************************************************************************
	 * @return priority queue of all distinct date times stored
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	private PriorityQueue<String> getConfigurationDateList() {
		PriorityQueue<String> dateList = new PriorityQueue<String>();
		PriorityQueue<String> convertedDateList = new PriorityQueue<String>();
		
		dateList = jdbc.fetchConfigurationCaptureDates();
		for(String date : dateList) {
			//convertedDateList.add(tokenToDateConversion(date));
			convertedDateList.add(date);
		}
		return convertedDateList;
	}
		
	/******************************************************************************************************************************************************
	 * @return All metadata in the passed org on given date
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public ArrayList<String> getMetadataNameList(String OrgID, String configurationDateTimeToken, String MetadataName) {		
		ArrayList<String> metadataList = new ArrayList<String>();			
		metadataList = jdbc.fetchMetadataName(OrgID, configurationDateTimeToken,MetadataName);		
		return metadataList;			
	}
	
	/******************************************************************************************************************************************************
	 * @return All impacted profile names in the passed org on given date
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public ArrayList<String> getProfileNameList(String strColumnName, String sqlQuery) {		
		ArrayList<String> profileNameList = new ArrayList<String>();			
		profileNameList = jdbc.getValues(strColumnName, sqlQuery);	
		
		return profileNameList;			
	}

	/******************************************************************************************************************************************************
	 * @return All metadata related info from SFDCCOMPRESULT Table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public ArrayList<String> getSFDCResultList(String MetadataName) {		
		ArrayList<String> metadataList = new ArrayList<String>();			
		metadataList = jdbc.fetchSFDCResultMetadataName(MetadataName);		
		return metadataList;			
	}
		
	/******************************************************************************************************************************************************
	 * @return All triggers in the passed org on given date
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public ArrayList<String> getTriggerNameList(String OrgID, String configurationDateTimeToken) {		
		ArrayList<String> triggerList = new ArrayList<String>();			
		triggerList = jdbc.fetchTriggers(OrgID, configurationDateTimeToken);		
		return triggerList;			
	}
	
//	/******************************************************************************************************************************************************
//	 * @return All Validation rules for a specific object
//	 * @author Parantap Samajdar
//	 ******************************************************************************************************************************************************/
//	public String getValidationRulesList(String OrgID, String configurationDateTimeToken, String objectName) {		
//		String validationRulesList = new String();			
//		validationRulesList = jdbc.fetchValidationRulesList(OrgID, configurationDateTimeToken,objectName);		
//		return validationRulesList;			
//	}
	/******************************************************************************************************************************************************
	 * @return Total Field Count for a specific object
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public int getFieldCountPerObject(String objectName) {	
		int TotalFieldCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);
		
		for(int i=0;i<impactedLayoutIDList.size();i++) {
				String subFieldsQuery = "Select Count(*) as TotalFields From FIELDSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				int subFieldsCount = Integer.parseInt(jdbc.getValue("TotalFields", subFieldsQuery));
				System.out.println("Sub field count for Object "+objectName+" :"+subFieldsCount);
				TotalFieldCount = TotalFieldCount + subFieldsCount;
		}
	
		return TotalFieldCount;			
	}	
	/******************************************************************************************************************************************************
	 * @return Impacted Fields Details for a specific object
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public String getImpactedFieldPerObject(String objectName) {	
		String impFieldList = new String();
		String finalReturnValue = new String();	
		int TotalFieldCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);

		for(int i=0;i<impactedLayoutIDList.size();i++) {
				ArrayList<String> distinctHeadingsList= jdbc.getValues("SECTIONHEADING", "Select DISTINCT(SectionHeading) From FIELDSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'");
				String subFieldsQuery = "Select LABEL,SectionHeading From FIELDSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				ArrayList<String> impactedFieldsNameList= jdbc.getValues("LABEL", subFieldsQuery);
				System.out.println("impactedLayoutNameList : "+impactedFieldsNameList);
				impFieldList = impFieldList + ("Below are the Impacted Fields for Page Layout Name : " + impactedLayoutNameList.get(i) + System.getProperty("line.separator") +
						 "" + impactedFieldsNameList + System.getProperty("line.separator")) ;
				impFieldList = impFieldList + ("------------------------------------------------------------------" + System.getProperty("line.separator"));

			}
			finalReturnValue = finalReturnValue + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			finalReturnValue = finalReturnValue + impFieldList;
			
		return finalReturnValue;
	}	
	/******************************************************************************************************************************************************
	 * @return Total Related Lists Count for a specific object
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public int getRelatedListCountPerObject(String objectName) {	
		int TotalRelatedListCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);
		
		for(int i=0;i<impactedLayoutIDList.size();i++) {
				String subRelatedListQuery = "Select Count(*) as TotalRelatedList From RELATEDLISTSRESULTSVIEW where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				int subRelatedListCount = Integer.parseInt(jdbc.getValue("TotalRelatedList", subRelatedListQuery));
				System.out.println("Sub RelatedList count for Object "+objectName+" :"+subRelatedListCount);
				TotalRelatedListCount = TotalRelatedListCount + subRelatedListCount;
		}
	
		return TotalRelatedListCount;			
	}
	/******************************************************************************************************************************************************
	 * @return Impacted Related List Details for a specific object
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public String getImpactedRelatedListPerObject(String objectName) {	
		String impRelatedList = new String();
		String finalReturnValue = new String();	
		int TotalRelatedListCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);

		for(int i=0;i<impactedLayoutIDList.size();i++) {
				//ArrayList<String> distinctHeadingsList= jdbc.getValues("SECTIONHEADING", "Select DISTINCT(SectionHeading) From FIELDSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'");
				String subRelatedListQuery = "Select COLUMNFIELD From RELATEDLISTSRESULTSVIEW where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				ArrayList<String> impactedRelatedListNameList= jdbc.getValues("COLUMNFIELD", subRelatedListQuery);
				System.out.println("impactedRelatedListNameList : "+impactedRelatedListNameList);
				impRelatedList = impRelatedList + ("Below are the Impacted Related Lists and it's Corresponding Fields for Page Layout Name : " + impactedLayoutNameList.get(i) + System.getProperty("line.separator") +
						 "" + impactedRelatedListNameList + System.getProperty("line.separator")) ;
				impRelatedList = impRelatedList + ("------------------------------------------------------------------" + System.getProperty("line.separator"));

			}
			finalReturnValue = finalReturnValue + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			finalReturnValue = finalReturnValue + impRelatedList;
			
		return finalReturnValue;
	}	
	/******************************************************************************************************************************************************
	 * @return Total Buttons Count for a specific object
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public int getButtonsCountPerObject(String objectName) {	
		int TotalButtonCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);
		
		for(int i=0;i<impactedLayoutIDList.size();i++) {
				String subButtonsQuery = "Select Count(*) as TotalButtons From ButtonSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				int subButtonsCount = Integer.parseInt(jdbc.getValue("TotalButtons", subButtonsQuery));
				System.out.println("Sub Button count for Object "+objectName+" :"+subButtonsCount);
				TotalButtonCount = TotalButtonCount + subButtonsCount;
		}
	
		return TotalButtonCount;			
	}
	/******************************************************************************************************************************************************
	 * @return Impacted SFDC Buttons per page layouts
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public String getImpactedButtonsPerObject(String objectName) {	
		String impButtonList = new String();
		String finalReturnValue = new String();
		int TotalButtonCount = 0;
		String strLayoutName = null;
		String impactedLayoutNameQuery = "Select MetadataName From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutNameList= jdbc.getValues("MetadataName", impactedLayoutNameQuery);
		System.out.println("impactedLayoutNameList : "+impactedLayoutNameList);
		for(int i=0;i<impactedLayoutNameList.size();i++) {
			String strLayout = impactedLayoutNameList.get(i);
			String[] arrLayout = strLayout.split("-");
			strLayoutName = arrLayout[0];
			if(strLayoutName.equals(objectName)) {
				objectName = strLayoutName;
				break;
			}		
		}
		System.out.println("impacted Object NameList : "+strLayoutName+" & "+objectName);
		String impactedLayoutIDQuery = "Select ID From METADATACOMPONENTResultsView where MetadataName like '"+objectName+"-%' And MetadataType = 'Layout'";	
		ArrayList<String> impactedLayoutIDList= jdbc.getValues("ID", impactedLayoutIDQuery);
		
		for(int i=0;i<impactedLayoutIDList.size();i++) {
				String subButtonsQuery = "Select LABEL From ButtonSResultsView where LayoutID = '"+impactedLayoutIDList.get(i)+"'";
				ArrayList<String> impactedButtonsNameList= jdbc.getValues("LABEL", subButtonsQuery);
				System.out.println("impactedLayoutNameList : "+impactedButtonsNameList);
				impButtonList = impButtonList + ("Impacted Buttons for Page Layout Name : " + impactedLayoutNameList.get(i) + System.getProperty("line.separator") +
						 "are : " + impactedButtonsNameList + System.getProperty("line.separator")) ;
				impButtonList = impButtonList + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			}
			finalReturnValue = finalReturnValue + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			finalReturnValue = finalReturnValue + impButtonList;
	
		return finalReturnValue;			
	}
	/******************************************************************************************************************************************************
	 * @return All triggers for a specific object
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public String getTriggers(String OrgID, String configurationDateTimeToken, String objectName) {		
		String trigger = new String();			
//		trigger = jdbc.fetchTriggers(OrgID, configurationDateTimeToken,objectName);		
		trigger = jdbc.fetchTriggers(objectName);
		return trigger;			
	}
		
	/******************************************************************************************************************************************************
	 * @return all metadata count in the passed org on given date
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public int getMetadataCount(String OrgID, String configurationDateTimeToken, String MetadataName) {		
		ArrayList<String> metadataList = new ArrayList<String>();			
		metadataList = jdbc.fetchMetadataName(OrgID, configurationDateTimeToken,MetadataName);		
		return metadataList.size();		
	}
	
	/******************************************************************************************************************************************************
	 * @return Difference in data between two given time periods
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void generateDifferenceReport(String orgID, String configurationDateTimeToken1, String configurationDateTimeToken2) {	
		String strDifferenceReport = null;
		Map<String,String> tableColumnDetail = new HashMap<String,String>(); 
		final String[] tableList = {"BUTTONS","FIELDS","METADATACOMPONENT","PROFILES","RECORDTYPES","RELATEDLISTS","TRIGGERS","VALIDATIONRULES"};
		for(String tableName : tableList) {
			tableColumnDetail = jdbc.getTableColumns(tableName);
			Set set = tableColumnDetail.entrySet();
			Iterator i = set.iterator();
			String tableColumnList = "";
			while(i.hasNext()) {
				Map.Entry m = (Map.Entry)i.next();
				if(m.getKey().equals("ORG_ID") || m.getKey().equals("TIMESTAMP")) {
					//Do nothing
				} else {
					if(tableColumnList.equals("")) {
						tableColumnList = tableColumnList + m.getKey();
					} else {
						tableColumnList = tableColumnList + "," + m.getKey();
					}					
				}						        
			}
			String queryString = "(Select " + tableColumnList + " from " + tableName + " where timestamp = '" + configurationDateTimeToken1 + "') except " + "(Select " + tableColumnList + " from " + tableName + " where timestamp = '" + configurationDateTimeToken2 + "')";
			jdbc.DifferenceReport(tableName, queryString, configurationDateTimeToken1, configurationDateTimeToken2);
		}			
	}
		
	/******************************************************************************************************************************************************
	 * Convert date received in mm/dd/yyyy hh:mm:ss format into mm-dd-yyyy-hh-mm-ss format
	 * @return Converted date time value 
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public static String dateToTokenConversion(String date) {
		String convertedDateTime = new String();
		String[] StringDateTime = date.split(" ");
		//System.out.println(date);
		//System.out.println(StringDateTime[1]);
		//System.out.println(StringDateTime[0]);
		//System.out.println(StringDateTime[1]);
		if(StringDateTime.length > 2) {
			System.out.println("Invalid date time format encountered");
			return null;		
		}				
		String[] strDatePortion = StringDateTime[0].split("/");
		
		String[] strTimePortion = StringDateTime[1].split(":");
		convertedDateTime = strDatePortion[1] + "-" + strDatePortion[0] + "-" + strDatePortion[2] + "-" + strTimePortion[0] + "-" + strTimePortion[1] + "-" + strTimePortion[2];
		//System.out.println(convertedDateTime);
		return convertedDateTime;		
	}
		
	/******************************************************************************************************************************************************
	 * Return date received in mm-dd-yyyy-hh-mm-ss format into mm/dd/yyyy hh:mm:ss format
	 * @return Converted date time value
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public static String tokenToDateConversion(String token) {
		String convertedDate = new String();

		String[] StringPieceCollection = token.split("-");		
		if(StringPieceCollection.length > 6) {
			System.out.println("Invalid date time format encountered");
			return null;		
		}		
		convertedDate = StringPieceCollection[1] + "/" + StringPieceCollection[0] + "/" + StringPieceCollection[2] + " "; // Date part of the string
		convertedDate = convertedDate + StringPieceCollection[3] + ":" + StringPieceCollection[4] + ":" + StringPieceCollection[5]; // Time part of the string
		//System.out.println("Inside Method : 'tokenToDateConversion' : Input Token value : "+token+" And Output Token Value : "+convertedDate);
		return convertedDate;		
	}
	
	/******************************************************************************************************************************************************
	 * Return date received in dd-mm-yyyy-hh-mm-ss format into dd/mm/yyyy format
	 * @return Converted date time value
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public static String stringToDateConversion(String localDate) {
		String convertedDate = new String();
		//System.out.println("Inside Method : 'stringToDateConversion' : Token value : "+localDate);
		String[] StringPieceCollection = localDate.split("-");		
		if(StringPieceCollection.length > 6) {
			System.out.println("Invalid date time format encountered");
			return null;		
		}
		if(StringPieceCollection[0].length()<4) {
			convertedDate = StringPieceCollection[0] + "/" + StringPieceCollection[1] + "/" + StringPieceCollection[2] + ""; // Date part of the string
		//convertedDate = convertedDate + StringPieceCollection[3] + ":" + StringPieceCollection[4] + ":" + StringPieceCollection[5]; // Time part of the string
		}else {
			convertedDate = StringPieceCollection[2] + "/" + StringPieceCollection[1] + "/" + StringPieceCollection[00] + ""; // Date part of the string
		}
		
		return convertedDate;		
	}	
	/*	
	 * @return Add or minus number of days to given date time value
	 * @author Debdatta Porya
	 */
	public static Date addDays(Date date,int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);   //minus number would decrement the days
		return cal.getTime();
	}
	/******************************************************************************************************************************************************
	 * Return date received in dd-mm-yyyy-hh-mm-ss format into given format like "MM/dd/yyyy hh:mm:ss" OR "EEE MMM dd HH:mm:ss zzz yyyy"
	 * @return Converted date time value
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	
	public static Date stringToGivenDateFormatConversion(String timestamp, String inputFormat) throws ParseException {
		Date convertedDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat(inputFormat , Locale.US);
		if(timestamp.contains("-")) {
			convertedDate = dateFormat.parse(tokenToDateConversion(timestamp));	
			//System.out.println(convertedDate);
		}else {
			convertedDate = dateFormat.parse(timestamp);	
			//System.out.println(convertedDate);
		}
		return convertedDate;
	}
}
