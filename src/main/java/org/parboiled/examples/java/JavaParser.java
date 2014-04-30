//===========================================================================
//
//  Parsing expression Grammar for Java 1.6 as a parboiled parser.
//  Based on Chapters 3 and 18 of Java Language Specification, Third Edition (JLS)
//  at http://java.sun.com/docs/books/jls/third_edition/html/j3TOC.html.
//
//---------------------------------------------------------------------------
//
//  Copyright (C) 2010 by Mathias Doenitz
//  Based on the Mouse 1.3 grammar for Java 1.6, which is
//  Copyright (C) 2006, 2009, 2010, 2011 by Roman R Redziejowski (www.romanredz.se).
//
//  The author gives unlimited permission to copy and distribute
//  this file, with or without modifications, as long as this notice
//  is preserved, and any changes are properly documented.
//
//  This file is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//---------------------------------------------------------------------------
//
//  Change log
//    2006-12-06 Posted on Internet.
//    2009-04-04 Modified to conform to Mouse syntax:
//               Underscore removed from names
//               \f in Space replaced by Unicode for FormFeed.
//    2009-07-10 Unused rule THREADSAFE removed.
//    2009-07-10 Copying and distribution conditions relaxed by the author.
//    2010-01-28 Transcribed to parboiled
//    2010-02-01 Fixed problem in rule "formalParameterDecls"
//    2010-03-29 Fixed problem in "annotation"
//    2010-03-31 Fixed problem in unicode escapes, String literals and line comments
//               (Thanks to Reinier Zwitserloot for the finds)
//    2010-07-26 Fixed problem in localVariableDeclarationStatement (accept annotations),
//               hexFloat (hexSignificant) and annotationTypeDeclaration (bug in the JLS!)
//    2010-10-07 Added full support of Unicode Identifiers as set forth in the JLS
//               (Thanks for Ville Peurala for the patch)
//    2011-07-23 Transcribed all missing fixes from Romans Mouse grammar (http://www.romanredz.se/papers/Java.1.6.peg)
//
//===========================================================================

package org.parboiled.examples.java;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.*;

@SuppressWarnings({"InfiniteRecursion"})
@BuildParseTree
public class JavaParser extends BaseParser<Object> {

    //-------------------------------------------------------------------------
    //  Compilation Unit
    //-------------------------------------------------------------------------

    public Rule compilationUnit() {
        return sequence(spacing(), optional(packageDeclaration()),
            zeroOrMore(importDeclaration()), zeroOrMore(typeDeclaration()), EOI);
    }

    Rule packageDeclaration() {
        return sequence(zeroOrMore(annotation()),
            sequence(PACKAGE, qualifiedIdentifier(), SEMI));
    }

    Rule importDeclaration() {
        return sequence(IMPORT, optional(STATIC), qualifiedIdentifier(),
            optional(DOT, STAR), SEMI);
    }

    Rule typeDeclaration() {
        return firstOf(sequence(zeroOrMore(modifier()),
                firstOf(classDeclaration(), enumDeclaration(),
                    interfaceDeclaration(), annotationTypeDeclaration())
            ), SEMI
        );
    }

    //-------------------------------------------------------------------------
    //  Class Declaration
    //-------------------------------------------------------------------------

    Rule classDeclaration() {
        return sequence(
                CLASS,
                identifier(),
                optional(TypeParameters()),
                optional(EXTENDS, classType()),
                optional(IMPLEMENTS, classTypeList()),
                classBody()
        );
    }

    Rule classBody() {
        return sequence(LWING, zeroOrMore(classBodyDeclaration()), RWING);
    }

    Rule classBodyDeclaration() {
        return firstOf(SEMI, sequence(optional(STATIC), block()),
            sequence(zeroOrMore(modifier()), memberDecl()));
    }

    Rule memberDecl() {
        return firstOf(
            sequence(TypeParameters(), genericMethodOrConstructorRest()),
            sequence(type(), identifier(), methodDeclaratorRest()),
            sequence(type(), variableDeclarators(), SEMI),
            sequence(VOID, identifier(), voidMethodDeclaratorRest()),
            sequence(identifier(), constructorDeclaratorRest()),
            interfaceDeclaration(), classDeclaration(), enumDeclaration(),
            annotationTypeDeclaration());
    }

    Rule genericMethodOrConstructorRest() {
        return firstOf(sequence(firstOf(type(), VOID), identifier(),
            methodDeclaratorRest()),
            sequence(identifier(), constructorDeclaratorRest()));
    }

    Rule methodDeclaratorRest() {
        return sequence(formalParameters(), zeroOrMore(dim()),
            optional(THROWS, classTypeList()), firstOf(methodBody(), SEMI));
    }

    Rule voidMethodDeclaratorRest() {
        return sequence(formalParameters(), optional(THROWS, classTypeList()),
            firstOf(methodBody(), SEMI));
    }

    Rule constructorDeclaratorRest() {
        return sequence(formalParameters(), optional(THROWS, classTypeList()),
            methodBody());
    }

    Rule methodBody() {
        return block();
    }

    //-------------------------------------------------------------------------
    //  Interface Declaration
    //-------------------------------------------------------------------------

    Rule interfaceDeclaration() {
        return sequence(INTERFACE, identifier(), optional(TypeParameters()),
            optional(EXTENDS, classTypeList()), interfaceBody());
    }

    Rule interfaceBody() {
        return sequence(LWING, zeroOrMore(interfaceBodyDeclaration()), RWING);
    }

    Rule interfaceBodyDeclaration() {
        return firstOf(sequence(zeroOrMore(modifier()), interfaceMemberDecl()),
            SEMI);
    }

    Rule interfaceMemberDecl() {
        return firstOf(interfaceMethodOrFieldDecl(),
            interfaceGenericMethodDecl(),
            sequence(VOID, identifier(), voidInterfaceMethodDeclaratorsRest()),
            interfaceDeclaration(), annotationTypeDeclaration(),
            classDeclaration(), enumDeclaration());
    }

    Rule interfaceMethodOrFieldDecl() {
        return sequence(sequence(type(), identifier()),
            interfaceMethodOrFieldRest());
    }

    Rule interfaceMethodOrFieldRest() {
        return firstOf(sequence(constantDeclaratorsRest(), SEMI),
            interfaceMethodDeclaratorRest());
    }

    Rule interfaceMethodDeclaratorRest() {
        return sequence(formalParameters(), zeroOrMore(dim()),
            optional(THROWS, classTypeList()), SEMI);
    }

    Rule interfaceGenericMethodDecl() {
        return sequence(TypeParameters(), firstOf(type(), VOID), identifier(),
            interfaceMethodDeclaratorRest());
    }

    Rule voidInterfaceMethodDeclaratorsRest() {
        return sequence(formalParameters(), optional(THROWS, classTypeList()),
            SEMI);
    }

    Rule constantDeclaratorsRest() {
        return sequence(constantDeclaratorRest(),
            zeroOrMore(COMMA, constantDeclarator()));
    }

    Rule constantDeclarator() {
        return sequence(identifier(), constantDeclaratorRest());
    }

    Rule constantDeclaratorRest() {
        return sequence(zeroOrMore(dim()), EQU, variableInitializer());
    }

    //-------------------------------------------------------------------------
    //  Enum Declaration
    //-------------------------------------------------------------------------

    Rule enumDeclaration() {
        return sequence(ENUM, identifier(),
            optional(IMPLEMENTS, classTypeList()), enumBody());
    }

    Rule enumBody() {
        return sequence(LWING, optional(enumConstants()), optional(COMMA),
            optional(enumBodyDeclarations()), RWING);
    }

    Rule enumConstants() {
        return sequence(enumConstant(), zeroOrMore(COMMA, enumConstant()));
    }

    Rule enumConstant() {
        return sequence(zeroOrMore(annotation()), identifier(),
            optional(arguments()), optional(classBody()));
    }

    Rule enumBodyDeclarations() {
        return sequence(SEMI, zeroOrMore(classBodyDeclaration()));
    }

    //-------------------------------------------------------------------------
    //  Variable Declarations
    //-------------------------------------------------------------------------    

    Rule localVariableDeclarationStatement() {
        return sequence(zeroOrMore(firstOf(FINAL, annotation())), type(),
            variableDeclarators(), SEMI);
    }

    Rule variableDeclarators() {
        return sequence(variableDeclarator(),
            zeroOrMore(COMMA, variableDeclarator()));
    }

    Rule variableDeclarator() {
        return sequence(identifier(), zeroOrMore(dim()),
            optional(EQU, variableInitializer()));
    }

    //-------------------------------------------------------------------------
    //  Formal Parameters
    //-------------------------------------------------------------------------

    Rule formalParameters() {
        return sequence(LPAR, optional(formalParameterDecls()), RPAR);
    }

    Rule formalParameter() {
        return sequence(zeroOrMore(firstOf(FINAL, annotation())), type(),
            variableDeclaratorId());
    }

    Rule formalParameterDecls() {
        return sequence(zeroOrMore(firstOf(FINAL, annotation())), type(),
            formalParameterDeclsRest());
    }

    Rule formalParameterDeclsRest() {
        return firstOf(sequence(variableDeclaratorId(),
            optional(COMMA, formalParameterDecls())),
            sequence(ELLIPSIS, variableDeclaratorId()));
    }

    Rule variableDeclaratorId() {
        return sequence(identifier(), zeroOrMore(dim()));
    }

    //-------------------------------------------------------------------------
    //  Statements
    //-------------------------------------------------------------------------    

    Rule block() {
        return Sequence(LWING, blockStatements(), RWING);
    }

    Rule blockStatements() {
        return zeroOrMore(blockStatement());
    }

    Rule blockStatement() {
        return firstOf(localVariableDeclarationStatement(),
            sequence(zeroOrMore(modifier()),
                firstOf(classDeclaration(), enumDeclaration())), statement());
    }

    Rule statement() {
        return firstOf(block(),
            sequence(ASSERT, expression(), optional(COLON, expression()), SEMI),
            sequence(IF, parExpression(), statement(),
                optional(ELSE, statement())),
            sequence(FOR, LPAR, optional(forInit()), SEMI,
                optional(expression()), SEMI, optional(forUpdate()), RPAR,
                statement()),
            sequence(FOR, LPAR, formalParameter(), COLON, expression(), RPAR,
                statement()), sequence(WHILE, parExpression(), statement()),
            sequence(DO, statement(), WHILE, parExpression(), SEMI),
            sequence(TRY, block(),
                firstOf(sequence(oneOrMore(catchBlock()), optional(
                    finallyBlock())),
                    finallyBlock())), sequence(SWITCH, parExpression(), LWING,
                switchBlockStatementGroups(), RWING),
            sequence(SYNCHRONIZED, parExpression(), block()),
            sequence(RETURN, optional(expression()), SEMI),
            sequence(THROW, expression(), SEMI),
            sequence(BREAK, optional(identifier()), SEMI),
            sequence(CONTINUE, optional(identifier()), SEMI),
            sequence(sequence(identifier(), COLON), statement()),
            sequence(statementExpression(), SEMI), SEMI
        );
    }

    Rule catchBlock() {
        return sequence(CATCH, LPAR, formalParameter(), RPAR, block());
    }

    Rule finallyBlock() {
        return sequence(FINALLY, block());
    }

    Rule switchBlockStatementGroups() {
        return zeroOrMore(switchBlockStatementGroup());
    }

    Rule switchBlockStatementGroup() {
        return sequence(switchLabel(), blockStatements());
    }

    Rule switchLabel() {
        return firstOf(sequence(CASE, constantExpression(), COLON),
            sequence(CASE, enumConstantName(), COLON), sequence(DEFAULT, COLON));
    }

    Rule forInit() {
        return firstOf(
            sequence(zeroOrMore(firstOf(FINAL, annotation())), type(),
                variableDeclarators()), sequence(statementExpression(),
                zeroOrMore(COMMA, statementExpression()))
        );
    }

    Rule forUpdate() {
        return sequence(statementExpression(),
            zeroOrMore(COMMA, statementExpression()));
    }

    Rule enumConstantName() {
        return identifier();
    }

    //-------------------------------------------------------------------------
    //  Expressions
    //-------------------------------------------------------------------------

    // The following is more generous than the definition in section 14.8,
    // which allows only specific forms of expression.

    Rule statementExpression() {
        return expression();
    }

    Rule constantExpression() {
        return expression();
    }

    // The following definition is part of the modification in JLS Chapter 18
    // to minimize look ahead. In JLS Chapter 15.27, expression is defined
    // as AssignmentExpression, which is effectively defined as
    // (LeftHandSide assignmentOperator)* conditionalExpression.
    // The following is obtained by allowing ANY conditionalExpression
    // as LeftHandSide, which results in accepting statements like 5 = a.

    Rule expression() {
        return sequence(conditionalExpression(),
            zeroOrMore(assignmentOperator(), conditionalExpression()));
    }

    Rule assignmentOperator() {
        return firstOf(EQU, PLUSEQU, MINUSEQU, STAREQU, DIVEQU, ANDEQU, OREQU,
            HATEQU, MODEQU, SLEQU, SREQU, BSREQU);
    }

    Rule conditionalExpression() {
        return sequence(conditionalOrExpression(),
            zeroOrMore(QUERY, expression(), COLON, conditionalOrExpression()));
    }

    Rule conditionalOrExpression() {
        return sequence(conditionalAndExpression(),
            zeroOrMore(OROR, conditionalAndExpression()));
    }

    Rule conditionalAndExpression() {
        return sequence(inclusiveOrExpression(),
            zeroOrMore(ANDAND, inclusiveOrExpression()));
    }

    Rule inclusiveOrExpression() {
        return sequence(exclusiveOrExpression(),
            zeroOrMore(OR, exclusiveOrExpression()));
    }

    Rule exclusiveOrExpression() {
        return sequence(andExpression(), ZeroOrMore(HAT, andExpression()));
    }

    Rule andExpression() {
        return sequence(equalityExpression(),
            zeroOrMore(AND, equalityExpression()));
    }

    Rule equalityExpression() {
        return sequence(relationalExpression(),
            zeroOrMore(firstOf(EQUAL, NOTEQUAL), relationalExpression()));
    }

    Rule relationalExpression() {
        return sequence(shiftExpression(), zeroOrMore(
                firstOf(sequence(firstOf(LE, GE, LT, GT), shiftExpression()),
                    sequence(INSTANCEOF, referenceType()))
            )
        );
    }

    Rule shiftExpression() {
        return sequence(additiveExpression(),
            zeroOrMore(firstOf(SL, SR, BSR), additiveExpression()));
    }

    Rule additiveExpression() {
        return sequence(multiplicativeExpression(),
            zeroOrMore(firstOf(PLUS, MINUS), multiplicativeExpression()));
    }

    Rule multiplicativeExpression() {
        return sequence(unaryExpression(),
            zeroOrMore(firstOf(STAR, DIV, MOD), unaryExpression()));
    }

    Rule unaryExpression() {
        return firstOf(sequence(prefixOp(), unaryExpression()),
            sequence(LPAR, type(), RPAR, unaryExpression()),
            sequence(primary(), zeroOrMore(selector()), zeroOrMore(postfixOp())));
    }

    Rule primary() {
        return firstOf(parExpression(), sequence(nonWildcardTypeArguments(),
                firstOf(explicitGenericInvocationSuffix(),
                    sequence(THIS, arguments()))),
            sequence(THIS, optional(arguments())),
            sequence(SUPER, superSuffix()), literal(), sequence(NEW, creator()),
            sequence(qualifiedIdentifier(), optional(identifierSuffix())),
            sequence(basicType(), zeroOrMore(dim()), DOT, CLASS),
            sequence(VOID, DOT, CLASS)
        );
    }

    Rule identifierSuffix() {
        return firstOf(sequence(LBRK,
                firstOf(sequence(RBRK, zeroOrMore(dim()), DOT, CLASS),
                    sequence(expression(), RBRK))
            ), arguments(), sequence(DOT,
                firstOf(CLASS, explicitGenericInvocation(), THIS,
                    sequence(SUPER, arguments()),
                    sequence(NEW, optional(nonWildcardTypeArguments()),
                        innerCreator())
                )
            )
        );
    }

    Rule explicitGenericInvocation() {
        return sequence(nonWildcardTypeArguments(),
            explicitGenericInvocationSuffix());
    }

    Rule nonWildcardTypeArguments() {
        return sequence(LPOINT, referenceType(),
            zeroOrMore(COMMA, referenceType()), RPOINT);
    }

    Rule explicitGenericInvocationSuffix() {
        return firstOf(sequence(SUPER, superSuffix()),
            sequence(identifier(), arguments()));
    }

    Rule prefixOp() {
        return firstOf(INC, DEC, BANG, TILDA, PLUS, MINUS);
    }

    Rule postfixOp() {
        return firstOf(INC, DEC);
    }

    Rule selector() {
        return firstOf(sequence(DOT, identifier(), optional(arguments())),
            sequence(DOT, explicitGenericInvocation()), sequence(DOT, THIS),
            sequence(DOT, SUPER, superSuffix()),
            sequence(DOT, NEW, optional(nonWildcardTypeArguments()),
                innerCreator()), dimExpr());
    }

    Rule superSuffix() {
        return firstOf(arguments(),
            sequence(DOT, identifier(), optional(arguments())));
    }

    @MemoMismatches
    Rule basicType() {
        return sequence(
            firstOf("byte", "short", "char", "int", "long", "float", "double",
                "boolean"), testNot(letterOrDigit()), spacing());
    }

    Rule arguments() {
        return sequence(LPAR,
            optional(expression(), zeroOrMore(COMMA, expression())), RPAR);
    }

    Rule creator() {
        return firstOf(
            sequence(optional(nonWildcardTypeArguments()), createdName(),
                classCreatorRest()),
            sequence(optional(nonWildcardTypeArguments()),
                firstOf(classType(), basicType()), arrayCreatorRest()));
    }

    Rule createdName() {
        return sequence(identifier(), optional(nonWildcardTypeArguments()),
            zeroOrMore(DOT, identifier(), optional(nonWildcardTypeArguments()))
        );
    }

    Rule innerCreator() {
        return sequence(identifier(), classCreatorRest());
    }

    // The following is more generous than JLS 15.10. According to that definition,
    // basicType must be followed by at least one dimExpr or by arrayInitializer.
    Rule arrayCreatorRest() {
        return sequence(LBRK,
            firstOf(sequence(RBRK, zeroOrMore(dim()), arrayInitializer()),
                sequence(expression(), RBRK, zeroOrMore(dimExpr()),
                    zeroOrMore(dim()))
            )
        );
    }

    Rule classCreatorRest() {
        return sequence(arguments(), optional(classBody()));
    }

    Rule arrayInitializer() {
        return sequence(LWING, optional(variableInitializer(),
                zeroOrMore(COMMA, variableInitializer())), optional(COMMA),
            RWING
        );
    }

    Rule variableInitializer() {
        return firstOf(arrayInitializer(), expression());
    }

    Rule parExpression() {
        return sequence(LPAR, expression(), RPAR);
    }

    Rule qualifiedIdentifier() {
        return sequence(identifier(), zeroOrMore(DOT, identifier()));
    }

    Rule dim() {
        return sequence(LBRK, RBRK);
    }

    Rule dimExpr() {
        return sequence(LBRK, expression(), RBRK);
    }

    //-------------------------------------------------------------------------
    //  Types and Modifiers
    //-------------------------------------------------------------------------

    Rule type() {
        return sequence(firstOf(basicType(), classType()), zeroOrMore(dim()));
    }

    Rule referenceType() {
        return firstOf(sequence(basicType(), oneOrMore(dim())),
            sequence(classType(), zeroOrMore(dim())));
    }

    Rule classType() {
        return sequence(identifier(), optional(typeArguments()),
            zeroOrMore(DOT, identifier(), optional(typeArguments())));
    }

    Rule classTypeList() {
        return sequence(classType(), zeroOrMore(COMMA, classType()));
    }

    Rule typeArguments() {
        return sequence(LPOINT, typeArgument(),
            zeroOrMore(COMMA, typeArgument()), RPOINT);
    }

    Rule typeArgument() {
        return firstOf(referenceType(),
            sequence(QUERY, optional(firstOf(EXTENDS, SUPER), referenceType())));
    }

    Rule TypeParameters() {
        return sequence(LPOINT, typeParameter(),
            zeroOrMore(COMMA, typeParameter()), RPOINT);
    }

    Rule typeParameter() {
        return sequence(identifier(), optional(EXTENDS, bound()));
    }

    Rule bound() {
        return sequence(classType(), zeroOrMore(AND, classType()));
    }

    // the following common definition of modifier is part of the modification
    // in JLS Chapter 18 to minimize look ahead. The main body of JLS has
    // different lists of modifiers for different language elements.
    Rule modifier() {
        return firstOf(annotation(), sequence(
                firstOf("public", "protected", "private", "static", "abstract",
                    "final", "native", "synchronized", "transient", "volatile",
                    "strictfp"), testNot(letterOrDigit()), spacing()
            )
        );
    }

    //-------------------------------------------------------------------------
    //  Annotations
    //-------------------------------------------------------------------------    

    Rule annotationTypeDeclaration() {
        return sequence(AT, INTERFACE, identifier(), annotationTypeBody());
    }

    Rule annotationTypeBody() {
        return sequence(LWING, zeroOrMore(annotationTypeElementDeclaration()),
            RWING);
    }

    Rule annotationTypeElementDeclaration() {
        return firstOf(
            sequence(zeroOrMore(modifier()), annotationTypeElementRest()), SEMI);
    }

    Rule annotationTypeElementRest() {
        return firstOf(sequence(type(), annotationMethodOrConstantRest(), SEMI),
            classDeclaration(), enumDeclaration(), interfaceDeclaration(),
            annotationTypeDeclaration());
    }

    Rule annotationMethodOrConstantRest() {
        return firstOf(annotationMethodRest(), annotationConstantRest());
    }

    Rule annotationMethodRest() {
        return sequence(identifier(), LPAR, RPAR, optional(defaultValue()));
    }

    Rule annotationConstantRest() {
        return variableDeclarators();
    }

    Rule defaultValue() {
        return sequence(DEFAULT, elementValue());
    }

    @MemoMismatches
    Rule annotation() {
        return sequence(AT, qualifiedIdentifier(), optional(annotationRest()));
    }

    Rule annotationRest() {
        return firstOf(normalAnnotationRest(), singleElementAnnotationRest());
    }

    Rule normalAnnotationRest() {
        return sequence(LPAR, optional(elementValuePairs()), RPAR);
    }

    Rule elementValuePairs() {
        return sequence(elementValuePair(),
            zeroOrMore(COMMA, elementValuePair()));
    }

    Rule elementValuePair() {
        return sequence(identifier(), EQU, elementValue());
    }

    Rule elementValue() {
        return firstOf(conditionalExpression(), annotation(),
            elementValueArrayInitializer());
    }

    Rule elementValueArrayInitializer() {
        return sequence(LWING, optional(elementValues()), optional(COMMA),
            RWING);
    }

    Rule elementValues() {
        return sequence(elementValue(), zeroOrMore(COMMA, elementValue()));
    }

    Rule singleElementAnnotationRest() {
        return sequence(LPAR, elementValue(), RPAR);
    }

    //-------------------------------------------------------------------------
    //  JLS 3.6-7  spacing
    //-------------------------------------------------------------------------

    @SuppressNode
    Rule spacing() {
        return zeroOrMore(firstOf(

            // whitespace
            oneOrMore(anyOf(" \t\r\n\f").label("Whitespace")),

            // traditional comment
            sequence("/*", zeroOrMore(testNot("*/"), ANY), "*/"),

            // end of line comment
            sequence("//", zeroOrMore(testNot(anyOf("\r\n")), ANY),
                firstOf("\r\n", '\r', '\n', EOI))
        ));
    }

    //-------------------------------------------------------------------------
    //  JLS 3.8  Identifiers
    //-------------------------------------------------------------------------

    @SuppressSubnodes
    @MemoMismatches
    Rule identifier() {
        return sequence(testNot(keyword()), letter(),
            zeroOrMore(letterOrDigit()), spacing());
    }

    // JLS defines letters and digits as Unicode characters recognized
    // as such by special Java procedures.

    Rule letter() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
        return firstOf(sequence('\\', unicodeEscape()), new JavaLetterMatcher());
    }

    @MemoMismatches
    Rule letterOrDigit() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '$');
        return firstOf(sequence('\\', unicodeEscape()),
            new JavaLetterOrDigitMatcher());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.9  Keywords
    //-------------------------------------------------------------------------

    @MemoMismatches
    Rule keyword() {
        return sequence(
            firstOf("assert", "break", "case", "catch", "class", "const",
                "continue", "default", "do", "else", "enum", "extends",
                "finally", "final", "for", "goto", "if", "implements", "import",
                "interface", "instanceof", "new", "package", "return", "static",
                "super", "switch", "synchronized", "this", "throws", "throw",
                "try", "void", "while"), testNot(letterOrDigit())
        );
    }

    public final Rule ASSERT = keyword("assert");
    public final Rule BREAK = keyword("break");
    public final Rule CASE = keyword("case");
    public final Rule CATCH = keyword("catch");
    public final Rule CLASS = keyword("class");
    public final Rule CONTINUE = keyword("continue");
    public final Rule DEFAULT = keyword("default");
    public final Rule DO = keyword("do");
    public final Rule ELSE = keyword("else");
    public final Rule ENUM = keyword("enum");
    public final Rule EXTENDS = keyword("extends");
    public final Rule FINALLY = keyword("finally");
    public final Rule FINAL = keyword("final");
    public final Rule FOR = keyword("for");
    public final Rule IF = keyword("if");
    public final Rule IMPLEMENTS = keyword("implements");
    public final Rule IMPORT = keyword("import");
    public final Rule INTERFACE = keyword("interface");
    public final Rule INSTANCEOF = keyword("instanceof");
    public final Rule NEW = keyword("new");
    public final Rule PACKAGE = keyword("package");
    public final Rule RETURN = keyword("return");
    public final Rule STATIC = keyword("static");
    public final Rule SUPER = keyword("super");
    public final Rule SWITCH = keyword("switch");
    public final Rule SYNCHRONIZED = keyword("synchronized");
    public final Rule THIS = keyword("this");
    public final Rule THROWS = keyword("throws");
    public final Rule THROW = keyword("throw");
    public final Rule TRY = keyword("try");
    public final Rule VOID = keyword("void");
    public final Rule WHILE = keyword("while");

    @SuppressNode
    @DontLabel
    Rule keyword(String keyword) {
        return terminal(keyword, letterOrDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.10  Literals
    //-------------------------------------------------------------------------

    Rule literal() {
        return sequence(firstOf(floatLiteral(), integerLiteral(), charLiteral(),
                stringLiteral(), sequence("true", testNot(letterOrDigit())),
                sequence("false", testNot(letterOrDigit())),
                sequence("null", testNot(letterOrDigit()))), spacing()
        );
    }

    @SuppressSubnodes
    Rule integerLiteral() {
        return sequence(firstOf(hexNumeral(), octalNumeral(), decimalNumeral()),
            optional(anyOf("lL")));
    }

    @SuppressSubnodes
    Rule decimalNumeral() {
        return firstOf('0', sequence(charRange('1', '9'), zeroOrMore(digit())));
    }

    @SuppressSubnodes

    @MemoMismatches
    Rule hexNumeral() {
        return sequence('0', ignoreCase('x'), oneOrMore(hexDigit()));
    }

    @SuppressSubnodes
    Rule octalNumeral() {
        return sequence('0', oneOrMore(charRange('0', '7')));
    }

    Rule floatLiteral() {
        return firstOf(hexFloat(), decimalFloat());
    }

    @SuppressSubnodes
    Rule decimalFloat() {
        return firstOf(sequence(oneOrMore(digit()), '.', zeroOrMore(digit()),
                optional(exponent()), optional(anyOf("fFdD"))),
            sequence('.', oneOrMore(digit()), optional(exponent()),
                optional(anyOf("fFdD"))),
            sequence(oneOrMore(digit()), exponent(), optional(anyOf("fFdD"))),
            sequence(oneOrMore(digit()), optional(exponent()), anyOf("fFdD"))
        );
    }

    Rule exponent() {
        return sequence(anyOf("eE"), optional(anyOf("+-")), oneOrMore(digit()));
    }

    @SuppressSubnodes
    Rule hexFloat() {
        return sequence(hexSignificant(), binaryExponent(),
            optional(anyOf("fFdD")));
    }

    Rule hexSignificant() {
        return firstOf(
            sequence(firstOf("0x", "0X"), zeroOrMore(hexDigit()), '.',
                oneOrMore(hexDigit())), sequence(hexNumeral(), optional('.')));
    }

    Rule binaryExponent() {
        return sequence(anyOf("pP"), optional(anyOf("+-")), oneOrMore(digit()));
    }

    Rule charLiteral() {
        return sequence('\'',
            firstOf(escape(), sequence(testNot(anyOf("'\\")), ANY))
                .suppressSubnodes(), '\'');
    }

    Rule stringLiteral() {
        return sequence('"', zeroOrMore(
                firstOf(escape(), sequence(testNot(anyOf("\r\n\"\\")), ANY))
            ).suppressSubnodes(), '"'
        );
    }

    Rule escape() {
        return sequence('\\',
            firstOf(anyOf("btnfr\"\'\\"), octalEscape(), unicodeEscape()));
    }

    Rule octalEscape() {
        return firstOf(sequence(charRange('0', '3'), charRange('0', '7'),
            charRange('0', '7')),
            sequence(charRange('0', '7'), charRange('0', '7')),
            charRange('0', '7'));
    }

    Rule unicodeEscape() {
        return Sequence(oneOrMore('u'), hexDigit(), hexDigit(), hexDigit(), hexDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.11-12  Separators, Operators
    //-------------------------------------------------------------------------

    final Rule AT = terminal("@");
    final Rule AND = terminal("&", anyOf("=&"));
    final Rule ANDAND = terminal("&&");
    final Rule ANDEQU = terminal("&=");
    final Rule BANG = terminal("!", ch('='));
    final Rule BSR = terminal(">>>", ch('='));
    final Rule BSREQU = terminal(">>>=");
    final Rule COLON = terminal(":");
    final Rule COMMA = terminal(",");
    final Rule DEC = terminal("--");
    final Rule DIV = terminal("/", ch('='));
    final Rule DIVEQU = terminal("/=");
    final Rule DOT = terminal(".");
    final Rule ELLIPSIS = terminal("...");
    final Rule EQU = terminal("=", ch('='));
    final Rule EQUAL = terminal("==");
    final Rule GE = terminal(">=");
    final Rule GT = terminal(">", anyOf("=>"));
    final Rule HAT = terminal("^", ch('='));
    final Rule HATEQU = terminal("^=");
    final Rule INC = terminal("++");
    final Rule LBRK = terminal("[");
    final Rule LE = terminal("<=");
    final Rule LPAR = terminal("(");
    final Rule LPOINT = terminal("<");
    final Rule LT = terminal("<", anyOf("=<"));
    final Rule LWING = terminal("{");
    final Rule MINUS = terminal("-", anyOf("=-"));
    final Rule MINUSEQU = terminal("-=");
    final Rule MOD = terminal("%", ch('='));
    final Rule MODEQU = terminal("%=");
    final Rule NOTEQUAL = terminal("!=");
    final Rule OR = terminal("|", anyOf("=|"));
    final Rule OREQU = terminal("|=");
    final Rule OROR = terminal("||");
    final Rule PLUS = terminal("+", anyOf("=+"));
    final Rule PLUSEQU = terminal("+=");
    final Rule QUERY = terminal("?");
    final Rule RBRK = terminal("]");
    final Rule RPAR = terminal(")");
    final Rule RPOINT = terminal(">");
    final Rule RWING = terminal("}");
    final Rule SEMI = terminal(";");
    final Rule SL = terminal("<<", ch('='));
    final Rule SLEQU = terminal("<<=");
    final Rule SR = terminal(">>", anyOf("=>"));
    final Rule SREQU = terminal(">>=");
    final Rule STAR = terminal("*", ch('='));
    final Rule STAREQU = terminal("*=");
    final Rule TILDA = terminal("~");

    //-------------------------------------------------------------------------
    //  helper methods
    //-------------------------------------------------------------------------

    @Override
    protected Rule fromCharLiteral(char c) {
        // turn of creation of parse tree nodes for single characters
        return super.fromCharLiteral(c).suppressNode();
    }

    @SuppressNode
    @DontLabel
    Rule terminal(String string) {
        return sequence(string, spacing()).label('\'' + string + '\'');
    }

    @SuppressNode
    @DontLabel
    Rule terminal(String string, Rule mustNotFollow) {
        return sequence(string, testNot(mustNotFollow), spacing()).label('\'' + string + '\'');
    }
}
