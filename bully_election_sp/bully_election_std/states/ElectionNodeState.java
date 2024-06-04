package projects.bully_election_std.states;

import projects.bully_election_std.CustomGlobal;
import projects.bully_election_std.nodes.messages.BullyMessage;
import projects.bully_election_std.nodes.nodeImplementations.ElectionNode;
import sinalgo.tools.Tools;

public abstract class ElectionNodeState {

    public enum States {
        Normal, NormalCoordinator, ElectionCandidate, ElectionParticipant, Down
    }

    final ElectionNode ctx;
    final CustomGlobal global;

    public ElectionNodeState(ElectionNode ctx) {
        this.ctx = ctx;
        this.global = (CustomGlobal) Tools.getCustomGlobal();
        if (this.ctx.activeTimeout != null) {
            this.ctx.activeTimeout.shouldFire = false;
            this.ctx.activeTimeout = null;
        }
    }

    public final void handleMessage (BullyMessage msg) {
        if (msg.ack) {
            handleAck(msg);
        } else {
            switch (msg.type) {
                case AYUp:
                    handleAYUp(msg);
                    break;
                case AYOk:
                    handleAYOk(msg);
                    break;
                case EnterElection:
                    handleEnterElection(msg);
                    break;
                case SetCoordinator:
                    handleSetCoordinator(msg);
                    break;
                case SetState:
                    handleSetState(msg);
                    break;
            }
        }
    }

    public void reply(BullyMessage msg) {
        int senderId = msg.senderId;
        BullyMessage reply = (BullyMessage) msg.clone();
        reply.senderId = ctx.ID;
        reply.c = ctx.c;
        reply.coordinatorId = ctx.coordinatorId;
        reply.ack = true;
        ctx.send(reply, Tools.getNodeByID(senderId));
        global.messagesSent++;
    }

    public abstract void handleAYUp (BullyMessage msg);
    public abstract void handleAYOk(BullyMessage msg);
    public abstract void handleEnterElection(BullyMessage msg);
    public abstract void handleSetCoordinator(BullyMessage msg);
    public abstract void handleSetState(BullyMessage msg);
    public abstract void handleAck(BullyMessage msg);
    public abstract void handleTimeout();
    public abstract void handleUpdate();
}
