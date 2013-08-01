// --------------------------------------------------------------------------
// Copyright (c) 2012 Henry Strickland & Thomas Shanks
// 
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// --------------------------------------------------------------------------
package terse.vm;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import terse.vm.Expr.Block;
import terse.vm.Expr.Lit;
import terse.vm.Expr.Send;
import terse.vm.Ur.Obj;

// BUG: (x-2,y-2) parses very wrong.  "-2" gets detected, and "." gets inserted.

public class Parser extends Obj {
	// =cls "meth" Parser Obj
	
	Cls onCls;
	Terp terp;
	TLex lex;
	HashMap<String, Integer> localVars; // maps to Local Var index.
	HashMap<String, String> localVarSpelling;
	HashMap<String, Integer> instVars;
	int temp = 0;

	public static boolean isAlphaMessage(String msg) {
		char m0 = msg.charAt(0);
		return Character.isLetter(m0);
	}

	// This is the public function to parse a string.
	public static Expr.MethTop parseMethod(Cls onCls, String methName, String code) {
		Terp terp = onCls.terp;
		Parser p = new Parser(onCls, methName, code);
		int numArgs = 0;
		if (isAlphaMessage(methName)) {
			for (int i = 0; i < methName.length(); i++) {
				if (methName.charAt(i) == ':') {
					String paramName = Character
							.toString((char) ((int) 'a' + numArgs));
					p.localVars.put(paramName, numArgs);
					p.localVarSpelling.put(paramName, paramName);
					++numArgs;
				}
			}
		} else {
			p.localVars.put("a", 0);
			p.localVarSpelling.put("a", "a");
			numArgs = 1;
		}
		try {
			p.parseExpr(); // Once to learn names of variables.
		} catch (Exception ex) {
			ex.printStackTrace();
			terp.toss("ERROR DURING PARSING: <%s>\nWHILE PARSING THIS SOURCE [len=%d]:\n`%s`\nUNPARSED REMAINDER [len=%d]:\n`%s`\n",
					show(ex), p.lex.front.length(), p.lex.front, p.lex.rest.length(), p.lex.rest);
		}
		if (p.lex.w.trim().length() > 0) {
			terp.toss("Parser:  Leftover word after parsing: <%s>",
					p.lex.w.trim());
		}
		if (p.lex.rest.trim().length() > 0) {
			terp.toss("Parser:  Leftover junk after parsing: <%s>",
					p.lex.rest.trim());
		}
		p.lex = new TLex(terp, code); // Ugly.
		Expr expr = p.parseExpr(); // Again for code generation.
		Expr.MethTop top = new Expr.MethTop(p.localVars.size(), numArgs, p.localVars,
				p.localVarSpelling, onCls, methName, expr, code);
		return top;
	}

	// These are keyboard character substitutions.
	static String[] TRIGRAPH_DATA = new String[] { "eq", "==", "ne", "!=",
			"lt", "<", "le", "<=", "gt", ">", "ge", ">=", "pl", "+", "ti", "*",
			"pc", "%", "an", "&", "or", "|", "xo", "^", "sl", "<<", "sr", ">>",
			"lr", ">>>", "co", ":", "as", "=", "cm", ",", "sc", ";", "dr", "$" };
	static HashMap<String, String> TRIGRAPH_MAP = new HashMap<String, String>();
	static {
		for (int i = 0; i < TRIGRAPH_DATA.length; i += 2) {
			TRIGRAPH_MAP.put(TRIGRAPH_DATA[i], TRIGRAPH_DATA[i + 1]);
		}
	}
	static Pattern TRIGRAPH = Pattern.compile("/[a-z][a-z]",
			Pattern.CASE_INSENSITIVE);

	static public String noGraphSubst(String s) {
		return s; // Disable them, until we fix them.
//		return bigraphSubst(trigraphSubst(s));
	}

	static public String trigraphSubst(String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (true) {
			Matcher m = TRIGRAPH.matcher(s);
			boolean found = m.find(i);
			if (!found) {
				sb.append(s.substring(i));
				return sb.toString();
			}
			int j = m.start();
			sb.append(s.substring(i, j));
			String abbrev = TRIGRAPH_MAP.get(s.substring(j + 1, j + 3));
			if (abbrev == null) {
				sb.append(s.substring(j, j + 3));
			} else {
				sb.append(abbrev);
			}
			i = j + 3;
		}
	}

	static Pattern COLON = Pattern.compile("\\.\\.");
	static Pattern DOLLAR = Pattern.compile(",,");
	static Pattern EQUALS = Pattern.compile("--");
	static Pattern SEMICOLON = Pattern.compile("\\.,|,\\.");
	static Pattern BANG = Pattern.compile("!");
	static Pattern HUNH = Pattern.compile("\\?");
	static Pattern BANG_EQ = Pattern.compile("\\(=");

	static public String bigraphSubst(String s) {
		String z = replace(COLON, ":", s);
		z = replace(DOLLAR, "\\$", z);
		z = replace(EQUALS, "=", z);
		z = replace(SEMICOLON, ";", z);
		z = replace(BANG, "(", z);
		z = replace(HUNH, ")", z);
		z = replace(BANG_EQ, "!=", z);
		return z;
	}

	public Parser(Cls onCls, String methName, String code) {
		super(onCls.terp.wrap.clsParser);
		this.onCls = onCls;
		this.terp = onCls.terp;
		this.lex = new TLex(terp, code);
		this.localVars = new HashMap<String, Integer>();
		this.localVarSpelling = new HashMap<String, String>();
		this.instVars = new HashMap<String, Integer>();
		// Include instVars from superclasses, like Python does.
		Cls c = onCls;
		while (c != null) {
			//say("PARSER ON %s at C=%s myVarNames %s", onCls, c, Static.arrayToString(c.myVarNames));
			for (String k : c.myVarNames) {
				Integer i = onCls.allVarMap.get(k);
				if (i == null) {
					terp.toss("allVarMap.get(%s) is NULL: onCls=%s cls=%s allVarMap=%s", onCls, c, Static.arrayToString(c.myVarNames));
				}
				//say("PARSER PUTTING VAR %s", k);
				this.instVars.put(k.toLowerCase(), i);
			}

			c = c.supercls;
		}
		//say("PARSER DONE %s", Static.show(this.instVars));
	}

	private static String replace(Pattern p, String replacement, String a) {
		Matcher m = p.matcher(a);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private boolean skipStmtEndersReturningTrueIfMissing() { // i.e. dots & semicolons
		boolean missing = true;
		while (lex.endsStmt()) {
			lex.advance();
			missing = false;
		}
		return missing;
	}

	private Expr parseExpr() {
		String front = lex.front; String white = lex.white;
		Expr[] v = emptyExprs;
		skipStmtEndersReturningTrueIfMissing();
		while (lex.t != null) {
			if (lex.closesParen())
				break;

			Expr s = parseStmt();
			v = append(v, s);
			if (skipStmtEndersReturningTrueIfMissing())
				break;
		}
		if (v.length == 0) {
			Expr z = new Expr.EmptyExprList(terp); // Special case.
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		} else if (v.length == 1) {
			return v[0]; // Trivial case.
		} else {
			Expr z = new Expr.Seq(terp, v);
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		}
	}

	private Expr parseStmt() {
		String front = lex.front; String white = lex.white;
		lex.storeState();
		Expr.LValue lvalue = parseLValueOrNull();
		if (lvalue != null && lex.w.equals("=")) {
			lvalue.fixIndices(this);
			lex.advance();
			Expr rhs = parseStmt();
			Expr z = new Expr.PutLValue(lvalue, rhs);
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		}
		lex.recallState();
		return parseList();
	}

	// When a variable is assigned, it must be either Inst or Local.
	// If it is in the class's allVars, then it is Inst.
	// Otherwise if we don't have it as a local yet, add it.
	// Returns -1 if inst var, or Local Var index if local.
	int indexForLocalVariable(String varName, String varKey) {
		// Determine Inst or Local. Globals are never assigned.
		if (this.instVars.containsKey(varKey)) {
			return -1;
		}
		int index;
		if (localVars.containsKey(varKey)) {
			index = localVars.get(varKey);
		} else {
			// Does not exist yet, so make it.
			index = localVars.size();
			localVars.put(varKey, index);
			localVarSpelling.put(varKey, varName);
		}
		return index;
	}

	private Expr parseList() {
		String front = lex.front; String white = lex.white;
		boolean itsAList = false;
		Expr[] v = exprs(parseTuple());
		while (lex.isLister()) {
			itsAList = true;
			lex.advance();
			if (lex.endsStmt() || lex.closesParen()
					|| lex.isEOF()) {
				break;
			}
			v = append(v, parseTuple());
		}
		if (itsAList) {
			Expr z = new Expr.MakeVec(terp, v, ';');
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		} else {
			// It's not a list at all.
			return v[0];
		}
	}

	private Expr parseTuple() {
		String front = lex.front; String white = lex.white;
		boolean itsATuple = false;
		Expr[] v = exprs(parseChain());
		while (lex.isTupler()) {
			itsATuple = true;
			lex.advance();
			if (lex.isLister() || lex.endsStmt()
					|| lex.closesParen() || lex.isEOF()) {
				break;
			}
			v = append(v, parseChain());
		}
		if (itsATuple) {
			Expr z = new Expr.MakeVec(terp, v, ',');
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		} else {
			// It's not a tuple at all.
			return v[0];
		}
	}

	private Expr parseChain() {
		// Normal SmallTalk "cascades" using ";", but we don't.
		// Instead we "chain" with ",," or '�'.
		// Whereas cascade sends subsequent messages to the same receiver,
		// this chaining sends subsequent messages to the previous result.
		String front = lex.front; String white = lex.white;
		Expr unary = parseUnary();
		Expr expr = parseKeyed(unary);
		while (lex.isChain()) {
			lex.advance();
			while (lex.t == Pat.NAME && !lex.isKeyword()) { 
				// parse Unary messages at front of chain.
				expr = new Expr.Send(expr, lex.w.toLowerCase(), emptyExprs, ints(lex.frontLocation()));
				lex.advance();
				expr.front = lex.rest;
				expr.rest = lex.rest;
			}
			if (lex.isKeyword() || lex.isTupler() || lex.isBinop()) {
				expr = parseKeyed(expr);
			}
		}
		return expr;
	}

	private Expr parseKeyed(Expr receiver) {
		// cap is obsolete now.
		receiver = parseBinary3(receiver);
		if (lex.isKeyword()) {
			while (lex.isKeyword()) {
				String front = lex.front; String white = lex.white;
				String keywords = "";
				Expr[] args = emptyExprs;
				int[] locs = emptyInts;
				while (lex.isKeyword()) {
					keywords += lex.keywordName().toLowerCase() + ":";
					locs = append(locs, lex.frontLocation());
					lex.advance();
					assert lex.w.charAt(0) == ':';
					lex.advance();
					Expr unary = parseUnary();
					Expr tuple = parseBinary3(unary);
					// WAS lesser strength // Expr arg = parseKeyed(tuple);
					args = append(args, tuple);
				}
				receiver = new Expr.Send(receiver, keywords, args, locs);
				receiver.front = front; receiver.white = white;
				receiver.rest = lex.rest;
			}
			return receiver;
		} else {
			return receiver; // No keyword with strength less than cap.
		}
	}

	private Expr parseBinary3(Expr receiver) {
		receiver = parseBinary25(receiver);
		Expr e = receiver;
		while (lex.isBinop3()) {
			String front = lex.front; String white = lex.white;
			String op = lex.w;
			int[] locs = ints(lex.frontLocation());
			lex.advance();
			Expr unary = parseUnary();
			Expr a = parseBinary25(unary);
			e = new Expr.Send(e, op, exprs(a), locs);
			e.front = front; e.white = white;
			e.rest = lex.rest;
		}
		return e;
	}
	private Expr parseBinary25(Expr receiver) {
		receiver = parseBinary2(receiver);
		Expr[] v = null;  // Be lazy.
		int[] locs = null;
		String front = lex.front; String white = lex.white;
		while (lex.w.equals("@")) {
			if (v == null) {  // Catch up for laziness.
				v = exprs(receiver);
				locs = ints();
			}
			locs = append(locs, lex.frontLocation());
			lex.advance();
			Expr unary = parseUnary();
			Expr a = parseBinary2(unary);
			v = append(v, a);
		}
		if (v != null) {
			Expr z = new Expr.MakeVec(terp, v, '@');
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		} else {
			// It's not a tuple at all.
			return receiver;
		}
	}
	private Expr parseBinary2(Expr receiver) {
		receiver = parseBinary1(receiver);
		Expr e = receiver;
		String prevRest = receiver.rest;
		while (lex.isBinop2() || lex.t == Pat.NUMBER && lex.w.charAt(0) == '-') {
			if (lex.t == Pat.NUMBER && prevRest.length() > 0) {
				// Handle special case like "x-2" where "-2" gets parsed as NUMBER.
//				try {
					String front = lex.front;
					String op = "-";
					int[] locs = ints(lex.frontLocation());
					Expr a = new Expr.Lit(new Ur.Num(terp,
							Double.parseDouble(lex.w.substring(1))));
					a.front = front + "-";
					a.white = e.white;
					a.rest = lex.rest;
					e = new Expr.Send(e, op, exprs(a), locs);
					e.front = front;
					e.white = "";
					e.rest = prevRest.substring(1);
//				} catch (RuntimeException ex) {
//					ex.printStackTrace();
//					throw ex;
//				}
			} else {
				String front = lex.front; String white = lex.white;
				String op = lex.w;
				int[] locs = ints(lex.frontLocation());
				lex.advance();
				Expr unary = parseUnary();
				Expr a = parseBinary1(unary);
				e = new Expr.Send(e, op, exprs(a), locs);
				e.front = front; e.white = white;
				e.rest = lex.rest;
			}
			prevRest = e.rest;
		}
		return e;
	}

	private Expr parseBinary1(Expr receiver) {
		Expr e = receiver;
		while (lex.isBinop1()) {
			String front = lex.front; String white = lex.white;
			String op = lex.w;
			int[] locs = ints(lex.frontLocation());
			lex.advance();
			Expr a = parseUnary();
			e = new Expr.Send(e, op, exprs(a), locs);
			e.front = front; e.white = white;
			e.rest = lex.rest;
		}
		return e;
	}

	private Expr parseUnary() {
		Expr e = parsePrim();
		while (lex.t == Pat.NAME && !lex.isKeyword() && !lex.isMacro()) {
			String front = lex.front; String white = lex.white;
			int[] locs = ints(lex.frontLocation());
			e = new Expr.Send(e, lex.w.toLowerCase(), emptyExprs, locs);
			lex.advance();
			e.front = front; e.white = white;
			e.rest = lex.rest;
		}
		return e;
	}
	
	private Expr interpolateLiteralString(String a) {
		try {
		final int n = a.length();
		Expr[] list = exprs();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			// say("... i=%d  list=%s  <#>%s<#>%s<#>", i, arrayToString(list), sb.toString(), a.substring(i));
			if (i+2 < n && a.charAt(i) == '[' && a.charAt(i+1) == '[') {
				// Double (( becomes literal single (
				sb.append('[');
				++i;  // Skip the extra (
				continue;
			} else if (i+2 < n && a.charAt(i) == '[' && a.charAt(i+1) != '[') {
				// We spotted single (, look for closing )
				Expr found = null;
				int nested = 0;
				int j = i + 1;
				for ( ; j < n; j++) {
					if (a.charAt(j) == ']' && nested == 0) {
						// We found a valid closing ).
						// CHEAT for now.
						Expr self = new Expr.GetSelf(terp);
						Expr body = new Expr.Lit(new Str(terp, a.substring(i, j)));
						Expr[] args = new Expr[]{ body };
						// TODO: correct location?
						found = new Expr.Send(self, "eval:", args, ints(lex.frontLocation()));
						break;
					} else if (a.charAt(j) == ']') {
						--nested;
					} else if (a.charAt(j) == '[') {
						++nested;
					} else {
						// Just continue
					}
				}
				// breaks to here.
				if (found != null) {
					if (sb.length() > 0) {
						Expr pending = new Expr.Lit(new Str(terp, sb.toString()));
						sb = new StringBuilder();
						list = append(list, pending);
					}
					list = append(list, found);
					i = j;
					continue;
				} else {
					// All the rest.
					sb.append(a.substring(i));
					break;
				}
			} else if (i+1 < n && a.charAt(i) == ']' && a.charAt(i+1) == ']') {
				sb.append(']');
				i++;
			} else {
				sb.append(a.charAt(i));
			}
		}
		
		// We finished scanning; build the final Expr.
		if (sb.length() > 0) {
			Expr pending = new Expr.Lit(new Str(terp, sb.toString()));
			list = append(list, pending);
		}
		
		final int ll = list.length;
		Expr z;
		if (ll == 0) {
			z = new Expr.Lit(new Str(terp, ""));
		} else if (ll == 1) {
			z = list[0];
		} else {
			Expr mkVec = new Expr.MakeVec(terp, list, ';');
			z = new Expr.Send(mkVec, "jam", terp.emptyExprs, ints(lex.frontLocation()));
		}
		// say("Interpolate <<< %s", a);
		// say("Interpolate >>> %s", z);
		return z;
		} catch (RuntimeException ex) {
			say("Exception in Interpolate: %s", show(ex));
			throw ex;
		}
	}
 
	private Expr parsePrim() {
		String front = lex.front; String white = lex.white;
		Expr z = null;
		boolean shouldAdvanceAtEnd = true;
		switch (lex.t) {
		case NUMBER:
			z = new Expr.Lit(new Ur.Num(terp, Double.parseDouble(lex.w)));
			break;
		case STRING:
			z = new Expr.Lit(new Ur.Str(terp, lex.w));
			// TRY INTERPOLATION ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ
			z = interpolateLiteralString(lex.w);
			break;
		case OTHER:
			z = lex.opensParen() ? parseParen() : null;
			break;
		case NAME:
			if (lex.isMacro()) {
				z = parseMacro();
				shouldAdvanceAtEnd = false;
			} else {
				z = getVar(lex.w, lex.w.toLowerCase());
			}
			break;
		}
		if (z == null) {
			terp.toss("Syntax Error in parsePrim: <%s>", lex.w);
		}
		// NOTA BENE: All of the above cases do not advance. We do it here.
		// Macros are the exception.
		if (shouldAdvanceAtEnd)
			lex.advance();
		z.front = front; z.white = white;
		z.rest = lex.rest;
		return z;
	}

	private Expr getVar(String varName, String varKey) {
		if (varKey.equals("nil"))
			return new Expr.Lit(terp.instNil);
		else if (varKey.equals("me"))
			return new Expr.GetSelf(terp);
		else if (varKey.equals("up"))
			return new Expr.Lit(terp.instSuper);
//		else if (varKey.equals("sp"))
//			return new Expr.Lit(terp.instSpace);
//		else if (varKey.equals("nl"))
//			return new Expr.Lit(terp.instNewline);
//		else if (varKey.equals("self"))
//			return (Expr) terp.toss("STILL USINGG OLD NAME: " + varKey);
//		else if (varKey.equals("se"))
//			return (Expr) terp.toss("STILL USINGG OLD NAME: " + varKey);
//		else if (varKey.equals("super"))
//			return (Expr) terp.toss("STILL USINGG OLD NAME: " + varKey);
//		else if (varKey.equals("su"))
//			return (Expr) terp.toss("STILL USINGG OLD NAME: " + varKey);
		else {
			// say("GETKEY <%s> <%s> %d", varName, varKey, varKey.length());
			;;;
			// Determine Inst or Local. Globals are never assigned.
			// TODO: check only this class's vars, not allVars.
			if (this.instVars.containsKey(varKey)) {
				return new Expr.GetInstVar(terp, varName,
						this.instVars.get(varKey));
			}
			int index;
			if (localVars.containsKey(varKey)) {
				index = localVars.get(varKey);
				return new Expr.GetLocalVar(terp, varName, index);
			} else {
				return new Expr.GetGlobalVar(terp, varKey);
			}
		}
	}

	private Expr parseBlock() {
		assert lex.opensParen();
		lex.advance();
		Expr[] params = exprs();

		while (true) {
			lex.storeState();
			Expr.LValue lvalue = parseLValueOrNull();
			if (lvalue != null && lex.w.equals(":")) {
				lvalue.fixIndices(this);
				params = append(params, lvalue);
				lex.advance();
			} else {
				lex.recallState();
				break;
			}
		}
		Expr body = parseExpr();
		assert lex.closesParen() : fmt(
				"lex=%s<%s> body=<%s>", lex.t, lex.w, body);// Do not advance;
															// parsePrim() does
															// that after
															// switch().
		return new Expr.Block(body, params);
	}

	private Expr parseParen() {
		assert lex.opensParen();
		lex.advance();
		if (lex.closesParen()) {
			// The unit tuple (). It's an empty but mutable vector, not a
			// literal.
			return new Expr.MakeVec(terp, emptyExprs, ',');
		}
		Expr body = parseExpr();
		assert lex.closesParen();
		// Do not advance; parsePrim() does that after switch().
		return body;
	}

	private Expr parseMacro() {
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Expr.Block> blocks = new ArrayList<Expr.Block>();
		int[] locs = ints(-1);  // "macro:" is implicit, thus -1.
		while (lex.t == Pat.NAME && lex.isMacro()) {
			names.add(lex.w);
			locs = append(locs, lex.frontLocation());
			lex.advance();
			assert lex.opensParen();
			Expr.Block b = (Expr.Block) parseBlock();
			blocks.add(b);
			assert lex.closesParen();
			lex.advance();
		}
		final int sz = names.size();
		StringBuilder message = new StringBuilder("macro:");
		Expr[] bb = exprs(new Expr.GetFrame(terp));
		for (int i = 0; i < sz; i++) {
			message.append(names.get(i).toLowerCase());
			message.append(':');
			bb = append(bb, blocks.get(i));
		}
		return new Expr.Send(new Expr.GetSelf(terp), message.toString(), bb, locs);
	}

	private Expr.LValue parseLValueOrNull() {
		String front = lex.front; String white = lex.white;
		Expr.LValue a = parseLvNameOrNull();
		if (a == null)
			return null;
		if (lex.w.equals(",")) {
			a = parseLvTupleOrNull(a);
			if (a == null)
				return null;
		}
		if (lex.w.equals(";")) {
			a = parseLvListOrNull(a);
		}
		a.front = front; a.white = white;
		a.rest = lex.rest;
		return a;
	}

	private Expr.LvName parseLvNameOrNull() {
		if (lex.t == Pat.NAME) {
			String front = lex.front; String white = lex.white;
			// local or inst?
			boolean isInstVar = this.instVars.containsKey(lex.w.toLowerCase());
			Expr.LvName z;
			if (isInstVar) {
				z = new Expr.LvInstName(terp, lex.w);
			} else {
				z = new Expr.LvLocalName(terp, lex.w);
			}
			lex.advance();
			z.front = front; z.white = white;
			z.rest = lex.rest;
			return z;
		}
		return null;
	}

	private Expr.LValue parseLvTupleOrNull(Expr a) {
		Expr.LvTuple z = new Expr.LvTuple(terp, urs(a));
		while (lex.w.equals(",")) {
			lex.advance();
			Expr.LvName x = parseLvNameOrNull();
			if (x == null)
				return null;
			z.arr = append(z.arr, x);
		}
		return z;
	}

	private Expr.LValue parseLvListOrNull(Expr a) {
		Expr.LvList z = new Expr.LvList(terp, urs(a));
		while (lex.w.equals(";")) {
			lex.advance();
			Expr.LValue x = parseLvNameOrNull();
			if (lex.w.equals(";")) {
				x = parseLvTupleOrNull(x);
				if (a == null)
					return null;
			}
			if (x == null)
				return null;
			z.arr = append(z.arr, x);
		}
		return z;
	}

	enum Pat {
		WHITE("\\s\\s*"), COMMENT("\"[^\"]*\""), STRING("'[^']*'"), NUMBER(
				"-?[0-9][0-9]*(\\.[0-9][0-9]*)?([Ee][+-]?[0-9][0-9]*)?"), NAME(
				"[A-Za-z][A-Za-z0-9_]*"), OTHER(
				"/[A-Za-z][A-Za-z]|\\.\\.|\\.,|,\\.|,,|--|==|!=|<=|>=|:+|.");

		Pattern p;

		Pat(String s) {
			this.p = Pattern.compile(s, 0);
		}

		Pat(String s, int flags) {
			this.p = Pattern.compile(s, flags);
		}

		Matcher matcher(String x) {
			// say("Attempting %s pattern %s ON %s", this, p, x);
			return p.matcher(x);
		}
	}

	static final class TLex extends Static {
		Terp terp;

		// The parser will use these fields:
		public String white; // preceeding white space & comments.
		public String w; // current word
		public Pat t; // type that matched
		public Matcher m;

		// These will be used for finding errors.
		String prog; // program to parse
		String front; // at front of last token.
		String rest; // Remaining substring of p to parse
		TLex storage = null;

		TLex(Terp t, String prog) {
			this.terp = t;
			this.prog = prog;
			this.white = prog;
			this.w = "";
			this.t = null;
			this.m = null;
			this.front = prog;
			this.rest = prog;
			advance();
			if (prog.length() > 0) {
				// prog length stops infinite recursion.
				storage = new TLex(terp, "");
			}
		}

		// Copy constructor.
		void copyStateFrom(TLex that) {
			this.terp = that.terp;
			this.prog = that.prog;
			this.white = that.white;
			this.w = that.w;
			this.t = that.t;
			this.m = that.m;
			this.front = that.front;
			this.rest = that.rest;
		}

		void storeState() {
			storage.copyStateFrom(this);
		}

		void recallState() {
			this.copyStateFrom(storage);
		}

		private boolean attempt(Pat pat) {
			m = pat.matcher(rest);
			if (m.lookingAt()) {
				w = rest.substring(m.start(), m.end()); // Detected word.
				rest = rest.substring(m.end() - m.start()); // New substring of
															// p
				// to match.
				t = pat; // Which pattern matched.

				return true;
			}
			return false;
		}

		void advance() {
			white = rest;
			while (true) {
				if (rest.length() == 0) {
					// EOF condition.
					w = "";
					t = null;
					return;
				}
				// Skip white space & comments.
				if (attempt(Pat.WHITE)) {
					// TODO: keep only the newlines.
					white += w;
				} else if (attempt(Pat.COMMENT)) {
					white += w;
				} else {
					break;
				}
			}
			front = rest;

			if (rest.length() == 0) {
				// EOF condition.
				w = "";
				t = null;
				return;
			}

			if (attempt(Pat.NAME) || attempt(Pat.NUMBER)) {
				return;
			} else if (attempt(Pat.STRING)) {
				StringBuffer sb = new StringBuffer();
				while (true) {
					sb.append(w.substring(1, w.length() - 1)); // leave off
					// initial &
					// final <'>
					// If another string starts immediately, it was a doubled
					// <''>.
					if (Pat.STRING.matcher(rest).lookingAt()) {
						sb.append("'"); // Add one <'> for the doubled <''>
						// input.
						attempt(Pat.STRING);
					} else {
						break;
					}
				}
				w = sb.toString();
				return;
			} else if (attempt(Pat.OTHER)) {
				return;
			} else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < prog.length(); i++) {
					sb.append(fmt("%d ", (int) prog.charAt(i)));
				}
				terp.say("PROG = %s", sb);
				terp.say("Entire Program (%d chars): <%s>", prog.length(), prog);
				terp.toss(
						"Parser Error at char <%d>, remaining unparsed code: <%s>",
						prog.length() - rest.length(), rest);
			}
		}

		boolean isEOF() {
			return t == null;
		}

		boolean endsStmt() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == '.';
		}

		boolean marksBlockParam() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == ':' || c == '.' && w.equals("..");
		}

		boolean isAssign() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == '=' || c == '-' && w.equals("--");
		}

		boolean isTupler() {
			if (t != Pat.OTHER)
				return false;
			return w.equals(",");
		}

		boolean isLister() {
			if (t != Pat.OTHER)
				return false;
			return w.equals(";") || w.equals(".,") || w.equals(",.");
		}

		boolean isChain() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == '$' || c == ',' && w.equals(",,");
		}
		
		boolean opensParen() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == '(' || c == '[' || c == '{' || c == '!';
		}

		boolean closesParen() {
			if (t != Pat.OTHER)
				return false;
			char c = w.charAt(0);
			return c == ')' || c == ']' || c == '}' || c == '?';
		}

		boolean isBinop3() {
			if (t != Pat.OTHER)
				return false;
			return w.equals("==") || w.equals("!=") || w.equals("<")
					|| w.equals("<=") || w.equals(">") || w.equals(">=")
					|| w.equals("/eq") || w.equals("/ne") || w.equals("/lt")
					|| w.equals("/le") || w.equals("/gt") || w.equals("/ge");
		}

		boolean isBinop2() {
			if (t != Pat.OTHER)
				return false;
			return w.equals("+") || w.equals("-") || w.equals("|")
					|| w.equals("^") || w.equals("/pl") || w.equals("/mi")
					|| w.equals("/or") || w.equals("/xo");
		}

		boolean isBinop1() {
			if (t != Pat.OTHER)
				return false;
			return w.equals("*") || w.equals("/") || w.equals("%")
					|| w.equals("&") || w.equals("/ti") || w.equals("/di")
					|| w.equals("/mo") || w.equals("/an");
		}

		boolean isBinop() {
			return isBinop1() || isBinop2() || isBinop3();
		}

		boolean isKeyword() {
			if (t == Pat.NAME) {
				storeState();
				advance();
				if (w.length() > 0 && w.charAt(0) == ':') {
					recallState();
					return true;
				}
				recallState();
			}
			return false;
		}

		/** Name without the trailing colons or slashes. */
		String keywordName() {
			assert isKeyword();
			return w;
		}

		/** Look ahead at the next token, to distinguish MACRO. */
		boolean isMacro() {
			assert (t == Pat.NAME);
			storeState();
			advance();
			boolean z = opensParen();
			recallState();
			return z;
		}
		
		int frontLocation() {
			return prog.length() - front.length();
		}
	}
}
