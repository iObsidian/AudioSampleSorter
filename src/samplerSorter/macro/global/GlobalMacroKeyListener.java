package samplerSorter.macro.global;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import samplerSorter.actions.Action;
import samplerSorter.macro.Key;
import samplerSorter.macro.MacroAction;
import samplerSorter.macro.MacroLoader;
import samplerSorter.util.NativeKeyEventToKey;

//Call GlobalScreen.unregisterNativeHook(); to remove (unneeded here)

public class GlobalMacroKeyListener implements NativeKeyListener {

	private static final String TAG = "GlobalMacroKeyListener";

	public boolean isListenningForGlobalInputs = true;

	public ArrayList<Key> pressedKeys = new ArrayList<Key>();

	private static GlobalMacroKeyListener me;

	/**
	 * @return self, creates if not already instantiated
	 */
	public static GlobalMacroKeyListener get() {
		if (me == null) {
			me = new GlobalMacroKeyListener();
		}
		return me;
	}

	/**
	 * NativeKeyListener pressed keys
	 */
	public void nativeKeyPressed(NativeKeyEvent ke) {

		if (isListenningForGlobalInputs) { //is not minimized and is focused

			Key e = NativeKeyEventToKey.getJavaKeyEvent(ke);
			if (!pressedKeys.contains(e)) {
				pressedKeys.add(e);

				checkForBinds();
			}

		}

	}

	public void changeListeningState(boolean newListeningState) {
		isListenningForGlobalInputs = newListeningState;
	}

	/**
	 * NativeKeyListener released keys
	 */
	public void nativeKeyReleased(NativeKeyEvent ke) {

		Key e = NativeKeyEventToKey.getJavaKeyEvent(ke);
		pressedKeys.remove(e);

	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { // left intentionally blank
	}

	public void init() {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			samplerSorter.logger.Logger.logError(TAG, "There was a problem registering the native hook.", ex);
			System.exit(1);
		}

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		// Change the level for all handlers attached to the default logger.
		Handler[] handlers = Logger.getLogger("").getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			handlers[i].setLevel(Level.OFF);
		}

		//Add 'this' to the nativeKeyListenners
		GlobalScreen.addNativeKeyListener(get());
	}

	public boolean shiftIsPressed() {
		return keyIsPressed(KeyEvent.VK_SHIFT);
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
	public boolean keyIsPressed(int keyCodeOfPressedKey) {
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

	private void checkForBinds() {

		//For every registered MacroActions
		for (MacroAction m : MacroLoader.macroActions) {

			// If the MacroAction's keys are globaly pressed
			if (keysArePressed(m.keys)) {

				System.out.println("M's key is pressed");

				for (Action a : m.actionsToPerform) {

					a.perform();

				}

			}

		}

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

		if (current == required) {
			return true;
		} else {
			return false;
		}

	}

}