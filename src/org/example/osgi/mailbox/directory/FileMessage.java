package org.example.osgi.mailbox.directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.example.osgi.mailbox.api.Message;
import org.example.osgi.mailbox.api.MessageReaderException;

public class FileMessage implements Message {

	private final long id;
	private final File file;
	private final String mimeType;

	public FileMessage(long id, File file, String mimeType) {
		this.id = id;
		this.file = file;
		this.mimeType = mimeType;
	}

	public InputStream getContent() throws MessageReaderException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new MessageReaderException("Unable to open file", e);
		}
	}

	public long getId() {
		return id;
	}

	public String getMIMEType() {
		return mimeType;
	}

	public String getSummary() {
		return file.getName();
	}

}
