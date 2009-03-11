package org.example.osgi.mailbox.db;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DBMailboxActivator implements BundleActivator {

	private DBMailboxTracker tracker;

	public void start(BundleContext context) throws Exception {
		tracker = new DBMailboxTracker(context);
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}

}
