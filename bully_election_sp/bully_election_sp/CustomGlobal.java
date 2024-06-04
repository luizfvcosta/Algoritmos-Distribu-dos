
package projects.bully_election_sp;


import java.io.File;
import java.io.FileOutputStream;

import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.SinalgoFatalException;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{

	public long workDone = 0;
	public long messagesSent = 0;
	public int totalNodes = 0;
	public String maliciousStr;

	public String baseFilepath = "output\\";

	public String algoName = "mdBm_3000_4_";
	
	@Override
	public boolean hasTerminated() {
		return false;
	}

	@Override
	public void onExit() {
		Logging logger = Logging.getLogger();

        try{
			totalNodes = Tools.getNodeList().size();
			appendToFile();
		}catch (CorruptConfigurationEntryException e) {
			logger.logln("Favor criar NumberOfNodes em Config.xml");
            throw new SinalgoFatalException(e.getMessage());
		}catch (Exception e) {
			logger.logln("Falhou ao criar csv");
			e.printStackTrace();
		}

		logger.logln("Number of nodes: " + totalNodes);
		logger.logln("Total work done: " + workDone);
		logger.logln("Total messages sent: " + messagesSent);
		logger.logln("Simulation steps: " + Tools.getGlobalTime());
		logger.logln("Total messages " + (workDone+messagesSent));
	}

	public void appendToFile() throws Exception {
		/*
	FileOutputStream fos = new FileOutputStream(baseFilepath + algoName  + "_" + totalNodes + "_nodes.csv", true);
		fos.write((workDone + ", " + messagesSent + ", " + Tools.getGlobalTime() + "\r\n").getBytes());
		fos.close();
	}
	*/
	File file = new File(baseFilepath + algoName  + "_" + totalNodes + "_nodes.csv");
    boolean fileExists = file.exists();

    try (FileOutputStream fos = new FileOutputStream(file, true)) {
        if (!fileExists) {
            // Write headers if the file does not exist
            fos.write("workDone, messagesSent, simulationTime,TotalMenssages\r\n".getBytes());
        }
        fos.write((workDone + ", " + messagesSent + ", " + Tools.getGlobalTime() + ", " + (messagesSent+workDone)  + "\r\n").getBytes());
        fos.close();
    }
	}
}

