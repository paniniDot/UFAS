package room.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;

public class WebSocketServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HttpServerOptions httpOptions = new HttpServerOptions().setHost("localhost").setPort(8080);
        HttpServer httpServer = vertx.createHttpServer(httpOptions);

        Router router = Router.router(vertx);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                                            .addInboundPermitted(new PermittedOptions().setAddress("web.to.mqtt"))
                                            .addOutboundPermitted(new PermittedOptions().setAddress("mqtt.to.web"));
        

        router.route("/eventbus/*").subRouter(sockJSHandler.bridge(options, be -> {
            if (be.type() == BridgeEventType.SOCKET_CREATED) {
                log("Client connected: " + be.socket().remoteAddress());
            } else if (be.type() == BridgeEventType.RECEIVE) {
                log("Received message from client: " + be.getRawMessage());
                vertx.eventBus().publish("web.to.mqtt", be.getRawMessage());
            }
            be.complete(true);
        }));

        vertx.eventBus().consumer("mqtt.to.web", message -> {
            String mqttMessage = (String) message.body();
            
            vertx.eventBus().publish("mqtt.to.web", mqttMessage);
            log("Sent MQTT message to clients: " + mqttMessage);
        });
        
        httpServer.requestHandler(router);

        httpServer.listen(ar -> {
            if (ar.succeeded()) {
                log("Server started on port " + ar.result().actualPort());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    private void log(String message) {
        System.out.println("[WebServer] " + message);
    }
}
