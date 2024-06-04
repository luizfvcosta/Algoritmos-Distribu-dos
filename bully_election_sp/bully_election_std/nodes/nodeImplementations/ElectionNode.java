/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.bully_election_std.nodes.nodeImplementations;

import projects.bully_election_std.nodes.messages.BullyMessage;
import projects.bully_election_std.nodes.timers.ElectionTimeoutTimer;
import projects.bully_election_std.nodes.timers.ElectionUpdateTimer;
import projects.bully_election_std.states.*;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.SinalgoFatalException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import java.awt.*;
import java.util.ArrayList;

/**
 * The Node of the sample project.
 */
public class ElectionNode extends Node {

    public ElectionNodeState state;

    private Antenna currentAntenna = null; // the antenna ths node is connected to, null if this node is not connected to an antenna

    public Antenna getCurrentAntenna() {
        return currentAntenna;
    }

    public void setCurrentAntenna(Antenna a) { currentAntenna = a; }

    public long c;
    public int coordinatorId;
    public ArrayList <Integer> up = new ArrayList<>();
    public int reliability;

    public ElectionTimeoutTimer activeTimeout = new ElectionTimeoutTimer(BullyMessage.MessageType.AYUp);

    Logging log = Logging.getLogger("election_log");

    private boolean isCoordinator() {
        return (this.ID == coordinatorId);
    }

    public void setState(ElectionNodeState.States state) {
        switch (state) {
            case Normal:
                this.state = new ElectionNodeStateNormal(this);
                break;
            case NormalCoordinator:
                this.state = new ElectionNodeStateNormalCoordinator(this);
                break;
            case ElectionCandidate:
                this.state = new ElectionNodeStateElectionCandidate(this);
                break;
            case ElectionParticipant:
                this.state = new ElectionNodeStateElectionParticipant(this);
                break;
            case Down:
                this.state = new ElectionNodeStateDown(this);
                break;
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            BullyMessage msg = (BullyMessage) inbox.next();
            this.state.handleMessage(msg);
        }
    }

    @Override
    public void preStep() {
    }

    @Override
    public void init() {
        this.c = 0;
        this.reliability = 50000;
        this.state = new ElectionNodeStateDown(this);

        // initialize the node
        try {
            this.setDefaultDrawingSizeInPixels(Configuration.getIntegerParameter("MobileNode/Size"));
        } catch (CorruptConfigurationEntryException e) {
            // Missing entry in the configuration file: Abort the simulation and
            // display a message to the user
            throw new SinalgoFatalException(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Missing entry in the configuration file: Abort the simulation and
            // display a message to the user
            // throw new SinalgoFatalException("The initial state (specified in the config
            // file) must be DOWN, REORGANIZING, ELECTION or NORMAL.");
        }

        try {
            new ElectionUpdateTimer().startRelative(1, this);
        } catch (CorruptConfigurationEntryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        Color nodeColor;

        if (state instanceof ElectionNodeStateDown) {
            nodeColor = Color.RED;
        } else if (state instanceof ElectionNodeStateNormalCoordinator) {
            nodeColor = Color.BLUE;
        } else if (state instanceof  ElectionNodeStateElectionParticipant) {
            nodeColor = Color.YELLOW;
        } else if (state instanceof ElectionNodeStateElectionCandidate) {
            nodeColor = Color.ORANGE;
        } else {
            nodeColor = Color.GREEN;
        }

        // set the color of this node
        // this.setColor(new Color((float) 0.5 / (1 + this.state.getValue()), (float)
        // 0.5, (float) 1.0 / (1 + this.state.getValue())));
        this.setColor(nodeColor);

        // set the text of this node
        String text = "<" + this.ID + ">";

        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 14, Color.WHITE);
    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }
}
