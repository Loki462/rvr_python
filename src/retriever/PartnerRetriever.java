package retriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sforce.soap.enterprise.DescribeAppMenuResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.metadata.ProfileFieldLevelSecurity;
import com.sforce.soap.metadata.QuickActionList;
import com.sforce.soap.metadata.RecordType;
import com.sforce.soap.partner.ChildRelationship;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DescribeLayout;
import com.sforce.soap.partner.DescribeLayoutButton;
import com.sforce.soap.partner.DescribeLayoutButtonSection;
import com.sforce.soap.partner.DescribeLayoutComponent;
import com.sforce.soap.partner.DescribeLayoutItem;
import com.sforce.soap.partner.DescribeLayoutResult;
import com.sforce.soap.partner.DescribeLayoutRow;
import com.sforce.soap.partner.DescribeLayoutSection;
import com.sforce.soap.partner.DescribeQuickActionListItemResult;
import com.sforce.soap.partner.DescribeQuickActionListResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.DescribeTab;
import com.sforce.soap.partner.DescribeTabSetResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.GetUserInfo_element;
import com.sforce.soap.partner.LayoutComponentType;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.PicklistEntry;
import com.sforce.soap.partner.PicklistForRecordType;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.RecordTypeInfo;
import com.sforce.soap.partner.RecordTypeMapping;
import com.sforce.soap.partner.RelatedList;
import com.sforce.soap.partner.RelatedListColumn;
import com.sforce.soap.partner.RelatedListSort;
import com.sforce.soap.partner.ShareAccessLevel;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.wsdl.Schema;

import utilities.HTML_Generator;
import utilities.JavaDBManager;
import utilities.PreferenceCollection;
import utilities.XML_Generator;

public class PartnerRetriever {
	
	private String UserID, Password, URL;
	private PartnerConnection connection = null;
	XML_Generator tmpObjectFile = new XML_Generator();
	private Logger logger = Logger.getLogger(PartnerRetriever.class.getCanonicalName());
	private PreferenceCollection prefCol = new PreferenceCollection();
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
    
	
	/*****************************************************************************************************************************************************                
	 * Constructor
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public PartnerRetriever(String username, String password, String loginUrl) throws ConnectionException {
    	UserID = username;
    	Password = password;
    	URL = loginUrl;
    	ConnectorConfig config = new ConnectorConfig();
		config.setUsername(UserID);
		config.setPassword(Password);		
		connection = Connector.newConnection(config);	
		
    }
	
	/******************************************************************************************************************************************************
     * Return organization id for the logged in user 
     * @author Parantap Samajdar      
     ******************************************************************************************************************************************************/
    public String getOrgID() throws ConnectionException {
    	GetUserInfoResult result = connection.getUserInfo();
    	return result.getOrganizationId();
    }

	
	/******************************************************************************************************************************************************
	 * Get count of users 
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public int getUsersCount(String Profile) {    	
		
		try	{				
				String query = "SELECT name, profile.Name from user where profile.Name like '%" + Profile + "%'"; //Add clause to filter out inactive users
				QueryResult result = connection.query(query);				
				jdbc.updateProfileUserCount(jdbc.getOrgID(), jdbc.getTimeStamp(), Profile, Integer.toString(result.getSize()));
				return result.getSize();					
			}
		catch(Exception ex)
		{
			logger.log(Level.WARNING, "Error retrieving user count");
			return 0;
		}    	
    }
	
	/******************************************************************************************************************************************************
	 * Get object permission for a profile 
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	public void getProfileFieldLevelSecurity(String ObjectName) {    	
		
		try	{				
				String query = "SELECT Id, Parent.label, SobjectType, PermissionsRead, Parent.PermissionsModifyAllData, ParentId FROM ObjectPermissions	WHERE PermissionsRead = true and SobjectType = " + ObjectName;
				System.out.println(query);
				QueryResult result = connection.query(query);
				System.out.println(result);
			}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}    	
    }
	
	/******************************************************************************************************************************************************                
	 * Get active users list
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
    public StringBuffer getUsersList(String Profile) { 		
    	
    	StringBuffer sb = new StringBuffer();
    	
		try
		{			
			String query = "SELECT name, profile.Name from user where profile.Name like '%" + Profile + "%'";
			QueryResult result = connection.query(query);
			if(result.getSize()>0)
			{
				boolean done=false;				
				while(!done)
				{					
					for (SObject record : result.getRecords()) 
					{
						sb.append((String)record.getField("Name"));
						sb.append(",");
						sb.append((String)record.getField("Profile.Name"));
						sb.append(";");
					}
					if (result.isDone()) {
						done = true;
					} else {
						result = connection.queryMore(result.getQueryLocator());
					}
				}
			}			
			return sb;
		}
		catch(Exception ex)
		{	
			ex.printStackTrace();
			logger.log(Level.WARNING, "Error retrieving users list");
			return null;
		}
    	
    }
    
    /******************************************************************************************************************************************************                
	 * Get Organization Information
	 * @author Debdatta Porya
	 ******************************************************************************************************************************************************/
    public ArrayList<String> getOrganizationInformation(String OrgID) { 		
    	
    	//StringBuffer sb = new StringBuffer();
    	ArrayList<String> sb = new ArrayList<String>();
		try
		{			
			String query = "SELECT Id,InstanceName,IsSandbox,Name,OrganizationType,PrimaryContact from Organization where Id = '" + OrgID + "'";
			QueryResult result = connection.query(query);
			if(result.getSize()>0)
			{
				boolean done=false;				
				while(!done)
				{					
					for (SObject record : result.getRecords()) 
					{
						sb.add((String)record.getField("Id"));
						sb.add((String)record.getField("InstanceName"));
						sb.add((String)record.getField("IsSandbox"));
						sb.add((String)record.getField("Name"));
						sb.add((String)record.getField("OrganizationType"));
						sb.add((String)record.getField("PrimaryContact"));
					}
					if (result.isDone()) {
						done = true;
					} else {
						result = connection.queryMore(result.getQueryLocator());
					}
				}
			}			
			return sb;
		}
		catch(Exception ex)
		{	
			ex.printStackTrace();
			logger.log(Level.WARNING, "Error retrieving users list");
			return null;
		}
    	
    }
    
           
    /******************************************************************************************************************************************************
     *  Get a list of Objects used in salesforce org 
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public ArrayList<String> getObjectsList() {
    	ArrayList<String> sb = new ArrayList<String>();
    	
		try {		
				com.sforce.soap.partner.DescribeGlobalResult describeGlobalResult = connection.describeGlobal();	        
				com.sforce.soap.partner.DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();	        
	        
	        for (int i = 0; i < sobjectResults.length; i++) {
	          sb.add(sobjectResults[i].getName());
	          //getProfileFieldLevelSecurity(sobjectResults[i].getName());
	        }
	        System.out.println("Method : getObjectsList - length - "+sb.size());
	        return sb;
	    } catch (ConnectionException ce) {
	    	logger.log(Level.WARNING, "Error retrieving objects list");
	        return null;
	    }
	}
    
    /******************************************************************************************************************************************************
     *  Get count of Objects used in salesforce org 
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public int getObjectsCount() {
    	try {			
				com.sforce.soap.partner.DescribeGlobalResult describeGlobalResult = connection.describeGlobal();	        
				com.sforce.soap.partner.DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();	        
				return sobjectResults.length;	        
	    } catch (ConnectionException ce) {
	    	logger.log(Level.WARNING, "Error retrieving object count");
	        return 0;
	    }
	}
    
    /******************************************************************************************************************************************************
     *  Get a list of Objects & record types used in salesforce org 
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public StringBuffer getObjectsDetail_Deprecated() { 
    	
    	long startTime, endTime;
    	StringBuffer sb = new StringBuffer();    	
    	
    	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	sb.append(System.lineSeparator());  
    	
		try {		
			startTime = System.nanoTime();
			com.sforce.soap.partner.DescribeGlobalResult describeGlobalResult = connection.describeGlobal();	        
			com.sforce.soap.partner.DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();			
			sb.append("<SFDC_Objects>");
			sb.append(System.lineSeparator());
	        for (com.sforce.soap.partner.DescribeGlobalSObjectResult sobjectResult : sobjectResults) {
	        	DescribeLayoutResult dlr = null;
	        	DescribeSObjectResult describeSobjectResult = connection.describeSObject(sobjectResult.getName());
	        	
	        	sb.append("\t<Object Name=\"" + describeSobjectResult.getName() + "\" Label=\"" + describeSobjectResult.getLabel() + "\" Custom=\"" + describeSobjectResult.getCustom() + "\">");	        	
	        	sb.append(System.lineSeparator());
	        	RecordTypeInfo[] RecordTypesList = describeSobjectResult.getRecordTypeInfos();	   
	        	try {
	        		dlr = connection.describeLayout(describeSobjectResult.getName(), null, null);
	        	} catch(Exception e) {
	        		//Go to next iteration in for loop in case describe layout is not supported 
	        		sb.append("\t</Object>");	  
		        	sb.append(System.lineSeparator());
	        		continue;
	        	}
	        	
	        	RecordTypeMapping[] recordTypeMappings = dlr.getRecordTypeMappings();
	        	DescribeLayout[] layouts = dlr.getLayouts();
	        	for(DescribeLayout layout : layouts) {
	        		
	        		DescribeLayoutButton[] ButtonList;
	        		try {	        			
	        			ButtonList = layout.getButtonLayoutSection().getDetailButtons();
	        			for(DescribeLayoutButton button : ButtonList) {
	        	        	sb.append("\t\t\t<Button Name=\"" + button.getName() + "\" Label=\"" + button.getLabel() + "\" Custom=\"" + button.getCustom() + "\" Behaviour=\"" + button.getBehavior() + "\" URL=\"" + button.getUrl() + "\">");
	        	        	sb.append("</Button>");
	            	        sb.append(System.lineSeparator());    	        	
	        	        }
	        		} catch (Exception ex) {
	        			sb.append("\t</Object>");	  
	    	        	sb.append(System.lineSeparator());
	        			continue;
	        		}
    	        	
	        		
    	        	DescribeLayoutSection[] detailLayoutSectionList = layout.getDetailLayoutSections();	        	         
        	        for(DescribeLayoutSection detailLayoutSection : detailLayoutSectionList) {
        	          sb.append("\t\t\t<Detail_Layout__Section_Heading>"+ detailLayoutSection.getHeading() + "</Detail_Layout__Section_Heading>");
        	          sb.append(System.lineSeparator());
        	          DescribeLayoutRow[] describeLayoutRows = detailLayoutSection.getLayoutRows();
        	          for(DescribeLayoutRow describeLayoutRow : describeLayoutRows) {
        	        	  DescribeLayoutItem[] describeLayoutItems = describeLayoutRow.getLayoutItems();
        	        	  for(DescribeLayoutItem describeLayoutItem : describeLayoutItems) {
        	        		  sb.append("\t\t\t\t<Label>" + describeLayoutItem.getLabel() + "</Label>");
        	        		  sb.append(System.lineSeparator());
        	        	  }
        	          }        	          
        	        }
        	        
        	        RelatedList[] relatedLists = layout.getRelatedLists();
        	        for(RelatedList relatedList : relatedLists) {
        	        	sb.append("\t\t\t<RelatedList Name=\"" + relatedList.getName() + "\" Label=\"" + relatedList.getLabel() + "\" Custom=\"" + relatedList.getCustom() + "\" Field=\"" + relatedList.getField() + "\" AccessLevelRequired=\"" + relatedList.getAccessLevelRequiredForCreate() + "\">");
        	        	sb.append(System.lineSeparator());
        	        	DescribeLayoutButton[] buttons = relatedList.getButtons();
        	        	for(DescribeLayoutButton button : buttons) {
        	        		sb.append("\t\t\t\t<Button Name=\"" + button.getName() + "\" Label=\"" + button.getLabel() + "\" Custom=\"" + button.getCustom() + "\" Behaviour=\"" + button.getBehavior() + "\" URL=\"" + button.getUrl() + "\">");
            	        	sb.append("</Button>");
                	        sb.append(System.lineSeparator());
        	        	}
        	        	
        	        	RelatedListColumn[] relatedListColumns = relatedList.getColumns();
        	        	for(RelatedListColumn rlc : relatedListColumns) {
        	        		sb.append("\t\t\t\t<Column Name=\"" + rlc.getName() + "\" Label=\"" + rlc.getLabel() + "\" Field=\"" + rlc.getField() + "\" Format=\"" + rlc.getFormat() + "\" LookupID=\"" + rlc.getLookupId() + "\">");
        	        		sb.append("</Column>");
                	        sb.append(System.lineSeparator());
        	        	}    	        	
        	        	
        	        	sb.append("\t\t\t</RelatedList>");
            	        sb.append(System.lineSeparator());
        	        }
        	        
        	        try {
        	        	DescribeQuickActionListResult quickActionList = layout.getQuickActionList();
	        	        DescribeQuickActionListItemResult[] quickActionListItems = quickActionList.getQuickActionListItems();
	        	        if(quickActionListItems.length > 0) {    	        
	    	    	        for(DescribeQuickActionListItemResult quickActionListItem : quickActionListItems) {
	    	    	        	sb.append("\t\t\t\t<QuickAction Name=\"" + quickActionListItem.getQuickActionName() + "\" Label=\"" + quickActionListItem.getLabel() + "\" IconURL=\"" + quickActionListItem.getIconUrl() + "\" Type=\"" + quickActionListItem.getType() + "\" MiniIconURL=\"" + quickActionListItem.getMiniIconUrl() + "\">");
	    		        		sb.append("</QuickAction>");
	    	        	        sb.append(System.lineSeparator());    	        	
	    	    	        }
	        	        } 
        	        } catch (Exception e) {
        	        	sb.append("\t</Object>");	  
        	        	sb.append(System.lineSeparator());
        	        	continue;
        	        }
        	        
        	        
        	        DescribeLayoutSection[] editLayoutSectionList = layout.getEditLayoutSections();
        	        // Write the headings of the edit layout sections 
        	        for(DescribeLayoutSection editLayoutSection : editLayoutSectionList) {  
        	          sb.append("\t\t\t<Edit_Layout__Section_Heading>" + editLayoutSection.getHeading() + "</Edit_Layout__Section_Heading>");
        	          sb.append(System.lineSeparator());
        	          // For each edit layout section, get its details.
          	        	for(int k = 0; k < editLayoutSectionList.length; k++) {
	          	        	DescribeLayoutSection els = editLayoutSection;    
	          	        	sb.append("\t\t\t\t\t\t<Edit_Layout_Section_Heading_Name>" + els.getHeading() + "></Edit_Layout_Section_Heading_Name>");
	          	        	sb.append(System.lineSeparator());
	          	        	DescribeLayoutRow[] dlrList = els.getLayoutRows();        	        	
	          	            for(int m = 0; m < dlrList.length; m++) {
	          	            	DescribeLayoutRow lr = dlrList[m];        	            	
	          	            	DescribeLayoutItem[] dliList = lr.getLayoutItems();
	          	            	for(int n = 0; n < dliList.length; n++) {
	          	            		DescribeLayoutItem li = dliList[n];
	          	            		if ((li.getLayoutComponents() != null) && (li.getLayoutComponents().length > 0)) {        	            			
	          	            			sb.append("\t\t\t\t\t\t<" + li.getLayoutComponents()[0].getType() + ">" + li.getLayoutComponents()[0].getValue() + "</" + li.getLayoutComponents()[0].getType() + ">");
	          	            			sb.append(System.lineSeparator());	          	            			
	          	            		}	          	            		                    
	          	            	}
	          	            }
	          	        }
        	        }
	        	}
	        	
	        	////////////////////////////////////////////////////////////////////////////////////////////
	        	for(RecordTypeInfo RecordTypes : RecordTypesList) {
		        	sb.append("\t\t<RecordType Name=\"" + RecordTypes.getName() + "\""+ "Available=\""+ RecordTypes.getAvailable() + "\" id=\"" + RecordTypes.getRecordTypeId() + "\">");		        	
	        		sb.append(System.lineSeparator());	        		
	        		//Find picklist values in the record type    	         
	    	        for(RecordTypeMapping recordTypeMapping : recordTypeMappings) {	    	        	
	    	        	PicklistForRecordType[] pickListForRecordTypes = recordTypeMapping.getPicklistsForRecordType();
	    	        	for(PicklistForRecordType pickListForRecordType : pickListForRecordTypes) {
	    	        		sb.append("\t\t\t\t<Picklist Name=\"" + pickListForRecordType.getPicklistName() + "\" ");	    	        		
	    	        		PicklistEntry[] pickListValues = pickListForRecordType.getPicklistValues();
	    	        		String pickListValueCollection = "";
	    	        		for(PicklistEntry pickListValue : pickListValues) {
	    	        			pickListValueCollection = pickListValueCollection + pickListValue.getLabel() + ";";
	    	        		}
	    	        		sb.append("Item_List=\"" + pickListValueCollection + "\">");    	        			
	    	        		sb.append("\t\t\t\t</Picklist>");
	    	        		sb.append(System.lineSeparator());
	    	        	}    	        	
	    	        }
	        		sb.append("\t\t</Record_Type>");
	        		sb.append(System.lineSeparator());	        		
	        	}
        		sb.append("\t</Object>");	  
	        	sb.append(System.lineSeparator());
	        }
	        sb.append("</SFDC_Objects>");
	        endTime = System.nanoTime();	        	    	
	        logger.log(Level.INFO, "Object detail capture complete. Total time taken : " + (endTime - startTime)/1000000000 + " Second.");
	        //System.out.println(sb);	        
	        return sb;
	    } catch (ConnectionException ce) {
	    	//logger.log(Level.INFO, "PartnerRetriever class :: getObjectsDetail function :: " + ce.getMessage());
	    	ce.printStackTrace();
	        return null;
	    } 	
	}
    
    /******************************************************************************************************************************************************
     *  Get a list of Objects & record types used in salesforce org - fresh implementation
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public void getObjectsDetail() {
    	
    	long startTime, endTime;
    	DescribeLayoutResult dlr;
    	
		try {		
			startTime = System.nanoTime();
			com.sforce.soap.partner.DescribeGlobalResult describeGlobalResult = connection.describeGlobal();	        
			com.sforce.soap.partner.DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();	
			for(com.sforce.soap.partner.DescribeGlobalSObjectResult sobjectResult : sobjectResults) {
				//DescribeSObjectResult describeSObjectResult = connection.describeSObject(sobjectResult.getName());
				try {
					dlr = connection.describeLayout(sobjectResult.getName(), null, null);
				} catch(Exception e) {
					jdbc.insertRecordTypes(jdbc.getOrgID(), jdbc.getTimeStamp(), sobjectResult.getName(), "NA", false, false, false, "NA", "NA"); //Enter blank row for object 
					//logger.log(Level.INFO, "Object " + sobjectResult.getName() + " is not supported in DescribeLayout call.");
					continue;
				}						
				
				RecordTypeMapping[] recordTypeMappings = dlr.getRecordTypeMappings();
				for(RecordTypeMapping rtm : recordTypeMappings) {				
					String recordTypeName = rtm.getName();
					boolean isAvailable = rtm.isAvailable();
					boolean isDefaultRecordType = rtm.isDefaultRecordTypeMapping();
					boolean isMaster = rtm.isMaster();
					String layoutID = rtm.getLayoutId();
					String recordTypeID = rtm.getRecordTypeId();	
				
					jdbc.insertRecordTypes(jdbc.getOrgID(), jdbc.getTimeStamp(), sobjectResult.getName(), recordTypeName, isAvailable, isDefaultRecordType, isMaster, layoutID, recordTypeID);
				}
				
	        	DescribeLayout[] layouts = dlr.getLayouts();
	        	for(DescribeLayout layout : layouts) {	        		
	        		DescribeLayoutButtonSection buttonLayout = layout.getButtonLayoutSection();
	        		String LayoutID = layout.getId();	  
	        		DescribeLayoutButton[] detailButtons;
	        		
	        		// Get buttons detail
	        		try {
	        			detailButtons = buttonLayout.getDetailButtons();
	        		} catch(Exception e) {	        			
	        			continue;
	        		}
	        		        		
	        		for(DescribeLayoutButton detailButton : detailButtons) {        			
	        			String Name = detailButton.getName();
	        			
	        			if(Name.length() > 255) {
	        				Name = Name.substring(0,254);
	        			}
	        			String Label = detailButton.getLabel();
	        			if(Label.length() > 255) {
	        				Label = Label.substring(0,254);
	        			}
	        			boolean Custom = detailButton.getCustom();
	        			String strURL = detailButton.getUrl();
	        			if(strURL != null && strURL.length() > 255) {
	        				strURL = strURL.substring(0,254);
	        			}
	        			String Encoding = detailButton.getEncoding();
	        			if(Encoding != null && Encoding.length() > 255) {
	        				Encoding = Encoding.substring(0,254);
	        			}
	        			String Content = detailButton.getContent();
	        			if(Content != null && Content.length() > 255) {
	        				Content = Content.substring(0,254);
	        			}
	        			jdbc.insertButtons(jdbc.getOrgID(), jdbc.getTimeStamp(), LayoutID, Name, Label, Custom, strURL, Encoding, Content);
	        		}
	        		
	        		DescribeLayoutSection[] detailLayoutSectionList = layout.getDetailLayoutSections();	        	         
	    	        for(DescribeLayoutSection detailLayoutSection : detailLayoutSectionList) {
	    	          boolean IsRequired;	
	    	          String Label = new String();
	    	          String API_Name = new String();    	          
	    	          String SectionHeading = detailLayoutSection.getHeading();
	    	          
	    	          DescribeLayoutRow[] describeLayoutRows = detailLayoutSection.getLayoutRows();
	    	          for(DescribeLayoutRow describeLayoutRow : describeLayoutRows) {
	    	        	  DescribeLayoutItem[] describeLayoutItems = describeLayoutRow.getLayoutItems();
	    	        	  for(DescribeLayoutItem describeLayoutItem : describeLayoutItems) {
	    	        		  Label = describeLayoutItem.getLabel();
	    	        		  IsRequired = describeLayoutItem.isRequired();
	    	        		  DescribeLayoutComponent[] describeLayoutComponents = describeLayoutItem.getLayoutComponents();    	        		  
	    	        		  for(DescribeLayoutComponent describeLayoutComponent : describeLayoutComponents) {
	    	        			  API_Name = describeLayoutComponent.getValue();    	        			      	        			  
	    	        		  }
	    	        	  jdbc.insertFields(jdbc.getOrgID(), jdbc.getTimeStamp(), LayoutID, "Detail", API_Name, Label, IsRequired, SectionHeading);	  
	    	        	  }
	    	          }    	          
	    	        }
	    	        
	    	        DescribeLayoutSection[] editLayoutSectionList = layout.getEditLayoutSections();    	         
	    	        for(DescribeLayoutSection editLayoutSection : editLayoutSectionList) {
	    	        	String Label = new String();    	        	
	    	        	String Value = new String();
	    	        	String API_Name = new String();
	    	        	boolean IsRequired;
	      	        	for(int k = 0; k < editLayoutSectionList.length; k++) {
	          	        	DescribeLayoutSection els = editLayoutSection;    
	          	        	String SectionHeading = els.getHeading();          	        	 
	          	        	DescribeLayoutRow[] dlrList = els.getLayoutRows();        	        	
	          	            for(int m = 0; m < dlrList.length; m++) {
	          	            	DescribeLayoutRow lr = dlrList[m];        	            	
	          	            	DescribeLayoutItem[] dliList = lr.getLayoutItems();
	          	            	for(int n = 0; n < dliList.length; n++) {
	          	            		DescribeLayoutItem li = dliList[n];
	          	            		if ((li.getLayoutComponents() != null) && (li.getLayoutComponents().length > 0)) {        	            			
	          	            			Value = li.getLayoutComponents()[0].getValue();
	          	            			IsRequired = li.isRequired();
	          	            			Label = li.getLabel();
	          	            			DescribeLayoutComponent[] describeLayoutComponents = li.getLayoutComponents();
	          	            			for(DescribeLayoutComponent describeLayoutComponent : describeLayoutComponents) {          	            				
	              	        			  API_Name = describeLayoutComponent.getValue();  
	              	        			  jdbc.insertFields(jdbc.getOrgID(), jdbc.getTimeStamp(), LayoutID, "Edit", API_Name, Label, IsRequired, SectionHeading);
	              	        		  }
	          	            		}	          	            		                    
	          	            	}
	          	            }
	          	        }
	    	        }
	    	        	    	         
	    	        String Name = new String(); 
	    	        String Label = new String(); 
	    	        boolean Custom = false; 
	    	        String SortColumn = new String(); 
	    	        boolean SortAscending = false; 
	    	        String ElementType = new String(); 
	    	        String ColumnName = new String(); 
	    	        String ColumnField = new String(); 
	    	        String ColumnFormat = new String(); 
	    	        String ColumnLookup = new String(); 
	    	        String ButtonName = new String(); 
	    	        String ButtonLabel = new String(); 
	    	        boolean ButtonCustom; 
	    	        String ButtonURL = new String();
	    	        String ButtonEncoding = new String(); 
	    	        String ButtonContent = new String();
	    	        
	    	        RelatedList[] relatedLists = layout.getRelatedLists();
	    	        for(RelatedList relatedList : relatedLists) {
	    	        	Name = relatedList.getName();
	    	        	Label = relatedList.getLabel();
	    	        	Custom = relatedList.getCustom();	
	    	        	//relatedList.getField();
	    	        	//relatedList.getSobject();
	    	        	RelatedListSort[] rlSorts = relatedList.getSort();
	    	        	for(RelatedListSort rlSort : rlSorts) {
	    	        		SortColumn = rlSort.getColumn();
	    	        		SortAscending = rlSort.isAscending();	    	        		
	    	        	}
	    	        	
	    	        	/*
	    	        	ShareAccessLevel accessLevelRequiredforCreate = relatedList.getAccessLevelRequiredForCreate();
	    	        	ShareAccessLevel[] shareAccessLevels = accessLevelRequiredforCreate.values();
	    	        	for(ShareAccessLevel shareAccessLevel : shareAccessLevels) {
	    	        		System.out.println(shareAccessLevel.toString());
	    	        	}
	    	        	*/
	    	        	DescribeLayoutButton[] rlButtons = relatedList.getButtons();
	    	        	ElementType = "Button";
	    	        	for(DescribeLayoutButton rlButton : rlButtons) {
	    	        		ButtonName = rlButton.getName();
	    	        		ButtonLabel = rlButton.getLabel();
	    	        		ButtonContent = rlButton.getContent();
	    	        		ButtonCustom= rlButton.getCustom();
	    	        		ButtonEncoding = rlButton.getEncoding();
	    	        		ButtonURL = rlButton.getUrl();	
	    	        		jdbc.insertRelatedLists(jdbc.getOrgID(), jdbc.getTimeStamp(), LayoutID, Name, Label, Custom, SortColumn, SortAscending, ElementType, "", "", "", "", ButtonName, ButtonLabel, ButtonCustom, ButtonURL, ButtonEncoding, ButtonContent);
	    	        	}
	    	        	
	    	        	RelatedListColumn[] rlColumns = relatedList.getColumns(); 
	    	        	ElementType = "Column";
	    	        	for(RelatedListColumn rlColumn : rlColumns) {
	    	        		ColumnName = rlColumn.getName();
	    	        		ColumnField = rlColumn.getField();
	    	        		ColumnFormat = rlColumn.getFormat();
	    	        		ColumnLookup = rlColumn.getLookupId();
	    	        		jdbc.insertRelatedLists(jdbc.getOrgID(), jdbc.getTimeStamp(), LayoutID, Name, Label, Custom, SortColumn, SortAscending, ElementType, ColumnName, ColumnField, ColumnFormat, ColumnLookup, "", "", false, "", "", "");
	    	        	}
	    	        	
	    	        }

			}
			}    	    
    	    endTime = System.nanoTime();
    	    logger.log(Level.INFO, "Object detail capture complete. Total time taken : " + (endTime - startTime)/1000000000 + " Second.");
        	
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    public void testFunct() {
    	try	{				
			String query = "SELECT Id, SObjectType, PermissionsRead, PermissionsCreate,PermissionsDelete FROM ObjectPermissions WHERE parentid in (select id from permissionset where PermissionSet.Profile.Name = 'System Administrator')";

			QueryResult result = connection.query(query);				
			System.out.println(result.toString());
								
		}
	catch(Exception ex)
	{
		ex.printStackTrace();
		
	}  
    }
}
