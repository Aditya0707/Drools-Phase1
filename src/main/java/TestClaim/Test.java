package TestClaim;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

//import org.slf4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test {
	  //Delimiters Used in the CSV file
	 private static final String CSV_Delimiter = ":";
	
	 
	 public static void main(String[] args)throws Exception{
		
		Calendar cal = Calendar.getInstance();
	    Date today = cal.getTime();
	    System.out.println(today);
	    cal.add(Calendar.YEAR, -1); // to get previous year add -1
	    Date lastYear = cal.getTime();
	    System.out.println(lastYear);
		
	  //Create List for Holding CalimInfo Objects
		  List<ClaimInfo> InputData = new ArrayList<ClaimInfo>();
		  
	  BufferedReader br = null;
	  try{
		  //Reading the csv file
		  br = new BufferedReader(new FileReader("ClientData.csv"));
		  
		  /*String[] evtnames = new String[]{"Medicare Part A","Medicare Part B","Medicare Part D"};
		  String[] evtDates = new String[]{"10/4/2017","12/7/2017","15/03/2017","16/06/2017","22/04/2017","30/11/2017"};
		  */
	
		  String line = "";
		  //Read to skip the header
		  br.readLine();
		  		  
		  //Reading from the second line
		  while ((line =br.readLine())!=null)
		  {
			  String[] CliamInfoInput = line.split(CSV_Delimiter);
			 
			  if(CliamInfoInput.length >0)
			  {
				  //save the Cliam INFORMATION in ClaimInfo object
				  ClaimInfo input = new ClaimInfo(CliamInfoInput[0],Integer.parseInt(CliamInfoInput[1]),Integer.parseInt(CliamInfoInput[2]),
						  Integer.parseInt(CliamInfoInput[3]),CliamInfoInput[4],CliamInfoInput[5],(CliamInfoInput[6].split(",")),CliamInfoInput[7],
						  Integer.parseInt(CliamInfoInput[8]),CliamInfoInput[9],(CliamInfoInput[10].split(",")),today,lastYear,Boolean.parseBoolean(CliamInfoInput[11]));
				  InputData.add(input);
			  }
		  }
	  }catch(Exception ee){
		  ee.printStackTrace();
	  }finally{
		  try{
			  br.close();
		  }catch(IOException ie){
			  System.out.println("Error occured while closing the BufferedReader");
			  ie.printStackTrace();
		  }
	  }
	  
    
	    /*Logger LOGGER = LoggerFactory.getLogger(Test.class);
	    try{*/
		/*List<ClaimInfo> inputData = Arrays.asList(
				new ClaimInfo("12345",35468,01,01,"58103","Part A" ,new String[]{"Medicare Part A", "Medicare Part B"},"Individual" ,
						00,"3/05/2017",new String[]{"10/04/2017", "12/07/2017", "15/03/2017", "16/06/2017"},today,lastYear,true),
				new ClaimInfo("145615",35468,01,01,"58103","Part B" ,new String[]{"Medicare Part A", "Medicare Part B","Stopped Working","Aged"},"Group" ,
						00,"3/06/2017",new String[]{"10/01/2017","19/01/2018","10/04/2017", "12/07/2017", "15/03/2017", "16/06/2017","15/05/2016","25/01/2018"},today,lastYear,true),
				new ClaimInfo("145616",35468,01,01,"58103","Part B" ,new String[]{"Medicare Part D", "Medicare Part B","Employeed"},"Group" ,
						00,"13/05/2017",new String[]{"10/01/2017","19/01/2018","10/04/2017", "12/07/2017", "15/03/2017", "16/06/2017"},today,lastYear,true),
				new ClaimInfo("145617",35468,01,01,"58103","Part B" ,new String[]{"Aged", "Medicare Part B","Employeed"},"Group" ,
						00,"23/06/2017",new String[]{"10/01/2017","19/01/2018","10/04/2017", "12/07/2017", "15/03/2017", "16/06/2017"},today,lastYear,true)
				
				);*/
	   
		
		/*Scanner while hasNext {
			csv.readline()
			list.add(new ClaimInfo(sc.[1], sc.[2]))
		}*/
		
		
		/*List<EventInfo> eventinfo = new ArrayList<EventInfo>();
		int i = 0;*/
        
		
		
		SparkConf conf = new SparkConf().setAppName("Phase-one Test").setMaster("local[2]").set("spark.executor.memory", "1g");
		
	    JavaSparkContext sc = new JavaSparkContext(conf);

	    KieBase rules = loadRules();
	    Broadcast<KieBase> broadcastRules = sc.broadcast(rules);

	    JavaRDD<ClaimInfo> claims = sc.parallelize(InputData);

	    long numApproved = claims.map( a -> applyRules(broadcastRules.value(), a) )
	                                 .filter( a -> a.isRecoverable() )
	                                 .count();

	    System.out.println("Number of Claims recoverable: " + numApproved);
	    
	    sc.close();
	   /* }catch(Exception e){
	    	LOGGER.error(e.getMessage(),e);
	    }*/
		
	}

	  public static KieBase loadRules() {
		  KieServices kieServices = KieServices.Factory.get();
		  Resource ruleFile = ResourceFactory.newFileResource("src/main/resources/main/rules.drl");
		//Resource ruleFile = ResourceFactory.newClassPathResource("/src/main/resources/main/KB-Rules.xls");
		 
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(ruleFile);
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		KieRepository kieRepository = kieServices.getRepository();
		ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
		KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
		return kieContainer.getKieBase();
	  }

	  public static ClaimInfo applyRules(KieBase base, ClaimInfo a) {
	    StatelessKieSession session = base.newStatelessKieSession();
	    session.execute(CommandFactory.newInsert(a));
	    return a;
	  }
	}


