package com.neuronrobotics.android;

public enum WalkingState {
	STOPPED,
	FORWARD,
	BACKWARD,
	TURN_LEFT,
	TURN_RIGHT,
	STRAIF_LEFT,
	STRAIF_RIGHT;
	
	public String toString() {
		switch(this) {
		case FORWARD:
			return "Forwards";
		case BACKWARD:
			return "Backwards";
		case TURN_LEFT:
			return "Turn Left";
		case TURN_RIGHT:
			return "Turn Right";
		case STRAIF_LEFT:
			return "Straif Left";
		case STRAIF_RIGHT:
			return "Straig Right";
		default:
			return "Stopped";
		}
	}
}
