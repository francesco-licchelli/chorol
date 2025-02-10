package it.unibo.tesi.chorol.utils;

import jolie.lang.parse.OLParser;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.Scanner;
import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ServiceNode;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static it.unibo.tesi.chorol.utils.Constants.JOLIE_EXTENSION_REGEX;
import static it.unibo.tesi.chorol.utils.Constants.MAX_VECTOR_SIZE;

public class Misc {
	private static final Logger logger = LoggerFactory.getLogger(Misc.class);

	public static Program loadProgram(Path path) {
		try (InputStream inputStream = Files.newInputStream(path)) {
			InputStreamReader isr = new InputStreamReader(inputStream);
			Scanner scanner = new Scanner(inputStream, new URI(path.toString()), isr.getEncoding());
			return new OLParser(scanner, new String[]{}, new ClassLoader() {
			}).parse();
		} catch (IOException e) {
			logger.warn("Error loading program in: {}", path);
		} catch (ParserException | URISyntaxException e) {
			logger.warn(e.getMessage());
		}
		return null;
	}

	public static List<ImportStatement> getProgramImports(Program program) {
		List<ImportStatement> res = new ArrayList<>();
		for (OLSyntaxNode child : program.children()) {
			if (child instanceof ServiceNode)
				res.addAll(getProgramImports(((ServiceNode) child).program()));
			else if (child instanceof ImportStatement)
				res.add((ImportStatement) child);
		}
		return res;
	}

	public static String parseJoliePaths(String path) {
		int count = 0;
		while (count < path.length() && path.charAt(count) == '.')
			count++;
		if (count == 0) {
			return path + (path.endsWith(".ol") ? "" : ".ol");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("../".repeat(count - 1));
		sb.append("./").append(path.substring(count));
		if (!sb.toString().matches(JOLIE_EXTENSION_REGEX)) {
			sb.append(".ol");
		}
		return sb.toString();
	}

	public static List<Path> getFilesPaths(Path root) {
		List<Path> result = new ArrayList<>();
		collectFilesPaths(root, new HashSet<>(), result);
		return result;
	}

	private static void collectFilesPaths(Path path, Set<Path> visited, List<Path> result) {
		Path absolutePath = path.toAbsolutePath().normalize();
		if (visited.contains(absolutePath)) {
			return;
		}
		visited.add(absolutePath);

		Program program = loadProgram(absolutePath);
		if (program == null) return;
		getProgramImports(program).forEach(imp -> {
			Path relChildPath = Paths.get(parseJoliePaths(imp.prettyPrintTarget()));
			Path absChildPath = absolutePath.getParent().resolve(relChildPath).normalize().toAbsolutePath();
			collectFilesPaths(absChildPath, visited, result);
		});
		result.add(absolutePath);
	}


	public static String getProtocolInfoKey(AssignmentOperation aop) {
		return aop.path().path().stream()
				       .map(a -> {
					       switch (a.key().getClass().getSimpleName()) {
						       case "ConstantStringExpression":
							       return a.key().toString();
						       case "SumExpressionNode":
							       SumExpressionNode sen3 = (SumExpressionNode) a.key();
							       return sen3.operands().stream()
									              .map(b -> (ProductExpressionNode) b.value())
									              .map(pen -> pen.operands().get(0).value().toString())
									              .collect(Collectors.joining(""));
						       default:
							       return a.key().getClass().getSimpleName() + " TODO";
					       }
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
		else {
			return String.format(
					"%d-%s",
					min,
					max != MAX_VECTOR_SIZE ? Integer.toString(max) : "*"
			);
		}
	}


}
