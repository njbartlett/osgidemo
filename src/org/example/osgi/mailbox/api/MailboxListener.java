package org.example.osgi.mailbox.api;

public interface MailboxListener {
	/**
	 * Called when a new message or messages have arrived in the specified
	 * mailbox.
	 * 
	 * @param mailboxName
	 *            The name of the mailbox in which new messages have arrived.
	 * @param mailbox
	 *            A reference to the mailbox.
	 * @param ids
	 *            An array of new message IDs.
	 */
	void messagesArrived(String mailboxName, Mailbox mailbox, long[] ids);
}