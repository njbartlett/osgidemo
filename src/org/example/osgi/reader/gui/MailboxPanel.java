package org.example.osgi.reader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.example.osgi.mailbox.api.Mailbox;
import org.example.osgi.mailbox.api.MailboxException;
import org.example.osgi.mailbox.api.Message;
import org.example.osgi.mailbox.api.MessageReaderException;

public class MailboxPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final MailboxTableModel tableModel;

	public MailboxPanel(Mailbox mbox) throws MailboxException {
		tableModel = new MailboxTableModel(mbox);
		
		// Table
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		
		// Text area
		final JTextArea textArea = new JTextArea();
		
		// Split Pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(scrollPane);
		splitPane.setBottomComponent(textArea);
		splitPane.setResizeWeight(0.5);
		scrollPane.setPreferredSize(new Dimension(200,200));
		textArea.setPreferredSize(new Dimension(200,200));
		
		// Table selection listener
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if(row == -1) {
					textArea.setText("");
				} else {
					Message message = tableModel.getRowMessage(row);
					textArea.setText(readMessage(message));
				}
			}
		});
	
		// Layout
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}
	
	private static String readMessage(Message message) {
		StringBuilder result = new StringBuilder();
		try {
			InputStream stream = message.getContent();
			InputStreamReader reader = new InputStreamReader(stream);
			
			char[] buffer = new char[1024];
			int charsRead = reader.read(buffer, 0, 1024);
			while(charsRead > -1) {
				result.append(buffer, 0, charsRead);
				charsRead = reader.read(buffer, 0, 1024);
			}
			return result.toString();
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		}
	}

	public MailboxTableModel getTableModel() {
		return tableModel;
	}
}