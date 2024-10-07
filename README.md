#SFDC IMPACT ANALYZER TOOL - VERSION 1.0
------------------------------------------------------------------------------------------------------------------------------------
Cognizant SFDC Impact Analyzer Tool is an open-source solution that provides user a unified interface , which helps to retrieve the latest components from given Salesforce Sandbox and for the current date.
Upon retrieving the same details, it gives user the ability to run a comparison with previous details .

##FEATURES 
-----------------------------------------------------------------------------------------------------------------------------------
###1 : Retrieve the Salesforce org compoenents of Current Date and store it in Java Derby Database with given reference of Organization ID and Time Stamp details
###2 : Run the comparison of already stored components details of past dates with the recent one and show it in Application UI in user readable format
###3 : Generate the HTML reports of comparison analysis for future reference

##DOWNLOAD 
-----------------------------------------------------------------------------------------------------------------------------------
Download the latest version from the Releases. After extracting the zip, follow the steps from Section : How to Install.

###Hardware Requirements#
RAM: 4 GB or More
Processor: Intel i3 Processor
Operating System: Windows (64 bit)

###Software Requirements#
Latest Java 1.8
IDE supporting Java Development (Eclipse, Netbeans, etc.)

##HOW TO INSTALL
----------------------------------------------------------------------------------------------------------------------------------
Step#1 : Follow below steps to generate jar files for respective WSDL files - metadata , enterprise , apex , tooling or partner WSDL files for your organization:
##1 : Log in to your Salesforce account. You must log in as an administrator or as a user who has the “Modify All Data” permission.
##2 : From Setup, enter API in the Quick Find box, then select API.
##3 : Click respective Generate WSDL button and save the XML WSDL file to your file system (for example within the same folder, you saved the SFDC Impact Analyzer Tool zip file).
		
Step#2 : Once you have the WSDL files, next step is to convert them to jar files and import them into your development platform so that your development/execution environment can perform the necessary activities

The basic syntax for converting wsdlc into JAR file is:
java -classpath pathToWsc;pathToWscDependencies com.sforce.ws.tools.wsdlc pathToWsdl/WsdlFilename pathToOutputJar/OutputJarFilename

For example, on Windows:
java –classpath force-wsc-44.0.0.jar;ST4-4.0.8.jar;antlr-runtime-3.5.jar com.sforce.ws.tools.wsdlc metadata.wsdl metadata.jar


##LAUNCHING UI 
--------------------------------------------------------------------------------------------------------------------------------
For Windows , Double Click the Run.bat to launch the UI. Note : Cognizant Intelligent Test Scripter is built in combination of Java FX, Java Derby DB . Hence it will work on any desktop OS which supports Java.
##Development / Contribution
Please read CONTRIBUTING.md before submitting your pull requests. It also has details on how to setup your development environment.

##CODE OF CONDUCT
---------------------------------------------------------------------------------------------------------------------------------
To provide clarity on what is expected of our members, Cognizant SFDC Impact Analyzer Tool has adopted the code of conduct defined by the Contributor Covenant. This document is used across many open source communities and we think it articulates our values well. For more, see the Code of Conduct.

##CONTACT US
--------------------------------------------------------------------------------------------------------------------------------- 
To ask specific questions on Cognizant SFDC Impact Analyzer Tool,  or to discuss about any future improvements in this tool, or for any other technical detail, please contact us @ QEAEASSalesforce@cognizant.com.

