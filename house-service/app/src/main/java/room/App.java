package room;

import io.vertx.core.Vertx;
import room.service.HTTPService;
import room.service.MQTTService;
import room.service.SerialService;
import room.utils.JsonUtils;

public class App {

	public static void main(String[] args) throws Exception {
		// Create a blocking queue to store the received messages
		SerialService serial = new SerialService("COM12", 9600);
		Vertx vertx = Vertx.vertx();
		HTTPService service = new HTTPService(8080, serial);
		vertx.deployVerticle(service);
		Thread serverThread = new Thread(() -> {
			new MQTTService(1883, "192.168.2.2", serial);
		});
		Thread readThread = new Thread(() -> {
			while (true) {
				try {
					String msg = serial.receiveMsg();
					System.out.println("arduino " + msg);
					if (JsonUtils.isFromArduino(msg)) {
						service.handleSendData(JsonUtils.getJsonWithTimestamp(msg));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(1000);
		serverThread.start();
		Thread.sleep(1000);
		readThread.start();
	}

}
