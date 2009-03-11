package org.example.osgi.mailbox.growable;

import java.util.Date;
import java.util.Properties;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.utils.WhiteboardHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class GrowableMailboxActivator implements BundleActivator {

	private static final String MAILBOX_NAME = "growing";
	private WhiteboardHelper<MailboxListener> whiteboard;
	private GrowableMailbox mailbox;
	private Thread messageAdderThread;
	private ServiceRegistration svcReg;

	public void start(BundleContext context) throws Exception {
		String mboxNameFilter = String.format("(%s=%s)", Mailbox.NAME_PROPERTY, MAILBOX_NAME);
		
		whiteboard = new WhiteboardHelper<MailboxListener>(context,
				MailboxListener.class, mboxNameFilter);
		whiteboard.open(true);

		mailbox = new GrowableMailbox(whiteboard, MAILBOX_NAME,
				new String[0][0]);
		messageAdderThread = new Thread(new MessageAdder());
		messageAdderThread.start();

		Properties props = new Properties();
		props.put(Mailbox.NAME_PROPERTY, MAILBOX_NAME);
		svcReg = context.registerService(Mailbox.class.getName(), mailbox,
				props);
	}

	public void stop(BundleContext context) throws Exception {
		svcReg.unregister();
		messageAdderThread.interrupt();
		whiteboard.close();
	}

	private class MessageAdder implements Runnable {
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					mailbox.addMessage("Message added at " + new Date(),
							"Hello again");
					Thread.sleep(5000);
				}
			} catch (InterruptedException e) {
				// Exit quietly
			}
		}
	}
}
