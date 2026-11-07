//
// The keyboard and mouse, and the game controller
//

#include <jni.h>

#include "game.h"
#include "key.h"

// The way to play the player picks at launch, see DescentActivity.chooseControls
#define CONTROLS_TOUCH 0
#define CONTROLS_KEYBOARD_MOUSE 1
#define CONTROLS_CONTROLLER 2

// Controller buttons as DescentView sends them
#define PAD_A 0
#define PAD_B 1
#define PAD_X 2
#define PAD_Y 3
#define PAD_L1 4
#define PAD_R1 5
#define PAD_L2 6
#define PAD_R2 7
#define PAD_THUMBL 8
#define PAD_THUMBR 9
#define PAD_START 10
#define PAD_SELECT 11
#define PAD_DPAD_UP 12
#define PAD_DPAD_DOWN 13
#define PAD_DPAD_LEFT 14
#define PAD_DPAD_RIGHT 15
#define PAD_STICK_UP 16
#define PAD_STICK_DOWN 17
#define PAD_STICK_LEFT 18
#define PAD_STICK_RIGHT 19
#define PAD_BUTTONS 20

// How fast the right stick turns the ship, as a mouse moving that far each frame
#define STICK_SPEED 40.0f

extern int Touch_controls_shown;
extern int Relative_mouse;

extern void key_handler(unsigned char scancode, bool down);
extern void mouse_button_handler(int button, bool down);

// Keys in flight: the left stick flies as WASD, the right one aims
static const unsigned char flight_keys[PAD_BUTTONS] = {
		KEY_PADMINUS,	// A: slide up
		KEY_PADPLUS,	// B: slide down
		KEY_COMMA,		// X: cycle primary
		KEY_PERIOD,		// Y: cycle secondary
		KEY_Q,			// L1: bank left
		KEY_E,			// R1: bank right
		KEY_SPACEBAR,	// L2: fire secondary
		KEY_LCTRL,		// R2: fire primary
		KEY_LSHIFT,		// left stick click: afterburner
		KEY_R,			// right stick click: rear view
		KEY_ESC,		// Start: menu
		KEY_TAB,		// Select: automap
		KEY_F,			// up: flare
		KEY_H,			// down: headlight
		KEY_T,			// left: energy to shield
		KEY_F3,			// right: cockpit view
		KEY_W, KEY_S, KEY_A, KEY_D,
};

// Keys in menus, briefings and movies
static const unsigned char menu_keys[PAD_BUTTONS] = {
		KEY_ENTER,		// A
		KEY_ESC,		// B
		0, 0, 0, 0, 0, 0, 0, 0,
		KEY_ENTER,		// Start
		0,
		KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT,
		KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT,
};

// The key each button holds down, to let go of the same one in a menu
static unsigned char pad_pressed[PAD_BUTTONS];

// Written on the UI thread, read on the game thread
static int mouse_dx, mouse_dy;
static volatile float stick_x, stick_y;

int in_game_view() {
	return Game_mode == GM_NORMAL && !In_screen;
}

// Called from mouse_get_delta on the game thread. The mouse gives how far it
// moved, the stick how far it leans, so the ship turns while it is held.
// Screen y grows downwards, and pushing away lifts the nose.
void take_mouse_delta(int *dx, int *dy) {
	*dx = __atomic_exchange_n(&mouse_dx, 0, __ATOMIC_RELAXED) + (int) (stick_x * STICK_SPEED);
	*dy = -__atomic_exchange_n(&mouse_dy, 0, __ATOMIC_RELAXED) - (int) (stick_y * STICK_SPEED);
}

JNIEXPORT void JNICALL
Java_astrohacker_descent2_DescentActivity_setControls(JNIEnv *env, jclass clazz, jint controls) {
	Touch_controls_shown = controls == CONTROLS_TOUCH;
	Relative_mouse = controls != CONTROLS_TOUCH;
}

JNIEXPORT jboolean JNICALL
Java_astrohacker_descent2_DescentView_inGameView(JNIEnv *env, jclass clazz) {
	return (jboolean) in_game_view();
}

JNIEXPORT void JNICALL
Java_astrohacker_descent2_DescentView_mouseMoved(JNIEnv *env, jclass clazz, jint dx, jint dy) {
	__atomic_add_fetch(&mouse_dx, dx, __ATOMIC_RELAXED);
	__atomic_add_fetch(&mouse_dy, dy, __ATOMIC_RELAXED);
}

JNIEXPORT void JNICALL
Java_astrohacker_descent2_DescentView_mouseButton(JNIEnv *env, jclass clazz, jint button,
												  jboolean down) {
	mouse_button_handler(button, down);
}

JNIEXPORT void JNICALL
Java_astrohacker_descent2_DescentView_padStick(JNIEnv *env, jclass clazz, jfloat x, jfloat y) {
	stick_x = x;
	stick_y = y;
}

JNIEXPORT void JNICALL
Java_astrohacker_descent2_DescentView_padButton(JNIEnv *env, jclass clazz, jint button,
												jboolean down) {
	unsigned char key;

	if (button < 0 || button >= PAD_BUTTONS) {
		return;
	}
	if (down) {
		key = in_game_view() ? flight_keys[button] : menu_keys[button];
		if (key != 0 && pad_pressed[button] == 0) {
			pad_pressed[button] = key;
			key_handler(key, true);
		}
	} else if (pad_pressed[button] != 0) {
		key_handler(pad_pressed[button], false);
		pad_pressed[button] = 0;
	}
}
