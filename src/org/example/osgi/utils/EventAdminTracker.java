package org.example.osgi.utils;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class EventAdminTracker extends ServiceTracker
                               implements EventAdmin {

	public EventAdminTracker(BundleContext context) {
		super(context, EventAdmin.class.getName(), null);
	}
	public void sendEvent(Event event) {
		EventAdmin ea = (EventAdmin) getService();
		if(ea != null) ea.sendEvent(event);
	}
	public void postEvent(Event event) {
		EventAdmin ea = (EventAdmin) getService();
		if(ea != null) ea.postEvent(event);
	}
}