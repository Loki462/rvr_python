/**
 * 
 */
package utilities;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * @author Parantap Samajdar
 *
 */
public class PreferenceCollection {
	
	private static final String orgIDKey = "OrgID";
	private static final String TimestampKey = "Timestamp";
	Preferences pref = Preferences.systemNodeForPackage(PreferenceCollection.class);
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	
	public void setOrgId(String orgID) {
		try {		
			pref.put(orgIDKey, orgID);			
		} catch(Exception e) {
			e.printStackTrace();			
		}		
	}
	
	public void setTimeStamp(String timeStamp) {		
		try {			
			pref.put(TimestampKey, timeStamp);
		} catch(Exception e) {
			e.printStackTrace();			
		}		
	}	

	
	public String getOrgID() {
		return pref.get(orgIDKey, "Default");
	}
	
	public String getTimeStamp() {
		return pref.get(TimestampKey, "01-01-01-01-01-01");		
	}
	
}
