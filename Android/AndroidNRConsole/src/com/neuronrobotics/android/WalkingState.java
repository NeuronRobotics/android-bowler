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
			return "Walking Forwards";
		case BACKWARD:
			return "Walking Backwards";
		case TURN_LEFT:
			return "Turning Left";
		case TURN_RIGHT:
			return "Turning Right";
		case STRAIF_LEFT:
			return "Straifing Left";
		case STRAIF_RIGHT:
			return "Straifing Right";
		default:
			return "Stopped";
		}
	}
}
