package com.dynatrace.diagnostics.plugins;

// Based on OraclePlugin 1.0.7  
// Created by Eric Burns (eric.burns@compuware.com) Sales Engineer, Compuware

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;
import java.sql.*;
 
import com.ibm.db2.jcc.*; 	// IBM Data Server Driver for 
							// JDBC and SQLJ 
							// implementation of JDBC 
							// standard extension APIs

import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;

public class DB2Plugin implements Monitor {
	
	
	// Data sent to use on how to connect:
	private String CONFIG_DB_NAME = "dbName";

	private String CONFIG_DB_USERNAME = "dbUsername";

	private String CONFIG_DB_PASSWORD = "dbPassword";

	private String CONFIG_PORT = "dbPort";

	// Data we collect:
	private String SESSION_METRIC_GROUP = "DB2 Sessions";

	private String SYSTEM_METRIC_GROUP = "DB2 System";
	
	private String LOCK_METRIC_GROUP = "Lock Metrics";
	
	public String CONNECTION_TIME = "Connection Time";

	public String TOTAL_USERS = "Total Users";

	public String SYNCHRONOUS_READ_PERCENT = "Synchronous Read Percent";

	public String ASYNCHRONOUS_WRITE_PERCENT = "Asynchronous Write Percent";
	
	public String INDEX_USAGE = "Index Usage";
	
	public String DEADLOCKS = "Deadlocks";

	public String WAITLOCKS = "Waitlocks";
	
	// Variables related to connection:
	private java.sql.Connection con = null;

	private String url = "jdbc:db2"; // "jdbc:oracle:thin";

	private String host;

	private String dbService;

	private String username;

	private String password;

	private String port;

	// And obviously logging . . . 
	private final Logger log = Logger.getLogger(DB2Plugin.class.getName());

	private String getConnectionUrl() {
		
		if (url.equals("jdbc:db2")) {

			return url + ":" + "//" + host + ":" + port + "/" + dbService;

		}
		//else if (url.equals("jdbc:jtds:sqlserver")) {

			//return url + ":" + "//@" + host + ":" + port + ":" + dbService;
		//}
		return new String("");
	}

	@Override
	public com.dynatrace.diagnostics.pdk.Status setup(MonitorEnvironment env) throws java.lang.Exception {

		Status stat = new Status();

		log.fine("Inside setup method ...");

		host = env.getHost().getAddress();
		dbService = env.getConfigString(CONFIG_DB_NAME);
		username = env.getConfigString(CONFIG_DB_USERNAME);
		port = (env.getConfigString(CONFIG_PORT) == null) ? "50001" : env.getConfigString(CONFIG_PORT);
		password = env.getConfigPassword(CONFIG_DB_PASSWORD);

		try {
			log.info("Connecting to DB2, connection string is ... " + getConnectionUrl());
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			con = java.sql.DriverManager.getConnection( getConnectionUrl(), username, password );
		}
		catch (ClassNotFoundException ex) {
			log.log(Level.WARNING, "could not load driver class.", ex);
			stat.setStatusCode(Status.StatusCode.ErrorTargetService);
			stat.setShortMessage("Database Connecting failed");
			stat.setMessage("ClassNotFound Exception");
			stat.setException(ex);
			return stat;
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "could not connect.", ex);
			stat.setStatusCode(Status.StatusCode.ErrorTargetService);
			stat.setShortMessage("Database Connecting failed");
			stat.setMessage("SQLException Exception");
			stat.setException(ex);
			return stat;
		}

		return new Status();
	}

	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
		Status stat = new Status();

		if (con.isClosed()) {
			log.info("Connection appears to be lost, trying to reconnect...");
			//con = java.sql.DriverManager.getConnection(getConnectionUrl(), username, password);
			con = DriverManager.getConnection( getConnectionUrl(), username, password );
		}

		log.fine("Inside execute method ...");

		// These actually get the data we want:
		populateSystemInfo(env);
		populateSessionInfo(env);
		populateSynchronousReadPercent(env);
		populateAsynchronousWritePercent(env);
		populateIndexUsage(env);
		populateLockData(env);
		
		return stat;
	}

	public void populateSystemInfo(MonitorEnvironment env) throws Exception {

		Collection<MonitorMeasure> measures = null;
		double timeBefore = 0;
		double timeAfter = 0;
		double totalConnectionTime = 0;
		java.sql.Connection timerCon = null;

		log.fine("Inside populateSystemInfo method ...");
		
		try {
			log.fine("Connecting to DB2 ...");
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			timeBefore = new GregorianCalendar().getTimeInMillis();
			timerCon = java.sql.DriverManager.getConnection(getConnectionUrl(), username, password);
			timeAfter = new GregorianCalendar().getTimeInMillis();
			totalConnectionTime = timeAfter - timeBefore;
			if ((measures = env.getMonitorMeasures(SYSTEM_METRIC_GROUP, CONNECTION_TIME)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating CONNECTION_TIME ... ");
					measure.setValue(totalConnectionTime);
				}
			} 
		}
		catch (ClassNotFoundException ex) {
			log.log(Level.WARNING, "could not load driver class.", ex);
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "could not connect to database.", ex);
		}

		log.fine("Closing database connection ...");
		timerCon.close();

	}
	
	// change to private for this and others
	public void populateSessionInfo(MonitorEnvironment env) throws Exception {
		String total_users_statement = "SELECT COUNT(*) FROM sysibmadm.applications";
		ResultSet sessionResult = null;
		Collection<MonitorMeasure> measures;
		Statement st = null;

		log.fine("Inside populateSessionInfo method ...");

		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(total_users_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(SESSION_METRIC_GROUP, TOTAL_USERS)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating TOTAL_USERS ... ");
					measure.setValue(sessionResult.getDouble("1"));
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
	}

	// Synchronous Read Percent from
	// http://www.dbisoftware.com/blog/db2_performance.php?id=96
	// Seems to return NULL when there has been no activity - need to test
	public void populateSynchronousReadPercent(MonitorEnvironment env) throws Exception {
		String calculate_SRP_statement = "select 100 - (((pool_async_data_reads + pool_async_index_reads) * 100 ) " +
						" / (pool_data_p_reads + pool_index_p_reads + 1)) as SRP " +
						" from sysibmadm.snapdb where DB_NAME = '" + dbService + "'"; //‘DBNAME’ 
	
	
		ResultSet sessionResult = null;
		Collection<MonitorMeasure> measures;
		Statement st = null;

		log.fine("Inside populateSyncronousReadPercent method ...");
		log.info("Doing SRP - query string is ... " + calculate_SRP_statement );
		
		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(calculate_SRP_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(SESSION_METRIC_GROUP, SYNCHRONOUS_READ_PERCENT)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating SYNCHRONOUS_READ_PERCENT ... ");
					measure.setValue(sessionResult.getDouble("SRP"));
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
	}
	
	// Asynchronous Write Percent from
	// http://www.dbisoftware.com/blog/db2_performance.php?id=117
	// Seems to return NULL when there has been no activity - need to test
	public void populateAsynchronousWritePercent(MonitorEnvironment env) throws Exception {
		String calculate_AWP_statement = " SELECT (((pool_async_data_writes + pool_async_index_writes) * 100 ) / " +
					"(pool_data_writes + pool_index_writes + 1)) " +
					" AS AWP FROM sysibmadm.snapdb WHERE " + 
					" db_name = '" + dbService + "'";  
	
		ResultSet sessionResult = null;
		Collection<MonitorMeasure> measures;
		Statement st = null;

		log.fine("Inside populateAsyncronousWritePercent method ...");
		log.info("Doing SRP - query string is ... " + calculate_AWP_statement );
		
		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(calculate_AWP_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(SESSION_METRIC_GROUP, ASYNCHRONOUS_WRITE_PERCENT)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating ASYNCHRONOUS_WRITE_PERCENT ... ");
					measure.setValue(sessionResult.getDouble("AWP"));
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
	}
	
	// Index Usage - percent of queries using an index
	public void populateIndexUsage(MonitorEnvironment env) throws Exception {
		String calculate_IU_statement = " 	SELECT rows_read, (rows_selected + rows_inserted + rows_updated + rows_deleted) " +
								" FROM sysibmadm.snapdb ";

		ResultSet sessionResult = null;
		Collection<MonitorMeasure> measures;
		Statement st = null;

		log.fine("Inside populateIndexUsagePercent method ...");
		log.info("Doing IU - query string is ... " + calculate_IU_statement );
		
		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(calculate_IU_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(SESSION_METRIC_GROUP, INDEX_USAGE)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating INDEX_USAGE ... ");
					double rows_read = sessionResult.getDouble("ROWS_READ");
					double rows_selected = sessionResult.getDouble("2");
					double results = (rows_selected / rows_read) * 100;
					//measure.setValue(sessionResult.getDouble("ROWS_READ"));
					measure.setValue(results);
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
	}    

	// Lock Data - 
	public void populateLockData(MonitorEnvironment env) throws Exception {
	
		log.fine("Inside populateLockData method ...");
		
		String get_deadlocks_statement = "SELECT deadlocks FROM sysibmadm.snapdb ";

		ResultSet sessionResult = null;
		Collection<MonitorMeasure> measures;
		Statement st = null;
		
		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(get_deadlocks_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(LOCK_METRIC_GROUP, DEADLOCKS)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating DEADLOCKS ... ");
					measure.setValue(sessionResult.getDouble("DEADLOCKS"));
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
		// Can we get a time delta to do deadlocks_per_second?

		String get_lockwaits_statement = "SELECT lock_waits FROM sysibmadm.snapdb";
		try {
			st = con.createStatement();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
		}
		try {
			sessionResult = st.executeQuery(get_lockwaits_statement);
			sessionResult.next();

			if ((measures = env.getMonitorMeasures(LOCK_METRIC_GROUP, WAITLOCKS)) != null) {
				for (MonitorMeasure measure : measures) {
					log.fine("Populating WAITLOCKS ... ");
					measure.setValue(sessionResult.getDouble("LOCK_WAITS"));
				}
			}
			st.close();
		}
		catch (SQLException ex) {
			log.log(Level.WARNING, "db read failed.", ex);
			st.close();
		}
		
	}    
    

	/*  Bufferpool Hit Ratio
	  http://dbaspot.com/ibm-db2/123280-bufferpool-hit-ratio.html says:
	 
	 	select distinct BP_NAME,
		NPAGES,
		PAGESIZE,
		decimal((bigint(npages)*bigint(pagesize)/(1024*1024))),

		decimal(1-((decimal(pool_data_p_reads)+decimal(pool_index_p_reads))/

		(1+decimal(pool_data_l_reads)+decimal(pool_index_l_reads))),20,5)*100
		as "BP Hit Ratio"
		from table( snapshot_bp( '', -1 ))
		as snapshot_database inner join syscat.bufferpools bp
		on bp.BPNAME = snapshot_database.BP_NAME

		Make sure you update '' with your database's name.
	 
	 
	 Also found:
	 SELECT 
          bp_name,
          pool_data_p_reads, pool_index_p_reads,
          pool_data_l_reads, pool_index_l_reads
        FROM 
          TABLE( snapshot_bp( 'THISDATABASE', -1 ))
        AS 
          snap
        INNER JOIN
          syscat.bufferpools sbp
        ON
          sbp.bpname = snap.bp_name
          
     */     
     
				
	@Override
	public void teardown(MonitorEnvironment env) throws Exception {
		log.fine("Exiting DB2 Monitor Plugin ... ");
		con.close();
	}
}
