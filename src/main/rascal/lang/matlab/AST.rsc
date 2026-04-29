module lang::matlab::AST

import util::FileSystem;
import util::Reflective;
import IO;
import String;
import List;
import Message;
import Node;


extend analysis::m3::AST;
extend analysis::m3::Core;

data Declaration
     = \class(Expression name, list[Attribute] attributes, list[Expression] superclasses, list[Declaration] body)
     | \function(list[Expression] returnVars, Expression name, list[Expression] parameters, Statement body)
     | \function(list[Expression] returnVars, Expression name, list[Expression] parameters)
     | \function(list[Expression] returnVars, Expression name, list[Expression] parameters, list[Declaration] argumentsBlocks, Statement body)
     | \methods(list[Attribute] attributes, list[Declaration] methods)
     | \arguments(list[Declaration] arguments)
     | \argument(Expression name, list[Expression] dimensions, Type \type, list[Expression] validators, Expression defaulValue)
     | \argument(Expression name, list[Expression] dimensions, list[Expression] validators, Expression defaultValue)
     | \argument(Expression name, list[Expression] dimensions, Type \type, list[Expression] validators)
     | \argument(Expression name, list[Expression] dimensions, list[Expression] validators)
     | \properties(list[Declaration] properties)
     | \property(Expression name, list[Expression] dimensions, Type \type, list[Expression] validators, Expression defaulValue)
     | \property(Expression name, list[Expression] dimensions, list[Expression] validators, Expression defaultValue)
     | \property(Expression name, list[Expression] dimensions, Type \type, list[Expression] validators)
     | \property(Expression name, list[Expression] dimensions, list[Expression] validators)
     | \events(list[Attribute] attributes, list[Declaration] events)
     | \event(Expression name)
     | \enumeration(list[Attribute] attributes, list[Declaration] enums)
     | \enumElem(Expression name, list[Expression] values)
     ;

data Statement
    = \assignment(Expression lhs, Expression rhs)
    | \break()
    | \continue()
    | \block(list[Statement] statement)
    | \command(Expression name, list[Expression] arguments)
    | \for(Expression index, Expression values)
    | \parFor(Expression index, Expression values, Expression options)
    | \while(Expression condition, Statement body)
    | \variable(VarOperator, Expression name)
    | \if(Expression condition, Statement thenBranch, list[Declaration] elseIf)
    | \if(Expression condition, Statement thenBranch, list[Declaration] elseIf, Statement elseBranch)
    | \elseIf(Expression condition, Statement thenBranch)
    | \spmd(list[Expression] params, Statement body)
    | \switch(Expression expression, list[Statement] cases)
    | \case(Expression expression, Statement body)
    | \otherwiseCase(Statement body)
    | \try(Statement body)
    | \try(Statement body, Statement catchClause)
    | \catch(Statement body)   
    | \return()
    ;

data Expression
    = \add(Expression lhs, Expression rhs)
    | \sub(Expression lhs, Expression rhs)
    | \mult(Expression lhs, Expression rhs)
    | \matMult(Expression lhs, Expression rhs)
    | \rightDiv(Expression lhs, Expression rhs)
    | \matRightDiv(Expression lhs, Expression rhs)
    | \leftDiv(Expression lhs, Expression rhs)
    | \matleftDiv(Expression lhs, Expression rhs)
    | \pow(Expression lhs, Expression rhs)
    | \matPow(Expression lhs, Expression rhs)
    | \transpose(Expression matrix)
    | \cTranspose(Expression matrix)
    | \eq(Expression lhs, Expression rhs)
    | \notEq(Expression lsh, Expression rhs)
    | \gt(Expression lhs, Expression rhs)
    | \gte(Expression lhs, Expression rhs)
    | \lt(Expression lhs, Expression rhs)
    | \lte(Expression lhs, Expression rhs)
    | \and(Expression lhs, Expression rhs)
    | \shortCircuitAnd(Expression lhs, Expression rhs)
    | \or(Expression lhs, Expression rhs)
    | \shortCircuitOr(Expression lhs, Expression rhs)
    | \not(Expression expression)
    | \metaClass(Expression object)
    | \handle(Expression expression)
    | \functionCallOrArrayAccess(Expression name, list[Expression] arguments)
    | \superFunctionCall(Expression name, Expression class, list[Expression] arguments)
    | \id(str string)
    | \lambda(list[Expression] params, Expression expression)
    | \matrix(list[Expression] rows)
    | \cellArray(list[Expression] rows)
    | \row(list[Expression] elements)
    | \number(str numberValue)
    | \string(str stringValue)
    | \parens(Expression expression)
    | \range(Expression startVal, Expression endVal)
    | \range(Expression startVal, Expression step, Expression endVal)
    | \fieldExpr(Expression object, list[Expression] fields)
    | \ignoreElem() 
    | \spreadOp()
    | \multiOutputVar(list[Expression] vars)
    | \validationFunctions(list[Expression] functions)
    ;

data Attribute
    = \attribute(Expression name, Expression attributeValue)
    ;

data VarOperator
    = \global()
    | \persistent()
    ;

data Type (TypeSymbol typ=unresolved())
    = qualifiedType(Expression name)
    | \int8()
    | \uint8()
    | \int16()
    | \uint16()
    | \int32()
    | \uint32()
    | \int64()
    | \uint64()
    | \single()
    | \double()
    | \logical()
    | \char()
    | \string()
    | \cell()
    | \struct()
    | \table()
    | \categorical()
    | \datetime()
    | \duration()
    ;

     