package it.unibo.tesi.chorol;


import it.unibo.tesi.chorol.controlflow.FlowManager;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.module.ModuleException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
	public static void main(String[] args) throws ParserException, IOException, ModuleException {
		String filename = "/home/kekko/Studio/tesi/chorol/src/main/resources/examples/roulette/Croupier.ol";

		args = new String[]{filename};
		if (args.length != 1) {
			System.out.println("Usage: java -jar Parser.jar <file>");
			return;
		}

		Path root = Paths.get(args[0]);
		FlowManager flowManager = new FlowManager(root);


	}
}
