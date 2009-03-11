package org.example.osgi.reader.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.mailbox.api.Message;

public class MailboxTableModel extends AbstractTableModel implements MailboxListener {

	private static final String ERROR = "ERROR";

	private final Mailbox mailbox;
	private final List<Message> messages;

	public MailboxTableModel(Mailbox mailbox) throws MailboxException {
		this.mailbox = mailbox;
		long[] messageIds = mailbox.getAllMessages();
		messages = new ArrayList<Message>(messageIds.length);
		Message[] messageArray = mailbox.getMessages(messageIds);
		for (Message message : messageArray) {
			messages.add(message);
		}
	}

	public synchronized int getRowCount() {
		return messages.size();
	}

	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Id";
		case 1:
			return "Subject";
		}
		return ERROR;
	}

	public synchronized Object getValueAt(int row, int column) {
		Message message = messages.get(row);
		switch (column) {
		case 0:
			return Long.toString(message.getId());
		case 1:
			return message.getSummary();
		}
		return ERROR;
	}
	
	public synchronized Message getRowMessage(int row) {
		return messages.get(row);
	}

	public void messagesArrived(String mailboxName, Mailbox mailbox, long[] ids) {
		try {
			long[] sortedIds = new long[ids.length];
			System.arraycopy(ids, 0, sortedIds, 0, ids.length);
			Arrays.sort(sortedIds);
			
			Message[] newMessages = mailbox.getMessages(sortedIds);
			
			final int firstRow;
			final int lastRow;
			
			synchronized (this) {
				firstRow = messages.size();
				for (Message message : newMessages) {
					messages.add(message);
				}
				lastRow = messages.size() - 1;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(firstRow, lastRow);
				}
			});
		} catch (MailboxException e) {
			e.printStackTrace();
		}
	}
}