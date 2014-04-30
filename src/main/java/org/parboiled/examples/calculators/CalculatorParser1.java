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
import org.parboiled.annotations.SuppressSubnodes;

/**
 * A calculator parser building calculation results directly in the parsers value stack.
 * All calculations are implemented directly in action expressions.
 */
@BuildParseTree
public class CalculatorParser1 extends CalculatorParser<Integer> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), EOI);
    }

    public Rule expression() {
        return sequence(term(),
            // a successful match of a term pushes one Integer value onto the value stack
            zeroOrMore(firstOf(
                    // the action that is run after the '+' and the term have been matched consumes the
                    // two top value stack elements and replaces them with the calculation result
                    sequence('+', term(), push(pop() + pop())),

                    // same for the '-' operator, however, here the order of the "pop"s matters, we need to
                    // retrieve the second to last value first, which is what the pop(1) call does
                    sequence('-', term(), push(pop(1) - pop())))
            )
        );
    }

    public Rule term() {
        return sequence(factor(),
            // a successful match of a factor pushes one Integer value onto the value stack
            zeroOrMore(firstOf(
                    // the action that is run after the '*' and the factor have been matched consumes the
                    // two top value stack elements and replaces them with the calculation result
                    sequence('*', factor(), push(pop() * pop())),

                    // same for the '/' operator, however, here the order of the "pop"s matters, we need to
                    // retrieve the second to last value first, which is what the pop(1) call does
                    sequence('/', factor(), push(pop(1) / pop())))
            )
        );
    }

    public Rule factor() {
        return firstOf(number(), parens()); // a factor "produces" exactly one Integer value on the value stack
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(digits(),

            // parse the input text matched by the preceding "digits" rule,
            // convert it into an Integer and push it onto the value stack
            // the action uses a default string in case it is run during error recovery (resynchronization)
            push(Integer.parseInt(matchOrDefault("0"))));
    }

    @SuppressSubnodes
    public Rule digits() {
        return oneOrMore(digit());
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser1.class);
    }

}