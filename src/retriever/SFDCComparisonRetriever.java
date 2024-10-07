package retriever;

import java.util.logging.Logger;

import com.sforce.soap.metadata.MetadataConnection;

import javafx.beans.property.SimpleStringProperty;

public class SFDCComparisonRetriever {
	private SimpleStringProperty stringMetadataType;
	private SimpleStringProperty stringMetadataName;
	private SimpleStringProperty stringID;
	private SimpleStringProperty stringCreatedDate; 
	private SimpleStringProperty stringLastModifiedDate;

    private static final Logger logger = Logger.getLogger(SFDCComparisonRetriever.class.getName());
    //private final SimpleStringProperty strMetadataType;
    public SFDCComparisonRetriever(String strMetadataType,String strMetadataName ,String strID,String strCreatedDate ,String strLastModifiedDate){
    	this.stringMetadataType = new SimpleStringProperty(strMetadataType) ;
    	this.stringMetadataName = new SimpleStringProperty(strMetadataName) ;
    	this.stringID = new SimpleStringProperty(strID) ;
    	this.stringCreatedDate = new SimpleStringProperty(strCreatedDate) ;
    	this.stringLastModifiedDate = new SimpleStringProperty(strLastModifiedDate) ;
    }
    
    public void setMetadataType(String strMetadataType) {
    	stringMetadataType.set(strMetadataType) ;
    }
    public void setMetadataName(String strMetadataName) {
    	stringMetadataName.set(strMetadataName);
    }
    public void setID(String strID) {
    	stringID.set(strID) ;
    }
    public void setCreatedDate(String strCreatedDate) {
    	stringCreatedDate.set(strCreatedDate);
    }
    public void setLastModifiedDate(String strLastModifiedDate) {
    	stringLastModifiedDate.set(strLastModifiedDate);
    }
    public String getMetadataType() {
    	return stringMetadataType.get();
    }
    public String getMetadataName() {
    	return stringMetadataName.get();
    }
    public String getID() {
    	return stringID.get();
    }
    public String getCreatedDate() {
    	return stringCreatedDate.get();
    }
    public String getLastModifiedDate() {
    	return stringLastModifiedDate.get();
    }
}
