package utilities;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

/**
 * 
 *
 */
public class HTML_Generator {
	
	DateUtil date = new DateUtil();
	String strCurrentDateTime = date.getCurrentDateTime();
	private static final Logger logger = Logger.getLogger(HTML_Generator.class.getName());
	private static HTML_Generator htmlGenerator;
	private JavaDBManager jdbc = JavaDBManager.getJavaDBManagerInstance();
	String resultDirPath, resultPath, fileName;
	File file, fileDir;	
	
	private HTML_Generator(String fileName) {
		
		String resultDirPath = System.getProperty("user.dir") + "\\Results\\" + strCurrentDateTime;
		String resultXMLPath = resultDirPath + "\\" + fileName + "_" + strCurrentDateTime + ".xml";
		
		fileDir = new File(resultDirPath);
		if(!fileDir.exists()) {
			fileDir.mkdirs();
		}
		
		file = new File(resultXMLPath);
		if(file.exists()) {
			System.out.println("File already exist");
		}
	}
	
	public static HTML_Generator getHTMLGenerator(String fileName) {
		if(htmlGenerator == null) {
			htmlGenerator = new HTML_Generator(fileName);
		} else {
			logger.log(Level.INFO, "One report generation is in progress. Report generation request ignored.");
		}
		return htmlGenerator;
	}
	
	
	/******************************************************************************************************************************************************
	 * Create HTML Report
	 * @author Parantap Samajdar
	 ******************************************************************************************************************************************************/
	
	public void createHTML_OLD(String heading, StringBuilder body) {			
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		try {
				fileWriter = new FileWriter(file);
				bufferedWriter = new BufferedWriter(fileWriter);
				String htmlPage = "<html><body style=’background-color:#ccc’><b><h3><center><u>" + heading + "</u></center></h3></b>";		
				bufferedWriter.write(htmlPage);
				bufferedWriter.append(body);				
				bufferedWriter.append("<Br>");				
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("--------------------------------------------------------------------------------------------");
				bufferedWriter.append("<footer>");
				bufferedWriter.append("<p>Authored by Cognizant</p>");					
				bufferedWriter.append("<p id=\"date\"></p>");
				bufferedWriter.append("</footer>");
				bufferedWriter.append("<script>document.getElementById(\"date\").innerHTML = Date();</script>");				
				bufferedWriter.append("--------------------------------------------------------------------------------------------");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.append("<Br>");
				bufferedWriter.flush();
				fileWriter.flush();
				try {		
					bufferedWriter.close();
					fileWriter.close();						
					try {
						Desktop.getDesktop().browse(file.toURI());
					} catch (IOException e) {
						e.printStackTrace();
					}
					file = null;
					fileDir = null;
					logger.log(Level.INFO, "HTML file " + resultPath + " created successfully.");
				} catch (IOException e) {
					e.printStackTrace();
				}
		} catch (IOException e) {			
			e.printStackTrace();
		} 	
	}	
	
//	/******************************************************************************************************************************************************
//	 * Constructor for HTML_Generator Class
//	 * @author Parantap Samajdar
//	 ******************************************************************************************************************************************************/
//	
//	private HTML_Generator(String fileName) {
//		
//		String resultDirPath = System.getProperty("user.dir") + "\\doc\\Result\\" + date.getCurrentDateTime();
//		String resultPath = resultDirPath + "\\" + fileName + "-" + date.getCurrentDateTime() + ".html";
//		
//		fileDir = new File(resultDirPath);
//		if(!fileDir.exists()) {
//			fileDir.mkdirs();
//		}
//		
//		file = new File(resultPath);
//		if(file.exists()) {
//			System.out.println("File already exist");
//		}
//	}
//	
//	public static HTML_Generator getHTMLGenerator(String fileName) {
//		if(htmlGenerator == null) {
//			htmlGenerator = new HTML_Generator(fileName);
//		} else {
//			logger.log(Level.INFO, "One report generation is in progress. Report generation request ignored.");
//		}
//		return htmlGenerator;
//	}
}
