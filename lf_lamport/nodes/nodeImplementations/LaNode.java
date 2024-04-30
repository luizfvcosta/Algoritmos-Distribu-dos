package projects.lf_lamport.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.lf_lamport.nodes.messages.LaMessage;
import projects.lf_lamport.nodes.timers.LaSendDirectTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.EPSOutputPrintStream;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;


public class LaNode extends Node {
	
	public int lamportInternalClock = 0;
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// Nothing to do - we could check here, that proper models are set, and other settings are correct
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			if(msg instanceof LaMessage) {
				LaMessage m = (LaMessage) msg;
				if(m.data > this.lamportInternalClock) {
					this.lamportInternalClock = m.data;
				}
				Tools.appendToOutput("lamportInternalClcok of " + ID +" is " + lamportInternalClock + "\n");
				// green messages are forwarded to all neighbors
				if(m.color == Color.GREEN && !this.getColor().equals(m.color)) {
					broadcast(m);
				} 
				this.setColor(m.color); // set this node's color
			}
		}
	}

	@NodePopupMethod(menuText="BROADCAST GREEN")
	public void broadcastGREEN() {
		sendColorMessage(Color.GREEN, null);
	}

	private void sendColorMessage(Color c, Node to) {
		LaMessage msg = new LaMessage();
		msg.color = c;
		lamportInternalClock = lamportInternalClock + 1;
		msg.data = lamportInternalClock;
		Tools.appendToOutput("lamportInternalClcok of " + ID +" is " + lamportInternalClock + "\n");
		if(Tools.isSimulationInAsynchroneMode()) {
			// sending the messages directly is OK in async mode
			if(to != null) {
				send(msg, to);
			} else {
				broadcast(msg);
			}
		} else {
			// In Synchronous mode, a node is only allowed to send messages during the 
			// execution of its step. We can easily schedule to send this message during the
			// next step by setting a timer. The MessageTimer from the default project already
			// implements the desired functionality.
			MessageTimer t;
			if(to != null) {
				t = new MessageTimer(msg, to); // unicast
			} else {
				t = new MessageTimer(msg); // multicast
			}
			t.startRelative(Tools.getRandomNumberGenerator().nextDouble(), this);
		}
	}
	
	@NodePopupMethod(menuText="Unicast Gray")
	public void unicastGRAY() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // the user aborted
				}
				sendColorMessage(Color.GRAY, n);
			}
		}, "Select a node to which you want to send a 'yellow' message.");
	}
	
	/**
	 * This popup method demonstrates how a message can be sent
	 * even when there is no edge between the sender and receiver  
	 */
	@NodePopupMethod(menuText="send DIRECT PINK")
	public void sendDirectPink() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // the user aborted
				}
				LaMessage msg = new LaMessage();
				msg.color = Color.pink;
				lamportInternalClock = lamportInternalClock + 1;
				msg.data = lamportInternalClock;
				Tools.appendToOutput("lamportInternalClcok of " + ID +" is " + lamportInternalClock + "\n");
				if(Tools.isSimulationInAsynchroneMode()) {
					sendDirect(msg, n);
				} else {
					// we need to set a timer, such that the message is
					// sent during the next round, when this node performs its step.
					LaSendDirectTimer timer = new LaSendDirectTimer(msg, n);
					timer.startRelative(1.0, LaNode.this);
				}
			}
		}, "Select a node to which you want to send a direct 'PINK' message.");
	}
	
	private boolean simpleDraw = false;
	
	@Override
	public void init() {
		if(Configuration.hasParameter("LaNode/simpleDraw")) {
			try {
				simpleDraw = Configuration.getBooleanParameter("LaNode/simpleDraw");
			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError("Invalid config field S4Node/simpleDraw: Expected a boolean.\n" + e.getMessage());
			}
		} else {
			simpleDraw = false;
		}
		// nothing to do here
	}

	@Override
	public void neighborhoodChange() {
		// not called in async mode!
	}

	@Override
	public void preStep() {
		// not called in async mode!
	}

	@Override
	public void postStep() {
		// not called in async mode!
	}
		
	private boolean drawRound = false;

	private boolean isDrawRound() {
		if(drawRound) {
			return true;
		}
		return false;
	}
	
	@NodePopupMethod(menuText="Draw as Circle")
	public void drawRound() {
		drawRound = !drawRound;
		Tools.repaintGUI();
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// overwrite the draw method to change how the GUI represents this node
		if(simpleDraw) {
			super.draw(g, pt, highlight);
		} else {
			if(isDrawRound()) {
				super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), 16, Color.WHITE);
			} else {
				super.drawNodeAsSquareWithText(g, pt, highlight, Integer.toString(this.ID), 16, Color.WHITE);
			}
		}
	}
	
	public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
		if(isDrawRound()) {
			super.drawToPostScriptAsDisk(pw, pt, drawingSizeInPixels/2, getColor());
		} else {
			super.drawToPostscriptAsSquare(pw, pt, drawingSizeInPixels, getColor());
		}
	}
}
