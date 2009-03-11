package org.example.osgi.reader.gui;

import java.awt.Component;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.MailboxListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class ReaderMailboxTracker extends ServiceTracker {

	private final JTabbedPane tabbedPane;
	private final Component placeholder;

	public ReaderMailboxTracker(BundleContext ctx, JTabbedPane tabbedPane, Component placeholder) {
		super(ctx, Mailbox.class.getName(), null);
		this.tabbedPane = tabbedPane;
		this.placeholder = placeholder;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		final String mboxName = (String) reference
				.getProperty(Mailbox.NAME_PROPERTY);
		final Mailbox mbox = (Mailbox) context.getService(reference);

		Callable<Pair<MailboxPanel,ServiceRegistration>> callable = new Callable<Pair<MailboxPanel,ServiceRegistration>>() {
			public Pair<MailboxPanel,ServiceRegistration> call() {
				Pair<MailboxPanel,ServiceRegistration> pair;
				try {
					MailboxPanel panel = new MailboxPanel(mbox);
					String title = (mboxName != null) ? mboxName : "<unknown>";
					tabbedPane.addTab(title, panel);
					tabbedPane.remove(placeholder);
					
					Properties props = new Properties();
					props.put(Mailbox.NAME_PROPERTY, title);
					ServiceRegistration registration = context.registerService(MailboxListener.class.getName(), panel.getTableModel(), props);
					pair = new Pair<MailboxPanel, ServiceRegistration>(panel,registration);
				} catch (MailboxException e) {
					JOptionPane.showMessageDialog(tabbedPane, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					pair = null;
				}
				return pair;
			}
		};
		FutureTask<Pair<MailboxPanel,ServiceRegistration>> future = new FutureTask<Pair<MailboxPanel,ServiceRegistration>>(callable);
		SwingUtilities.invokeLater(future);

		return future;
	}

	@Override
	public void removedService(ServiceReference reference, Object svc) {

		@SuppressWarnings("unchecked")
		final Future<Pair<MailboxPanel,ServiceRegistration>> panelRef = (Future<Pair<MailboxPanel,ServiceRegistration>>) svc;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Pair<MailboxPanel, ServiceRegistration> pair = panelRef.get();
					if (pair != null) {
						tabbedPane.remove(pair.getA());
						if(tabbedPane.getTabCount() == 0) {
							tabbedPane.add("Mailbox Reader", placeholder);
						}
						try {
							pair.getB().unregister();
						} catch (IllegalStateException e) {
							// The service was likely already unregistered by shutdown of the bundle
						}
					}
				} catch (ExecutionException e) {
					// The MailboxPanel was not successfully created
				} catch (InterruptedException e) {
					// Restore interruption status
					Thread.currentThread().interrupt();
				}
			}
		});

		context.ungetService(reference);
	}
}
