package org.travelbot.java;

import org.joo.scorpius.Application;
import org.travelbot.java.MessengerVertxBootstrap;

public class Main {

	public static void main(String[] args) {
		Application app = new Application();
		app.run(new MessengerVertxBootstrap());
	}
}
