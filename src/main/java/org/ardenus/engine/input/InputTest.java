package org.ardenus.engine.input;

import java.text.DecimalFormat;

import org.ardenus.engine.graphics.window.Window;
import org.ardenus.engine.input.device.Controller;
import org.ardenus.engine.input.device.GameCubeController;
import org.ardenus.engine.input.device.InputDevice;
import org.ardenus.engine.input.device.Keyboard;
import org.ardenus.engine.input.device.PlayStationController;
import org.ardenus.engine.input.device.SwitchController;
import org.ardenus.engine.input.device.XboxController;
import org.ardenus.engine.input.device.event.DeviceConnectEvent;
import org.ardenus.engine.input.device.event.FeaturePressEvent;
import org.ardenus.engine.input.device.seeker.USBGameCubeSeeker;

import com.whirvex.event.EventHandler;
import com.whirvex.event.EventListener;
import com.whirvex.event.EventManager;

public class InputTest implements EventListener {

	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@EventHandler
	public void onPress(FeaturePressEvent event) {
		Direction d = event.getDirection();
		System.out.println(event.getDevice().getClass().getSimpleName()
				+ " pressed " + event.getFeature().id()
				+ (d != null ? " (in " + d + " direction)" : ""));

	}
	
	@EventHandler
	public void onConnect(DeviceConnectEvent event) {
		System.out.println(event.getDevice().getClass().getSimpleName() + " connected");
	}

	public static void main(String[] args) {
		Window.init();
		Window window = new Window(1024, 768, "input");

		EventManager e = new EventManager();
		e.register(new InputTest());

		Input.init(e);

		Input.addSeeker(window.createSeeker(Keyboard.class));
		Input.addSeeker(window.createSeeker(XboxController.class));
		Input.addSeeker(window.createSeeker(SwitchController.class));
		Input.addSeeker(window.createSeeker(PlayStationController.class));
		Input.addSeeker(new USBGameCubeSeeker());

		while (!window.shouldClose()) {
			Window.pollEvents();
			Input.poll();

			for (InputDevice d : Input.connected()) {
				if (!(d instanceof Controller)) {
					continue;
				}

				Controller c = (Controller) d;
				if (c.isPressed(GameCubeController.A)) {
					c.setVibration(0.5F);
				} else {
					c.setVibration(0.0F);
				}
			}

			window.swapBuffers();
		}

		window.close();
		Window.terminate();
	}

}
