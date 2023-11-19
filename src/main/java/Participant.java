public interface Participant {
    /**
     * Call from coordinator to ask whether it can commit a transaction.
     *
     * @param tid id of the transaction of query
     * @return Always ready to commit in this simplified example
     */
    public boolean canCommit(String tid);

    /**
     * Call from coordinator to tell participant to commit its part of a transaction.
     *
     * @param tid id of the transaction to commit
     */
    public void doCommit(String tid);

    /**
     * Call from coordinator to tell participant to abort its part of a transaction
     *
     * @param tid id of the transaction to abort
     */
    public void doAbort(String tid);
}
