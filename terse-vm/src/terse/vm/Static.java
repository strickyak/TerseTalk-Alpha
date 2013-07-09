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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import terse.vm.Ur.Dict;
import terse.vm.Ur.Ht;
import terse.vm.Ur.Html;
import terse.vm.Ur.Num;
import terse.vm.Ur.Str;
import terse.vm.Ur.Vec;

public class Static {

	public static final String PRELUDE = "prelude.txt";

	public static void breakHere(Object x) {
		x.toString();
	}

	public static String fmt(String s, Object... objects) {
		return String.format(s, objects);
	}

	public static String GetStackTrace(final Throwable e) {
		final StringBuilder sb = new StringBuilder();
		final OutputStream out = new OutputStream() {
			@Override
			public void write(int ch) throws IOException {
				sb.append((char) ch);
			}
		};
		final PrintStream ps = new PrintStream(out);
		e.printStackTrace(ps);
		return sb.toString();
	}

	public static String htmlEscape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;");
	}
	
	public static String describe(Throwable ex) {
		StringBuilder sb = new StringBuilder("EXCEPTION: " + ex
				+ "\n\n");
		StackTraceElement[] st = ex.getStackTrace();
		for (int i = 0; i < st.length; i++) {
			sb.append("  * ");
			sb.append(st[i]);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String arrayToString(Expr[] a) {
		StringBuilder z = new StringBuilder();
		for (Expr e : a) {
			z.append(fmt("%s, ", e.toString()));
		}
		z.append("]");
		return z.toString();
	}

	public static String arrayToString(Ur[] a) {
		StringBuilder z = new StringBuilder();
		for (Ur e : a) {
			z.append(fmt("%s, ", e.toString()));
		}
		z.append("]");
		return z.toString();
	}

	public static String arrayToString(String[] a) {
		if (a == null)
			return "<?WHY_IS_THE_String[]_NULL?>";
		StringBuilder z = new StringBuilder();
		for (String e : a) {
			z.append(fmt("\"%s\", ", e));
		}
		z.append("]");
		return z.toString();
	}

	public static String arrayToString(int[] a) {
		if (a == null)
			return "<?WHY_IS_THE_int[]_NULL?>";
		StringBuilder z = new StringBuilder();
		for (int e : a) {
			z.append(fmt("\"%s\", ", e));
		}
		z.append("]");
		return z.toString();
	}
	@SuppressWarnings("rawtypes")
	public static String hashMapToMultiLineString(HashMap h) {
		StringBuilder sb = new StringBuilder();
		for (Object k : h.keySet()) {
			Object v = h.get(k);
			sb.append(fmt("<%s> := <%s>\n", k, v));
		}
		return sb.toString();
	}

	public static String repeat(int n, String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	// Little functions to avoid List objects for small arrays.
	// append() builds arrays in O(n**2) time, but n is usually very small.
	public static String[] strs(String... strings) {
		return strings;
	}

	public static String[] emptyStrs = new String[0];

	public static String[] append(String[] arr, String s) {
		if (arr == null || arr.length == 0) {
			return new String[] { s };
		}
		String[] z = new String[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			z[i] = arr[i];
		}
		z[arr.length] = s;
		return z;
	}

	public static String[] filterOutEmptyStrings(String[] a) {
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i].length() > 0)
				++count;
		}
		String[] z = new String[count];
		int j = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i].length() > 0) {
				z[j] = a[i];
				++j;
			}
		}
		return z;
	}

	public static Expr[] exprs(Expr... exprs) {
		return exprs;
	}

	public static Expr[] emptyExprs = new Expr[0];

	public static Expr[] append(Expr[] arr, Expr s) {
		if (arr == null || arr.length == 0) {
			return new Expr[] { s };
		}
		Expr[] z = new Expr[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			z[i] = arr[i];
		}
		z[arr.length] = s;
		return z;
	}

	public static Ur[] urs(Ur... urs) {
		return urs;
	}

	public static Ur[] emptyUrs = new Ur[0];

	public static Ur[] append(Ur[] arr, Ur s) {
		if (arr == null || arr.length == 0) {
			return new Ur[] { s };
		}
		Ur[] z = new Ur[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			z[i] = arr[i];
		}
		z[arr.length] = s;
		return z;
	}

	public static float[] floats(float... floats) {
		return floats;
	}

	public static int[] ints(int... ints) {
		return ints;
	}

	public static int[] emptyInts = new int[0];

	public static int[] append(int[] arr, int s) {
		if (arr == null || arr.length == 0) {
			return new int[] { s };
		}
		int[] z = new int[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			z[i] = arr[i];
		}
		z[arr.length] = s;
		return z;
	}

	// I got infinite recursion trying to use Java split() with quoted pattern.
	// So i write a simple one myself:
	public static String[] splitNonEmpty(String src, char delim) {
		String[] z = emptyStrs;
		while (src.length() > 0) {
			int i = src.indexOf(delim);
			if (i < 0) {
				// Not found; take the rest.
				z = append(z, src);
				break;
			} else if (i > 0) {
				z = append(z, src.substring(0, i));
				src = src.substring(i + 1);
			} else { // i == 0
				// Don't append empty string.
				src = src.substring(1);
			}
		}
		return z;
	}

	public static Pattern htmlTagP = Pattern
			.compile("[A-Za-z][-A-Za-z0-9_.:]*");

	public static String stringAt(Ur p, String i) {
		return stringAt(p, i, null);
	}

	public static String stringAt(Ur p, String i, String dflt) {
		if (!(p instanceof Dict))
			return dflt;
		Dict d = (Dict) p;
		Ur x = d.dict.get(d.terp().newStr(i));
		if (!(x instanceof Str))
			return dflt;
		return ((Str) x).str;
	}

	public static Html htmlAt(Ur p, String i) {
		if (!(p instanceof Dict)) {
			p.terp().toss("In Static.htmlAt, not a dict: <%s#%s>", p.cls, p);
		}
		Dict d = (Dict) p;
		Ur x = d.dict.get(d.terp().newStr(i));
		if (!(x instanceof Ht)) {
			p.terp().toss("In Static.htmlAt, did not find Ht: <%s#%s>", x.cls,
					x);
		}
		return ((Ht) x).html;
	}

	public static double floatAt(Ur p, String i, double dflt) {
		if (!(p instanceof Dict))
			return dflt;
		Dict d = (Dict) p;
		Ur x = d.dict.get(d.terp().newStr(i));
		if (!(x instanceof Num))
			return dflt;
		return ((Num) x).num;
	}

	public static Ur urAt(Ur p, int i) {
		if (!(p instanceof Vec))
			return p.terp().instNil;
		Vec v = (Vec) p;
		return v.vec.get(i);
	}

	public static Ur urAt(Ur p, String i) {
		if (!(p instanceof Dict))
			return p.terp().instNil;
		Dict d = (Dict) p;
		return d.dict.get(d.terp().newStr(i));
	}

	public static String stringAt(Ur p, int i) {
		return stringAt(p, i, null);
	}

	public static String stringAt(Ur p, int i, String dflt) {
		if (!(p instanceof Vec))
			return dflt;
		Vec v = (Vec) p;
		int sz = v.vec.size();
		if (sz <= i)
			return dflt;
		Ur x = v.vec.get(i);
		if (!(x instanceof Str))
			return dflt;
		return ((Str) x).str;
	}

	public static float floatAt(Ur p, int i) {
		if (!(p instanceof Vec))
			return -1;
		Vec v = (Vec) p;
		int sz = v.vec.size();
		if (sz <= i)
			return -1;
		Ur x = v.vec.get(i);
		if (!(x instanceof Num))
			return -1;
		return (float) ((Num) x).num;
	}
	
	public static byte[] StringToLow8(String s) {
		final int n = s.length();
		byte[] b = new byte[n];
		for (int i = 0; i < n; i++) {
			b[i] = (byte) s.charAt(i);
		}
		return b;
	}
	
	public static String Low8ToString(byte[] b) {
		final int n = b.length;
		char[] c = new char[n];
		for (int i = 0; i < n; i++) {
			c[i] = (char) b[i];
		}
		return new String(c);
	}
	
	public static void CopyBytes(byte[] in, int inOffset, int n, byte[] out, int outOffset) {
		for (int i = 0; i < n; i++) {
			out[i + outOffset] = in[i + inOffset];
		}
	}
	
	public static byte[] HexChars = "0123456789abcdef".getBytes();
	
	public static byte[] BytesToHex(byte[] a) {
		final int n = a.length;
		byte[] z = new byte[2*n];
		for (int i = 0; i < n; i++) {
			z[2*i+0] = HexChars[ (a[i]>>4)&15 ];
			z[2*i+1] = HexChars[ (a[i]>>0)&15 ];
		}
		return z;
	}
	
	public static int ValueOfHexChar(char c) {
		if ('0' <= c && c <= '9') {
			return c - '0';
		} else if ('a' <= c && c <= 'f') {
			return c - 'a' + 10;
		} else if ('A' <= c && c <= 'F') {
			return c - 'A' + 10;
		} else {
			throw new RuntimeException("Bad HexValue char: " + (int)c);
		}
	}
	
	public static byte[] HexToBytes(byte[] a) {
		final int n = a.length / 2;
		byte[] z = new byte[n];
		for (int i = 0; i < n; i++) {
			z[i] = (byte)(ValueOfHexChar((char)a[2*i])*16 + ValueOfHexChar((char)a[2*i+1]));
		}
		return z;
	}
	
	public static String UrlEncode(String s) {
		String z = "?";
		try {
			z = URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UrlEncode(): " + e);
		}
		return z;
	}

	public static String UrlDecode(String s) {
		StringBuffer sb = new StringBuffer();
		final int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '+':
				sb.append(" ");
				break;
			case '%':
				if (i + 2 < n) {
					char c1 = s.charAt(i + 1);
					char c2 = s.charAt(i + 2);
					int x = ValueOfHexChar(c1) * 16 + ValueOfHexChar(c2);
					sb.append((char) x);
					i += 2;
				} else {
					throw new IllegalArgumentException(
							"UrlDecode(): bad string: " + s);
				}
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
	
	public static String CurlyEncode(String s) {
		final int n = s.length();
		if (n == 0) {
			return "{}"; // Special Case.
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			if ('"' < c && c < '{') {
				sb.append(c);
			} else {
				sb.append("{" + (int) c + "}"); // {%d}
			}
		}
		return sb.toString();
	}
	
	public static String CurlyDecode(String s) {
		if (s.equals("{}"))  // Special case for {}
			return "";
				
		final int n = s.length();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '{':
				int j = i + 1;
				int x = 0;
				while (j < n && '0' <= s.charAt(j) && s.charAt(j) <= '9') {
					x = x * 10 + s.charAt(j) - '0';
					j++; 
				}
				if (j < n && s.charAt(j) == '}' && j > i+1) {
					sb.append((char)x);
					i = j;
					continue;
				}
				// else fall through
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static byte[] StringToCurly(String s) {
		return StringToLow8(CurlyEncode(s));
	}
	public static String CurlyToString(byte[] b) {
		return CurlyDecode(Low8ToString(b));
	}
	
	public static Charset utf8 = Charset.forName("utf-8");
	public static byte[] StringToUtf8(String s) {
		return s.getBytes(utf8);
	}
	public static String Utf8ToString(byte[] b) {
		return new String(b, utf8);
	}
}
