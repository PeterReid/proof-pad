package org.proofpad;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.*;

public class Toolbar extends JPanel {
	private static final int BUTTON_GAP = 4;
	private static final boolean OSX = IdeWindow.OSX;
	private static final String modKeyStr = (OSX ? "\u2318" : "Ctrl + ");
	private static final long serialVersionUID = -333358626303272834L;
	JButton updateButton;

	public Toolbar(final IdeWindow parent) {
		new JPanel();
		if (OSX) {
			setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		} else {
			setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		}
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JButton button;
		button = new JButton(new ImageIcon(getClass().getResource("/media/open.png")));
		button.setToolTipText("Open a file for editing. (" + modKeyStr + "O)");
		button.addActionListener(IdeWindow.openAction);
		button.putClientProperty("JButton.buttonType", "textured");
		add(button);
		add(Box.createHorizontalStrut(BUTTON_GAP));
		button = new JButton(new ImageIcon(getClass().getResource("/media/save.png")));
		parent.saveButton = button;
		button.setToolTipText("Save the current file. (" + modKeyStr + "S)");
		button.addActionListener(parent.saveAction);
		button.putClientProperty("JButton.buttonType", "textured");
		add(button);
		add(Box.createHorizontalStrut(BUTTON_GAP));
		button = new JButton(new ImageIcon(getClass().getResource("/media/undo.png")));
		button.setToolTipText("Undo the last action. (" + modKeyStr + "Z)");
		parent.undoButton = button;
		button.addActionListener(parent.undoAction);
		button.putClientProperty("JButton.buttonType", "segmentedTextured");
		button.putClientProperty("JButton.segmentPosition", "first");
		button.setEnabled(false);
		add(button);
		button = new JButton(new ImageIcon(getClass().getResource("/media/redo.png")));
		button.setToolTipText("Redo the last action. (" + modKeyStr + (OSX ? "\u21e7Z" : "Y" ) + ")");
		parent.redoButton = button;
		button.addActionListener(parent.redoAction);
		button.putClientProperty("JButton.buttonType", "segmentedTextured");
		button.putClientProperty("JButton.segmentPosition", "last");
		button.setEnabled(false);
		add(button);
		add(Box.createHorizontalStrut(BUTTON_GAP));
		button = new JButton(new ImageIcon(getClass().getResource("/media/build.png")));
		final JButton buildButton = button;
		buildButton.addActionListener(parent.buildAction);
		button.setToolTipText("Create an executable from your source file. (" +
				modKeyStr + "B)");
		button.putClientProperty("JButton.buttonType", "textured");
		add(button);
		add(Box.createHorizontalStrut(BUTTON_GAP));
		button = new JButton(new ImageIcon(getClass().getResource("/media/book.png")));
		button.setToolTipText("Include an external book.");
		button.putClientProperty("JButton.buttonType", "textured");
		button.addActionListener(parent.includeBookAction);
		add(button);
		add(Box.createGlue());
		button = new JButton(new ImageIcon(getClass().getResource("/media/update.png")));
		updateButton = button;
		button.setToolTipText("An update is available.");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().browse(new URI("http://proofpad.org/"));
				} catch (IOException e) {
				} catch (URISyntaxException e) { }
			}
		});
		button.setVisible(false);
		checkForUpdate();
		button.putClientProperty("JButton.buttonType", "textured");
		add(button);
		add(Box.createHorizontalStrut(BUTTON_GAP));
		button = new JButton();
		button.putClientProperty("JButton.buttonType", "help");
		if (!OSX) {
			button.setText("Tutorial");
		}
		button.addActionListener(parent.tutorialAction);
		add(button);
	}
	
	public void checkForUpdate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Scanner s = new Scanner(new URL("http://proofpad.org/CURRENT").openStream());
					if (s.nextInt() > Main.RELEASE) {
						updateButton.setVisible(true);
					}
				} catch (MalformedURLException e) {
				} catch (IOException e) { }
			}
		}).start();
	}
}
