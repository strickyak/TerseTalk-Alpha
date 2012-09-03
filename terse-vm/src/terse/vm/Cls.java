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

import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import terse.vm.Expr.MethTop;
import terse.vm.Ur.Obj;
import terse.vm.Usr.UsrCls;
import terse.vm.Terp.Frame;

public class Cls extends Obj {
	// =cls "sys" Cls Obj

	public final Terp terp;
	public final String cname;
	public Cls supercls; // tObjCls.supercls will be patched up, so not final.
	final HashMap<String, Meth> meths;
	public boolean trace;
	long countInstances;
	HashMap<String, Integer> allVarMap; // Calculated.
	String[] myVarNames; // Authoritative.
	SortedSet<String> subclasses; // keep them lower.

	static int generationCounter = 1;
	public int generation;

	public Cls(Cls cls, Terp terp, String name, Cls supercls) {
		super(cls);
		this.terp = terp;
		this.cname = name;
		this.supercls = supercls;
		this.meths = new HashMap<String, Meth>();
		this.trace = false;
		this.countInstances = 0;
		this.subclasses = new TreeSet<String>();
		this.myVarNames = emptyStrs;

		String key = name.toLowerCase();
		if (terp.clss.containsKey(key)) {
			Ur existing = terp.clss.get(name);
			if (existing instanceof Cls) {
				terp.toss("Class named <%s> already exists",
						((Cls) existing).cname);
			} else {
				terp.toss("Object named <%s> already exists in Globals: <%s>",
						name, existing.toString());
			}
		}
		terp.clss.put(name.toLowerCase(), this);

		this.allVarMap = new HashMap<String, Integer>();
		if (supercls != null) {
			// Object has no supercls, and during bootstrapping it can be null.
			supercls.subclasses.add(this.cname.toLowerCase());
			recalculateAllVars(); // Not used.
		}
	}
	
	// =meth ClsCls "access" at:
	public static Ur at__(Terp terp, String s) {
		return terp.nullToNil(terp.clss.get(s.toLowerCase()));
	}

	
	// =meth Cls "access" at:
	public Ur at_(String s) {
		return terp.nullToNil(meths.get(s.toLowerCase()));
	}

	private void advanceGeneration() {
		this.generation = generationCounter;
		++generationCounter;
	}

	private void recalculateAllVars() {
		this.allVarMap.clear();
		if (supercls != null) {
			this.allVarMap.putAll(supercls.allVarMap);
		}
		int varNum = this.allVarMap.size();
		for (String k : myVarNames) {
			if (this.allVarMap.containsKey(k)) {
				toss("Duplicate inst var <%s> already exists in a superclass of %s", k, this);
			}
			this.allVarMap.put(k, varNum);
			++varNum;
		}
	}

	// Send this to tPro, and let it recurse.
	void recalculateAllVarsHereAndBelow() {
		// First do my own (trusting that supercls.allVars is correct).
		recalculateAllVars();
		// Bust cache.
		// TODO: Right idea, but wrong method. Really we need to recompile all
		// methods to get correct field indices.
		advanceGeneration();
		// Recurse to subclasses.
		for (String s : subclasses) {
			Ur p = terp.clss.get(s.toLowerCase());
			if (p == null) {
				terp.toss("Cls <%s> not found in globals", s);
			}
			Cls c = p.asCls();
			if (c == null) {
				terp.toss("Cls <%s> is not a Cls in globals", s);
			}
			c.recalculateAllVarsHereAndBelow();
		}
	}

	public Cls asCls() {
		return this;
	}

	public String toString() {
		return cname;
	}

	void addMethod(Meth m) {
		assert m != null;
		assert m.abbrev != null;
		assert meths != null;
		if (m.abbrev.length() > 0) {
			meths.put(m.abbrev.toLowerCase(), m);
		}
		meths.put(m.name.toLowerCase(), m);
		advanceGeneration();
		
		if (m instanceof UsrMeth) {
			String[] lines = ((UsrMeth) m).src.split("\n");
			try {
				terp.appendWorldFile(fmt("meth %s %s", cname, m.name), lines);
				if (m.abbrev.length() > 0 && !m.abbrev.equals(m.name)) {
					terp.appendWorldFile(fmt("meth %s %s", cname, m.abbrev),
							lines);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				terp.toss("Cannot write image file: " + e);
			}
		}
	}

	// =meth Cls "new" defSub:
	public Cls defineSubclass(String newname) {
		if (newname.endsWith("Cls")) {
			terp.toss(
					"Don't define classes ending with 'Cls' using defineClass: name=<%s>",
					newname);
		}
		if (this.cls == terp().tMetacls) {
			terp.toss(
					"Don't define classes on Metaclasses using defineClass: me=<%s>",
					this.cls.cname);
		}
		if (terp.clss.containsKey(newname.toLowerCase())) {
			terp.toss("Name <%s> already exists in Global Frame", newname);
		}
		if (terp.clss.containsKey(newname.toLowerCase() + "cls")) {
			terp.toss("Name <%sCls> already exists in Global Frame", newname);
		}
		Cls newMetaCls = new Cls(terp.tMetacls, terp, newname + "Cls", this.cls);
		Cls newCls = (this instanceof Usr.UsrCls) ? new Usr.UsrCls(newMetaCls,
				terp, newname, this) : new Cls(newMetaCls, terp, newname, this);
		terp.clss.put(newMetaCls.cname.toLowerCase(), newMetaCls);
		terp.clss.put(newCls.cname.toLowerCase(), newCls);

		try {
			terp.appendWorldFile(fmt("cls %s %s", newname, cname), null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			terp.toss("Cannot write image file: " + e);
		}

		return newCls;
	}

	// =meth Cls "access" allMeths
	public Dict allMeths() {
		Dict z = new Dict(terp);
		for (String s : meths.keySet()) {
			z.dict.put(terp.newStr(s), meths.get(s));
		}
		return z;
	}
	
	// =meth Cls "access" vars
	public Vec _vars() {
		return terp.mkStrVec(myVarNames);
	}

	// =meth Cls "access" defVars:
	public void defVars_(String varNames) {
		String[] myVarNames = filterOutEmptyStrings(varNames.split("\\s+"));

		// Check for Ignore-Case uniqueness.
		for (int i = 0; i < myVarNames.length; i++) {
			String iStr = myVarNames[i];
			String iLow = iStr.toLowerCase();
			for (Cls c = this.supercls; c != null; c = c.supercls) {
				for (String y : c.myVarNames) {
					if (iLow.equals(y.toLowerCase())) {
						terp.toss(
								"Cannot add inst var <%s> because Cls <%s> also has <%s>",
								iStr, c.cname, y);
					}
				}
				for (int j = i + 1; j < myVarNames.length; j++) {
					if (iLow.equals(myVarNames[j].toLowerCase())) {
						terp.toss(
								"Cannot add 2 inst vars <%s> <%s> with same name.",
								iStr, myVarNames[j]);
					}
				}
			}
		}

		this.myVarNames = myVarNames;
		terp.tUr.recalculateAllVarsHereAndBelow();

		if (terp.loadingWorldFile) {
			// don't write world file if loading world file.
		} else {
			try {
				terp.appendWorldFile(fmt("instvars %s", this.cname),
						Terp.strs(varNames));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				terp.toss("Cannot write image file: " + e);
			}
		}
	}

	// =meth Cls "access" name
	public String _name() {
		return cname;
	}

	// =meth Cls "access" superCls,sup
	public Cls _superCls() {
		return supercls;
	}

	// =meth Cls "access" meths
	public Dict _meths() {
		Dict z = new Dict(terp);
		for (String s : meths.keySet()) {
			z.dict.put(terp.newStr(s), meths.get(s));
		}
		return z;
	}

	// =meth Cls "meth" defineMethod:abbrev:doc:code:,defMeth:a:d:c: ""
	public void defMeth(String methName, String abbrev, String doc, String code) {
		Expr.MethTop top = Parser.parseMethod(this, methName, code);
		Meth m = new UsrMeth(this, methName, abbrev, doc, code, top);
		addMethod(m);
	}

	// =meth Cls "meth" trace: ""
	public void trace_(boolean a) {
		this.trace = a;
	}

	// =meth ClsCls "meth" all "dict of all classes, by name"
	public static Dict _all(Terp terp) {
		Dict z = new Dict(terp);
		int i = 0;
		for (String k : terp.clss.keySet()) {
			Ur key = terp.newStr(k);
			Ur value = terp.clss.get(k);
			z.dict.put(key, value);
			++i;
		}
		return z;
	}

	public static abstract class Meth extends Obj {
		// =cls "sys" Meth Obj

		// =get Meth . onCls onCls
		Cls onCls;
		// =get Meth String name name
		String name;
		// =get Meth String abbrev abbrev
		String abbrev;
		// =get Meth String doc doc
		String doc;

		boolean trace;
		long countMessages;

		public Meth(Cls cls, Cls onCls, String name, String abbrev, String doc) {
			super(cls);
			this.onCls = onCls;
			this.name = name;
			this.abbrev = abbrev == null || abbrev.length() == 0 ? name
					: abbrev;
			this.doc = doc;
			this.trace = false;
			this.countMessages = 0;
			assert this.name != null;
			assert this.abbrev != null;

		}

		public abstract Ur apply(Frame f, Ur r, Ur[] args);
	}

	/**
	 * Meth implemented in Java. Notice that the CTOR automatically adds the
	 * method to onCls!
	 */
	public static abstract class JavaMeth extends Meth {
		// =cls "sys" JavaMeth Meth
		public JavaMeth(Cls onCls, String name, String abbrev) {
			super(onCls.terp.tJavaMeth, onCls, name, abbrev, "");
			onCls.addMethod(this); // NOTA BENE
		}

		public JavaMeth(Cls onCls, String name, String abbrev, String doc) {
			super(onCls.terp.tJavaMeth, onCls, name, abbrev, doc);
		}
		// =meth Meth "eval" applyFrame:receiver:args:
		public abstract Ur apply(Frame f, Ur r, Ur[] args);

		public String toString() {
			return fmt("\"<%s %s> Built-In Method\"", onCls, name);
		}
	}

	public static class UsrMeth extends Meth {

		// =get UsrMeth String src src
		public String src;
		public Expr.MethTop um_top;

		// =cls "sys" UsrMeth Meth
		public UsrMeth(Cls onCls, String name, String abbrev, String doc,
				String src, Expr.MethTop expr) {
			// N.B. cls is the class of the UsrMeth instance (probably
			// terp.tUsrMeth),
			// not the class that it is a method on.
			super(onCls.terp.tUsrMeth, onCls, name, abbrev, doc);
			this.src = src;
			this.um_top = expr;
		}
		// =meth UsrMeth "eval" top
		public MethTop _top() {
			if (um_top == null) {
				// Lazily compile, if um_top was null.
				um_top = Parser.parseMethod(onCls, name, src);
			}
			return um_top;
		}

		@Override
		public Ur apply(Frame f, Ur r, Ur[] args) {
			_top();  // Force compilation.
			assert args.length == um_top.numArgs;

			Frame f2 = f.terp().newFrame(f, r, um_top);

			for (int i = 0; i < args.length; i++) {
				f2.locals[i] = args[i];
			}

			Ur z = um_top.eval(f2);
			return z;
		}

		public String toString() {
			// return fmt("%s \n\n##########\n\n%s", src, um_top.toPrettyString());
			return src;
		}
	}
}
