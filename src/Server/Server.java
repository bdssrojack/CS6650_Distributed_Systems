package Server;

import Common.LoggerInitiator;
import Common.Protocol;
import Common.Request;
import Common.Response;

import java.io.EOFException;
import java.util.HashMap;
import java.util.logging.Logger;

public abstract class Server {
    int port;
    Protocol protocol;
    Logger logger;
    HashMap<String, String> store;

    public Server(int port) {
        this.port = port;
        store = new HashMap<>();
    }

    /**
     * Get the request from client
     *
     * @return a Request object
     * @throws EOFException notify the main loop the current client is disconnected.
     */
    public abstract Request getRequest() throws EOFException;

    /**
     * Response to client
     * @param response the response object
     * @param request request from client to identify the return address and port
     */
    public abstract void response(Response response, Request request);

    /**
     * Display the requests received from a particular
     * InetAddress and port number for a specific word
     * @param request
     */
    void log(Request request) {
        logger.info(String.format("Received %s query from %s:%s, key: %s, value(if applicable): %s.", request.operation.toString(), request.clientAddress, request.clientPort, request.key, request.value));
    }

    /**
     * Display the response to a query
     * @param response
     */
    void log(Response response) {
        if (response.status) {
            logger.info(response.content);
        } else {
            logger.severe(response.content);
        }
    }

    /**
     * manipulate PUT operation
     * if there's an existing entry, fail the operation
     * log and return the response
     * @param key
     * @param value
     * @return
     */
    Response put(String key, String value) {
        Response response = new Response();
        if (store.containsKey(key)) {
            response.status = true;
            response.content = String.format("Operation succeed. Updated key [%s] with value [%s].", key, value);
        } else {
            store.put(key, value);
            response.status = true;
            response.content = String.format("Operation succeed. Added key [%s] value [%s] pair to store.", key, value);
        }
        log(response);
        return response;
    }

    /**
     * manipulate GET operation
     * if no such key exist, fail the operation
     * log and return the response
     * @param key
     * @return
     */
    Response get(String key) {
        Response response = new Response();
        if (!store.containsKey(key)) {
            response.status = false;
            response.content = String.format("Operation failed. No such key [%s] in store.", key);
        } else {
            response.status = true;
            response.content = String.format("Operation succeed. Value of key [%s] in store is: %s.", key, store.get(key));
        }
        log(response);
        return response;
    }

    /**
     * execute DELETE operation
     * of no such key exist, fail the operation
     * log and return the response
     * @param key
     * @return
     */
    Response delete(String key) {
        Response response = new Response();
        if (!store.containsKey(key)) {
            response.status = false;
            response.content = String.format("Operation failed. No such key [%s] in store.", key);
        } else {
            String v = store.remove(key);
            response.status = true;
            response.content = String.format("Operation succeed. key-value pair [%s-%s] was deleted", key, v);
        }
        log(response);
        return response;
    }

    /**
     * Start the server main loop listening to client requests.
     */
    abstract void run();

    /**
     * Close the server
     */
    abstract void close();
}
