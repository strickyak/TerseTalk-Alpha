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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import terse.vm.Cls.JavaMeth;
import terse.vm.Ur.Obj;
import terse.vm.Terp.Frame;
import terse.vm.Terp.ICanv;
import terse.vm.Terp.IInk;

public class Usr extends Obj {
	private static int next_oid = Math.abs((int) System.currentTimeMillis()) % 1499999999 + 101;

	// inst
	private int usr_oid_or_zero = 0; // Transient id, while in memory.
	public String usr_oname = null; // For saved, persistent objects.

	// =cls "usr" Usr Obj
	public Usr(Cls cls) {
		super(cls);
	}

	@Override
	public UsrCls usrCls() {
		return (UsrCls) cls;
	}

	// =meth Usr "usr" opath
	public String opath() {
		return oname() + "@" + cls.cname;
	}

	// =meth Usr "usr" oname
	public String oname() {
		if (usr_oname == null) {
			return Integer.toString(usr_oid_or_zero);
		} else {
			return usr_oname;
		}
	}

	// =meth Usr "access" oid
	public int oid() {
		if (usr_oid_or_zero == 0) {
			usr_oid_or_zero = next_oid;
			++next_oid;
		}
		usrCls().cache.store(this);
		return usr_oid_or_zero;
	}

	// =meth Usr "access" omention
	public int omention() {
		int z = oid();
		usrCls().cache.store(this);
		return z;
	}

	public void visit(Visitor v) {
		v.visitUsr(this);
	}

	public Usr asUsr() {
		return this;
	}

	public static class Tmp extends Usr {
		// =cls "usr" Tmp Usr
		Tmp(Terp t) {
			super(t.tTmp);
		}
	}

	public static class LRUsr {
		Usr[] memory; // High index is front; 0 is back.
		int size;

		public LRUsr(int size) {
			this.size = size;
			this.memory = new Usr[size];
		}

		public void store(Usr u) {
			for (int i = 0; i < size; i++) {
				if (memory[i] == u) {
					// Move u to front, so slide the rest 1 step to back.
					for (int j = i; j < size - 1; j++) {
						memory[j] = memory[j + 1];
					}
					memory[size - 1] = u;
					return;
				}
			}
			// Move everything 1 step to back.
			for (int i = 0; i < size - 1; i++) {
				memory[i] = memory[i + 1];
			}
			memory[size - 1] = u;
		}

		public Usr find(int id) {
			for (int i = 0; i < size; i++) {
				Usr obj = memory[i];
				if (obj != null && obj.oid() == id) {
					return obj;
				}
			}
			return null; // Not found.
		}

		public Usr find(String name) {
			if (name.matches("[0-9]+")) {
				// Numeric name; use int.
				int id = Integer.parseInt(name);
				for (int i = 0; i < size; i++) {
					Usr obj = memory[i];
					if (obj != null && obj.oid() == id) {
						return obj;
					}
				}
				return null; // Not found.
			}
			// Use String.
			for (int i = 0; i < size; i++) {
				Usr obj = memory[i];
				if (obj != null && obj.usr_oname == name) {
					return obj;
				}
			}
			return null; // Not found.
		}
	}

	public static Pattern JUST_NAME = Pattern.compile("([-a-z0-9]+)$",
			Pattern.CASE_INSENSITIVE);
	public static Pattern NAME_WITH_WORLD = Pattern.compile(
			"([-a-z0-9]+)_([a-z][a-z][a-z][0-9]{0,3})$",
			Pattern.CASE_INSENSITIVE);

	public static class UsrCls extends Cls {
		LRUsr cache = new LRUsr(32);

		HashMap<String, Usr> savedInsts = new HashMap<String, Usr>();
		HashMap<String, String> savedInstsUnrealized = new HashMap<String, String>();

		// =cls "meth" UsrCls Cls
		UsrCls(Cls cls, Terp terp, String name, Cls supercls) {
			super(cls, terp, name, supercls);
		}

		// =meth UsrCls "new" new
		public Usr _new() {
			Usr z = new Usr(this);
			z.omention();
			return z;
		}

		public Obj findById(int id) {
			Usr z = cache.find(id);
			return z == null ? terp.instNil : z;
		}

		// =meth UsrCls "usr" find:
		public Obj find(String idOrName) {
			// Returns Nil or a Usr, therefore returns Obj.
			Usr z = cache.find(idOrName);
			if (z != null) {
				return z;
			}
			// If just the name, without world, append current worldName.
			Matcher match_jn = JUST_NAME.matcher(idOrName);
			if (match_jn.lookingAt()) {
				idOrName += "_" + terp.worldName;
			}
			// Look up the name with world.
			Matcher match_nww = NAME_WITH_WORLD.matcher(idOrName);
			if (match_nww.lookingAt()) {
				Usr x = savedInsts.get(idOrName.toLowerCase());
				if (x != null) {
					return x;
				}
				String state = savedInstsUnrealized.get(idOrName.toLowerCase());
				if (state != null) {
					x = constructFromSavedString(state);
					savedInsts.put(idOrName.toLowerCase(), x);
					return x;
				}
			}
			return terp.instNil;
		}

		public Usr constructFromSavedString(String state) {
			// For now, it's just a dict of field values.
			Dict d = eval(state).mustDict();
			Usr z = new Usr(this);
			z.pokeVars_(d);
			return z;
		}
	}
}
