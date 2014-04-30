/*
 * Copyright (c) 2009 Ken Wenzel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.parboiled.examples.sparql;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * SPARQL Parser
 *
 * @author Ken Wenzel, adapted by Mathias Doenitz
 */
@SuppressWarnings({"InfiniteRecursion"})
public class SparqlParser extends BaseParser<Object> {
    // <Parser>
    public Rule query() {
        return sequence(whitespace(), prologue(),
            firstOf(selectQuery(), constructQuery(), describeQuery(),
                askQuery()), EOI);
    }

    public Rule prologue() {
        return sequence(optional(baseDecl()), zeroOrMore(prefixDecl()));
    }

    public Rule baseDecl() {
        return sequence(base(), iriRef());
    }

    public Rule prefixDecl() {
        return sequence(prefix(), pnameNs(), iriRef());
    }

    public Rule selectQuery() {
        return sequence(select(), optional(firstOf(distinct(), reduced())),
            firstOf(oneOrMore(var()), asterisk()), zeroOrMore(datasetClause()),
            whereClause(), solutionModifier()
        );
    }

    public Rule constructQuery() {
        return sequence(construct(), constructTemplate(),
            zeroOrMore(datasetClause()), whereClause(), solutionModifier());
    }

    public Rule describeQuery() {
        return sequence(describe(),
            firstOf(oneOrMore(varOrIRIref()), asterisk()),
            zeroOrMore(datasetClause()), optional(whereClause()),
            solutionModifier()
        );
    }

    public Rule askQuery() {
        return sequence(ask(), zeroOrMore(datasetClause()), whereClause());
    }

    public Rule datasetClause() {
        return sequence(from(),
            firstOf(defaultGraphClause(), namedGraphClause()));
    }

    public Rule defaultGraphClause() {
        return sourceSelector();
    }

    public Rule namedGraphClause() {
        return sequence(named(), sourceSelector());
    }

    public Rule sourceSelector() {
        return iriReference();
    }

    public Rule whereClause() {
        return sequence(optional(where()), groupGraphPattern());
    }

    public Rule solutionModifier() {
        return sequence(optional(OrderClause()), optional(LimitOffsetClauses()));
    }

    public Rule LimitOffsetClauses() {
        return firstOf(sequence(LimitClause(), optional(OffsetClause())),
                sequence(OffsetClause(), optional(LimitClause())));
    }

    public Rule OrderClause() {
        return sequence(order(), by(), oneOrMore(OrderCondition()));
    }

    public Rule OrderCondition() {
        return firstOf(
                sequence(firstOf(asc(), desc()), bracketedExpression()),
                firstOf(Constraint(), var()));
    }

    public Rule LimitClause() {
        return sequence(limit(), integer());
    }

    public Rule OffsetClause() {
        return sequence(offset(), integer());
    }

    public Rule groupGraphPattern() {
        return sequence(openingCurlyBrace(), optional(TriplesBlock()),
            zeroOrMore(sequence(firstOf(GraphPatternNotTriples(), Filter()),
                optional(dot()), optional(TriplesBlock()))), closingCurlyBrace()
        );
    }

    public Rule TriplesBlock() {
        return sequence(TriplesSameSubject(),
            optional(sequence(dot(), optional(TriplesBlock()))));
    }

    public Rule GraphPatternNotTriples() {
        return firstOf(OptionalGraphPattern(), GroupOrUnionGraphPattern(),
                GraphGraphPattern());
    }

    public Rule OptionalGraphPattern() {
        return sequence(_optional(), groupGraphPattern());
    }

    public Rule GraphGraphPattern() {
        return sequence(graph(), varOrIRIref(), groupGraphPattern());
    }

    public Rule GroupOrUnionGraphPattern() {
        return sequence(groupGraphPattern(),
            zeroOrMore(sequence(union(), groupGraphPattern())));
    }

    public Rule Filter() {
        return sequence(filter(), Constraint());
    }

    public Rule Constraint() {
        return firstOf(bracketedExpression(), builtinCall(), FunctionCall());
    }

    public Rule FunctionCall() {
        return sequence(iriReference(), ArgList());
    }

    public Rule ArgList() {
        return firstOf(sequence(openingParen(), closingParen()), sequence(
            openingParen(), expression(),
            zeroOrMore(sequence(comma(), expression())), closingParen()
        ));
    }

    public Rule constructTemplate() {
        return sequence(openingCurlyBrace(), optional(ConstructTriples()),
            closingCurlyBrace());
    }

    public Rule ConstructTriples() {
        return sequence(TriplesSameSubject(),
            optional(sequence(dot(), optional(ConstructTriples()))));
    }

    public Rule TriplesSameSubject() {
        return firstOf(sequence(VarOrTerm(), PropertyListNotEmpty()), sequence(
            TriplesNode(), PropertyList()));
    }

    public Rule PropertyListNotEmpty() {
        return sequence(Verb(), ObjectList(), zeroOrMore(
            sequence(semicolon(), optional(sequence(Verb(), ObjectList())))));
    }

    public Rule PropertyList() {
        return optional(PropertyListNotEmpty());
    }

    public Rule ObjectList() {
        return sequence(Object_(), zeroOrMore(sequence(comma(), Object_())));
    }

    public Rule Object_() {
        return GraphNode();
    }

    public Rule Verb() {
        return firstOf(varOrIRIref(), A());
    }

    public Rule TriplesNode() {
        return firstOf(Collection(), BlankNodePropertyList());
    }

    public Rule BlankNodePropertyList() {
        return sequence(openingBracket(), PropertyListNotEmpty(),
            closingBracket());
    }

    public Rule Collection() {
        return sequence(openingParen(), oneOrMore(GraphNode()), closingParen());
    }

    public Rule GraphNode() {
        return firstOf(VarOrTerm(), TriplesNode());
    }

    public Rule VarOrTerm() {
        return firstOf(var(), GraphTerm());
    }

    public Rule varOrIRIref() {
        return firstOf(var(), iriReference());
    }

    public Rule var() {
        return firstOf(var1(), var2());
    }

    public Rule GraphTerm() {
        return firstOf(iriReference(), rdfLiteral(), numericLiteral(),
                booleanLiteral(), blankNode(), sequence(openingParen(),
                closingParen()));
    }

    public Rule expression() {
        return conditionalOrExpression();
    }

    public Rule conditionalOrExpression() {
        return sequence(conditionalAndExpression(),
            zeroOrMore(sequence(or(), conditionalAndExpression())));
    }

    public Rule conditionalAndExpression() {
        return sequence(valueLogical(),
            zeroOrMore(sequence(and(), valueLogical())));
    }

    public Rule valueLogical() {
        return relationalExpression();
    }

    public Rule relationalExpression() {
        return sequence(numericExpression(), optional(firstOf(//
            sequence(equal(), numericExpression()), //
            sequence(notEqual(), numericExpression()), //
            sequence(less(), numericExpression()), //
            sequence(greater(), numericExpression()), //
            sequence(lessOrEqual(), numericExpression()), //
            sequence(greaterOrEqual(), numericExpression()) //
        ) //
        ));
    }

    public Rule numericExpression() {
        return additiveExpression();
    }

    public Rule additiveExpression() {
        return sequence(multiplicativeExpression(), //
            zeroOrMore(firstOf(sequence(plus(), multiplicativeExpression()), //
                sequence(minus(), multiplicativeExpression()), //
                numericLiteralPositive(), numericLiteralNegative()) //
            )
        );
    }

    public Rule multiplicativeExpression() {
        return sequence(unaryExpression(), zeroOrMore(
            firstOf(sequence(asterisk(), unaryExpression()),
                sequence(divide(), unaryExpression()))));
    }

    public Rule unaryExpression() {
        return firstOf(sequence(not(), primaryExpression()), sequence(plus(),
                primaryExpression()), sequence(minus(), primaryExpression()),
                primaryExpression());
    }

    public Rule primaryExpression() {
        return firstOf(bracketedExpression(), builtinCall(),
                iriRefOrFunction(), rdfLiteral(), numericLiteral(),
                booleanLiteral(), var());
    }

    public Rule bracketedExpression() {
        return sequence(openingParen(), expression(), closingParen());
    }

    public Rule builtinCall() {
        return firstOf(
                sequence(str(), openingParen(), expression(), closingParen()),
                sequence(lang(), openingParen(), expression(), closingParen()),
                sequence(langMatches(), openingParen(), expression(), comma(),
                    expression(), closingParen()),
                sequence(dataType(), openingParen(), expression(), closingParen()),
                sequence(bound(), openingParen(), var(), closingParen()),
                sequence(sameTerm(), openingParen(), expression(), comma(),
                    expression(), closingParen()),
                sequence(isIri(), openingParen(), expression(), closingParen()),
                sequence(isUri(), openingParen(), expression(), closingParen()),
                sequence(isBlank(), openingParen(), expression(), closingParen()),
                sequence(isLiteral(), openingParen(), expression(), closingParen()),
                RegexExpression());
    }

    public Rule RegexExpression() {
        return sequence(regex(), openingParen(), expression(), comma(),
            expression(), optional(sequence(comma(), expression())),
            closingParen());
    }

    public Rule iriRefOrFunction() {
        return sequence(iriReference(), optional(ArgList()));
    }

    public Rule rdfLiteral() {
        return sequence(string(),
            optional(firstOf(langTag(), sequence(reference(), iriReference()))));
    }

    public Rule numericLiteral() {
        return firstOf(numericLiteralUnsigned(), numericLiteralPositive(),
                numericLiteralNegative());
    }

    public Rule numericLiteralUnsigned() {
        return firstOf(doubleLiteral(), decimal(), integer());
    }

    public Rule numericLiteralPositive() {
        return firstOf(positiveDouble(), positiveDecimal(),
                positiveInteger());
    }

    public Rule numericLiteralNegative() {
        return firstOf(negativeDouble(), negativeDecimal(),
                negativeInteger());
    }

    public Rule booleanLiteral() {
        return firstOf(booleanTrue(), booleanFalse());
    }

    public Rule string() {
        return firstOf(stringLiteralLong1(), stringLiteral1(),
                stringLiteralLong2(), stringLiteral2());
    }

    public Rule iriReference() {
        return firstOf(iriRef(), prefixedName());
    }

    public Rule prefixedName() {
        return firstOf(pnameLn(), pnameNs());
    }

    public Rule blankNode() {
        return firstOf(blankNodeLabel(), sequence(openingBracket(),
            closingBracket()));
    }
    // </Parser>

    // <Lexer>

    public Rule whitespace() {
        return zeroOrMore(firstOf(comment(), wsNoComment()));
    }

    public Rule wsNoComment() {
        return firstOf(ch(' '), ch('\t'), ch('\f'), eol());
    }

    public Rule pnameNs() {
        return sequence(optional(pnPrefix()), chws(':'));
    }

    public Rule pnameLn() {
        return sequence(pnameNs(), pnLocal());
    }

    public Rule base() {
        return stringIgnoreCaseWS("BASE");
    }

    public Rule prefix() {
        return stringIgnoreCaseWS("PREFIX");
    }

    public Rule select() {
        return stringIgnoreCaseWS("SELECT");
    }

    public Rule distinct() {
        return stringIgnoreCaseWS("DISTINCT");
    }

    public Rule reduced() {
        return stringIgnoreCaseWS("REDUCED");
    }

    public Rule construct() {
        return stringIgnoreCaseWS("CONSTRUCT");
    }

    public Rule describe() {
        return stringIgnoreCaseWS("DESCRIBE");
    }

    public Rule ask() {
        return stringIgnoreCaseWS("ASK");
    }

    public Rule from() {
        return stringIgnoreCaseWS("FROM");
    }

    public Rule named() {
        return stringIgnoreCaseWS("NAMED");
    }

    public Rule where() {
        return stringIgnoreCaseWS("WHERE");
    }

    public Rule order() {
        return stringIgnoreCaseWS("ORDER");
    }

    public Rule by() {
        return stringIgnoreCaseWS("BY");
    }

    public Rule asc() {
        return stringIgnoreCaseWS("ASC");
    }

    public Rule desc() {
        return stringIgnoreCaseWS("DESC");
    }

    public Rule limit() {
        return stringIgnoreCaseWS("LIMIT");
    }

    public Rule offset() {
        return stringIgnoreCaseWS("OFFSET");
    }

    public Rule _optional() {
        return stringIgnoreCaseWS("OPTIONAL");
    }

    public Rule graph() {
        return stringIgnoreCaseWS("graph");
    }

    public Rule union() {
        return stringIgnoreCaseWS("UNION");
    }

    public Rule filter() {
        return stringIgnoreCaseWS("FILTER");
    }

    public Rule A() {
        return chws('a');
    }

    public Rule str() {
        return stringIgnoreCaseWS("STR");
    }

    public Rule lang() {
        return stringIgnoreCaseWS("LANG");
    }

    public Rule langMatches() {
        return stringIgnoreCaseWS("LANGMATCHES");
    }

    public Rule dataType() {
        return stringIgnoreCaseWS("DATATYPE");
    }

    public Rule bound() {
        return stringIgnoreCaseWS("BOUND");
    }

    public Rule sameTerm() {
        return stringIgnoreCaseWS("SAMETERM");
    }

    public Rule isIri() {
        return stringIgnoreCaseWS("ISIRI");
    }

    public Rule isUri() {
        return stringIgnoreCaseWS("ISURI");
    }

    public Rule isBlank() {
        return stringIgnoreCaseWS("ISBLANK");
    }

    public Rule isLiteral() {
        return stringIgnoreCaseWS("ISLITERAL");
    }

    public Rule regex() {
        return stringIgnoreCaseWS("REGEX");
    }

    public Rule booleanTrue() {
        return stringIgnoreCaseWS("TRUE");
    }

    public Rule booleanFalse() {
        return stringIgnoreCaseWS("FALSE");
    }

    public Rule iriRef() {
        return sequence(lessNoComment(), //
            zeroOrMore(sequence(testNot(
                firstOf(lessNoComment(), greater(), '"', openingCurlyBrace(),
                    closingCurlyBrace(), '|', '^', '\\', '`',
                    charRange('\u0000', '\u0020'))
            ), ANY)), //
            greater()
        );
    }

    public Rule blankNodeLabel() {
        return sequence("_:", pnLocal(), whitespace());
    }

    public Rule var1() {
        return sequence('?', varName(), whitespace());
    }

    public Rule var2() {
        return sequence('$', varName(), whitespace());
    }

    public Rule langTag() {
        return sequence('@', oneOrMore(pnCharsBase()), zeroOrMore(
            sequence(minus(), oneOrMore(sequence(pnCharsBase(), digit())))),
            whitespace());
    }

    public Rule integer() {
        return sequence(oneOrMore(digit()), whitespace());
    }

    public Rule decimal() {
        return sequence(firstOf( //
            sequence(oneOrMore(digit()), dot(), zeroOrMore(digit())), //
            sequence(dot(), oneOrMore(digit())) //
        ), whitespace());
    }

    public Rule doubleLiteral() {
        return sequence(firstOf(//
            sequence(oneOrMore(digit()), dot(), zeroOrMore(digit()),
                exponent()), //
            sequence(dot(), oneOrMore(digit()), exponent()), //
            sequence(oneOrMore(digit()), exponent())
        ), whitespace());
    }

    public Rule positiveInteger() {
        return sequence(plus(), integer());
    }

    public Rule positiveDecimal() {
        return sequence(plus(), decimal());
    }

    public Rule positiveDouble() {
        return sequence(plus(), doubleLiteral());
    }

    public Rule negativeInteger() {
        return sequence(minus(), integer());
    }

    public Rule negativeDecimal() {
        return sequence(minus(), decimal());
    }

    public Rule negativeDouble() {
        return sequence(minus(), doubleLiteral());
    }

    public Rule exponent() {
        return sequence(ignoreCase('e'), optional(firstOf(plus(), minus())),
            oneOrMore(digit()));
    }

    public Rule stringLiteral1() {
        return sequence("'", zeroOrMore(
            firstOf(sequence(testNot(firstOf("'", '\\', '\n', '\r')), ANY),
                echar())), "'", whitespace());
    }

    public Rule stringLiteral2() {
        return sequence('"', zeroOrMore(
            firstOf(sequence(testNot(anyOf("\"\\\n\r")), ANY), echar())), '"',
            whitespace());
    }

    public Rule stringLiteralLong1() {
        return sequence("'''", zeroOrMore(sequence(optional(firstOf("''", "'")),
            firstOf(sequence(testNot(firstOf("'", "\\")), ANY), echar())
        )), "'''", whitespace());
    }

    public Rule stringLiteralLong2() {
        return sequence("\"\"\"", zeroOrMore(
            sequence(optional(firstOf("\"\"", "\"")),
                firstOf(sequence(testNot(firstOf("\"", "\\")), ANY), echar()))),
            "\"\"\"", whitespace());
    }

    public Rule echar() {
        return sequence('\\', anyOf("tbnrf\\\"\'"));
    }

    public Rule pnCharsU() {
        return firstOf(pnCharsBase(), '_');
    }

    public Rule varName() {
        return sequence(firstOf(pnCharsU(), digit()), zeroOrMore(
            firstOf(pnCharsU(), digit(), '\u00B7',
                charRange('\u0300', '\u036F'), charRange('\u203F', '\u2040'))),
            whitespace());
    }

    public Rule pnChars() {
        return firstOf(minus(), digit(), pnCharsU(), '\u00B7',
                charRange('\u0300', '\u036F'), charRange('\u203F', '\u2040'));
    }

    public Rule pnPrefix() {
        return sequence(pnCharsBase(), optional(
            zeroOrMore(firstOf(pnChars(), sequence(dot(), pnChars())))));
    }

    public Rule pnLocal() {
        return sequence(firstOf(pnCharsU(), digit()), optional(
                zeroOrMore(firstOf(pnChars(), sequence(dot(), pnChars())))),
            whitespace());
    }

    public Rule pnCharsBase() {
        return firstOf( //
                alpha(),
                charRange('\u00C0', '\u00D6'), //
                charRange('\u00D8', '\u00F6'), //
                charRange('\u00F8', '\u02FF'), //
                charRange('\u0370', '\u037D'), //
                charRange('\u037F', '\u1FFF'), //
                charRange('\u200C', '\u200D'), //
                charRange('\u2070', '\u218F'), //
                charRange('\u2C00', '\u2FEF'), //
                charRange('\u3001', '\uD7FF'), //
                charRange('\uF900', '\uFDCF'), //
                charRange('\uFDF0', '\uFFFD') //
        );
    }

    public Rule comment() {
        return sequence('#', zeroOrMore(sequence(testNot(eol()), ANY)), eol());
    }

    public Rule eol() {
        return anyOf("\n\r");
    }

    public Rule reference() {
        return StringWS("^^");
    }

    public Rule lessOrEqual() {
        return StringWS("<=");
    }

    public Rule greaterOrEqual() {
        return StringWS(">=");
    }

    public Rule notEqual() {
        return StringWS("!=");
    }

    public Rule and() {
        return StringWS("&&");
    }

    public Rule or() {
        return StringWS("||");
    }

    public Rule openingParen() {
        return chws('(');
    }

    public Rule closingParen() {
        return chws(')');
    }

    public Rule openingCurlyBrace() {
        return chws('{');
    }

    public Rule closingCurlyBrace() {
        return chws('}');
    }

    public Rule openingBracket() {
        return chws('[');
    }

    public Rule closingBracket() {
        return chws(']');
    }

    public Rule semicolon() {
        return chws(';');
    }

    public Rule dot() {
        return chws('.');
    }

    public Rule plus() {
        return chws('+');
    }

    public Rule minus() {
        return chws('-');
    }

    public Rule asterisk() {
        return chws('*');
    }

    public Rule comma() {
        return chws(',');
    }

    public Rule not() {
        return chws('!');
    }

    public Rule divide() {
        return chws('/');
    }

    public Rule equal() {
        return chws('=');
    }

    public Rule lessNoComment() {
        return sequence(ch('<'), zeroOrMore(wsNoComment()));
    }

    public Rule less() {
        return chws('<');
    }

    public Rule greater() {
        return chws('>');
    }
    // </Lexer>

    public Rule chws(char c) {
        return sequence(ch(c), whitespace());
    }

    public Rule StringWS(String s) {
        return sequence(string(s), whitespace());
    }

    public Rule stringIgnoreCaseWS(String string) {
        return sequence(ignoreCase(string), whitespace());
    }

}