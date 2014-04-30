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

/**
 * A basic calculator parser without any actions.
 */
@BuildParseTree
public class CalculatorParser0 extends CalculatorParser<Integer> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), EOI);
    }

    Rule expression() {
        return sequence(term(), zeroOrMore(anyOf("+-"), term()));
    }

    Rule term() {
        return sequence(factor(), zeroOrMore(anyOf("*/"), factor()));
    }

    Rule factor() {
        return firstOf(number(), parens());
    }

    Rule parens() {
        return sequence('(', expression(), ')');
    }

    Rule number() {
        return oneOrMore(digit());
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser0.class);
    }

}