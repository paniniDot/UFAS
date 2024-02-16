package room.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jssc.SerialPortException;

public class HTTPService extends AbstractVerticle {

	private int port;
	private SerialService serial;
	private ServerWebSocket clientWebSocket;

	public HTTPService(int port, SerialService serial) {
		this.port = port;
		this.serial = serial;
	}

	@Override
	public void start() {
		HttpServer server = vertx.createHttpServer();

		server.webSocketHandler(webSocket -> {
			if (clientWebSocket == null) {
				clientWebSocket = webSocket;

				webSocket.textMessageHandler(this::handleReceiveData);
				webSocket.closeHandler(this::handleWebSocketClose);
			} else {
				webSocket.reject();
			}
		});

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		server.requestHandler(router).listen(port);

		log("Service ready on port: " + port);
	}

	private void handleReceiveData(String message) {
		try {
			serial.sendMsg(message);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	private void handleWebSocketClose(Void unused) {
		clientWebSocket = null;
	}

	public void handleSendData(String jsonMeasure) {
		if (clientWebSocket != null) {
			clientWebSocket.writeTextMessage(jsonMeasure);
		}
	}

	private void log(String msg) {
		System.out.println("[DATA SERVICE] " + msg);
	}
}
