/**
 * 
 */
package utilities;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sforce.ws.ConnectionException;

import retriever.DataRetriever;
import retriever.SFDCComparisonRetriever;

//import retriever.ToolingRetriever;

/*******************************************************************************************************************************************************
 * @author Parantap Samajdar
 * 
 * Perform SFDB database related operations 
 ******************************************************************************************************************************************************/
public class JavaDBManager {
	
	private static JavaDBManager javaDBManager;
	private static Connection conn1 = null;	
	//private static String dbURL = "jdbc:derby://localhost:1527/C:/Users/Debdatta Porya/eclipse-workspace/SFDCComparatorBOT/lib/sfdb";
//	private static String dbURL = "jdbc:derby:C:\\Users\\873173\\OneDrive - Cognizant\\Important\\IATOSAReport\\SFDC_Quality_Analyzer_V2\\lib\\Adsfdb;create=true";
//	private static String dbURL = "jdbc:derby:C:\\Users\\2266819\\CCTP\\SFDC_Quality_Analyzer_V2\\lib\\lksfdb;create=true";
	private static String dbURL = "jdbc:derby:C:\\Users\\2266819\\CCTP\\SFDC_Quality_Analyzer_V2\\lib\\lksfdb;create=true";
	private PreparedStatement preparedStatement = null;
	private static final Logger logger = Logger.getLogger(JavaDBManager.class.getName());
	private DataRetriever dr;
	/******************************************************************************************************************************************************
	 * Constructor
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 ******************************************************************************************************************************************************/
	private JavaDBManager() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		//Debdatta : Below Code snippet is for Derby Embedded driver
		DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		Properties p = System.getProperties();
//    	p.setProperty("derby.system.home", "C:\\1_SOURAV\\SFDC_Quality_Analyzer-ScriptAnalyzerReport\\lib\\db-derby-10.14.2.0-bin\\bin");  // *** To be updated to remove hard coded path
		p.setProperty("derby.system.home", "C:\\Users\\2266819\\CCTP\\SFDC_Quality_Analyzer_V2\\lib\\db-derby-10.14.2.0-bin\\bin");  // *** To be updated to remove hard coded path
//		p.setProperty("derby.system.home", "C:\\\\Users\\\\2266819\\\\CCTP\\\\SFDC_Quality_Analyzer_V2\\\\lib\\\\lksfdb");  // *** To be updated to remove hard coded path
		
    	//p.setProperty("derby.system.home", "////common/ing/HFDDATA/HFDDC/OPERAT/pdcsrVOL1/ADCS/Web_Automation/db"); // *** To be updated to remove hard coded path
        try {
        	//Debdatta : Below Code snippet is for Derby Client Server
//        	Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        	conn1 = DriverManager.getConnection(dbURL);
        	conn1.setAutoCommit(false);
        	this.checkTables();
        	logger.log(Level.INFO, "Connection to SFDB successful");        	        	
        } catch (SQLException e) {			
			logger.log(Level.INFO, "Connection to SFDB failed. Aborting execution.");			
			System.exit(0); 
		} 
	}
	
	/******************************************************************************************************************************************************
	 * Create instance of JavaDBManager class if one is not available already 
	 * @author Parantap Samajdar
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 ******************************************************************************************************************************************************/
	public static JavaDBManager getJavaDBManagerInstance() {		
		if(javaDBManager == null) {
				try {
					javaDBManager = new JavaDBManager();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return javaDBManager;		
	}
	
	
	/******************************************************************************************************************************************************
	 * Get all table columns 
	 *  @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public Map<String,String> getTableColumns(String tableName) {	
		ArrayList<String> columnNames = new ArrayList<String>();
		Map<String,String> columnInfo = new HashMap<String,String>();
		String quertyTableSchema = "select COLUMNNAME,COLUMNDATATYPE FROM sys.systables t, sys.syscolumns WHERE TABLEID = REFERENCEID and tablename = '" + tableName + "'";
		try	{
		 	PreparedStatement pStmt = conn1.prepareStatement(quertyTableSchema);
		 	java.sql.ResultSet rs = pStmt.executeQuery();
            while(rs.next()) {
            	String columnName = rs.getString("COLUMNNAME");
            	String columnDataType = rs.getString("COLUMNDATATYPE");
            	columnInfo.put(columnName,columnDataType);            			            
	        }            
            rs.close();
            pStmt.close();
        } catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
            
        }
		return columnInfo;
	}
	
	/******************************************************************************************************************************************************
	 * Fetch Latest configuration capture dates from database 
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public String getTimeStamp() {
			
		String Query = "select TimeStampKey from PreferenceCollection Order By ID Desc FETCH FIRST ROW ONLY";    
		String timeStamp = null;
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	timeStamp = rs.getString("TimeStampKey");		            
		        }
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 //logger.log(Level.INFO, "Method executed :: getTimeStamp ");
		 return timeStamp;		 
	}
	
	/******************************************************************************************************************************************************
	 * Fetch single row of given column for the given SQL Query
	 * @author Debdatta Porya
	 * @input parameter : strColumnName , sqlQuery
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public String getValue(String strColumnName, String sqlQuery) {
			
		String Query = sqlQuery;    
		String retResult = null;
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	retResult = rs.getString(strColumnName);		            
		        }
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 //logger.log(Level.INFO, "Method executed :: getOrgID ");
		 return retResult;		 
	}
	/******************************************************************************************************************************************************
	 * Fetch all rows of given column for the given sql query
	 * @author Debdatta Porya
	 * @input parameter : strColumnName , sqlQuery
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<String> getValues(String strColumnName, String sqlQuery) {
		ArrayList<String> retResults = new ArrayList<>();		
		String Query = sqlQuery;		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(Query);		
			java.sql.ResultSet rs = pStmt.executeQuery();
			while(rs.next()) {
				if(rs.getString(strColumnName) != " ") {
					retResults.add(rs.getString(strColumnName));
				}
			}			
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
		return retResults;		
	}
	/******************************************************************************************************************************************************
	 * Fetch all rows of given view
	 * @author Debdatta Porya
	 * @input parameter : viewName
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ResultSet getViewResultSet(String ViewName) {	
		String Query = "Select * from "+ViewName;		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(Query);		
			java.sql.ResultSet retResults = pStmt.executeQuery();
			return retResults;
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
	}
	/******************************************************************************************************************************************************
	 * Fetch all rows of given Table or View based on Query string
	 * @author Debdatta Porya
	 * @input parameter : Query String
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ResultSet getResultSet(String QueryString) {	
		String Query = QueryString;		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(Query);		
			java.sql.ResultSet retResults = pStmt.executeQuery();
			return retResults;
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
	}
	
	/******************************************************************************************************************************************************
	 * Fetch Distinct row value of given columns in view or Table
	 * @author Debdatta Porya
	 * @input parameter : viewName , column names
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ResultSet getResultSetWithDistinctRow(String TableName, String columnNames) {	
		
		String Query = "Select DISTINCT "+columnNames+" from "+TableName;		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(Query);		
			java.sql.ResultSet retResults = pStmt.executeQuery();
			return retResults;
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
	}
	/******************************************************************************************************************************************************
	 * Fetch Latest configured Org Id from database 
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public String getOrgID() {
			
		String Query = "select OrgIDKey from PreferenceCollection Order By ID Desc FETCH FIRST ROW ONLY";    
		String orgId = null;
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	orgId = rs.getString("OrgIDKey");		            
		        }
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 //logger.log(Level.INFO, "Method executed :: getOrgID ");
		 return orgId;		 
	}
	/******************************************************************************************************************************************************
	 * Fetch all configuration capture dates from database 
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public PriorityQueue<String> fetchConfigurationCaptureDates() {
		
		PriorityQueue<String> dateList = new PriorityQueue<String>();		
		String Query = "select distinct(TIMESTAMP) from MetadataComponent";    
		
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	String timeStamp = rs.getString("TIMESTAMP");
	            	dateList.add(timeStamp);		            
		        }
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 //logger.log(Level.INFO, "Method executed :: fetchConfigurationCaptureDates");
		 return dateList;		 
	}
	
	/******************************************************************************************************************************************************
	 * Fetch all validation rules for the given object 
	 * @author Parantap Samajdar
	 * @modifedby Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public String fetchValidationRulesList(String objectName) {
//		public String fetchValidationRulesList(String orgID, String configurationDateTimeToken, String objectName) {
		String validationRulesList = new String();
		String finalReturnValue = new String();
		int validationRuleCount = 0;
//		String query = "Select * from ValidationRules Where org_id = ? and timestamp = ? and Objectname = ?";
		String query = "Select * from ValidationRules Where Objectname = ?";
		try {
			PreparedStatement pStmt = conn1.prepareStatement(query);
//			pStmt.setString(1, orgID);
//			pStmt.setString(2, configurationDateTimeToken);
			pStmt.setString(1, objectName);
			java.sql.ResultSet rs = pStmt.executeQuery();
			while(rs.next()) {
				validationRuleCount++;
				String validationName = rs.getString("VALIDATIONNAME");
				String errorFormula = rs.getString("ERRORFORMULA");
				String errorDisplayField = rs.getString("ERRORDISPLAYFIELD");
				String errorMessage = rs.getString("ERRORMESSAGE");
				
				validationRulesList = validationRulesList + ("Name : " + validationName + System.getProperty("line.separator") +
										 "Error Formula : " + errorFormula + System.getProperty("line.separator") +
										 "Error Display Field : " + errorDisplayField + System.getProperty("line.separator") +
										 "Error Message : " + errorMessage + System.getProperty("line.separator") 
										);
				validationRulesList = validationRulesList + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			}
			finalReturnValue = "Validation rule count for " + objectName + " object is : " + validationRuleCount + System.getProperty("line.separator");
			finalReturnValue = finalReturnValue + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			finalReturnValue = finalReturnValue + validationRulesList;
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
		return finalReturnValue;
	}
	
	/******************************************************************************************************************************************************
	 * Fetch all triggers for the given object 
	 * @author Parantap Samajdar
	 * @modifedby Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public String fetchTriggers(String objectName) {
//		public String fetchTriggers(String orgID, String configurationDateTimeToken, String objectName) {
		String triggerList = new String();
		String finalReturnValue = new String();
		int triggerCount = 0;
		String query = "Select * from Triggers Where Objectname = ?";		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(query);
//			pStmt.setString(1, orgID);
//			pStmt.setString(2, configurationDateTimeToken);
			pStmt.setString(1, objectName);
			java.sql.ResultSet rs = pStmt.executeQuery();
			while(rs.next()) {
				triggerCount++;
				String triggerName = rs.getString("TRIGGERNAME");
				String triggerStatus = rs.getString("TRIGGERSTATUS");
				double apiVersion = rs.getDouble("APIVERSION");
				boolean isBulkTrigger = rs.getBoolean("UsageIsBulk");
				
				triggerList = triggerList + ("Trigger Name : " + triggerName + System.getProperty("line.separator") +
										 "Trigger Status : " + triggerStatus + System.getProperty("line.separator") +
										 "API Version : " + apiVersion + System.getProperty("line.separator") +
										 "Bulk Trigger : " + isBulkTrigger + System.getProperty("line.separator") 
										);
				triggerList = triggerList + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			}
			finalReturnValue = "Trigger count for " + objectName + " object is : " + triggerCount + System.getProperty("line.separator");
			finalReturnValue = finalReturnValue + ("------------------------------------------------------------------" + System.getProperty("line.separator"));
			finalReturnValue = finalReturnValue + triggerList;
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
		return finalReturnValue;		
	}
	
	/******************************************************************************************************************************************************
	 * Fetch all triggers for the org for a given time period
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<String> fetchTriggers(String orgID, String configurationDateTimeToken) {
		ArrayList<String> triggerList = new ArrayList<>();		
		String query = "Select TRIGGERNAME from Triggers Where org_id = ? and timestamp = ?";		
		try {
			PreparedStatement pStmt = conn1.prepareStatement(query);
			pStmt.setString(1, orgID);
			pStmt.setString(2, configurationDateTimeToken);			
			java.sql.ResultSet rs = pStmt.executeQuery();
			while(rs.next()) {				
				triggerList.add(rs.getString("TRIGGERNAME"));
			}			
		} catch(SQLException sqlExcept) {
			sqlExcept.printStackTrace();
            return null;
		}
		return triggerList;		
	}

	/******************************************************************************************************************************************************
	 * Fetch all profiles from database for the given org id on given date 
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<String> fetchMetadataName(String OrgID, String configurationDateTimeToken, String MetadataName) {
		
		ArrayList<String> profileList = new ArrayList<String>();		
		String Query = "Select MetadataName from MetadataComponent where metadatatype = '" + MetadataName + "' and ORG_ID = '" + OrgID + "' and TIMESTAMP = '" + configurationDateTimeToken + "'";    
		
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	String strMetadataName = rs.getString("MetadataName");
	            	profileList.add(strMetadataName);		            
		        }
	            //logger.log(Level.INFO, "Query executed :: " + Query);
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 		 
		 return profileList;		 
	}
	/******************************************************************************************************************************************************
	 * Fetch all SFDC comparison results from SFDCCompResults Table for the given Metadatatype
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<SFDCComparisonRetriever> fetchSFDCComparisonResults(String MetadataType) {
		
		ArrayList<SFDCComparisonRetriever> sfdcComponentList = new ArrayList<SFDCComparisonRetriever>();		
		String Query = "Select MetadataType,MetadataName,ID,CreatedDate,LastModifiedDate from METADATACOMPONENTResultsView where metadatatype = '" + MetadataType + "'";    
		
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	 String strMetadataType = rs.getString("MetadataType");
	            	 String strMetadataName = rs.getString("MetadataName");
	            	 String strID = rs.getString("ID");
	            	 String strCreatedDate = rs.getString("CreatedDate");
	            	 String strLastModifiedDate = rs.getString("LastModifiedDate");
	            	 SFDCComparisonRetriever sfdcComponent = new SFDCComparisonRetriever(strMetadataType,strMetadataName,strID,strCreatedDate,strLastModifiedDate);
	            	 sfdcComponentList.add(sfdcComponent);		            
		        }
	            //logger.log(Level.INFO, "Query executed :: " + Query);
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 		 
		 return sfdcComponentList;		 
	}
	
	/******************************************************************************************************************************************************
	 * Fetch all Metadatanames from database for the given Metadatatype
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<String> fetchSFDCResultMetadataName(String MetadataType) {
		
		ArrayList<String> metadataNameList = new ArrayList<String>();		
		String Query = "Select MetadataName from METADATACOMPONENTResultsView where metadatatype = '" + MetadataType + "'";    
		
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	String strMetadataName = rs.getString("MetadataName");
	            	metadataNameList.add(strMetadataName);		            
		        }
	            //logger.log(Level.INFO, "Query executed :: " + Query);
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 		 
		 return metadataNameList;		 
	}
	/******************************************************************************************************************************************************
	 * insert data into User table
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public void insertUserTable(String UserName, String Password, String URL, String OrgID, String timeStamp) {		 
		
		 String insertQuery = "Insert into USERS (USERNAME,PASSWORD,URL,ORG_ID,TIMESTAMPKEY) values ('" + UserName + "','" + Password + "','" + URL + "','" + OrgID + "','" + timeStamp +"')";
		 String findExistingQuery = "Select count(*) as currentCount from USERS where username = '" + UserName + "' and password = '"+ Password +"' and org_id = '" + OrgID + "'";		 
		 
		 try {
			PreparedStatement pstmtCheckDup = conn1.prepareStatement(findExistingQuery);
			java.sql.ResultSet rs = pstmtCheckDup.executeQuery();
			rs.next();
			if(rs.getInt("currentCount") != 0) {				
				String updateQuery = "Update USERS Set PASSWORD = '" + Password + "' Where USERNAME = '" + UserName + "'";
				Statement InsertStmt = conn1.createStatement();
				InsertStmt.executeUpdate(updateQuery);					
				InsertStmt.close();
				conn1.commit();
				logger.log(Level.INFO, "User name " + UserName + " already exists for org " + OrgID + ". Updated entry with latest information.");
			} else {				
				try {
					Statement InsertStmt = conn1.createStatement();
					InsertStmt.executeUpdate(insertQuery);					
					InsertStmt.close();
					conn1.commit();
					logger.log(Level.INFO, "User name " + UserName + " for org " + OrgID + " is entered in database.");
				} catch (SQLException ex) {
					logger.log(Level.INFO, "Error entering user name into database.");										
				}				
			}	
			pstmtCheckDup.close();
		} catch (SQLException e) {	
			logger.log(Level.INFO, "Error checking user name into database");
			e.printStackTrace();
		} finally {
			//logger.log(Level.INFO, "Method executed :: updateUserTable");			
		}
	}
	
	/******************************************************************************************************************************************************
	 * insert data into Metadata component table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void updateMetadataComponentTable(String OrgID, String TimeStamp, String MetadataType, String MetadataName, String FileName, String ID, String Type, String ClassName, String CreatedDate, String CreatedBy, String LastModifiedDate, String LastModifiedBy) {
		String insertQuery = "Insert into MetadataComponent Values (" + "'" + OrgID + "','" + TimeStamp + "','" + MetadataType + "','"  + MetadataName + "','" +  FileName + "','" +  ID + "','" + Type + "','" + ClassName + "','" + CreatedDate + "','"  + CreatedBy + "','"  + LastModifiedDate + "','"   + LastModifiedBy + "')";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(insertQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error updating Metadata Component table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
		
	}
	/******************************************************************************************************************************************************
	 * insert data into SFDC_CONFIGTODATE_TABLE table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void updateSFDC_CONFIGTODATE_Table(String OrgID, String TimeStamp) {
		String insertQuery = "Insert into SFDC_CONFIGTODATE_TABLE SELECT * FROM METADATACOMPONENT Where ORG_ID = '" + OrgID + "' And TIMESTAMP = '" + TimeStamp + "'";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(insertQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error updating SFDC_CONFIGTODATE_TABLE table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
		
	}
	/******************************************************************************************************************************************************
	 * insert data into SFDC_CONFIGWITHDATE_TABLE table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void updateSFDC_CONFIGWITHDATE_Table(String OrgID, String TimeStamp) {
		String insertQuery = "Insert into SFDC_CONFIGWITHDATE_TABLE SELECT * FROM METADATACOMPONENT Where ORG_ID = '" + OrgID + "' And TIMESTAMP = '" + TimeStamp + "'";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(insertQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error updating SFDC_CONFIGWITHDATE_TABLE table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
		
	}
	/******************************************************************************************************************************************************
	 * insert data difference into View
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void createSFDCComparisonResultsView(String insertQuery) {
		//String insertQuery = "CREATE VIEW SFDCMETADATACOMPONENT SELECT * FROM (SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM SFDC_CONFIGTODATE_TABLE UNION ALL SELECT METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE FROM SFDC_CONFIGWITHDATE_TABLE) A GROUP BY METADATATYPE,METADATANAME,ID,CREATEDDATE,LASTMODIFIEDDATE HAVING COUNT(*) <> 2";
		logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(insertQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error updating SFDCComparisonResultsView View");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executjpooked :: updateMetadataComponentTable");
			}
		
	}
	/******************************************************************************************************************************************************
	 * Delete all data from SFDCCOMPRESULTS table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void deleteSFDCCOMPRESULTSTable() {
		String deleteQuery = "Delete from SFDCCOMPRESULTS";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(deleteQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error deleting data from SFDCCOMPRESULTS table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
		
	}
	/******************************************************************************************************************************************************
	 * Delete all data from SFDC_CONFIGTODATE_TABLE table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void deleteSFDCONFIGTODATETable() {
		String deleteQuery = "Delete from SFDC_CONFIGTODATE_TABLE";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(deleteQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error deleting data from SFDC_CONFIGTODATE_TABLE table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	/******************************************************************************************************************************************************
	 * Delete all data from SFDC_CONFIGWITHDATE_TABLE table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void deleteSFDCONFIGWITHDATETable() {
		String deleteQuery = "Delete from SFDC_CONFIGWITHDATE_TABLE";
		//logger.log(Level.INFO,insertQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(deleteQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error deleting data from SFDC_CONFIGWITHDATE_TABLE table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	
	/******************************************************************************************************************************************************
	 * Create View to store data with SFDC Config Table Data
	 * Input Parameter : OrgID, TimeStamp with Org Comparison WITH DATE
	 * @author Debdatta Porya
	 * @throws ParseException 
	 ******************************************************************************************************************************************************/
	public void createViewWithConfigWithDate(String viewName , String tableName, String OrgID, String TimeStamp, Boolean lastModifiedDateExist) throws ParseException {
		String createQuery = null;
		Date dtConfigWithDate = DataRetriever.stringToGivenDateFormatConversion(TimeStamp,"MM/dd/yyyy hh:mm:ss");
		
		if((lastModifiedDateExist == true)) {
			createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'";
			/*createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'"
					+ " AND LASTMODIFIEDDATE >= '"+DataRetriever.stringToGivenDateFormatConversion(TimeStamp,"MM/dd/yyyy hh:mm:ss")+"'";*/	
		}else {
			createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'";	
		}
		logger.log(Level.INFO, "Create Query : "+createQuery);
		 try {
			 	System.out.println("The query prepared is:"+createQuery);
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(createQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Creating "+viewName+" View");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
	}
	
	/******************************************************************************************************************************************************
	 * Create View to store data with SFDC Config Table Data
	 * Input Parameter : OrgID, TimeStamp with Org Comparison WITH DATE
	 * @author Debdatta Porya
	 * @throws ParseException 
	 ******************************************************************************************************************************************************/
	public void createViewWithConfigToDate(String viewName , String tableName, String OrgID, String TimeStamp, Boolean lastModifiedDateExist) throws ParseException {
		String createQuery = null;
		if(lastModifiedDateExist == true) {
			createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'";	
			/*createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'"
					+ " AND LASTMODIFIEDDATE <= '"+DataRetriever.stringToGivenDateFormatConversion(TimeStamp,"MM/dd/yyyy hh:mm:ss")+"'";*/
		}else {
			createQuery = "CREATE VIEW "+viewName+" AS SELECT * FROM "+tableName+" WHERE ORG_ID = '"+OrgID+"' AND TIMESTAMP = '"+TimeStamp+"'";	
		}
		logger.log(Level.INFO, "Create Query : "+createQuery);
		 try {
			 	System.out.println("The query prepared is: "+createQuery);
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(createQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Creating "+viewName+" View");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	
	/******************************************************************************************************************************************************
	 * Copy Data to One Table from another Table
	 * Input Parameter : childTable , parentTable
	 * @author Debdatta Porya
	 * @throws ParseException 
	 ******************************************************************************************************************************************************/
	public void copyTableData(String childTable, String parentTable, String ColumnNames){
		String CopyTableQuery = "Insert Into "+childTable+" ("+ColumnNames+") SELECT "+ColumnNames+" FROM "+parentTable+"";
		logger.log(Level.INFO, "Copy Data Query : "+CopyTableQuery);
		 try{
				PreparedStatement pstmtUpdateView = conn1.prepareStatement(CopyTableQuery);
				pstmtUpdateView.executeUpdate();				
				pstmtUpdateView.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Copying data into "+childTable+" Table");
				e.printStackTrace();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
	}
	
	/******************************************************************************************************************************************************
	 * Create View to store data with SFDC Config resultant View Data
	 * Input Parameter : ResultantPrimaryViewName , ConfigToDate, ConfigWithDate
	 * @author Debdatta Porya
	 * @throws ParseException 
	 ******************************************************************************************************************************************************/
	public void updateFinalResult(String SummaryPrimaryTableName , String ResultantSecondaryViewName, String summaryTableColumnNames) throws ParseException {
		//**Removed from Method input parameter - Date ConfigWithDate, Date ConfigToDate
		String selectQuery = "SELECT * FROM "+ResultantSecondaryViewName;
		ArrayList<Date> listLastModfiedDate = new ArrayList<Date>();
		int totalCountList = 0;
		Date dtConfigWithDate = DataRetriever.stringToGivenDateFormatConversion(ConfigDifferenceCalculator.getConfigWithDate(),"MM/dd/yyyy hh:mm:ss");
		Date dtConfigToDate = DataRetriever.stringToGivenDateFormatConversion(ConfigDifferenceCalculator.getConfigToDate(),"MM/dd/yyyy hh:mm:ss");
		 try{
			 	PreparedStatement pStmt = conn1.prepareStatement(selectQuery);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	Date dtLastModifiedDate = DataRetriever.stringToGivenDateFormatConversion(rs.getString("LASTMODIFIEDDATE"),"EEE MMM dd HH:mm:ss zzz yyyy");
	            	if(dtConfigToDate.after(dtLastModifiedDate) && dtConfigWithDate.before(dtLastModifiedDate)) {
	            		logger.log(Level.INFO, "("+dtLastModifiedDate+" AFTER "+dtConfigWithDate+") AND ("+dtLastModifiedDate+" BEFORE "+dtConfigToDate+") in View : "+ResultantSecondaryViewName);
	            		totalCountList = totalCountList + 1;
	            		//listLastModfiedDate.add(dtLastModifiedDate);
						String updateViewQuery = "INSERT INTO "+SummaryPrimaryTableName+" ("+summaryTableColumnNames+") SELECT "+summaryTableColumnNames+" FROM "+ResultantSecondaryViewName+" WHERE LASTMODIFIEDDATE = '"+rs.getString("LASTMODIFIEDDATE")+"'";
						PreparedStatement pInsertViewStmt = conn1.prepareStatement(updateViewQuery);
						pInsertViewStmt.executeUpdate(); 
						pInsertViewStmt.close();	 
	            	}else {
	            		logger.log(Level.INFO, "Either ("+dtLastModifiedDate+"  NOT AFTER "+dtConfigWithDate+") OR ("+dtLastModifiedDate+" NOT BEFORE "+dtConfigToDate+") in View : "+ResultantSecondaryViewName);
	            	}
	            }
	            rs.close();
	            pStmt.close();
	            logger.log(Level.INFO, "Total "+totalCountList+" rows updated in "+SummaryPrimaryTableName+" with value of "+listLastModfiedDate);
	        } catch (SQLException sqlExcept){
	            sqlExcept.printStackTrace();
	        }finally {
				try {
					conn1.commit();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
	        }
		}
		 
	 /******************************************************************************************************************************************************
		 * Create View
		 * Input Parameter : View Name
		 * @author Debdatta Porya
		 ******************************************************************************************************************************************************/
		public void createView(String viewName) {
			String createQuery = "Create VIEW "+viewName;
			logger.log(Level.INFO, "Create View Query : "+createQuery);
			 try {
					PreparedStatement pstmtCheckDup = conn1.prepareStatement(createQuery);
					pstmtCheckDup.executeUpdate();				
					pstmtCheckDup.close();
				} catch (SQLException e) {	
					logger.log(Level.INFO, "Error Creating "+viewName+" View");
					e.getMessage();
				} finally {
					try {
						conn1.commit();
					} catch (SQLException e) {
						logger.log(Level.INFO, "Error Creating "+viewName+" View");
						//e.printStackTrace();
						e.getMessage();
					}
					//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
				}
		}
	
	/******************************************************************************************************************************************************
	 * Drop View to Delete data with SFDC Config Table Data
	 * Input Parameter : view name
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void dropView(String viewName) {
		String dropQuery = "DROP VIEW "+viewName;
		logger.log(Level.INFO, "Drop Query : "+dropQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(dropQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Deleting "+viewName+" View");
				e.getMessage();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {
					logger.log(Level.INFO, "Error Deleting "+viewName+" View");
					//e.printStackTrace();
					e.getMessage();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	
	/******************************************************************************************************************************************************
	 * Drop Table to Delete data with SFDC Config Table Data
	 * Input Parameter : table name
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void dropTable(String tableName) {
		String dropQuery = "DROP TABLE "+tableName;
		logger.log(Level.INFO, "Drop Query : "+dropQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(dropQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Deleting "+tableName+" table");
				e.getMessage();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {
					logger.log(Level.INFO, "Error Deleting "+tableName+" table");
					//e.printStackTrace();
					e.getMessage();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	/******************************************************************************************************************************************************
	 * Delete data with SFDC Summary Table
	 * Input Parameter : table name
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void deleteTableData(String tableName) {
		String dropQuery = "DELETE FROM "+tableName;
		logger.log(Level.INFO, "Delete Query : "+dropQuery);
		 try {
				PreparedStatement pstmtCheckDup = conn1.prepareStatement(dropQuery);
				pstmtCheckDup.executeUpdate();				
				pstmtCheckDup.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error Deleting "+tableName+" table Data");
				e.getMessage();
			} finally {
				try {
					conn1.commit();
				} catch (SQLException e) {
					logger.log(Level.INFO, "Error Deleting "+tableName+" table Data");
					//e.printStackTrace();
					e.getMessage();
				}
				//logger.log(Level.INFO, "Method executed :: updateMetadataComponentTable");
			}
	}
	/******************************************************************************************************************************************************
	 * Fetch Total count per salesforce component 
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public ArrayList<ArrayList<String>> fetchTotalConfigCount(String strQuery) {
		
		ArrayList<String> totalCountList = new ArrayList<String>();	
		ArrayList<String> elementList = new ArrayList<String>();	
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

		String Query = strQuery;
		try	{
			 	PreparedStatement pStmt = conn1.prepareStatement(Query);
			 	java.sql.ResultSet rs = pStmt.executeQuery();
	            while(rs.next()) {
	            	String totalCount = rs.getString(1);
	            	totalCountList.add(totalCount);
	            	String elementName = rs.getString(2);
	            	elementList.add(elementName);		   	
		        }
	            //totalCountList = String.valueOf(elementList.size());
	            result.add(totalCountList);
	            result.add(elementList);
      	      	//result.add(totalCountList.toString());
	            //logger.log(Level.INFO, "Query executed :: " + Query);
	            rs.close();
	            pStmt.close();
	        } catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	            return null;
	        }
		 		 
		 return result;		 
	}
	
	/******************************************************************************************************************************************************
	 * insert data into Metadata component table - Batch version
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void batchUpdateMetadataComponentTable(ArrayList<ArrayList<String>>  QueryList) {
		String insertQuery = "Insert into MetadataComponent (ORG_ID, TIMESTAMP, METADATATYPE, METADATAName, FILENAME, ID, Type, CLASS, CREATEDDATE, CREATEDBY, LASTMODIFIEDDATE, LASTMODIFIEDBY) Values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final int batchSize = 1000;
		int batchCounter = 0;	
		
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				for(ArrayList<String> query : QueryList) {
					pstmt.setString(1, query.get(0));
					pstmt.setString(2, query.get(1));
					pstmt.setString(3, query.get(2));
					pstmt.setString(4, query.get(3));
					pstmt.setString(5, query.get(4));
					pstmt.setString(6, query.get(5));
					pstmt.setString(7, query.get(6));
					pstmt.setString(8, query.get(7));
					pstmt.setString(9, query.get(8));
					pstmt.setString(10, query.get(9));
					pstmt.setString(11, query.get(10));
					pstmt.setString(12, query.get(11));
					/*
					System.out.println(query.get(0));
					System.out.println(query.get(1));
					System.out.println(query.get(2));
					System.out.println(query.get(3));
					System.out.println(query.get(4));
					System.out.println(query.get(5));
					System.out.println(query.get(6));
					System.out.println(query.get(7));
					System.out.println(query.get(8));
					System.out.println(query.get(9));
					System.out.println(query.get(10));
					System.out.println(query.get(11));
					System.out.println(query.get(12));
					*/
					pstmt.addBatch();
					if(++batchCounter % batchSize ==0) {
						pstmt.executeBatch();
						conn1.commit();
					}
				}
				pstmt.executeBatch();		
				conn1.commit();
				pstmt.close();
			} catch (SQLException e) {	
				logger.log(Level.INFO, "Error updating Metadata Component table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: batchUpdateMetadataComponentTable");	
			}
		
	}
	
	/******************************************************************************************************************************************************
	 * insert data into Metadata table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void updateSFDCORGTable(String OrgID, String TimeStamp, ArrayList<String> profileList, ArrayList<String> objectList) {
		String insertQuery = "Insert into SFDC_ORG Values ('" + OrgID + "','" + TimeStamp + "','" + profileList + "','" + objectList + "')";
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.executeUpdate();				
				pstmt.close();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating SFDC ORG table");				
				e.printStackTrace();
			} finally {
				logger.log(Level.INFO, "Method executed :: updateSFDCORGTable");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * insert data into Profile table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void insertProfileTable(String OrgID, String TimeStamp, String ProfileName, String UserCount, String CreatedBy, String CreatedDate, String LastModifiedDate, String LastModifiedBy) {
		String insertQuery = "Insert into Profiles Values ('" + OrgID + "','" + TimeStamp + "','" + ProfileName + "','" + UserCount + "','" + CreatedDate + "','" + CreatedBy + "','" + LastModifiedDate + "','" + LastModifiedBy + "')";		
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {				
				logger.log(Level.WARNING, "Error updating Profiles table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertProfileTable");			
			}
	}
		
	/******************************************************************************************************************************************************
	 * update user count into appropriate row in Profile table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void updateProfileUserCount(String OrgID, String TimeStamp, String ProfileName, String UserCount) {
		String updateQuery = "Update Profiles Set USER_COUNT = ? Where ORG_ID = '" + OrgID + "' And TIMESTAMP = '" + TimeStamp + "' And PROFILE_NAME = '" + ProfileName + "'";		
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(updateQuery);
				pstmt.setString(1, UserCount);
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating Profiles table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: updateProfileUserCount");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * update WebLink table
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	 public void insertWebLinks(String OrgID, String TimeStamp, String linkName, String linkDescription, String linkDisplayType, String linkEncodingKey, boolean hasMenuBar, 
			 boolean hasScrollBar, boolean hasToolBar, String linkType, String masterLabel, String openType, String URL, 
			 String CreatedDate, String CreatedBy,String LastModifiedDate, String LastModifiedBy) throws SQLException {
		
		 String insertQuery = "Insert into Weblinks Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp + "','" 
								+ linkName + "','" 
								+ linkDescription + "','"
								+ linkDisplayType + "','"
								+ linkEncodingKey + "','"
								+ hasMenuBar  + "','"
								+ hasScrollBar + "','"
								+ hasToolBar + "','"
								+ linkType + "','"
								+ masterLabel + "','"
								+ openType + "','"
								+ URL + "','"
								+ CreatedDate + "','"
								+ CreatedBy + "','"
								+ LastModifiedDate + "','"
								+ LastModifiedBy + "')";
		 logger.log(Level.INFO, "Query : " + insertQuery);						
		 try {				
			 	PreparedStatement pstmt = conn1.prepareStatement(insertQuery);				
				pstmt.executeUpdate();				
				pstmt.close();				
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating WebLinks table. Query used :: " + insertQuery);
				//e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertWebLinks");
				conn1.commit();
			}
	}
	
	
	/******************************************************************************************************************************************************
	 * update record type table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void insertRecordTypes(String OrgID, String TimeStamp, String objectName, String recordTypeName, boolean isAvailable, boolean isDefaultRecordType, boolean isMaster, String layoutID, String recordTypeID ) {
		String insertQuery = "Insert into RecordTypes Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp + "','" 
								+ objectName + "','" 
								+ isAvailable + "','"
								+ isDefaultRecordType + "','"
								+ isMaster + "','"
								+ layoutID  + "','"
								+ recordTypeID + "','"
								+ recordTypeName + "')";
								
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);				
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating RecordType table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: updateRecordTypes");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * update buttons table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void insertButtons(String OrgID, String TimeStamp, String LayoutID, String Name, String Label, boolean Custom, String URL, String Encoding, String Content) {
		String insertQuery = "Insert into Buttons Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp + "','"
								+ LayoutID + "','"
								+ Name + "','" 
								+ Label + "','"
								+ Custom + "','"								
								+ URL + "','"
								+ Encoding + "','"
								+ Content + "')";
		 						
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);				
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating Buttons table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertButtons");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * Update validation rules table
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public void insertValidationRules(String OrgID, String TimeStamp, boolean active, String description, String errorDisplayField, String errorMessage, 
			String errorFormula, String id, String validationName, String objectName, String createdDate, String createdBy, String LastModifiedDate, String LastModifiedBy) throws SQLException {
		PreparedStatement pstmt = null;				
		String insertQuery = "Insert into ValidationRules Values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";								
		 try {
				pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, OrgID);
				pstmt.setString(2, TimeStamp);
				pstmt.setBoolean(3, active);
				pstmt.setString(4, description);
				pstmt.setString(5, errorDisplayField);
				pstmt.setString(6, errorMessage);
				pstmt.setString(7, errorFormula);
				pstmt.setString(8, id);
				pstmt.setString(9, validationName);				
				pstmt.setString(10, objectName);
				pstmt.setString(11, createdDate);
				pstmt.setString(12, createdBy);
				pstmt.setString(13, LastModifiedDate);
				pstmt.setString(14, LastModifiedBy);
				pstmt.executeUpdate();							
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating ValidationRules table");
				e.printStackTrace();
			} finally {
				pstmt.close();							
			}
	}
	
	/******************************************************************************************************************************************************
	 * Update Triggers 
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public void insertTriggers(String OrgID, String TimeStamp, String TriggerName, String ObjectName, double ApiVersion, String TriggerBody, String TriggerStatus,
		boolean UsageAfterDelete, boolean UsageAfterInsert, boolean	UsageAfterUndelete, boolean UsageAfterUpdate, boolean UsageBeforeDelete, 
		boolean UsageBeforeInsert, boolean UsageBeforeUpdate, boolean UsageIsBulk, String createdDate, String createdBy, String lastModifiedDate, String lastModifiedBy) {
			
			String query = "Insert into Triggers values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			try {
				PreparedStatement pStmt = conn1.prepareStatement(query);
				pStmt.setString(1, OrgID);
				pStmt.setString(2, TimeStamp);
				pStmt.setString(3, TriggerName);
				pStmt.setString(4, ObjectName);
				pStmt.setDouble(5, ApiVersion);
				pStmt.setString(6, TriggerBody);
				pStmt.setString(7, TriggerStatus);
				pStmt.setBoolean(8, UsageAfterDelete);
				pStmt.setBoolean(9, UsageAfterInsert);
				pStmt.setBoolean(10, UsageAfterUndelete);
				pStmt.setBoolean(11, UsageAfterUpdate);
				pStmt.setBoolean(12, UsageBeforeDelete);
				pStmt.setBoolean(13, UsageBeforeInsert);
				pStmt.setBoolean(14, UsageBeforeUpdate);
				pStmt.setBoolean(15, UsageIsBulk);
				pStmt.setString(16, createdDate);
				pStmt.setString(17, createdBy);
				pStmt.setString(18, lastModifiedDate);
				pStmt.setString(19, lastModifiedBy);
				pStmt.executeUpdate();	
				pStmt.close();
				conn1.commit();
			} catch(SQLException e) {
				e.printStackTrace();
			}
	}
	/******************************************************************************************************************************************************
	 * update Fields table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void insertFields(String OrgID, String TimeStamp, String LayoutID, String LayoutType, String APIName, String Label, boolean IsRequired, String SectionHeading) {
		
		/*
		String insertQuery = "Insert into Fields Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp + "','"
								+ LayoutID + "','"
								+ LayoutType + "','"
								+ APIName + "','" 
								+ Label + "','"
								+ IsRequired + "','"								
								+ SectionHeading + "')";								
		 */
		String insertQuery = "Insert into Fields Values (?, ?, ?, ?, ?, ?, ?, ?)";
		 try {				 	
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, OrgID);
				pstmt.setString(2, TimeStamp);
				pstmt.setString(3, LayoutID);
				pstmt.setString(4, LayoutType);
				pstmt.setString(5, APIName);
				pstmt.setString(6, Label);
				pstmt.setBoolean(7, IsRequired);				
				pstmt.setString(8, SectionHeading);
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {
				System.out.println(insertQuery);
				logger.log(Level.WARNING, "Error updating Fields table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertFields");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * update PreferenceCollection table with TimeStamp and OrgID
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void insertPreferenceCollection(String OrgID, String TimeStamp) {
		
		/*
		String insertQuery = "Insert into Fields Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp"')";								
		 */
		System.out.println("Inside Preference Collection Table : Org Id : "+OrgID+" , Time Stamp : "+TimeStamp);
		String insertQuery = "Insert into PREFERENCECOLLECTION (orgIDKey,TimestampKey) Values (?, ?)";
		 try {				 	
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, OrgID);
				pstmt.setString(2, TimeStamp);

				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {
				System.out.println(insertQuery);
				logger.log(Level.WARNING, "Error updating Preference Collection table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertFields");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * update SFDC Table with table name and table type
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void insertSFDCCompToolsTable(String TableName, String TableType) {
		
		/*
		String insertQuery = "Insert into Fields Values "
								+ "('" + OrgID + "','" 
								+ TimeStamp"')";								
		 */
		String insertQuery = "Insert into SFDC_COMPTOOLS_TABLE(TABLLENAME,TABLETYPE) Values (?, ?)";
		 try {				 	
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, TableName);
				pstmt.setString(2, TableType);

				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {
				System.out.println(insertQuery);
				logger.log(Level.WARNING, "Error updating SFDC Tables table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertFields");			
			}
	}
	
	/******************************************************************************************************************************************************
	 * Update Related Lists table
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void insertRelatedLists(String OrgID, String TimeStamp, String LayoutID, String Name, String Label, boolean Custom, String SortColumn, boolean SortAscending, String ElementType, String ColumnName, String ColumnField, String ColumnFormat, String ColumnLookup, String ButtonName, String ButtonLabel, boolean buttonCustom, String ButtonURL,String ButtonEncoding, String ButtonContent) {
		

		String insertQuery = "Insert into RelatedLists Values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		 try {				 	
				PreparedStatement pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, OrgID);
				pstmt.setString(2, TimeStamp);
				pstmt.setString(3, LayoutID);				
				pstmt.setString(4, Name);
				pstmt.setString(5, Label);
				pstmt.setBoolean(6, Custom);				
				pstmt.setString(7, SortColumn);
				pstmt.setBoolean(8, SortAscending);
				pstmt.setString(9, ElementType);
				pstmt.setString(10, ColumnName);
				pstmt.setString(11, ColumnField);
				pstmt.setString(12, ColumnFormat);
				pstmt.setString(13, ColumnLookup);
				pstmt.setString(14, ButtonName);				
				pstmt.setString(15, ButtonLabel);
				pstmt.setBoolean(16, buttonCustom);
				pstmt.setString(17, ButtonURL);
				pstmt.setString(18, ButtonEncoding);
				pstmt.setString(19, ButtonContent);
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {
				System.out.println(insertQuery);
				logger.log(Level.WARNING, "Error updating Related Lists table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: insertFields");			
			}
	}
	
	
	/******************************************************************************************************************************************************
	 * Insert rows into IA_TO_DOTNEXT_MAPPING table
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public void insertIntoIADotNextTable(String OrgID, String TimeStamp, String objectName, String layoutID, String LayoutName, 
			String buttons, String fields, String relatedLists, String recordTypes, String triggers, String validationRules) throws SQLException {
		PreparedStatement pstmt = null;				
		String insertQuery = "Insert into ValidationRules Values (?,?,?,?,?,?,?,?,?,?,?,?)";								
		 try {
				pstmt = conn1.prepareStatement(insertQuery);
				pstmt.setString(1, OrgID);
				pstmt.setString(2, TimeStamp);
				pstmt.setString(3, objectName);
				pstmt.setString(4, layoutID);
				pstmt.setString(5, LayoutName);
				pstmt.setString(6, buttons);
				pstmt.setString(7, fields);
				pstmt.setString(8, relatedLists);
				pstmt.setString(9, recordTypes);				
				pstmt.setString(10, objectName);
				pstmt.setString(11, triggers);
				pstmt.setString(12, validationRules);
				pstmt.executeUpdate();							
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error Inserting into IA_TO_DOTNEXT_MAPPING table");
				e.printStackTrace();
			} finally {
				pstmt.close();							
			}
	}
	
	/******************************************************************************************************************************************************
	 * Insert rows into IA_TO_DOTNEXT_MAPPING table
	 * @author Debdatta Porya
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	public void insertIntoIADotNextTable(String columnNames, String columnValues) throws SQLException {
		PreparedStatement pstmt = null;				
		String insertQuery = "Insert Into IA_TO_DOTNEXT_MAPPING ("+columnNames+") Values ("+columnValues+")";								
		 try {
				pstmt = conn1.prepareStatement(insertQuery);
				pstmt.executeUpdate();							
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error Inserting into IA_TO_DOTNEXT_MAPPING table");
				e.printStackTrace();
			} finally {
				pstmt.close();							
			}
	}
	/******************************************************************************************************************************************************
	 * update rows into IA_TO_DOTNEXT_MAPPING table
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
	public void updateIADotNextTable(String OrgID, String TimeStamp, String objectName, String layoutID, String columnNameToBeUpdated, String columnValueToBeUpdated) {
		String updateQuery = "Update IA_TO_DOTNEXT_MAPPING Set "+columnNameToBeUpdated+" = ? Where ORG_ID = '" + OrgID + "' And TIMESTAMP = '" + TimeStamp + "' And OBJECTNAME = '"+objectName+"' And LayoutID = '" + layoutID + "'";		
		 try {
				PreparedStatement pstmt = conn1.prepareStatement(updateQuery);
				pstmt.setString(1, columnValueToBeUpdated);
				pstmt.executeUpdate();				
				pstmt.close();
				conn1.commit();
			} catch (SQLException e) {	
				logger.log(Level.WARNING, "Error updating IA_TO_DOTNEXT_MAPPING table");
				e.printStackTrace();
			} finally {
				//logger.log(Level.INFO, "Method executed :: updateProfileUserCount");			
			}
	}
	/******************************************************************************************************************************************************
	 * Display data from username table - Test method - not used
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void displayUserNames() {
		 try
	        {	            
	            String displayQuery = "select * from USERS";            	            
	            PreparedStatement pStmt = conn1.prepareStatement(displayQuery);
	            java.sql.ResultSet rs = pStmt.executeQuery();
	            System.out.println("ID" + "      " + "UserName" + "      " + "Password" + "      " + "URL");
	            System.out.println("---------------------------------------------------------------------");	            
	            while(rs.next()) {
	            	int ID = rs.getInt("ID");
	            	String UserName = rs.getString("USERNAME");
		            String Password = rs.getString("PASSWORD");
		            String URL = rs.getString("URL");
		            System.out.println(ID + "    " + UserName + "    " + Password + "        " + URL);
		            System.out.println("---------------------------------------------------------------------");
		        }
	            rs.close();	            	           	            
	            pStmt.close();
	        }
	        catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	        }
	}
	
	/******************************************************************************************************************************************************
	 * Generate report on differences between data on two timeframes
	 * @author Parantap Samajdar
	 * @param queryString 
	 * @param tableColumnDetail 
	 * @param tableName 
	 ******************************************************************************************************************************************************/
	public void DifferenceReport(String tableName, String queryString, String configurationDateTimeToken1, String configurationDateTimeToken2) {
		String columnName = null, columnDataType = null;
		ArrayList<String> columnNameList = new ArrayList<String>();
		Map<String,String> tableColumnInfo = this.getTableColumns(tableName);
		Set set = tableColumnInfo.entrySet();
		Iterator i = set.iterator();
		System.out.println("Table name : " + tableName + "******************************");
		while(i.hasNext()) {
			Map.Entry m = (Map.Entry)i.next();
			columnName = m.getKey().toString();
			columnDataType = m.getValue().toString();
			columnNameList.add(columnName);
		}
			
		try {
			PreparedStatement pstmt = conn1.prepareStatement(queryString,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			System.out.println(queryString);
			java.sql.ResultSet rs =  pstmt.executeQuery();
			if(!rs.isBeforeFirst()) {
				logger.log(Level.INFO, "No change in " + tableName);
			}
			while(rs.next()) {				
				for(int columnNameIterator = 0; columnNameIterator < columnNameList.size(); columnNameIterator++) {					
					if(columnNameList.get(columnNameIterator).equals("ORG_ID") || columnNameList.get(columnNameIterator).equals("TIMESTAMP")) {
						//Do nothing
					} else {
						System.out.println(columnNameList.get(columnNameIterator) + " ::: ");
						if(tableColumnInfo.get(columnNameList.get(columnNameIterator)).contains("BOOLEAN")) {
							System.out.println(rs.getBoolean(columnNameList.get(columnNameIterator)));
						} else if(tableColumnInfo.get(columnNameList.get(columnNameIterator)).contains("VARCHAR")) {
							System.out.println(rs.getString(columnNameList.get(columnNameIterator)));
						} else if(tableColumnInfo.get(columnNameList.get(columnNameIterator)).contains("DOUBLE")) {
							System.out.println(rs.getDouble(columnNameList.get(columnNameIterator)));
						}
					}									
				}				
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}				
	}
	
	/******************************************************************************************************************************************************
	 * Generate report from database
	 * @author Parantap Samajdar
	 * @throws ConnectionException 
	******************************************************************************************************************************************************/	
	public void generateSummary() throws ConnectionException {
		
		String queryOrgIDTimeStamp = "select distinct org_id, timestamp from Metadatacomponent ";		
		StringBuilder finalReport = new StringBuilder();
		
		int i = 0; //Temporary - remove once testing complete
		//ToolingRetriever tr = new ToolingRetriever("parantap.samajdar.test@voya.com.sw.accp","Cognizant@20");
		
		try
	        {	            
	                        	            
	            PreparedStatement pStmtQueryOrgID = conn1.prepareStatement(queryOrgIDTimeStamp);
	            java.sql.ResultSet rs = pStmtQueryOrgID.executeQuery();	            
	            	            
	            while(rs.next()) {	            	
	            	String orgID = rs.getString("ORG_ID");
	            	String timeStamp = rs.getString("TIMESTAMP");		            
	            	//finalReport.append("<br>");
	            	//finalReport.append("<b>Org Name : " + tr.getOrgName() + "</b>");
		            //finalReport.append("<br>");
		            //finalReport.append("<b>Org ID : " + tr.getOrgId() + "</b>");
		            finalReport.append("<br>");
		            finalReport.append("Report time " + timeStamp);
		            finalReport.append("<br>");		            
		            finalReport.append("<hr size='1' margin-left='auto' margin-right='auto'>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<div classname='Org_Summary'>");		            
		            String queryMetadata = "select Metadatatype, count(metadataname) as Num from MetadataComponent where timestamp = '" + timeStamp + "' and org_id = '" + orgID + "' group by metadatatype";
		            PreparedStatement pStmtQueryMetadata = conn1.prepareStatement(queryMetadata);
		            java.sql.ResultSet rsMetadata = pStmtQueryMetadata.executeQuery();
		            finalReport.append("<table border='1' width='30%'>");
		            finalReport.append("<caption>" + "Org Metadata Summary" + "</caption");
		            finalReport.append("<tr>");
		            finalReport.append("<th>Metadata Type</th>");
		            finalReport.append("<th>Count</th>");
		            finalReport.append("</tr>");
		            while(rsMetadata.next()) {
		            	String metadataName = rsMetadata.getString("METADATATYPE");
		            	int itemCount = rsMetadata.getInt("NUM");
		            	finalReport.append("<tr>");
		            	finalReport.append("<td>" + metadataName + "</td>");
		            	finalReport.append("<td>" + itemCount + "</td>");
		            	finalReport.append("</tr>");
		            }
		            finalReport.append("</table>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");		            
		            finalReport.append("</div>");
		            finalReport.append("<hr size='1' margin-left='auto' margin-right='auto'>");
		            finalReport.append("<br>");	
		            finalReport.append("<div classname='Object_Detail'>");
		            finalReport.append("<b><u>Object Details</u></b>");
		            finalReport.append("<br>");
		            String queryObjectLayout = "select distinct *"
		    				+ "	from RecordTypes rt"
		    				+ "	where "
		    				+ " rt.Objectname "
		    				+ "in (select MetadataName from MetadataComponent "
		    				+ "where Org_id = '" + orgID + "'and timestamp = '" + timeStamp + "' and Metadatatype = 'CustomObject')";
		            
		            PreparedStatement pStmtQueryObjectLayout = conn1.prepareStatement(queryObjectLayout);
		            java.sql.ResultSet rsObjectLayout = pStmtQueryObjectLayout.executeQuery();
		            finalReport.append("<table border='1' width='100%'>");
		            finalReport.append("<caption>" + "Object Information" + "</caption");
		            finalReport.append("<tr>");
		            finalReport.append("<th>Object Name</th>");
		            finalReport.append("<th>Record Type Name</th>");
		            //finalReport.append("<th>Section Heading</th>");
		            finalReport.append("<th>Buttons</th>");
		            finalReport.append("<th>Fields</th>");
		            finalReport.append("<th>Related Lists</th>");
		            finalReport.append("</tr>");		            
		            while(rsObjectLayout.next()) {
		            	String objectName = rsObjectLayout.getString("OBJECTNAME");
		            	String layoutID = rsObjectLayout.getString("LAYOUTID");
		            	String layoutName = rsObjectLayout.getString("LAYOUTNAME");
		            	finalReport.append("<tr>");
		            	finalReport.append("<td>" + objectName + "</td>");            	
		            	finalReport.append("<td title='" + layoutID + "'>" + layoutName + "</td>");
		            	
		            	String queryFields = "select distinct * from Fields where layoutid = '" + layoutID + "'";
		            	String queryButtons = "select distinct * from Buttons where layoutid = '" + layoutID + "'";
		            	String queryRelatedLists = "select distinct * from RelatedLists where layoutid = '" + layoutID + "'";
		            	PreparedStatement pStmtQueryFields = conn1.prepareStatement(queryFields);
		            	PreparedStatement pStmtQueryButtons = conn1.prepareStatement(queryButtons);
		            	PreparedStatement pStmtRelatedLists = conn1.prepareStatement(queryRelatedLists);
			            java.sql.ResultSet rsFields = pStmtQueryFields.executeQuery();
			            java.sql.ResultSet rsButtons = pStmtQueryButtons.executeQuery();
			            java.sql.ResultSet rsRelatedLists = pStmtRelatedLists.executeQuery();	
			            
			            finalReport.append("<td>");
			            while(rsButtons.next()) {
			            	String buttonName = rsButtons.getString("NAME");
			            	String buttonLabel = rsButtons.getString("LABEL");
			            	boolean isCustom = rsButtons.getBoolean("CUSTOM");
			            	String buttonURL = rsButtons.getString("URL");			            	
			            	finalReport.append("Name='" + buttonName + "'&nbsp");
			            	finalReport.append("Label='" + buttonLabel + "'&nbsp");
			            	finalReport.append("Custom='" + isCustom + "'&nbsp");
			            	finalReport.append("URL='" + buttonURL + "'&nbsp");	
			            	finalReport.append("<br>");
			            }
			            finalReport.append("</td>");
			            finalReport.append("<td>");
			            while(rsFields.next()) {
			            	String sectionHeading = rsFields.getString("SECTIONHEADING");
			            	String fieldApiName = rsFields.getString("APINAME");
			            	String fieldLabel = rsFields.getString("LABEL");
			            	boolean isRequired = rsFields.getBoolean("REQUIREDFIELD");	
			            	finalReport.append("<td>" + sectionHeading + "</td>");
			            	finalReport.append("Label='" + fieldLabel + "'" +  "&nbsp");
			            	finalReport.append("API Name='" + fieldApiName + "'" +  "&nbsp");
			            	finalReport.append("Required='" + isRequired + "'" +  "&nbsp");
			            	finalReport.append("<br>");
			            }
			            finalReport.append("</td>");
			            
			            finalReport.append("<td>");			            
			            while(rsRelatedLists.next()) {
			            	String elementType = rsRelatedLists.getString("ELEMENTTYPE");
			            	String relatedListName = rsRelatedLists.getString("NAME");
			            	String relatedListLabel = rsRelatedLists.getString("LABEL");
			            	String fieldLabel = rsRelatedLists.getString("LABEL");
			            	boolean isCustom = rsRelatedLists.getBoolean("CUSTOM");
			            	finalReport.append("Name='" + relatedListName + "'" +  "&nbsp");			            	
			            	finalReport.append("Related List Label='" + relatedListLabel + "'" +  "&nbsp");
			            	/*
			            	finalReport.append("Field Label='" + fieldLabel + "'" +  "&nbsp");
			            	finalReport.append("<br>");
			            	if(elementType.equals("Button")) {
			            		String relatedListButtonName = rsRelatedLists.getString("BUTTONNAME");
			            		String relatedListButtonLabel = rsRelatedLists.getString("BUTTONLABEL");
			            		String relatedListButtonContent = rsRelatedLists.getString("BUTTONCONTENT");
			            		finalReport.append("Button Name = '" + relatedListButtonName + "'" +  "&nbsp");
			            		finalReport.append("Button Label = '" + relatedListButtonLabel + "'" +  "&nbsp");
			            		finalReport.append("Button Content = '" + relatedListButtonContent + "'" +  "&nbsp");
			            		finalReport.append("<br>");
			            	} else {
			            		String RelatedListColumnField = rsRelatedLists.getString("COLUMNNAME");
			            		finalReport.append("Column Name = '" + RelatedListColumnField + "'");
			            		finalReport.append("<br>");
			            	}	
			            	*/		            	
			            				            	
			            }			            
			            finalReport.append("</td>");			            
		            	finalReport.append("</tr>");
		            	//newHtml.createHTML("Test-" + ++i,"Salesforce Organization Analysis",finalReport);
		            }		            
		        }
	            rs.close();	            	           	            
	            pStmtQueryOrgID.close();
	        }
	        catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	        } finally {
	        	HTML_Generator.getHTMLGenerator("Test - ").createHTML_OLD("Salesforce Organization Analysis",finalReport);
	        }
	        
		
	}
	
	/******************************************************************************************************************************************************
	 * Create Detailed report -- Not complete 
	 * @author Parantap Samajdar
	 * @throws ConnectionException 
	 ******************************************************************************************************************************************************/
	private void detailReport() throws ConnectionException {
		// Current just a copy of create summary function - need to refactor
		String queryOrgIDTimeStamp = "select distinct org_id, timestamp from Metadatacomponent ";		
		StringBuilder finalReport = new StringBuilder();
		int i = 0; //Temporary - remove once testing complete
		//ToolingRetriever tr = new ToolingRetriever("parantap.samajdar.test@voya.com.sw.accp","Cognizant@20");
		
		try
	        {	            
	                        	            
	            PreparedStatement pStmtQueryOrgID = conn1.prepareStatement(queryOrgIDTimeStamp);
	            java.sql.ResultSet rs = pStmtQueryOrgID.executeQuery();	            
	            	            
	            while(rs.next()) {	            	
	            	String orgID = rs.getString("ORG_ID");
	            	String timeStamp = rs.getString("TIMESTAMP");		            
	            	finalReport.append("<br>");
	            	//finalReport.append("<b>Org Name : " + tr.getOrgName() + "</b>");
		            //finalReport.append("<br>");
		            //finalReport.append("<b>Org ID : " + tr.getOrgId() + "</b>");
		            //finalReport.append("<br>");
		            finalReport.append("Report time " + timeStamp);
		            finalReport.append("<br>");		            
		            finalReport.append("<hr size='1' margin-left='auto' margin-right='auto'>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");
		            finalReport.append("<div classname='Org_Summary'>");		            
		            String queryMetadata = "select Metadatatype, count(metadataname) as Num from MetadataComponent where timestamp = '" + timeStamp + "' and org_id = '" + orgID + "' group by metadatatype";
		            PreparedStatement pStmtQueryMetadata = conn1.prepareStatement(queryMetadata);
		            java.sql.ResultSet rsMetadata = pStmtQueryMetadata.executeQuery();
		            finalReport.append("<table border='1' width='30%'>");
		            finalReport.append("<caption>" + "Org Metadata Summary" + "</caption");
		            finalReport.append("<tr>");
		            finalReport.append("<th>Metadata Type</th>");
		            finalReport.append("<th>Count</th>");
		            finalReport.append("</tr>");
		            while(rsMetadata.next()) {
		            	String metadataName = rsMetadata.getString("METADATATYPE");
		            	int itemCount = rsMetadata.getInt("NUM");
		            	finalReport.append("<tr>");
		            	finalReport.append("<td>" + metadataName + "</td>");
		            	finalReport.append("<td>" + itemCount + "</td>");
		            	finalReport.append("</tr>");
		            }
		            finalReport.append("</table>");
		            finalReport.append("<br>");
		            finalReport.append("<br>");		            
		            finalReport.append("</div>");
		            finalReport.append("<hr size='1' margin-left='auto' margin-right='auto'>");
		            finalReport.append("<br>");	
		            finalReport.append("<div classname='Object_Detail'>");
		            finalReport.append("<u>Object Details</u>");
		            finalReport.append("<br>");
		            String queryObjectLayout = "select distinct *"
		    				+ "	from RecordTypes rt"
		    				+ "	where "
		    				+ " rt.Objectname "
		    				+ "in (select MetadataName from MetadataComponent "
		    				+ "where Org_id = '" + orgID + "'and timestamp = '" + timeStamp + "' and Metadatatype = 'CustomObject')";
		            
		            PreparedStatement pStmtQueryObjectLayout = conn1.prepareStatement(queryObjectLayout);
		            java.sql.ResultSet rsObjectLayout = pStmtQueryObjectLayout.executeQuery();
		            
		            while(rsObjectLayout.next()) {
		            	String objectName = rsObjectLayout.getString("OBJECTNAME");
		            	String layoutID = rsObjectLayout.getString("LAYOUTID");
		            	String layoutName = rsObjectLayout.getString("LAYOUTNAME");
		            	if(!finalReport.toString().contains(objectName)) {
		            		finalReport.append("Object - " + objectName);
		            		finalReport.append("<br>");
		            		finalReport.append("-----------------------------------------------------------------------------------------");
		            		finalReport.append("<br>");
		            	}            	
		            	
		            	finalReport.append("--" + "Layout Name - " + layoutName + "&nbsp&nbsp&nbsp&nbsp" + "Layout ID - " + layoutID);
		            	finalReport.append("<br>");
		            	finalReport.append("..............................................................................................");
	            		finalReport.append("<br>");
		            	
		            	String queryFields = "select distinct * from Fields where layoutid = '" + layoutID + "'";
		            	String queryButtons = "select distinct * from Buttons where layoutid = '" + layoutID + "'";
		            	String queryRelatedLists = "select distinct * from RelatedLists where layoutid = '" + layoutID + "'";
		            	PreparedStatement pStmtQueryFields = conn1.prepareStatement(queryFields);
		            	PreparedStatement pStmtQueryButtons = conn1.prepareStatement(queryButtons);
		            	PreparedStatement pStmtRelatedLists = conn1.prepareStatement(queryRelatedLists);
			            java.sql.ResultSet rsFields = pStmtQueryFields.executeQuery();
			            java.sql.ResultSet rsButtons = pStmtQueryButtons.executeQuery();
			            java.sql.ResultSet rsRelatedLists = pStmtRelatedLists.executeQuery();
			            finalReport.append("++++++  Buttons  ++++++");
	            		finalReport.append("<br>");
			            while(rsButtons.next()) {
			            	String buttonName = rsButtons.getString("NAME");
			            	String buttonLabel = rsButtons.getString("LABEL");
			            	boolean isCustom = rsButtons.getBoolean("CUSTOM");
			            	String buttonURL = rsButtons.getString("URL");
			            	finalReport.append("----" + " Name = " + buttonName + " ; " + "Label = " + buttonLabel + " ; " + "Is Custome Button ? " + isCustom + " Button URL = " + buttonURL);
			            	finalReport.append("<br>");
			            }
			            finalReport.append("++++++ Fields ++++++");
	            		finalReport.append("<br>");
			            while(rsFields.next()) {
			            	String sectionHeading = rsFields.getString("SECTIONHEADING");
			            	String fieldApiName = rsFields.getString("APINAME");
			            	String fieldLabel = rsFields.getString("LABEL");
			            	boolean isRequired = rsFields.getBoolean("REQUIREDFIELD");
			            	if(!finalReport.toString().contains(sectionHeading)) {
			            		finalReport.append("--------" + " Section = " + sectionHeading);
			            		finalReport.append("<br>");
			            	}
			            	finalReport.append("----------------" + " Label = " + fieldLabel + " ; " + " API = " + fieldApiName + " ; " + "Is Required Field ? " + isRequired);
			            	finalReport.append("<br>");
			            }
		            	finalReport.append("----------------------------------------------------------------------------------------------");
		            	finalReport.append("<br>");
		            	finalReport.append("</div>");
		            	HTML_Generator.getHTMLGenerator("Test - " +  ++i).createHTML_OLD("Salesforce Organization Analysis",finalReport);
		            	pStmtQueryFields.close();
		            	pStmtQueryButtons.close();
		            	pStmtRelatedLists.close();
		            }
		        }
	            rs.close();	            	           	            
	            pStmtQueryOrgID.close();	            
	            
	        }
	        catch (SQLException sqlExcept)
	        {
	            sqlExcept.printStackTrace();
	        } finally {
	        	HTML_Generator.getHTMLGenerator("Test - ").createHTML_OLD("Salesforce Organization Analysis",finalReport);
	        }
	}
	
	/******************************************************************************************************************************************************
	 * Create required tables in SFDB -- Not complete 
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	private void createTable() {		
		ArrayList<String> queryList = new ArrayList<>();
		// List of create table queries 
		String createPrefTable = "CREATE TABLE PREFERENCECOLLECTION "
				+ "(ID INT Not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
				+ "orgIDKey VARCHAR(50),"
				+ "TimestampKey VARCHAR(50))";
		String createSFDCTable = "CREATE TABLE SFDC_COMPTOOLS_TABLE "
				+ "(ID INT Not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
				+ "TABLLENAME VARCHAR(50),"
				+ "TABLETYPE VARCHAR(50))";
		String createUsersTable = "CREATE TABLE USERS "
				+ "(ID INT Not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
				+ "USERNAME VARCHAR(50),"
				+ "PASSWORD VARCHAR(50),"
				+ "URL VARCHAR(50),"
				+ "ORG_ID VARCHAR(20),"
				+ "TimestampKey VARCHAR(50))";
		String createProfileTable = "CREATE TABLE PROFILES "
				+ "(ORG_ID VARCHAR(20), "
				+ "TIMESTAMP VARCHAR(30), "
				+ "PROFILE_NAME VARCHAR(1000), "
				+ "USER_COUNT VARCHAR(10), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "CREATEDBY VARCHAR(500), "
				+ "LASTMODIFIEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDBY VARCHAR(500))";
		String createTableSummaryProfile = "CREATE TABLE SUMMARYPROFILES "
				+ "(PROFILE_NAME VARCHAR(1000), "
				+ "USER_COUNT VARCHAR(10), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDDATE VARCHAR(500))";
		String createTableSfdcOrg = "CREATE TABLE SFDC_ORG "
				+ "(ORG_ID VARCHAR(30),"
				+ "TIMESTAMP VARCHAR(30), "
				+ "PROFILE_LIST VARCHAR(5000), "
				+ "OBJECT_LIST VARCHAR(32000))";
		String createTableMetadataComponents = "CREATE TABLE MetadataComponent "
				+ "(ORG_ID VARCHAR(30),"
				+ "TIMESTAMP VARCHAR(30), "
				+ "METADATATYPE VARCHAR(100), "
				+ "METADATANAME VARCHAR(500), "
				+ "FILENAME VARCHAR(500), "
				+ "ID VARCHAR(100), "
				+ "Type VARCHAR(100), "
				+ "CLASS VARCHAR(100), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "CREATEDBY VARCHAR(100), "
				+ "LASTMODIFIEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDBY VARCHAR(100))";
		String createTableSummaryMetadataComponents = "CREATE TABLE SummaryMetadataComponent "
				+ "(METADATATYPE VARCHAR(100), "
				+ "METADATANAME VARCHAR(500), "
				+ "ID VARCHAR(100), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDDATE VARCHAR(500))";
		String createTableRecordTypes = "CREATE TABLE RecordTypes (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"ObjectName VARCHAR(255),\r\n" + 
				"IsAvailable Boolean, \r\n" + 
				"IsDefaultRecordType Boolean,\r\n" + 
				"IsMaster Boolean,\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"RecordTypeID VARCHAR(30),\r\n" + 
				"LayoutName VARCHAR(80)\r\n" + 
				")\r\n";	
		String createTableSummaryRecordTypes = "CREATE TABLE SummaryRecordTypes (\r\n" + 
				"ObjectName VARCHAR(255),\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"RecordTypeID VARCHAR(30),\r\n" + 
				"LayoutName VARCHAR(80)\r\n" + 
				")\r\n";
		String createTableButtons = "Create Table Buttons (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"Name VARCHAR(255),\r\n" + 
				"Label VARCHAR(255),\r\n" + 
				"Custom Boolean,\r\n" + 
				"URL VARCHAR(255),\r\n" + 
				"Encoding VARCHAR(255),\r\n" + 
				"Content VARCHAR(255)\r\n" + 
				")";
		String createTableSummaryButtons = "Create Table SummaryButtons (\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"Name VARCHAR(255),\r\n" + 
				"Label VARCHAR(255)\r\n" + 
				")";
		String createTableFields = "Create Table Fields (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"LayoutType VARCHAR(10),\r\n" + 
				"APIName VARCHAR(255),\r\n" + 
				"Label VARCHAR(255),\r\n" + 
				"RequiredField Boolean,\r\n" + 
				"SectionHeading VARCHAR(255)\r\n" + 
				")";
		String createTableSummaryFields = "Create Table SummaryFields (\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"LayoutType VARCHAR(10),\r\n" + 
				"APIName VARCHAR(255),\r\n" + 
				"Label VARCHAR(255),\r\n" + 
				"SectionHeading VARCHAR(255)\r\n" + 
				")";
		String createTableRelatedLists = "Create Table RelatedLists (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"Name VARCHAR(255),\r\n" + 
				"Label VARCHAR(255),\r\n" + 
				"Custom Boolean,\r\n" + 
				"SortColumn VARCHAR(255),\r\n" + 
				"SortAscending Boolean,\r\n" + 
				"ElementType VARCHAR(10),\r\n" + 
				"ColumnName VARCHAR(255),\r\n" + 
				"ColumnField VARCHAR(255),\r\n" + 
				"ColumnFormat VARCHAR(255),\r\n" + 
				"ColumnLookup VARCHAR(255),\r\n" + 
				"ButtonName VARCHAR(255),\r\n" + 
				"ButtonLabel VARCHAR(255),\r\n" + 
				"ButtonCustom Boolean,\r\n" + 
				"ButtonURL VARCHAR(255),\r\n" + 
				"ButtonEncoding VARCHAR(255),\r\n" + 
				"ButtonContent VARCHAR(255)\r\n" + 
				")";	
		String createTableSummaryRelatedLists = "Create Table SummaryRelatedLists (\r\n" + 
				"LayoutID VARCHAR(30),\r\n" + 
				"Name VARCHAR(255),\r\n" + 
				"Label VARCHAR(255),\r\n" + 
				"ColumnName VARCHAR(255),\r\n" + 
				"ColumnField VARCHAR(255),\r\n" + 
				"ColumnLookup VARCHAR(255),\r\n" + 
				"ButtonName VARCHAR(255),\r\n" + 
				"ButtonLabel VARCHAR(255)\r\n" + 
				")";
		String createTableValidationRule = "Create Table ValidationRules (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"Active Boolean,\r\n" + 
				"Description VARCHAR(255),\r\n" + 
				"ErrorDisplayField VARCHAR(255),\r\n" + 
				"ErrorMessage VARCHAR(255),\r\n" + 
				"ErrorFormula VARCHAR(4000),\r\n" + 
				"ID VARCHAR(30),\r\n" + 
				"ValidationName VARCHAR(255),\r\n" + 
				"ObjectName VARCHAR(255),\r\n" +
				"CREATEDDATE VARCHAR(500),\r\n" +
				"CREATEDBY VARCHAR(100),\r\n" +
				"LASTMODIFIEDDATE VARCHAR(500),\r\n" +
				"LASTMODIFIEDBY VARCHAR(100)\r\n" +
				")";
		String createTableSummaryValidationRule = "Create Table SummaryValidationRules (\r\n" +
				"Active Boolean,\r\n" + 
				"ErrorDisplayField VARCHAR(255),\r\n" + 
				"ErrorMessage VARCHAR(255),\r\n" + 
				"ErrorFormula VARCHAR(4000),\r\n" + 
				"ID VARCHAR(30),\r\n" + 
				"ValidationName VARCHAR(255),\r\n" + 
				"ObjectName VARCHAR(255),\r\n" +
				"CREATEDDATE VARCHAR(500),\r\n" +
				"LASTMODIFIEDDATE VARCHAR(500))";
		String createTableTriggers = "Create Table Triggers (\r\n" + 
				"ORG_ID VARCHAR(30),\r\n" + 
				"TIMESTAMP VARCHAR(30),\r\n" + 
				"TriggerName VARCHAR(100),\r\n" + 
				"ObjectName VARCHAR(100),\r\n" + 
				"ApiVersion double,\r\n" + 
				"TriggerBody VARCHAR(4000),\r\n" + 
				"TriggerStatus VARCHAR(30),\r\n" + 
				"UsageAfterDelete boolean,\r\n" + 
				"UsageAfterInsert boolean,\r\n" + 
				"UsageAfterUndelete boolean,\r\n" + 
				"UsageAfterUpdate boolean, \r\n" + 
				"UsageBeforeDelete boolean, \r\n" + 
				"UsageBeforeInsert boolean, \r\n" + 
				"UsageBeforeUpdate boolean, \r\n" + 
				"UsageIsBulk boolean,\r\n" +
				"CREATEDDATE VARCHAR(500),\r\n" +
				"CREATEDBY VARCHAR(100),\r\n" +
				"LASTMODIFIEDDATE VARCHAR(500),\r\n" +
				"LASTMODIFIEDBY VARCHAR(100)\r\n" +
				")";
		String createTableSummaryTriggers = "Create Table SummaryTriggers (\r\n" + 
				"TriggerName VARCHAR(100),\r\n" + 
				"ObjectName VARCHAR(100),\r\n" + 
				"TriggerBody VARCHAR(4000),\r\n" + 
				"TriggerStatus VARCHAR(30),\r\n" + 
				"CREATEDDATE VARCHAR(500),\r\n" +
				"LASTMODIFIEDDATE VARCHAR(500))";
		String createTableWebLinks = "CREATE TABLE Weblinks "
				+ "(ORG_ID VARCHAR(30),"
				+ "TIMESTAMP VARCHAR(30), "
				+ "linkName VARCHAR(500), "
				+ "linkDescription VARCHAR(2500), "
				+ "linkDisplayType VARCHAR(500), "
				+ "linkEncodingKey VARCHAR(500), "
				+ "hasMenuBar VARCHAR(500), "
				+ "hasScrollBar VARCHAR(500), "
				+ "hasToolBar VARCHAR(500), "
				+ "linkType VARCHAR(500), "
				+ "masterLabel VARCHAR(500), "
				+ "openType VARCHAR(500), "
				+ "URL VARCHAR(500), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "CREATEDBY VARCHAR(100), "
				+ "LASTMODIFIEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDBY VARCHAR(100))";
		String createTableSummaryWeblinks = "CREATE TABLE SummaryWeblinks "
				+ "(linkName VARCHAR(500), "
				+ "linkDescription VARCHAR(2500), "
				+ "masterLabel VARCHAR(500), "
				+ "URL VARCHAR(500), "
				+ "CREATEDDATE VARCHAR(500), "
				+ "LASTMODIFIEDDATE VARCHAR(500))";
		String createTableIA_TO_DOTNEXT_MAPPING = "CREATE TABLE IA_TO_DOTNEXT_MAPPING "
				+ "(ORG_ID VARCHAR(30),"
				+ "TIMESTAMP VARCHAR(30),"
				+ "OBJECTNAME VARCHAR(200),"
				+ "LAYOUTID VARCHAR(500)," 
				+ "LAYOUTNAME VARCHAR(500),"
				+ "BUTTONS VARCHAR(5000),"
				+ "FIELDS VARCHAR(5000),"
				+ "RELATEDLISTS VARCHAR(5000),"
				+ "RELATEDLISTSCOLUMNS VARCHAR(5000)," 
				+ "RELATEDLISTSBUTTONS VARCHAR(5000),"
				+ "RECORDTYPES VARCHAR(5000),"
				+ "TRIGGERS VARCHAR(5000),"
				+ "VALIDATIONRULES VARCHAR(5000))";
		// Collect all queries in an ArrayList
		queryList.add(createPrefTable);
		queryList.add(createSFDCTable);
		queryList.add(createUsersTable);
		queryList.add(createProfileTable);
		queryList.add(createTableSummaryProfile);
		queryList.add(createTableSfdcOrg);
		queryList.add(createTableMetadataComponents);
		queryList.add(createTableSummaryMetadataComponents);
		queryList.add(createTableRecordTypes);
		queryList.add(createTableSummaryRecordTypes);
		queryList.add(createTableButtons);
		queryList.add(createTableSummaryButtons);
		queryList.add(createTableFields);
		queryList.add(createTableSummaryFields);
		queryList.add(createTableRelatedLists);
		queryList.add(createTableSummaryRelatedLists);
		queryList.add(createTableValidationRule);
		queryList.add(createTableSummaryValidationRule);
		queryList.add(createTableTriggers);
		queryList.add(createTableSummaryTriggers);
		queryList.add(createTableWebLinks);
		queryList.add(createTableSummaryWeblinks);
		queryList.add(createTableIA_TO_DOTNEXT_MAPPING);	
		
		try {
			if(conn1.isValid(3)) {				
				for(String query : queryList) {
					System.out.println("Create Table Query :"+query);
					if(query != null) {						
						try {        			
							PreparedStatement preparedStatement = conn1.prepareStatement(query);							
							preparedStatement.executeUpdate();  
							logger.log(Level.INFO, "Query successful :: \n" + query );
						} catch (SQLException e) {	
							e.printStackTrace();
							logger.log(Level.WARNING, "Query failed :: \n" + query );							
						}
					}					
				}				
			}
			else {
				logger.log(Level.WARNING, "Database connection timed out");				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.INFO, "Create table function execution Unsuccessful.");
		}
		finally {
			logger.log(Level.INFO, "Create table function execution complete.");
		}
	}
	
	/******************************************************************************************************************************************************
	 * Get a list of tables in SFDB  
	 * @author Parantap Samajdar
	 * @throws SQLException 
	 ******************************************************************************************************************************************************/
	private void checkTables() throws SQLException {		
		try {
			DatabaseMetaData meta = conn1.getMetaData();
			ResultSet res;
			res = meta.getTables(null, null, null, new String[] {"TABLE"});
			String returnTableName = "";
			if (!res.next()) { //This condition is for first time installation process
				logger.log(Level.INFO, "Preparing DB to create all SFDC related tables.");
				this.createTable();
				//@author : Debdatta Porya
				//Purpose : Adding new tables to SFDC Component Table
				ResultSet resLatest = meta.getTables(null, null, null, new String[] {"TABLE"});
				while(resLatest.next()) {
					this.insertSFDCCompToolsTable(resLatest.getString("TABLE_NAME"), resLatest.getString("TABLE_TYPE"));
					logger.log(Level.INFO, "Adding into SFDC Component Table : "+ res.getString("TABLE_NAME") + ", "+res.getString("TABLE_TYPE"));
				}
			//@author : Debdatta Porya
			//Purpose : to verify if any new table need to be added
			}else {
				logger.log(Level.INFO, "List of tables : "); 
				while (res.next()) {
						logger.log(Level.INFO, res.getString("TABLE_NAME") + ", "+res.getString("TABLE_TYPE"));	
					}
			}			
			res.close();
		} catch (SQLSyntaxErrorException e) {			
			logger.log(Level.WARNING, "Error reading / creating tables in the database.");
			e.printStackTrace();
		}		  
	}
	
	/******************************************************************************************************************************************************
	 * Close database connection
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void closeDB() {
		try {
			conn1.close();
		} catch (SQLException e) {			
			logger.log(Level.WARNING, "Database connection not terminated correctly.");
			e.printStackTrace();
		}
	}
	
	/******************************************************************************************************************************************************
	 * Private functions for database setup for first time use
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/	
	private void createSFDB() {
	//Create database structure on client machine for first time use
		
	}

	

	
	
	
}

