package room;

import io.vertx.core.Vertx;
import room.service.HTTPService;
import room.service.MQTTService;
import room.utils.JsonUtils;

public class App {

	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();
		HTTPService httpService = new HTTPService(8080);
		vertx.deployVerticle(service);
		Thread serverThread = new Thread(() -> {
			new MQTTService(1883, "192.168.2.2", httpService);
		});
		Thread.sleep(1000);
		serverThread.start();
	}

}
