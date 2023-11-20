import com.cs6650.server_client.Operation;
import com.cs6650.server_client.Request;

public class MessageLib {
    /* status */
    private static final String OPERATION_SUCCEED = "Operation SUCCEED. ";
    private static final String OPERATION_FAILED = "Operation FAILED. ";
    public static final String INVALID_OPERATION = "Invalid operation.";

    /*
    PUT
     */
    private static final String KEY_UPDATED = "Updated key [%s] with value [%s].";
    private static final String KEY_ADDED = "Added key [%s] - value [%s] pair to store.";
    private static final String KEY_PUT = "Put key [%s] - value [%s] pair to store.";
    private static final String PUT_FAILED = "Can not add/update key [%s] with empty value.";

    public static String UPDATE_SUCCEED(String key, String value) {
        return OPERATION_SUCCEED + String.format(KEY_UPDATED, key, value);
    }

    public static String PUT_SUCCEED(String key, String value) {
        return OPERATION_SUCCEED + String.format(KEY_PUT, key, value);
    }

    public static String ADD_SUCCEED(String key, String value) {
        return OPERATION_SUCCEED + String.format(KEY_ADDED, key, value);
    }

    public static String UPDATE_FAILED(String key) {
        return OPERATION_FAILED + String.format(PUT_FAILED, key);
    }

    /*
    GET
     */
    private static final String KEY_NOT_FOUND = "Key [%s] not found in store.";
    private static final String VALUE_FOUND = "Value of key [%s] is: %s.";

    public static String GET_SUCCEED(String key, String value) {
        return OPERATION_SUCCEED + String.format(VALUE_FOUND, key, value);
    }

    public static String GET_FAILED(String key) {
        return OPERATION_FAILED + String.format(KEY_NOT_FOUND, key);
    }

    /*
    DELETE
     */
    private static final String KEY_DELETED = "Key-value pair [%s - %s] was deleted";

    public static String DELETE_SUCCEED(String key, String value) {
        return OPERATION_SUCCEED + String.format(KEY_DELETED, key, value);
    }

    /*
    Request
     */
    public static final String REQUEST_WITH_VALUE = "[%s] key [%s] with value [%s].";
    public static final String REQUEST_WITHOUT_VALUE = "[%s] key [%s].";

    public static String REQUEST(Operation operation, String key, String value) {
        return "Request: " + (value.isEmpty() ? String.format(REQUEST_WITHOUT_VALUE, operation, key) :
                                String.format(REQUEST_WITH_VALUE, operation, key, value));
    }

    public static String REQUEST(Request request){
        return REQUEST(request.getOperation(), request.getKey(), request.getValue());
    }

}
