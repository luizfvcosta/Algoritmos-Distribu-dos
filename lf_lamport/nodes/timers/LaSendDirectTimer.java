package projects.lf_lamport.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class LaSendDirectTimer extends sinalgo.nodes.timers.Timer {

	private Message msg; // the msg to send
	private Node dest; // the node to send the msg to
	
	public LaSendDirectTimer(Message msg, Node destination) {
		this.msg = msg;
		this.dest = destination;
	}
	
	@Override
	public void fire() {
		this.node.sendDirect(msg, dest);
	}

}
