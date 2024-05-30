package projects.bully_election_sp.states;

import java.util.ArrayList;

import projects.bully_election_sp.CustomGlobal;
import projects.bully_election_sp.nodes.messages.BullyMessage;
import projects.bully_election_sp.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_sp.nodes.timers.ElectionTimeoutTimer;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class ElectionNodeStateNormal extends ElectionNodeState {
    boolean responded = true;

    CustomGlobal global;

    public ElectionNodeStateNormal(ElectionNode ctx) {
        super(ctx);

        global = (CustomGlobal) Tools.getCustomGlobal();
        ctx.c = 0;

        //Tools.appendToOutput("Node " + ctx.ID + " going back to normal\n");
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
    	//Tools.appendToOutput("Node " + msg.senderId + " can Be coordinator \n");
    	if (ctx.procoord < msg.senderId) {
    		ctx.procoord = msg.senderId;
    	}

    	//ctx.setState(States.ElectionParticipant);
    	
    	/*BullyMessage m = new BullyMessage(ctx.ID, ctx.c,ctx.procoord , BullyMessage.MessageType.SetCoordinator , false);


         for (Node n: Tools.getNodeList()) {
             if (n instanceof ElectionNode) {
                 ElectionNode aux = (ElectionNode) n;
                 //if (aux.getCurrentAntenna() != null) {
                     ctx.send(m.clone(), n);
                     global.messagesSent++;
                 //}
             }
         }*/
    	
    }
    
    @Override
    public void handleQ_msg(BullyMessage msg) {
    	BullyMessage m = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId , BullyMessage.MessageType.SetCoordinator , false);
    	ctx.send(m, Tools.getNodeByID(msg.senderId));
    	global.messagesSent++;
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
    	Tools.appendToOutput("Node " + ctx.ID + " is the new coordinator \n");
    	 if( msg.coordinatorId == ctx.ID) {
    		 ctx.coordinatorId = ctx.ID;
    		 ctx.procoord = 0;
    		 ctx.setState(States.NormalCoordinator); 
    	 }
    	 else ctx.setState(States.Normal); 
    }

    @Override
    public void handleSetState(BullyMessage msg) {

    }
    
    @Override
    public void handleFindCoordinator(BullyMessage msg) {
    	//Tools.appendToOutput("Node " + ctx.ID + " invited to be the new coordinator \n");
    	
    	//ctx.setState(States.ElectionCandidate);
    	BullyMessage m = new BullyMessage(ctx.ID, ctx.c, msg.senderId, BullyMessage.MessageType.AYOk, false);
    	ctx.send(m, Tools.getNodeByID(msg.senderId));
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
            //Tools.appendToOutput("Node " + ctx.ID + " coordinator failed to respond to ping - calling elections\n");
            //ctx.setState(States.ElectionCandidate);
        	Tools.appendToOutput("Node " + ctx.ID + " coordinator failed to respond to ping - find new coordinator\n");
        	for (int i = ctx.coordinatorId-1; i >= ctx.ID ; i--) {
        		
        		BullyMessage msg = new BullyMessage(ctx.ID, ctx.c, i, BullyMessage.MessageType.FindCoordinator, false);
        		
        		ctx.send(msg, Tools.getNodeByID((int) i));
        		global.messagesSent++;
            }
        	
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
    
        if (ctx.procoord > 0){
            ctx.coordinatorId = ctx.procoord;
            ctx.procoord = 0;
            BullyMessage m = new BullyMessage(ctx.ID, ctx.c, ctx.coordinatorId , BullyMessage.MessageType.SetCoordinator , false);
            global.workDone-- ;

            for (Node n: Tools.getNodeList()) {
                if (n instanceof ElectionNode) {
                    ElectionNode aux = (ElectionNode) n;
                    //if (aux.getCurrentAntenna() != null) {
                        ctx.send(m.clone(), n);
                        global.messagesSent++;
                    //}
                }
            }
        }
    
    }
}
