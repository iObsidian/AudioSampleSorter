package global.icons.iconChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import global.icons.Icons;
import global.icons.StaticIcon;
import global.logger.Logger;
import keybinds.macro.edit.ToolBarButton;
import ui.MiddleOfTheScreen;

public class IconChooser extends JFrame {

	protected static final String TAG = "IconChooser";

	private GetIcon waitingForAnswer;

	private JPanel contentPane;
	private JPanel iconsPanel;

	public final Font font = new Font("Segoe UI Light", Font.PLAIN, 13);

	/**
	 * Create the frame.
	 */
	public IconChooser() {
		setTitle("Icon chooser");
		setIconImage(Icons.ICON_CHOOSER.getImage());

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocation(MiddleOfTheScreen.getLocationFor(this));

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});

		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		JPanel content = new JPanel();
		contentPane.add(content);
		content.setLayout(new BorderLayout(0, 0));

		JScrollPane iconScrollpane = new JScrollPane();
		content.add(iconScrollpane, BorderLayout.CENTER);
		iconScrollpane.getVerticalScrollBar().setUnitIncrement(16);

		iconsPanel = new JPanel();
		iconsPanel.setBackground(SystemColor.textHighlightText);
		iconScrollpane.setViewportView(iconsPanel);
		iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.Y_AXIS));

		JPanel importPanel = new JPanel();
		importPanel.setBackground(SystemColor.control);
		iconScrollpane.setColumnHeaderView(importPanel);

		JButton btnImport = new JButton("Refresh list");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populate();
			}
		});
		btnImport.setFont(font.deriveFont(Font.BOLD));
		importPanel.add(btnImport);

		JButton btnOpenContainingFolder = new JButton("Open containing folder");
		btnOpenContainingFolder.setFont(font.deriveFont(Font.BOLD));
		btnOpenContainingFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(contentPane, "Opening the icon folder. \nIcons are loaded as 16x16 images that support transparency.\nIcons are stored in " + Icons.LOCATION_OF_ICONS
						+ ".\nYou might have to reload the program to see your icon.");

				try {
					Desktop.getDesktop().open(new File(Icons.LOCATION_OF_ICONS));
				} catch (IOException e1) {
					Logger.logError(TAG, "Could not open folder containing icon images!");
					e1.printStackTrace();
				}

			}
		});
		btnOpenContainingFolder.setBackground(Color.ORANGE);
		btnOpenContainingFolder.setForeground(Color.BLACK);
		btnOpenContainingFolder.setIcon(Icons.ABOUT.getImageIcon());
		importPanel.add(btnOpenContainingFolder);

		populate();
	}

	public void populate() {

		Logger.logInfo(TAG, "Repopulating...");

		iconsPanel.removeAll();
		iconsPanel.revalidate();
		iconsPanel.repaint();
		iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.Y_AXIS));

		for (StaticIcon i : Icons.images) {

			JPanel icon = new JPanel();
			iconsPanel.add(icon);

			ToolBarButton selectThisIcon = new ToolBarButton(i);
			selectThisIcon.setFont(font);
			selectThisIcon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//selectThisIcon.getIcon(), 

					waitingForAnswer.GetResponse(selectThisIcon.getStaticIcon());
					setVisible(false);
				}
			});
			icon.add(selectThisIcon);

		}

	}

	/**
	 * Use IconChooser.getIcon(this) when this implements GetIcon and it will
	 * receive the icon with the GetResponse method
	 */

	public void getIcon(GetIcon g) {
		waitingForAnswer = g;
		setVisible(true);
	}

}