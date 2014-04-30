/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.examples.calculators;

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.examples.calculators.CalculatorParser3.CalcNode;
import org.parboiled.support.Var;

/**
 * A calculator parser defining the same language as the CalculatorParser3 but using a rule building helper methods
 * to factor out common constructs.
 */
@BuildParseTree
public class CalculatorParser4 extends CalculatorParser<CalcNode> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), EOI);
    }

    public Rule expression() {
        return operatorRule(term(), firstOf("+ ", "- "));
    }

    public Rule term() {
        return operatorRule(factor(), firstOf("* ", "/ "));
    }

    public Rule factor() {
        // by using toRule("^ ") instead of Ch('^') we make use of the fromCharLiteral(...) transformation below
        return operatorRule(atom(), toRule("^ "));
    }

    public Rule operatorRule(Rule subRule, Rule operatorRule) {
        Var<Character> op = new Var<Character>();
        return sequence(subRule,
            zeroOrMore(operatorRule, op.set(matchedChar()), subRule,
                push(new CalcNode(op.get(), pop(1), pop())))
        );
    }

    public Rule atom() {
        return firstOf(number(), squareRoot(), parens());
    }

    public Rule squareRoot() {
        return sequence("SQRT", parens(), push(new CalcNode('R', pop(), null)));
    }

    public Rule parens() {
        return sequence("( ", expression(), ") ");
    }

    public Rule number() {
        return sequence(sequence(optional(ch('-')), oneOrMore(digit()),
                optional(ch('.'), oneOrMore(digit()))),
            // the action uses a default string in case it is run during error recovery (resynchronization)
            push(new CalcNode(Double.parseDouble(matchOrDefault("0")))),
            whiteSpace()
        );
    }

    public Rule whiteSpace() {
        return zeroOrMore(anyOf(" \t\f"));
    }

    // we redefine the rule creation for string literals to automatically match trailing whitespace if the string
    // literal ends with a space character, this way we don't have to insert extra whitespace() rules after each
    // character or string literal
    @Override
    protected Rule fromStringLiteral(String string) {
        return string.endsWith(" ") ?
                sequence(string(string.substring(0, string.length() - 1)), whiteSpace()) :
                string(string);
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser4.class);
    }
}