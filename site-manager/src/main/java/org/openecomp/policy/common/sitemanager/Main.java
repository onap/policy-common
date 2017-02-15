/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.policy.common.sitemanager;

/*
 * Site Manager argument list:
 *
 * none - dump help information
 * show - dump information about all nodes 
 *		([site, nodetype, resourceName],
 *			adminState, opState, availStatus, standbyStatus)
 *			The first 3 determine the sort order.
 * setAdminState [ -s <site> | -r <resourceName> ] <new-state>
 * lock [ -s <site> | -r <resourceName> ]
 * unlock [ -s <site> | -r <resourceName> ]
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.Persistence;
import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.openecomp.policy.common.im.jpa.ResourceRegistrationEntity;
import org.openecomp.policy.common.im.jpa.StateManagementEntity;
import org.openecomp.policy.common.im.jmx.ComponentAdminMBean;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class contains the main entry point for Site Manager.
 */
public class Main
{
  // table mapping 'resourceName' to 'StateManagmentEntity'
  static HashMap<String, StateManagementEntity> stateManagementTable =
	new HashMap<String, StateManagementEntity>();

  // table mapping 'resourceName' to 'StateManagmentEntity'
  static HashMap<String, ResourceRegistrationEntity> resourceRegistrationTable =
	new HashMap<String, ResourceRegistrationEntity>();

  /**
   * Print out help information regarding command arguments.
   */
  private static void help()
  {
	System.out.print
	  ("Usage:\n"
	   + "    siteManager show [ -s <site> | -r <resourceName> ] :\n"
	   + "        display node information\n"
	   + "    siteManager setAdminState { -s <site> | -r <resourceName> }"
	   + " <new-state> :\n"
	   + "        update admin state on selected nodes\n"
	   + "    siteManager lock { -s <site> | -r <resourceName> } :\n"
	   + "        lock selected nodes\n"
	   + "    siteManager unlock { -s <site> | -r <resourceName> } :\n"
	   + "        unlock selected nodes\n");
  }

  /**
   * Print out help information regarding the properties file.
   *
   * @param propertiesFileName the path to the properties file
   */
  private static void helpProperties(String propertiesFileName)
  {
	if (propertiesFileName == null)
	  {
		// file name not specified (missing system property)
		System.out.print
		  ("'siteManager' needs to be passed the system property\n"
		   + "'siteManager.properties', which is the file name of a\n"
		   + "properties file containing database access information\n\n");
	  }
	else
	  {
		File file = new File(propertiesFileName);
		if (!file.exists())
		  {
			// file name specified, but does not exist
			System.out.print
			  ("Properties file '" + file.getAbsolutePath()
			   + "' does not exist.\n\n");
		  }
		else
		  {
			// file name specified and does exist -- presumably, the
			// problem is with one or more properties
			System.out.print
			  ("One or more missing properties in\n'" + file.getAbsolutePath()
			   + "'.\n\n");
		  }
	  }

	System.out.print
	  ("The following properties need to be specified:\n\n"
	   + "    javax.persistence.jdbc.driver -"
	   + " typically 'org.mariadb.jdbc.Driver'\n"
	   + "    javax.persistence.jdbc.url - URL referring to the database,\n"
	   + "        which typically has the form:"
	   + " 'jdbc:mariadb://<host>:<port>/<db>'\n"
	   + "        ('<db>' is probably 'xacml' in this case)\n"
	   + "    javax.persistence.jdbc.user - the user id for accessing the"
	   + " database\n"
	   + "    javax.persistence.jdbc.password - password for accessing the"
	   + " database\n");
  }

  /**
   * This is the main entry point
   *
   * @param args these are command-line arguments to 'siteManager'
   */
  public static void main(String... args)
  {
	Options options = new Options();
	options.addOption("s", true, "specify site");
	options.addOption("r", true, "specify resource name");
	options.addOption("h", false, "display help");
	options.addOption("?", false, "display help");

	// parse options
	CommandLineParser parser = new DefaultParser();
	CommandLine cmd = null;

	try
	  {
		cmd = parser.parse(options, args);
	  }
	catch (ParseException e)
	  {
		System.out.println(e.getMessage());
		help();
		System.exit(1);
	  }

	if (cmd.getOptionValue('h') != null || cmd.getOptionValue('?') != null)
	  {
		help();
		System.exit(0);
	  }

	// fetch options, and remaining arguments
	String sOption = cmd.getOptionValue('s');
	String rOption = cmd.getOptionValue('r');
	List<String> argList = cmd.getArgList();

	// a number of commands require either the '-r' option or '-s' option
	boolean optionLetterSpecified = (rOption != null || sOption != null);

	// used to accumulate any error messages that are generated
	StringBuilder error = new StringBuilder();

	// first non-option argument
	String arg0 = null;

	if (argList.size() == 0)
	  {
		error.append("No command specified\n");
	  }
	else
	  {
		arg0 = argList.get(0);
		if ("show".equalsIgnoreCase(arg0))
		  {
			// show [ -s <site> | -r <resourceName> ]
			if (argList.size() != 1)
			  {
				error.append("show: Extra arguments\n");
			  }
		  }
		else if ("setAdminState".equalsIgnoreCase(arg0))
		  {
			// setAdminState { -s <site> | -r <resourceName> } <new-state>
			switch (argList.size())
			  {
			  case 1:
				error.append("setAdminState: Missing <new-state> value\n");
				break;
			  case 2:
				// this is expected
				break;
			  default:
				error.append("setAdminState: Extra arguments\n");
				break;
			  }
			if (!optionLetterSpecified)
			  {
				error.append
				  ("setAdminState: Either '-s' or '-r' option is needed\n");
			  }
		  }
		else if ("lock".equalsIgnoreCase(arg0))
		  {
			// lock { -s <site> | -r <resourceName> }
			if (argList.size() != 1)
			  {
				error.append("lock: Extra arguments\n");
			  }
			if (!optionLetterSpecified)
			  {
				error.append("lock: Either '-s' or '-r' option is needed\n");
			  }
		  }
		else if ("unlock".equalsIgnoreCase(arg0))
		  {
			// unlock { -s <site> | -r <resourceName> }
			if (argList.size() != 1)
			  {
				error.append("unlock: Extra arguments\n");
			  }
			if (!optionLetterSpecified)
			  {
				error.append("unlock: Either '-s' or '-r' option is needed\n");
			  }
		  }
		else
		  {
			error.append(arg0).append(": Unknown command\n");
		  }
	  }
	if (sOption != null && rOption != null)
	  {
		error
		  .append(arg0)
		  .append(":  'r' and 's' options are mutually exclusive\n");
	  }
	if (error.length() != 0)
	  {
		// if any errors have occurred, dump out the error string,
		// help information, and exit
		System.out.println(error.toString());
		help();
		System.exit(2);
	  }

	// read in properties used to access the database
	String propertiesFileName = System.getProperty("siteManager.properties");
	File propertiesFile = null;

	if (propertiesFileName == null
		|| !(propertiesFile = new File(propertiesFileName)).exists())
	  {
		helpProperties(propertiesFileName);
		System.exit(3);
	  }
	FileInputStream fis = null;
	Properties properties = new Properties();
	try
	  {
		fis = new FileInputStream(propertiesFile);
		properties.load(fis);
	  }
	catch (Exception e)
	  {
		System.out.println("Exception loading properties: " + e);
		helpProperties(propertiesFileName);
		System.exit(3);
	  }
	finally
	  {
		try
		  {
			fis.close();
		  }
		catch (Exception e)
		  {
			// ignore exception
		  }
	  }

	// verify that we have all of the properties needed
	if (properties.getProperty("javax.persistence.jdbc.driver") == null
		|| properties.getProperty("javax.persistence.jdbc.url") == null
		|| properties.getProperty("javax.persistence.jdbc.user") == null
		|| properties.getProperty("javax.persistence.jdbc.password") == null)
	  {
		// one or more missing properties
		helpProperties(propertiesFileName);
		System.exit(3);
	  }

	// access database through 'EntityManager'
	EntityManagerFactory emf =
	  Persistence.createEntityManagerFactory("operationalPU", properties);
	EntityManager em = emf.createEntityManager();

	// sQuery - used for StateManagementEntity table
	// rQuery - used for ResourceRegistrationEntity table
	Query sQuery, rQuery;

	if (rOption != null)
	  {
		// 'resourceName' specified -- both queries are limited to this
		// resource
		sQuery = em.createQuery("SELECT s FROM StateManagementEntity s"
								+ " WHERE s.resourceName = :resourceName")
		  .setParameter("resourceName", rOption);
		rQuery = em.createQuery("SELECT r FROM ResourceRegistrationEntity r"
								+ " WHERE r.resourceName = :resourceName")
		  .setParameter("resourceName", rOption);
	  }
	else if (sOption != null)
	  {
		// 'site' is specified -- 'ResourceRegistrationEntity' has a 'site'
		// field, but 'StateManagementEntity' does not
		sQuery = em.createQuery("SELECT s FROM StateManagementEntity s");
		rQuery = em.createQuery("SELECT r FROM ResourceRegistrationEntity r"
								+ " WHERE r.site = :site")
		  .setParameter("site", sOption);
	  }
	else
	  {
		// query all entries
		sQuery = em.createQuery("SELECT s FROM StateManagementEntity s");
		rQuery = em.createQuery("SELECT r FROM ResourceRegistrationEntity r");
	  }

	// perform 'StateManagementEntity' query, and place matching entries
	// in 'stateManagementTable'
	for (Object o : sQuery.getResultList())
	  {
		if (o instanceof StateManagementEntity)
		  {
			StateManagementEntity s = (StateManagementEntity) o;
			stateManagementTable.put(s.getResourceName(), s);
		  }
	  }

	// perform 'ResourceRegistrationQuery', and place matching entries
	// in 'resourceRegistrationTable' ONLY if there is also an associated
	// 'stateManagementTable' entry
	for (Object o : rQuery.getResultList())
	  {
		if (o instanceof ResourceRegistrationEntity)
		  {
			ResourceRegistrationEntity r = (ResourceRegistrationEntity) o;
			String resourceName = r.getResourceName();
			if (stateManagementTable.get(resourceName) != null)
			  {
				// only include entries that have a corresponding
				// state table entry -- silently ignore the rest
				resourceRegistrationTable.put(resourceName, r);
			  }
		  }
	  }

	if (resourceRegistrationTable.size() == 0)
	  {
		System.out.println(arg0 + ": No matching entries");
		System.exit(4);
	  }

	if ("setAdminState".equalsIgnoreCase(arg0))
	  {
		// update admin state on all of the nodes
		String adminState = argList.get(1);
		EntityTransaction et = em.getTransaction();
		et.begin();
		try
		  {
			// iterate over all matching 'ResourceRegistrationEntity' instances
			for (ResourceRegistrationEntity r :
				   resourceRegistrationTable.values())
			  {
				// we know the corresponding 'StateManagementEntity' exists --
				// 'ResourceRegistrationEntity' entries without a matching
				// 'StateManagementEntity' entry were not placed in the table
				StateManagementEntity s =
				  stateManagementTable.get(r.getResourceName());

				// update the admin state, and save the changes
				s.setAdminState(adminState);
				em.persist(s);
			  }
		  }
		finally
		  {
			// do the commit
			em.flush();
			et.commit();
		  }
	  }
	else if ("lock".equalsIgnoreCase(arg0) || "unlock".equalsIgnoreCase(arg0))
	  {
		// these use the JMX interface
		for (ResourceRegistrationEntity r :
			   resourceRegistrationTable.values())
		  {
			// lock or unlock the entity
			jmxOp(arg0, r);

			// change should be reflected in 'adminState'
			em.refresh(stateManagementTable.get(r.getResourceName()));
		  }
	  }

	// free connection to the database
	em.close();

	// display all entries
	display();
  }

  /**
   * Process a 'lock' or 'unlock' operation on a single
   * 'ResourceRegistrationEntity'
   *
   * @param arg0 this is the string "lock" or "unlock"
   * @param r this is the ResourceRegistrationEntity to lock or unlock
   */
  static void jmxOp(String arg0, ResourceRegistrationEntity r)
  {
	String resourceName = r.getResourceName();
	String jmxUrl = r.getResourceUrl();
	if (jmxUrl == null)
	  {
		System.out.println(arg0 + ": no resource URL for '"
						   + resourceName + "'");
		return;
	  }

	JMXConnector connector = null;
	try
	  {
		connector = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl));
		ComponentAdminMBean admin = JMX.newMXBeanProxy
		  (connector.getMBeanServerConnection(),
		   new ObjectName("ECOMP_POLICY_COMP:name=" + resourceName),
		   ComponentAdminMBean.class);

		if ("lock".equals(arg0))
		  {
			admin.lock();
		  }
		else
		  {
			admin.unlock();
		  }
	  }
	catch (Exception e)
	  {
		// e.printStackTrace();
		System.out.println(arg0 + " failed for '" + resourceName + "': " + e);
	  }
	finally
	  {
		if (connector != null)
		  {
			try
			  {
				connector.close();
			  }
			catch (Exception e)
			  {
				// ignore any errors here
			  }
		  }
	  }
  }

  /**
   * Compare two strings, either of which may be null
   *
   * @param s1 the first string
   * @param s2 the second string
   * @return a negative value if s1<s2, 0 if they are equal,
   *	and positive if s1>s2
   */
  static private int stringCompare(String s1, String s2)
  {
	return ((s1 == null) ?
			(s2 == null ? 0 : -1) :
			(s2 == null ? 1 : s1.compareTo(s2)));
  }

  /**
   * Update an array of 'length' fields using an array of Strings, any of
   * which may be 'null'. This method is used to determine the field width
   * of each column in a tabular dump.
   *
   * @param current this is an array of length 7, containing the current
   *	maximum lengths of each column in the tabular dump
   * @param s this is an array of length 7, containing the current String
   *	entry for each column
   */
  static private void updateLengths(int[] current, String[] s)
  {
	for (int i = 0 ; i < 7 ; i += 1)
	  {
		String str = s[i];
		int newLength = (str == null ? 4 : str.length());
		if (current[i] < newLength)
		  {
			// this column needs to be expanded
			current[i] = newLength;
		  }
	  }
  }

  /**
   * Ordered display -- dump out all of the entries, in 
   */
  static void display()
  {
	TreeSet<String[]> treeset = new TreeSet<String[]>
	  (new Comparator<String[]>()
	  {
		public int compare(String[] r1, String[] r2)
		  {
			int rval = 0;

			// the first 3 columns are 'Site', 'NodeType', and 'ResourceName',
			// and are used to sort the entries
			for (int i = 0 ; i < 3 ; i += 1)
			  {
				if ((rval = stringCompare(r1[i], r2[i])) != 0)
				  break;
			  }
			return(rval);
		  }
	  });

	String[] labels = new String[]
	  {"Site", "NodeType", "ResourceName",
	   "AdminState", "OpState", "AvailStatus", "StandbyStatus"};
	String[] underlines = new String[]
	  {"----", "--------", "------------",
	   "----------", "-------", "-----------", "-------------"};

	// each column needs to be at least wide enough to fit the column label
	int lengths[] = new int[7];
	updateLengths(lengths, labels);

	// Go through the 'resourceRegistrationTable', and generate the
	// associated table row. Maximum column widths are updated, and the
	// entry is inserted into tree, which has the effect of sorting the
	// entries.
	for (ResourceRegistrationEntity r : resourceRegistrationTable.values())
	  {
		StateManagementEntity s =
		  stateManagementTable.get(r.getResourceName());

		// these are the entries to be displayed for this row
		String[] values = new String[]
		  {
			r.getSite(), r.getNodeType(), r.getResourceName(),
			s.getAdminState(), s.getOpState(),
			s.getAvailStatus(), s.getStandbyStatus()
		  };

		treeset.add(values);
		updateLengths(lengths, values);
	  }

	// generate format string
	StringBuilder sb = new StringBuilder();
	for (int i = 0 ; i < 7 ; i += 1)
	  {
		sb.append('%').append(i+1).append("$-")
		  .append(lengths[i]).append("s ");
	  }
	sb.setCharAt(sb.length()-1, '\n');
	String formatString = sb.toString();

	// display column headers
	System.out.printf(formatString, labels);
	System.out.printf(formatString, underlines);

	// display all of the rows
	for (String[] values : treeset)
	  {
		System.out.printf(formatString, values);
	  }
  }
}
