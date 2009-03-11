package org.example.osgi.mailbox.db;

import java.sql.Connection;
import java.util.Properties;

import org.example.osgi.mailbox.api.Mailbox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;


public class DBMailboxTracker extends ServiceTracker {
	public DBMailboxTracker(BundleContext context) {
		super(context, Connection.class.getName(), null);
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		Connection connection = (Connection) context.getService(reference);
		DBMailbox mailbox = new DBMailbox(connection);
		
		Properties props = new Properties();
		props.put(Mailbox.NAME_PROPERTY, reference.getProperty("dbname"));
		
		ServiceRegistration reg = context.registerService(Mailbox.class.getName(), mailbox, props);
		return reg;
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		ServiceRegistration reg = (ServiceRegistration) service;
		reg.unregister();
		context.ungetService(reference);
	}
}
