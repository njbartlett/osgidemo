package org.example.osgi.mailbox.growable;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxListener;
import org.example.osgi.mailbox.api.Message;
import org.example.osgi.mailbox.fixed.FixedMailbox;
import org.example.osgi.mailbox.fixed.StringMessage;
import org.example.osgi.utils.Visitor;
import org.example.osgi.utils.WhiteboardHelper;

public class GrowableMailbox extends FixedMailbox {

	private final WhiteboardHelper<MailboxListener> whiteboard;
	private final String mailboxName;

	public GrowableMailbox(WhiteboardHelper<MailboxListener> wb,
			String mailboxName, String[][] initialContents) {
		super(initialContents);
		this.whiteboard = wb;
		this.mailboxName = mailboxName;
	}

	protected void addMessage(String subject, String text) {
		final int newMessageId;

		synchronized (this) {
			newMessageId = messages.size();
			Message newMessage = new StringMessage(newMessageId, subject, text);
			messages.add(newMessage);
		}

		final long[] newMessageIds = new long[] { newMessageId };
		final Mailbox source = this;
		Visitor<MailboxListener> v = new Visitor<MailboxListener>() {
			public void visit(MailboxListener l) {
				l.messagesArrived(mailboxName, source, newMessageIds);
			}
		};
		whiteboard.accept(v);
	}
}
