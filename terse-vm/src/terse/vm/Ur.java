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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import terse.vm.Cls.JavaMeth;
import terse.vm.Cls.Meth;
import terse.vm.Cls.UsrMeth;
import terse.vm.Expr.Seq;
import terse.vm.Terp.Frame;
import terse.vm.Usr.UsrCls;

@SuppressWarnings("rawtypes")
public class Ur extends Static implements Comparable {
	// =get Ur . cls cls
	public Cls cls;
	// =get Ur Ur[] instVars peekInstVars
	Ur[] instVars; // TODO: move to Usr & UsrCls.

	// =cls "Sys" Ur
	public Ur(Cls cls) {
		this.cls = cls;
		if (cls == null) { // null cls happens during bootup.
			this.instVars = emptyUrs;
			if (!Terp.tolerateNullClass) {
				breakHere(this);
				toss("Null .cls in instance of <%s>", this.getClass());
			}
		} else {
			++cls.countInstances;
			final int allVarsSize = cls.allVarMap.size();
			if (allVarsSize == 0) {
				this.instVars = emptyUrs;
			} else {
				this.instVars = new Ur[allVarsSize];
				Arrays.fill(this.instVars, cls.terp.instNil);
			}
		}
	}

	// =meth Ur "access" repr
	public String repr() {
		return toString();
	}

	// =meth Ur "access" str
	public String toString() {
		return fmt("%s~%s", cls.cname, hashCode());
	}

	public void visit(Visitor v) {
		v.visitUr(this);
	}

	public final Terp terp() {
		return cls.terp;
	}

	public final void say(String msg, Object... objects) {
		cls.terp.say(msg, objects);
	}

	public final Ur toss(String msg, Object... objects) {
		return cls.terp.toss(msg, objects);
	}

	public final Ur retoss(String msg, Object... objects) {
		return cls.terp.retoss(msg, objects);
	}

	public UsrCls usrCls() {
		return null;
	}

	public Obj asObj() {
		return null;
	}

	public Num asNum() {
		return null;
	}

	public Str asStr() {
		return null;
	}

	public Cls asCls() {
		return null;
	}

	public Blk asBlk() {
		return null;
	}

	public Undefined asNil() {
		return null;
	}

	public Usr asUsr() {
		return null;
	}

	public Vec asVec() {
		return null;
	}

	public Dict asDict() {
		return null;
	}

	public final Obj mustObj() {
		try {
			return (Obj) this;
		} catch (Exception ex) {
			return (Obj) toss("Should have been an Obj, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Str mustStr() {
		try {
			return (Str) this;
		} catch (Exception ex) {
			return (Str) toss("Should have been a Str, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Num mustNum() {
		try {
			return (Num) this;
		} catch (Exception ex) {
			return (Num) toss("Should have been a Num, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Vec mustVec() {
		try {
			return (Vec) this;
		} catch (Exception ex) {
			return (Vec) toss("Should have been a Vec, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Vec mustPair() {
		try {
			Vec z = (Vec) this;
			if (z.vec.size() != 2) {
				toss("Should have been a Vec of length 2, but was length %d: <%s>",
						z.vec.size(), z);
			}
			return z;
		} catch (Exception ex) {
			return (Vec) toss("Should have been a Vec, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Dict mustDict() {
		try {
			return (Dict) this;
		} catch (Exception ex) {
			return (Dict) toss("Should have been a Dict, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	public final Blk mustBlk() {
		try {
			return (Blk) this;
		} catch (Exception ex) {
			return (Blk) toss("Should have been a Blk, but wasn't: <%s#%s>",
					this.cls, this);
		}
	}

	// TODO: Some of the following should be down at Obj.
	// Ur should toss subclassResponsibility for anything it can.
	// No one will care, until somebody uses Ur.


	// =meth Ur "access" truth
	public boolean truth() {
		// Most objects are true.
		return true;
	}

	Comparable innerValue() {
		return null;
	}

	// =meth Ur "access" hash
	public int hashCode() {
		Object innerThis = this.innerValue();
		if (innerThis == null) {
			// Avoid infinite recursion.
			return System.identityHashCode(this);
		} else {
			return innerValue().hashCode();
		}
	}

	// =meth Ur "cmp" equals:
	public boolean equals(Object obj) {
		if (obj instanceof Ur) {
			Ur that = (Ur) obj;
			if (this.cls != that.cls) {
				return false;
			}
			Object innerThis = this.innerValue();
			Object innerThat = that.innerValue();
			if (innerThis != null && innerThat != null) {
				// Both inner objects exist; compare them.
				boolean z = innerThis.equals(innerThat);
				return z;
			} else {
				// Avoid infinite recursion.
				boolean z = this == that;
				return z;
			}
		} else {
			return false;
		}
	}

	public int compareTo(Object obj) {
		// say("CMP <%s> <%s>", this, obj);
		if (obj instanceof Ur) {
			Ur that = (Ur) obj;
			if (this.cls != that.cls) {
				// say("CMP cls.name <%s> <%s>", this.cls.name, that.cls.name);
				return this.cls.cname.compareTo(that.cls.cname);
			}
			Object innerThis = this.innerValue();
			Object innerThat = that.innerValue();
			if (innerThis != null && innerThat != null) {
				// Not Equal because innerObject returned Value-like objects.
				// say("CMP inner <%s> <%s>", innerThis, innerThat);
				return ((Comparable) innerThis).compareTo(innerThat);
			}
		}
		// say("CMP identityHashCode <%s> <%s>", System.identityHashCode(this),
		// System.identityHashCode(obj));
		return new Integer(System.identityHashCode(this))
				.compareTo(new Integer(System.identityHashCode(obj)));
	}

	// =meth Ur "debug" pokeInstVarsDict:
	public void pokeInstVarsDict_(Dict d) {
		for (String k : cls.allVarMap.keySet()) {
			int i = cls.allVarMap.get(k);
			Ur x = d.dict.get(terp().newStr(k));
			if (x != null) {
				instVars[i] = x;
			}
		}
	}

	// =meth Ur "debug" peekInstVarsDict
	public Dict _peekInstVarsDict() {
		Dict d = terp().newDict(emptyUrs);
		for (String k : cls.allVarMap.keySet()) {
			int i = cls.allVarMap.get(k);
			d.dict.put(terp().newStr(k), this.instVars[i]);
		}
		return d;
	}

	// =meth Ur "debug" dumpVarMap
	public void _dumpVarMap() {
		say("=== dumpVarMap === <%s#%s>", cls, this);
		for (Cls c = cls; c != null; c = c.supercls) {
			say("..at level <%s>:", c);
			for (int i = 0; i < c.myVarNames.length; i++) {
				say("....myVarNames[%s : %d] %s", c, i, c.myVarNames[i]);
			}
		}
		for (String k : cls.allVarMap.keySet()) {
			int i = cls.allVarMap.get(k);
			say("myVarNames[%d] %s --> <%s>", i, k, this.instVars[i]);
		}
	}

	// =meth Obj "math" nearestInt
	// "convert a Num to the nearest int, by adding 0.5 and flooring"
	public int toNearestInt() {
		Num num = this.asNum();
		if (num == null) {
			toss("Object is a %s, not a Num", this.cls.cname);
		}
		int i = (int) Math.floor(num.num + 0.5); // closest int
		return i;
	}
	
	// =meth Ur "access" does:
	public boolean does_(String a) {
		Meth meth = Expr.Send.findMeth(this, a, false);
		if (meth == null) return false;
		// TODO: consider empty and "just me" meths false.
		return true;
	}

	/** Special marker object, for message to super. */
	public static class Super extends Ur {
		// =cls "Sys" Super Ur
		Super(Terp terp) {
			super(terp.tSuper);
		}

		public String toString() {
			return "super ";
		}
	}

	// =cls "Sys" Obj Ur
	public static class Obj extends Ur {
		public Obj(Cls cls) {
			super(cls);
		}

		public Obj asObj() {
			return this;
		}

		// =meth Obj "macro" macro:cond:
		// "return first X whose P is true, with body of the form P1,X2;P2,X2;P3,X3;..."
		public Ur macroCond(Frame f, Blk b) {
			if (!(b.body instanceof Expr.MakeVec)) {
				toss("Expected COND body of form P1,X1;P2,X2;P3,X3;... but got a <%s> instead: <%s#%s>",
						b.getClass().getName(), b.cls, b);
			}
			Expr.MakeVec mv = (Expr.MakeVec) b.body;
			final int n = mv.elements.length;
			for (int i = 0; i < n; i++) {
				if (!(mv.elements[i] instanceof Expr.MakeVec)) {
					toss("Expected inner element to be a list: <%s>",
							mv.elements[i]);
				}
				Expr.MakeVec pair = (Expr.MakeVec) mv.elements[i];
				if (pair.elements.length != 2) {
					toss("Expected inner list to have length 2: <%s>", pair);
				}
				Ur pred = pair.elements[0].eval(f);
				if (pred.truth()) {
					return pair.elements[1].eval(f);
				}
			}
			return terp().instNil;
		}

		// =meth Obj "macro" macro:case:of:
		public Ur macroCaseOf(Frame f, Blk b, Blk c) {
			return macroCaseOfElse(f, b, c, null);
		}

		// =meth Obj "macro" macro:case:of:else:
		public Ur macroCaseOfElse(Frame f, Blk b, Blk c, Blk d) {
			Ur target = b.evalWithoutArgs();
			if (!(c.body instanceof Expr.MakeVec)) {
				toss("Expected COND body of form P1,X2;P2,X2;P3,X3;...");
			}
			Expr.MakeVec mv = (Expr.MakeVec) c.body;
			final int n = mv.elements.length;
			for (int i = 0; i < n; i++) {
				if (!(mv.elements[i] instanceof Expr.MakeVec)) {
					toss("Expected inner element to be a list: <%s>",
							mv.elements[i]);
				}
				Expr.MakeVec pair = (Expr.MakeVec) mv.elements[i];
				if (pair.elements.length != 2) {
					toss("Expected inner list to have length 2: <%s>", pair);
				}
				Ur key = pair.elements[0].eval(f);
				if (key.equals(target)) {
					return pair.elements[1].eval(f);
				}
			}
			if (d == null) {
				return terp().instNil; // For macroCaseOf().
			} else {
				return d.evalWithoutArgs();
			}
		}

		// =meth Obj "macro" macro:try:catch:
		public Ur macroTryCatch(Frame f, Blk b, Blk c) {
			try {
				return b.evalWithoutArgs();
			} catch (RuntimeException ex) {
				Str what = terp().newStr(ex.toString());
				return c.evalWith1Arg(what);
			}
		}

		public static Vec evalMacroBlockMakingVec(Blk b) {
			Ur[] arr;
			if (b.body instanceof Expr.MakeVec) {
				Expr.MakeVec mv = (Expr.MakeVec) b.body;
				final int n = mv.elements.length;
				arr = new Ur[n];
				for (int i = 0; i < n; i++) {
					// Eval in blk's frame!
					Ur x = mv.elements[i].eval(b.f);
					arr[i] = x;
				}
			} else if (b.body instanceof Expr.EmptyExprList) {
				arr = emptyUrs;
			} else {
				// Anything else is a singleton.
				Ur x = b.evalWithoutArgs();
				arr = urs(x);
			}
			return new Vec(b.terp(), arr);
		}

		// =meth Obj "macro" macro:vec: "Execute the block to return a vector."
		public Ur macroVec(Frame _, Blk b) {
			return evalMacroBlockMakingVec(b);
		}

		// =meth Obj "macro" macro:dict: "Construct a Dict from list of pairs."
		public Ur macroDict(Frame _, Blk b) {
			Vec list = evalMacroBlockMakingVec(b);
			Dict z = new Dict(b.terp());
			int sz = list.vec.size();
			for (int i = 0; i < sz; i++) {
				Vec tuple = list.vec.get(i).mustVec();
				assert tuple.vec.size() == 2 : tuple;
				z.dict.put(tuple.vec.get(0), tuple.vec.get(1));
			}
			return z;
		}

		// =meth Obj "macro" macro:while:do:
		public void macroWhileDo(Frame _, Blk b, Blk c) {
			while (b.evalWithoutArgs().truth()) {
				c.evalWithoutArgs();
			}
		}

		// =meth Obj "macro" macro:for:do:
		public Ur macroForDo(Frame _, Blk b, Blk c) {
			Ur coll = b.evalWithoutArgs();
			Num num = coll.asNum();
			if (num != null) {
				final int n = num.toNearestInt();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, ii);
					c.evalWithoutArgs();
				}
				return terp().instNil;
			}
			Vec vec = coll.asVec();
			if (vec != null) {
				final int n = vec.vec.size();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, vec.vec.get(i));
					c.evalWithoutArgs();
				}
				return terp().instNil;
			}
			Dict dict = coll.asDict();
			if (dict != null) {
				Vec[] assocs = dict.sortedAssocs();
				final int n = assocs.length;
				for (int i = 0; i < n; i++) {
					b.storeAtParamKV(assocs[i].vec.get(0), assocs[i].vec.get(1));
					c.evalWithoutArgs();
				}
				return terp().instNil;
			}
			return toss("For needs a Num, Vec, or Dict, but got <%s>", coll);
		}

		// =meth Obj "macro" macro:for:map:
		public Ur macroForMap(Frame _, Blk b, Blk c) {
			Ur coll = b.evalWithoutArgs();
			Num num = coll.asNum();
			if (num != null) {
				final int n = num.toNearestInt();
				Ur[] zz = new Ur[n];
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, ii);
					zz[i] = c.evalWithoutArgs();
				}
				return new Vec(terp(), zz);
			}
			Vec vec = coll.asVec();
			if (vec != null) {
				final int n = vec.vec.size();
				Ur[] zz = new Ur[n];
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, vec.vec.get(i));
					zz[i] = c.evalWithoutArgs();
				}
				return new Vec(terp(), zz);
			}
			Dict dict = coll.asDict();
			if (dict != null) {
				Vec[] assocs = dict.sortedAssocs();
				final int n = assocs.length;
				Ur[] zz = new Ur[n];
				for (int i = 0; i < n; i++) {
					b.storeAtParamKV(assocs[i].vec.get(0), assocs[i].vec.get(1));
					zz[i] = c.evalWithoutArgs();
				}
				return new Vec(terp(), zz);
			}
			return toss("For needs a Num, Vec, or Dict, but got <%s>", coll);
		}

		// =meth Obj "macro" macro:for:map:if:
		public Ur macroForMapIf(Frame _, Blk b, Blk c, Blk d) {
			Ur coll = b.evalWithoutArgs();
			Vec z = new Vec(terp());
			Num num = coll.asNum();
			if (num != null) {
				final int n = num.toNearestInt();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, ii);
					if (d != null) {
						if (!(d.evalWithoutArgs().truth())) continue;
					}
					z.vec.add( c.evalWithoutArgs());
				}
				return z;
			}
			Vec vec = coll.asVec();
			if (vec != null) {
				final int n = vec.vec.size();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, vec.vec.get(i));
					if (d != null) {
						if (!(d.evalWithoutArgs().truth())) continue;
					}
					z.vec.add( c.evalWithoutArgs());
				}
				return z;
			}
			Dict dict = coll.asDict();
			if (dict != null) {
				Vec[] assocs = dict.sortedAssocs();
				final int n = assocs.length;
				for (int i = 0; i < n; i++) {
					b.storeAtParamKV(assocs[i].vec.get(0), assocs[i].vec.get(1));
					if (d != null) {
						if (!(d.evalWithoutArgs().truth())) continue;
					}
					z.vec.add( c.evalWithoutArgs());
				}
				return z;
			}
			return toss("For needs a Num, Vec, or Dict, but got <%s#%s>", coll.cls, coll);
		}

		// =meth Obj "macro" macro:for:init:reduce:
		public Ur macroForMap(Frame _, Blk b, Blk init, Blk c) {
			Ur coll = b.evalWithoutArgs();
			Ur state = init.evalWithoutArgs();
			Num num = coll.asNum();
			if (num != null) {
				final int n = num.toNearestInt();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, ii);
					init.storeAtParam0(state);
					state = c.evalWithoutArgs();
				}
				return state;
			}
			Vec vec = coll.asVec();
			if (vec != null) {
				final int n = vec.vec.size();
				for (int i = 0; i < n; i++) {
					Num ii = terp().newNum(i);
					b.storeAtParamKV(ii, vec.vec.get(i));
					init.storeAtParam0(state);
					state = c.evalWithoutArgs();
				}
				return state;
			}
			Dict dict = coll.asDict();
			if (dict != null) {
				Vec[] assocs = dict.sortedAssocs();
				final int n = assocs.length;
				for (int i = 0; i < n; i++) {
					b.storeAtParamKV(assocs[i].vec.get(0), assocs[i].vec.get(1));
					init.storeAtParam0(state);
					state = c.evalWithoutArgs();
				}
				return state;
			}
			return toss("For needs a Num, Vec, or Dict, but got <%s>", coll);
		}

		// =meth Obj "macro" macro:if:then:
		public Ur macroIfThenElse(Frame _, Blk b, Blk c) {
			if (b.evalWithoutArgs().truth()) {
				return c.evalWithoutArgs();
			} else {
				return terp().instNil;
			}
		}

		// =meth Obj "macro" macro:if:then:else:
		public Ur macroIfThenElse(Frame _, Blk b, Blk c, Blk d) {
			if (b.evalWithoutArgs().truth()) {
				return c.evalWithoutArgs();
			} else {
				return d.evalWithoutArgs();
			}
		}

		// =meth Obj "macro" macro:if:then:elif:then:else:
		public Ur macroIfThenElse(Frame _, Blk b, Blk c, Blk d, Blk e, Blk f) {
			if (b.evalWithoutArgs().truth()) {
				return c.evalWithoutArgs();
			} else if (d.evalWithoutArgs().truth()) {
				return e.evalWithoutArgs();
			} else {
				return f.evalWithoutArgs();
			}
		}

		// =meth Obj "macro" macro:if:then:elif:then:
		public Ur macroIfThenElse(Frame _, Blk b, Blk c, Blk d, Blk e) {
			if (b.evalWithoutArgs().truth()) {
				return c.evalWithoutArgs();
			} else if (d.evalWithoutArgs().truth()) {
				return e.evalWithoutArgs();
			} else {
				return terp().instNil;
			}
		}

		// =meth Obj "macro" macro:and:
		public Ur macroAnd(Frame _, Blk b) {
			if (b.body instanceof Expr.MakeVec) {
				Expr.MakeVec mv = (Expr.MakeVec) b.body;
				for (int i = 0; i < mv.elements.length; i++) {
					// Eval in blk's frame!
					Ur x = mv.elements[i].eval(b.f);
					// If any evals false, return false.
					if (!x.truth()) {
						return terp().instFalse;
					}
				}
				return terp().instTrue; // All were true.
			} else {
				return terp().toss(
						"Body of AND macro is not a vector constructor: <%s>",
						b.body);
			}
		}

		// =meth Obj "macro" macro:or:
		public Ur macroOr(Frame _, Blk b) {
			if (b.body instanceof Expr.MakeVec) {
				Expr.MakeVec mv = (Expr.MakeVec) b.body;
				for (int i = 0; i < mv.elements.length; i++) {
					// Eval in blk's frame!
					Ur x = mv.elements[i].eval(b.f);
					// If any evals true, return true.
					if (x.truth()) {
						return terp().instTrue;
					}
				}
				return terp().instFalse; // All were false.
			} else {
				return terp().toss(
						"Body of AND macro is not a vector constructor: <%s>",
						b.body);
			}
		}

		// =meth Obj "macro" macro:ht:
		// "concat Str and Ht and (recursively) Vec, as Ht"
		public Ht macroHt(Frame _, Blk b) {
			return new Ht(terp(), macroHtOnVec(evalMacroBlockMakingVec(b)));
		}

		private Html macroHtOnVec(Vec v) {
			int n = v.vec.size();
			Html html = new Html();
			for (int i = 0; i < n; i++) {
				Ur x = v.vec.get(i);
				if (x instanceof Ht) {
					html.append(((Ht) x).html);
				} else if (x instanceof Vec) {
					html.append(macroHtOnVec(((Vec) x)));
				} else {
					html.append(x.toString());
				}
			}
			return html;
		}

		// =meth Obj "macro" macro:tag:
		// "first element; rest are Ht, Str, or (key, value) params"
		public Ht macroTag(Frame _, Blk b) {
			Vec v = evalMacroBlockMakingVec(b);
			int n = v.vec.size();
			Html body = new Html();
			String[] params = emptyStrs;
			// 0th is the tag name.
			String tag = v.vec.get(0).mustStr().str;
			// So start iterating at 1.
			for (int i = 1; i < n; i++) {
				Ur x = v.vec.get(i);
				if (x instanceof Ht) {
					body.append(((Ht) x).html);
				} else if (x instanceof Vec) {
					Vec kv = (Vec) x;
					if (kv.vec.size() != 2) {
						terp().toss(
								"Subvec not size 2, in params to tag: <%s>", kv);
					}
					params = append(params, kv.vec.get(0).toString());
					params = append(params, kv.vec.get(1).toString());
				} else {
					body.append(x.toString());
				}
			}
			return new Ht(terp(), Html.tag(null, tag, params, body));
		}

		// =meth Obj "eval" apply:args:
		public Ur apply_args_(String msg, Ur[] args) {
			Meth meth = Expr.Send.findMeth(this, msg, false);
			if (meth == null) {
				return toss("Cannot send %s to %d", msg, this);
			}
			UsrMeth m = (UsrMeth) meth;
			Frame f = terp().newFrame(null, this, m._top());
			return meth.apply(f, this, args);
		}

		// =meth Obj "eval" eval: "Evaluate a string as code in this receiver."
		public Ur eval(String code) {
			Expr.MethTop top = Parser.parseMethod(cls, "__eval__", code);

			// DEBUG
			// top.dump("EVAL/TOP ");

			Ur z = top.eval(cls.terp.newFrame(null, this, top));
			assert z != null : fmt("Null result in %s.eval <%s>", this, code);
			return z;
		}

		// =meth Obj "eval" eval:arg:
		// "Evaluate a string as code in this receiver."
		public Ur eval(String code, Ur a) {
			// say("EVAL <%s> <<< <%s> <%s>", this, code, a);
			Expr.MethTop top = Parser.parseMethod(cls, "__eval__:", code);
			Frame f = cls.terp.newFrame(null, this, top);
			f.locals[0] = a;
			Ur z = top.eval(f);
			assert z != null : fmt("Null result in %s.eval <%s>", this, code);
			// say("EVAL <%s> >>> <%s>", this, z);
			return z;
		}

		// =meth Obj "eval" eval:arg:arg:
		// "Evaluate a string as code in this receiver."
		public Ur eval(String code, Ur a, Ur b) {
			// say("EVAL <%s> <<< <%s> <%s> <%s>", this, code, a, b);
			Expr.MethTop top = Parser.parseMethod(cls, "__eval__:__eval__:",
					code);
			Frame f = cls.terp.newFrame(null, this, top);
			f.locals[0] = a;
			f.locals[1] = b;
			Ur z = top.eval(f);
			assert z != null : fmt("Null result in %s.eval <%s>", this, code);
			// say("EVAL <%s> >>> <%s>", this, z);
			return z;
		}

		// =meth Obj "basic" must
		public Obj _must() {
			if (!this.truth()) {
				toss("MUST be True, but isn't: <%s>", this);
			}
			return this;
		}

		// =meth Obj "basic" must:
		public Obj must_(Obj a) {
			if (!this.truth()) {
				String why;
				if (a instanceof Blk) {
					why = ((Blk) a).evalWithoutArgs().toString();
				} else {
					why = a.toString();
				}
				toss("MUST be True, but isn't: <%s>  <%s>", this, why);
			}
			return this;
		}

		// =meth Obj "basic" cant
		public Obj _cant() {
			if (this.truth()) {
				toss("CAN'T be True, but is: <%s>", this);
			}
			return this;
		}

		// =meth Obj "basic" cant:
		public Obj cant_(Obj a) {
			if (this.truth()) {
				String why;
				if (a instanceof Blk) {
					why = ((Blk) a).evalWithoutArgs().toString();
				} else {
					why = a.toString();
				}
				toss("CAN'T be True, but is: <%s>  <%s>", this, why);
			}
			return this;
		}

		// =meth Obj "basic" err
		public Ur _err() {
			return toss("ERROR: <%s>", this);
		}

		// =meth Obj "basic" err:
		public Ur err_(Obj a) {
			return toss("ERROR: <%s> <%s>", this, a);
		}

		// =meth Obj "basic" say
		public void _say() {
			say("#SAY# <%s>", this);
		}

		// =meth Obj "basic" say:
		public void say_(Obj a) {
			say("#SAY# <%s> <%s>", this, a);
		}

		// =meth Obj "basic" sysHash
		public Num _syshash() {
			return terp().newNum(System.identityHashCode(this));
		}

		// =meth Obj "basic" not
		public Num _not() {
			return terp().boolObj(!this.truth());
		}

		// =meth Obj "control" ifNil:,ifn:
		public Ur ifNil_(Blk a) {
			if (this.asNil() != null) {
				return a.body.eval(a.f);
			} else {
				return this;
			}
		}

		// =meth Obj "control" ifNotNil:,ifnn:
		public Ur ifNotNil_(Blk a) {
			if (this.asNil() == null) {
				return a.body.eval(a.f);
			} else {
				return this;
			}
		}

		// =meth Obj "control" ifNil:ifNotNil:,ifn:ifnn:
		public Ur ifNil_ifNotNil_(Blk a, Blk b) {
			Blk blk = this.asNil() == null ? b : a;
			say("ifNil:ifNotNil: --> Choosing block %d: %s",
					this.asNil() == null ? 1 : 0, blk);
			return blk.body.eval(blk.f);
		}

		// =meth Obj "control" ifNotNil:ifNil:,ifnn:ifn:
		public Ur ifNotNil_ifNil_(Blk a, Blk b) {
			Blk blk = this.asNil() == null ? a : b;
			say("ifNotNil:ifNil: --> Choosing block %d: %s",
					this.asNil() == null ? 0 : 1, blk);
			return blk.body.eval(blk.f);
		}

		// =meth Obj "control" ifTrue:,y:
		public Ur ifTrue_(Blk a) {
			if (this.truth()) {
				return a.evalWithoutArgs();
			} else {
				return terp().instNil;
			}
		}

		// =meth Obj "control" ifFalse_:,n:
		public Ur ifFalse_(Blk a) {
			if (this.truth()) {
				return terp().instNil;
			} else {
				return a.evalWithoutArgs();
			}
		}

		// =meth Obj "control" ifTrue:ifFalse:,y:n:
		public Ur ifTrue_ifFalse_(Blk a, Blk b) {
			Blk blk = this.truth() ? a : b;
			return blk.body.eval(blk.f);
		}

		// =meth Obj "control" ifFalse:ifTrue:,n:y:
		public Ur ifFalse_ifTrue_(Blk a, Blk b) {
			Blk blk = this.truth() ? b : a;
			return blk.body.eval(blk.f);
		}

		// =meth Obj "basic" isa:
		// "is self an instance of said class or a subclass of it"
		public boolean isa_(Cls query) {
			for (Cls p = this.cls; p != null; p = p.supercls) {
				if (p == query) {
					return true;
				}
			}
			return false;
		}

		// =meth Obj "basic" is:
		// "does the argument have the same identity as self"
		public boolean is_(Ur x) {
			return this == x;
		}
	}

	// =cls "Sys" File Obj
	public final static class File extends Obj {
		public File(Terp terp) {
			super(terp.wrap.clsFile);
		}

		// =meth FileCls "io" dir
		// "Return list of tuples: names, mtime, length."
		public static Vec _dir(Terp terp) {
			Vec z = new Vec(terp);
			java.io.File theDir = terp.getFilesDir();
			String[] names = theDir.list();
			for (int i = 0; i < names.length; i++) {
				java.io.File theFile = new java.io.File(theDir, names[i]);
				String name = names[i];
				if (name.endsWith(".txt")) {
					z.vec.add(new Vec(terp, urs(terp.newStr(name),
							terp.newNum(theFile.lastModified() / 1000),
							terp.newNum(theFile.length()))));
				}
			}
			return z;
		}

		// =meth FileCls "io" read: "Read a text file as one big Str."
		public static Ur read_(Terp terp, String filename) {
			terp.checkTxtFileNameSyntax(filename);
			StringBuilder sb = new StringBuilder();
			try {
				FileInputStream fis = terp.openFileRead(filename);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				while (true) {
					String line = br.readLine();
					if (line == null)
						break;
					sb.append(line);
					sb.append('\n');
				}
				fis.close();
				return terp.newStr(sb.toString());
			} catch (IOException e) {
				return terp.toss("Cannot readTxt: <%s>: %s", filename, e);
			}
		}

		public static void writeOrAppendTxt(Terp terp, String filename,
				String content, boolean append) {
			terp.checkTxtFileNameSyntax(filename);
			StringBuilder sb = new StringBuilder();
			try {
				FileOutputStream fos = append ? terp.openFileAppend(filename)
						: terp.openFileWrite(filename);
				PrintStream ps = new PrintStream(fos);
				ps.print(content);
			} catch (IOException e) {
				terp.toss("Cannot writeTxt: <%s>: %s", filename, e);
			}
		}

		// =meth FileCls "io" write:value: "Write a text file as one big Str."
		public static void write_value_(Terp terp, String filename,
				String content) {
			writeOrAppendTxt(terp, filename, content, false);
		}

		// =meth FileCls "io" append:value:
		// "Append a text file as one big Str."
		public static void append_value_(Terp terp, String filename,
				String content) {
			writeOrAppendTxt(terp, filename, content, true);
		}
		
		// =meth FileCls "io" delete:
		public static void delete_(Terp terp, String filename) {
			terp.deleteFile(filename);
		}
	}

	// =cls "Sys" Hub Obj
	public final static class Hub extends Obj {
		public Hub(Terp terp) {
			super(terp.wrap.clsHub);
		}

		// =meth HubCls "io" dir "List files on web with mtime and size."
		public static Vec _dir(Terp terp) {
			return terp.listOfWebFiles();
		}

		// =meth HubCls "io" read: "Pull a file from the Web."
		public static String read_(Terp terp, String filename) {
			return terp.pullFromWeb(filename);
		}

		// =meth HubCls "io" write:value: "Push a file to the Web."
		public static void write_value_(Terp terp, String filename,
				String content) {
			terp.pushToWeb(filename, content);
		}
	}

	// =cls "Sys" Sys Obj
	public final static class Sys extends Obj {
		public Sys(Terp terp) {
			super(terp.tSys);
		}

		// =meth SysCls "debug" said
		public static Vec _said(Terp terp) {
			return terp.mkStrVec(terp.getLog());
		}

		// =meth SysCls "usr" find:
		public static Obj find_(Terp terp, String oname) {
			String[] ww = oname.split("@");
			if (ww.length != 2) {
				terp.toss("Should split into 2 parts on '@': <%s>", oname);
			}
			Cls c = terp.clss.get(ww[1].toLowerCase());
			if (c == null) {
				terp.toss("Cannot find the class <%s> for object <%s>", ww[1],
						oname);
			}
			if (ww[0].length() == 0) {
				// Empty instance part means return the Cls object.
				return c;
			}
			if (!(c instanceof UsrCls)) {
				terp.toss(
						"class <%s> is not a subclass of Usr, for object <%s>",
						c.cname, oname);
			}
			UsrCls uc = (UsrCls) c;
			Obj x = uc.find(ww[0]);
			return x;
		}

		// =meth SysCls "sys" sleep:
		public static void sleep_(Terp terp, float secs) {
			try {
				Thread.sleep((long) (secs * 1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// =meth SysCls "sys" secs
		public static Num secs(Terp terp) {
			return terp.newNum(System.currentTimeMillis() / 1000.0);
		}

		// =meth SysCls "sys" nanos
		public static Num nanos(Terp terp) {
			return terp.newNum(System.nanoTime());
		}

		// =meth SysCls "sys" worldName "which world is loaded"
		public static String _worldName(Terp terp) {
			return terp.worldName;
		}

		// =meth SysCls "sys" worldFileName "which world is loaded"
		public static String _worldFileName(Terp terp) {
			return terp.worldFilename;
		}

		// =meth SysCls "sys" fail "Create a Java Exception"
		public static void _fail(Terp terp) {
			terp.toss("FAIL: <Sys fail> was called");
		}

		// =meth SysCls "sys" fail: "Create a Java Exception"
		public static void _fail(Terp terp, Ur msg) {
			terp.toss("FAIL: " + msg);
		}

		// =meth SysCls "sys" trigraphs "keyboard character substitution dict"
		public static Ur trigraphs(Terp terp) {
			Dict d = new Dict(terp);
			for (int i = 0; i < Parser.TRIGRAPH_DATA.length; i += 2) {
				d.dict.put(terp.newStr("/" + Parser.TRIGRAPH_DATA[i]),
						terp.newStr(Parser.TRIGRAPH_DATA[i + 1]));
			}
			return d;
		}
	}

	public final static class Undefined extends Obj { // Nil
		// =cls "Data" Undefined Obj
		public Undefined(Cls cls) {
			super(cls);
		}

		@Override
		public Undefined asNil() {
			return this;
		}

		@Override
		public String toString() {
			return "Nil";
		}

		@Override
		public void visit(Visitor v) {
			v.visitUndefined(this);
		}

		// Most objects are true.
		// nil is false.
		@Override
		public boolean truth() {
			return false;
		}
	}

	public final static class Blk extends Obj {
		;;;;;
		Expr.Block block;
		Expr body;
		Expr[] params;
		Frame f;

		// =cls "Data" Blk Obj
		public Blk(Expr.Block block, Frame f) {
			super(f.terp().tBlk);
			this.block = block;
			this.body = block.body;
			this.params = block.params;
			this.f = f;
		}

		public Blk asBlk() {
			return this;
		}

		public String toString() {
			return block.toString();
		}

		// =meth Blk "param" storeAtParam0:
		public void storeAtParam0(Ur x) {
			if (params.length > 0) {
				((Expr.LValue) params[0]).store(f, x);
			}
		}

		// =meth Blk "param" storeAtParam1:
		public void storeAtParam1(Ur x) {
			if (params.length > 1) {
				((Expr.LValue) params[1]).store(f, x);
			}
		}

		// =meth Blk "param" storeAtParam:value:
		public void storeAtParamKV(Ur k, Ur v) {
			if (params.length == 1) {
				((Expr.LValue) params[0]).store(f, v);
			} else if (params.length > 1) {
				((Expr.LValue) params[0]).store(f, k);
				((Expr.LValue) params[1]).store(f, v);
			}
		}

		// =meth Blk "eval" value
		public Ur evalWithoutArgs() {
			// Notice the block runs in its own frame, not the caller's frame.
			return this.body.eval(this.f);
		}

		// =meth Blk "eval" value:
		public Ur evalWith1Arg(Ur arg0) {
			if (this.params.length < 1) {
				toss("Block takes <%d> params, but calling it with 1 arg: Blk=<%s> arg0=<%s>",
						this.params.length, this, arg0);
			}
			// Notice the block runs in its own frame, not the caller's frame.
			((Expr.LValue) params[0]).store(this.f, arg0);
			return this.body.eval(this.f);
		}

		// =meth Blk "eval" value:value:
		public Ur evalWith2Args(Ur arg0, Ur arg1) {
			if (this.params.length < 2) {
				toss("Block takes <%d> params, but calling it with 2 args: Blk=<%s> arg0=<%s> arg1=<%s>",
						this.params.length, this, arg0, arg1);
			}
			// Notice the block runs in its own frame, not the caller's frame.
			((Expr.LValue) params[0]).store(this.f, arg0);
			((Expr.LValue) params[1]).store(this.f, arg1);
			return this.body.eval(this.f);
		}
	}

	public final static class Num extends Obj {
		public double num;

		// =cls "Data" Num Obj
		Num(Terp t, double num) {
			super(t.tNum);
			if (Double.isNaN(num)) {
				toss("NotANumber");
			}
			if (Double.isInfinite(num)) {
				toss("IsInfinite");
			}
			this.num = num;
		}

		public Num asNum() {
			return this;
		}

		public String toString() {
			long truncated = (long) num;
			if (num == truncated) {
				return fmt("%d", truncated);
			} else {
				return fmt("%s", num);
			}
		}

		@Override
		Comparable innerValue() {
			return new Double(num);
		}

		public void visit(Visitor v) {
			v.visitNum(this);
		}

		// =meth Num "access" fmt:
		// "format with Java floating point format string"
		public String fmt_(String s) {
			return fmt(s, num);
		}

		// =meth Num "access" chr
		// "covert integer to single-char Str with that unicode codepoint"
		public String _chr() {
			return new Character((char) toNearestInt()).toString();
		}

		// Most objects are true.
		// nil and Nums that round to 0 are false.
		@Override
		public boolean truth() {
			return toNearestInt() != 0;
		}

		// TODO -- get rid of eq: ne: etc. names --

		// =meth Num "binop" ==,eq: "eq two Nums"
		public Num _eq_(Num a) {
			return this.num == a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" !=,ne: "ne two Nums"
		public Num _ne_(Num a) {
			return this.num != a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" <,lt: "lt two Nums"
		public Num _lt_(Num a) {
			return this.num < a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" <=,le: "le two Nums"
		public Num _le_(Num a) {
			return this.num <= a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" >,gt: "gt two Nums"
		public Num _gt_(Num a) {
			return this.num > a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" >=,ge: "ge two Nums"
		public Num _ge_(Num a) {
			return this.num >= a.num ? cls.terp.instTrue : cls.terp.instFalse;
		}

		// =meth Num "binop" + "add two Nums"
		public Num _pl_(Num a) {
			return terp().newNum(this.num + a.num);
		}

		// =meth Num "binop" - "add two Nums"
		public Num _mi_(Num a) {
			return terp().newNum(this.num - a.num);
		}

		// =meth Num "binop" | "bitwise-or two Nums as 32bit integers"
		public Num _or_(Num a) {
			return terp().newNum((int) this.num | (int) a.num);
		}

		// =meth Num "binop" ^ "bitwise-xor two Nums as 32bit integers"
		public Num _xo_(Num a) {
			return terp().newNum((int) this.num ^ (int) a.num);
		}

		// =meth Num "binop" * "multiply two Nums"
		public Num _ti_(Num a) {
			return terp().newNum(this.num * a.num);
		}

		// =meth Num "binop" / "divide two Nums"
		public Num _di_(Num a) {
			return terp().newNum(this.num / a.num);
		}

		// =meth Num "binop" % "modulo two Nums"
		public Num _mo_(Num a) {
			double z = this.num % a.num;
			if (z < 0) {
				z = z + a.num;  // Positive residues are more useful.
			}
			return terp().newNum(z);
		}

		// =meth Num "binop" & "bitwise-and two Nums as 32bit integers"
		public Num _an_(Num a) {
			return terp().newNum((int) this.num & (int) a.num);
		}

		// =meth NumCls "num" rand "Random float between 0 and 1."
		public static Num rand(Terp terp) {
			return terp.newNum(Math.random());
		}

		// =meth NumCls "num" rand: "Random integer between 0 and n-1."
		public static Num rand_(Terp terp, int n) {
			return terp.newNum(Math.floor(Math.random() * n));
		}

		// =meth Num "num" range "vec of ints from 0 to self - 1"
		public Vec _range() {
			Terp t = terp();
			int n = this.toNearestInt();
			Ur[] arr = new Ur[n];
			for (int i = 0; i < n; i++) {
				arr[i] = t.newNum(i);
			}
			return new Vec(t, arr);
		}

		// =meth Num "num" do:
		// "do the block self times, passing 1 arg, from 0 to self-1"
		public void do_(Blk blk) {
			Terp t = terp();
			double stop = this.toNearestInt();
			for (int i = 0; i < stop; i++) {
				blk.evalWith1Arg(new Num(t, i));
			}
		}

		// =meth Num "convert" num
		public Num _num() {
			return this;
		}

		// =meth Num "convert" neg
		public Num _neg() {
			return new Num(cls.terp, -num);
		}

		// =meth Num "math" sgn
		public Num _sgn() {
			return new Num(cls.terp, num < 0 ? -1 : num > 0 ? 1 : 0);
		}

		// =meth Num "convert" int
		public Num _int() {
			return new Num(cls.terp, (int) num);
		}

		// =meth Num "convert" floor
		public Num _floor() {
			return new Num(cls.terp, Math.floor(num));
		}

		// =meth Num "convert" round
		public Num _round() {
			return new Num(cls.terp, Math.floor(num + 0.5));
		}

		// =meth Num "math" abs
		public Num _abs() {
			return new Num(cls.terp, num < 0 ? -num : num);
		}

		// =meth Num "math" sq
		public Num _sq() {
			return new Num(cls.terp, num*num);
		}

		// =meth Num "math" sqrt 
		public Num _sqrt() {
			return new Num(cls.terp, Math.sqrt(num));
		}

		// =meth Num "math" sin
		public Num _sin() {
			return new Num(cls.terp, Math.sin(num));
		}

		// =meth Num "math" cos
		public Num _cos() {
			return new Num(cls.terp, Math.cos(num));
		}

		// =meth Num "math" tan
		public Num _tan() {
			return new Num(cls.terp, Math.tan(num));
		}

		// =meth Num "math" asin
		public Num _asin() {
			return new Num(cls.terp, Math.asin(num));
		}

		// =meth Num "math" acos
		public Num _acos() {
			return new Num(cls.terp, Math.acos(num));
		}

		// =meth Num "math" atan
		public Num _atan() {
			return new Num(cls.terp, Math.atan(num));
		}

		// =meth Num "math" sinh
		public Num _sinh() {
			return new Num(cls.terp, Math.sinh(num));
		}

		// =meth Num "math" cosh
		public Num _cosh() {
			return new Num(cls.terp, Math.cosh(num));
		}

		// =meth Num "math" tanh
		public Num _tanh() {
			return new Num(cls.terp, Math.tanh(num));
		}

		// =meth Num "math" ln
		public Num _ln() {
			return new Num(cls.terp, -num);
		}

		// =meth Num "math" log10
		public Num _log10() {
			return new Num(cls.terp, -num);
		}

		// =meth Num "math" exp
		public Num _exp() {
			return new Num(cls.terp, -num);
		}

		// =meth NumCls "math" pi
		public static Num _pi(Terp terp) {
			return new Num(terp, Math.PI);
		}

		// =meth NumCls "math" tau
		public static Num _tau(Terp terp) {
			return new Num(terp, 2 * Math.PI);
		}

		// =meth NumCls "math" e
		public static Num _e(Terp terp) {
			return new Num(terp, Math.E);
		}

		// =meth Num "math" idiv:
		public Num idiv_(Num a) {
			return new Num(cls.terp, (long) num / (long) a.num);
		}

		// =meth Num "math" imod:
		public Num imod_(Num a) {
			return new Num(cls.terp, (long) num % (long) a.num);
		}

		// =meth Num "math" pow:
		public Num pow_(Num a) {
			return new Num(cls.terp, Math.pow(num, a.num));
		}
	}

	public final static class Buf extends Obj {
		StringBuffer buf;

		// =cls "Data" Buf Obj
		Buf(Terp t) {
			super(t.tBuf);
			this.buf = new StringBuffer();
		}

		Buf(Terp t, String s) {
			super(t.tBuf);
			this.buf = new StringBuffer(s);
		}

		public String toString() {
			return buf.toString();
		}

		// =meth Buf "access" append:,ap:
		public Buf append_(Obj a) {
			this.buf.append(a.toString());
			return this;
		}

		// =meth BufCls "new" new
		public static Buf cls_new(Terp t) {
			return new Buf(t);
		}

		// =meth BufCls "new" append:,ap:
		public static Buf cls_append_(Terp t, Obj a) {
			return new Buf(t, a.toString());
		}
	}

	public final static class Str extends Obj {
		public String str;

		// =cls "Data" Str Obj
		Str(Terp t, String str) {
			super(t.tStr);
			this.str = str;
		}

		public Str asStr() {
			return this;
		}

		@Override
		public boolean truth() {
			return str.length() > 0;
		}

		public String repr() {
			if (str == null) {
				return "<?NULL?>";
			}
			return fmt("'%s'", str.replaceAll("'", "''"));
		}

		public String toString() {
			return str;
		}

		@Override
		Comparable innerValue() {
			return str;
		}

		public void visit(Visitor v) {
			v.visitStr(this);
		}

		// =meth Str "access" applySubstitutions ""
		public Str applySubstitutions() {
			return terp().newStr(Parser.charSubsts(this.str));
		}

		// =meth Str "access" ord
		// "unicode codepoint number of first char in Str"
		public int ord() {
			return str.charAt(0);
		}
		
		// =meth Str "access" explode "explode into Vec of numbers"
		public Vec _explode() {
			Vec z = new Vec(terp());
			for (int i = 0; i < str.length(); i++) {
				z.vec.add(new Num(terp(), str.charAt(i)));
			}
			return z;
		}

		abstract static class BinaryStrPredMeth extends JavaMeth {
			BinaryStrPredMeth(Terp terp, String name) {
				super(terp.tStr, name, null, "");
			}

			public Ur apply(Frame f, Ur r, Ur[] args) {
				assert args.length == 1;
				Str s = (Str) r;
				Str a = args[0].asStr();
				if (a == null) {
					toss("Argument of Str relop not a Str");
				}
				return terp().boolObj(compute(s.str, a.str));
			}

			abstract boolean compute(String sstr, String astr);
		}

		static void addBuiltinMethodsForStr(final Terp terp) {
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "split:", "",
					"Split string into a Vec, using arg as delimiter.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					assert args.length == 1;
					Str s = (Str) r;
					Str a = args[0].asStr();
					if (a == null) {
						toss("Argument of Str>>spli: not a Str");
					}
					// Using Java's split caused infinite recursion in
					// Pattern:
					// String[] parts =
					// s.str.split(Pattern.quote(a.str));
					String[] parts = splitNonEmpty(s.str, a.str.charAt(0));
					Ur[] arr = new Ur[parts.length];
					for (int i = 0; i < parts.length; i++) {
						arr[i] = terp.newStr(parts[i]);
					}
					return terp.newVec(arr);
				}
			});
			terp.tStr
					.addMethod(new JavaMeth(terp.tStr, "append:", "ap:",
							"Append the argument (as a string) to the string, modifying self.") {
						public Ur apply(Frame f, Ur r, Ur[] args) {
							assert args.length == 1;
							Str s = (Str) r;
							Str a = args[0].asStr();
							if (a == null) {
								toss("Argument of Str>>ap: not a Str");
							}
							return new Str(terp, s.str + a.str);
						}
					});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "substr:to:", "ss:to:",
					"Substring starting at first index, "
							+ "ending before second index, like Java substr.") { // Substring
						public Ur apply(Frame f, Ur r, Ur[] args) {
							assert args.length == 2;
							Str s = (Str) r;
							assert (s != null);
							int a = args[0].toNearestInt();
							int b = args[1].toNearestInt();
							return new Str(terp, s.str.substring(a, b));
						}
					});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "head", "hd",
					"First char of Str.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					Str self = (Str) r;
					if (self.str.length() == 0) {
						toss("Cannot take head of empty Str");
					}
					return terp.newStr(self.str.substring(0, 1));
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "tail", "tl",
					"All but first char of Str.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					Str self = (Str) r;
					if (self.str.length() == 0) {
						toss("Cannot take tail of empty Str");
					}
					return terp.newStr(self.str.substring(1));
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "length", "len",
					"Length of Str.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					return terp.newNum(((Str) r).str.length());
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "toNumber", "num",
					"Convert ASCII Str to Num") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					Str self = (Str) r;
					double x = 0;
					try {
						x = new Float(self.str);
					} catch (NumberFormatException ex) {
						toss("Cannot convert Str to Num: <%s>", self.str);
					}
					return terp.newNum(x);
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "lower", "low",
					"Convert string to lowercase.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					return terp.newStr(((Str) r).str.toLowerCase());
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "upper", "upp",
					"Convert string to uppercase.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					return terp.newStr(((Str) r).str.toUpperCase());
				}
			});
			terp.tStr.addMethod(new JavaMeth(terp.tStr, "trimWhite", "trimw",
					"Trim whitespace from front and back.") {
				public Ur apply(Frame f, Ur r, Ur[] args) {
					return terp.newStr(((Str) r).str.trim());
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "eq:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) == 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "ne:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) != 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "lt:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) < 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "le:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) <= 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "gt:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) > 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "ge:") {
				boolean compute(String sstr, String astr) {
					return sstr.compareTo(astr) >= 0;
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "starts:") {
				boolean compute(String sstr, String astr) {
					return sstr.startsWith(astr);
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "ends:") {
				boolean compute(String sstr, String astr) {
					return sstr.endsWith(astr);
				}
			});
			terp.tStr.addMethod(new BinaryStrPredMeth(terp, "matchp:") {
				boolean compute(String sstr, String astr) {
					return Pattern.matches(astr, sstr);
				}
			});
		}
	}

	public final static class Rex extends Obj {
		public Pattern p;

		// =cls "data" Rex Obj
		public Rex(Terp t, String pattern) {
			super(t.wrap.clsRex);
			this.p = Pattern.compile(pattern);
		}

		// =meth RexCls "new" new:
		public static Rex new_(Terp t, String pat) {
			return new Rex(t, pat);
		}

		@Override
		public String toString() {
			return "Rex new: " + terp().newStr(p.toString()).repr();
		}

		// =meth Rex "rex" match:
		public Ur match(String s) {
			Matcher m = p.matcher(s);
			if (m.lookingAt()) {
				Vec v = new Vec(terp());
				int n = m.groupCount();
				for (int i = 0; i <= n; i++) {
					v.vec.add(terp().newStr(m.group(i)));
				}
				return v;
			} else {
				return terp().instNil;
			}
		}
	}

	public final static class Vec extends Obj {
		public ArrayList<Ur> vec;

		// =cls "Data" Vec Obj
		public Vec(Terp t) {
			super(t.tVec);
			this.vec = new ArrayList<Ur>();
		}

		public Vec(Terp t, Ur[] arr) {
			super(t.tVec);
			this.vec = new ArrayList<Ur>();
			for (int i = 0; i < arr.length; i++) {
				this.vec.add(arr[i]);
			}
		}

		public Vec(Terp t, int[] arr) {
			super(t.tVec);
			this.vec = new ArrayList<Ur>();
			for (int i = 0; i < arr.length; i++) {
				this.vec.add(t.newNum(arr[i]));
			}
		}

		public Vec asVec() {
			return this;
		}

		@Override
		public boolean truth() {
			return vec.size() > 0;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("VEC(");
			for (int i = 0; i < vec.size(); i++) {
				assert vec.get(i) != null;
				sb.append(vec.get(i) == null ? " nil\"NULL\" " : vec.get(i)
						.repr());
				sb.append("; ");
			}
			sb.append(") ");
			return sb.toString();
		}

		public void visit(Visitor v) {
			v.visitVec(this);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Vec) {
				Vec that = (Vec) o;
				int sz = this.vec.size();
				if (sz == that.vec.size()) {
					for (int i = 0; i < sz; i++) {
						if (!this.vec.get(i).equals(that.vec.get(i))) {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof Vec) {
				Vec that = (Vec) o;
				int sz = this.vec.size();
				if (sz == that.vec.size()) {
					for (int i = 0; i < sz; i++) {
						int z = this.vec.get(i).compareTo(that.vec.get(i));
						if (z != 0) {
							return z;
						}
					}
					return 0;
				} else {
					return new Integer(this.vec.size()).compareTo(new Integer(
							that.vec.size()));
				}
			} else if (o instanceof Ur) {
				// Within our Ur world, order by classname, providing stability
				// after filein/fileout.
				Ur that = (Ur) o;
				return this.cls.cname.compareTo(that.cls.cname);
			} else {
				// Against foreign objects, fall back to identity hash.
				return new Integer(System.identityHashCode(this))
						.compareTo(new Integer(System.identityHashCode(o)));
			}
		}

		public int hashCode() {
			int z = 0;
			int sz = this.vec.size();
			for (int i = 0; i < sz; i++) {
				z = 13 * z + this.vec.get(i).hashCode();
			}
			return z;
		}

		int toNearestIndex(Ur a) {
			final int vlen = vec.size();
			if (vlen == 0) {
				toss("Cannot index into empty Vec");
			}
			Num num = a.asNum();
			if (num == null) {
				toss("Index is a %s, not a Num, in <Vec at:>", a.cls.cname);
			}
			int i = (int) Math.floor(num.num + 0.5); // closest int
			i = ((i % vlen) + vlen) % vlen; // Positive-only Modulus
			return i;
		}

		// =meth Vec "access" len "return length of the Vec"
		public Num _len() {
			return cls.terp.newNum(vec.size());
		}

		// =meth Vec "access" at: "get element at index a, modulo length of Vec"
		public Ur at_(Num a) {
			return vec.get(toNearestIndex(a));
		}

		// =meth Vec "access" at:put:,at:p:
		// "put element b at given a, modulo length of Vec"
		public void at_put_(Num a, Ur b) {
			vec.set(toNearestIndex(a), b);
		}

		// =meth Vec "access" append:,ap: "add new element ato end of Vec"
		public void append_(Ur a) {
			vec.add(a);
		}
		
		// =meth Vec "access" cat: "concat with vector, changing me."
		public void cat_(Vec a) {
			final int n = a.vec.size();
			for (int i = 0; i < n; ++i) {
				this.vec.add(a.vec.get(i));
			}
		}

		// =meth Vec "string" join:
		// "Join strings with given string."
		public Str join(String a) {
			StringBuilder sb = new StringBuilder();
			final int n = vec.size();
			for (int i = 0; i < n; i++) {
				if (i > 0) {
					sb.append(a);
				}
				sb.append(vec.get(i).toString());
			}
			return terp().newStr(sb.toString());
		}

		// =meth Vec "string" join
		// "Join strings with spaces."
		public Str join() {
			StringBuilder sb = new StringBuilder();
			final int n = vec.size();
			for (int i = 0; i < n; i++) {
				if (i > 0) {
					sb.append(' ');
				}
				sb.append(vec.get(i).toString());
			}
			return terp().newStr(sb.toString());
		}

		// =meth Vec "string" jam
		// "Join strings with no separator char."
		public Str jam() {
			StringBuilder sb = new StringBuilder();
			final int n = vec.size();
			for (int i = 0; i < n; i++) {
				sb.append(vec.get(i).toString());
			}
			return terp().newStr(sb.toString());
		}

		// =meth Vec "string" implode,imp
		// "Implode strings and ints (as chars) and subvectors."
		public Str implode() {
			StringBuilder sb = new StringBuilder();
			implodeRecursive(sb);
			return terp().newStr(sb.toString());
		}

		private void implodeRecursive(StringBuilder sb) {
			final int n = vec.size();
			for (int i = 0; i < n; i++) {
				Ur x = vec.get(i);
				if (x instanceof Num) {
					char ch = (char) (((Num) x).toNearestInt());
					sb.append(ch);
				} else if (x instanceof Vec) {
					((Vec) x).implodeRecursive(sb);
				} else {
					sb.append(x.toString());
				}
			}
		}

		// =meth Vec "control" doWithEach:,do:
		// "Iterate the block with one argument, for each item in self."
		public Undefined doWithEach_(Blk b) {
			int n = this.vec.size();
			for (int i = 0; i < n; i++) {
				b.evalWith1Arg(this.vec.get(i));
			}
			return terp().instNil;
			
		}

		// =meth VecCls "access" new "create a new, empty Vec"
		public static Vec cls_new(Terp terp) {
			return new Vec(terp);
		}


		// =meth VecCls "access" append:,ap: "add element to a new Vec"
		public static Vec cls_append_(Terp terp, Ur a) {
			Vec z = new Vec(terp);
			z.vec.add(a);
			return z;
		}
		
		// =meth Vec "math" dot: "Dot product of two numerical Vecs"
		public Vec dot_(Vec a) {
			int nthis = this.vec.size();
			int na = a.vec.size();
			int n = (nthis < na) ? nthis : na;  // Minimum.
			Vec z = new Vec(terp());
			for (int i = 0; i < n; i++) {
				double x = this.vec.get(i).mustNum().num;
				double y = a.vec.get(i).mustNum().num;
				z.vec.add(terp().newNum(x*y));
			}
			return z;
		}
		
		// =meth Vec "math" cross: "Cross product of two 3-element numerical Vecs"
		public Vec cross_(Vec b) {
			int nthis = this.vec.size();
			int na = b.vec.size();
			if (nthis != 3 || na != 3) {
				toss("Expected 3 elements in Vec for crossproduct: %d %d", nthis, na);
				return null;  // NOTREACHED
			}
			double a1 = this.vec.get(0).mustNum().num;
			double a2 = this.vec.get(1).mustNum().num;
			double a3 = this.vec.get(2).mustNum().num;
			double b1 = b.vec.get(0).mustNum().num;
			double b2 = b.vec.get(1).mustNum().num;
			double b3 = b.vec.get(2).mustNum().num;
			Vec z = terp().newVec(emptyInts);
			z.vec.add(terp().newNum(a2*b3 - a3*b2));
			z.vec.add(terp().newNum(a3*b1 - a1*b3));
			z.vec.add(terp().newNum(a1*b2 - a2*b1));
			return z;
		}
		

		// =meth Vec "math" abs
		// "Absolute euclidian length of numerical vector"
		public double _abs() {
			int nthis = this.vec.size();
			double x = 0;
			for (int i = 0; i < nthis; i++) {
				double e = this.vec.get(i).mustNum().num;
				x += e * e;
			}
			return Math.sqrt(x);
		}

		// =meth Vec "math" unit
		// "Return vector in same direction but unit length"
		public Vec _unit() {
			double abs = this._abs();
			if (abs == 0.0) {
				toss("Cannot unit a Vec with abs len 0");
			}
			Vec z = terp().newVec(emptyInts);
			int nthis = this.vec.size();
			for (int i = 0; i < nthis; i++) {
				double e = this.vec.get(i).mustNum().num;
				z.vec.add(terp().newNum(e / abs));
			}
			return z;
		}
			
	}

	public final static class Dict extends Obj {
		public HashMap<Ur, Ur> dict;

		// =cls "Data" Dict Obj
		Dict(Terp t) {
			super(t.tDict);
			this.dict = new HashMap<Ur, Ur>();
		}

		public Dict asDict() {
			return this;
		}

		@Override
		public boolean truth() {
			return dict.size() > 0;
		}

		public String toString() {
			Vec[] aarr = sortedAssocs();
			StringBuffer sb = new StringBuffer("DICT(");
			for (Vec a : aarr) {
				sb.append("(");
				sb.append(a.vec.get(0).repr());
				sb.append("), (");
				sb.append(a.vec.get(1).repr());
				sb.append("); ");
			}
			sb.append(") ");
			return sb.toString();
		}

		public boolean equals(Object o) {
			if (o instanceof Dict) {
				Dict that = (Dict) o;
				int sz = this.dict.size();
				if (sz == that.dict.size()) {
					Vec[] a = this.sortedAssocs();
					Vec[] b = that.sortedAssocs();
					for (int i = 0; i < sz; i++) {
						if (!a[i].vec.get(0).equals(b[i].vec.get(0))
								|| !a[i].vec.get(1).equals(b[i].vec.get(1))) {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		public int hashCode() {
			int z = 0;
			Vec[] a = this.sortedAssocs();
			for (int i = 0; i < a.length; i++) {
				z = 13 * z + a[i].vec.get(0).hashCode();
				z = 13 * z + a[i].vec.get(1).hashCode();
			}
			return z;
		}

		public void visit(Visitor v) {
			v.visitDict(this);
		}

		public Vec[] sortedAssocs() {
			Vec[] z = new Vec[dict.size()];
			int i = 0;
			for (Ur key : dict.keySet()) {
				z[i] = new Vec(terp(), urs(key, dict.get(key)));
				++i;
			}
			Arrays.sort(z);
			return z;
		}
		
		// =meth Dict "access" len "number of entries in the Dict"
		public int _len() {
			return dict.size(); 
		}
		
		// =meth Dict "access" dir "list keys in the Dict"
		public Vec _dir() {
			Vec z = new Vec(terp());
			for (Vec aa : this.asDict().sortedAssocs()) {
				z.vec.add(aa.vec.get(0));
			}
			return z;
		}
		// =meth Dict "access" at:
		public Ur at_(Ur key) {
			Ur z = dict.get(key);
			return terp().nullToNil(z);
		}
		// =meth Dict "access" at:put:
		public void at_put_(Ur key, Ur value) {
			dict.put(key,  value);
		}
		// =meth DictCls "new" new
		public static Dict _new(Terp t) {
			return new Dict(t);
		}
	}

	public static class Html extends Static { // For XSS Safety.
		private StringBuffer sb;

		public Html() {
			this.sb = new StringBuffer();
		}

		public Html(Html that) {
			this.sb = new StringBuffer(that.sb.toString());
		}

		public Html(String s) {
			this.sb = new StringBuffer(Static.htmlEscape(s));
		}

		public String toString() {
			return sb.toString();
		}

		public Html append(String s) {
			sb.append(htmlEscape(s));
			return this;
		}

		public Html append(Html that) {
			sb.append(that.toString());
			return this;
		}

		public Html appendLink(String link, String label) {
			Html.tag(this, "a", strs("href", link), label);
			return this;
		}

		public Html append(Ur that) {
			sb.append(htmlEscape(that.toString()));
			return this;
		}

		static public Html entity(String name) {
			Html ht = new Html();
			ht.sb.append(fmt("&%s;", name));
			return ht;
		}

		static public Html tag(Html appendMe, String type, String[] args,
				String body) {
			return tag(appendMe, type, args, new Html(body));
		}

		static public Html tag(Html appendMe, String type, String[] args,
				Html body) {
			Html z = appendMe == null ? new Html() : appendMe;
			assert htmlTagP.matcher(type).matches();
			z.sb.append(fmt("<%s ", type));
			if (args != null) {
				for (int i = 0; i < args.length; i += 2) {
					assert htmlTagP.matcher(args[i]).matches();
					z.sb.append(fmt("%s=\"%s\" ", args[i],
							htmlEscape(args[i + 1])));
				}
			}
			z.sb.append(fmt("\n>%s</%s\n>", body, type));
			return z;
		}

		static public Html tag(Html appendMe, String type, String[] args) {
			Html z = appendMe == null ? new Html() : appendMe;
			assert htmlTagP.matcher(type).matches();
			z.sb.append(fmt("<%s ", type));
			if (args != null) {
				for (int i = 0; i < args.length; i += 2) {
					assert htmlTagP.matcher(args[i]).matches();
					z.sb.append(fmt("%s=\"%s\" ", args[i],
							htmlEscape(args[i + 1])));
				}
			}
			z.sb.append("\n/>");
			return z;
		}
	}

	public static class Ht extends Obj {
		Html html;

		// =cls "html" Ht Obj
		public Ht(Terp terp) {
			super(terp.wrap.clsHt);
			html = new Html();
		}

		public Ht(Terp terp, Html x) {
			super(terp.wrap.clsHt);
			html = new Html(x);
		}

		@Override
		public String toString() {
			return html.toString();
		}

		// =meth Ht "html" append:,ap: "take an Ht or a Str"
		public void append(Ur x) {
			if (x instanceof Ht) {
				html.append(((Ht) x).html);
			} else {
				html.append(x.toString());
			}
		}

		// =meth HtCls "html" new: "take an Ht or a Str"
		public static Ht new_(Terp t, Ur a) {
			Ht z = new Ht(t);
			z.append(a);
			return z;
		}

		// =meth HtCls "html" entity:
		public static Ht entity(Terp t, String name) {
			return new Ht(t, Html.entity(name));
		}

		// =meth HtCls "html" tag:params:body:
		public static Ht tag(Terp t, String name, Ur params, Ur body) {
			String[] args = emptyStrs;
			if (params == t.instNil) {
				// empty args
			} else {
				Vec v = params.mustVec();
				int n = v.vec.size();
				for (int i = 0; i < n; i++) {
					Vec kv = v.vec.get(i).mustVec();
					if (kv.vec.size() != 2) {
						t.toss("Subvec not size 2, in params to tag: <%s>", kv);
					}
					args = append(args, kv.vec.get(0).toString());
					args = append(args, kv.vec.get(1).toString());
				}
			}
			Html bodyHt;
			if (body == t.instNil) {
				bodyHt = new Html();
			} else if (body instanceof Ht) {
				bodyHt = new Html(((Ht) body).html);
			} else {
				bodyHt = new Html(body.toString());
			}
			return new Ht(t, Html.tag(null, name, args, bodyHt));
		}
	}

	public static class Visitor {
		protected Terp t;

		public Visitor(Terp t) {
			super();
			this.t = t;
		}

		public void visitUr(Ur a) {
			t.toss("SubclassResponsibility(Visitor::visitUr)");
		}

		public void visitCls(Cls a) {
			visitUr(a);
		}

		public void visitNum(Num a) {
			visitUr(a);
		}

		public void visitStr(Str a) {
			visitUr(a);
		}

		public void visitUndefined(Undefined a) {
			visitUr(a);
		}

		public void visitVec(Vec a) {
			visitUr(a);
		}

		public void visitDict(Dict a) {
			visitUr(a);
		}

		public void visitUsr(Usr a) {
			visitUr(a);
		}
	}
}
