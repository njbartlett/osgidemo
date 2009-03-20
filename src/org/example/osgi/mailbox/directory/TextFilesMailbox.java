package org.example.osgi.mailbox.directory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.mailbox.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFilesMailbox implements Mailbox {

	private final Logger log = LoggerFactory.getLogger(TextFilesMailbox.class);

	private static final String PROP_DIRECTORY = "directory";
	private static final String PROP_NAME = "name";
	private static final String DEFAULT_NAME = "TextFilesMailbox";

	private static final String TEXT_SUFFIX = ".txt";
	private static final String TEXT_MIME_TYPE = "text/plain";

	// Configured state
	private String name;
	private File directory;

	// Live state
	private Thread thread;
	private long nextId = 0;
	private final Set<File> monitoredFiles = new HashSet<File>();
	private final SortedMap<Long, FileMessage> messages = new TreeMap<Long, FileMessage>();
	
	private final List<MailboxListener> listeners = Collections
			.synchronizedList(new LinkedList<MailboxListener>());

	// Bind/unbind
	protected void addMailboxListener(MailboxListener l) {
		listeners.add(l);
	}

	protected void removeMailboxListener(MailboxListener l) {
		listeners.remove(l);
	}

	// Lifecycle Methods
	protected void activate(Map<String, Object> properties)
			throws MailboxException {
		log.info("Activating");
		// Name property
		String nameTmp = (String) properties.get(PROP_NAME);
		name = (nameTmp != null) ? nameTmp : DEFAULT_NAME;

		// Directory property
		String dirName = (String) properties.get(PROP_DIRECTORY);
		if (dirName == null) {
			throw new MailboxException("Missing 'directory' property.");
		}
		directory = new File(dirName);
		if (!directory.isDirectory()) {
			throw new MailboxException(
					"Specified directory does not exist or is not a directory ("
							+ dirName + ").");
		}

		// Start the monitor thread
		thread = new MonitorThread();
		thread.start();
	}

	protected void deactivate() {
		thread.interrupt();
	}

	public synchronized long[] getAllMessages() throws MailboxException {
		return setToArray(messages.keySet());
	}

	public synchronized Message[] getMessages(long[] ids) throws MailboxException {
		Message[] result = new Message[ids.length];
		for (int i = 0; i < ids.length; i++) {
			result[i] = messages.get(ids[i]);
		}
		return result;
	}

	public synchronized long[] getMessagesSince(long id) throws MailboxException {
		Map<Long,FileMessage> tailMap = messages.tailMap(id+1);
		return setToArray(tailMap.keySet());
	}

	private long[] setToArray(Set<Long> set) {
		long[] ids = new long[set.size()];
		int i=0;
		for(Iterator<Long> iter = set.iterator(); iter.hasNext(); i++) {
			ids[i] = iter.next();
		}
		return ids;
	}

	public void markRead(boolean read, long[] ids) throws MailboxException {
		// Ignore
	}

	protected void fireMessagesAdded(long[] ids) {
		MailboxListener[] listenerArray = (MailboxListener[]) listeners
				.toArray(new MailboxListener[listeners.size()]);
		for (MailboxListener listener : listenerArray) {
			listener.messagesArrived(name, this, ids);
		}
	}

	private class MonitorThread extends Thread {
		@Override
		public void run() {
			try {
				log.info("Starting monitor thread");
				while (!Thread.interrupted()) {
					// Sleep
					Thread.sleep(2000);
					
					// Scan the directory
					File[] fileList = directory.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(TEXT_SUFFIX);
						}
					});
					
					if (fileList != null && fileList.length > 0) {
						// Work out which files are new
						long lowestId;
						long currentId;
						synchronized (TextFilesMailbox.this) {
							lowestId = nextId; currentId = lowestId;
							for (File file : fileList) {
								boolean isNew = monitoredFiles.add(file);
								if(isNew) {
									currentId = nextId++;
									messages.put(currentId, new FileMessage(currentId,
											file, TEXT_MIME_TYPE));
								}
							}
						}
						
						// Notify the listeners
						if(currentId > lowestId) {
							long[] ids = new long[(int) (currentId - lowestId)];
							for(int i=0; i<ids.length; i++) {
								ids[i] = lowestId + i; 
							}
							fireMessagesAdded(ids);
						}
					}
					
				}
			} catch (InterruptedException e) {
				// Ignore
			} finally {
				log.info("Monitor thread exiting");
			}
		}
	}
}
