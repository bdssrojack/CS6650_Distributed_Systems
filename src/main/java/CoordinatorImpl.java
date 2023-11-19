import com.cs6650.server_client.Request;

import java.util.List;

public class CoordinatorImpl implements Coordinator {
    private List<ParticipantImpl> participants;

    public CoordinatorImpl(List<String> participantHosts) {

    }

    public void haveCommitted(String tid) {

    }

    public void newRequest(String tid, Request request){

    }

    public boolean getDecision(String tid) {
        return false;
    }


}
