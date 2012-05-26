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
package terse.web;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import terse.vm.Ur.Dict;
import terse.vm.Ur.Html;
import terse.vm.Ur.Undefined;
import terse.vm.Ur.Num;
import terse.vm.Ur.Str;
import terse.vm.Ur.Vec;
import terse.vm.Ur.Visitor;
import terse.vm.Async;
import terse.vm.Cls;
import terse.vm.Ur;
import terse.vm.Static;
import terse.vm.Terp;
import terse.vm.Terp.Factory;
import terse.vm.Usr;

public class WebServer extends Static {
	Async async;
	Terp terp;
	String[] TABLE_PARAMS;
	Terp.Factory factory = new WebTerpFactory();

	static Pattern LINK_P = Pattern
			.compile("[|]link[|](/[-A-Za-z_0-9.:]*)[|]([^|]+)[|](.*)");

	public WebServer() throws IOException {
		super();
		async = new Async(factory, "web");
		async.start();

		// TODO: Can we do without an initial terp?
		beginNewTerp();
	}

	void beginNewTerp() throws IOException {
		Terp t = factory.createTerp(true, "web");
		setTerp(t);
	}

	void setTerp(Terp t) {
		this.terp = t;
		this.TABLE_PARAMS = strs("cellpadding", "8", "border", "1");
	}

	public static class InspectorVisitor extends Visitor {
		Html ht;

		InspectorVisitor(Terp t) {
			super(t);
			ht = new Html();
		}

		public void visitUr(Ur a) {
			ht.append("\"Ur:\"" + a.toString());
		}

		public void visitCls(Cls a) {
			ht.append("\"Cls:\"" + a.toString());
		}

		public void visitNum(Num a) {
			Html.tag(ht, "span", strs("style", "color=#555555"),
					"" + a.toString());
		}

		public void visitStr(Str a) {
			Html.tag(ht, "span",
					strs("style", "color: #338033; font-family: courier"),
					a.toString());
		}

		public void visitUndefined(Undefined a) {
			ht.append("Nil");
		}

		public void visitVec(Vec a) {
			ht.append("Vec{");
			Html defs = new Html();
			for (int i = 0; i < a.vec.size(); i++) {
				InspectorVisitor valueVisitor = new InspectorVisitor(t);
				valueVisitor.ht.append(" ( ");
				a.vec.get(i).visit(valueVisitor);
				Html.tag(defs, "li", null, valueVisitor.ht.append(" );"));
			}
			Html.tag(ht, "ol", null, defs);
			ht.append("}");
		}

		public void visitDict(Dict a) {
			ht.append("Dict{");
			Html defs = new Html();
			Vec[] assocs = a.sortedAssocs();
			for (int i = 0; i < assocs.length; i++) {
				InspectorVisitor keyVisitor = new InspectorVisitor(t);
				assocs[i].vec.get(0).visit(keyVisitor);
				InspectorVisitor valueVisitor = new InspectorVisitor(t);
				assocs[i].vec.get(1).visit(valueVisitor);
				Html.tag(defs, "dt", null, keyVisitor.ht.append(";"));
				Html.tag(defs, "dd", null, valueVisitor.ht.append(";"));
			}
			Html.tag(ht, "dl", null, defs);
			ht.append("}");
		}

		public void visitUsr(Usr a) {
			ht.append("\"Usr:\"" + a.toString());
		}
	}

	String addSingleLetterQueryPartsToLink(String link,
			HashMap<String, String> query) {
		// Copy all single-letter query parts to the new link; they are sticky.
		boolean needsQuestion = link.indexOf('?') < 0; // If no '?' then it
														// needs a '?'
		for (String k : query.keySet()) {
			if (k.length() == 1) {
				link = link + (needsQuestion ? "?" : "&") + k + "="
						+ query.get(k);
				needsQuestion = false;
			}
		}
		return link;
	}

	Html maybeLink(String s, HashMap<String, String> query) {
		Matcher m = LINK_P.matcher(s);
		if (m.matches()) {
			String link = addSingleLetterQueryPartsToLink(m.group(1), query);
			String label = m.group(2);
			String text = m.group(3);
			String[] params = strs("href", link);
			Html z = Html.tag(null, "a", params, label);
			z.append(" ");
			z.append(text);
			return z;
		} else {
			return new Html(s);
		}
	}

	Html maybeLink(Ur x, HashMap<String, String> query) {
		return maybeLink(maybeString(x, null), query);
	}

	String maybeString(String s, String defaultString) {

		return s == null ? defaultString == null ? "nil" : defaultString : s;
	}

	String maybeString(Ur x, String defaultString) {
		if (x == null) {
			return defaultString == null ? "nil" : defaultString;
		}
		Str s = x.asStr();
		if (s == null) {
			return defaultString == null ? x.toString() : defaultString;
		}
		return s.str;
	}

	public class EditRenderer extends BaseRenderer {
		public EditRenderer(Dict dict, HashMap<String, String> query) {
			super(dict, query);
		}

		public Html innerHt() {
			String text = stringAt(dict, "value",
					"ERROR: Missing value in dict.");
			String action = stringAt(dict, "action",
					"ERROR: Missing action in dict.");
			Html page = new Html();

			// name=text wrap=virtual rows=25 cols=70 style=" width: 95%;  "
			Html form = new Html();
			String[] textareaParams = strs("name", "text", "wrap", "virtual",
					"rows", "25", "cols", "80", "style", "width; 95%");
			if (stringAt(dict, "field1") != null) {
				String fname = stringAt(dict, "field1", "?");
				String fvalue = stringAt(dict, "value1", "?");
				Html.tag(form, "b", null, fname);
				form.append(Html.entity("nbsp"));
				Html.tag(form, "input", strs("name", fname, "value", fvalue),
						"");
				Html.tag(form, "p", null, "");
			}
			if (stringAt(dict, "field2") != null) {
				String fname = stringAt(dict, "field2", "?");
				String fvalue = stringAt(dict, "value2", "?");
				Html.tag(form, "b", null, fname);
				form.append(Html.entity("nbsp"));
				Html.tag(form, "input", strs("name", fname, "value", fvalue),
						"");
				Html.tag(form, "p", null, "");
			}
			Html.tag(form, "textarea", textareaParams, text);
			Html.tag(form, "p", null, "");
			Html.tag(form, "input", strs("type", "submit", "value", "Submit"),
					"");
			Html.tag(form, "input", strs("type", "reset"), "");

			Html.tag(
					page,
					"form",
					strs("method", "POST", "action",
							addSingleLetterQueryPartsToLink(action, query)),
					form);
			return page;
		}
	}

	public class DrawRenderer extends BaseRenderer {
		public DrawRenderer(Dict dict, HashMap<String, String> query) {
			super(dict, query);
		}

		public Html innerHt() {
			Vec v = urAt(dict, "value").asVec();
			Html svg = new Html();

			for (int i = 0; i < v.vec.size(); i++) {
				Vec u = v.vec.get(i).asVec();
				if (u.vec.get(0).asStr().str.equals("text")) {
					String x1 = u.vec.get(1).asNum().toString();
					String y1 = u.vec.get(2).asNum().toString();
					String txt = u.vec.get(3).toString();
					// String rgb = "rgb(0,0,0)";
					String[] how = strs("x", x1, "y", y1);
					Html.tag(svg, "text", how, txt);
				}
				if (u.vec.get(0).asStr().str.equals("line")) {
					String x1 = u.vec.get(1).asNum().toString();
					String y1 = u.vec.get(2).asNum().toString();
					String x2 = u.vec.get(3).asNum().toString();
					String y2 = u.vec.get(4).asNum().toString();
					String wid = "1";
					if (u.vec.size() > 5)
						wid = u.vec.get(5).asNum().toString();
					String rgb = "rgb(0,0,0)";
					if (u.vec.size() > 6)
						rgb = u.vec.get(6).asStr().str;
					String[] how = strs("x1", x1, "y1", y1, "x2", x2, "y2", y2,
							"style", fmt("stroke:%s;stroke-width:%s", rgb, wid));
					Html.tag(svg, "line", how);
				}
				if (u.vec.get(0).asStr().str.equals("rect")) {
					String x = u.vec.get(1).asNum().toString();
					String y = u.vec.get(2).asNum().toString();
					String width = u.vec.get(3).asNum().toString();
					String height = u.vec.get(4).asNum().toString();
					String wid = "1";
					if (u.vec.size() > 5)
						wid = u.vec.get(5).asNum().toString();
					String rgb = "rgb(0,0,0)";
					if (u.vec.size() > 6)
						rgb = u.vec.get(6).asStr().str;
					String[] how = strs("x", x, "y", y, "width", width,
							"height", height, "stroke-width", wid, "fill", rgb);
					Html.tag(svg, "rect", how);
				}
			}

			String[] params = strs("xmlns", "http://www.w3.org/2000/svg",
					"version", "1.1", "xmlns:xlink",
					"http://www.w3.org/1999/xlink");
			Ur width = dict.dict.get(terp.newStr("width"));
			Ur height = dict.dict.get(terp.newStr("height"));
			if (width != null)
				params = append(append(params, "width"), width.asNum()
						.toString());
			if (height != null)
				params = append(append(params, "height"), height.asNum()
						.toString());

			return Html.tag(null, "svg", params, svg);
		}
	}

	public class TextRenderer extends BaseRenderer {
		public TextRenderer(Dict dict, HashMap<String, String> query) {
			super(dict, query);
		}

		public Html innerHt() {
			String text = stringAt(dict, "value",
					"ERROR: Missing value in dict.");
			Html page = new Html();
			Html.tag(page, "pre", null, text);
			return page;
		}
	}

	public class HtmlRenderer extends BaseRenderer {
		public HtmlRenderer(Dict dict, HashMap<String, String> query) {
			super(dict, query);
		}

		public Html innerHt() {
			return htmlAt(dict, "value");
		}
	}

	public class ListRenderer extends BaseRenderer {
		public ListRenderer(Dict dict, HashMap<String, String> query) {
			super(dict, query);
		}

		public Html innerHt() {
			Html rows = new Html();
			Vec list = urAt(dict, "value").mustVec();
			int llen = list.vec.size();
			for (int i = 0; i < llen; i++) {
				Ur elem = list.vec.get(i);
				Vec tuple = elem.asVec();
				Html cols = new Html();
				if (tuple != null) {
					// Old Style
					for (int j = 0; j < tuple.vec.size(); j++) {
						Ur x = tuple.vec.get(j);
						Html.tag(cols, "td", null, maybeLink(x, query));
					}
				} else {
					// New Style
					String s = elem.toString();
					if (s.charAt(0) == '/') {
						Html.tag(cols, "td", null, Html.tag(null, "a", strs("href", s), s));
					} else {
						Html.tag(cols, "td", null, s);
					}
				}
				Html.tag(rows, "tr", null, cols);
			}
			Html page = new Html();
			Html.tag(page, "table", TABLE_PARAMS, rows);
			return page;
		}
	}

	public class BaseRenderer {
		Dict dict;
		HashMap<String, String> query;

		public BaseRenderer(Dict dict, HashMap<String, String> query) {
			this.dict = dict;
			this.query = query;
		}

		public Html toHt() {
			try {
				Html head = new Html();
				Html.tag(head, "title", null,
						stringAt(dict, "title", "Untitled"));

				Html body = new Html();
				Html.tag(body, "b", null, stringAt(dict, "title", "Untitled"));
				Html.tag(body, "hr", null, "");
				body.append(this.innerHt());
				Html.tag(body, "hr", null, "");

				// Ask Home for standard footer buttons, and render them.
				Vec fb = terp.newTmp().eval("Top footerButtons").asVec();
				Html footer = new Html();
				String nextLink = stringAt(dict, "url");
				if (nextLink != null) {
					footer.appendLink(nextLink, "NEXT");
				}
				for (int i = 0; i < fb.vec.size(); i++) {
					String s = ((Ur) fb.vec.get(i)).asStr().str;
					footer.append(Html.entity("nbsp"));
					footer.append(maybeLink(s, query));
					footer.append(Html.entity("nbsp"));
				}
				Html.tag(body, "tt", null, footer);
				Html.tag(body, "p", null, "");
				Html.tag(body, "small", null, "\"DEBUG:\"" + dict.toString());

				return Html.tag(null, "html", null, head.append(body));

			} catch (Exception ex) {
				StackTraceElement[] arr = ex.getStackTrace();

				Html pre = new Html();
				pre.append(ex.toString());
				for (StackTraceElement x : arr) {
					pre.append("\n***  " + x.toString());
				}

				Html z = new Html();
				Html.tag(z, "pre", null, pre);
				Html.tag(z, "hr", null, "");
				Html.tag(z, "small", null, dict.toString());
				return z;
			}
		}

		Html innerHt() {
			// A crude fallback if 'type' field is unknown.
			InspectorVisitor vis = new InspectorVisitor(dict.terp());
			vis.visitDict(dict);
			return vis.ht;
		}
	}

	public class TerpHandler extends Static implements HttpHandler {
		public TerpHandler() {
			super();
			System.err.println("Initialized Terp Hander.");
		}

		public void handle(HttpExchange t) throws IOException {
			// beginNewTerp();
			System.err.println("handle().");
			String response = "?";
			String responseType = "text/plain";
			try {
				// Path and body query.
				URI uri = t.getRequestURI();
				String path = uri.getPath();
				InputStream is = t.getRequestBody();
				String bodyQuery = readAll(is);

				// Url query.
				HashMap<String, String> query = new HashMap<String, String>();
				String uriQuery = uri.getQuery();
				uriQuery = uriQuery == null ? "" : uriQuery; // Don't be null.

				String[] parts = (uriQuery + "&" + bodyQuery).split("&");
				for (String part : parts) {
					String[] kv = part.split("=", 2);
					if (kv.length == 2)
						query.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
				}

				// // Special Hack while debugging and testing:
				// // Going HOME to '/' actually resets the terp.
				// if (path.equals("/")) {
				// beginNewTerp(Static.STANDARD_INIT_FILENAME);
				// }

				try {
					// Create a job.
					Async.Job job = async.newJob(path, query);
					async.inQueue.put(job);
					// Block waiting for a reply.
					Async.Result reply = job.reply.take();
					setTerp(reply.terp);

					Dict dict = reply.renderMe;
					String raw = query.get("raw");

					String type = stringAt(dict, "type");
					BaseRenderer r;
					if (raw != null) {
						r = new BaseRenderer(dict, query);
					} else if (type.equals("list")) {
						r = new ListRenderer(dict, query);
					} else if (type.equals("text")) {
						r = new TextRenderer(dict, query);
					} else if (type.equals("edit")) {
						r = new EditRenderer(dict, query);
					} else if (type.equals("draw")) {
						r = new DrawRenderer(dict, query);
					} else if (type.equals("html")) {
						r = new HtmlRenderer(dict, query);
					} else {
						r = new BaseRenderer(dict, query); // Crude fallback.
					}

					response = r.toHt().toString();
					responseType = "text/html";
				} catch (Exception ex) {
					ex.printStackTrace();
					response = "*** ERROR *** " + ex;
					System.err.println("[1] " + response);
				}
			} catch (Exception ex) {
				response = "*** (Outer handle) ERROR ***" + ex;
				System.err.println("[2] " + response);
			}

			Headers respHeads = t.getResponseHeaders();
			respHeads.set("Content-Type", responseType);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}

		public String readAll(InputStream is) throws IOException {
			StringBuffer b = new StringBuffer();
			while (true) {
				int c = is.read();
				if (c < 0)
					break;
				b.append((char) c);
			}
			return b.toString();
		}
	}

	public static class FavIconHandler extends Static implements HttpHandler {

		public void handle(HttpExchange t) throws IOException {
			Headers respHeads = t.getResponseHeaders();
			respHeads.set("Content-Type", "text/plain");
			t.sendResponseHeaders(400, 0);
			OutputStream os = t.getResponseBody();
			os.close();
		}
	}

	public void run(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 5);
		server.createContext("/favicon.ico", new FavIconHandler());
		server.createContext("/", new TerpHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	public static void main(String[] args) throws IOException {
		new WebServer().run(args);
	}

	public static class WebTerp extends Terp.PosixTerp {

		public WebTerp(boolean loadPrelude, String worldName)
				throws IOException {
			super(loadPrelude, worldName);
		}

		static FileWriter logWriter;

		@Override
		public String say(String s, Object... objects) {
			try {
				if (logWriter == null) {
					logWriter = new FileWriter("__log_webterp.txt");
				}
				String msg = super.say(s, objects); // to stderr.
				logWriter.write(msg);
				logWriter.write("\n");
				logWriter.flush();
				return msg;
			} catch (IOException e) {
				e.printStackTrace();
				throw new AssertionError(e.toString());
			}
		}
	}

	public static class WebTerpFactory implements Factory {

		@Override
		public Terp createTerp(boolean loadPrelude, String worldName)
				throws IOException {
			return new WebTerp(loadPrelude, worldName);
		}

	}
}
