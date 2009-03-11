package org.example.osgi.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class WhiteboardHelper<T> extends ServiceTracker {

	public WhiteboardHelper(BundleContext context, Class<T> clazz) {
		super(context, clazz.getName(), null);
	}
	
	public WhiteboardHelper(BundleContext context, Class<T> clazz, String filterStr) throws InvalidSyntaxException {
		super(context, buildFilter(clazz, filterStr), null);
	}
	
	private static Filter buildFilter(Class<?> clazz, String filterStr) throws InvalidSyntaxException {
		String completedFilter = String.format("(&(%s=%s)%s)", Constants.OBJECTCLASS, clazz.getName(), filterStr);
		System.out.println(completedFilter);
		return FrameworkUtil.createFilter(completedFilter);
	}

	public void accept(Visitor<? super T> visitor) {
		Object[] svcs = getServices();
		for (int i = 0; svcs != null && i < svcs.length; i++) {
			@SuppressWarnings("unchecked") T service = (T) svcs[i];
			visitor.visit(service);
		}
	}
}