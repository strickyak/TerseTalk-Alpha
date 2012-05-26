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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import terse.vm.Terp;
import terse.vm.Terp.Frame;
import junit.framework.TestCase;

public class TerpTest extends TestCase {
	Terp t;
	Frame f;

	public TerpTest(String arg0) {
		super(arg0);
	}

	public void testTerpSimple() throws IOException {
		new Terp.PosixTerp(false, "");
	}

	public void testTerpWithPrelude() throws IOException {
		new Terp.PosixTerp(true, "");
	}

	public void testStrs() {
		String[] a = Static.strs("one", "two");
		String[] b = Static.append(a, "three");
		assertEquals(3, b.length);
		assertEquals("one", b[0]);
		assertEquals("two", b[1]);
		assertEquals("three", b[2]);
	}

	public void testhtmlEscape() {
		assertEquals("it&quot;s &lt;pb&gt; &amp; J.\n",
				Static.htmlEscape("it\"s <pb> & J.\n"));
	}

	public void testSplitNonEmpty() {
		assertEquals("foo", Static.splitNonEmpty("foo", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("/foo", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("foo/", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("//foo", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("foo//", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("/foo//bar//", '/')[0]);
		assertEquals("foo", Static.splitNonEmpty("foo/bar", '/')[0]);
		assertEquals("bar", Static.splitNonEmpty("/foo//bar//", '/')[1]);
		assertEquals("bar", Static.splitNonEmpty("foo/bar", '/')[1]);
	}

//	public static class TestingTerp extends Terp {
//
//		protected TestingTerp(boolean loadPrelude, String imageName,
//				String initFilename) throws IOException {
//			super(loadPrelude, imageName, initFilename);
//		}
//
//		@Override
//		public void say(String s, Object... objects) {
//			// TODO Auto-generated method stub
//			String msg = fmt(s, objects);
//			System.out.println(msg);
//		}
//
//		@Override
//		public void loadPrelude() throws IOException {
//			loadFile("prelude.txt");
//		}
//
//		@Override
//		public FileInputStream openFileRead(String filename)
//				throws FileNotFoundException {
//			return new FileInputStream(filename);
//		}
//
//		@Override
//		public FileOutputStream openFileWrite(String filename)
//				throws FileNotFoundException {
//			return new FileOutputStream(filename, false);
//		}
//
//		@Override
//		public FileOutputStream openFileAppend(String filename)
//				throws FileNotFoundException {
//			return new FileOutputStream(filename, true);
//		}
//	}
}
