package ass.keyboard.key;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ass.keyboard.macro.MacroAction;
import ass.keyboard.macro.MacroEditor;
import key.NativeKeyEventToKey;

//Call GlobalScreen.unregisterNativeHook(); to remove (unneeded here)

public class GlobalKeyListener implements NativeKeyListener {

	Logger log = LoggerFactory.getLogger(GlobalKeyListener.class);

	public boolean isListenningForInputs = false; //only true when the frame is visible (set to false at first)

	private ArrayList<Key> pressedKeys = new ArrayList<>();

	private MacroEditor macroEditor;

	private static GlobalKeyListener me;

	/**
	 * @return self, creates if not already instantiated
	 */
	public static GlobalKeyListener get() {
		if (me == null) {
			me = new GlobalKeyListener();
		}
		return me;
	}

	/**
	 * NativeKeyListener pressed keys
	 */
	public void nativeKeyPressed(NativeKeyEvent ke) {

		transferToMacroEditor(ke, true);

		if (isListenningForInputs) { //is not minimized and is focused

			Key e = NativeKeyEventToKey.getJavaKeyEvent(ke);

			if (!pressedKeys.contains(e)) {
				pressedKeys.add(e);

				//CHECK FOR BINDS

				//For every registered MacroActions
				for (MacroAction m : macroEditor.macroLoader.macroActions) {

					//If m has keys, otherwise it will trigger every key presses
					//If the MacroAction's keys are globaly pressed
					if (!m.keys.isEmpty() && keysArePressed(m.keys)) {

						//PERFORM ACTIONS
						m.perform();
					}
				}
			}
		}
	}

	private void transferToMacroEditor(NativeKeyEvent ke, boolean isPressed) {
		//Transfer input to MacroEditor (fix for the JTable focus)
		macroEditor.macroEditorUI.globalKeyBoardInput(ke, isPressed);
	}

	/**
	 * NativeKeyListener released keys
	 */
	public void nativeKeyReleased(NativeKeyEvent ke) {

		transferToMacroEditor(ke, false);

		Key e = NativeKeyEventToKey.getJavaKeyEvent(ke);
		pressedKeys.remove(e);

	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { // left intentionally blank
	}

	public void init(MacroEditor m) {

		this.macroEditor = m;

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			log.error("There was a problem registering the native hook.", ex);
			System.exit(1);
		}
		
		// Get the logger for "org.jnativehook" and set the level to off.
		java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		// Change the level for all handlers attached to the default logger.
		Handler[] handlers = java.util.logging.Logger.getLogger("").getHandlers();
		for (Handler handler : handlers) {
			handler.setLevel(Level.OFF);
		}

		//Add 'this' to the nativeKeyListenners
		GlobalScreen.addNativeKeyListener(get());
	}

	/**
	 * @param pressedKey new Key(KeyEvent.VK_SHIFT)
	 */
	public boolean keyIsPressed(Key pressedKey) {
		return pressedKeys.contains(pressedKey);
	}

	/**
	 * @param pressedKey KeyEvent.VK_SHIFT
	 */
	boolean keyIsPressed(int keyCodeOfPressedKey) {
		return pressedKeys.contains(new Key(keyCodeOfPressedKey));
	}

	/**
	 * For all pressed keys, if a required one is not pressed, then it returns false
	 * Otherwise it returns true
	 */
	private boolean keysArePressed(ArrayList<Key> keys) {

		for (Key key : keys) {
			if (!pressedKeys.contains(key)) {
				return false;
			}
		}

		return true;
	}

	public boolean keyCombinaisonIsPressed(ArrayList<Key> requiredKeys) {
		final int required = requiredKeys.size();
		int current = 0;

		for (Key kR : requiredKeys) {
			for (Key k : pressedKeys) {
				if (k.equals(kR)) {
					current++;
				}
			}
		}

		return current == required;

	}

	public boolean shiftIsPressed() {
		return keyIsPressed(KeyEvent.VK_SHIFT);
	}

	public boolean ctrlIsPressed() {
		return keyIsPressed(KeyEvent.VK_CONTROL);
	}

}