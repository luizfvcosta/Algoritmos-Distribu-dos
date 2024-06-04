package projects.bully_election_std.nodes.timers;

import projects.bully_election_std.nodes.nodeImplementations.Antenna;
import projects.bully_election_std.nodes.nodeImplementations.ElectionNode;
import projects.bully_election_std.states.ElectionNodeState;
import projects.bully_election_std.states.ElectionNodeStateDown;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class ElectionUpdateTimer extends Timer {
	int radius;
	public ElectionUpdateTimer() throws CorruptConfigurationEntryException {
		radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax") / 2;
		radius *= radius;
	}
	
	@Override
	public void fire() {
		// Setting current antenna
		ElectionNode mn = (ElectionNode) this.getTargetNode();
		Position pos = mn.getPosition();

		Antenna connectedAntenna = null;
		for (Node n: Tools.getNodeList()) {
			if (n instanceof Antenna) {
				Antenna a = (Antenna) n;
				if (a.getPosition().squareDistanceTo(pos) < radius) {
					connectedAntenna = a;
					break;
				}
			}
		}

		if (connectedAntenna == null && !(mn.state instanceof ElectionNodeStateDown)) {
			mn.setState(ElectionNodeState.States.Down);
		}

		mn.setCurrentAntenna(connectedAntenna);
		Tools.reevaluateConnections();
		mn.state.handleUpdate();

		this.startRelative(1, this.getTargetNode());
	}

}
