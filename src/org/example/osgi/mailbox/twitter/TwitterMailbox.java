package org.example.osgi.mailbox.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.mailbox.api.Message;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.TwitterException;

public class TwitterMailbox implements Mailbox, Runnable {
	
	private final TreeMap<Long, Message> tweets = new TreeMap<Long, Message>();
	private final List<MailboxListener> listeners = Collections.synchronizedList(new LinkedList<MailboxListener>());
	private final AtomicReference<LogService> logRef = new AtomicReference<LogService>(null);
	
	private ITimeline timeline;
	private String mailboxName;
	private Thread thread;

	protected final void setTimeline(ITimeline timeline) {
		this.timeline = timeline;
	}
	
	protected final void unsetTimeline(ITimeline timeline) {
		// Ignore
	}
	
	protected final void addListener(MailboxListener listener) {
		listeners.add(listener);
	}
	
	protected final void removeListener(MailboxListener listener) {
		listeners.remove(listener);
	}
	
	protected final void setLog(LogService log) {
		logRef.set(log);
	}
	
	protected final void unsetLog(LogService log) {
		logRef.compareAndSet(log, null);
	}
	
	// LIFECYCLE
	protected void activate(Map props) {
		mailboxName = (String) props.get(Mailbox.NAME_PROPERTY);
		thread = new Thread(this);
		thread.start();
	}
	
	protected void deactivate() {
		thread.interrupt();
	}
	
	// MESSAGE INTERFACE IMPLEMENTATION METHODS
	public synchronized long[] getAllMessages() throws MailboxException {
		return toLongArray(tweets.keySet());
	}

	public synchronized Message[] getMessages(long[] ids) throws MailboxException {
		Message[] messages = new Message[ids.length];
		for (int i = 0; i < ids.length; i++) {
			messages[i] = tweets.get(ids[i]);
		}
		
		return messages;
	}

	public long[] getMessagesSince(long id) throws MailboxException {
		SortedMap<Long, Message> tail = tweets.tailMap(id+1);
		return toLongArray(tail.keySet());
	}

	public void markRead(boolean read, long[] ids) throws MailboxException {
		// Ignore
	}
	
	private static long[] toLongArray(Collection<Long> collection) {
		long[] result = new long[collection.size()];
		int i=0; for (Long id : collection) {
			result[i++] = id.longValue();
		}
		return result;
	}

	
	// TWITTER POLLING THREAD
	public void run() {
		long lastId = -1;
		List<Message> messages = new ArrayList<Message>(20);
		
		log(LogService.LOG_INFO, "Starting TwitterMailbox thread", null);
		try {
			while(!Thread.interrupted()) {
				// Get timeline
				messages.clear();
				try {
					if(lastId == -1) {
						lastId = timeline.getInitialTimeline(messages);
					} else {
						lastId = timeline.getTimelineSinceId(lastId, messages);
					}
				} catch (TwitterException e) {
					log(LogService.LOG_ERROR, "Error querying Twitter timeline", e);
				}
				
				// Add statuses to the map
				long[] newIds = new long[messages.size()];
				synchronized(this) {
					for(int i = 0; i < messages.size(); i++) {
						Message message = messages.get(i);
						newIds[i] = message.getId();
						tweets.put(message.getId(), message);
					}
				}
				
				Object[] listeners = this.listeners.toArray();
				for (Object listener : listeners) {
					((MailboxListener) listener).messagesArrived(mailboxName, this, newIds);
				}
				
				log(LogService.LOG_DEBUG, "Sleeping for 100 seconds", null);
				Thread.sleep(100000);
			}
		} catch (InterruptedException e) {
			log(LogService.LOG_DEBUG, "Interrupted", null);
		} finally {
			log(LogService.LOG_INFO, "TwitterMailbox thread exiting", null);
		}
	}
	
	private void log(int level, String message, Throwable t) {
		LogService log = logRef.get();
		if(log != null) {
			log.log(level, message, t);
		}
	}
	
}
