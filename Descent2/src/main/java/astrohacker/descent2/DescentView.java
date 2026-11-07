package astrohacker.descent2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class DescentView extends SurfaceView implements KeyEvent.Callback, SurfaceHolder.Callback {
	// Controller buttons as input.c knows them
	private static final int PAD_A = 0, PAD_B = 1, PAD_X = 2, PAD_Y = 3, PAD_L1 = 4, PAD_R1 = 5,
			PAD_L2 = 6, PAD_R2 = 7, PAD_THUMBL = 8, PAD_THUMBR = 9, PAD_START = 10, PAD_SELECT = 11,
			PAD_DPAD_UP = 12, PAD_DPAD_DOWN = 13, PAD_DPAD_LEFT = 14, PAD_DPAD_RIGHT = 15,
			PAD_STICK_UP = 16, PAD_STICK_DOWN = 17, PAD_STICK_LEFT = 18, PAD_STICK_RIGHT = 19;

	// How far a stick or a trigger goes before it counts as a press
	private static final float PRESS = 0.5f;
	private static final float DEAD_ZONE = 0.15f;

	private static final int MB_RIGHT = 1;

	// Android key codes to the PC scan codes the game reads, so the keys sit
	// where they did whatever the keyboard layout
	private static final int[] SCAN_CODES = new int[KeyEvent.getMaxKeyCode() + 1];

	static {
		int[] pairs = {
				KeyEvent.KEYCODE_ESCAPE, 0x01, KeyEvent.KEYCODE_1, 0x02, KeyEvent.KEYCODE_2, 0x03,
				KeyEvent.KEYCODE_3, 0x04, KeyEvent.KEYCODE_4, 0x05, KeyEvent.KEYCODE_5, 0x06,
				KeyEvent.KEYCODE_6, 0x07, KeyEvent.KEYCODE_7, 0x08, KeyEvent.KEYCODE_8, 0x09,
				KeyEvent.KEYCODE_9, 0x0A, KeyEvent.KEYCODE_0, 0x0B, KeyEvent.KEYCODE_MINUS, 0x0C,
				KeyEvent.KEYCODE_EQUALS, 0x0D, KeyEvent.KEYCODE_DEL, 0x0E, KeyEvent.KEYCODE_TAB, 0x0F,
				KeyEvent.KEYCODE_Q, 0x10, KeyEvent.KEYCODE_W, 0x11, KeyEvent.KEYCODE_E, 0x12,
				KeyEvent.KEYCODE_R, 0x13, KeyEvent.KEYCODE_T, 0x14, KeyEvent.KEYCODE_Y, 0x15,
				KeyEvent.KEYCODE_U, 0x16, KeyEvent.KEYCODE_I, 0x17, KeyEvent.KEYCODE_O, 0x18,
				KeyEvent.KEYCODE_P, 0x19, KeyEvent.KEYCODE_LEFT_BRACKET, 0x1A,
				KeyEvent.KEYCODE_RIGHT_BRACKET, 0x1B, KeyEvent.KEYCODE_ENTER, 0x1C,
				KeyEvent.KEYCODE_CTRL_LEFT, 0x1D, KeyEvent.KEYCODE_A, 0x1E, KeyEvent.KEYCODE_S, 0x1F,
				KeyEvent.KEYCODE_D, 0x20, KeyEvent.KEYCODE_F, 0x21, KeyEvent.KEYCODE_G, 0x22,
				KeyEvent.KEYCODE_H, 0x23, KeyEvent.KEYCODE_J, 0x24, KeyEvent.KEYCODE_K, 0x25,
				KeyEvent.KEYCODE_L, 0x26, KeyEvent.KEYCODE_SEMICOLON, 0x27,
				KeyEvent.KEYCODE_APOSTROPHE, 0x28, KeyEvent.KEYCODE_GRAVE, 0x29,
				KeyEvent.KEYCODE_SHIFT_LEFT, 0x2A, KeyEvent.KEYCODE_BACKSLASH, 0x2B,
				KeyEvent.KEYCODE_Z, 0x2C, KeyEvent.KEYCODE_X, 0x2D, KeyEvent.KEYCODE_C, 0x2E,
				KeyEvent.KEYCODE_V, 0x2F, KeyEvent.KEYCODE_B, 0x30, KeyEvent.KEYCODE_N, 0x31,
				KeyEvent.KEYCODE_M, 0x32, KeyEvent.KEYCODE_COMMA, 0x33, KeyEvent.KEYCODE_PERIOD, 0x34,
				KeyEvent.KEYCODE_SLASH, 0x35, KeyEvent.KEYCODE_SHIFT_RIGHT, 0x36,
				KeyEvent.KEYCODE_NUMPAD_MULTIPLY, 0x37, KeyEvent.KEYCODE_ALT_LEFT, 0x38,
				KeyEvent.KEYCODE_SPACE, 0x39, KeyEvent.KEYCODE_CAPS_LOCK, 0x3A,
				KeyEvent.KEYCODE_F1, 0x3B, KeyEvent.KEYCODE_F2, 0x3C, KeyEvent.KEYCODE_F3, 0x3D,
				KeyEvent.KEYCODE_F4, 0x3E, KeyEvent.KEYCODE_F5, 0x3F, KeyEvent.KEYCODE_F6, 0x40,
				KeyEvent.KEYCODE_F7, 0x41, KeyEvent.KEYCODE_F8, 0x42, KeyEvent.KEYCODE_F9, 0x43,
				KeyEvent.KEYCODE_F10, 0x44, KeyEvent.KEYCODE_NUM_LOCK, 0x45,
				KeyEvent.KEYCODE_SCROLL_LOCK, 0x46, KeyEvent.KEYCODE_NUMPAD_7, 0x47,
				KeyEvent.KEYCODE_NUMPAD_8, 0x48, KeyEvent.KEYCODE_NUMPAD_9, 0x49,
				KeyEvent.KEYCODE_NUMPAD_SUBTRACT, 0x4A, KeyEvent.KEYCODE_NUMPAD_4, 0x4B,
				KeyEvent.KEYCODE_NUMPAD_5, 0x4C, KeyEvent.KEYCODE_NUMPAD_6, 0x4D,
				KeyEvent.KEYCODE_NUMPAD_ADD, 0x4E, KeyEvent.KEYCODE_NUMPAD_1, 0x4F,
				KeyEvent.KEYCODE_NUMPAD_2, 0x50, KeyEvent.KEYCODE_NUMPAD_3, 0x51,
				KeyEvent.KEYCODE_NUMPAD_0, 0x52, KeyEvent.KEYCODE_NUMPAD_DOT, 0x53,
				KeyEvent.KEYCODE_F11, 0x57, KeyEvent.KEYCODE_F12, 0x58,
				KeyEvent.KEYCODE_NUMPAD_ENTER, 0x9C, KeyEvent.KEYCODE_CTRL_RIGHT, 0x9D,
				KeyEvent.KEYCODE_NUMPAD_DIVIDE, 0xB5, KeyEvent.KEYCODE_ALT_RIGHT, 0xB8,
				KeyEvent.KEYCODE_MOVE_HOME, 0xC7, KeyEvent.KEYCODE_DPAD_UP, 0xC8,
				KeyEvent.KEYCODE_PAGE_UP, 0xC9, KeyEvent.KEYCODE_DPAD_LEFT, 0xCB,
				KeyEvent.KEYCODE_DPAD_RIGHT, 0xCD, KeyEvent.KEYCODE_MOVE_END, 0xCF,
				KeyEvent.KEYCODE_DPAD_DOWN, 0xD0, KeyEvent.KEYCODE_PAGE_DOWN, 0xD1,
				KeyEvent.KEYCODE_INSERT, 0xD2, KeyEvent.KEYCODE_FORWARD_DEL, 0xD3,
		};
		for (int i = 0; i < pairs.length; i += 2) {
			SCAN_CODES[pairs[i]] = pairs[i + 1];
		}
	}

	private final int controls;
	private boolean flying, leftDown, rightDown;
	private boolean[] padPressed = new boolean[PAD_STICK_RIGHT + 1];
	private float lastMouseX = Float.NaN, lastMouseY = Float.NaN;
	private boolean descentRunning, paused, surfaceWasDestroyed, textActive;
	private Context context;
	private DescentView thiz;
	private Handler mainHandler;
	private InputMethodManager imm;
	private final Object renderThreadObj = new Object();
	private Point size;
	private SurfaceHolder holder;

	public DescentView(Context context, int controls) {
		super(context);
		this.controls = controls;
		this.context = context;
		this.descentRunning = false;
		this.holder = getHolder();
		this.thiz = this;
		this.textActive = false;
		this.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		this.mainHandler = new Handler(context.getMainLooper());
		this.setFocusableInTouchMode(true);
		holder.addCallback(this);
		if (controls == DescentActivity.CONTROLS_KEYBOARD_MOUSE) {
			watchFlight();
		}
	}

	/**
	 * Whether the sources of an input device are those of a game controller
	 */
	static boolean isController(int sources) {
		return (sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
				|| (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
	}

	// The game does not say when it starts or stops flying, so look often.
	// While flying the mouse steers, so its pointer is hidden (or captured
	// where Android can) and its buttons fire; in menus it is the pointer again.
	private void watchFlight() {
		mainHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean nowFlying = descentRunning && inGameView();
				if (nowFlying != flying) {
					flying = nowFlying;
					lastMouseX = lastMouseY = Float.NaN;
					if (Build.VERSION.SDK_INT >= 26) {
						if (flying) {
							requestPointerCapture();
						} else {
							releasePointerCapture();
						}
					} else if (Build.VERSION.SDK_INT >= 24) {
						setPointerIcon(PointerIcon.getSystemIcon(context, flying
								? PointerIcon.TYPE_NULL : PointerIcon.TYPE_DEFAULT));
					}
				}
				mainHandler.postDelayed(this, 100);
			}
		}, 100);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		// The capture ends when the window loses the focus; take it again
		if (Build.VERSION.SDK_INT >= 26 && hasWindowFocus && flying) {
			requestPointerCapture();
		}
	}

	@Override
	public boolean onCapturedPointerEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				mouseMoved((int) event.getX(), (int) event.getY());
				return true;
			case MotionEvent.ACTION_BUTTON_PRESS:
			case MotionEvent.ACTION_BUTTON_RELEASE:
				mouseButton(event);
				return true;
		}
		return false;
	}

	// A press can come both as a touch and as a hover event; count it once
	@TargetApi(23)
	private void mouseButton(MotionEvent event) {
		boolean down = event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS;

		if (event.getActionButton() == MotionEvent.BUTTON_PRIMARY && leftDown != down) {
			leftDown = down;
			mouseHandler((short) event.getX(), (short) event.getY(), down);
		} else if (event.getActionButton() == MotionEvent.BUTTON_SECONDARY && rightDown != down) {
			rightDown = down;
			mouseButton(MB_RIGHT, down);
		}
	}

	/**
	 * Mouse moves, and the sticks and triggers of a controller
	 */
	public boolean handleGenericMotion(MotionEvent event) {
		int action = event.getActionMasked();

		if (isController(event.getSource()) && action == MotionEvent.ACTION_MOVE) {
			controllerMoved(event);
			return true;
		}
		if (!event.isFromSource(InputDevice.SOURCE_MOUSE)) {
			return false;
		}
		if (Build.VERSION.SDK_INT >= 23 && (action == MotionEvent.ACTION_BUTTON_PRESS
				|| action == MotionEvent.ACTION_BUTTON_RELEASE)) {
			mouseButton(event);
			return true;
		}
		if (action == MotionEvent.ACTION_HOVER_MOVE && flying) {
			if (Build.VERSION.SDK_INT >= 24) {
				mouseMoved((int) event.getAxisValue(MotionEvent.AXIS_RELATIVE_X),
						(int) event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y));
			} else {
				// No relative axes before Android 7: the pointer stops at the edges
				if (!Float.isNaN(lastMouseX)) {
					mouseMoved((int) (event.getX() - lastMouseX), (int) (event.getY() - lastMouseY));
				}
				lastMouseX = event.getX();
				lastMouseY = event.getY();
			}
			return true;
		}
		return false;
	}

	private static float axis(MotionEvent event, int axis) {
		float value = event.getAxisValue(axis);
		if (Math.abs(value) < DEAD_ZONE) {
			return 0;
		}
		return (value - Math.signum(value) * DEAD_ZONE) / (1 - DEAD_ZONE);
	}

	private void controllerMoved(MotionEvent event) {
		float x = event.getAxisValue(MotionEvent.AXIS_X);
		float y = event.getAxisValue(MotionEvent.AXIS_Y);
		float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
		float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
		float leftTrigger = Math.max(event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
				event.getAxisValue(MotionEvent.AXIS_BRAKE));
		float rightTrigger = Math.max(event.getAxisValue(MotionEvent.AXIS_RTRIGGER),
				event.getAxisValue(MotionEvent.AXIS_GAS));

		// The left stick flies, the right one aims
		padStick(axis(event, MotionEvent.AXIS_Z), axis(event, MotionEvent.AXIS_RZ));
		press(PAD_STICK_UP, y < -PRESS);
		press(PAD_STICK_DOWN, y > PRESS);
		press(PAD_STICK_LEFT, x < -PRESS);
		press(PAD_STICK_RIGHT, x > PRESS);
		press(PAD_DPAD_UP, hatY < -PRESS);
		press(PAD_DPAD_DOWN, hatY > PRESS);
		press(PAD_DPAD_LEFT, hatX < -PRESS);
		press(PAD_DPAD_RIGHT, hatX > PRESS);
		press(PAD_L2, leftTrigger > PRESS);
		press(PAD_R2, rightTrigger > PRESS);
	}

	// A button can come both as a key and as an axis; press it once
	private void press(int button, boolean pressed) {
		if (padPressed[button] != pressed) {
			padPressed[button] = pressed;
			padButton(button, pressed);
		}
	}

	private static int padButtonOf(int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BUTTON_A: return PAD_A;
			case KeyEvent.KEYCODE_BUTTON_B: return PAD_B;
			case KeyEvent.KEYCODE_BUTTON_X: return PAD_X;
			case KeyEvent.KEYCODE_BUTTON_Y: return PAD_Y;
			case KeyEvent.KEYCODE_BUTTON_L1: return PAD_L1;
			case KeyEvent.KEYCODE_BUTTON_R1: return PAD_R1;
			case KeyEvent.KEYCODE_BUTTON_L2: return PAD_L2;
			case KeyEvent.KEYCODE_BUTTON_R2: return PAD_R2;
			case KeyEvent.KEYCODE_BUTTON_THUMBL: return PAD_THUMBL;
			case KeyEvent.KEYCODE_BUTTON_THUMBR: return PAD_THUMBR;
			case KeyEvent.KEYCODE_BUTTON_START: return PAD_START;
			case KeyEvent.KEYCODE_BUTTON_SELECT: return PAD_SELECT;
			case KeyEvent.KEYCODE_DPAD_UP: return PAD_DPAD_UP;
			case KeyEvent.KEYCODE_DPAD_DOWN: return PAD_DPAD_DOWN;
			case KeyEvent.KEYCODE_DPAD_LEFT: return PAD_DPAD_LEFT;
			case KeyEvent.KEYCODE_DPAD_RIGHT: return PAD_DPAD_RIGHT;
			case KeyEvent.KEYCODE_DPAD_CENTER: return PAD_A;
			default: return -1;
		}
	}

	/**
	 * Keys of a keyboard or a controller. Text being typed goes through
	 * onKeyDown and onKeyUp as before.
	 */
	public boolean handleKey(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		int button, scanCode;

		if (!descentRunning || (action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_UP)) {
			return false;
		}
		button = padButtonOf(keyCode);
		if (button != -1 && event.getDevice() != null
				&& isController(event.getDevice().getSources())) {
			press(button, action == KeyEvent.ACTION_DOWN);
			return true;
		}
		if (textActive || keyCode >= SCAN_CODES.length) {
			return false;
		}
		scanCode = SCAN_CODES[keyCode];
		if (scanCode == 0) {
			return false;
		}
		if (event.getRepeatCount() == 0) {
			keyHandler((char) scanCode, action == KeyEvent.ACTION_DOWN);
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		int i, historySize, firstPointerIndex, numPointers;
		int action = event.getActionMasked();
		float prevX, prevY;
		boolean touchHandled = false;

		// The mouse buttons come as their own events, see handleGenericMotion
		if (Build.VERSION.SDK_INT >= 23 && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
			if (action == MotionEvent.ACTION_BUTTON_PRESS
					|| action == MotionEvent.ACTION_BUTTON_RELEASE) {
				mouseButton(event);
			} else {
				mouseSetPos((short) event.getX(), (short) event.getY());
			}
			return true;
		}

		historySize = event.getHistorySize();
		if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
			firstPointerIndex = event.getActionIndex();
		} else {
			firstPointerIndex = 0;
		}
		if (action == MotionEvent.ACTION_MOVE) {
			numPointers = event.getPointerCount();
		} else {
			numPointers = 1;
		}
		for (i = firstPointerIndex; i < numPointers + firstPointerIndex; ++i) {
			if (historySize > 0) {
				prevX = event.getHistoricalX(i, 0);
				prevY = event.getHistoricalY(i, 0);
			} else {
				prevX = event.getX(i);
				prevY = event.getY(i);
			}
			touchHandled |= touchHandler(event.getActionMasked(), event.getPointerId(i),
					event.getX(i), event.getY(i), prevX, prevY);
		}
		if (!touchHandled && (action == MotionEvent.ACTION_DOWN ||
				action == MotionEvent.ACTION_UP)) {
			mouseHandler((short) event.getX(), (short) event.getY(),
					action == MotionEvent.ACTION_DOWN);
			return true;
		} else {
			mouseSetPos((short) event.getX(), (short) event.getY());
		}
		return touchHandled;
	}

	@SuppressWarnings("unused")
	private boolean textIsActive() {
		return textActive;
	}

	@SuppressWarnings("unused")
	private void activateText() {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				requestFocus();
				imm.showSoftInput(thiz, InputMethodManager.SHOW_FORCED);
				textActive = true;
			}
		});
	}

	@SuppressWarnings("unused")
	private void deactivateText() {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
				clearFocus();
				textActive = false;
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DEL) {
			keyHandler((char) 0x0E, false);
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			keyHandler((char) 0x1C, false);
		} else {
			keyHandler((char) event.getUnicodeChar(), false);
		}
		return event.getUnicodeChar() != 0;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DEL) {
			keyHandler((char) 0x0E, true);
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			keyHandler((char) 0x1C, true);
		} else {
			keyHandler((char) event.getUnicodeChar(), true);
		}
		return event.getUnicodeChar() != 0;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && textActive) {
			sendEscape();
			return true;
		}
		return false;
	}

	/**
	 * Sends an Escape key press to Descent
	 */
	public void sendEscape() {
		keyHandler((char) 0x01, true);
		keyHandler((char) 0x01, false);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void surfaceCreated(SurfaceHolder holder) {
		if (!descentRunning) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					size = new Point();
					if (Build.VERSION.SDK_INT >= 19) {
						display.getRealSize(size);
					} else if (Build.VERSION.SDK_INT >= 13) {
						display.getSize(size);
					} else {
						size.set(display.getWidth(), display.getHeight());
					}
					initEgl();

					// Prepare for Descent...
					descentMain(size.x, size.y, context, thiz, context.getAssets(),
							context.getFilesDir().getAbsolutePath(),
							context.getCacheDir().getAbsolutePath(),
							((DescentActivity) context).getDataPath());

					// The player chose Quit. Descent keeps its state in native globals
					// and cannot be restarted in this process, so close the activity
					// and end the process to free everything.
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							Activity activity = (Activity) context;
							if (Build.VERSION.SDK_INT >= 21) {
								activity.finishAndRemoveTask();
							} else {
								activity.finish();
							}
							System.exit(0);
						}
					});
				}
			}).start();
			descentRunning = true;
		} else {
			this.holder = holder;
			resumeRenderThread();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceWasDestroyed = true;
		surfaceWasDestroyed();
	}

	public boolean getSurfaceWasDestroyed() {
		return surfaceWasDestroyed;
	}

	public void resumeRenderThread() {
		synchronized (renderThreadObj) {
			paused = false;
			surfaceWasDestroyed = false;
			renderThreadObj.notifyAll();
		}
	}

	@SuppressWarnings("unused")
	private void pauseRenderThread() {
		synchronized (renderThreadObj) {
			paused = true;
			while (paused) {
				try {
					renderThreadObj.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Initializes EGL for the current thread
	 */
	private void initEgl() {
		int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		int[] num_config = new int[1];
		final EGLConfig configs[] = new EGLConfig[1];
		int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 1, EGL10.EGL_NONE};
		EGL10 egl;
		EGLConfig eglConfig;
		EGLContext eglContext;
		EGLDisplay eglDisplay;
		EGLSurface eglSurface;
		GL10 gl;

		egl = (EGL10) EGLContext.getEGL();
		eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		egl.eglInitialize(eglDisplay, new int[]{1, 0});
		egl.eglChooseConfig(eglDisplay, new int[]{
				EGL10.EGL_RED_SIZE, 8,
				EGL10.EGL_GREEN_SIZE, 8,
				EGL10.EGL_BLUE_SIZE, 8,
				EGL10.EGL_ALPHA_SIZE, 8,
				EGL10.EGL_DEPTH_SIZE, 16,
				EGL10.EGL_STENCIL_SIZE, 0,
				EGL10.EGL_NONE}, configs, 1, num_config);
		eglConfig = configs[0];
		eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
		eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, holder, null);
		egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
		gl = (GL10) eglContext.getGL();

		// Clear to back
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Enable culling
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK);

		// Viewport is screen bounds
		gl.glViewport(0, 0, size.x, size.y);

		// Enable blending for alphas
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	private static native void keyHandler(char key, boolean down);

	private static native boolean inGameView();

	private static native void mouseMoved(int dx, int dy);

	private static native void mouseButton(int button, boolean down);

	private static native void padStick(float x, float y);

	private static native void padButton(int button, boolean down);

	private static native void mouseHandler(short x, short y, boolean down);

	private static native void mouseSetPos(short x, short y);

	private static native boolean touchHandler(int action, int pointerId, float x, float y,
											   float prevX, float prevY);

	private static native void surfaceWasDestroyed();

	private static native void descentMain(int w, int h, Context activity,
										   DescentView descentView,
										   AssetManager assetManager, String documentPath,
										   String cachePath, String dataPath);
}
