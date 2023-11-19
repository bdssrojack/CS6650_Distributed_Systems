import com.cs6650.server_client.Request;

public interface Coordinator {
    /**
     * Call from participant to confirm that it has committed the transaction with tid.
     *
     * @param tid id of the transaction that committed
     */
    public void haveCommitted(String tid);

    /**
     * Call from participant to ask for the decision on a transaction by tid when it
     * has voted Yes but has still had no reply after some delay. Used to recover from server
     * crash or delayed messages.
     *
     * @param tid id of the transaction of query
     * @return true if to commit, false if to abort
     */
    public boolean getDecision(String tid);

    /**
     * Call from participant to inform the coordinator that new transaction has been raised.
     *
     * @param tid transaction id
     * @param request detail of transaction
     */
    public void newRequest(String tid, Request request);
}
