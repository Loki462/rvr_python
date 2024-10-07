package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Excel_Generator {
	
	int sheetCounter = 0; // Tracks duplicate sheet names in addSheet method	
	Workbook wb = null;
	String fileName = new String();
	Logger logger = Logger.getLogger(Excel_Generator.class.getName());
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	public String resultDirPath = null;
	String strDateTime = null;
	//*******************Commenting out for testing purpose*************************
	
	public Excel_Generator(String strDateTime) {
		this.strDateTime = strDateTime;
				//DateUtil.getCurrentDateTime();
	}

	
	private String getFileName(String baseName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTimeInfo = dateFormat.format(new Date());
        return baseName.concat(String.format("_%s.xlsx", dateTimeInfo));
    }
 
    public void export(String table, ResultSet result) { 
        String excelFilePath = getFileName(table.concat("_REPORT"));
 
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(table);
 
            writeHeaderLine(result, sheet);
 
            writeDataLines(result, workbook, sheet);
 
        	//String strDateTime = DateUtil.getCurrentDateTime();
            //System.out.print("File Path Date Time: "+strDateTime);
        	resultDirPath = System.getProperty("user.dir") + "\\Results\\Excel_reports\\"+strDateTime;
        	System.out.println("Excel File Path for (" + table +"): " + resultDirPath);

        	File fileDir = new File(resultDirPath);
        	if(!fileDir.exists()) {
        		fileDir.mkdirs();
        	}
            FileOutputStream outputStream = new FileOutputStream(resultDirPath+"\\"+excelFilePath);
            workbook.write(outputStream);
            workbook.close(); 
        } catch (SQLException e) {
            System.out.println("Datababse error:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File IO error:");
            e.printStackTrace();
        }
    }
    
    public void replaceExcelCellWithGivenValue(String strFileName, String inputValue) {
    	try {
    		String FileNameSearchString = "IA_TO_DOTNEXT_MAPPING_SUMMARY__REPORT";
    		File dir = new File(resultDirPath);
    		FilenameFilter filter = new FilenameFilter() {
    	         public boolean accept (File dir, String name) { 
    	            return name.startsWith(strFileName) && name.endsWith("xlsx");
    	         } 
    	      }; 
    	      String[] children = dir.list(filter);
    	      if (children == null) {
    	         System.out.println("Either dir does not exist or is not a directory"); 
    	      } else { 
    	         for (int i = 0; i< children.length; i++) {
    	        	String filename = children[i];
    	            System.out.println(filename);
    	            String filePath = resultDirPath+"\\"+filename;
    			    FileInputStream inputStr = new FileInputStream(filePath);
    			    XSSFWorkbook xssfWork = new XSSFWorkbook(inputStr) ;
    			    XSSFSheet sheet1 = xssfWork.getSheetAt(0);
    			    Iterator rowItr = sheet1.rowIterator();
    			    int intColNum = sheet1.getRow(0).getLastCellNum();
    			    System.out.println("Total No. of Column "+intColNum);
    			    
    			    for(Row row : sheet1) {
    			    	System.out.println("ROW:-->"+row.getRowNum());
    			    	   for(int cn=0; cn<intColNum; cn++) {
    			    	       // If the cell is missing from the file, generate a blank one
    			    	       // (Works by specifying a MissingCellPolicy)
    			    	       Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    			    	       // Print the cell for debugging
    			    	       System.out.println("CELL: " + cn + " --> " + cell.toString());
    			    	       if(cell.toString().isBlank()) {
    			    	    	   cell.setCellValue("null");
    			    	       }
    			    	   }
    			    	}
    			    inputStr.close();

    	            FileOutputStream outFile =new FileOutputStream(new File(filePath));
    	            xssfWork.write(outFile);
    	            outFile.close();
    	         } 
    	      }
		} catch (Exception e) {
		    e.printStackTrace();
		}
    }
 
    public void JsonGenerator() throws SQLException, UnsupportedEncodingException {
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
			  
			  GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
			  builder.setPrettyPrinting();
			  Gson gson = builder.create();
			  //Gson gson = new GsonBuilder().disableHtmlEscaping().create();	
			  JsonParser jp = new JsonParser(); 
			  JsonElement je = jp.parse(jsonObjectDetails.toString());
			  String prettyJsonString = gson.toJson(je); 

			  try { 
				  resultDirPath = System.getProperty("user.dir") + "\\Results\\Excel_reports\\"+strDateTime;
		        	System.out.println("Excel File Path :"+resultDirPath);

		        	File fileDir = new File(resultDirPath);
		        	if(!fileDir.exists()) {
		        		fileDir.mkdirs();
		        	}
				FileWriter file = new FileWriter(resultDirPath+"\\ImpactAnalyzer_JSON_Report.json"); 
				file.write(prettyJsonString); 
				file.close(); 
			  } catch (IOException e) { 
			//TODO Auto-generated catch block e.printStackTrace(); } 
	      System.out.println("JSON file created......");
		}
    }
    private void writeHeaderLine(ResultSet result, XSSFSheet sheet) throws SQLException {
        // write header line containing column names
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();
 
        Row headerRow = sheet.createRow(0);
 
        // exclude the first column which is the ID field
        for (int i = 1; i <= numberOfColumns; i++) {
            String columnName = metaData.getColumnName(i);
            Cell headerCell = headerRow.createCell(i - 1);
            headerCell.setCellValue(columnName);
        }
    }
 
    private void writeDataLines(ResultSet result, XSSFWorkbook workbook, XSSFSheet sheet) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();
 
        int rowCount = 1;
 
        while (result.next()) {
            Row row = sheet.createRow(rowCount++);
 
            for (int i = 1; i <= numberOfColumns; i++) {
                Object valueObject = result.getObject(i);
 
                Cell cell = row.createCell(i - 1);
 
                if (valueObject instanceof Boolean)
                    cell.setCellValue((Boolean) valueObject);
                else if (valueObject instanceof Double)
                    cell.setCellValue((double) valueObject);
                else if (valueObject instanceof Float)
                    cell.setCellValue((float) valueObject);
                else if (valueObject instanceof Date) {
                    cell.setCellValue((Date) valueObject);
                    formatDateCell(workbook, cell);
                } else cell.setCellValue((String) valueObject);
 
            }
 
        }
    }
 
    private void formatDateCell(XSSFWorkbook workbook, Cell cell) {
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        cell.setCellStyle(cellStyle);
    }
	
	public Sheet addSheet(String sheetName) {	
		try {
			Sheet sheet = this.wb.createSheet(sheetName);
			return sheet;
		} catch(Exception e) {
			sheetCounter ++;
			Sheet sheet = this.wb.createSheet(sheetName + "_" + this.sheetCounter );
			return sheet;
		}		
	}
	
	public void addTableToSheet(Sheet sheet, ArrayList<ArrayList<String>> tableData) {
		int rowNum = 0;
		for (ArrayList<String> tableRow : tableData) {
			Row row = sheet.createRow(rowNum++ );
			int colNum = 0;
			for(String cellData : tableRow) {
				Cell cell = row.createCell(colNum++);
				cell.setCellValue(cellData);
			}
		}
		cleanupWorkSheet(sheet);
	}
	
	public void saveWorkbook() {
		try {
			FileOutputStream out = new FileOutputStream(this.fileName);
			try {
				this.wb.write(out);
				this.wb.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} 
	}
	
	private void cleanupWorkSheet(Sheet sheet) {		
	}
	
}
