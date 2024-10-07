package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import utilities.JavaDBManager;
/**
 * @author debdatta porya
 *
 */
public class JSONUtil {
	   public static void main(String args[]) throws Exception {
		  JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
		  String strOrgID = null;
		  String strTimeStamp = null;
	      //Creating a JSONObject object
		  JSONObject jsonObjectDetails = new JSONObject();
	      JSONArray objectArray = new JSONArray();
	      //ResultSet rsTotalObjectCount = jdbc.getResultSet("Select Count(*) as TotalCount,OBJECTNAME From IA_TO_DOTNEXT_MAPPING GROUP BY OBJECTNAME");
	      ResultSet rsComparisonDetails = jdbc.getResultSet("Select * From IA_TO_DOTNEXT_MAPPING");
	    
	      ArrayList<ArrayList<String>> rsTotalObjectCount = jdbc.fetchTotalConfigCount("Select Count(*) as TotalCount,OBJECTNAME From IA_TO_DOTNEXT_MAPPING GROUP BY OBJECTNAME");
	      ArrayList<String> ObjectCounts = rsTotalObjectCount.get(0);
	      ArrayList<String> ObjectNames = rsTotalObjectCount.get(1);
	      System.out.println("Total Object Details : "+ObjectCounts+" AND "+ObjectNames+" AND Final query result Sise : "+ObjectCounts.size());
      	  //Count(*),ORG_ID,TIMESTAMP,OBJECTNAME,LAYOUTID,LAYOUTNAME,BUTTONS,FIELDS,RELATEDLISTS,RELATEDLISTSCOLUMNS,RELATEDLISTSBUTTONS,RECORDTYPES,TRIGGERS,VALIDATIONRULES
	      for(int i=0;i<ObjectCounts.size();i++) {
			  ResultSet rsLayoutDetails =jdbc.getResultSet("Select * From IA_TO_DOTNEXT_MAPPING Where OBJECTNAME = '"+ObjectNames.get(i)+"'"); 
			  //Inserting  ResutlSet data into the json object while(rsTotalObjectCount.next()) {
			  JSONObject recordObjectDetails = new JSONObject(); //Inserting key-valuepairs into the json object recordObjectDetails.put("ID", objId);
			  recordObjectDetails.put("ObjectId","OBJID-"+(i+1)); 
			  recordObjectDetails.put("ObjectName",ObjectNames.get(i)); 
			  int lid = 1;
		      //Creating a json array
		      JSONObject jsonLayoutObject = new JSONObject();
		      JSONArray layoutArray = new JSONArray();
			  while(rsLayoutDetails.next()) { 
				  JSONObject recordLayoutDetails = new JSONObject(); //Inserting key-value pairs into the json object
				  recordLayoutDetails.put("lid", "LID-"+lid); 
				  recordLayoutDetails.put("layoutID",rsLayoutDetails.getString("LAYOUTID")); 
				  recordLayoutDetails.put("LayoutName",rsLayoutDetails.getString("LAYOUTNAME")); 
				  recordLayoutDetails.put("Buttons",rsLayoutDetails.getString("BUTTONS")); 
				  recordLayoutDetails.put("Fields",rsLayoutDetails.getString("FIELDS")); 
				  recordLayoutDetails.put("RelatedLists",rsLayoutDetails.getString("RELATEDLISTS"));
				  recordLayoutDetails.put("RecordTypes",rsLayoutDetails.getString("RECORDTYPES"));
				  recordObjectDetails.put("Triggers", rsLayoutDetails.getString("TRIGGERS"));
				  recordObjectDetails.put("ValidationRules",rsLayoutDetails.getString("VALIDATIONRULES"));
				  layoutArray.add(recordLayoutDetails); 
				  lid = lid + 1; 
				  }
			  recordObjectDetails.put("LayoutDetails", layoutArray);
			  objectArray.add(recordObjectDetails); 
			  }
			  jsonObjectDetails.put("ComparisonDetails",objectArray);
			  Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
			  JsonParser jp = new JsonParser(); 
			  JsonElement je = jp.parse(jsonObjectDetails.toString());
			  String prettyJsonString = gson.toJson(je); 
			  try { FileWriter file = new FileWriter("C:\\Users\\porya\\eclipse-workspace\\SFDC_Quality_Analyzer_V2\\Results\\Excel_reports\\TestJson.json"); 
			  	file.write(prettyJsonString); 
			  	file.close(); 
			  } catch (IOException e) { 
			//TODO Auto-generated catch block e.printStackTrace(); } 
	      System.out.println("JSON file created......");
		}
	}
}
