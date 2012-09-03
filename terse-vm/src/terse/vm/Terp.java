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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import terse.vm.Cls.UsrMeth;
import terse.vm.Expr.Send;
import terse.vm.Ur.Blk;
import terse.vm.Ur.Buf;
import terse.vm.Ur.Dict;
import terse.vm.Ur.Undefined;
import terse.vm.Ur.Num;
import terse.vm.Ur.Obj;
import terse.vm.Ur.Str;
import terse.vm.Ur.Sys;
import terse.vm.Ur.Vec;
import terse.vm.Usr.Tmp;
import terse.vm.Usr.UsrCls;

public abstract class Terp extends Static {

	public static Pattern WORLD_P = Pattern.compile(
			"^[a-z][a-z][a-z][0-9]{0,3}$", Pattern.CASE_INSENSITIVE);

	static Pattern TXTFILE_P = Pattern.compile("^[a-z][a-z0-9_]{1,31}\\.txt$");

	protected static String YAK_WEB_PAGE = "http://wiki.yak.net/1017";
	
	public static Pattern WHITE_PLUS = Pattern.compile("\\s+");

	public String worldName = "";
	public String worldFilename = "";
	boolean loadingWorldFile = false;
	public HashMap<String, Cls> clss = new HashMap<String, Cls>();
	public HashMap<String, Integer> allMethodNames = new HashMap<String, Integer>();

	public long tickCounter = Long.MAX_VALUE;
	public int expectingTerseException = 0;

	public Wrap wrap;
	public Cls tUr;
	public Cls tUrCls;
	public Cls tObj;
	public Cls tObjCls;
	public Cls tCls;
	public Cls tClsCls;
	public Cls tMetacls;
	public Cls tMetaclsCls;
	Cls tSuper;
	Cls tSuperCls;
	Cls tMeth;
	Cls tMethCls;
	Cls tJavaMeth;
	Cls tJavaMethCls;
	Cls tUsrMeth;
	Cls tUsrMethCls;
	Cls tSys;
	Cls tSysCls;
	Cls tNum;
	Cls tNumCls;
	Cls tBuf;
	Cls tBufCls;
	Cls tStr;
	Cls tStrCls;
	Cls tUndefined;
	Cls tUndefinedCls;
	Cls tBlk;
	Cls tBlkCls;
	Cls tVec;
	Cls tVecCls;
	Cls tDict;
	Cls tDictCls;
	Cls tExpr;
	Cls tExprCls;
	public Cls tUsr;
	public Cls tUsrCls;
	Cls tTmp;
	Cls tTmpCls;
	public Num instTrue;
	public Num instFalse;
	public Undefined instNil;
	Ur.Super instSuper;
	public Str instSpace;
	public Str instNewline;
	
	Exception loadWorldException;

	static boolean tolerateNullClass = false;

	/**
	 * The Interpreter.
	 * 
	 * @throws IOException
	 */
	protected Terp(boolean pleaseLoadPrelude, String worldName)
			throws IOException {
		if (worldName.length() > 0) {
			if (!WORLD_P.matcher(worldName).matches()) {
				toss("Bad world name <%s>", worldName);
			}
			this.worldName = worldName;
			this.worldFilename = "w_" + worldName + ".txt";
		}

		tolerateNullClass = true;
		this.tUrCls = new Cls(null/* Metacls */, this, "UrCls", null/* Cls */);
		this.tUr = new Cls(tUrCls, this, "Ur", null/* Ur has no super */);

		this.tObjCls = new Cls(null/* Metacls */, this, "ObjCls", null/* Cls */);
		this.tObj = new Cls(tObjCls, this, "Obj", tUr);

		this.tClsCls = new Cls(null/* MetaCls */, this, "ClsCls", tObjCls);
		this.tCls = new Cls(tClsCls, this, "Cls", tObj);

		this.tMetaclsCls = new Cls(null/* MetaCls */, this, "MetaclsCls",
				tObjCls);
		this.tMetacls = new Cls(tMetaclsCls, this, "Metacls", tCls);

		// Patch up: All *Cls's are instances of Metacls.
		this.tUrCls.cls = this.tMetacls;
		this.tObjCls.cls = this.tMetacls;
		this.tClsCls.cls = this.tMetacls;
		this.tMetaclsCls.cls = this.tMetacls;

		// Patch up: All *Cls's ultimately derive from Cls.
		this.tUrCls.supercls = tCls;
		this.tObjCls.supercls = tCls;

		tolerateNullClass = false;

		// Super is a very special class, from Ur, not from Obj.
		this.tSuperCls = new Cls(tMetacls, this, "SuperCls", tUrCls);
		this.tSuper = new Cls(tSuperCls, this, "Super", tUr);

		this.tMethCls = new Cls(tMetacls, this, "MethCls", tObjCls);
		this.tMeth = new Cls(tMethCls, this, "Meth", tObj);
		this.tJavaMethCls = new Cls(tMetacls, this, "JavMethCls", tMethCls);
		this.tJavaMeth = new Cls(tJavaMethCls, this, "JavMeth", tMeth);
		this.tUsrMethCls = new Cls(tMetacls, this, "UsrMethCls", tMethCls);
		this.tUsrMeth = new Cls(tUsrMethCls, this, "UsrMeth", tMeth);
		this.tSysCls = new Cls(tMetacls, this, "SysCls", tObjCls);
		this.tSys = new Cls(tSysCls, this, "Sys", tObj);
		this.tNumCls = new Cls(tMetacls, this, "NumCls", tObjCls);
		this.tNum = new Cls(tNumCls, this, "Num", tObj);
		this.tBufCls = new Cls(tMetacls, this, "BufCls", tObjCls);
		this.tBuf = new Cls(tBufCls, this, "Buf", tObj);
		this.tStrCls = new Cls(tMetacls, this, "StrCls", tObjCls);
		this.tStr = new Cls(tStrCls, this, "Str", tObj);

		// UsrCls is the only concrete Java subtype of Cls,
		// and it is a little bit weird.
		this.tUsrCls = new Cls(tMetacls, this, "UsrCls", tObjCls);
		this.tUsr = new Usr.UsrCls(tUsrCls, this, "Usr", tObj);
		this.tTmpCls = new Cls(tMetacls, this, "TmpCls", tUsrCls);
		this.tTmp = new Usr.UsrCls(tTmpCls, this, "Tmp", tUsr);
		// this.tUsr.myVars = strs("p", "q"); // TODO: temp hack.
		// this.tUsr.recalculateAllVarsHereAndBelow();

		this.tUndefinedCls = new Cls(tMetacls, this, "UndefinedCls", tObjCls);
		this.tUndefined = new Cls(tUndefinedCls, this, "Undefined", tObj);
		this.tBlkCls = new Cls(tMetacls, this, "BlkCls", tObjCls);
		this.tBlk = new Cls(tBlkCls, this, "Blk", tObj);
		this.tVecCls = new Cls(tMetacls, this, "VecCls", tObjCls);
		this.tVec = new Cls(tVecCls, this, "Vec", tObj);
		this.tDictCls = new Cls(tMetacls, this, "DictCls", tObjCls);
		this.tDict = new Cls(tDictCls, this, "Dict", tObj);

		this.tExprCls = new Cls(tMetacls, this, "ExprCls", tObjCls);
		this.tExpr = new Cls(tExprCls, this, "Expr", tObj);

		this.instTrue = newNum(1);
		this.instFalse = newNum(0);
		this.instNil = new Undefined(tUndefined);
		this.instSuper = new Ur.Super(this);
		this.instSpace = new Str(this, " ");
		this.instNewline = new Str(this, "\n");

		// TODO: Convert all methods to "wrap" style, and get rid of all these
		// static inits.
		Str.addBuiltinMethodsForStr(this);

		tolerateNullClass = true;
		loadingWorldFile = true;
		wrap = new Wrap();
		wrap.installClasses(this);
		wrap.installMethods(this);
		loadingWorldFile = false;
		tolerateNullClass = false;

		if (pleaseLoadPrelude) {
			loadPrelude();
		}

		if (worldName.length() > 0) {
			loadWorldException = new InitialWorldReader(worldName).loadFile(worldFilename);
			if (loadWorldException != null && !(loadWorldException instanceof FileNotFoundException)) {
				say("COULD BE A PROBLEM IN InitialWorldReader(%s)loadfile(%s): %s", worldName, worldFilename, loadWorldException);
			}
		}
	}

	final static int LOG_LEN = 128;
	String[] logArray = new String[LOG_LEN];
	int logPtr = 0;

	protected void recordLog(String msg) {
//		double t = (System.nanoTime() % (100 * 1000000000)) / 1000000000.0;
		double t = (System.currentTimeMillis() % (100 * 1000)) / 1000.0;
		logArray[logPtr] = fmt("[%.3f] %s", t, msg);
		logPtr = (logPtr + 1) % LOG_LEN;
	}
	public String[] getLog() {
		int n = 0;
		for (int i = 0; i < LOG_LEN; i++) {
			if (logArray[i] != null) {
				++n;
			}
		}
		String[] z = new String[n];
		int k = 0;
		for (int i = 0; i < LOG_LEN; i++) {
			int j = (i + logPtr) % LOG_LEN;
			if (logArray[j] != null) {
				z[k] = logArray[j];
				++k;
			}
		}
		for (int i = 0; i < n/2; i++) {
			String t = z[i];
			z[i] = z[n-i-1];
			z[n-i-1] = t;
		}
		return z;
	}
	
	
	public static class TooManyTicks extends Error {
	}

	public void tick() {
		--tickCounter;
		if (tickCounter < 1) {
			try {
				throw new RuntimeException("Going To Throw TooManyTicks");
			} catch (RuntimeException ex) {
				ex.printStackTrace();

				StringBuffer sb = new StringBuffer(ex.toString());
				StackTraceElement[] elems = ex.getStackTrace();
				for (StackTraceElement e : elems) {
					sb.append("\n  * ");
					sb.append(e.toString());
				}
				
				say(sb.toString());
			}
			throw new TooManyTicks();
		}

	}

	public Frame newFrame(Frame prev, Ur self, Expr.MethTop top) {
		return new Frame(prev, self, top);
	}

	public class Frame extends Obj { // Important: non-static, tied to a Terp.

		// =get Frame . prev prevFrame
		Frame prev; // for listing the stack
		// =get Frame . self receiver
		Ur self;
		// =get Frame Ur[] locals localVars
		Ur[] locals;
		// =get Frame . top currentMethodExpr
		Expr.MethTop top;
		// =get Frame int level level
		int level;

		// =cls "Sys" Frame Obj
		private Frame(Frame prev, Ur self, Expr.MethTop top) {
			super(wrap.clsFrame);
			this.prev = prev;
			this.self = self;
			this.top = top;
			this.level = (prev == null) ? 0 : prev.level + 1;
			this.locals = new Ur[top.numLocals];
			for (int i = 0; i < locals.length; i++) {
				this.locals[i] = getTerp().instNil;
			}
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("FRAME{");
			sb.append(fmt("me=<%s>; ", self));
			for (int i = 0; i < locals.length; i++) {
				sb.append(fmt("\"%d\"<%s>; ", i, locals[i]));
			}
			sb.append("}");
			return sb.toString();
		}
	}

	final public Terp getTerp() {
		return this;
	}
	
	final public Ur nullToNil(Ur x) {
		return (x == null) ? instNil : x;
	}

	final public Num newNum(double a) {
		return new Num(this, a);
	}

	final public Str newStr(String s) {
		return new Str(this, s);
	}

	final public Tmp newTmp() {
		return new Tmp(this);
	}

	final public Dict newDict(Ur[] arr) {
		Dict z = new Dict(this);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] instanceof Vec) {
				Vec v = (Vec) arr[i];
				if (v.vec.size() == 2) {
					z.dict.put(v.vec.get(0), v.vec.get(1));
				} else {
					toss("To initialize assoc in Dict, expected Vec of length 2, but got <%s#%d#%s>; inside <%s>",
							v.cls, v.vec.size(), v, arrayToString(arr));
				}
			} else {
				toss("To initialize assoc in Dict, expected Vec of length 2, but got <%s#%s>; inside <%s>",
						arr[i].cls, arr[i], arrayToString(arr));
			}
		}
		return z;
	}

	final public Vec newVec(Ur[] a) {
		Vec z = new Vec(this);
		for (int i = 0; i < a.length; ++i) {
			z.vec.add(a[i]);
		}
		return z;
	}

	final public Vec newVec(int[] a) {
		Vec z = new Vec(this);
		for (int i = 0; i < a.length; ++i) {
			z.vec.add(newNum(a[i]));
		}
		return z;
	}

	final public Vec mkFloatVec(float...a) {
		Vec z = new Vec(this);
		for (int i = 0; i < a.length; ++i) {
			z.vec.add(newNum(a[i]));
		}
		return z;
	}

	final public Vec mkStrVec(String... strs) {
		Ur[] arr = new Ur[strs.length];
		for (int i = 0; i < strs.length; i++) {
			arr[i] = newStr(strs[i]);
		}
		return newVec(arr);
	}

	final public Vec mkSingletonStrVecVec(String... strs) {
		Ur[] arr = new Ur[strs.length];
		for (int i = 0; i < strs.length; i++) {
			arr[i] = mkStrVec(strs[i]);
		}
		return newVec(arr);
	}

	public Dict handleUrl(String url, HashMap<String, String> query) {
		say("runUrl: %s", url);
		query = (query == null) ? new HashMap<String, String>() : query;
		Ur[] queryArr = new Ur[query.size()];
		int i = 0;
		for (String k : query.keySet()) {
			String v = query.get(k);
			if (k == null)
				k = "HOW_DID_WE_GET_A_NULL_KEY";
			if (v == null)
				v = "HOW_DID_WE_GET_A_NULL_VALUE";
			Ur queryKey = newStr(k);
			Ur queryValue = newStr(v.replaceAll("\r\n", "\n"));
			queryArr[i] = new Vec(this, urs(queryKey, queryValue));
			++i;
		}
		Dict qDict = newDict(queryArr);
		assert url.startsWith("/");
		if (url.equals("/")) {
			url = "/Top";
		}

		// To get app name, skip the initial '/', and split on dots.
		String[] word = url.substring(1).split("[.]");
		assert word.length > 0;
		String appName = word[0];

		Dict result = null;
		try {
			Cls cls = getTerp().clss.get(appName.toLowerCase());
			if (cls == null) {
				toss("Rendering class does not exist: <%s>", appName);
			}

			String urlRepr = newStr(url).repr();
			String qDictRepr = qDict.repr(); // Inefficient. TODO.
			Ur result_ur = instNil;
			int id = 0;

			Obj inst = null;
			try {
				id = Integer.parseInt(word[1]);
				if (cls instanceof Usr.UsrCls) {
					inst = ((Usr.UsrCls) cls).cache.find(id);
				}
			} catch (Exception _) {
				// pass.
			}
			long before = tickCounter;
			long nanosBefore = System.nanoTime();
			if (inst != null) {
				result_ur = inst.eval(fmt("me handle: (%s) query: (%s)",
						urlRepr, qDictRepr));
			} else if (Send.understands(cls, "handle:query:")) {
				say("CLS <%s> understands handle:query: so sending to class.", cls);
				// First try sending to the class.
				result_ur = cls.eval(fmt("me handle: (%s) query: (%s)",
						urlRepr, qDictRepr));
			} else {
				Ur instance = cls.eval("me new");
				Usr usrInst = instance.asUsr();
				// TODO: LRU & mention() conflict with Cls.insts map.
				id = usrInst == null ? 0 : usrInst.omention(); // LRU Cache

				// Next try creating new instance, and send to it.
				result_ur = instance.asObj().eval(
						fmt("me handle: (%s) query: (%s)",
								newStr(url).repr(), qDict.repr()));
			}
			result = result_ur.asDict();
			if (result == null) {
				toss("Sending <handle:query:> to instance of <%s> did not return a Dict: <%s>",
						appName, result_ur);
			}
			result.dict.put(newStr("id"), newStr(Integer.toString(id)));
			long after = tickCounter;
			long nanosAfter = System.nanoTime();
			result.dict.put(newStr("ticks"), newNum(before - after));
			result.dict.put(newStr("nanos"), newNum(nanosAfter - nanosBefore));
			say("<handle:query:> used %d ticks and %.3f secs.", before - after,
					(double) (nanosAfter - nanosBefore) / 1000000000.0);

		} catch (Exception ex) {
			ex.printStackTrace();
			StringBuffer sb = new StringBuffer(ex.toString());
			StackTraceElement[] elems = ex.getStackTrace();
			for (StackTraceElement e : elems) {
				sb.append("\n  * ");
				sb.append(e.toString());
			}
			Ur[] dict_arr = urs(
					new Vec(this, urs(newStr("type"), newStr("text"))),
					new Vec(this, urs(newStr("title"), newStr(ex.toString()))),
					new Vec(this, urs(newStr("value"), newStr(sb.toString()))));
			result = newDict(dict_arr);
		} catch (TooManyTicks err) {
			err.printStackTrace();
			String s = fmt("TOO_MANY_TICKS_IN_handleUrl <%s> qdict <%s>", url, qDict);
			Ur[] dict_arr = urs(
					new Vec(this, urs(newStr("type"), newStr("text"))),
					new Vec(this, urs(newStr("title"), newStr(err.toString()))),
					new Vec(this, urs(newStr("value"), newStr(s))));
			result = newDict(dict_arr);
		} catch (Error err) {
			err.printStackTrace();
			Ur[] dict_arr = urs(
					new Vec(this, urs(newStr("type"), newStr("text"))),
					new Vec(this, urs(newStr("title"), newStr(err.toString()))),
					new Vec(this, urs(newStr("value"), newStr(err.toString()))));
			result = newDict(dict_arr);
		}
		return result;
	}

	static Pattern INITIAL_WHITE = Pattern.compile("^\\s");

	public abstract class WorldReader {
		protected int lineNum;
		protected String world;
		
		public WorldReader(String world) {
			this.world = world;
		}

		abstract public void doCls(String[] words, String more);

		abstract public void doVars(String[] words, String more);

		abstract public void doMeth(String[] words, String more);

		abstract public void doEquals(String[] words, String more);

		abstract public void doInst(String[] words, String more);

		public Exception loadFile(String initFilename) {
			try {
				say("LOADING FILE <%s> for world <%s>", initFilename, worldName);
				FileInputStream fis = openFileRead(initFilename);
				loadReader(new InputStreamReader(fis));
				discoverAllMethodNames();
			} catch (IOException ex) {
				ex.printStackTrace();
				say("ERROR Loading <%s> at line %d:  %s", initFilename, lineNum, ex);
				return ex;
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				say("ERROR Loading <%s> at line %d:  %s", initFilename, lineNum, ex);
				return ex;
			}
			return null;
		}

		public void loadReader(InputStreamReader isr) throws IOException {
			BufferedReader br = new BufferedReader(isr);
			loadingWorldFile = true;
			lineNum = 0;
			try {
				String line = br.readLine();
				++lineNum;
				while (true) {
					// Break at EOF.
					if (line == null)
						break;
					// Skip blank lines.
					String trimmed = line.trim();
					if (trimmed.length() == 0) {
						line = br.readLine();
						++lineNum;
						continue;
					}
					// Skip comments.
					if (line.charAt(0) == '#') {
						line = br.readLine();
						++lineNum;
						continue;
					}

					char c = line.charAt(0);
					if (c != '(' && c != ')') {
						if (line.charAt(0) < 'a' || line.charAt(0) > 'z') {
							toss("loadInitFile: Should have started with some lowercase letter a-z: <%s>",
									line);
						}
					}
					
					//say("COMMAND: %s", line);

					// Store the command line in words[] and command.
					String[] words = line.trim().split("\\s+");
					if (words.length < 1) {
						line = br.readLine();
						++lineNum;
						continue;
					}
					// Recognize new timestamped format with "(".
					if (words[0].equals("(") && words.length > 2) {
						// Skip two words: the "("or ")", and a timestamp that
						// follows
						// it.
						// words = Arrays.copyOfRange(words, 2, words.length);
						String[] new_words = new String[words.length - 2];
						System.arraycopy(words, 2, new_words, 0,
								words.length - 2);
						words = new_words;
					}
					// Recognize new timestamped format ending with ")" line.
					if (words[0].equals(")")) {
						line = br.readLine();
						++lineNum;
						continue;
					}

					String command = words[0];

					// Now slurp any lines beginning with white space into more.
					StringBuffer sb = new StringBuffer();
					line = br.readLine();
					++lineNum;
					while (line != null
							&& (INITIAL_WHITE.matcher(line).lookingAt() || line
									.length() == 0)) {
						sb.append(line.length() > 0 ? line.substring(1) : "");
						sb.append("\n");
						line = br.readLine();
						++lineNum;
					}
					// Keep line for next time through big loop.
					String more = sb.toString();
					//say("MORE: len=%s", more.length());

					try {
						int ca0 = command.charAt(0);
						//say("Char At 0 == %d; cmd=%s; words=%s", ca0, command, mkStrVec(words));
						if (command.charAt(0) == ')') {
							// NOP.
						} else if (command.equals("meth")) {
							doMeth(words, more);
						} else if (command.equals("vars")) {
							doVars(words, more);
						} else if (command.equals("instvars")) { // Archaic spelling.
							doVars(words, more);
						} else if (command.equals("inst")) {
							doInst(words, more);
						} else if (command.equals("cls")) {
							doCls(words, more);
						} else if (command.equals("class")) {  // Archaic spelling.
							doCls(words, more);
						} else if (command.equals("equals")) {
							doEquals(words, more);
						} else if (command.equals("stop")) {
							break; // For testing partial files.
						} else {
							toss("loadInitFile: Unknown command: <%s>", mkStrVec(words));
						}
					} catch (RuntimeException ex) {
						ex.printStackTrace();
						toss("Loading at line %d:  %s", lineNum, ex);
					}
				}
			} finally {
				loadingWorldFile = false;
			}
		}
	}

	public class InitialWorldReader extends WorldReader {

		public InitialWorldReader(String world) {
			super(world);
		}

		@Override
		public void doCls(String[] words, String more) {
			String className = words[1];
			String superName = words[2];
			Cls clsObj = clss.get(className.toLowerCase());
			Cls supObj = clss.get(superName.toLowerCase());
			if (more.trim().length() > 0) {
				toss("Was not expecting more: <%s>");
			}
			if (supObj == null) {
				toss("loadInitFile: Cannot define subclass <%s> of <%s>: class <%s> does not exist.",
						className, superName, superName);
			}

			if (clsObj == null && supObj != null) {
				// We can create the class.
				supObj.defineSubclass(className);
			} else {
				toss("loadInitFile: Cannot define subclass <%s> of <%s>",
						className, superName);
			}
		}

		@Override
		public void doVars(String[] words, String more) {
			String className = words[1];
			Cls cls = clss.get(className.toLowerCase());
			if (cls == null) {
				cls = defineOrphan(className);
			}
			cls.defVars_(more);
		}

		@Override
		public void doMeth(String[] words, String more) {
			String className = words[1];
			String methodName = words[2];

			Cls cls = clss.get(className.toLowerCase());
			if (cls == null) {
				cls = defineOrphan(className);
			}
			UsrMeth um = new UsrMeth(cls, methodName, "", "", more, null);
			cls.meths.put(methodName.toLowerCase(), um);

		}

		@Override
		public void doEquals(String[] words, String more) {
			String expected = "";
			for (int i = 1; i < words.length; i++) {
				expected += words[i] + " ";
			}
			Ur p1 = newTmp().eval(expected);
			Ur p2 = newTmp().eval(more);
			if (!p1.equals(p2)) {
				toss("loadInitFile: eq check failed: line %d: <%s> --> <%s> but <%s> --> <%s>",
						lineNum, expected, p1, more, p2);
			}
		}

		@Override
		public void doInst(String[] words, String more) {
			String className = words[1];
			String instName = words[2];
			Matcher match_nww = Usr.NAME_WITH_WORLD.matcher(instName);
			if (match_nww.lookingAt()) {
				// Already has the world on it.
			} else {
				instName += "_" + world;
			}
			
			UsrCls usrCls = (UsrCls) clss.get(className.toLowerCase());
			if (usrCls == null) {
				usrCls = defineOrphan(className);
			}
			usrCls.savedInstsUnrealized.put(instName, more);
		}

	}

	void appendWorldFile(String firstLine, String[] rest) throws IOException {
		if (loadingWorldFile) {
			return; // Don't cause infinite loop!
		}
		if (worldFilename.length() == 0) {
			return; // For unit tests.
		}
		long timestamp = System.currentTimeMillis() / 1000;
		FileOutputStream fos = openFileAppend(worldFilename);
		PrintStream ps = new PrintStream(fos);
		ps.println(fmt("( %d %s", timestamp, firstLine));
		if (rest != null) {
			for (String s : rest) {
				ps.println(" " + s); // Stuff 1 space before each line.
			}
		}
		ps.println(fmt(") %d", timestamp));
		ps.flush();
		fos.flush();
		fos.close();
	}

	public UsrCls defineOrphan(String className) {
		UsrCls orphan = (UsrCls) clss.get("orphan");
		say("COULD BE A PROBLEM:  Defining Orphaned Class <%s>", className);
		return (UsrCls) orphan.defineSubclass(className);
	}
	public Ur toss(String s, Object... objects) {
		if (expectingTerseException > 0) {
			throw new TerseExpectedException(s, objects);
		} else {
			throw new TerseException(s, objects);
		}
	}

	public Ur retoss(String s, Object... objects) {
		if (expectingTerseException > 0) {
			throw new TerseExpectedRetossException(s, objects);
		} else {
			throw new TerseRetossException(s, objects);
		}
	}

	public Ur tossNotUnderstood(Cls c, String msg) {
		return toss("Message <%s> not understood by class <%s>", msg, c.cname);
	}

	public void checkTxtFileNameSyntax(String filename) {
		if (!TXTFILE_P.matcher(filename).matches()) {
			toss("Bad filename, should match <%s> : <%s>", TXTFILE_P, filename);
		}
	}

	public Num boolObj(boolean x) {
		return x ?instTrue : instFalse;
	}

	public abstract String say(String s, Object... objects);

	public abstract void loadPrelude() throws IOException;

	public abstract FileInputStream openFileRead(String filename)
			throws FileNotFoundException;

	public abstract FileOutputStream openFileWrite(String filename)
			throws FileNotFoundException;

	public abstract FileOutputStream openFileAppend(String filename)
			throws FileNotFoundException;

	public abstract File getFilesDir();
	
	public abstract boolean deleteFile(String filename);

	public abstract Vec listOfWebFiles();

	public abstract void pushToWeb(String filename, String content);

	public abstract String pullFromWeb(String filename);

	// DRAW ABSTRACTIONS
	public interface IInk {
		void setColor(int rgbDecimal);

		void setFont(String name);

		void setFontSize(int scaledPoints);

		void setThickness(int pixels);
	}

	public interface ICanv {
		void drawLine(IInk k, float x1, float y1, float x2, float y2);

		void drawRect(IInk k, float x1, float y1, float x2, float y2);

		void drawText(IInk k, float x, float y);
	}

	public static class PosixTerp extends Terp {

		protected PosixTerp(boolean loadPrelude, String imageName)
				throws IOException {
			super(loadPrelude, imageName);
		}

		@Override
		public String say(String s, Object... objects) {
			String msg = fmt(s, objects);
			System.err.println(msg);
			recordLog(msg);
			return msg;
		}

		@Override
		public void loadPrelude() throws IOException {
			new InitialWorldReader("pre0").loadFile("prelude.txt");
		}

		@Override
		public FileInputStream openFileRead(String filename)
				throws FileNotFoundException {
			return new FileInputStream(filename);
		}

		@Override
		public FileOutputStream openFileWrite(String filename)
				throws FileNotFoundException {
			return new FileOutputStream(filename, false);
		}

		@Override
		public FileOutputStream openFileAppend(String filename)
				throws FileNotFoundException {
			return new FileOutputStream(filename, true);
		}

		@Override
		public File getFilesDir() {
			return new File(".");
		}

		@Override
		public void pushToWeb(String filename, String content) {
			// import org.apache.http.impl.client.DefaultHttpClient;
			toss("Not Implemented in PosixTerp: pushToWeb");
		}

		@Override
		public String pullFromWeb(String filename) {
			// import org.apache.http.impl.client.DefaultHttpClient;
			toss("Not Implemented in PosixTerp: pullFromWeb");
			return null;
		}

		@Override
		public Vec listOfWebFiles() {
			toss("Not Implemented in PosixTerp: listOfWebFiles");
			return null;
		}

		@Override
		public boolean deleteFile(String filename) {
			File f = new File(filename);
			return f.delete();
			// TODO: some regexp.
		}
	}

	public interface Factory {
		Terp createTerp(boolean loadPrelude, String imageName)
				throws IOException;
	}

	void discoverAllMethodNames() {
		allMethodNames.clear();
		for (String clsName : clss.keySet()) {
			Cls c = clss.get(clsName);
			for (String methName : c.meths.keySet()) {
				int numColons = 0;
				for (int i = 0; i < methName.length(); i++) {
					if (methName.charAt(i) == ':')
						++numColons;
				}
				allMethodNames.put(methName, numColons);
			}
		}
	}
	

	private class TerseBaseException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		protected String msg;
		protected Object[] args;
		protected boolean retoss;

		public TerseBaseException(boolean retoss, String format,
				Object... args) {
			this.retoss = retoss;
			this.msg = "?????";
			this.args = args;

			String[] argstrs = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				try {
					argstrs[i] = fmt("<%s:%s>", args[i].getClass().getName(),
							args[i].toString());
				} catch (RuntimeException _) {
					argstrs[i] = "???";
				}
			}
			this.msg = fmt(format, (Object[]) /*argstrs*/ args);
		}

		@Override
		public String toString() {
			return fmt(msg, args);
		}
	}
	private class TerseException extends TerseBaseException {
		private static final long serialVersionUID = 1L;

		public TerseException(String format,
				Object[] args) {
			super(false, format, args);
			say("TerseException(%s)<<%s>>", retoss ? "retoss" : "", msg);
			if (retoss) {
				breakHere("retoss");
			} else {
				breakHere("toss");
			}
		}
	}
	private class TerseRetossException extends TerseBaseException {
		private static final long serialVersionUID = 1L;

		public TerseRetossException(String format,
				Object[] args) {
			super(true, format, args);
			say("TerseException(%s)<<%s>>", retoss ? "retoss" : "", msg);
			if (retoss) {
				breakHere("retoss");
			} else {
				breakHere("toss");
			}
		}
	}
	private class TerseExpectedException extends TerseBaseException {
		private static final long serialVersionUID = 1L;

		public TerseExpectedException(String format,
				Object[] args) {
			super(false, format, args);
			say("TerseEXPECTEDException(%s)<<%s>>", retoss ? "retoss" : "", msg);
			if (retoss) {
				breakHere("expected retoss");
			} else {
				breakHere("expected toss");
			}
		}
	}
	private class TerseExpectedRetossException extends TerseBaseException {
		private static final long serialVersionUID = 1L;

		public TerseExpectedRetossException(String format,
				Object[] args) {
			super(true, format, args);
			say("TerseEXPECTEDException(%s)<<%s>>", retoss ? "retoss" : "", msg);
			if (retoss) {
				breakHere("expected retoss");
			} else {
				breakHere("expected toss");
			}
		}
	}
}
