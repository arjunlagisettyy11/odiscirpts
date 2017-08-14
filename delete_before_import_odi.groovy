//Created by ODI Studio
/*Use this to run this file in ODI studio*/
//evaluate(new File("C:\\Users\\arjunl\\Documents\\GitHub\\odiscripts\\import_odi.groovy"))

import oracle.odi.core.config.MasterRepositoryDbInfo;
import oracle.odi.core.config.WorkRepositoryDbInfo;
import oracle.odi.core.OdiInstance;
import oracle.odi.core.config.OdiInstanceConfig;
import oracle.odi.core.security.Authentication;
import oracle.odi.core.persistence.transaction.ITransactionDefinition;
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition;
import oracle.odi.core.persistence.transaction.ITransactionManager;
import oracle.odi.core.persistence.transaction.ITransactionStatus;
import oracle.odi.domain.project.OdiProject;
import oracle.odi.domain.project.OdiFolder;
import oracle.odi.domain.project.finder.IOdiFolderFinder;
import oracle.odi.impexp.smartie.ISmartExportService;
import oracle.odi.impexp.smartie.ISmartExportable;
import oracle.odi.impexp.smartie.impl.SmartExportServiceImpl;
import oracle.odi.impexp.EncodingOptions;
import java.util.logging.Logger;
import oracle.odi.domain.topology.OdiDataServer;
import oracle.odi.core.config.MasterRepositoryDbInfo;
import oracle.odi.domain.topology.finder.IOdiDataServerFinder;
import oracle.odi.publicapi.samples.SimpleOdiInstanceHandle;


Logger logger = Logger.getLogger("")
logger.info("I am a test info log")
String PROJECT_CODE = 'BIAPPS';
String ODI_DATA_SERVER = 'OBIA_BIA_ODIREPO';
String exportFolderName = 'C:\\Users\\arjunl\\Documents\\odi\\odi_sdk_automationtest\\';
String exportMetaDataFileName = 'C:\\Users\\arjunl\\Documents\\odi\\odi_sdk_automationtest\\ExportMetaData3.dat';
Date exportStartTime = new Date();

//Create file for exporting metadata
def exportMetaDataFile = new File(exportMetaDataFileName);
EncodingOptions encodeOptions = new EncodingOptions();

def objectImportResultMap = new HashMap < String, Expando > ();
def objectImportInput = new Expando()

println "Starting Process: " + exportStartTime

OdiDataServer odiRepoDataServer = ((IOdiDataServerFinder) odiInstance.getTransactionalEntityManager().getFinder(OdiDataServer.class)).findByName(ODI_DATA_SERVER);
String repoUrl =  odiRepoDataServer.getConnectionSettings().getJdbcUrl();
String repoDriver =  "oracle.jdbc.OracleDriver";
String repoUsername = odiRepoDataServer.getUsername();
String repoPassword = null;
String workrepName='BIAPPS_WORKREP';
String odiUsername='';
String odiPassword='';
SimpleOdiInstanceHandle MyOdiInstanceHandle = null;

switch (repoUrl.split(':')[-1]){
	case 'biststrp':
		repoPassword = 'ucb1s1sth3sm2rt3st_odirepo';
		odiUsername='biappsadmin';		
		odiPassword='ucb1s1sth3b0ss1nh3r3';
		break;
	case 'bisdevrp':
		repoPassword = 'ucb1s1sth3sm2rt3st_odirepo';
		odiUsername='biappsadmin';		
		odiPassword='ucb1s1sth3sm2rt3st';
		break;
	case 'bisqarp':
		repoPassword = 'oyQgNlXFTMg9KFfM505g';
		odiUsername='biappsadmin';		
		odiPassword='rcw32oKAZBONtkcraIlK';
		break;
}

try {

    //Create list objects for export 
	println exportMetaDataFile
    def objectDetails = []
    exportMetaDataFile.eachLine {
        line ->

		objectDetails=line.split(/,/, -1)
		
		//ignore malformed and empty lines 
		if (objectDetails.size()==6)
		{
			objectImportInput = new Expando();
			objectImportInput.srcFolderName = objectDetails[0];
			objectImportInput.exportedSuccessfully = objectDetails[1];
			objectImportInput.exportFileName = exportFolderName + objectDetails[2];
			objectImportInput.internalId = objectDetails[3];
			objectImportInput.result = objectDetails[4];
			objectImportInput.exportTimeString = objectDetails[5];
			objectImportResultMap[objectImportInput.srcFolderName] = objectImportInput;
		}
		else 
		{
			println "Ignoring malformed export metadata line: " + line
		}
    }
	
	println objectImportResultMap
	
	//Transaction Instance

    ITransactionDefinition txnDef = new DefaultTransactionDefinition();
    ITransactionManager tm = odiInstance.getTransactionManager();
    ITransactionStatus txnStatus = tm.getTransaction(txnDef);
	OdiFolder tempOdiFolderObj =null;
	
	int deleteTries = 0;
	deleteSuccess = false;
	deleteTriesLimit =1 
	while (deleteTries < deleteTriesLimit)
	{	
	
		
		objectImportResultMap.each {
		importObject ->
			//Open a new instance and delete object 

			MyOdiInstanceHandle = SimpleOdiInstanceHandle
					.create(repoUrl,  //JDBC driver URL
					repoDriver,  //Driver
					repoUsername, //Your Master repository username should come here
					repoPassword, //Your Master repository password should come here
					workrepName, //Work repository name should come here
					odiUsername,  //ODI login username
					odiPassword);   //ODI login password

			tempTxnDef = new DefaultTransactionDefinition();
			tempTm =  MyOdiInstanceHandle.getOdiInstance().getTransactionManager();
			tempTxnStatus = tempTm.getTransaction(tempTxnDef);
			
			tempOdiFolderObj = importObject.value.internalId.isInteger() ? ((IOdiFolderFinder) MyOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiFolder.class)).findById(importObject.value.internalId.toInteger()) : null;
			importObject.value.foundMatchingObject = tempOdiFolderObj;
			if (tempOdiFolderObj)
				{	
					try 
					{
						println "Found object and trying to delete: " + tempOdiFolderObj
						MyOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().remove(tempOdiFolderObj) 
						tempTm.commit(tempTxnStatus)
						println "Deleted successfully: " + tempOdiFolderObj
					}
					catch (e)
					{
						println "Unable to delete folder: " + importObject.value.srcFolderName
						println e
					}
					finally{
					//MyOdiInstanceHandle.release();
					}
				}
			else {
				println "Object NOT found: " + importObject.value.srcFolderName
			}
			
			MyOdiInstanceHandle.release();
			println "Release instance handle"
			//Delete and close the connection
		}
		deleteTries++;
	}
	//tm.commit(txnStatus)
/*
    
    //Create the smart export service
    ISmartExportService smartExpSvc = new SmartExportServiceImpl(odiInstance);
    Collection < OdiFolder > allFoldersMatchedByName = new LinkedList();

    //SDK Folder Objects
    //We create a map from objectImportResultMap contains expando object for each value entry
    //Expando object stores the naame of the folder given in the input file 
	//each matched object
	//a final object which qualifies i.e. 
    objectImportResultMap.each {
        tempObject ->
            Collection < OdiFolder > tempCollection = ((IOdiFolderFinder) odiInstance.getTransactionalEntityManager().getFinder(OdiFolder.class)).findByName(tempObject.value.srcFolderName, PROJECT_CODE);
        allFoldersMatchedByName.addAll(tempCollection);
		//Add all the matched folders to the 
        objectImportResultMap[tempObject.value.srcFolderName].objectsMatchedbyName = tempCollection;
		objectImportResultMap[tempObject.value.srcFolderName].objectsMatchedbyName.each {
        matchedObject ->
            if (matchedObject.getParentFolder().getName().startsWith("CUSTOM")) {
				objectImportResultMap[tempObject.value.srcFolderName].qualifiedObject = matchedObject
            }
    };
		
    }
    //export all objects in the map
    objectImportResultMap.each {
        exportObject ->
        if (exportObject.value.qualifiedObject) {
			tempList = new LinkedList();
			tempList.add(exportObject.value.qualifiedObject);
			println "Exporting: " + exportObject.value.qualifiedObject 
			try {
				//smartExpSvc.exportToXml(tempList,exportFolderName,exportObject.value.exportFileName,true,false,encodeOptions,false,null);
				exportObject.value.result = "Export Successful"
				exportObject.value.exportedSuccessfully =true
				exportObject.value.internalId = exportObject.value.qualifiedObject.getInternalId()
			}
			catch (Exception e){
				exportObject.value.exportedSuccessfully =false
				exportObject.value.result = "Found the object unable to export see exception"
				throw e;
			}
		}
		else
		{
			println "Unable to find: " + exportObject.value.srcFolderName 
			exportObject.value.result = "Unable to find the object"
			exportObject.value.exportedSuccessfully =false
		}
    }
	println "Export Complete: " +  new Date();
	
	println "Result: \n\t" + objectImportResultMap
    //Commit transaction, Close Authentication and ODI Instance
    tm.commit(txnStatus);
    objectImportResultMap.each {
		exportObject ->
		exportMetaDataFile.append exportObject.value.srcFolderName+','+exportObject.value.exportedSuccessfully+','+exportObject.value.exportFileName+','+exportObject.value.internalId+','+exportObject.value.result+','+exportStartTime+System.getProperty("line.separator")
			
			;
	}
*/	
} catch (Exception e) {

    //Commit transaction, Close Authentication and ODI Instance in Exception Block

    //auth.close();
    //odiInstance.close();
	println "Result: \n\t" + objectImportResultMap
    println(e);
}
finally {
//if running externally close auth and instance here
//auth.close();
//odiInstance.close();
}