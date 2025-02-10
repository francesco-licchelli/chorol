package it.unibo.tesi.chorol;


import it.unibo.tesi.chorol.symbols.SymbolManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
	public static void main(String[] args) {
		String filename = "/home/kekko/Studio/tesi/chorol/src/main/resources/examples/super_calculator/CalculatorService.ol";

		args = new String[]{filename};
		if (args.length != 1) {
			System.out.println("Usage: java -jar Parser.jar <file>");
			return;
		}

		//
		/*
		 * Come prima cosa, mi carico tutti i simboli
		 * interfacce, definizioni, porte, ...
		 * */
		Path root = Paths.get(args[0]);
		SymbolManager s = new SymbolManager(root);


//        ServiceParser parser = new ServiceParser(args[0]);
		//nello stesso file ci possono essere piu' servizi

	}
}
