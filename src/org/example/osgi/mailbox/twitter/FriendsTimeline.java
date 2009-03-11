/*******************************************************************************
 * Copyright (c) 2009 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package org.example.osgi.mailbox.twitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.example.osgi.mailbox.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Neil Bartlett
 *
 */
public class FriendsTimeline implements ITimeline {
	
	private Logger log = LoggerFactory.getLogger(FriendsTimeline.class);
	
	private Twitter twitter;
	
	protected void activate(Map props) {
		String username = (String) props.get("username");
		String password = (String) props.get("password");
		
		twitter = new Twitter(username, password);
	}
	
	protected void deactivate() {
	}


	public long getInitialTimeline(Collection<? super Message> into) throws TwitterException {
		long highestStatusSeen = 0;
		
		List<Status> statuses = twitter.getFriendsTimeline();
		for (Status status : statuses) {
			long id = status.getId();
			
			highestStatusSeen = (id > highestStatusSeen) ? id : highestStatusSeen;
			into.add(new TweetMessage(status));
		}
		
		return highestStatusSeen;
	}
	
	public long getTimelineSinceId(long sinceId, Collection<? super Message> into) throws TwitterException {
		long highestStatusSeen = 0;
		
		int page = 1;
		boolean done = false;
		
		while(!done) {
			List<Status> statuses = twitter.getFriendsTimelineByPage(page++);
			for (Status status : statuses) {
				long id = status.getId();
				
				highestStatusSeen = (id > highestStatusSeen) ? id : highestStatusSeen;
				if(id > sinceId) {
					into.add(new TweetMessage(status));
				} else {
					done = true;
					break;
				}
			}
		}
		
		return highestStatusSeen;
	}
}
