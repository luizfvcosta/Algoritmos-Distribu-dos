package projects.bully_election_std.states;

import projects.bully_election_std.CustomGlobal;
import projects.bully_election_std.nodes.messages.BullyMessage;
import projects.bully_election_std.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_std.nodes.timers.ElectionTimeoutTimer;
import sinalgo.tools.Tools;

public class ElectionNodeStateNormal extends ElectionNodeState {
    boolean responded = true;

    CustomGlobal global;

    public ElectionNodeStateNormal(ElectionNode ctx) {
        super(ctx);

        global = (CustomGlobal) Tools.getCustomGlobal();
        ctx.c = 0;

        Tools.appendToOutput("Node " + ctx.ID + " going back to normal\n");
        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYOk);
        ctx.activeTimeout.startRelative(6, ctx);
    }

    @Override
    public void handleAYUp(BullyMessage msg) {
        reply(msg);
        ctx.reliability++;

        ctx.activeTimeout.shouldFire = false;
        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYOk);
        ctx.activeTimeout.startRelative(6, ctx);
    }

    @Override
    public void handleAYOk(BullyMessage msg) {

    }

    @Override
    public void handleEnterElection(BullyMessage msg) {
        Tools.appendToOutput("Node " + ctx.ID + " invited to take part in election\n");
        if(ctx.ID < msg.senderId){
            Tools.appendToOutput("Node " + ctx.ID + " giving up on CADIDATE state\n");
            reply(msg);
            ctx.setState(States.ElectionParticipant);
        }else{
        ctx.setState(States.ElectionCandidate);
        }
    }

    @Override
    public void handleSetCoordinator(BullyMessage msg) {

    }

    @Override
    public void handleSetState(BullyMessage msg) {

    }

    @Override
    public void handleAck(BullyMessage msg) {
        if (msg.type == BullyMessage.MessageType.AYOk && msg.senderId == ctx.coordinatorId) {
            Tools.appendToOutput("Node " + ctx.ID + " coordinator responded to ping\n");
            responded = true;
        }
    }

    @Override
    public void handleTimeout() {
        if (!responded) {
            Tools.appendToOutput("Node " + ctx.ID + " coordinator failed to respond to ping - calling elections\n");
            ctx.setState(States.ElectionCandidate);
        } else {
            Tools.appendToOutput("Node " + ctx.ID + " pinging coordinator\n");
            
            responded = false;
            BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.AYOk, false);
            

            ctx.send(msg, Tools.getNodeByID((int) ctx.coordinatorId));
            
            global.messagesSent++;
            ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYOk);
            ctx.activeTimeout.startRelative(3, ctx);
        }
    }

    @Override
    public void handleUpdate() {
        global.workDone++;

        if (ctx.ID > ctx.coordinatorId){
            ctx.setState(States.ElectionCandidate);
            //ctx.coordinatorId = -1;
            ctx.c = -1;
        }
    }
}
