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
    Rule SpecialEscape()
    {
        return Sequence('\\', AnyOf("\"\\/bfnrt"));
    }

    Rule UTF16Escape()
    {
        return Sequence(
            "\\u",
            NTimes(4, AnyOf("abcdefABCDEF0123456789").label("HexDigit"))
        );
    }

    Rule StringSpecial()
    {
        return FirstOf(SpecialEscape(), UTF16Escape());
    }

    Rule StringNormal()
    {
        return ZeroOrMore(NoneOf("\"\\\b\f\n\r\t"));
    }

    Rule JsonStringContent()
    {
        return Join(StringNormal(), StringSpecial());
    }

    Rule JsonString()
    {
        return Sequence('"', JsonStringContent(), '"');
    }

    Rule JsonBoolean()
    {
        return FirstOf("true", "false");
    }

    Rule JsonNull()
    {
        return String("null");
    }

    Rule Digits()
    {
        return OneOrMore(CharRange('0', '9'));
    }

    Rule Int()
    {
        return FirstOf(Sequence(TestNot('0'), Digits()), '0');
    }

    Rule Frac()
    {
        return Sequence('.', Digits());
    }

    Rule Exp()
    {
        return Sequence(AnyOf("eE"), Optional(AnyOf("+-")), Digits());
    }

    Rule JsonNumber()
    {
        return Sequence(
            Optional('-'),
            Int(),
            Optional(Frac()),
            Optional(Exp())
        );
    }

    Rule JsonPrimitive()
    {
        return FirstOf(JsonString(), JsonNumber(), JsonBoolean(), JsonNull());
    }

    Rule JsonArray()
    {
        return Sequence(
            '[', WS(),
            Optional(Join(JsonValue(), Sequence(WS(), ',', WS()))),
            WS(), ']'
        );
    }

    Rule ObjectMember()
    {
        return Sequence(JsonString(), WS(), ':', WS(), JsonValue());
    }

    Rule JsonObject()
    {
        return Sequence(
            '{', WS(),
            Optional(Join(ObjectMember(), Sequence(WS(), ',', WS()))),
            WS(), '}'
        );
    }

    Rule JsonValue()
    {
        return FirstOf(JsonObject(), JsonArray(), JsonPrimitive());
    }

    Rule JsonText()
    {
        return Sequence(JsonValue(), EOI);
    }

    Rule WS()
    {
        return ZeroOrMore(AnyOf(" \n\r\f"));
    }

    @Cached
    @DontExtend
    Rule Join(final Rule normal, final Rule special)
    {
        return Sequence(normal, ZeroOrMore(Sequence(special, normal)));
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
            result = new ReportingParseRunner(parser.JsonText())
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
