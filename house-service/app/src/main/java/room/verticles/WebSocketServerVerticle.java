package room.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;

public class WebSocketServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HttpServerOptions httpOptions = new HttpServerOptions().setHost("localhost").setPort(8080);
        HttpServer httpServer = vertx.createHttpServer(httpOptions);

        Router router = Router.router(vertx);

        // Enable WebSocket and SockJS support
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("mqtt.message"));
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);

        httpServer.requestHandler(router);

        httpServer.listen(ar -> {
            if (ar.succeeded()) {
                System.out.println("WebSocket server started on port " + ar.result().actualPort());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
