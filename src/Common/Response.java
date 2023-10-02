package Common;

/**
 * Class of response created by server and logged out in client side
 */
public class Response implements java.io.Serializable{
    public boolean status;
    public String content;

    public Response(boolean status, String content) {
        this.status = status;
        this.content = content;
    }

    public Response() {

    }
}
