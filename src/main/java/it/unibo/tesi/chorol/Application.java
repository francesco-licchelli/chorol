package it.unibo.tesi.chorol;


import it.unibo.tesi.chorol.utils.OutputSettings;
import it.unibo.tesi.chorol.visitor.flow.FlowController;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.module.ModuleException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
	public static void main(String[] args) throws ParserException, IOException, ModuleException {
		Options options = new Options();
		options.addOption("T", "full-type", false, "Output completo del tipo");

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("ImpostaBooleano", options);
			System.exit(1);
			return;
		}

		if (cmd.hasOption("full-type") || cmd.hasOption("T")) OutputSettings.setFullType(true);

		String filename = "/home/kekko/Studio/tesi/chorol/src/main/resources/examples/roulette/Table.ol";

		args = new String[]{filename};

		Path root = Paths.get(args[0]);
		FlowController flowController = new FlowController(root);


	}
}
