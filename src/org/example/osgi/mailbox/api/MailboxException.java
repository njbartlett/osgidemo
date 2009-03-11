package org.example.osgi.mailbox.api;

public class MailboxException extends Exception {

	private static final long serialVersionUID = 1L;

	public MailboxException(String message) {
		super(message);
	}

	public MailboxException(Throwable cause) {
		super(cause);
	}

	public MailboxException(String message, Throwable cause) {
		super(message, cause);
	}
}