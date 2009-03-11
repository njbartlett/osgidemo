package org.example.osgi.mailbox.twitter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.example.osgi.mailbox.api.Message;
import org.example.osgi.mailbox.api.MessageReaderException;

import twitter4j.Status;
import twitter4j.Tweet;

public class TweetMessage implements Message {
	
	private long id;
	private String text;
	private String from;
	
	public TweetMessage(Status status) {
		id = status.getId();
		text = status.getText();
		from = status.getUser().getScreenName();
	}
	
	public TweetMessage(Tweet tweet) {
		id = tweet.getId();
		text = tweet.getText();
		from = tweet.getFromUser();
	}
	
	public InputStream getContent() throws MessageReaderException {
		return new ByteArrayInputStream(text.getBytes());
	}

	public long getId() {
		return id;
	}

	public String getMIMEType() {
		return "text/plain";
	}

	public String getSummary() {
		return from;
	}

}
