all: ExprLexer.class Formula.class Yytoken.class

test: all
	java Formula 2*3+4
	java Formula "2*(3+4)"

ExprLexer.class Formula.class Yytoken.class: ExprLexer.java Formula.java Yytoken.java
	javac ExprLexer.java Yytoken.java Formula.java

ExprLexer.java: formula.jflex
	java -jar jflex-1.6.0.jar formula.jflex

clean:
	rm -rf *.class *~ ExprLexer.java *.bak
