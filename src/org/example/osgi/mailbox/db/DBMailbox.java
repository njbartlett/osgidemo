package org.example.osgi.mailbox.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.Message;
import org.example.osgi.mailbox.api.MessageReaderException;


public class DBMailbox implements Mailbox {
	
	private final Connection connection;

	public DBMailbox(Connection connection) {
		this.connection = connection;
	}

	public long[] getAllMessages() throws MailboxException {
		final List<Long> list = new ArrayList<Long>();
		
		RowHandler handler = new RowHandler() {
			public void handleRow(ResultSet rs) throws SQLException {
				long id = rs.getLong(1);
				list.add(id);
			}
		};
		
		try {
			executeQuery("SELECT id FROM messages", handler);
		} catch (SQLException e) {
			throw new MailboxException(e);
		}
		
		long[] array = new long[list.size()];
		for(int i=0; i<array.length; i++) {
			array[i] = list.get(i);
		}
		
		return array;
	}

	public Message[] getMessages(long[] ids) throws MailboxException {
		Message[] messages = new Message[ids.length];
		
		for (int i = 0; i < ids.length; i++) {
			messages[i] = getMessage(ids[i]);
		}
		
		return messages;
	}
	
	protected Message getMessage(long id) throws MailboxException {
		final Message[] holder = new Message[1];
		
		RowHandler handler = new RowHandler() {
			public void handleRow(ResultSet rs) throws SQLException {
				long id = rs.getLong(1);
				String subject = rs.getString(2);
				String content = rs.getString(3);
				
				holder[0] = new StringMessage(id, subject, content);
			}
		};
		
		try {
			executeQuery("SELECT id, subject, content FROM messages WHERE id = " + id, handler);
		} catch (SQLException e) {
			throw new MailboxException(e);
		}
		
		return holder[0];
	}

	public long[] getMessagesSince(long id) throws MailboxException {
		return new long[0];
	}

	public void markRead(boolean read, long[] ids) throws MailboxException {
	}
	
	protected void executeQuery(String query, RowHandler rowHandler) throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		
		try {
			statement = connection.createStatement();
			rs = statement.executeQuery(query);
			
			while(rs.next()) {
				rowHandler.handleRow(rs);
			}
		} finally {
			if(rs != null) {
				try { rs.close(); } catch(SQLException e) {}
			}
			if(statement != null) {
				try { statement.close(); } catch (SQLException e) {}
			}
		}
	}

}

interface RowHandler {
	public void handleRow(ResultSet rs) throws SQLException;
}

class StringMessage implements Message {

	private static final String MIME_TYPE_TEXT = "text/plain";

	private final long id;
	private final String subject;
	private final String text;

	public StringMessage(long id, String subject, String text) {
		this.id = id;
		this.subject = subject;
		this.text = text;
	}

	public InputStream getContent() throws MessageReaderException {
		return new ByteArrayInputStream(text.getBytes());
	}

	public long getId() {
		return id;
	}

	public String getMIMEType() {
		return MIME_TYPE_TEXT;
	}

	public String getSummary() {
		return subject;
	}

}

