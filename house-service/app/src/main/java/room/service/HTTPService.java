package room.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HTTPService extends AbstractVerticle {

	private int port;
	private ServerWebSocket clientWebSocket;

	public HTTPService(int port) {
		this.port = port;
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
		// implementa la logica per gestire le luci dalla dashboard (ricevi i dati dalla dashboard e inviali al servizio MQTT)
	}

	private void handleWebSocketClose(Void unused) {
		clientWebSocket = null;
	}

	public void handleSendData(String message) {
		if (clientWebSocket != null) {
			clientWebSocket.writeTextMessage(message);
		}
	}

	private void log(String msg) {
		System.out.println("[DATA SERVICE] " + msg);
	}
}
