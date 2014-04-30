package org.parboiled.examples.indenting;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class SimpleIndent extends BaseParser<IndentNode> {

	Rule parent() {
		return sequence(push(new IndentNode("root")), oneOrMore(data()), EOI);
	}

	Rule data() {
		return sequence(Identifier(), push(new IndentNode(match())),
            peek(1).addChild(peek()),
            optional(sequence(spacing(), childNodeList())), drop()
        );
	}

	Rule childNodeList() {
		return sequence(INDENT, spacing(), oneOrMore(data(), spacing()), DEDENT);
	}

	Rule Identifier() {
		return sequence(pnCharsU(), zeroOrMore(pnCharsDigitU()));
	}

	public Rule pnCharsDigitU() {
		return firstOf(pnCharsU(), digit());
	}

	public Rule pnCharsU() {
		return firstOf(pnCharsBase(), '_');
	}

	public Rule pnCharsBase() {
		return firstOf(charRange('A', 'Z'), charRange('a', 'z'),
            charRange('\u00C0', '\u00D6'), charRange('\u00D8', '\u00F6'),
            charRange('\u00F8', '\u02FF'), charRange('\u0370', '\u037D'),
            charRange('\u037F', '\u1FFF'), charRange('\u200C', '\u200D'),
            charRange('\u2070', '\u218F'), charRange('\u2C00', '\u2FEF'),
            charRange('\u3001', '\uD7FF'), charRange('\uF900', '\uFDCF'),
            charRange('\uFDF0', '\uFFFD'));
	}

	Rule spacing() {
		return zeroOrMore(anyOf(" \t\r\n\f").label("Whitespace"));
	}
}
