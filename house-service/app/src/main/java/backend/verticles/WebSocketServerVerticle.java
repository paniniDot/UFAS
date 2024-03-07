package backend.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;

public class WebSocketServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HttpServer httpServer = createHttpServer("localhost", 8080);

        Router router = Router.router(vertx);

        configureEventBusSubRouter(router, createSockJSBridgeRouter());
        
        httpServer.requestHandler(router);

        handleIncomingWebClientMessage();
        handleIncomingMqttMessage();

        httpServer.listen(ar -> {
            if (ar.succeeded()) {
                log("Server started on port " + ar.result().actualPort());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    private HttpServer createHttpServer(final String host, final int port) {
        return vertx.createHttpServer(new HttpServerOptions().setHost(host).setPort(port));
    }

    private Router createSockJSBridgeRouter() {
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("webclient.to.webserver"))
                .addOutboundPermitted(new PermittedOptions().setAddress("webserver.to.webclient"));
    
        return SockJSHandler.create(vertx).bridge(options, this::handleBridgeEvent);
    }
    
    private void configureEventBusSubRouter(Router router, Router subRouter) {
        router.route("/eventbus/*").subRouter(subRouter);
    }
    
    private void handleBridgeEvent(BridgeEvent be) {
        if (be.type() == BridgeEventType.SOCKET_CREATED) {
            log("Client connected: " + be.socket().remoteAddress());
        }
        be.complete(true);
    }

    private void handleIncomingWebClientMessage() {
        vertx.eventBus().consumer("webclient.to.webserver", message -> {
            String webMessage = message.body().toString();
            vertx.eventBus().publish("webserver.to.mqttserver", webMessage);
        });
    }

    private void handleIncomingMqttMessage() {
        vertx.eventBus().consumer("mqttserver.to.webserver", message -> {
            String mqttMessage = message.body().toString();

            vertx.eventBus().publish("webserver.to.webclient", mqttMessage);
        });
    }

    private void log(String message) {
        System.out.println("[WebServer] " + message);
    }
}
