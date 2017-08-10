//Created by ODI Studio
/*Use this to run this file in ODI studio
//evaluate(new File("C:\\Users\\arjunl\\Documents\\GitHub\\odiscripts\\smart_export_odi.groovy"))

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
import java.util.logging.Logger

Logger logger = Logger.getLogger("")
logger.info("I am a test info log")
String PROJECT_CODE = 'BIAPPS';
String folderListFile = 'C:\\Users\\arjunl\\Documents\\odi\\folderlist.txt';
String exportFolderName = 'C:\\Users\\arjunl\\Documents\\odi\\odi_sdk_automationtest\\';
Date exportStartTime = new Date();

//Create file for exporting metadata
def exportMetaDataFile = new File(exportFolderName + 'ExportMetaData' + '.dat');
exportMetaDataFile.write ""
EncodingOptions encodeOptions = new EncodingOptions();

def objectExportResultMap = new HashMap < String, Expando > ();
def objectExportResult = new Expando()

println "Starting Process: " + exportStartTime

try {

    //Create list objects for export 

    //def foldersList = []
    new File(folderListFile).eachLine {
        line ->
            objectExportResult = new Expando();
        objectExportResult.srcFolderName = line;
		objectExportResult.exportFileName = line.replaceAll("\\s","")+".xml";
        objectExportResultMap[line] = objectExportResult;
    }

    //Transaction Instance

    ITransactionDefinition txnDef = new DefaultTransactionDefinition();
    ITransactionManager tm = odiInstance.getTransactionManager();
    ITransactionStatus txnStatus = tm.getTransaction(txnDef);

    //Create the smart export service
    ISmartExportService smartExpSvc = new SmartExportServiceImpl(odiInstance);
    Collection < OdiFolder > allFoldersMatchedByName = new LinkedList();

    //SDK Folder Objects
    //We create a map from objectExportResultMap contains expando object for each value entry
    //Expando object stores the naame of the folder given in the input file 
	//each matched object
	//a final object which qualifies i.e. 
    objectExportResultMap.each {
        tempObject ->
            Collection < OdiFolder > tempCollection = ((IOdiFolderFinder) odiInstance.getTransactionalEntityManager().getFinder(OdiFolder.class)).findByName(tempObject.value.srcFolderName, PROJECT_CODE);
        allFoldersMatchedByName.addAll(tempCollection);
		//Add all the matched folders to the 
        objectExportResultMap[tempObject.value.srcFolderName].objectsMatchedbyName = tempCollection;
		objectExportResultMap[tempObject.value.srcFolderName].objectsMatchedbyName.each {
        matchedObject ->
            if (matchedObject.getParentFolder().getName().startsWith("CUSTOM")) {
				objectExportResultMap[tempObject.value.srcFolderName].qualifiedObject = matchedObject
            }
    };
		
    }
    //export all objects in the map
    objectExportResultMap.each {
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
	
	println "Result: \n\t" + objectExportResultMap
    //Commit transaction, Close Aithentication and ODI Instance
    tm.commit(txnStatus);
    objectExportResultMap.each {
		exportObject ->
		exportMetaDataFile.append exportObject.value.srcFolderName+','+exportObject.value.exportedSuccessfully+','+exportObject.value.exportFileName+','+exportObject.value.internalId+','+exportObject.value.result+','+exportStartTime+System.getProperty("line.separator")
			
			;
	}
	
} catch (Exception e) {

    //Commit transaction, Close Aithentication and ODI Instance in Exception Block

    //auth.close();
    //odiInstance.close();
	println "Result: \n\t" + objectExportResultMap
    println(e);
}