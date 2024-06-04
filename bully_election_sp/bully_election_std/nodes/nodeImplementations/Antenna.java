package projects.bully_election_std.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;

public class Antenna extends Node {
	@Override
	public void checkRequirements() {
	}

	@Override
	public void handleMessages(Inbox inbox) {
	}
	@Override
	public void init() {

	}
	
	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
	}

	@Override
	public void postStep() {
	}

	private static int radius;
	{
		try {
			radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax") / 2;
		} catch (CorruptConfigurationEntryException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight){
		Color bckup = g.getColor();
		g.setColor(Color.BLACK);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
		super.drawAsDisk(g, pt, highlight, drawingSizeInPixels);
		g.setColor(Color.LIGHT_GRAY);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		g.setColor(bckup);
	}

	public Antenna() throws CorruptConfigurationEntryException {
		this.setDefaultDrawingSizeInPixels(Configuration.getIntegerParameter("Antenna/Size"));
	}

}
