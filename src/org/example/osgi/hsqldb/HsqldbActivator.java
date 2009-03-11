package org.example.osgi.hsqldb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hsqldb.DatabaseURL;
import org.hsqldb.jdbc.jdbcConnection;
import org.hsqldb.persist.HsqlProperties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class HsqldbActivator implements BundleActivator {
	
	private static final String JDBC_URL = "jdbc:hsqldb:file:hsqldb/messages";
	private static final String CREATE_TABLE =
		"CREATE TEXT TABLE messages (" +
		"	id      BIGINT    GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
		"	subject VARCHAR   NOT NULL, " +
		"	content VARCHAR   NOT NULL" +
		")";
	private static final String SET_TABLE_SOURCE = "SET TABLE messages SOURCE \"messages.csv\"";
	private static final String PROP_DBNAME = "dbname";
	
	private jdbcConnection connection;
	private ServiceRegistration reg;

	public void start(BundleContext context) throws Exception {
		boolean isNew = false;
		
		// Create the connection
		HsqlProperties props = DatabaseURL.parseURL(JDBC_URL, true);
		connection = new jdbcConnection(props);
		
		// Initialize database and table
		Statement statement = connection.createStatement();
		try {
			statement.executeUpdate(CREATE_TABLE);
			isNew = true;
		} catch (SQLException e) {
			// Ignore: probably table already exists
		}
		
		// Set the table source file
		statement.executeUpdate(SET_TABLE_SOURCE);
		statement.close();
		
		// If the table is new, add an initial record
		if(isNew) {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO messages (subject, content) VALUES (?, ?)");
			preparedStatement.setString(1, "Hello");
			preparedStatement.setString(2, "Welcome to OSGi");
			preparedStatement.execute();
		}
		
		// Register service
		Properties svcProps = new Properties();
		svcProps.put(PROP_DBNAME, "hsqldb_messages");
		reg = context.registerService(Connection.class.getName(), connection, svcProps);
	}

	public void stop(BundleContext context) throws Exception {
		// Unregister the service
		reg.unregister();
		
		// Shutdown the database
		connection.createStatement().executeUpdate("SHUTDOWN");
		connection.close();
	}

}