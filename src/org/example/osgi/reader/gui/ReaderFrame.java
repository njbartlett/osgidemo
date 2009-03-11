package org.example.osgi.reader.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.osgi.framework.BundleContext;

public class ReaderFrame extends JFrame {

	private JTabbedPane tabbedPane;
	private ReaderMailboxTracker tracker;

	public ReaderFrame() {
		super("Mailbox Reader");

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Mailboxes", createIntroPanel());
		tabbedPane.setPreferredSize(new Dimension(400, 400));

		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	}

	private Component createIntroPanel() {
		JLabel label = new JLabel("Select a Mailbox");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	protected void openTracking(BundleContext context) {
		tracker = new ReaderMailboxTracker(context, tabbedPane);
		tracker.open();
	}

	protected void closeTracking() {
		tracker.close();
	}
}
