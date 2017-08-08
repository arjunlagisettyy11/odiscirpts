//Created by ODI Studio
/*Use this to run this file in ODI studio
//evaluate(new File("C:\\Users\\arjunl\\Documents\\odi\\smart_export_odi.groovy"))
*/

import oracle.odi.core.config.MasterRepositoryDbInfo;
import oracle.odi.core.config.WorkRepositoryDbInfo;
import oracle.odi.core.config.PoolingAttributes;
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
import oracle.odi.domain.project.finder.IOdiInterfaceFinder;
import oracle.odi.domain.model.finder.IOdiDataStoreFinder;
import oracle.odi.domain.topology.OdiContext;
import oracle.odi.domain.model.OdiDataStore; 
import oracle.odi.domain.project.OdiFolder;
import oracle.odi.domain.project.OdiInterface;
import oracle.odi.domain.project.interfaces.DataSet;
import oracle.odi.interfaces.interactive.support.InteractiveInterfaceHelperWithActions;
import oracle.odi.interfaces.interactive.support.actions.InterfaceActionAddSourceDataStore;
import oracle.odi.interfaces.interactive.support.actions.InterfaceActionSetTargetDataStore;
import oracle.odi.interfaces.interactive.support.actions.InterfaceActionOnTargetDataStoreComputeAutoMapping;
import oracle.odi.interfaces.interactive.support.aliascomputers.AliasComputerDoubleChecker;
import oracle.odi.interfaces.interactive.support.clauseimporters.ClauseImporterDefault;
import oracle.odi.interfaces.interactive.support.mapping.automap.AutoMappingComputerColumnName;
import oracle.odi.interfaces.interactive.support.mapping.matchpolicy.MappingMatchPolicyColumnName;
import oracle.odi.interfaces.interactive.support.targetkeychoosers.TargetKeyChooserPrimaryKey;

import oracle.odi.impexp.smartie.ISmartExportService;
import oracle.odi.impexp.smartie.ISmartExportable;
import oracle.odi.impexp.smartie.impl.SmartExportServiceImpl;
import oracle.odi.impexp.EncodingOptions;

import java.util.logging.Logger

Logger logger = Logger.getLogger("")
logger.info ("I am a test info log")
String PROJECT_CODE = 'BIAPPS';
String folderListFile = 'C:\\Users\\arjunl\\Documents\\odi\\folderlist.txt';
String exportFolderName= 'C:\\Users\\arjunl\\Documents\\odi\\odi_sdk_automation';



try {

//Create list objects for export 

def foldersList = []
new File(folderListFile).eachLine { line ->
    foldersList << line
}

println foldersList

//Transaction Instance

ITransactionDefinition txnDef = new DefaultTransactionDefinition();
ITransactionManager tm = odiInstance.getTransactionManager();
ITransactionStatus txnStatus = tm.getTransaction(txnDef);

//Create the smart export service
ISmartExportService smartExpSvc = new SmartExportServiceImpl (odiInstance);
Collection <OdiFolder> allFoldersMatchedByName = new LinkedList();

//SDK Folder Objects

foldersList.each{ folderName ->
allFoldersMatchedByName.addAll(((IOdiFolderFinder)odiInstance.getTransactionalEntityManager().getFinder(OdiFolder.class)).findByName(folderName,PROJECT_CODE));
}
println "All folder matched by name "+allFoldersMatchedByName.size()+" "+allFoldersMatchedByName;

List <OdiFolder> qualifiedFoldersList = new LinkedList();

allFoldersMatchedByName.each{ folder -> 
	if(folder.getParentFolder().getName().startsWith("CUSTOM"))
	{
		qualifiedFoldersList.add(folder);
	}
};

//Convert collection to List
println "All folders which are Qualified "+qualifiedFoldersList.size() +" "+qualifiedFoldersList;

println "sarting export";
EncodingOptions encodeOptions= new  EncodingOptions();

qualifiedFoldersList.each { folder ->
	tempList = new LinkedList();
	tempList.add(folder);
	smartExpSvc.exportToXml(tempList,exportFolderName,folder.getName()+".xml",true,false,encodeOptions,false,null);
}
//export all objects in the list


//Commit transaction, Close Aithentication and ODI Instance

tm.commit(txnStatus);
println "after commit";
//auth.close();
println "after auth close";
//odiInstance.close();

} 
catch (Exception e)
{

//Commit transaction, Close Aithentication and ODI Instance in Exception Block

  //auth.close();
  //odiInstance.close();
  println(e);
}