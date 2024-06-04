package projects.bully_election_std.states;

import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.ArrayList;

import projects.bully_election_std.nodes.messages.BullyMessage;
import projects.bully_election_std.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_std.nodes.timers.ElectionTimeoutTimer;

public class ElectionNodeStateElectionCandidate extends ElectionNodeState {
    private final ArrayList<Integer> responded = new ArrayList<>();

    public ElectionNodeStateElectionCandidate(ElectionNode ctx) {
        super(ctx);
        ctx.up.clear();
        ctx.coordinatorId = -1;

        BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.AYUp, false);

        ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYUp);
        ctx.activeTimeout.startRelative(3, ctx);

        Tools.appendToOutput("Node " + ctx.ID + " entering CANDIDATE state\n");
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
        if(ctx.ID < msg.senderId){
            Tools.appendToOutput("Node " + ctx.ID + " giving up on CADIDATE state\n");
            reply(msg);
            ctx.setState(States.ElectionParticipant);
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
        Tools.appendToOutput("Node " + ctx.ID + " received ack from " + msg.senderId + "\n");

        if (msg.type == BullyMessage.MessageType.AYUp) {
            Tools.appendToOutput("Node " + ctx.ID + " received UP from <" + msg.c + "," + msg.senderId + ">\n");
            
            
            if ( msg.senderId > ctx.ID) {
               // Tools.appendToOutput("Node " + ctx.getID() + " giving up on CADIDATE state\n");
             
             /*if (msg.coordinatorId != -1) {
                    ctx.coordinatorId = msg.coordinatorId;
                    ctx.setState(States.Normal);
                } else {
                    */
                   // ctx.setState(States.ElectionParticipant);
                //}
            }
            
        } 
        else if (msg.type == BullyMessage.MessageType.EnterElection) {

            if(ctx.ID < msg.senderId){
            Tools.appendToOutput("Node " + ctx.ID + " giving up on CADIDATE state\n");
            ctx.setState(States.ElectionParticipant);
            }else{
            
                Tools.appendToOutput("Node " + ctx.ID + " adding " + msg.senderId + " to up list.\n");
                ctx.up.add(msg.senderId);
            }
        } 
        else if (msg.type == BullyMessage.MessageType.SetCoordinator) {
            Tools.appendToOutput("Node " + ctx.ID + " adding " + msg.senderId + " to SetCoordinator responded list.\n");
            responded.add(msg.senderId);
            if (responded.containsAll(ctx.up)) {
                ctx.activeTimeout.shouldFire = false;
                handleTimeout();
            }
        } 
        else if (msg.type == BullyMessage.MessageType.SetState) {
            Tools.appendToOutput("Node " + ctx.ID + " adding " + msg.senderId + " to SetState responded list.\n");
            responded.add( msg.senderId);
            if (responded.containsAll(ctx.up)) {
                ctx.activeTimeout.shouldFire = false;
                handleTimeout();
            }
        }
    }

    @Override
    public void handleTimeout() {
        if (ctx.activeTimeout.type == BullyMessage.MessageType.AYUp) {

                Tools.appendToOutput("Node " + ctx.ID + " calling for elections.\n");
                BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.EnterElection, false);


                for (Node n: Tools.getNodeList()) {
                    if (n instanceof ElectionNode) {
                        ElectionNode aux = (ElectionNode) n;
                        //if (aux.getCurrentAntenna() != null) {
                            ctx.send(msg.clone(), n);
                            global.messagesSent++;
                        //}
                    }
                }

                ctx.up.clear();
                ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.EnterElection);
                ctx.activeTimeout.startRelative(6, ctx);

        } 
        else if (ctx.activeTimeout.type == BullyMessage.MessageType.EnterElection) {
            Tools.appendToOutput("Node " + ctx.ID + " electing himself\n");
            BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.SetCoordinator, false);

            Tools.appendToOutput("Node " + ctx.ID + " up: " + ctx.up + "\n");
            for (int id: ctx.up) {
                ElectionNode n = (ElectionNode) Tools.getNodeByID(id);
                ctx.send(msg.clone(), n);
                global.messagesSent++;
            }
            responded.clear();
            ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.SetCoordinator);
            ctx.activeTimeout.startRelative(3, ctx);
        } else if (ctx.activeTimeout.type == BullyMessage.MessageType.SetCoordinator) {
            if (!responded.containsAll(ctx.up)) {
                Tools.appendToOutput("Node " + ctx.ID + " trying again - not all nodes responded to SetCoordinator\n");
                ctx.setState(States.ElectionCandidate);
            } else {
                ctx.coordinatorId = ctx.ID;
                Tools.appendToOutput("Node " + ctx.ID + " setting normal\n");
                BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId, BullyMessage.MessageType.SetState, false);

                for (int id: ctx.up) {
                    ElectionNode n = (ElectionNode) Tools.getNodeByID(id);
                    ctx.send(msg.clone(), n);
                    global.messagesSent++;
                }

                responded.clear();
                ctx.activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.SetState);
                ctx.activeTimeout.startRelative(3, ctx);
            }
        } else if (ctx.activeTimeout.type == BullyMessage.MessageType.SetState) {
            if (!responded.containsAll(ctx.up)) {
                Tools.appendToOutput("Node " + ctx.ID + " trying again - not all nodes responded to SetState\n");
                ctx.setState(States.ElectionCandidate);
            } else {
                Tools.appendToOutput("Node " + ctx.ID + " becoming coordinator\n");
                ctx.setState(States.NormalCoordinator);
            }
        }
    }

    @Override
    public void handleUpdate() {

    }
}
