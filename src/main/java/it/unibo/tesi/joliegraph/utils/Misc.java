package it.unibo.tesi.joliegraph.utils;

import jolie.lang.parse.OLParser;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.Scanner;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode.AssignmentOperation;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.util.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static it.unibo.tesi.joliegraph.utils.Constants.MAX_VECTOR_SIZE;

public class Misc {
	private static final Logger logger = LoggerFactory.getLogger(Misc.class);

	public static Program loadProgram(URI path) {
		try (InputStream inputStream = Files.newInputStream(Paths.get(path.getPath()))) {
			InputStreamReader isr = new InputStreamReader(inputStream);
			Scanner scanner = new Scanner(inputStream, new URI(path.toString()), isr.getEncoding());
			return new OLParser(scanner, new String[]{}, new ClassLoader() {
			}).parse();
		} catch (IOException e) {
			Misc.logger.warn("Error loading program in: {}", path);
		} catch (ParserException | URISyntaxException e) {
			Misc.logger.warn(e.getMessage());
		}
		return null;
	}

	public static String getProtocolInfoKey(AssignmentOperation aop) {
		return aop.path().path().stream()
				       .map(a -> switch (a.key().getClass().getSimpleName()) {
					       case "ConstantStringExpression" -> a.key().toString();
					       case "SumExpressionNode" -> {
						       SumExpressionNode sen3 = (SumExpressionNode) a.key();
						       yield sen3.operands().stream()
								             .map(b -> (ProductExpressionNode) b.value())
								             .map(pen -> pen.operands().get(0).value().toString())
								             .collect(Collectors.joining(""));
					       }
					       default -> a.key().getClass().getSimpleName() + " TODO";
				       })
				       .collect(Collectors.joining("."));
	}

	public static String getProtocolInfoValue(SumExpressionNode sen) {
		return sen.operands().stream()
				       .map(a -> (ProductExpressionNode) a.value())
				       .map(pen -> pen.operands().get(0).value().toString())
				       .collect(Collectors.joining());
	}


	public static String rangeToString(Range r) {
		int min = r.min();
		int max = r.max();
		if (min == max)
			return String.format("%d", min);
		else return String.format(
				"%d-%s",
				min,
				max != MAX_VECTOR_SIZE ? Integer.toString(max) : "*"
		);
	}


}
