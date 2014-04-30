package org.parboiled.examples.doublequotedstring;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

/**
 * A simple, double quoted string matching grammar
 *
 * <p>This will match inputs such as:</p>
 *
 * <ul>
 *     <li>{@code "hello world"};</li>
 *     <li>{@code "hello \"world\""};</li>
 *     <li>{@code ""};</li>
 *     <li>{@code "\""}</li>
 * </ul>
 *
 * <p>and so on.</p>
 *
 * <p>This parser builds the parse tree (the class is annotated using {@link
 * BuildParseTree}) and uses a {@link TracingParseRunner} so that you can see
 * the parsing process in action.</p>
 *
 * <p>This parser uses the {@code normal* (special normal*)*} pattern, often
 * used in regexes to "unroll the loop". In this case:</p>
 *
 * <ul>
 *     <li>{@code normal} is everything but a backslash or a double quote,</li>
 *     <li>{@code special} is the character sequence {@code '\', '"'}.</li>
 * </ul>
 */
@BuildParseTree
public class DoubleQuotedString
    extends BaseParser<Void>
{
    Rule normal()
    {
        return noneOf("\\\"");
    }

    Rule special()
    {
        return string("\\\"");
    }

    Rule nsn()
    {
        return sequence(zeroOrMore(normal()),
            zeroOrMore(special(), zeroOrMore(normal())));
    }

    Rule doubleQuotedString()
    {
        return sequence('"', nsn(), '"', EOI);
    }

    public static void main(final String... args)
    {
        final DoubleQuotedString parser
            = Parboiled.createParser(DoubleQuotedString.class);

        final Scanner scanner = new Scanner(System.in);
        ParsingResult<?> result;
        String line;

        while (true) {
            System.out.print("Enter a value to test (empty to quit): ");
            line = scanner.nextLine();
            if (line.isEmpty())
                break;
            result = new TracingParseRunner(parser.doubleQuotedString())
                .run(line);
            if (result.hasErrors()) {
                System.out.println("Invalid input!");
                System.out.println(ErrorUtils.printParseErrors(result));
            } else {
                System.out.println(ParseTreeUtils.printNodeTree(result));
            }
        }
    }
}
