package retriever;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sforce.soap.metadata.ApexClass;
import com.sforce.soap.metadata.ApexComponent;
import com.sforce.soap.metadata.ApexPage;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.DescribeMetadata_element;
import com.sforce.soap.metadata.DescribeValueTypeResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.HomePageComponent;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.enterprise.AppMenuType;
import com.sforce.soap.enterprise.DescribeAppMenuResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.metadata.RetrieveMessage;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.metadata.RetrieveStatus;
import com.sforce.soap.metadata.SharingCriteriaRule;
import com.sforce.soap.metadata.SharingRules;
import com.sforce.soap.metadata.StaticResource;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.wsdl.Schema;

import utilities.JavaDBManager;
import utilities.PreferenceCollection;
import utilities.SF_ORG_Analytics;

import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.Profile;
import com.sforce.soap.metadata.ReadResult;

public class MetadataRetriever {
	
	// Binding for the metadata WSDL used for making metadata API calls
    private MetadataConnection metadataConnection;    
    private static BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
    // One second in milliseconds
    private static final long ONE_SECOND = 1000;
    // Maximum number of attempts to retrieve the results
    private static final int MAX_NUM_POLL_REQUESTS = 50;
    // Manifest file that controls which components get retrieved
    private static final String MANIFEST_FILE = "package.xml";
    // Salesforce api version
    private static final double API_VERSION = 44.0;
    private String UserID, Password, URL; 
    private PreferenceCollection prefCol = new PreferenceCollection();    
    private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
    private static final Logger logger = Logger.getLogger(MetadataRetriever.class.getName());
    
    public MetadataRetriever(String username, String password, String loginUrl) throws ConnectionException {
    	UserID = username;
    	Password = password;
    	URL = loginUrl;
    	createMetadataConnection(UserID, Password, URL);    	
    }
    
    /******************************************************************************************************************************************************
     *  Metadata object collection 
     * @author Parantap Samajdar   
     ******************************************************************************************************************************************************/
    public boolean listMetadataObjects() {    	
    	String strMetadataTypeName = null;
    	ListMetadataQuery query = new ListMetadataQuery();    
    	ArrayList<ArrayList<String>> queryList = new ArrayList<ArrayList<String>>();
    	ArrayList<String> inner = new ArrayList<>();
    	long startTime = System.nanoTime(); 	   	
    	    	
    	try {
    		DescribeMetadataResult rsMetadata = metadataConnection.describeMetadata(API_VERSION);
    		DescribeMetadataObject[] metadataObjects = rsMetadata.getMetadataObjects();
    		
    		for(int i=0; i<metadataObjects.length; i++) {    			
    			strMetadataTypeName = metadataObjects[i].getXmlName();
    			query.setType(strMetadataTypeName);
    			FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[] {query}, API_VERSION);    			
    			if(lmr != null) {
    				for (FileProperties n : lmr) { 
    					jdbc.updateMetadataComponentTable(jdbc.getOrgID(), jdbc.getTimeStamp(), strMetadataTypeName, n.getFullName(), n.getFileName(), n.getId(), n.getType(), n.getClass().toString(), n.getCreatedDate().getTime().toString(), n.getCreatedByName(), n.getLastModifiedDate().getTime().toString(), n.getLastModifiedByName());
  	    	      	}
    			}  
    			
    			//jdbc.batchUpdateMetadataComponentTable(queryList);
    		}    		
        	Long endTime = System.nanoTime();
        	logger.log(Level.INFO, "Time taken to fetch metadata information : " + (endTime - startTime)/1000000000 + " Second.");		
        	return true;
		} catch (ConnectionException e) {			
			//e.printStackTrace();
			return false;
		}    	
    }
    
    /******************************************************************************************************************************************************
     * Return list of all user profiles in the SFDC org  -- Redundant - can be retrieved by querying metadata components table
     * @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public ArrayList<String> listProfiles() {    		
    	  try {
    		  	ArrayList<String> sb = new ArrayList<String>();
  	    	    ListMetadataQuery query = new ListMetadataQuery();
  	    	    query.setType("Profile");  	    	    
  	    	    FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[] {query}, API_VERSION);
  	    	    if (lmr != null) {  	    	      
  	    	      for (FileProperties n : lmr) {  	
  	    	    		sb.add(n.getFullName()); 
  	    	    		//System.out.println(n.getLastModifiedDate().getTime().toString()+" - "+n.getCreatedDate().getTime().toString());
  	    	    		jdbc.insertProfileTable(jdbc.getOrgID(), jdbc.getTimeStamp(), n.getFullName(), "0", n.getCreatedDate().getTime().toString(), n.getCreatedByName().toString() , 
  	    	    				n.getLastModifiedDate().getTime().toString(), n.getLastModifiedByName().toString());
  	    	      }
  	    	    } 
  	    	    
  	    	  System.out.println("inside Profile list method:"+sb);
  	    	    return sb;
    	  } catch (ConnectionException ce) {
    	    ce.printStackTrace();
    	    return null;
    	  }
      }
    
    /******************************************************************************************************************************************************
     *  Return list of all custom objects in the SFDC org  -- Redundant - can be retrieved by querying metadata components table
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public StringBuffer listCustomObjects() {    	
  	  try {		
  		  		StringBuffer sb = new StringBuffer();
	    	    ListMetadataQuery query = new ListMetadataQuery();
	    	    query.setType("CustomObject");	    	    
	    	    //double asOfVersion = 37.0;	    	    
	    	    // Assuming that the SOAP binding has already been established.
	    	    FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[] {query}, API_VERSION);
	    	    if (lmr != null) {
	    	      for (FileProperties n : lmr) {
	    	        sb.append(n.getFullName());
	    	        sb.append(";");	    	        
  	    	      }
	    	    }
	    	    System.out.println("Inside Method : listCustomObjects - Length -"+sb.length());
	    	    return sb;  	      
	    	    
  	  } catch (ConnectionException ce) {
  	    ce.printStackTrace();
  	    return null;
  	  }
    }
    
    
    /******************************************************************************************************************************************************
     *  Retrieve specific metadata information (passed as parameter)  
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public StringBuffer describeMetadata(String type) {
    	  try {    	        	    
	    	    DescribeMetadataResult res1 = metadataConnection.describeMetadata(API_VERSION);    	    
	    	    StringBuffer sb = new StringBuffer();
	    	    if (res1 != null && res1.getMetadataObjects().length > 0) {
	    	      for (DescribeMetadataObject obj : res1.getMetadataObjects()) {
	    	        sb.append("***************************************************\n");
	    	        sb.append("XMLName: " + obj.getXmlName() + "\n");
	    	        sb.append("DirName: " + obj.getDirectoryName() + "\n");
	    	        sb.append("Suffix: " + obj.getSuffix() + "\n");    	        
	    	        sb.append("***************************************************\n");
	    	        //System.out.println(obj.toString());
	    	      }    	      
	    	    } else {
	    	      sb.append("Failed to obtain metadata types.");
	    	    }
	    	    //System.out.println(sb.toString());
	    	    return sb;
	    	  } catch (ConnectionException ce) {
	    	    ce.printStackTrace();
	    	    return null;
	    	  }    	  
    }
    
    /******************************************************************************************************************************************************
     *  Retrieve all SFDC configuration into hard disk in zip file format
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    public void retrieveZip() throws RemoteException, Exception    {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        // The version in package.xml overrides the version in RetrieveRequest
        retrieveRequest.setApiVersion(API_VERSION);
        setUnpackaged(retrieveRequest);

        // Start the retrieve operation
        AsyncResult asyncResult = metadataConnection.retrieve(retrieveRequest);
        String asyncResultId = asyncResult.getId();
        
        // Wait for the retrieve to complete
        int poll = 0;
        long waitTimeMilliSecs = ONE_SECOND;
        RetrieveResult result = null;
        do {
            Thread.sleep(waitTimeMilliSecs);
            // Double the wait time for the next iteration
            waitTimeMilliSecs *= 2;
            if (poll++ > MAX_NUM_POLL_REQUESTS) {
                throw new Exception("Request timed out. If this is a large set of metadata components, check that the time allowed by MAX_NUM_POLL_REQUESTS is sufficient.");
            }
            result = metadataConnection.checkRetrieveStatus(asyncResultId, true);
            System.out.println("Retrieve Status: " + result.getStatus());
        } while (!result.isDone());

        if (result.getStatus() == RetrieveStatus.Failed) {
            throw new Exception(result.getErrorStatusCode() + " msg: " +  result.getErrorMessage());
        } else if (result.getStatus() == RetrieveStatus.Succeeded) {      
            // Print out any warning messages
            StringBuilder buf = new StringBuilder();
            if (result.getMessages() != null) {
                for (RetrieveMessage rm : result.getMessages()) {
                    buf.append(rm.getFileName() + " - " + rm.getProblem());
                }
            }
            if (buf.length() > 0) {
                System.out.println("Retrieve warnings:\n" + buf);
            }
    
            // Write the zip to the file system
            System.out.println("Writing results to zip file");
            ByteArrayInputStream bais = new ByteArrayInputStream(result.getZipFile());
            File resultsFile = new File("retrieveResults.zip");
            FileOutputStream os = new FileOutputStream(resultsFile);
            try {
                ReadableByteChannel src = Channels.newChannel(bais);
                FileChannel dest = os.getChannel();
                copy(src, dest);
                
                System.out.println("Results written to " + resultsFile.getAbsolutePath());
            } finally {
                os.close();
            }
        }
    }
    
    /**
     * Helper method to copy from a readable channel to a writable channel,
     * using an in-memory buffer.
     */
    private void copy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        // Use an in-memory byte buffer
        ByteBuffer buffer = ByteBuffer.allocate(8092);
        while (src.read(buffer) != -1) {
            buffer.flip();
            while(buffer.hasRemaining()) {
                dest.write(buffer);
            }
            buffer.clear();
        }
    }
    
    /******************************************************************************************************************************************************
     *  Auxiliary function for retrieveZip
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    private void setUnpackaged(RetrieveRequest request) throws Exception {
        // Edit the path, if necessary, if your package.xml file is located elsewhere
        File unpackedManifest = new File(MANIFEST_FILE);
        System.out.println("Manifest file: " + unpackedManifest.getAbsolutePath());
        
        if (!unpackedManifest.exists() || !unpackedManifest.isFile())
            throw new Exception("Should provide a valid retrieve manifest " +
                    "for unpackaged content. " +
                    "Looking for " + unpackedManifest.getAbsolutePath());

        // Note that we populate the _package object by parsing a manifest file here.
        // You could populate the _package based on any source for your
        // particular application.
        com.sforce.soap.metadata.Package p = parsePackage(unpackedManifest);
        request.setUnpackaged(p);
    }
    
    /******************************************************************************************************************************************************
     *  Auxiliary function for retrieveZip
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    private com.sforce.soap.metadata.Package parsePackage(File file) throws Exception {
        try {
            InputStream is = new FileInputStream(file);
            List<PackageTypeMembers> pd = new ArrayList<PackageTypeMembers>();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element d = db.parse(is).getDocumentElement();
            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Element) {
                    Element ce = (Element)c;                    
                    NodeList namee = ce.getElementsByTagName("name");
                    if (namee.getLength() == 0) {
                        continue;
                    }
                    String name = namee.item(0).getTextContent();
                    NodeList m = ce.getElementsByTagName("members");
                    List<String> members = new ArrayList<String>();
                    for (int i = 0; i < m.getLength(); i++) {
                        Node mm = m.item(i);
                        members.add(mm.getTextContent());
                    }
                    PackageTypeMembers pdi = new PackageTypeMembers();
                    pdi.setName(name);
                    pdi.setMembers(members.toArray(new String[members.size()]));
                    pd.add(pdi);
                }
            }
            com.sforce.soap.metadata.Package r = new com.sforce.soap.metadata.Package();
            r.setTypes(pd.toArray(new PackageTypeMembers[pd.size()]));
            r.setVersion(API_VERSION + "");
            return r;
        } catch (ParserConfigurationException pce) {
            throw new Exception("Cannot create XML parser", pce);
        } catch (IOException ioe) {
            throw new Exception(ioe);
        } catch (SAXException se) {
            throw new Exception(se);
        }
    }
    
    /******************************************************************************************************************************************************
     *  Generate metadata connection
     *  @author Parantap Samajdar
     ******************************************************************************************************************************************************/
    private void createMetadataConnection(final String username,final String password, final String loginUrl) throws ConnectionException {
        final ConnectorConfig loginConfig = new ConnectorConfig();
        loginConfig.setAuthEndpoint(loginUrl);
        loginConfig.setServiceEndpoint(loginUrl);
        loginConfig.setManualLogin(true);
        //LoginResult loginResult = (new EnterpriseConnection(loginConfig)).login(username, password);
        com.sforce.soap.partner.LoginResult loginResult = (new PartnerConnection(loginConfig)).login(username, password);

        final ConnectorConfig metadataConfig = new ConnectorConfig();
        metadataConfig.setServiceEndpoint(loginResult.getMetadataServerUrl());
        metadataConfig.setSessionId(loginResult.getSessionId());
        this.metadataConnection = new MetadataConnection(metadataConfig);	// This line uses that configuration to establish a connection to the Salesforce Metadata API.

    }
    
    //The sample client application retrieves the user's login credentials.
    // Helper function for retrieving user input from the console
    String getUserInput(String prompt) {
        System.out.print(prompt);
        try {
            return rdr.readLine();
        }
        catch (IOException ex) {
            return null;
        }
    }
    
    /******************************************************************************************************************************************************
     *  Return list of all installed apps in the SFDC org  -- Redundant - can be retrieved by querying metadata components table - not working as of now
     *  @author Parantap Samajdar 
     ******************************************************************************************************************************************************/    
    public StringBuffer listApps() throws ConnectionException {    	
		AppMenuType[] AppMenu = AppMenuType.values();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< AppMenu.length; i ++) {			
			sb.append(AppMenu[i]);			
			sb.append(";");
		}				
		return sb;
	}
            
    /******************************************************************************************************************************************************
     *  Apex class   
     *  @author Parantap Samajdar   
     ******************************************************************************************************************************************************/
    public void getApexClassDetail() throws ConnectionException {
    	ReadResult readResult = metadataConnection.readMetadata("ApexClass", new String[] {"I_RuleEngineHelper"});    	
    	Metadata[] mdInfo = readResult.getRecords();
    	
    	for(Metadata md : mdInfo) {
    		if(md !=null) {
    			ApexClass sr = (ApexClass) md;
    			System.out.println(sr.getStatus().toString());
    			System.out.println(sr.getFullName());
    			byte[] apexClassContent = sr.getContent();
    			String decode = Base64.getEncoder().encodeToString(apexClassContent);
    			System.out.println(decode);
    		}
    	}
    }
    
    /******************************************************************************************************************************************************
     *  Apex component    
     *  @author Parantap Samajdar  
     ******************************************************************************************************************************************************/
    public void getApexComponentDetail() throws ConnectionException {
    	ReadResult readResult = metadataConnection.readMetadata("ApexComponent", new String[] {"dsfs__DocuSignConsole"});    	
    	Metadata[] mdInfo = readResult.getRecords();
    	for(Metadata md : mdInfo) {
    		if(md !=null) {
    			ApexComponent sr = (ApexComponent) md;    			
    			System.out.println(sr.getDescription());
    			System.out.println(sr.getFullName());
    			System.out.println(sr.getLabel());
    			byte[] apexComponentContent = sr.getContent();    			
    			String decode = Base64.getEncoder().encodeToString(apexComponentContent);    			
    		}
    	}
    }
    
    /******************************************************************************************************************************************************
     *  Apex Page  
     *  @author Parantap Samajdar    
     ******************************************************************************************************************************************************/
    public void testFunction() throws ConnectionException {
    	ReadResult readResult = metadataConnection.readMetadata("CustomObject", new String[] {"CAO Application"});    	
    	Metadata[] mdInfo = readResult.getRecords();
    	for(Metadata md : mdInfo) {
    		if(md !=null) {
    			ApexPage sr = (ApexPage) md;
    			System.out.println(sr.getDescription());
    			System.out.println("----------------------------------");
    			System.out.println(sr.getFullName());
    			System.out.println("----------------------------------");
    			System.out.println(sr.getLabel());
    			System.out.println("----------------------------------");
    			
    		}
    	}
    }
    
    /******************************************************************************************************************************************************
     *  Metadata list  
     *  @author Parantap Samajdar    
     ******************************************************************************************************************************************************/
    public void listMetadata() {
    	try {
	    	    ListMetadataQuery query = new ListMetadataQuery();	    	    
	    	    query.setType("Profile");	    	    
	    	    double asOfVersion = 44.0;	    	    
	    	    FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[] {query}, asOfVersion);
	    	    if (lmr != null) {
	    	      for (FileProperties n : lmr) {
	    	        //System.out.println("Component fullName: " + n.getFullName());
	    	        //System.out.println("Component type: " + n.getType());
	    	        //System.out.println("Component ID: " + n.getId());
	    	    	  //Profile currentProfile = n;
	    	    	  
	    	      } 	    	    	  
	    	     } else {
	    	    	 System.out.println("No items found");
	    	    }            
	    	    
    	  } catch (ConnectionException ce) {
    		  ce.printStackTrace();
    	  }
    }
    
}
