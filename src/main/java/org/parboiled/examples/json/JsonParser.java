package org.parboiled.examples.json;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.Cached;
import org.parboiled.annotations.DontExtend;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.examples.doublequotedstring.DoubleQuotedString;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

/**
 * A complete, JSON parser as specified by RFC 7159
 *
 * <p>This parser uses the same parsing routine as {@link DoubleQuotedString}.
 * </p>
 *
 * <p>Note that RFC 7159 (which obsoletes RFC 4627) specifies that a JSON Text
 * can be any JSON value; RFC 4627 limited a JSON Text to being only an object
 * or an array.</p>
 */
@BuildParseTree
public class JsonParser
    extends BaseParser<Void>
{
    Rule specialEscape()
    {
        return sequence('\\', anyOf("\"\\/bfnrt"));
    }

    Rule utf16Escape()
    {
        return sequence("\\u", nTimes(4, hexDigit()));
    }

    Rule stringSpecial()
    {
        return firstOf(specialEscape(), utf16Escape());
    }

    Rule stringNormal()
    {
        return zeroOrMore(noneOf("\"\\\b\f\n\r\t"));
    }

    Rule stringContent()
    {
        return join(stringNormal(), stringSpecial());
    }

    Rule jsonString()
    {
        return sequence('"', stringContent(), '"');
    }

    Rule jsonBoolean()
    {
        return firstOf("true", "false");
    }

    Rule jsonNull()
    {
        return string("null");
    }

    Rule digits()
    {
        return oneOrMore(digit());
    }

    Rule integer()
    {
        return firstOf(sequence(testNot('0'), digits()), '0');
    }

    Rule fraction()
    {
        return sequence('.', digits());
    }

    Rule exponent()
    {
        return sequence(anyOf("eE"), optional(anyOf("+-")), digits());
    }

    Rule jsonNumber()
    {
        return sequence(optional('-'), integer(), optional(fraction()),
            optional(exponent()));
    }

    Rule jsonPrimitive()
    {
        return firstOf(jsonString(), jsonNumber(), jsonBoolean(), jsonNull());
    }

    Rule jsonArray()
    {
        return sequence('[', whiteSpace(), optional(
                join(jsonValue(), sequence(whiteSpace(), ',', whiteSpace()))),
            whiteSpace(), ']'
        );
    }

    Rule objectMember()
    {
        return sequence(jsonString(), whiteSpace(), ':', whiteSpace(),
            jsonValue());
    }

    Rule jsonObject()
    {
        return sequence('{', whiteSpace(), optional(join(objectMember(),
                sequence(whiteSpace(), ',', whiteSpace()))), whiteSpace(), '}'
        );
    }

    Rule jsonValue()
    {
        return firstOf(jsonObject(), jsonArray(), jsonPrimitive());
    }

    Rule jsonText()
    {
        return sequence(jsonValue(), EOI);
    }

    Rule whiteSpace()
    {
        return zeroOrMore(anyOf(" \n\r\f"));
    }

    @Cached
    @DontExtend
    Rule join(final Rule normal, final Rule special)
    {
        return sequence(normal, zeroOrMore(sequence(special, normal)));
    }

    public static void main(final String... args)
    {
        final JsonParser parser
            = Parboiled.createParser(JsonParser.class);

        final Scanner scanner = new Scanner(System.in);
        ParsingResult<?> result;
        String line;

        while (true) {
            System.out.print("Enter a value to test (empty to quit): ");
            line = scanner.nextLine();
            if (line.isEmpty())
                break;
            result = new ReportingParseRunner(parser.jsonText())
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
