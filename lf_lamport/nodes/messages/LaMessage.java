package projects.lf_lamport.nodes.messages;

import java.awt.Color;

import sinalgo.nodes.messages.Message;

public class LaMessage extends Message {
	public Color color; // the color the receiver should take 
	
	public int data;
	
	@Override
	public Message clone() {
		// This is a read-only message! Receivers may not modify the message
		// If this is not the case, we need to return a clone of this msg.
		return this;
	}

}
