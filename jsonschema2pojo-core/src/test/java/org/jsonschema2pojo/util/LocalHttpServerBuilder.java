/*
 * Copyright © 2010-2014 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * @author {@link "https://github.com/s13o" "s13o"}
 * @since 3/16/2017
 */
public class LocalHttpServerBuilder {

    public interface Server extends Closeable {

        /**
         * Start the Server on first available port from pointed range
         * @param from the first port to test for availability
         * @param count max number to increment the port value
         * @return port of the server
         */
        int startInRange(int from, int count);

        /**
         *
         * @return actual port or '-1' if the server has not been started
         */
        int getPort();
    }

    private static class Srv implements Server {
        private final List<Context> contexts = new ArrayList<Context>();
        private HttpServer server;

        private Srv(Context... context) {
            if (context == null || context.length == 0)
                throw new IllegalArgumentException("No context provided");
            Collections.addAll(contexts, context);
        }

        @Override
        public int getPort() {
            if (server != null)
                return server.getAddress().getPort();
            return -1;
        }

        @Override
        public void close() throws IOException {
            if (server != null)
                server.stop(0);
        }

        private int getFreePort(int from, int count) {
            for (int port = from; port < from + count; port++) {
                try {
                    new ServerSocket(port & 0xFFFF).close();
                    return port;
                } catch (IOException e) {
                }
            }
            throw new IllegalArgumentException(String.format("No free ports from %s to %s", from, from + count));
        }

        @Override
        public int startInRange(int from, int count) {
            final int port = getFreePort(from, count);
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                for (Context c : contexts) {
                    server.createContext(c.context, new ContextHandler(c));
                }
                server.setExecutor(Executors.newCachedThreadPool());
                server.start();
                return port;
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public static class Context {
        private final String context;
        private final String contentType;
        private final String encoding;
        private final byte[] content;

        private Context(String context, String contentType, String encoding, byte[] content) {
            this.context = context;
            this.contentType = contentType;
            this.encoding = encoding;
            this.content = content;
        }
    }

    private static class ContextHandler implements HttpHandler {
        private final Context context;

        private ContextHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                final String in = IOUtils.toString(exchange.getRequestBody(), context.encoding);
                exchange.getResponseHeaders().set("Content-Type", context.contentType);
                exchange.sendResponseHeaders(200, context.content.length);
                exchange.getResponseBody().write(context.content);
            } catch (Exception e) {
                exchange.getResponseHeaders().set("Content-Type", "plain/text");
                final byte[] out = e.toString().getBytes(context.encoding);
                exchange.sendResponseHeaders(500, out.length);
                exchange.getResponseBody().write(out);
            } finally {
                exchange.close();
            }
        }
    }

    public static Context context(String context, String contentType, String encoding, byte[] content) {
        return new Context(context, contentType, encoding, content);
    }

    public static Server createServer(Context... context) {
        return new Srv(context);
    }

}
