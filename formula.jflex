/**
	JFlex lesgreinir fyrir einfaldar formúlur.
	Höfundur: Snorri Agnarsson
 */

%%

%public
%class ExprLexer
%line
%column

%unicode

%{

public int getLine() { return yyline; }
public int getColumn() { return yycolumn; }	

}%

%{

private String lexeme;
public String getLexeme() { return lexeme; }
static public final int ERR = -1;
static public final int NUM = 1;

static public final int EOF = 0;
static public final int LITERAL = -2;
static public final int OPERATOR = -3;
static public final int IF = -4;
static public final int ELSEIF = -5;
static public final int ELSE = -6;
static public final int WHILE = -7;
static public final int VAR = -8;
static public final int RETURN = -9;
static public final int NAME = -10;
static public final int WHILE = -11;
static public final int EXPRESSION = -12;



%}

  /* Reglulegar skilgreiningar */
/*
DIGIT=[0-9]
FLOAT={DIGIT}+(\.{DIGIT}+([eE][+-]?{DIGIT}+)?)?
OPERATOR=[+|\-|*|/|<|>|\&|!|\|]+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_NAME=([:letter:]|[\+\-*/!%&=><\:\^\~&|?]|{DIGIT})+
_SINGLESYMBOL=[{};\[\]\(\)\.]
_BOOLEAN=null|true|false;
_IGNORED=[ |\r|\n|\t]+
*/

DIGIT=[0-9]
FLOAT={DIGIT}+(\.{DIGIT}+([eE][+-]?{DIGIT}+)?)?
STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
LETTER = [a-zA-ZþæöðÞÆÖÐáéíúýÁÉÍÚÝ]
INT = {DIGIT}+
OPCHAR = [+\-*/!%&|=<>:\^]
[DELIM] = [(),;{}]




%%

{FLOAT}|{INT}|{CHAR}|{STRING} {
	return new Yytoken(LITERAL, yytext());
}

{OPCHAR}+ {
	return new Yytoken(OPERATOR, yytext());
}

"if" {
	return new Yytoken(IF, yytext());
}

"elseif" {
	return new Yytoken(ELSEIF, yytext());
}

"else" {
	return new Yytoken(ELSE, yytext());
}

"while" {
	return new Yytoken(WHILE, yytext());
}

"var" {
	return new Yytoken(VAR, yytext());
}

"return" {
	return new Yytoken(RETURN, yytext());
}

{LETTER} ({LETTER}|{DIGIT})* {
	return new Yytoken(NAME, yytext());
}

{DELIM} {
	return new Yytoken(yycharat(0), yytext());
}

[\t\r\n] {
	
}

";;;".*{
	
}

. {
	return new Yytoken(ERR);
}


/*
{_CHAR} | {_STRING} | {_BOOLEAN} {
	return new Yytoken(LITERAL,yytext());
}

{OPERATOR} {
	return new Yytoken(OPERATOR,yytext());
}

"var" {
	return new Yytoken(DECL, yytext());
}

{_NAME} {
	return new Yytoken(NAME,yytext());
}

{_SINGLESYMBOL} {
	return new Yytoken(yycharat(0), yytext());
}


{_IGNORED} {
	
}*/

