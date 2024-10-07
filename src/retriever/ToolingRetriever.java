package retriever;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sforce.soap.metadata.ReadResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.tooling.*;
import com.sforce.soap.tooling.metadata.ApexClass;
import com.sforce.soap.tooling.metadata.ProfileApplicationVisibility;
import com.sforce.soap.tooling.sobject.ApexComponent;
import com.sforce.soap.tooling.sobject.ApexTrigger;
import com.sforce.soap.tooling.sobject.AssignmentRule;
import com.sforce.soap.tooling.sobject.BusinessProcess;
import com.sforce.soap.tooling.sobject.CustomField;
import com.sforce.soap.tooling.sobject.CustomObject;
import com.sforce.soap.tooling.sobject.CustomTab;
import com.sforce.soap.tooling.sobject.EmailTemplate;
import com.sforce.soap.tooling.sobject.FlexiPage;
import com.sforce.soap.tooling.sobject.Flow;
import com.sforce.soap.tooling.sobject.HomePageComponent;
import com.sforce.soap.tooling.sobject.HomePageLayout;
import com.sforce.soap.tooling.sobject.Layout;
import com.sforce.soap.tooling.sobject.MetadataContainer;
import com.sforce.soap.tooling.sobject.PermissionSet;
import com.sforce.soap.tooling.sobject.Profile;
import com.sforce.soap.tooling.sobject.ProfileLayout;
import com.sforce.soap.tooling.sobject.RecordType;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.sobject.ValidationRule;
import com.sforce.soap.tooling.sobject.WebLink;
import com.sforce.soap.tooling.sobject.WorkflowRule;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.transport.SoapConnection;

import utilities.DateUtil;
import utilities.Excel_Generator;
import utilities.JavaDBManager;
import utilities.PreferenceCollection;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.regex.Matcher;

public class ToolingRetriever {	
	
	private ToolingConnection toolingConnection;
	private Logger logger = Logger.getLogger(ToolingRetriever.class.getCanonicalName());
	private PreferenceCollection prefCol = new PreferenceCollection();
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	
	/*****************************************************************************************************************************************************                
	 * Constructor
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public ToolingRetriever(String username, String password) throws ConnectionException {
		ConnectorConfig config = new ConnectorConfig();
		config.setManualLogin(true);
		PartnerConnection partnerConnection = com.sforce.soap.partner.Connector.newConnection(config);		
		com.sforce.soap.partner.LoginResult lr = partnerConnection.login(username,password);
		ConnectorConfig toolingConfig = new ConnectorConfig();
		toolingConfig.setSessionId(lr.getSessionId());
		toolingConfig.setServiceEndpoint(lr.getServerUrl().replace('u', 'T'));
		toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);
		
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Apex class code
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getApexClassBody(String apexClassName) throws ConnectionException {
		QueryResult queryResult = toolingConnection.query("select Body from ApexClass where Name = '" + apexClassName + "'");	
		String apexClassBody = null;		
		if(queryResult.getSize() > 0) {
			for(SObject so : queryResult.getRecords()) {	
				apexClassBody = so.toString();								
			}
		} else {
			apexClassBody = "Code not available for Apex class." + apexClassName;
		}
		return apexClassBody;
	}
	
	/*****************************************************************************************************************************************************                
	 * Calculate and Return Org complexity
	 * Formula - TBD
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getOrgComplexity() {		
		String complexity = "";
		//String queryApexClassCodeLength = "SELECT Sum(LengthWithoutComments) FROM ApexClass WHERE ID NOT IN (SELECT ApexTestClassId FROM ApexCodeCoverage)";
		String queryApexClassCodeLength = "SELECT LengthWithoutComments FROM ApexClass WHERE ID NOT IN (SELECT ApexTestClassId FROM ApexCodeCoverage)";
		QueryResult queryResult;
		try {
			queryResult = toolingConnection.query(queryApexClassCodeLength);
			System.out.println(queryResult.getSize());
			if(queryResult.getSize() > 0) {
				for(SObject so : queryResult.getRecords()) {
					System.out.println(so.toString());
				}				
			}
		} catch (ConnectionException e) {			
			e.printStackTrace();
		}		
		return complexity;
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Apex Trigger code
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getApexTriggerBody(String apexTriggerName) throws ConnectionException {
		QueryResult queryResult = toolingConnection.query("select Body from ApexTrigger where Name = '" + apexTriggerName + "'");	
		String apexTriggerBody = null;		
		if(queryResult.getSize() > 0) {
			for(SObject so : queryResult.getRecords()) {	
				apexTriggerBody = so.toString();								
			}
		} else {
			apexTriggerBody = "Code not available for Apex trigger." + apexTriggerBody;
		}
		return apexTriggerBody;
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Apex class Symbol Table
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getApexClassSymbolTable(String apexClassName) throws ConnectionException {
		QueryResult queryResult = toolingConnection.query("select SymbolTable from ApexClass where Name = '" + apexClassName + "'");
		String apexClassSymbolTable = null;		
		if(queryResult.getSize() > 0) {
			for(SObject so : queryResult.getRecords()) {				
				apexClassSymbolTable = so.toString();								
			}
		}
		return apexClassSymbolTable;
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Apex Trigger information
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public void getApexTrigger() throws ConnectionException {
		
		QueryResult queryResult = toolingConnection.query("Select Name, EntityDefinitionId, ApiVersion, body, status, UsageAfterDelete, UsageAfterInsert, UsageAfterUndelete, UsageAfterUpdate, UsageBeforeDelete, UsageBeforeInsert, UsageBeforeUpdate, UsageIsBulk,LastModifiedDate,CreatedDate from ApexTrigger");
		String objectName = null;
		System.out.println("Apex Trigger:"+queryResult.getSize());
		if(queryResult.getSize() > 0) {
			
			for(SObject so : queryResult.getRecords()) {				
				ApexTrigger at = (ApexTrigger)so;	
				if(isSalesforceID(at.getEntityDefinitionId())) {
					QueryResult queryObjectName = toolingConnection.query("select DeveloperName from CustomObject where id = '" + at.getEntityDefinitionId() + "'");
					if(queryObjectName.getSize() > 0) {
						CustomObject customObject = (CustomObject)queryObjectName.getRecords()[0];
						objectName = customObject.getDeveloperName();

					}					
				} else {
					objectName = at.getEntityDefinitionId();
				}
				//Commenting Out for now
				//String.valueOf(at.getCreatedDate()),String.valueOf(at.getCreatedBy()),String.valueOf(at.getLastModifiedDate()),String.valueOf(at.getLastModifiedBy());				
				jdbc.insertTriggers(jdbc.getOrgID(), jdbc.getTimeStamp(), at.getName(), objectName, at.getApiVersion(), at.getBody().substring(0,Math.min(at.getBody().length(),3999)), 
						at.getStatus(), at.getUsageAfterDelete(), at.getUsageAfterInsert(), at.getUsageAfterUndelete(), at.getUsageAfterUpdate(), at.getUsageBeforeDelete(), 
						at.getUsageBeforeInsert(), at.getUsageBeforeUpdate(), at.getUsageIsBulk(),at.getCreatedDate().getTime().toString(),null,
						at.getLastModifiedDate().getTime().toString(),null);
				//at.getLastModifiedBy().toString(),at.getCreatedBy().toString()
			}
		}
		logger.log(Level.INFO, "Apex triggers captured");
	}
	
	/*****************************************************************************************************************************************************                
	 * Update validation rules
	 * @author Parantap Samajdar
	 * @throws SQLException 
	******************************************************************************************************************************************************/
	public void getValidationRules() throws ConnectionException, SQLException {
		QueryResult queryAllValidationRules = toolingConnection.query("select id from ValidationRule");	
		System.out.println("validation rules:"+queryAllValidationRules.getSize());
		if(queryAllValidationRules.getSize() > 0) {			
			for(SObject so : queryAllValidationRules.getRecords()) {				
				QueryResult queryindividualValidationRules = toolingConnection.query("select active, fullname, description, ErrorDisplayField, ErrorMessage, id, Metadata, ValidationName, EntityDefinition.FullName,LastModifiedDate,CreatedDate from ValidationRule where id = '" + so.getId() + "'");
				ValidationRule vr = (ValidationRule)queryindividualValidationRules.getRecords()[0];		
				//Commenting Out for now
				//,vr.getCreatedDate(),vr.getCreatedBy(),vr.getLastModifiedDate(),vr.getLastModifiedBy();
				jdbc.insertValidationRules(jdbc.getOrgID(), jdbc.getTimeStamp(), vr.getActive(), vr.getDescription(), vr.getErrorDisplayField(), vr.getErrorMessage(), 
						vr.getMetadata().getErrorConditionFormula(), so.getId(), vr.getValidationName(),vr.getEntityDefinition().getFullName(),
						vr.getCreatedDate().getTime().toString(),null,vr.getLastModifiedDate().getTime().toString(),null);				 
			}	
		}
		logger.log(Level.INFO, "Validation rules captured");
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Profile information
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getProfile(String ProfileName) throws ConnectionException {
		QueryResult queryResult = toolingConnection.query("select metadata from profile where name= '" + ProfileName + "'");		
		String profileMetadata = null;		
		if(queryResult.getSize() > 0) {
			for(SObject so : queryResult.getRecords()) {
				Profile profile = (Profile)so;
				try {
					SObject[] profileLayouts = profile.getProfileLayouts().getRecords();
					for(SObject layout : profileLayouts) {
						ProfileLayout profileLayout = (ProfileLayout)layout;
						System.out.println(profileLayouts.toString());		
						//Commenting Out for now
						//profile.getCreatedBy(),profile.getCreatedDate(),profile.getLastModifiedBy(),profile.getLastModifiedDate();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		return profileMetadata;
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Org ID of the logged in user
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getOrgId() throws ConnectionException {		
		return toolingConnection.getUserInfo().getOrganizationId();
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Org Name of the logged in user
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getOrgName() throws ConnectionException {
		return toolingConnection.getUserInfo().getOrganizationName();
		
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Field Definition
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getFieldDefinition() throws ConnectionException {
		QueryResult queryFieldDefinition = toolingConnection.query("select id, DataType,NamespacePrefix, DeveloperName from FieldDefinition where EntityDefinition.QualifiedApiName = 'Account'");
		String fieldDefinition = null;
		if(queryFieldDefinition.getSize() > 0) {
			for(SObject so : queryFieldDefinition.getRecords()) {
				//fieldDefinition = so.															
			}
		} 
		return fieldDefinition;
	}
	
	/*****************************************************************************************************************************************************                
	 * Return Debug Log
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public String getApexLog() throws ConnectionException {
		QueryResult ApexLog = toolingConnection.query("select Operation, Request, LogUserId, Location, Application, StartTime, Status from ApexLog");
		String fieldDefinition = null;
		if(ApexLog.getSize() > 0) {
			for(SObject so : ApexLog.getRecords()) {
																			
			}
		} 
		return fieldDefinition;
	}
	
	/*****************************************************************************************************************************************************                
	 * Test function
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public void test1() {
		MetadataContainer metadataContainer = new MetadataContainer();
		QueryResult custmFieldMembers = metadataContainer.getCustomFieldMembers();
		System.out.println(custmFieldMembers.getTotalSize());
		try {
			SObject[] sObjects = custmFieldMembers.getRecords();
			for(SObject sObject : sObjects) {
				System.out.println(sObject.getId());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*****************************************************************************************************************************************************                
	 * Test function
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public void displayAllObjectInformation() throws ConnectionException {
		DescribeGlobalResult res = toolingConnection.describeGlobal();
		DescribeGlobalSObjectResult[] describeGlobalSObjectResults = res.getSobjects();		
		
		for(DescribeGlobalSObjectResult describeGlobalSObjectResult : describeGlobalSObjectResults) {
			System.out.println("Name :: " + describeGlobalSObjectResult.getName());
			System.out.println("Label :: " + describeGlobalSObjectResult.getLabel());						
		}		
	}
	
	/*****************************************************************************************************************************************************                
	 * Display all metadata information
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public ArrayList<String> describeAllMetadata() throws ConnectionException {
		
		ArrayList<String> metadataList = new ArrayList<>();
		
		DescribeGlobalResult globalResults = toolingConnection.describeGlobal();
		DescribeGlobalSObjectResult[] globalSObjectResults = globalResults.getSobjects();
		
		for(DescribeGlobalSObjectResult globalSObjectResult : globalSObjectResults) {
			//DescribeSObjectResult describeSObjectResults = toolingConnection.describeSObject(globalSObjectResult.getName());
			metadataList.add(globalSObjectResult.getName());			
		}			
		
		return metadataList;
	}
	
	/*****************************************************************************************************************************************************                
	 * Compute all metadata changes done in last n days information
	 * @author Parantap Samajdar
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	******************************************************************************************************************************************************/
	public ArrayList<ArrayList<String>> getMetadataChanges(int last_N_Days) throws FileNotFoundException, UnsupportedEncodingException {
		
		String query;
		QueryResult queryAllMetadata;
		ArrayList<ArrayList<String>> changeItemsList = new ArrayList<ArrayList<String>>();	
		ArrayList<String> mtd = new ArrayList<>();
		try {
			ArrayList<String> metadataList = this.describeAllMetadata();	
			System.out.println(metadataList);
			for(String metadata : metadataList) {
				switch (metadata) {
					case "WebLink" :
						query = "select MasterLabel from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								WebLink  wl = (WebLink)so;								
								mtd.add(wl.getMasterLabel());								
							}							
						}						
						break;
					
					case "CustomObject" :
						query = "select DeveloperName from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								CustomObject  wl = (CustomObject)so;
								mtd.add(wl.getDeveloperName());								
							}							
						}						
						break;						
					
					case "ApexComponent" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								ApexComponent   wl = (ApexComponent)so;								
								mtd.add(wl.getName());								
							}							
						}						
						break;
					
					case "BusinessProcess" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								BusinessProcess wl = (BusinessProcess)so;								
								mtd.add(wl.getName());								
							}							
						}						
						break;
					
					case "CustomField" :
						query = "select DeveloperName from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								CustomField wl = (CustomField)so;								
								mtd.add(wl.getDeveloperName());								
							}							
						}						
						break;
						
					case "CustomTab" :
						String queryAll = "select id from " + metadata;
						queryAllMetadata = toolingConnection.query(queryAll);						
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								query = "select FullName from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days + " and id = '" + so.getId() + "'";
								QueryResult queryAllCustomTabNames = toolingConnection.query(query);
								for(SObject cts : queryAllCustomTabNames.getRecords()) {
									CustomTab ct = (CustomTab)cts;								
									mtd.add(ct.getFullName());
								}																
							}							
						}						
						break;
						
					case "EmailTemplate" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								EmailTemplate et = (EmailTemplate)so;								
								mtd.add(et.getName());								
							}							
						}						
						break;
						
					case "FlexiPage" :
						query = "select MasterLabel from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								FlexiPage fp = (FlexiPage)so;								
								mtd.add(fp.getMasterLabel());								
							}							
						}						
						break;
						
					case "Flow" :
						query = "select MasterLabel from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								Flow fl = (Flow)so;								
								mtd.add(fl.getMasterLabel());								
							}							
						}						
						break;
						
					case "HomePageComponent" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								HomePageComponent hpc = (HomePageComponent)so;								
								mtd.add(hpc.getName());								
							}							
						}						
						break;
						
					case "HomePageLayout" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								HomePageLayout hpl = (HomePageLayout)so;								
								mtd.add(hpl.getName());								
							}							
						}						
						break;
						
					case "Layout" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								Layout lo = (Layout)so;								
								mtd.add(lo.getName());								
							}							
						}						
						break;
					
					/*  Commented out as permission sets are not yet providing meaningful data for QA. To be researched further   	
					case "PermissionSet" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								PermissionSet ps = (PermissionSet)so;								
								mtd.add(ps.getName());								
							}							
						}						
						break;
					*/
						
					case "Profile" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								Profile pr = (Profile)so;								
								mtd.add(pr.getName());								
							}							
						}						
						break;
						
					case "RecordType" :
						query = "select Name, SObjectType from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								RecordType rt = (RecordType)so;								
								mtd.add(rt.getName() + " " + rt.getSobjectType());								
							}							
						}						
						break;
						
					case "ValidationRule" :
						query = "select ValidationName from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								ValidationRule vr = (ValidationRule)so;								
								mtd.add(vr.getValidationName());								
							}							
						}						
						break;
						
					case "WorkflowRule" :
						query = "select Name from " + metadata + " where LastModifiedDate >= LAST_N_DAYS:" + last_N_Days;
						queryAllMetadata = toolingConnection.query(query);
						if(queryAllMetadata.getSize()>0) {
							mtd.add(metadata);
							for(SObject so : queryAllMetadata.getRecords()) {
								WorkflowRule wr = (WorkflowRule)so;								
								mtd.add(wr.getName());								
							}							
						}						
						break;
						
					default :
						mtd.add("");
					}
				}
				if(mtd.size() > 1) {
						changeItemsList.add(mtd);
					}
				System.out.println(changeItemsList);
		} catch (ConnectionException e) {			
			e.printStackTrace();
		} finally {
			/*
			 * Excel_Generator xl = new Excel_Generator(DateUtil.getCurrentDateTime());
			 * String sheetName = "Chng_list_" + utilities.DateUtil.getCurrentDateTime();
			 * Sheet sheet = xl.addSheet(sheetName); xl.addTableToSheet(sheet,
			 * changeItemsList); xl.saveWorkbook();
			 */
		}
		return changeItemsList;
	}
	
	/*****************************************************************************************************************************************************                
	 * Test function
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	public void testMetadataContainer() throws ConnectionException {
		QueryResult queryResult = toolingConnection.query("select Id, ApiVersion, Name, NamespacePrefix, SymbolTable, Body from ApexClass");		
		
		if(queryResult.getSize() > 0)
			for(SObject so : queryResult.getRecords()) {
				System.out.println(so.getId());
				//System.out.println(so.toString());				
			}
		
	}
	
	/*****************************************************************************************************************************************************                
	 * Private method - verify if a passed string is salesforce id or not
	 * @author Parantap Samajdar
	******************************************************************************************************************************************************/
	private boolean isSalesforceID(String input) {				  
		Pattern id = Pattern.compile("[a-zA-Z0-9]{15}|[a-zA-Z0-9]{18}");		
		Matcher matcher = id.matcher(input);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}		  
	}
	
	/*****************************************************************************************************************************************************                
	 * Fetch Weblink information from SFDC org and update databes	 
	 * @author Parantap Samajdar
	 * @throws ConnectionException 
	******************************************************************************************************************************************************/
	public void getWebLinkDetails() throws ConnectionException {		
		String query;
		QueryResult queryAllMetadata;
		query = "select Name, Description, DisplayType, EncodingKey, HasMenubar, HasScrollbars, HasToolbar, LinkType, MasterLabel, OpenType, URL, CreatedDate,LastModifiedDate from weblink";
		//query = "select * from weblink";				
							
			queryAllMetadata = toolingConnection.query(query);	
			System.out.println("Weblinks count"+queryAllMetadata.getSize());
			try {
				if(queryAllMetadata.getSize()>0) {
					for(SObject so : queryAllMetadata.getRecords()) {
						WebLink  wl = (WebLink)so;
						System.out.println(wl.getClass()+"-"+wl.getFullName()+"-"+wl.getCreatedDate().getTime().toString()+"-"+wl.getLastModifiedDate().getTime().toString());
						try {
							String descr = wl.getDescription();
							if(descr != null) {
								descr = descr.replaceAll("\'", "\'\'");
							}
							String URL = wl.getUrl();
							if(URL != null) {
								URL = URL.replaceAll("\'", "\'\'");
							}
							
							jdbc.insertWebLinks(jdbc.getOrgID(), jdbc.getTimeStamp(), wl.getName(), descr, wl.getDisplayType(), wl.getEncodingKey(), wl.getHasMenubar(), 
									wl.getHasScrollbars(), wl.getHasToolbar(), wl.getLinkType(), wl.getMasterLabel(), wl.getOpenType(), URL, 
									wl.getCreatedDate().getTime().toString(), null, wl.getLastModifiedDate().getTime().toString(), null);
						} catch(Exception e) {
							logger.log(Level.WARNING, e.getMessage());
						}				
					}	
				}			 			
			}catch(Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
	}
}