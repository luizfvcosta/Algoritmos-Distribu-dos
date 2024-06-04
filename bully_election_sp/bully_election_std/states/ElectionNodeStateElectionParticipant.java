package projects.bully_election_std.states;

import projects.bully_election_std.nodes.messages.BullyMessage;
import projects.bully_election_std.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_std.nodes.timers.ElectionTimeoutTimer;
import sinalgo.tools.Tools;

public class ElectionNodeStateElectionParticipant extends ElectionNodeState {
    public ElectionNodeStateElectionParticipant(ElectionNode ctx) {
        super(ctx);
        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.SetCoordinator);
        ctx.activeTimeout.startRelative(8, ctx);
        Tools.appendToOutput("Node " + ctx.ID + " joining election\n");
    }

    @Override
    public void handleAYUp(BullyMessage msg) {
        reply(msg);
    }

    @Override
    public void handleAYOk(BullyMessage msg) {

    }

    @Override
    public void handleEnterElection(BullyMessage msg) {
        int senderId = msg.senderId;
        Tools.appendToOutput("Node " + ctx.ID + " entering election from node " + senderId + "\n");

        reply(msg);

        ctx.activeTimeout.shouldFire = false;
        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.SetCoordinator);
        ctx.activeTimeout.startRelative(8, ctx);
    }

    @Override
    public void handleSetCoordinator(BullyMessage msg) {
        int senderId = msg.senderId;
        Tools.appendToOutput("Node " + ctx.ID + " accepting new coordinator " + senderId + "\n");

        reply(msg);

        ctx.activeTimeout.shouldFire = false;
        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.SetState);
        ctx.activeTimeout.startRelative(8, ctx);
    }

    @Override
    public void handleSetState(BullyMessage msg) {
        int senderId = msg.senderId;
        Tools.appendToOutput("Node " + ctx.ID + " accepting election result\n");

        ctx.coordinatorId = msg.coordinatorId;

        reply(msg);

        ctx.setState(States.Normal);
    }

    @Override
    public void handleAck(BullyMessage msg) {

    }

    @Override
    public void handleTimeout() {
        Tools.appendToOutput("Node " + ctx.ID + " election went silent, trying to call new one\n");
        ctx.setState(States.ElectionCandidate);
    }

    @Override
    public void handleUpdate() {

    }
}
