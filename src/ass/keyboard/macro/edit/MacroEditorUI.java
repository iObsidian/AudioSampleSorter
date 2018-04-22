package ass.keyboard.macro.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ass.action.DeleteAction;
import ass.action.MoveToFolderAction;
import ass.action.OpenContainingFolderAction;
import ass.action.RemoveSelectedFilesAction;
import ass.action.RenameAction;
import ass.action.SimpleUIAction;
import ass.action.TestAction;
import ass.action.editeable.EditablePropertyEditor;
import ass.action.interfaces.Action;
import ass.keyboard.key.Key;
import ass.keyboard.macro.MacroAction;
import ass.keyboard.macro.MacroEditor;
import constants.icons.UserIcon;
import constants.icons.iconChooser.GetIcon;
import constants.icons.iconChooser.IconChooser;
import key.NativeKeyEventToKey;

public class MacroEditorUI extends JPanel {

	static Logger log = LoggerFactory.getLogger(MacroEditorUI.class);

	private static ArrayList<Action> default_actions = new ArrayList<>();

	static {

		// Simple UI Actions
		SimpleUIAction.init();

		default_actions.addAll(SimpleUIAction.UIActions);

		//Other Actions
		default_actions.add(new RenameAction());
		default_actions.add(new DeleteAction());
		default_actions.add(new RemoveSelectedFilesAction());
		default_actions.add(new OpenContainingFolderAction());
		default_actions.add(new MoveToFolderAction());
		default_actions.add(new TestAction());

		//default_actions.add(new TestAction());

		log.info("Found " + default_actions.size() + " actions.");

	}

	private MacroEditorUI me = this;

	//

	private MacroAction keyBindToEdit;

	//

	private static final Font RESULT_FONT_PLAIN = new Font("Segoe UI Light", Font.PLAIN, 20);
	private static final Font RESULT_FONT_BOLD = new Font("Segoe UI Light", Font.BOLD, 20);

	private boolean isListenningForKeyInputs = false;
	private ArrayList<Key> keysPressedAndNotReleased = new ArrayList<>();
	private JTextField keyEditorInputBox;

	private boolean newKeyBind;

	private static JPanel columnpanel = new JPanel();
	private static JPanel borderlaoutpanel;

	private static JScrollPane scrollPane;

	private ArrayList<Action> actions = new ArrayList<>();
	private ArrayList<MacroActionPanel> macroActionEditPanels = new ArrayList<>();

	public EditablePropertyEditor propertyEditor = new EditablePropertyEditor();
	private JTextField titleEditor;
	private JButton iconButton;
	private JCheckBox chckboxMenu;
	private JCheckBox chckbxToolbar;

	public void onHide() {
		isListenningForKeyInputs = false;
	}

	public void onShow() {
	}

	/**
	 * Create the frame.
	 */
	public MacroEditorUI(MacroEditor m) {

		setBounds(0, 0, 355, 310);
		setVisible(true);
		setLayout(null);

		JButton btnAdd = new JButton("Save");
		btnAdd.addActionListener(e -> {

			if (newKeyBind) {
				newKeyBind = false;
				log.info("Creating new KeyBind");

				keyBindToEdit.actionsToPerform.clear();

				m.macroLoader.addNewMacro(keyBindToEdit);

			}

			keyBindToEdit.actionsToPerform.clear();

			for (Action action : actions) {
				keyBindToEdit.actionsToPerform.add(action);
			}

			m.macroListUI.refreshInfoPanel();

			m.macroLoader.serialise(); //just save

			m.showMacroEditUI();

			m.macroLoader.tellMacroChanged();
		});
		btnAdd.setBounds(12, 272, 330, 25);
		add(btnAdd);

		keyEditorInputBox = new JTextField("Click to edit macro");
		keyEditorInputBox.setToolTipText("Shortcut");
		keyEditorInputBox.setHorizontalAlignment(SwingConstants.CENTER);
		keyEditorInputBox.setFont(RESULT_FONT_PLAIN);
		keyEditorInputBox.setBackground(Color.decode("#FCFEFF")); //'Ultra Light Cyan'
		keyEditorInputBox.setEditable(false);
		keyEditorInputBox.setBounds(12, 224, 330, 35);
		keyEditorInputBox.setColumns(10);
		add(keyEditorInputBox);

		//Populate the ComboBox

		JComboBox<Action> comboBox = new JComboBox<>();
		comboBox.setToolTipText("Add action");
		for (Action a : default_actions) {
			comboBox.addItem(a); //uses action.toString to display
		}

		comboBox.setBounds(105, 58, 237, 22);
		add(comboBox);

		comboBox.addActionListener(evt -> {
			if (comboBox.getSelectedItem() != null) {
				Action selectedAction = (Action) comboBox.getSelectedItem();
				addActionAndActionEditPanel(selectedAction);

			}
		});

		scrollPane = new JScrollPane();
		scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 93, 330, 118);
		scrollPane.setAutoscrolls(true);
		// scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);

		borderlaoutpanel = new JPanel();
		scrollPane.setViewportView(borderlaoutpanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		borderlaoutpanel.setLayout(new BorderLayout(0, 0));

		columnpanel = new JPanel();
		columnpanel.setLayout(new GridLayout(0, 1, 0, 1));
		columnpanel.setBackground(Color.gray);
		borderlaoutpanel.add(columnpanel, BorderLayout.NORTH);

		titleEditor = new JTextField();
		titleEditor.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateName();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateName();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			public void updateName() {
				if (keyBindToEdit != null) { //sometimes is triggered on software launch
					keyBindToEdit.setName(titleEditor.getText());
				}
			}
		});

		titleEditor.setToolTipText("Click to edit title");
		titleEditor.setFont(new Font("Segoe UI Light", Font.BOLD, 20));
		titleEditor.setText("Title");
		titleEditor.setHorizontalAlignment(SwingConstants.CENTER);
		titleEditor.setBounds(56, 13, 195, 32);

		titleEditor.addActionListener(e -> {
			keyBindToEdit.setName(titleEditor.getText());
			titleEditor.transferFocus();
		});

		add(titleEditor);
		titleEditor.setColumns(10);

		//

		iconButton = new JButton();
		iconButton.setToolTipText("Click to edit icon");
		iconButton.setIcon(new ImageIcon(MacroEditorUI.class
				.getResource("/com/sun/deploy/uitoolkit/impl/fx/ui/resources/image/graybox_error.png")));

		iconButton.setFocusable(false); //Removes the stupid 'selected' border
		iconButton.addActionListener(e -> {
			IconChooser i = new IconChooser(new GetIcon() {
				@Override
				public void getResponse(UserIcon icon) {
					keyBindToEdit.setIcon(icon);
					iconButton.setIcon(icon.getImageIcon());
				}
			});

		});

		//

		iconButton.setBounds(12, 13, 32, 32);
		add(iconButton);

		JLabel lblAddAction = new JLabel("Add action :");
		lblAddAction.setBounds(12, 61, 81, 16);
		add(lblAddAction);

		chckboxMenu = new JCheckBox("Menu");
		chckboxMenu.setToolTipText("Show in right click menu");
		chckboxMenu.setSelected(true);
		chckboxMenu.setBounds(259, 8, 83, 22);

		ActionListener act2 = actionEvent -> keyBindToEdit.showInMenu = chckboxMenu.isSelected();
		chckboxMenu.addActionListener(act2);
		add(chckboxMenu);

		chckbxToolbar = new JCheckBox("Toolbar");
		chckboxMenu.setToolTipText("Show in toolbar");
		chckboxMenu.setSelected(true);
		chckbxToolbar.setBounds(259, 31, 83, 18);

		ActionListener act = actionEvent -> keyBindToEdit.showInToolbar = chckbxToolbar.isSelected();
		chckbxToolbar.addActionListener(act);
		add(chckbxToolbar);

		keyEditorInputBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isListenningForKeyInputs) {
					keyBindToEdit.clearKeys();
					keyEditorInputBox.setText("Press any key(s)");
					log.info("Now listenning for key inputs");
					isListenningForKeyInputs = true;
					keyEditorInputBox.setFont(RESULT_FONT_PLAIN);
				} else {
					log.info("Already listenning for key inputs!");
				}
			}
		});

	}

	public void changeKeyBindToEdit(MacroAction keyBindToEdit) {

		//if keyBindToEdit is null, it means MacroListUI wants us to create a new keyBind

		if (keyBindToEdit != null) {
			newKeyBind = false;

			log.info("Editing existing keybind");

			for (Action a : keyBindToEdit.actionsToPerform) {
				addActionAndActionEditPanel(a);
			}

			chckboxMenu.setSelected(keyBindToEdit.showInMenu);

			if (keyBindToEdit.getIcon() != null) {
				iconButton.setIcon(keyBindToEdit.getIcon());
			}

		} else {

			//Reset for fresh start

			log.info("Creating new keyBind");

			chckboxMenu.setSelected(true);

			newKeyBind = true;
			keyBindToEdit = new MacroAction("Title");

		}

		titleEditor.setText(keyBindToEdit.getName());

		this.keyBindToEdit = keyBindToEdit;

		updateInputBoxText();
	}

	private void updateInputBoxText() {
		if (keyBindToEdit != null) {
			if (keyBindToEdit.keys.size() > 0) {

				keyEditorInputBox.setText("[" + keyBindToEdit.getKeysAsString() + "]");

			} else {
				keyEditorInputBox.setText("Edit shortcut");
			}
		} //Else : its a new keyBind, so do nothing
	}

	/**
	 * This is a work-around the previous keyboard register hook thing
	 *  that caused both the JTable and the PropertyEditor to not receive inputs.
	 */
	public void globalKeyBoardInput(NativeKeyEvent ke, boolean isPressed) {

		Key k = NativeKeyEventToKey.getJavaKeyEvent(ke);

		if (isListenningForKeyInputs) {

			if (isPressed) {
				keyPressed(k);
			} else {
				keyReleased(k);
			}

			updateInputBoxText();
		}

	}

	private void keyPressed(Key k) {
		if (!keysPressedAndNotReleased.contains(k)) {
			keysPressedAndNotReleased.add(k);

			if (!keyBindToEdit.keys.contains(k)) {
				keyBindToEdit.keys.add(k);
			}
		}
	}

	private void keyReleased(Key k) {

		if (keysPressedAndNotReleased.contains(k)) {
			keysPressedAndNotReleased.remove(k);
		}

		if (keysPressedAndNotReleased.size() == 0) { //no more keys to be released
			log.info("Stopped listenning for events.");
			isListenningForKeyInputs = false;
			keyEditorInputBox.setFont(RESULT_FONT_BOLD);
		}

	}

	// TODO : update this when adding fiels
	void addActionAndActionEditPanel(Action action) {

		MacroActionPanel infoPanel = new MacroActionPanel(action, this);

		columnpanel.add(infoPanel);

		actions.add(action);
		macroActionEditPanels.add(infoPanel);

		refreshPanels();

	}

	public void clearActionEditPanels() {
		try {
			for (Iterator<MacroActionPanel> iterator = macroActionEditPanels.iterator(); iterator.hasNext();) {
				MacroActionPanel infoP = iterator.next();

				iterator.remove();
				columnpanel.remove(infoP);
			}

			actions.clear();

			refreshPanels();

		} catch (Exception e) {
			log.error("Error in clearActionEditPanels", e);
			e.printStackTrace();
		}
	}

	void refreshPanels() {

		for (MacroActionPanel logPanel : macroActionEditPanels) {
			logPanel.validate();
			logPanel.repaint();
		}

		columnpanel.validate();
		columnpanel.repaint();

		borderlaoutpanel.validate();
		borderlaoutpanel.repaint();

		scrollPane.validate();
		scrollPane.repaint();

	}

	public static Color hex2Rgb(String colorStr) {
		//@formatter:off
		return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
		//@formatter:on
	}

	public void removeFromPanels(MacroActionPanel me) {

		actions.remove(me.action);

		columnpanel.remove(me);
		macroActionEditPanels.remove(me);

		refreshPanels();
	}

}
