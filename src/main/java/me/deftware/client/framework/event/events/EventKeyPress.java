package me.deftware.client.framework.event.events;

import me.deftware.client.framework.event.Event;

public class EventKeyPress extends Event {

	private int key;

	public EventKeyPress(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

}
