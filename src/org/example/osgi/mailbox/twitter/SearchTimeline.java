package org.example.osgi.mailbox.twitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.example.osgi.mailbox.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SearchTimeline implements ITimeline {
	
	private final Logger log = LoggerFactory.getLogger(SearchTimeline.class);
	
	private String queryStr;

	protected void activate(Map props) {
		queryStr = (String) props.get("query");
	}

	public long getInitialTimeline(Collection<? super Message> into)
			throws TwitterException {
		return getTimelineSinceId(0, into);
	}

	public long getTimelineSinceId(long sinceId, Collection<? super Message> into)
			throws TwitterException {
		Twitter twitter = new Twitter();
		Query query = new Query(queryStr);
		
		QueryResult result = twitter.search(query);
		List<Tweet> tweets = result.getTweets();
		
		long highestId = 0;
		for (Tweet tweet : tweets) {
			long id = tweet.getId();
			highestId = (id > highestId) ? id : highestId;
			
			if(id > sinceId) {
				log.info("Adding tweet with ID={}", id);
				into.add(new TweetMessage(tweet));
			}
		}
		
		return highestId;  
	}

}
