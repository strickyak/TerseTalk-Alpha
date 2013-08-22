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

import java.util.HashMap;

import terse.vm.Cls.Meth;
import terse.vm.Ur.Obj;
import terse.vm.Terp.Frame;

public abstract class Expr extends Obj {
	// =cls "Parser" Expr Obj

	// =get Expr String white white
	public String white = null;
	// =get Expr String front front
	public String front = null;
	// =get Expr String rest rest
	public String rest = null;

	public Expr(Cls cls) {
		super(cls);
	}

	// =meth Expr "eval" evalFrame:
	public abstract Ur eval(Frame f);

	final Expr setFront(String front) {
		this.front = front;
		return this;
	}

	final Expr setRest(String rest) {
		this.rest = rest;
		return this;
	}
	
	final String substr() {
		return front.substring(0, front.length() - rest.length());
	}

	/** Does it need parens around it, in toString()? */
	boolean isComplicated() {
		return false;
	}

	// =meth Expr "parser" depth
	public int depth() {
		return 0;
	}

	void pretty(int level, StringBuffer sb) {
		sb.append(this.toString());
	}

	public String toPrettyString() {
		StringBuffer sb = new StringBuffer();
		pretty(0, sb);
		return sb.toString();
	}
	
	public void dump(String ind) {
		terp().say("%s| @@ <%s#%s> ============", ind, this.getClass().getName(), this);
		if (this.front != null) {
		terp().say("%d %d %d", this.white.length(), this.front.length(), this.rest.length());
		assert this.white.length() >= this.front.length();
		assert this.front.length() >= this.rest.length();
		terp().say("%s^^^W: @@ <<<%s>>>W", ind, /*this.white == null ? "<NULL>" : */this.white.substring(0, this.white.length() - this.front.length()).replace('\n', '_'));
		terp().say("%s^^^F: @@ <<<%s>>>F", ind, /*this.front == null ? "<NULL>" : */this.front.substring(0, this.front.length() - this.rest.length()).replace('\n', '_'));
		terp().say("%s^^^R: @@ <<<%s>>>R", ind, /*this.rest == null ? "<NULL>" : */this.rest.replace('\n', '_'));
		}
	}
	
	public void visit(Visitor v) {
		v.visitExpr(this);
	}

	public static abstract class LValue extends Expr {
		// =cls "Parser" LValue Expr
		public LValue(Cls cls) {
			super(cls);
		}

		@Override
		public Ur eval(Frame f) {
			return f.terp().toss("Should not eval: <%s>", this);
		}

		// =meth LValue "parser" storeFrame:value:
		public abstract void store(Frame f, Ur x);
		// =meth LValue "parser" recallFrame:
		public abstract Ur recall(Frame f);

		public abstract void fixIndices(Parser p);
	}

	public static abstract class LvName extends LValue {
		// =cls "Parser" LvName LValue		

		// =get LvName String name name
		String name;

		// =get LvName int index index
		int index;

		public LvName(Cls cls, String name) {
			super(cls);
			this.name = name;
			this.index = 0xDeadBeef;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static final class LvLocalName extends LvName {
		// =cls "Parser" LvLocalName LvName
		public LvLocalName(Terp terp, String name) {
			super(terp.wrap.clsLvLocalName, name);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public void store(Frame f, Ur x) {
			f.locals[index] = x;
		}

		@Override
		public Ur recall(Frame f) {
			return f.locals[index];
		}

		@Override
		public void fixIndices(Parser p) {
			index = p.indexForLocalVariable(name, name.toLowerCase());
		}
		@Override
		public void visit(Visitor v) {
			v.visitLvLocalName(this);
		}
	}

	public static final class LvInstName extends LvName {
		// =cls "Parser" LvInstName LvName
		public LvInstName(Terp terp, String name) {
			super(terp.wrap.clsLvLocalName, name);
		}

		@Override
		public String toString() {
			return fmt("\"[%d]\" %s", index, name);
		}

		@Override
		public void store(Frame f, Ur x) {
			f.self.instVars[index] = x;
		}

		@Override
		public Ur recall(Frame f) {
			return f.self.instVars[index];
		}

		@Override
		public void fixIndices(Parser p) {
			index = p.instVars.get(name.toLowerCase());
		}
		@Override
		public void visit(Visitor v) {
			v.visitLvInstName(this);
		}
	}

	public static class LvTuple extends LValue {
		// =cls "Parser" LvTuple LValue
		

		// =get LvTuple Ur[] arr arr
		public Ur[] arr;

		public LvTuple(Cls cls, Ur[] arr) {
			super(cls);
			this.arr = arr;
		}
		public LvTuple(Terp terp, Ur[] arr) {
			super(terp.wrap.clsLvTuple);
			this.arr = arr;
		}

		@Override
		public String toString() {
			StringBuilder z = new StringBuilder();
			for (Ur e : arr) {
				z.append(fmt("%s,", e.toString()));
			}
			return z.toString();
		}

		@Override
		public void store(Frame f, Ur x) {
			Vec v = x.asVec();
			if (v == null) {
				terp().toss(
						"Expected a Vec value for destructuring assignment to <%s> but got value <%s#%s>",
						this, x.cls, x);
			}
			if (v.vec.size() < arr.length) {
				terp().toss(
						"A Vec value with length at least %d is required for destructuring assignment to <%s> but got value <%s#%s>",
						arr.length, this, x.cls, x);
			}
			for (int i = 0; i < arr.length; i++) {
				((LValue) arr[i]).store(f, v.vec.get(i));
			}
		}

		@Override
		public Ur recall(Frame f) {
			Ur[] zz = new Ur[arr.length];
			for (int i = 0; i < arr.length; i++) {
				zz[i] = ((LValue) arr[i]).recall(f);
			}
			return new Vec(f.terp(), zz);
		}

		@Override
		public void fixIndices(Parser p) {
			for (Ur x : arr) {
				((LValue) x).fixIndices(p);
			}

		}
		
		@Override
		public void visit(Visitor v) {
			v.visitLvTupleOrList(this);
		}
	}

	public static final class LvList extends LvTuple {
		// =cls "Parser" LvList LvTuple

		public LvList(Terp terp, Ur[] arr) {
			super(terp.wrap.clsLvList, arr);
			this.arr = arr;
		}

		@Override
		public String toString() {
			StringBuilder z = new StringBuilder();
			for (Ur e : arr) {
				z.append(fmt("%s;", e.toString()));
			}
			return z.toString();
		}
	}

	public static final class MethTop extends Expr {
		// =cls "Parser" MethTop Expr
		
		// =get MethTop int numLocals numLocals
		public int numLocals; // Counts args. Does not count "self".

		// =get MethTop int numArgs numArgs
		public int numArgs; // Does not count "self".
		HashMap<String, Integer> localVars; // Keys are lowercase.
		HashMap<String, String> localVarSpelling; // Values retain case.
		

		// =get MethTop . onCls onCls
		public Cls onCls;
		// =get MethTop String methName methName
		public String methName;
		// =get MethTop . body body
		public Expr body;
		// =get MethTop String source source
		public String source;

		public MethTop(int numVars, int numArgs,
				HashMap<String, Integer> localVars,
				HashMap<String, String> localVarSpelling, Cls onCls,
				String methName, Expr body, String source) {
			super(onCls.terp.wrap.clsMethTop);
			this.numLocals = numVars;
			this.numArgs = numArgs;
			this.localVars = localVars;
			this.localVarSpelling = localVarSpelling;
			this.onCls = onCls;
			this.methName = methName;
			this.body = body;
			this.source = source;
			this.white = source;
			this.front = source;
			this.rest = "";
		}

		@Override
		public Ur eval(Frame f) {
			return body.eval(f);
		}

		public String toString() {
			return fmt(
					"TOP(cls=%s methName=%s numLocals=%s numArgs=%s body=<%s>)",
					onCls, methName, numLocals, numArgs, body);
		}

		void pretty(int level, StringBuffer sb) {
			body.pretty(1, sb);
		}
		
		public void dump(String ind) {
			terp().say("\n\n(((((\n\n");
			super.dump(ind);
			body.dump(ind + "|");
			terp().say("\n\n)))))\n\n");
		}
		
		@Override
		public void visit(Visitor v) {
			v.visitTop(this);
		}

		// =meth MethTop "meth" sends
		public Dict _sends() {
			HashMap<String, int[]> z = new HashMap<String, int[]>();
			MessageSendsVisitor msv = new MessageSendsVisitor(this, z);
			msv.visitTop(this);
			Dict d = new Dict(terp());
			for (String k : z.keySet()) {
				d.dict.put(terp().newStr(k), terp().newVec(z.get(k)));
			}
			return d;
		}
	}

	public static final class PutLValue extends Expr {
		// =cls "Parser" PutLValue Expr
		LValue lvalue;
		Expr expr;

		public PutLValue(LValue lvalue, Expr expr) {
			super(expr.terp().wrap.clsPutLValue);
			this.lvalue = lvalue;
			this.expr = expr;
		}

		public Ur eval(Frame f) {
			Ur x = expr.eval(f);
			if (f.self.cls.trace) {
				say("PutInstVar: %s := %s#%s", lvalue, x.cls.cname, x);
			}
			lvalue.store(f, x);
			return x;
		}

		public String toString() {
			return fmt("%s = %s", lvalue, expr);
		}
	}

//	public static final class PutInstVar extends Expr {
//		// =cls "Parser" PutInstVar Expr
//		String name;
//		Expr expr;
//		int index;
//
//		public PutInstVar(String name, int index, Expr expr) {
//			super(expr.terp().wrap.clsPutInstVar);
//			this.name = name;
//			this.index = index;
//			this.expr = expr;
//		}
//
//		public Ur eval(Frame f) {
//			Ur x = expr.eval(f);
//			if (f.self.cls.trace) {
//				say("PutInstVar: %s#%s \"i%d\" %s := %s#%s", f.self.cls.name,
//						f.self.hashCode(), index, name, x.cls.name,
//						x.toString());
//			}
//			f.self.instVars[index] = x;
//			return x;
//		}
//
//		public String toString() {
//			return fmt("%s= %s", name, expr);
//		}
//	}

	public static final class GetInstVar extends Expr {
		// =cls "Parser" GetInstVar Expr
		String name;
		int index;

		public GetInstVar(Terp t, String name, int index) {
			super(t.wrap.clsGetInstVar);
			this.name = name;
			this.index = index;
		}

		public Ur eval(Frame f) {
			return f.self.instVars[index];
		}

		public String toString() {
			return fmt("\"[%d]\" %s", index, name);
		}
	}

	public static final class GetLocalVar extends Expr {
		// =cls "Parser" GetLocalVar Expr
		String name;
		int index;

		GetLocalVar(Terp t, String key, int index) {
			super(t.wrap.clsGetLocalVar);
			this.name = key;
			this.index = index;
		}

		public Ur eval(Frame f) {
			if (index >= f.locals.length) {
				toss("GetLocalVar OutOfBounds name=%s index=%s frame=%s", name,
						index, f);
			}
			return f.locals[index];
		}

		public String toString() {
			return fmt("%s", name);
		}
	}

	public static final class GetSelf extends Expr {
		// =cls "Parser" GetSelf Expr
		GetSelf(Terp t) {
			super(t.wrap.clsGetSelf);
		}

		public Ur eval(Frame f) {
			return f.self;
		}

		public String toString() {
			return fmt("me");
		}
	}

	public static final class GetFrame extends Expr {
		// =cls "Parser" GetFrame Expr
		GetFrame(Terp t) {
			super(t.wrap.clsGetFrame);
		}

		public Ur eval(Frame f) {
			return f;
		}

		public String toString() {
			return fmt("Sys frame");
		}
	}

	public static final class GetGlobalVar extends Expr {
		// =cls "Parser" GetGlobalVar Expr
		String key;

		GetGlobalVar(Terp t, String key) {
			super(t.wrap.clsGetGlobalVar);
			this.key = key;
		}

		public Ur eval(Frame f) {
			Ur z = f.terp().clss.get(key);
			if (z == null) {
				if (key.equals("me")) {
					toss("STUPID me ERROR");
				}
				toss("Global Variable <%s> was never set. (%d)", key, key.length());
			}
			return z;
		}

		public String toString() {
			return fmt("%s", key);
		}
	}

//	public static final class PutLocalVar extends Expr {
//		// =cls "Parser" PutLocalVar Expr
//		String key;
//		int index;
//		Expr expr;
//
//		PutLocalVar(String key, int index, Expr expr) {
//			super(expr.terp().wrap.clsPutLocalVar);
//			this.key = key;
//			this.index = index;
//			this.expr = expr;
//		}
//
//		public Ur eval(Frame f) {
//			Ur value = expr.eval(f);
//			f.locals[index] = value;
//			return value;
//		}
//
//		public String toString() {
//			return fmt("%s= %s", key, expr);
//		}
//	}

	public static final class Send extends Expr {
		// =get Send . rcvr rcvr
		public Expr rcvr;
		// =get Send String msg	msg
		public String msg;
		// =get Send Expr[] args args
		public Expr[] args;
		// =get Send int[] sourceLoc sourceLoc
		public int[] sourceLoc;

		// Single slot Call-Site cache.
		Cls cacheCls = null;
		int cacheGeneration = 0;
		Meth cacheMeth = null;

		// =cls "Parser" Send Expr
		Send(Expr rcvr, String msg, Expr args[], int[] sourceLoc) {
			super(rcvr.terp().wrap.clsSend);
			this.rcvr = rcvr;
			this.msg = msg;
			this.args = args;
			this.sourceLoc = sourceLoc;
		}

		static boolean understands(Ur r, String msg) {
			// Build just enough of a Send to answer question.
			return null != findMeth(r, msg, false);
		}

		public static Meth findMeth(Ur r, String msg, boolean gripe) {
			Terp terp = r.terp();
			for (Cls c = r.cls; c != null; c = c.supercls) {
				Meth m = c.meths.get(msg);
				if (m != null)
					return m;
			}
			if (gripe) {
				// If above fails, list the possibilities.
				terp.say("Looking for method <%s> on class <%s>...", msg,
						r.cls.cname);
				for (Cls c = r.cls; c != null; c = c.supercls) {
					terp.say("Visiting class <%s>", c.cname);
					for (String k : c.meths.keySet()) {
						terp.say("Method <%s> exists on class <%s>", k, c.cname);
					}
				}
				terp.say("Couldn't find method <%s> on class <%s>", msg,
						r.cls.cname);
			}
			return null;
		}

		public static Meth findSuperMeth(Frame f, String msg) {
			Terp terp = f.terp();
			MethTop top = f.top;
			for (Cls c = top.onCls.supercls; c != null; c = c.supercls) {
				Meth m = c.meths.get(msg);
				if (m != null)
					return m;
			}
			terp.toss("Couldn't find super method <%s> on current class <%s> on actual instance <%s#%s>",
					msg, top.onCls, f.self.cls, f.self);
			return null;  // NOTREACHED
		}

		@Override
		public Ur eval(Frame f) {
			final Terp t = terp();
			t.tick();
			
			Ur r = rcvr.eval(f);
			assert r != null;
			Ur a[] = new Ur[args.length];
			for (int i = 0; i < args.length; i++) {
				a[i] = args[i].eval(f);
				assert a[i] != null;
			}
			Meth m;
			if (r == r.cls.terp.instSuper) {
				m = findSuperMeth(f, msg);
				r = f.self;  // change instSuper back to actual self.
			} else {
				// Use call-site cache.
				if (r.cls == cacheCls && r.cls.generation == cacheGeneration) {
					// TODO: it seems generation should depend on cls match,
					// so quit checking for cls match.
					m = cacheMeth;
				} else {
					m = findMeth(r, msg, true);

					if (m == null) {
						toss("MessageNotUnderstood: message <%s> to instance of class <%s>: <%s>",
								msg, r.cls.cname, r);
					}
					// Set call-site cache.
					cacheCls = r.cls;
					cacheGeneration = cacheCls.generation;
					cacheMeth = m;
				}
			}
			if (r.cls.trace || m.trace) {
				say("Sending message <%s> to <%s#%s> with <%s>", m.name,
						r.cls.cname, r.toString(), show(args));
			}
			Ur z = null;
			try {
				z = m.apply(f, r, a);
			} catch (RuntimeException ex) {
				String what = fmt(
						"\n  * During SEND: <%s %s>\n  * * TO: %s«%s»",
						r.cls.cname, m.name, r.cls.cname, r.toString());
				for (Ur u : a) {
					what += "\n  * * * ARG: " + u.cls.cname + "«" + u + "»"; 
				}
				retoss("%s", ex.toString() + what);
			}
			if (r.cls.trace || m.trace) {
				say("Message <%s> to <%s#%s> returns <%s#%s>", m.name,
						r.cls.cname, r.toString(), z.cls.cname, z.toString());
			}
			return z;
		}

		public String toString() {
			if (args.length == 0) {
				if (rcvr instanceof Block) {
					// Special presentation of unary message to Block:
					// Use MACRO syntax, with UpperCase message, and curly
					// braces.
					if (msg.toLowerCase().equals("r")
							|| msg.toLowerCase().equals("run")) {
						// Recognize RUN Block, and just print parens.
						return fmt("(%s) ", ((Block) rcvr).gutsToString());
					} else {
						return fmt("%s(%s) ", msg.toUpperCase(),
								((Block) rcvr).gutsToString());
					}
				} else if (rcvr.isComplicated()) {
					return fmt("(%s) %s ", rcvr, msg);
				} else {
					return fmt("%s %s ", rcvr, msg);
				}
			} else {
				StringBuffer sb = new StringBuffer();
				if (rcvr.isComplicated()) {
					sb.append(fmt("(%s) ", rcvr.toString()));
				} else {
					sb.append(fmt("%s ", rcvr.toString()));
				}
				char m0 = msg.charAt(0);
				if (Character.isLetter(m0)) {
					// Keyword message.
					String[] words = msg.split(":");
					for (int i = 0; i < args.length; i++) {
						if (args[i].isComplicated()) {
							sb.append(fmt("%s: (%s) ", words[i],
									args[i].toString()));
						} else {
							sb.append(fmt("%s: %s ", words[i],
									args[i].toString()));
						}
					}
				} else {
					// Binary operator message.
					assert args.length == 1 : args.length;
					if (args[0].isComplicated()) {
						sb.append(fmt("%s (%s) ", msg, args[0].toString()));
					} else {
						sb.append(fmt("%s %s ", msg, args[0].toString()));
					}
				}
				return sb.toString();
			}
		}

		boolean isComplicated() {
			return args.length > 0;
		}
		

		public void dump(String ind) {
			super.dump(ind);
			terp().say("%s| %s#%s", ind, this.getClass().getName(), this);
			
			terp().say("<><><>");
			String[] words = msg.split(":");
			terp().say("msg words:", show(words));
			terp().say("locations:", show(this.sourceLoc));
			for (int i = 0; i < words.length; i++) {
				if (words[i].length() > 0 && this.sourceLoc[i] >= 0) {
					terp().say("[%d] '%s' @%s >>====>>", i, words[i], sourceLoc[i]);
				}
			}
			terp().say("<><><>");
			
			rcvr.dump(ind + fmt(" Send{%s} R ", this.msg));
			for (int i = 0; i < args.length; i++) {
				args[i].dump(ind + fmt(" Send A%d ", i));
			}
		}

		
		@Override
		public void visit(Visitor v) {
			v.visitSend(this);
		}
	}

	public static final class Block extends Expr {
		// =cls "Parser" Block Expr
		Expr body;
		Expr[] params;

		public Block(Expr body, Expr[] params) {
			super(body.terp().wrap.clsBlock);
			this.body = body;
			this.params = params;
		}

		@Override
		public Ur eval(Frame f) {
			return new Blk(this, f);
		}

		/** toString but without the brackets */
		String gutsToString() {
			StringBuilder sb = new StringBuilder();
			for (Expr param : params) {
				sb.append(param.toString());
				sb.append(": ");
			}
			sb.append(body.toString());
			return sb.toString();
		}

		public String toString() {
			return "[ " + gutsToString() + "] ";
		}

		void pretty(int level, StringBuffer sb) {
			sb.append("[ ");
			for (Expr param : params) {
				sb.append(param.toString());
				sb.append(": ");
			}
			sb.append("\n");
			body.pretty(level + 1, sb);
			sb.append("]\n");
		}

		public int depth() {
			return 1 + body.depth();
		}
		

		public void dump(String ind) {
			super.dump(ind);
			for (int i = 0; i < params.length; i++) {
				params[i].dump(ind + fmt(" Block P%d ", i));
			}
			body.dump(ind + " Block Body ");
		}
		
		@Override
		public void visit(Visitor v) {
			v.visitBlock(this);
		}
	}

	public static final class Seq extends Expr {
		// =cls "Parser" Seq Expr
		Expr[] body; // Eval these in order; return value of last.

		public Seq(Terp t, Expr[] body) {
			super(t.wrap.clsSeq);
			this.body = body;
		}

		public Ur eval(Frame f) {
			Ur z = f.terp().instNil;
			for (Expr expr : body) {
				terp().tick();
				z = expr.eval(f);
				assert z != null;
			}
			return z;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < body.length; i++) {
				sb.append(body[i].toString());
				sb.append(". ");
			}
			return sb.toString();
		}

		void pretty(int level, StringBuffer sb) {
			for (int i = 0; i < body.length; i++) {
				sb.append(repeat(level, "  "));
				sb.append(body[i].toString());
				sb.append(".\n");
			}
		}

		boolean isComplicated() {
			return true;
		}

		public int depth() {
			int z = 0;
			for (int i = 0; i < body.length; i++) {
				z = Math.max(z, 1 + body[i].depth());
			}
			return z;
		}
		

		public void dump(String ind) {
			super.dump(ind);
			for (int i = 0; i < body.length; i++) {
				body[i].dump(ind + fmt(" Seq #%d ", i));
			}
		}
		
		@Override
		public void visit(Visitor v) {
			v.visitSeq(this);
		}
	}

	public static final class MakeVec extends Expr {
		// =cls "Parser" MakeVec Expr
		Expr[] elements; // Eval these in order; return value of last.
		char delim;

		public MakeVec(Terp t, Expr[] elements, char delim) {
			super(t.wrap.clsMakeVec);
			this.elements = elements;
			this.delim = delim;
		}

		public Ur eval(Frame f) {
			Vec z = new Vec(cls.terp);
			for (Expr expr : elements) {
				terp().tick();
				Ur x = expr.eval(f);
				assert x != null;
				z.vec.add(x);
			}
			return z;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(" (");
			for (int i = 0; i < elements.length; i++) {
				sb.append(elements[i].toString());
				sb.append(delim);
				sb.append(' ');
			}
			sb.append(") ");
			return sb.toString();
		}

		void pretty(int level, StringBuffer sb) {
			sb.append(" (");
			for (int i = 0; i < elements.length; i++) {
				sb.append(repeat(level, "  "));
				sb.append(elements[i].toString());
				sb.append(delim);
				sb.append('\n');
			}
			sb.append(") ");
		}

		boolean isComplicated() {
			return true;
		}

		public int depth() {
			int z = 0;
			for (int i = 0; i < elements.length; i++) {
				z = Math.max(z, 1 + elements[i].depth());
			}
			return z;
		}
		
		public void dump(String ind) {
			super.dump(ind);
			for (int i = 0; i < elements.length; i++) {
				elements[i].dump(ind + fmt(" MkVec #%d ", i));
			}
		}
		
		@Override
		public void visit(Visitor v) {
			v.visitMakeVec(this);
		}
	}

	public static final class Lit extends Expr {
		// =cls "Parser" Lit Expr
		Ur value; // Literal value.

		public Lit(Ur value) {
			super(value.terp().wrap.clsLit);
			this.value = value;
		}

		public Ur eval(Frame f) {
			return value;
		}

		public String toString() {
			return value.repr();
		}
	}

	// For when an expression list is empty.
	// An empty block [] evaluates to nil.
	// But an empty macro Vec() or Dict() should not
	// contain a single nil element.
	public static final class EmptyExprList extends Expr {
		// =cls "Parser" EmptyExprList Expr

		public EmptyExprList(Terp t) {
			super(t.wrap.clsEmptyExprList);
		}

		public Ur eval(Frame f) {
			return terp().instNil;
		}

		public String toString() {
			return "";
		}
	}
	

	public static abstract class Visitor {
		protected MethTop top;

		public Visitor(MethTop top) {
			super();
			this.top = top;
		}

		public void visitMakeVec(MakeVec makeVec) {
			for (int i = 0; i < makeVec.elements.length; ++i) {
				makeVec.elements[i].visit(this);
			}
		}

		public void visitSeq(Seq seq) {
			for (int i = 0; i < seq.body.length; ++i) {
				seq.body[i].visit(this);
			}
		}

		public void visitBlock(Block block) {
			for (int i = 0; i < block.params.length; ++i) {
				block.params[i].visit(this);
			}
			block.body.visit(this);
		}

		public void visitSend(Send send) {
			send.rcvr.visit(this);
			for (int i = 0; i < send.args.length; ++i) {
				send.args[i].visit(this);
			}
		}

		public void visitTop(MethTop top2) {
			top2.body.visit(this);
		}

		public void visitLvTupleOrList(LvTuple lvTuple) {
			for (int i = 0; i < lvTuple.arr.length; ++i) {
				((Expr)lvTuple.arr[i]).visit(this);
			}
		}

		public void visitLvInstName(LvInstName lvInstName) {
		}

		public void visitLvLocalName(LvLocalName lvLocalName) {
		}

		public void visitExpr(Expr e) {
		}
	}
	
	public static class MessageSendsVisitor extends Visitor {
		HashMap<String,int[]> z;

		public MessageSendsVisitor(MethTop top, HashMap<String,int[]> z) {
			super(top);
			this.z = z;
		}
		
		@Override public void visitSend(Send send) {
			int[] v = z.get(send.msg);
			if (v == null) v = emptyInts;
			int n = send.sourceLoc.length;
			for (int i = 0; i < n; i++) {
				v = append(v, send.sourceLoc[i]);
			}
			z.put(send.msg, v);
			super.visitSend(send);
		}
		
	}
}
