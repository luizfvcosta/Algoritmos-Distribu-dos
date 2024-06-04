package projects.bully_election_std.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the ElectionNodes in the Election project. They
 * contain one bool as payload.
 */
public class BullyMessage extends Message{
    public enum MessageType {
        AYUp, AYOk, EnterElection, SetCoordinator, SetState
    }

    public int senderId;
    public long c;
    public int coordinatorId;
    public MessageType type;
    public boolean ack;

    public BullyMessage(int senderId, long c, int coordinatorId, MessageType type, boolean ack) {
        this.senderId = senderId;
        this.c = c;
        this.coordinatorId = coordinatorId;
        this.type = type;
        this.ack = ack;
    }
   
    @Override
    public Message clone() {
        return new BullyMessage(this.senderId, this.c, this.coordinatorId, this.type, this.ack);
    }
    
}
