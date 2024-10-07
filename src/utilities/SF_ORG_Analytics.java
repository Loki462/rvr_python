package utilities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sforce.ws.ConnectionException;

import retriever.MetadataRetriever;
import retriever.PartnerRetriever;
import retriever.ToolingRetriever;

public class SF_ORG_Analytics {	
	
	private static String login_ID, Password, URL;	

	private MetadataRetriever sfMetadata;
	private PartnerRetriever sfPartner;
	private ToolingRetriever sfTooling;
	private static final Logger logger = Logger.getLogger(SF_ORG_Analytics.class.getName());
	public static String OrgID = null;	
	public static ArrayList<String> ProfileList = null;	
	public static int ProfileCount = 0;	
	public static StringBuffer AppList = null;
	public static ArrayList<String> ObjectList = null;
	public static int ObjectCount = 0;
	public static ArrayList<Observer> observers = new ArrayList<Observer>();
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	private static DateUtil currentDateTime = new DateUtil();
	public static String timeStamp = currentDateTime.getCurrentDateTime();
	PreferenceCollection prefCol = new PreferenceCollection();
				
	/**
	 * Constructor
	 * @param id
	 * @param pw
	 * @param url
	 */
	public SF_ORG_Analytics(String id, String pw, String url) {
		login_ID = id; 
		Password = pw;
		URL = url;
		
		try {
			logger.log(Level.INFO,"Starting SFDC Org Analysis ...");
			long startTime = System.nanoTime();
			sfMetadata = new MetadataRetriever(login_ID, Password, URL);
			sfPartner = new PartnerRetriever(login_ID, Password, URL);
			sfTooling = new ToolingRetriever(login_ID, Password);
			OrgID = sfTooling.getOrgId();
			//prefCol.setOrgId(OrgID);			
			//prefCol.setTimeStamp(timeStamp);
			jdbc.insertPreferenceCollection(OrgID,timeStamp);
			
			if(sfMetadata.listMetadataObjects()) {
				logger.log(Level.INFO, "Metadata information updated for OrgID - " + OrgID + " at " + jdbc.getTimeStamp() + ".");
			} else {
				logger.log(Level.INFO, "Error updating metadata information.");
			}
			
			sfMetadata.getApexComponentDetail();
			sfPartner.getObjectsDetail();
			ProfileList = listProfiles();
			updateUserCount(ProfileList);
			//ProfileCount = getProfileCount();
			AppList = listApps();
			ObjectList = getObjectsList();
			ObjectCount = getObjectsCount();
			sfTooling.getValidationRules();
			sfTooling.getApexTrigger();
			sfTooling.getWebLinkDetails();
			Long endTime = System.nanoTime();

			jdbc.updateSFDCORGTable(OrgID,timeStamp,ProfileList,ObjectList);
			logger.log(Level.INFO, "Time taken to execute SFDC Org Analysis : " + (endTime - startTime)/1000000000 + " Second.");			
		} catch (ConnectionException | SQLException e) {			
			e.printStackTrace();
			logger.log(Level.SEVERE,"Failed to retrieve SFDC org information");
		}
		
	}
			 
	/*******************************************************
	 * @author Parantap Samajdar
	 * Return count of users in a specific profile
	 * @param Salesforce profile
	 * @return list of users assigned to passed profile
	 * @throws ConnectionException
	 */
	 public int getUserCount(String profile) throws ConnectionException {
			return sfPartner.getUsersCount(profile);		
	 }
	 /*****************************************************/
	
	 /*******************************************************
	  * @author Parantap Samajdar
	  * Return list of users in a specific profile
	  * @param Salesforce profile
	  * @return list of users assigned to passed profile
	  * @throws ConnectionException
	  */  
	  public StringBuffer getUsersList(String Profile) throws ConnectionException {
	    return sfPartner.getUsersList(Profile);		
	  }	
	  /*****************************************************/
		
	  /*******************************************************
	   * @author Parantap Samajdar
	   * Return list of profiles used in Salesforce org
	   * @param Salesforce profile
	   * @return list of users assigned to passed profile
	   * @throws ConnectionException
	   */
	  public ArrayList<String> listProfiles() {		
		try {
			ArrayList<String> ProfileList = sfMetadata.listProfiles();
			System.out.println("Profile List:"+ProfileList);
			if(ProfileList == null) {
				logger.log(Level.WARNING, "Error updating profile list.");				
			} else {
				logger.log(Level.INFO, "Profile list updated successfully.");
			}
			return ProfileList;
		} catch(Exception e) {			
			logger.log(Level.FINEST, e.getMessage());
			return null;
		}	  
	  }
	  /*****************************************************/
	
	// Return list of custom apps in Salesforce -- not ready
	public StringBuffer listApps() {	
		try {			
			StringBuffer sb = sfMetadata.listApps();			
			return sb;
		} catch(Exception e) {			
			logger.log(Level.SEVERE, e.getMessage());
			return null;
		}		
	}
	
	// Return count of profiles being used in Salesforce org
	public int getProfileCount() {		
		ArrayList<String> sb = sfMetadata.listProfiles();	
		return sb.size();		
	}
		
 	/**************************************************************
	 * @author Parantap Samajdar
	 * Update user count for all profiles in database
	 * @param Salesforce profile
	 * @return count of users assigned to passed profile
	 * @throws ConnectionException
	 **************************************************************/
	public void updateUserCount(ArrayList<String> ProfileList) throws ConnectionException {
			 for(String strProfile : ProfileList) {
				 String userCount = Integer.toString(sfPartner.getUsersCount(strProfile));
				 jdbc.updateProfileUserCount(jdbc.getOrgID(), jdbc.getTimeStamp(), strProfile, userCount);
			 }			
		 }
	/**************************************************************/ 
	// Return list of objects used in Salesforce org	
	public ArrayList<String> getObjectsList() throws ConnectionException {		
		ArrayList<String> sb = new ArrayList<String>();
		sb = sfPartner.getObjectsList();			
		return sb;
	}	
	
	// Return count of objects used in Salesforce org
	public int getObjectsCount() throws ConnectionException {		
		ArrayList<String> sb = new ArrayList<String>();
		sb = sfPartner.getObjectsList();			
		return sb.size();
	}	
	
	// Get ORG ID
	public String getOrgID() throws ConnectionException {
		String strLoggedInUserOrgID = sfTooling.getOrgId();
		//jdbc.UserTable(login_ID, Password, URL, strLoggedInUserOrgID);
		return strLoggedInUserOrgID;
	}

	// Return a list of custom objects
	public StringBuffer listCustomObjects() throws ConnectionException {
		return sfMetadata.listCustomObjects();		
	}
	
	
	//Store org metadata in XMLformat
	private void saveMetadata() {
		
	}
		
	// Attach observers to monitor state
	
	public void attach(Observer observer) {
		observers.add(observer);	
	}
	// Update observers to monitor state
	public void notifyAllObservers() {
		for(Observer observer : observers) {
			observer.update(null, observer);
		}
	}
	
	
}
