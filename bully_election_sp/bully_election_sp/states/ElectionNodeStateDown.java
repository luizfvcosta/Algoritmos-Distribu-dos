package projects.bully_election_sp.states;

import projects.bully_election_sp.nodes.messages.BullyMessage;
import projects.bully_election_sp.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_sp.nodes.timers.ElectionTimeoutTimer;
import sinalgo.tools.Tools;

public class ElectionNodeStateDown extends ElectionNodeState {
    public ElectionNodeStateDown(ElectionNode ctx) {
        super(ctx);
    	
    }

    @Override
    public void handleAYUp(BullyMessage msg) {
    	
    }

    @Override
    public void handleAYOk(BullyMessage msg) {

    }

    @Override
    public void handleEnterElection(BullyMessage msg) {

    }

    @Override
    public void handleSetCoordinator(BullyMessage msg) {
    	if (msg.coordinatorId >= ctx.coordinatorId && msg.coordinatorId > ctx.ID) {
    		ctx.coordinatorId = msg.coordinatorId;
    		ctx.setState(States.Normal);
    	}
    }

    @Override
    public void handleSetState(BullyMessage msg) {

    }

    @Override
    public void handleFindCoordinator(BullyMessage msg) {
    }
    
    @Override
    public void handleQ_msg(BullyMessage msg) {
    }
    
    @Override
    public void handleAck(BullyMessage msg) {
    }
    
    @Override
    public void handleTimeout() {
    	if (ctx.coordinatorId == -1) {
    		ctx.up.clear();
    		ctx.c--;
            ctx.setState(States.ElectionCandidate);
    	}
    	else {
    	 BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.Q_msg, false);
         
         ctx.send(msg, Tools.getNodeByID((int) ctx.coordinatorId));
         ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYOk);
         ctx.activeTimeout.startRelative(3, ctx);
         ctx.coordinatorId = -1;
         global.messagesSent++;
    	} 
    }

    @Override
    public void handleUpdate() {

        ctx.reliability--;

        if(ctx.reliability <= 0){
            ctx.reliability = 0;
        }

        if (ctx.getCurrentAntenna() != null) {
            Tools.appendToOutput("Node " + ctx.ID + " found connection\n");
            if (ctx.activeTimeout == null) {
                ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYOk);
                ctx.activeTimeout.startRelative(1, ctx);
            }
        }
    }
}
