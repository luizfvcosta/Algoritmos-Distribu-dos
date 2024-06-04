/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.bully_election_sp.models.connectivityModels;


import projects.bully_election_sp.nodes.nodeImplementations.Antenna;
import projects.bully_election_sp.nodes.nodeImplementations.ElectionNode;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;


/**
 * Implements a connection from a node to the antenna.
 */
public class AntennaConnection extends ConnectivityModelHelper {
	/**
	 * The constructor reads the antenna-config settings from the config file.
	 * @throws CorruptConfigurationEntryException When there is a missing entry in the 
	 * config file.
	 */
	public AntennaConnection() throws CorruptConfigurationEntryException {
	}
	/**
	 * verifica se ambos os n�s s�o inst�ncias de ElectionNode
	 * se sim, ele obt�m as antenas atuais (currentAntenna) dos n�s from e to
	 * 
	 * Ele retorna true se ambas as antenas n�o forem null e forem a mesma inst�ncia 
	 * (ou seja, apontam para a mesma antena). Caso contr�rio, retorna false
	 */
	protected boolean isConnected(Node from, Node to) {
		if(from instanceof ElectionNode && to instanceof ElectionNode) {
			Antenna to_antenna = ((ElectionNode) to).getCurrentAntenna();
			Antenna from_antenna = ((ElectionNode) from).getCurrentAntenna();

			return to_antenna != null && to_antenna == from_antenna;
		}
		return false;
	}

}
