import java.io.*;

public class Formula
{
	static private ExprLexer lexer;
	static private Yytoken next_token;

	static private void advance()
	{
		try
		{
			next_token = lexer.yylex();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
		if( next_token == null )
		{
			next_token = new Yytoken(ExprLexer.EOF,"EOF");
		}
	}

	static private String over(int tok) {
		if( next_token.number != tok)
			expected(tok);
		String res = next_token.string;
		advance();
		return res;
	}

	static private String tokname(int tok) {
		switch(tok) {
			case ExprLexer.LITERAL:	return "literal";
			case ExprLexer.NAME:	return "name";
			case ExprLexer.IF:	return "if";
			case ExprLexer.ELSE:	return "else";
			case ExprLexer.ELSEIF:	return "elseif";
			case ExprLexer.WHILE:	return "literal";
			case ExprLexer.OPERATOR:	return "operation";
			case ExprLexer.VAR:	return "var";
			case ExprLexer.RETURN:	return "return";
			case ExprLexer.EOF:	return "EOF";
			case ExprLexer.ERR:	return "urecognized lexeme";
		}
		return "'"+(char)tok+"'";
	}

	static private void expected(int tok) {
		expected(tokname(tok));
	}

	static private void expected(String exp) {
		System.err.println("Expected "+exp+", found "+next_token.string+" in line "+lexer.getLine()+", column "+lexer.getColumn());
		System.exit(1);
	}

	static private boolean look(int tok) {
		return next_token.number == tok;
	}

	static private Object[] program() {
		ArrayList<Object> program = new ArrayList<Object>();
		program.add(function());
		while(look(ExprLexer.EOF)) program.add(function());
		return program.toArray(); 
	}

	static private Object[] function() {
		ArrayList<Object> function = new ArrayList<Object>();
		function.add(over(ExprLexer.NAME));
		over('(');
		ArrayList<String> args = new ArrayList<String>();
		if(look(ExprLexer.NAME)) {
			args.add(over(ExprLexer.NAME));
			while(look(',')) {
				advance();
				args.add(over(ExprLexer.NAME));
			}
		}
		funtion.add(args.size());
		args.clear();
		over(')');
		over('{');
		ArrayList<Object> varDecls = new ArrayList<Object>();
		while(look(ExprLexer.VAR)) {
			varDecls.add(decl());
			over(';');
		}
		function.add(varDecls.toArray());
		ArrayList<Object> expressions = new ArrayList<Object>();
		expressions.add(expr());
		over(';');
		while(!look('}')) {
			expressions.add(expr());
			over(';');
		}
		advance();
		return function.toArray();
	}

	static Object[] decl() {
		over(ExprLexer.VAR);
		ArrayList<Object> varNames = new ArrayList<Object>();
		varNames.add(over(ExprLexer.NAME));
		while(look(',')) {
			advance();
			varNames.add(over(ExprLexer.NAME));
		}
		return varNames.toArray();
	}

	static Object[] expr() {
		ArrayList<Object> expression = new ArrayList<Object>();
		expression.add(small_expr());
		while(look(ExprLexer.OPERATOR)) {
			expression.add(over(ExprLexer.OPERATOR));
			advance();
			expression.add(small_expr());
		}
		return expression.toArray();
	}

	static Object[] small_expr() {
		ArrayList<Object> smallExpr = new ArrayList<Object>();
		if(look(ExprLexer.NAME)) {
			smallExpr.add(ExprLexer.NAME);
			ArrayList<Object> nameExpression = new ArrayList<Object>();
			nameExpression.add(over(ExprLexer.NAME));
			if(look('=')) { //assign
				nameExpression.add(over('='));
				nameExpression.add(expr());
				smallExpr.add(nameExpression.toArray());
				return smallExpr.toArray();
			}
			if(!look('(')) {
				nameExpression.add("retrieve");
				smallExpr.add(nameExpression.toArray());
				return smallExpr.toArray(); //retrieve
			}
			advance();
			ArrayList<Object> args = new ArrayList<Object>(); //call
			nameExpression.add("call");
			if(look(')')) {
				advance();
				nameExpression.add(args.toArray());
				smallExpr.add(nameExpression.toArray());
				return smallExpr.toArray();
			}
			args.add(expr());
			while(look(',')) {
				advance();
				args.add(expr());
			}
			over(')');
			nameExpression.add(args.toArray());
			smallExpr.add(nameExpression.toArray());
			return smallExpr.toArray();
		}
		if(look(ExprLexer.RETURN)) {
			smallExpr.add(ExprLexer.RETURN);
			advance();
			smallExpr.add(expr());
			return smallExpr.toArray();
		}
		if(look(ExprLexer.OPERATOR)) {
			smallExpr.add(ExprLexer.OPERATOR);
			Object[] operatorStatement = new Object[2];
			operatorStatement[0] = over(ExprLexer.OPERATOR);
			operatorStatement[1] = small_expr();
			smallExpr.add(operatorStatement);
			return smallExpr.toArray();
		}
		if(look(ExprLexer.LITERAL)) {
			smallExpr.add(ExprLexer.LITERAL);
			smallExpr.add(over(ExprLexer.LITERAL));
			return smallExpr.toArray();
		}
		if(look('(')) {
			//smallExpr.add(ExprLexer.EXPRESSION);
			advance();
			Object[] temp = expr();
			//smallExpr.add(expr());
			over(')');
			return temp;
		}
		if(look(ExprLexer.IF)) {
			smallExpr.add(ExprLexer.IF);
			ArrayList<Objext> ifStatement = new ArrayList<Object>();
			advance();
			over('(');
			ifStatement.add(expr());
			over(')');
			ifStatement.add(body());
			ArrayList<Object> elseIfs = new ArrayList<Object>();
			while(look(ExprLexer.ELSEIF)) {
				Object[] temp = new Object[2];
				advance();
				over('(');
				temp[0] = expr();
				over(')');
				temp[1] = body();
				elseIfs.add(temp);
			}
			ifStatement.add(elseIfs.toArray());
			if(look(ExprLexer.ELSE)) {
				//smallExpr.add(ExprLexer.ELSE);
				advance();
				ifStatement.add(body());
				smallExpr.add(ifStatement.toArray());
				return smallExpr.toArray();
			}
			smallExpr.add(ifStatement.toArray());
			return smallExpr.toArray();
		}
		if(look(ExprLexer.WHILE)) {
			smallExpr.add(ExprLexer.WHILE);
			Object[] whileStatement = new Object[2];
			advance();
			over('(');
			whileStatement[0] = expr();
			over(')');
			whileStatement[1] = body();
			smallExpr.add(whileStatement);
			return smallExpr.toArray();
		}
		expected("expression");
	}
	static private Object[] body() {
		over('{');
		ArrayList<Object> body = new ArrayList<Object>();
		body.add(expr());
		over(';');
		while(!look('}')) {
			body.add(expr());
			over(';');
		}
		advance();
		return body.toArray();
	}

	// Notkun: generateProgram(p);
	// Fyrir:  p er fylki fallsskilgreininga,
	//         þ.e. fylki af milliþulum fyrir föll.
	// Eftir:  Búið er að skrifa lokaþulu fyrir forrit sem
	//         samanstendur af föllunum þ.a. name er nafn
	//         forritsins.
	static void generateProgram( String name, Object[] p )
	{
		emit("\""+name+".mexe\" = main in");
		emit("!{{");
		for( int i=0 ; i!=p.length ; i++ ) generateFunction((Object[])p[i]);
		emit("}}*BASIS;");
	}

	// Notkun: generateFunction(f);
	// Fyrir:  f er milliþula fyrir fall.
	// Eftir:  Búið er að skrifa lokaþulu fyrir fallið á
	//         aðalúttak.
	static void generateFunction( Object[] f )
	{
		// f = {fname,argcount,expr}
		String fname = (String)f[0];
		int count = (Integer)f[1];
		emit("#\""+fname+"[f"+count+"]\" =");
		emit("[");
		generateVarDecls((Object[])f[2])
		generateExpression((Object[])f[3]);
		emit("];");
	}

	static void generateVarDecls(Object[] d) {
			emit("(MakeVal null)")
		for(int i = 0; i < d.length; i++) {
			//generate empty variable for name
			//int pos = newLab();
			emit("(Push)");
		}
	}

	static void generateExpression(Object[] e) {
		generateSmallExpression((Object[])e[0])
		for(int i = 1; i < e.size; i+=2) {
			int operator = (int)e[i];
			//something with operator
			generateSmallExpression((Object[])[i+1]);
		}
	}

	static void generateSmallExpression(Object[] s) {
		switch(s[0]) {
			case ExprLexer.NAME: generateNameExpression(s[1]);
			case ExprLexer.RETURN: generateReturnExpression((Object[])s[1]);
			case ExprLexer.OPERATOR: generateOperatorExpression((Object[])s[1]);
			case ExprLexer.LITERAL: generateLiteralExpression((Object[])s[1]);
			case ExprLexer.IF: generateIfExpression((Object[])s[1]);
			case ExprLexer.WHILE: generateWhileExpression((Object[])s[1]);
			default: generateExpression(s);
		}
	}

	static void generateNameExpression(Object[] n) {
		//[NAME,'=',       expr]
		//[NAME,"retrieve"]
		//[NAME,"call",    expr[]]
		//[0,   1,         2     ]
		if((int)n[1] == '=') {

		}else if((String)n[1].equals("retrieve")) {
			emit("(Fetch "+n[0]+")");
		}else{
			Object[] args = (Object[])n[2];
			int i;
			if(args.length != 0) generateExpr((Object[])args[0]);
			for( i=1 ; i<args.length ; i++ ){
				emit("(Push)");
				generateExpr((Object[])args[i]);
			}

			emit("(Call #\""+n[0]+"[f"+i+"]\" "+i+")");
		}
	}

	static void generateReturnExpression(Object[] n) {
		//[expr]
		//[0   ]
		generateExpression(n[0]);
		emit("(Return)");
		
	}

	static void generateOperatorExpression(Object[] n) {
		//[OPERATOR, small_expr]
		//[0,        1         ]

	}

	static void generateLiteralExpression(Object[] n) {
		//[LITERAL]
		//[0      ]
		emit("(MakeVal "+n[0]+")");
	}

	static void generateIfExpression(Object[] n) {
		//[expr,body,elseIf[],else]
		//[0,   1,   2,       3   ]	
		int labElse = newLab();
		int labEnd = newLab();
		ArrayList<Integer> labElseIf = new ArrayList<Integer>();
		Object[] elseIfs = (Object[]) n[2];
		for(int i = 0; i < elseIfs.length; i++) {
			labElseIf.add(newLab());
		}
		int nextLab = labElseIf.size() > 0 ? labElseIf.get(0) : labElse;
		generateJump((Object[])n[0],0, nextLab);
		generateBody((Object[])n[1]);
		emit("(Go _"+labEnd+")");
		for(int i = 0; i < elseIfs.length; i++){
			Object[] elseIf = (Object[]) elseIfs[i];
			nextLab = i+1 < labElseIf.size() ? labElseIf.get(i+1) : labElse;

			emit("_"+labElseIf.get(i)+":");
			generateJump(elseIf[0],0,nextLab);
			generateBody(elseIf[1]);
			emit("(Go _"+labEnd+")");
			
		}
		emit("_"+labElse+":");
		generateBody(e[3]);
		emit("_"+labEnd+":");
	}

	static void generateWhileExpression(Object[] n) {
		//[expr,body]
		//[0,   1   ]
		int labStart = newLab();
		int labEnd = newLab();
		
		emit("_"+labStart+":");
		generateJump(n[0],0,labEnd);
		generateBody(n[1]);
		emit("(Go _"+labStart+")");

		emit("_"+labEnd+":");
	}

	static void generateBody(Object[] n) {
		//[expr[]]
		//[0     ]
		Object[] expressions = (Object[]) n[0];
		if(expressions.length != 0) generateExpression((Object[])n[0])
		for(int i = 1; i < expressions.length; i++) {
			emit("(Push)");
			generateExpression((Object[])n[i]);
		}

	}

	// Notkun: generateJump(e,labTrue,labTrue);
	// Fyrir:  e er milliþula fyrir segð, labTrue og
	//         labFalse eru heiltölur sem standa fyrir
	//         merki eða eru núll.
	// Eftir:  Búið er að skrifa lokaþulu fyrir segðina
	//         á aðalúttak.  Lokaþulan veldur stökki til
	//         merkisins labTrue ef segðina skilar sönnu,
	//         annars stökki til labFalse.  Ef annað merkið
	//         er núll þá er það jafngilt merki sem er rétt
	//         fyrir aftan þulu segðarinnar.
	static void generateJump( Object[] e, int labTrue, int labFalse )
	{
		switch( (CodeType)e[0] )
		{
		case LITERAL:
			String literal = (String)e[1];
			if( literal.equals("false") || literal.equals("null") )
			{
				if( labFalse!=0 ) emit("(Go _"+labFalse+")");
				return;
			}
			if( labTrue!=0 ) emit("(Go _"+labTrue+")");
			return;
		default:
			generateExpr(e);
			if( labTrue!=0 ) emit("(GoTrue _"+labTrue+")");
			if( labFalse!=0 ) emit("(GoFalse _"+labFalse+")");
		}
	}


	static int nextLab = 1;

	// Notkun: int i = newLab();
	// Eftir:  i er jákvæð heiltala sem ekki hefur áður
	//         verið skilað úr þessu falli.  Tilgangurinn
	//         er að búa til nýtt merki (label), sem er
	//         ekki það sama og neitt annað merki.
	static int newLab()
	{
		return nextLab++;
	}



/*
	static private void program() {
		while(next_token.number != ExprLexer.EOF) {			
			fundecl();
			vardecl();
			expr();
		}
	}

	static private void fundecl() {
		if(next_token.string.equals("fun")) {
			advance();
			if(next_token.number != ExprLexer.NAME) {
				throw new Error("Expected 'NAME', found "+next_token);
			}
			advance();
			if(next_token.number != (int)'(') {
				throw new Error("Expected '(', found "+next_token);
			}
			advance();
			while(next_token.number != (int)')') {
				if(next_token.number == (int)',') advance();
				if(next_token.number != ExprLexer.NAME) {
					throw new Error("Expected 'NAME', found "+next_token);
				}
				advance();
			}
			advance();
			body();
			if(next_token.number != ExprLexer.LINE_TERMINATOR) throw new Error("Expected ';', found "+next_token);
			advance();
		}
	}

	static private void vardecl() {
		if(next_token.string.equals("var")) {
			advance();
			while(next_token.number != ExprLexer.LINE_TERMINATOR) {
				if(next_token.number != ExprLexer.NAME) {
					throw new Error("Expected 'NAME', found "+next_token);
				}
				advance();
				if(next_token.number == (int)'=') {
					advance();
					if(!expr()) throw new Error("'=' without expression "+next_token);
				}
			}
			advance();
		}
	}

	static private boolean expr() {
		if(next_token.string.equals("return")) {
			advance();
			expr();
		} else if(next_token.number == ExprLexer.NAME) {
			advance();
			if(next_token.number != (int)'=') throw new Error("Expected '=', found "+next_token);
		} 
		if(!orexpr()) return false;
		if(next_token.number != ExprLexer.LINE_TERMINATOR) throw new Error("Expected ';', found "+next_token);
		advance();
	}

	static private boolean orexpr() {
		while(andexpr()) {
			 if(next_token.string.equals("||")) advance();
		}
		return true;
	}

	static private boolean andexpr() {
		while(notexpr()) {
			 if(next_token.string.equals("&&")) advance();
		}
		return true;
	}

	static private boolean notexpr() {
		if(next_token.number == (int)'!') {
			advance();
			notexpr();
		}else {
			opexpr();
		}
		return true;
	}

	static private void opexpr() {
		smallexpr();
		if(next_token.number == (int) ExprLexer.OPERATOR) {
			advance();
			opexpr();
		}
	}

	static private boolean smallexpr() {
		if(next_token.number == (int) ExprLexer.NAME) {
			advance();
			if(next_token.number == (int) '(') {
				advance();
				while(next_token.number != (int)')') {
					advance();
					if(next_token.number == (int)',') advance();
					expr();
				}
			}
		}else if(next_token.number == ExprLexer.OPERATOR) {
			advance();
			if(!expr()) throw new Error("Expected expression, found "+next_token);
		}else if(next_token.number == ExprLexer.LITERAL) {
			advance();
		}else if(next_token.number == (int) '(') {
			advance();
			if(!expr()) throw new Error("Expected expression, found "+next_token);
			if(next_token.number != (int) ')') {
				throw new Error("Expected ')', found "+next_token);
			}
			advance();
		}else if(next_token.string.equals("while")) {
			advance();
			if(next_token.number != (int) '(') {
				throw new Error("Expected '(', found "+next_token);
			}
			advance();
			if(next_token.number != (int) ')') {
				throw new Error("Expected ')', found "+next_token);
			}
			advance();
			body();
		}else if(next_token.string.equals("fun")) {
			advance();
			if(next_token.number != (int)'(') {
				throw new Error("Expected '(', found "+next_token);
			}
			advance();
			while(next_token.number != (int)')') {
				advance();
				if(next_token.number == (int)',') advance();
				if(next_token.number != ExprLexer.NAME) {
					throw new Error("Expected 'NAME', found "+next_token);
				}
			}
			advance();
			body();
		}else {
			ifexpr();
		}
		return true;
	}

    static private void ifexpr() {
    	if(next_token.string.equals("if")) {
    		advance();
    		if(next_token.number != (int)"("){
                throw new Error("Expected '(', found "+next_token)
            }
            advance();
            if (!expr()) {
            	throw new Error("Expected '(', found "+next_token)	
            }
            if(next_token.number != (int)")"){
                throw new Error("Expected ')', found "+next_token)
            }
            advance();
            body();
            while(next_token.string.equals("elseif")) {
            	advance();
            	if(next_token.number != (int)"("){
	                throw new Error("Expected '(', found "+next_token)
	            }
	            advance();
	            if (!expr()) {
	            	throw new Error("Expected '(', found "+next_token)	
	            }
	            if(next_token.number != (int)")"){
	                throw new Error("Expected ')', found "+next_token)
	            }
	            advance();
	            body();
            }

            if(next_token.string.equals("else")) {
            	advance();
            	body();
            }

    	}
    }
 
 
    }
 
    static private void body() {
        if(next.token.number == (int)"{"){
            advance();
            while(next.token.number != (int)"}"){
                fundecl();
                vardecl();
                expr();
            }
        }
        advance();
    }


	
	static private double e()
	{
		double x = t();
		x = ep(x);
		return x;
	}
	
	static private double ep( double x )
	{
		if( next_token.number == (int)'+' )
		{
			advance();
			x = x+t();
			return ep(x);
		}
		else if( next_token.number == (int)'-' )
		{
			advance();
			x = x-t();
			return ep(x);
		}
		else
		{
			return x;
		}
	}

	static private double t()
	{
		double x = f();
		x = tp(x);
		return x;
	}
	
	static private double tp( double x )
	{
		if( next_token.number == (int)'*' )
		{
			advance();
			x = x*f();
			return tp(x);
		}
		else if( next_token.number == (int)'/' )
		{
			advance();
			x = x/f();
			return tp(x);
		}
		else
		{
			return x;
		}
	}

	static private double f()
	{
		if( next_token.number == (int)'(' )
		{
			advance();
			double x = e();
			if( next_token.number != (int)')' )
			{
				throw new Error("Expected ')', found "+next_token);
			}
			advance();
			return x;
		}
		else if( next_token.number == ExprLexer.NUM )
		{
			double x = Double.parseDouble(next_token.string);
			advance();
			return x;
		}
		else
		{
			throw new Error("Expected factor, found "+next_token);
		}
	}*/

	static public void main( String args[] )
	{
		//lexer = new ExprLexer(new StringReader(args[0]));
		try {
			lexer = new ExprLexer(new FileReader(args[0]));
			advance();
		}
		catch(FileNotFoundException e) {
			System.out.println("no file");
		}

		Object midOracle = program();
		over(ExprLexer.EOF);
		generateProgram((Object[])midOracle);


		/*
		while( next_token.number != ExprLexer.EOF) {
			System.out.println(next_token.number+": "+next_token);
			advance();
		}*/
		/*
		double x = e();
		if( next_token.number != ExprLexer.EOF )
		{
			throw new Error("Expected EOF, found "+next_token);
		}
		System.out.println(x);*/
	}
}
