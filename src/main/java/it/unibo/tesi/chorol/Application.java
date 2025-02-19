package it.unibo.tesi.chorol;


import it.unibo.tesi.chorol.controlflow.FlowController;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.module.ModuleException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
	public static void main(String[] args) throws ParserException, IOException, ModuleException {
		String filename = "/home/kekko/Studio/tesi/chorol/src/main/resources/examples/roulette/Croupier.ol";

		args = new String[]{filename};

		Path root = Paths.get(args[0]);
		FlowController flowController = new FlowController(root);


	}
}
