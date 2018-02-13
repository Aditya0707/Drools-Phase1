package TestClaim;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;

public class Test {

	public static void main(String[] args)throws Exception{

		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		System.out.println(today);
		cal.add(Calendar.YEAR, -1); // to get previous year add -1
		Date lastYear = cal.getTime();
		System.out.println(lastYear);
		try{


			//reading Data from SQL Server
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			System.out.println("JDBC Driver is Loaded");

			String server = "SEVEN\\SQLEXPRESS01";

			String dbName = "health_care_COB_stg";
			String jdbcUrl = "jdbc:sqlserver://"+server+";databaseName="+dbName+";integratedSecurity=true";
			Connection sql_Connnection = DriverManager.getConnection(jdbcUrl);
			System.out.println("Connection Established");
			Statement sql_Statement = sql_Connnection.createStatement();
			String query = "SELECT [EVENT_NAMES],[EVENT_START_DATES],[EVENT_END_DATES],[mbs_type],[mbs_id],[line_date]"
					+" ,[line_num],[primacy],[proc_medicare_covered],[pvd_zipcode],[part_type],[primary_member]"
					+",[emp_size_aged],[emp_size_disabled],[clm_id],[emp_tin]"
					+" FROM [health_care_COB_stg].[dbo].[VW_RECOVERABLE_UPDATED]"
					+" WHERE [EVENT_NAMES] IS NOT NULL";

			ResultSet sql_ResultSet = sql_Statement.executeQuery(query);
			
			//Create Spark Instance

			SparkConf conf = new SparkConf().setAppName("Phase-one Test").setMaster("local[2]").set("spark.executor.memory", "1g");

			JavaSparkContext sc = new JavaSparkContext(conf);

			KieBase rules = loadRules();

			Broadcast<KieBase> broadcastRules = sc.broadcast(rules);

			//Create List for Holding the Input CalimInfo Objects
			
			List<ClaimInfo> InputData = new ArrayList<ClaimInfo>();
			
			String claim_id;
			Date line_Date_Tst;
			int Line_Num ;
			int primacy_value;
			int procedure_covered;
			int provider_Zipcode ;
			String part_Type;
			String event_Names;
			String mbs_Type;
			String event_Start_Dates;
			long numApproved = 0;
			String event_End_Dates = null;

			//Map for Displaying the output from our rules.
			Map<String,Boolean> output = new HashMap<String,Boolean>();

			//get the input from SqlServer into our local variables

			while (sql_ResultSet.next()){

				claim_id = sql_ResultSet.getString("clm_id");

				line_Date_Tst  = sql_ResultSet.getDate("line_date");

				Line_Num = sql_ResultSet.getInt("line_num");

				primacy_value = sql_ResultSet.getInt("primacy");

				procedure_covered = sql_ResultSet.getInt("proc_medicare_covered");

				provider_Zipcode = sql_ResultSet.getInt("pvd_Zipcode");

				part_Type = sql_ResultSet.getString("part_type");

				event_Names = sql_ResultSet.getString("EVENT_NAMES");

				mbs_Type = sql_ResultSet.getString("mbs_type");

				event_Start_Dates = sql_ResultSet.getString("EVENT_START_DATES");

				event_End_Dates = sql_ResultSet.getString("EVENT_END_DATES");

				Boolean primary_Memeber = sql_ResultSet.getBoolean("primary_member");

				int employer_Size_Aged = sql_ResultSet.getInt("emp_size_aged");

				int employer_Size_Disabled = sql_ResultSet.getInt("emp_size_disabled");

				
				//Calling the ClaimInfo Constructor with the inputs that we have obtained from SqlServer		 
				InputData = Arrays.asList(new ClaimInfo(claim_id, Line_Num, primacy_value, procedure_covered, provider_Zipcode,
						part_Type, event_Names.split(","), mbs_Type, employer_Size_Aged,employer_Size_Disabled, line_Date_Tst,
						event_Start_Dates.split(","),event_End_Dates.split(","), today,lastYear,primary_Memeber));
				
				/* InputData.add(new ClaimInfo(claim_id, Line_Num, primacy_value, procedure_covered, provider_Zipcode,
						 part_Type, event_Names.split(","), mbs_Type, employer_Size_Aged,employer_Size_Disabled, line_Date_Tst,
						 event_Start_Dates.split(","),event_End_Dates.split(","), today,lastYear,primary_Memeber));*/

				//create the Java RDD with the claimInfo object that we have created 

				JavaRDD<ClaimInfo> claims = sc.parallelize(InputData);
				
				//Broadcasting the RULES to all the nodes and 
				//executing the rules on the chunks of data in those nodes

				numApproved += claims.map( a -> applyRules(broadcastRules.value(), a) )
						.filter( a -> ClaimInfo.isRecoverable() )
						.count();
				
				String claim_Id = InputData.get(0).getClm_id();
				
				//Inserting the claims values into our output HashMap
				output.put(claim_Id, ClaimInfo.isRecoverable());


				// inserting the Claim id and recoverability values into the table in sql server

				String query2 = "insert into [health_care_COB_stg].[dbo].[Claims_Recovery_Info] "
						+ "(CLAIM_ID, RECOVERABLE, recent_TimeStamp) values (?,?,?)";
				
				PreparedStatement stmt = sql_Connnection.prepareStatement(query2);
				stmt.setString(1, claim_Id);
				stmt.setBoolean(2, ClaimInfo.isRecoverable());
				stmt.setTimestamp(3,new java.sql.Timestamp(today.getTime()));
				stmt.executeUpdate();
			
			}


			//Displaying our Output from the HashMap

			for(Map.Entry<String, Boolean> entry : output.entrySet()){

				if(entry.getValue() == true){
					System.out.println("Claim Id : "+entry.getKey()+" is recoverable");

				}else{
					System.out.println("Claim Id : "+entry.getKey()+" is not recoverable");
				}

			}
			//Displaying the total number of claims approved

			System.out.println("Number of Claims recoverable: " + numApproved);

			sc.close();
		}catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(2);
		}
	}


	//Method for Loading the Rules
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
	// Method for applying the rules to the Input Data
	public static ClaimInfo applyRules(KieBase base, ClaimInfo a) {
		StatelessKieSession session = base.newStatelessKieSession();
		session.execute(CommandFactory.newInsert(a));
		return a;
	}
}


