package org.example.osgi.mailbox.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.mailbox.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.TwitterException;

public class TwitterMailbox implements Mailbox, Runnable {
	
	private final Logger log = LoggerFactory.getLogger(TwitterMailbox.class);
	
	private final TreeMap<Long, Message> tweets = new TreeMap<Long, Message>();
	private final List<MailboxListener> listeners = Collections.synchronizedList(new LinkedList<MailboxListener>());
	
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
		
		log.info("Starting TwitterMailbox thread");
		try {
			while(!Thread.interrupted()) {
				// Get timeline
				messages.clear();
				if(lastId == -1) {
					lastId = timeline.getInitialTimeline(messages);
				} else {
					lastId = timeline.getTimelineSinceId(lastId, messages);
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
				
				log.info("Sleeping for 100 seconds");
				Thread.sleep(100000);
			}
		} catch (TwitterException e) {
			log.error("Twitter error", e);
		} catch (InterruptedException e) {
			log.debug("Interrupted");
		} finally {
			log.info("TwitterMailbox thread exiting");
		}
	}
	
}
