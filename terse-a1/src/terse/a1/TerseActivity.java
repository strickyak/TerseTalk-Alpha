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
package terse.a1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

import terse.vm.Cls;
import terse.vm.Parser;
import terse.vm.Wrap;
import terse.vm.Terp.ICanv;
import terse.vm.Terp.IInk;
import terse.vm.Ur;
import terse.vm.Ur.Blk;
import terse.vm.Ur.Num;
import terse.vm.Static;
import terse.vm.Terp;
import terse.vm.Ur.Dict;
import terse.vm.Ur.Obj;
import terse.vm.Ur.Str;
import terse.vm.Ur.Vec;
import terse.vm.Usr;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TerseActivity extends Activity {

	public static Thread singleWorkThread;
	public static AndyTerp terp;
	public static String terp_error;
	public static String world = "tmp0";

	private static int nextWorkThreadSerial = 0;

	protected String taSaveMe;
	String taPath;
	String taQueryStr;
	HashMap<String, String> taQuery;
	boolean taGotLongClick = false;

	GLSurfaceView glSurfaceView;

	static Pattern LINK_P = Pattern
			.compile("[|]link[|](/[-A-Za-z_0-9.:]*)[|]([^|]+)[|](.*)");

	public Context context() {
		return this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		if (glSurfaceView != null) {
			glSurfaceView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		if (glSurfaceView != null) {
			glSurfaceView.onResume();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		glSurfaceView = null; // Forget gl on new activity.

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("TerseActivity")) {
			taSaveMe = savedInstanceState.getString("TerseActivity");
		} else {
			taSaveMe = null;
		}

		Runnable bg = new Runnable() {
			@Override
			public void run() {
				resetTerp();
			}
		};

		Runnable fg = new Runnable() {
			@Override
			public void run() {
				Intent intent = getIntent();
				Uri uri = intent.getData();
				Bundle extras = intent.getExtras();
				String path = uri == null ? "/" : uri.getPath();
				String query = uri == null ? "" : uri.getQuery();

				viewPath(path, query, extras, savedInstanceState);
			}
		};
		if (terp == null) {
			TextView tv = new TextView(TerseActivity.this);
			tv.setText(Static.fmt(
					"Building new TerseTalk VM for world <%s>...", world));
			tv.setTextAppearance(this, R.style.teletype);
			tv.setBackgroundColor(Color.BLACK);
			tv.setTextColor(Color.DKGRAY);
			tv.setTextSize(24);
			setContentView(tv);
			setContentViewThenBgThenFg("ResetSplash", tv, bg, fg);
		} else {
			fg.run();
		}
	}

	void setContentViewThenBgThenFg(final String threadName, final View v,
			final Runnable bg, final Runnable fg) {
		setContentView(v);
		class BgThread extends Thread {
			BgThread() {
				super(threadName);
			}

			@Override
			public void run() {
				// terp.say("ThenBgThenFg: Running BG for %s", name);
				if (bg != null)
					bg.run();
				// terp.say("ThenBgThenFg: Scheduling FG in UI for %s", name);
				runOnUiThread(fg);
			}
		}
		// terp.say("ThenBgThenFg: Starting BG thread for %s", name);
		new BgThread().start();
	}

	void resetTerp() {
		try {
			terp = new AndyTerp(true, world);

			// Let's explore the disk.
			File theDir = getFilesDir();
			terp.say("DIR: %s", theDir);
			String[] names = theDir.list();
			for (int i = 0; i < names.length; i++) {
				File theFile = new File(theDir, names[i]);
				terp.say("FILE[%d]: <%s> time %d len %d", i,
						theFile.getAbsoluteFile(), theFile.lastModified(),
						theFile.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
			terp_error = e.toString();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("TerseActivity", taSaveMe);
	}

	public static class Motion extends Obj {
		private View v;
		private MotionEvent ev;
		private Blk block;

		// =cls "Android" Motion Obj
		public Motion(AndyTerp t, View v, MotionEvent ev, Blk block) {
			super(t.wrapandy.clsMotion);
			this.v = v;
			this.ev = ev;
			this.block = block;
		}

		// =meth Motion "access" x
		public double _x() {
			// return ev.getX();
			return ev.getRawX() - v.getLeft();
		}

		// =meth Motion "access" y
		public double _y() {
			// return ev.getY();
			return ev.getRawY() - v.getTop();
		}

		// =meth Motion "access" action
		public int _action() {
			return ev.getAction();
		}
	}

	public class AndyTerp extends Terp {
		final public WrapAndy wrapandy;
		final BlockingQueue<Motion> eventQueue = new ArrayBlockingQueue<Motion>(
				64);

		protected AndyTerp(boolean loadPrelude, String imageName)
				throws IOException {
			super(loadPrelude, imageName);
			wrapandy = new WrapAndy();
			wrapandy.installClasses(this);
			wrapandy.installMethods(this);
		}

		public void clearEventQueue() {
			eventQueue.clear();
		}

		@Override
		public String say(String s, Object... objects) {
			String msg = fmt(s, objects);
			Log.i("Terse", msg);
			recordLog(msg);
			return msg;
		}

		@Override
		public void loadPrelude() throws IOException {
			new InitialWorldReader("pre0").loadReader(preludeReader());
		}

		@Override
		public FileInputStream openFileRead(String filename)
				throws FileNotFoundException {
			return openFileInput(filename);
		}

		@Override
		public FileOutputStream openFileWrite(String filename)
				throws FileNotFoundException {
			return openFileOutput(filename, Context.MODE_WORLD_READABLE
					| Context.MODE_WORLD_WRITEABLE);
		}

		@Override
		public FileOutputStream openFileAppend(String filename)
				throws FileNotFoundException {
			return openFileOutput(filename, Context.MODE_WORLD_READABLE
					| Context.MODE_WORLD_WRITEABLE | Context.MODE_APPEND);
		}

		@Override
		public File getFilesDir() {
			return context().getFilesDir();
		}

		@Override
		public void pushToWeb(String filename, String content) {
			terp.checkTxtFileNameSyntax(filename);
			String basename = filename.substring(0, filename.indexOf('.'));
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(fmt("%s.push.%s", YAK_WEB_PAGE,
					basename));

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("content", content));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					line.length(); // Ignore it for now.
				}
			} catch (IOException e) {
				e.printStackTrace();
				toss("Error during pushToWeb <%s>: %s", filename, e);
			}
		}

		@Override
		public String pullFromWeb(String filename) {
			terp.checkTxtFileNameSyntax(filename);
			String basename = filename.substring(0, filename.indexOf('.'));
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet post = new HttpGet(
					fmt("%s.pull.%s", YAK_WEB_PAGE, basename));

			try {
				HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuilder sb = new StringBuilder();
				while (true) {
					String line = rd.readLine();
					if (line == null)
						break;
					sb.append(line);
					sb.append('\n');
				}
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
				toss("Error during pullFromWeb <%s>: %s", filename, e);
				return null;
			}
		}

		@Override
		public Vec listOfWebFiles() {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet post = new HttpGet(fmt("%s.dir", YAK_WEB_PAGE));

			try {
				HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				Vec z = terp.newVec(emptyUrs);
				while (true) {
					String line = rd.readLine();
					if (line == null)
						break;
					if (line.startsWith("<li>")) {
						line = line.substring(4);
						if (line.startsWith("<tt>")) {
							line = line.substring(4);
						}
						if (line.endsWith("</tt>")) {
							line = line.substring(0, line.length() - 5);
						}
						String[] words = line.split(" ");
						if (words.length == 3) {
							z.vec.add(new Vec(terp, urs(terp.newStr(words[0]),
									terp.newNum(Integer.parseInt(words[1])),
									terp.newNum(Integer.parseInt(words[2])))));
						}
					}
				}
				return z;
			} catch (IOException e) {
				e.printStackTrace();
				toss("Error during listOfWebFiles: %s", e);
				return null;
			}
		}

		private class WorkThread extends Thread {
			private Runnable innerRunnable;
			private String desc;
			private int serial;

			WorkThread(Runnable runnable, String desc) {
				this.innerRunnable = runnable;
				this.desc = desc;
				this.serial = nextWorkThreadSerial;
				++nextWorkThreadSerial;
				terp.say("WorkThread #%d CTOR<%s>", serial, desc);
			}

			@Override
			public void run() {
				terp.say("WorkThread #%d RUN<%s>", serial, desc);
				// Wait for a running WorkThread to remove itself.
				while (singleWorkThread != null) {
					// Ask the running WorkThread to die.
					terp.say("WorkThread #%d TickDown<%s>: %s", serial, desc,
							AndyTerp.this.tickCounter);
					AndyTerp.this.tickCounter = 0;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// Don't care if we wake up early.
					}
				}
				singleWorkThread = this;
				AndyTerp.this.tickCounter = Integer.MAX_VALUE;
				try {
					if (innerRunnable != null)
						innerRunnable.run();
				} catch (final Terp.TooManyTicks ex) {
					// Toast.makeText(getApplicationContext(),
					// Static.fmt("TOO MANY TICKS: %s", desc),
					// Toast.LENGTH_SHORT).show();
					terp.say(
							"WorkThread #%d CATCH<%s>: Dying quietly on TooManyTicks",
							serial, desc);
				} catch (final RuntimeException ex) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView tv = new TextView(TerseActivity.this);
							tv.setText(fmt(
									"EXCEPTION IN WorkThread #%d <%s>:\n\n%s",
									serial, desc, ex));
							SetContentViewWithHomeButtonAndScroll(tv);
						}
					});
				} finally {
					if (singleWorkThread == this) {
						singleWorkThread = null;
					}
				}
				terp.say("WorkThread #%d DONE<%s>", serial, desc);
			}
		}

		public void runOnWorkThread(Runnable runnable, String desc) {
			new WorkThread(runnable, desc).start();
		}

		@Override
		public boolean deleteFile(String filename) {
			File dir = getFilesDir();
			File f = new File(dir, filename);
			return f.delete();
			// TODO: some regexp.
		}

	}

	public InputStreamReader preludeReader() {
		InputStream is = getResources().openRawResource(R.raw.prelude);
		return new InputStreamReader(is);
	}

	public void viewPath(String path, String queryStr, Bundle extras,
			Bundle savedInstanceState) {
		// Stop any running WorkThread before we continue.
		viewPath1prepare(path, queryStr);
		viewPath2parseQuery(queryStr, extras);

		final LayoutParams widgetParams = new LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);

		TextView splash = new TextView(TerseActivity.this);
		splash.setText("Launching\n\n" + taPath + "\n\n"
				+ Static.hashMapToMultiLineString(taQuery));
		splash.setTextAppearance(this, R.style.teletype);
		splash.setBackgroundColor(Color.BLACK);
		splash.setTextColor(Color.DKGRAY);
		splash.setTextSize(24);

		Runnable bg = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		Runnable fg = new Runnable() {
			@Override
			public void run() {
				viewPath9display(taPath, widgetParams);
			}
		};

		setContentViewThenBgThenFg("viewPath8", splash, bg, fg);
	}

	private void viewPath2parseQuery(String queryStr, Bundle extras) {
		// Url query.
		taQuery = new HashMap<String, String>();
		queryStr = queryStr == null ? "" : queryStr; // Don't be null.

		String[] parts = queryStr.split("&");
		for (String part : parts) {
			String[] kv = part.split("=", 2);
			if (kv.length == 2)
				try {
					taQuery.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					terp.toss("%s", e);
				}
		}
		if (extras != null) {
			for (String key : extras.keySet()) {
				taQuery.put(key, extras.getString(key));
			}
		}
	}

	private void viewPath9display(String path, LayoutParams widgetParams) {
		String explain;
		if (terp_error != null) {
			explain = "terp_error = " + terp_error;
		} else {
			try {
				terp.say("Sending to terp: %s", path);
				final Dict d = terp.handleUrl(path, taQuery);
				explain = "DEFAULT EXPLANATION:\n\n" + d.toString();

				Str TYPE = d.cls.terp.newStr("type");
				Str VALUE = d.cls.terp.newStr("value");
				Str TITLE = d.cls.terp.newStr("title");
				Str type = (Str) d.dict.get(TYPE);
				Ur value = d.dict.get(VALUE);
				Ur title = d.dict.get(TITLE);

				// {
				// double ticks = Static.floatAt(d, "ticks", -1);
				// double nanos = Static.floatAt(d, "nanos", -1);
				// Toast.makeText(
				// getApplicationContext(),
				// Static.fmt("%d ticks, %.3f secs", (long) ticks,
				// (double) nanos / 1e9), Toast.LENGTH_SHORT)
				// .show();
				// }

				if (type.str.equals("list") && value instanceof Vec) {
					final ArrayList<Ur> v = ((Vec) value).vec;
					final ArrayList<String> labels = new ArrayList<String>();
					final ArrayList<String> links = new ArrayList<String>();
					for (int i = 0; i < v.size(); i++) {
						Ur item = v.get(i);
						String label = item instanceof Str ? ((Str) item).str
								: item.toString();
						if (item instanceof Vec && ((Vec) item).vec.size() == 2) {
							// OLD STYLE
							label = ((Vec) item).vec.get(0).toString();
							Matcher m = LINK_P.matcher(label);
							if (m.lookingAt()) {
								label = m.group(2) + " " + m.group(3);
							}
							label += "    ["
									+ ((Vec) item).vec.get(1).toString()
											.length() + "]";
							links.add(null); // Use old style, not links.
						} else {
							// NEW STYLE
							label = item.toString();
							if (label.charAt(0) == '/') {
								String link = Terp.WHITE_PLUS.split(label, 2)[0];
								links.add(link);
							} else {
								links.add("");
							}
						}
						labels.add(label);
					}
					if (labels.size() != links.size())
						terp.toss("lables#%d links#%d", labels.size(),
								links.size());

					ListView listv = new ListView(this);
					listv.setAdapter(new ArrayAdapter<String>(this,
							R.layout.list_item, labels));
					listv.setLayoutParams(widgetParams);
					listv.setTextFilterEnabled(true);

					listv.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// When clicked, show a toast with the TextView text
							// Toast.makeText(getApplicationContext(),
							// ((TextView) view).getText(),
							// Toast.LENGTH_SHORT).show();
							String toast_text = ((TextView) view).getText()
									.toString();
							// if (v.get(position) instanceof Vec) {
							if (links.get(position) == null) {
								// OLD STYLE
								Vec pair = (Vec) v.get(position);
								if (pair.vec.size() == 2) {
									if (pair.vec.get(0) instanceof Str) {
										String[] words = ((Str) pair.vec.get(0)).str
												.split("\\|");
										Log.i("TT-WORDS",
												terp.arrayToString(words));
										toast_text += "\n\n"
												+ Static.arrayToString(words);
										if (words[1].equals("link")) {
											Uri uri = new Uri.Builder()
													.scheme("terse")
													.path(words[2]).build();
											Intent intent = new Intent(
													"android.intent.action.MAIN",
													uri);
											intent.setClass(
													getApplicationContext(),
													TerseActivity.class);

											startActivity(intent);
										}
									}
								}
							} else {
								// NEW STYLE
								terp.say(
										"NEW STYLE LIST SELECT #%d link=<%s> label=<%s>",
										position, links.get(position),
										labels.get(position));
								if (links.get(position).length() > 0) {
									Uri uri = new Uri.Builder().scheme("terse")
											.path(links.get(position)).build();
									Intent intent = new Intent(
											"android.intent.action.MAIN", uri);
									intent.setClass(getApplicationContext(),
											TerseActivity.class);

									startActivity(intent);
								}
							}
							// }
							// Toast.makeText(getApplicationContext(),
							// ((TextView) view).getText(),
							// Toast.LENGTH_SHORT).show();
						}
					});
					setContentView(listv);
					return;
				} else if (type.str.equals("edit") && value instanceof Str) {
					final EditText ed = new EditText(this);

					ed.setText(taSaveMe == null ? value.toString() : taSaveMe);

					ed.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_FLAG_MULTI_LINE
							| InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
							| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					ed.setLayoutParams(widgetParams);
					// ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
					ed.setTextAppearance(this, R.style.teletype);
					ed.setBackgroundColor(Color.BLACK);
					ed.setGravity(Gravity.TOP);
					ed.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
					ed.setVerticalFadingEdgeEnabled(true);
					ed.setVerticalScrollBarEnabled(true);
					ed.setOnKeyListener(new OnKeyListener() {
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							// If the event is a key-down event on the "enter"
							// button
							// if ((event.getAction() == KeyEvent.ACTION_DOWN)
							// &&
							// (keyCode == KeyEvent.KEYCODE_ENTER)) {
							// // Perform action on key press
							// Toast.makeText(TerseActivity.this, ed.getText(),
							// Toast.LENGTH_SHORT).show();
							// return true;
							// }
							return false;
						}
					});

					Button btn = new Button(this);
					btn.setText("Save");
					btn.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							// Perform action on clicks
							String text = ed.getText().toString();
							text = Parser.charSubsts(text);
							Toast.makeText(TerseActivity.this, text,
									Toast.LENGTH_SHORT).show();
							String action = stringAt(d, "action");
							String query = "";

							String f1 = stringAt(d, "field1");
							String v1 = stringAt(d, "value1");
							String f2 = stringAt(d, "field2");
							String v2 = stringAt(d, "value2");
							f1 = (f1 == null) ? "f1null" : f1;
							v1 = (v1 == null) ? "v1null" : v1;
							f2 = (f2 == null) ? "f2null" : f2;
							v2 = (v2 == null) ? "v2null" : v2;

							startTerseActivity(action, query,
									stringAt(d, "field1"),
									stringAt(d, "value1"),
									stringAt(d, "field2"),
									stringAt(d, "value2"), "text", text);
						}
					});

					LinearLayout linear = new LinearLayout(this);
					linear.setOrientation(LinearLayout.VERTICAL);
					linear.addView(btn);
					linear.addView(ed);
					setContentView(linear);
					return;

				} else if (type.str.equals("draw") && value instanceof Vec) {
					Vec v = ((Vec) value);
					DrawView dv = new DrawView(this, v.vec, d);
					dv.setLayoutParams(widgetParams);
					setContentView(dv);
					return;
				} else if (type.str.equals("live")) {
					Blk blk = value.mustBlk();
					Blk event = Static.urAt(d, "event").asBlk();
					TerseSurfView tsv = new TerseSurfView(this, blk, event);
					setContentView(tsv);
					return;
				} else if (type.str.equals("fnord")) {
					Obj app = value.mustObj();
					FnordView fnord = new FnordView(this, app);
					setContentView(fnord);
					return;
				} else if (type.str.equals("world") && value instanceof Str) {
					String newWorld = value.toString();
					if (Terp.WORLD_P.matcher(newWorld).matches()) {
						world = newWorld;
						resetTerp();
						explain = Static
								.fmt("Switching to world <%s>\nUse menu to go Home.",
										world);
						Toast.makeText(getApplicationContext(), explain,
								Toast.LENGTH_LONG).show();
					} else {
						terp.toss(
								"Bad world syntax (must be 3 letters then 0 to 3 digits: <%s>",
								newWorld);
					}
					// Fall thru for explainv.setText(explain).
				} else if (type.str.equals("text")) {
					explain = "<<< " + title + " >>>\n\n" + value.toString();
					// Fall thru for explainv.setText(explain).
				} else if (type.str.equals("html")) {
					final WebView webview = new WebView(this);
					// webview.loadData(value.toString(), "text/html", null);
					webview.loadDataWithBaseURL("terse://terse",
							value.toString(), "text/html", "UTF-8", null);
					webview.setWebViewClient(new WebViewClient() {
						@Override
						public boolean shouldOverrideUrlLoading(WebView view,
								String url) {
							// terp.say("WebView UrlLoading: url=%s", url);
							URI uri = URI.create("" + url);
							// terp.say("WebView UrlLoading: URI=%s", uri);
							terp.say("WebView UrlLoading: getPath=%s",
									uri.getPath());
							terp.say("WebView UrlLoading: getQuery=%s",
									uri.getQuery());

							// Toast.makeText(getApplicationContext(),
							// uri.toASCIIString(), Toast.LENGTH_SHORT)
							// .show();
							// webview.invalidate();
							//
							// TextView quick = new
							// TextView(TerseActivity.this);
							// quick.setText(uri.toASCIIString());
							// quick.setBackgroundColor(Color.BLACK);
							// quick.setTextColor(Color.WHITE);
							// setContentView(quick);

							startTerseActivity(uri.getPath(), uri.getQuery());

							return true;
						}
					});

					// webview.setWebChromeClient(new WebChromeClient());
					webview.getSettings().setBuiltInZoomControls(true);
					// webview.getSettings().setJavaScriptEnabled(true);
					webview.getSettings().setDefaultFontSize(18);
					webview.getSettings().setNeedInitialFocus(true);
					webview.getSettings().setSupportZoom(true);
					webview.getSettings().setSaveFormData(true);
					setContentView(webview);

					// ScrollView scrollv = new ScrollView(this);
					// scrollv.addView(webview);
					// setContentView(scrollv);
					return;
				} else {
					explain = "Unknown page type: " + type.str
							+ " with vaule type: " + value.cls
							+ "\n\n##############\n\n";
					explain += value.toString();
					// Fall thru for explainv.setText(explain).
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				explain = Static.describe(ex);
			}
		}

		TextView explainv = new TextView(this);
		explainv.setText(explain);
		explainv.setBackgroundColor(Color.BLACK);
		explainv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		explainv.setTextColor(Color.YELLOW);

		SetContentViewWithHomeButtonAndScroll(explainv);
	}

	private void postMortem(Throwable ex) {
		String explain = Static.describe(ex);
		TextView explainv = new TextView(this);
		explainv.setText(explain);
		explainv.setBackgroundColor(Color.BLACK);
		explainv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		explainv.setTextColor(Color.RED);

		SetContentViewWithHomeButtonAndScroll(explainv);
	}

	private void viewPath1prepare(String path, String queryStr) {
		try {
			Thread.sleep(50);
			terp.runOnWorkThread(null, "null");
			Thread.sleep(50);
			terp.runOnWorkThread(null, "null");
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		terp.clearEventQueue();
		terp.tickCounter = Integer.MAX_VALUE; // // TRYTHIS

		if (path.toLowerCase().equals("/reset")) {
			resetTerp();
			path = "/Top";
		}
		this.taPath = path;
		this.taQueryStr = queryStr;
	}

	void SetContentViewWithHomeButtonAndScroll(View v) {
		Button btn = new Button(this);
		btn.setText("[HOME]");
		// btn.setTextSize(15);
		// btn.setHeight(25);
		// btn.setMaxHeight(25);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				startTerseActivity("/Top", "");
			};
		});

		LinearLayout linear = new LinearLayout(this);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.addView(btn);
		linear.addView(v);

		ScrollView scrollv = new ScrollView(this);
		scrollv.addView(linear);
		setContentView(scrollv);
	}

	@Override
	public void setContentView(View view) {
		// view.setOnCreateContextMenuListener(this);
		super.setContentView(view);
	}

	class DrawView extends View {
		ArrayList<Ur> list;
		Dict dict;

		public DrawView(Context context, ArrayList<Ur> list, Dict dict) {
			super(context);
			this.list = list;
			this.dict = dict;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Paint green = new Paint();
			green.setColor(Color.GREEN);
			green.setStrokeWidth(2);

			Paint blue = new Paint();
			blue.setColor(Color.BLUE);
			blue.setStrokeWidth(1);

			Paint yellow = new Paint();
			yellow.setColor(Color.YELLOW);
			yellow.setStrokeWidth(1);
			yellow.setTextSize(12);

			for (int i = 0; i < list.size(); i++) {
				Ur p = list.get(i);
				String drawtype = stringAt(p, 0);
				if (drawtype.equals("line")) {
					float x1 = floatAt(p, 1);
					float y1 = floatAt(p, 2);
					float x2 = floatAt(p, 3);
					float y2 = floatAt(p, 4);
					canvas.drawLine(x1, y1, x2, y2, green);
				} else if (drawtype.equals("rect")) {
					float x1 = floatAt(p, 1);
					float y1 = floatAt(p, 2);
					float x2 = floatAt(p, 3) + x1;
					float y2 = floatAt(p, 4) + y1;
					canvas.drawRect(x1, y1, x2, y2, blue);
				} else if (drawtype.equals("text")) {
					float x1 = floatAt(p, 1);
					float y1 = floatAt(p, 2);
					String text = stringAt(p, 3);
					canvas.drawText(text, x1, y1, yellow);
				}
			}

			setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();
					// NOT IN 2.1: int flags = event.getFlags();
					float x = event.getX();
					float y = event.getY();
					terp.say("TOUCH: %s %s %s", action, x, y);
					if (action == MotionEvent.ACTION_UP) {
						terp.say("ACTION_UP: %s %s %s", action, x, y);
						// If they gave us an url, use it.
						String nextPath = stringAt(dict, "url");
						// Otherwise, use the same path.
						nextPath = nextPath == null ? taPath : nextPath;
						startTerseActivity(nextPath, taQueryStr, "action",
								"up", "ex", Float.toString(x), "ey",
								Float.toString(y));
						return true;
					}
					return false;
				}
			});

			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					terp.say("CLICK");
				}
			});

			setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					terp.say("LONG CLICK");
					taGotLongClick = true;
					return false;
				}
			});
		}
	}

	public static class Screen extends Obj {
		AndyTerp aterp;
		public SurfaceHolder holder;
		public View view;
		private Bitmap bm = null;
		private Canvas canv = null;
		// =get Screen int width width
		public int width = 0;
		// =get Screen int height height
		public int height = 0;
		public double firstPost = 0;
		public double lastPost = 0;
		public int numPosts = 0;

		// =cls "draw" Screen Usr
		public Screen(AndyTerp terp, SurfaceHolder holder, View view,
				int width, int height) {
			super(terp.wrapandy.clsScreen);
			this.aterp = terp;
			this.holder = holder;
			this.view = view;
			this.width = width;
			this.height = height;
		}

		// =meth Screen . newInk:
		public Ink newInk_(int colorRgbDecimal) {
			return new Ink(aterp, this, colorRgbDecimal);
		}

		public synchronized Canvas canvas() {
			if (canv == null) {
				bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				say("createBitmap w=%d h=%d -> %s", width, height, bm);
				canv = new Canvas(bm);
			}
			return canv;
		}

		// =meth Screen "draw" fps
		public Obj _fps() {
			if (numPosts > 1) {
				return aterp.newNum(numPosts / (lastPost - firstPost) * 1000);
			} else {
				return aterp.instNil;
			}
		}

		// =meth Screen "draw" post
		public synchronized void post() {
			canvas(); // allocate bm & canv, if needed.
			bm.prepareToDraw();
			Canvas locked = holder.lockCanvas();
			if (locked != null) {
				try {
					locked.drawBitmap(bm, 0, 0, null);
					if (firstPost == 0) {
						firstPost = System.currentTimeMillis();
					}
					lastPost = System.currentTimeMillis();
					++numPosts;
				} finally {
					holder.unlockCanvasAndPost(locked);
					view.postInvalidate();
				}
			} else {
				toss("locked is NULL");
			}

			// Handle Events
			while (true) {
				Motion mot = terp.eventQueue.poll();
				if (mot == null)
					break;
				int action = mot._action();
				double x = mot._x();
				double y = mot._y();
				Vec xy = terp.newVec(urs(terp.newNum(x), terp.newNum(y)));
				terp.say("MOTION CALLBACK: %s %s --> <%s>", action, xy,
						mot.block);
				mot.block.evalWith2Args(terp.newNum(action), xy);
			}
		}

		// =meth Screen "draw" clear:
		public void clear_(int rgbDecimal) {
			canvas().drawColor(Ink.colorDecimalToColor(rgbDecimal));
		}

		// =meth ScreenCls "sys" work:
		// "stop any running WorkThread and start the block arg."
		public static void work_(Terp terp, Blk block) {
			final Blk b = block;
			terp.toss("METHOD work: DISABLED: <%sd>", b);
			((AndyTerp) terp).runOnWorkThread(new Runnable() {
				@Override
				public void run() {
					b.evalWithoutArgs();
				}
			}, fmt("evalWithoutArgs[%s]", b));
		}
	}

	public static class Ink extends Obj {
		AndyTerp aterp;

		public Paint paint;
		// =get Ink . scr scr
		public Screen scr;

		// =cls "draw" Ink Usr
		public Ink(AndyTerp terp, Screen scr, int colorRgbDecimal) {
			super(terp.wrapandy.clsInk);
			this.aterp = terp;
			this.scr = scr;
			this.paint = new Paint();
			this.paint.setColor(colorDecimalToColor(colorRgbDecimal));
		}

		public Canvas canvas() {
			return scr.canvas();
		}

		// public void setFont(String a) {
		// Typeface tf = ???;
		// paint.setTypeface(a);
		// }

		// =meth Ink "live" fontsize:
		public void setFontSize(int a) {
			paint.setTextSize(a);
		}

		// =meth Ink "live" thick:
		public void setThickness(int pixels) {
			paint.setStrokeWidth(pixels);
		}

		// =meth Ink "live" color:
		public void setColor(int rgbDecimal) {
			paint.setColor(colorDecimalToColor(rgbDecimal));
		}

		public static int colorDecimalToColor(int rgbDecimal) {
			double r = Math.floor(rgbDecimal / 100 % 10 * (255.0 / 9.0));
			double g = Math.floor(rgbDecimal / 10 % 10 * (255.0 / 9.0));
			double b = Math.floor(rgbDecimal / 1 % 10 * (255.0 / 9.0));
			return Color.rgb((int) r, (int) g, (int) b);
		}

		// =meth Ink "draw" line:to:
		public void line_to_(Vec a, Vec b) {
			canvas().drawLine(floatAt(a, 0), floatAt(a, 1), floatAt(b, 0),
					floatAt(b, 1), paint);
		}

		// =meth Ink "draw" rect:to:
		public void rect_to_(Vec a, Vec b) {
			canvas().drawRect(floatAt(a, 0), floatAt(a, 1), floatAt(b, 0),
					floatAt(b, 1), paint);
		}

		// =meth Ink "draw" circ:r:
		public void circ_r_(Vec a, Num b) {
			canvas().drawCircle(floatAt(a, 0), floatAt(a, 1), (float) b.num,
					paint);
		}

		// =meth Ink "draw" dot:
		public void dot_(Vec a) {
			float[] dots = new float[] { floatAt(a, 0), floatAt(a, 1) };
			canvas().drawPoints(dots, paint);
		}

		// =meth Ink "draw" dots:
		public void dots_(Vec a) {
			final int n = a.vec.size();
			final float[] dots = new float[2 * n];
			for (int i = 0; i < n; ++i) {
				dots[2 * i] = floatAt(a.vec.get(i), 0);
				dots[2 * i + 1] = floatAt(a.vec.get(i), 1);
			}
			canvas().drawPoints(dots, paint);
		}

		// =meth Ink "draw" text:sw:
		public void drawText(Str s, Vec b) {
			canvas().drawText(s.str, floatAt(b, 0), floatAt(b, 1), paint);
		}

	}

	static FloatBuffer newFloatBuffer(float[] a) {
		ByteBuffer bb = ByteBuffer.allocateDirect(a.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer zz = bb.asFloatBuffer();
		zz.put(a);
		zz.position(0);
		return zz;
	}
	

	public class FnordView extends GLSurfaceView {
		// Thanks http://developer.android.com/resources/tutorials/opengl/opengl-es10.html
		final Obj app;
		GObj model = null;
		GLSurfaceView.Renderer renderer;
		GGl ggl;
		AndyTerp terp;

		public FnordView(Context context, final Obj app) {
			super(context);
			this.app = app;
			this.model = null;
			this.terp = (AndyTerp) app.cls.terp;
			this.ggl = new GGl(terp.wrapandy.clsGGl);
			this.renderer = new FnordRenderer();

			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(renderer);
			
			Runnable runTheApp = new Runnable() {
				@Override
				public void run() {
					terp.say("Run Fnord App app=%s ggl=%s", app, ggl);
					try {
						app.eval("self run: a", ggl);
					} catch (Throwable ex) {
						terp.say("Caught Fnord App ex=%s", ex);
					}
					terp.say("Finished Fnord App app=%s", app);
				}
			};
			terp.runOnWorkThread(runTheApp, "RunFnordApp");
		}
		
		public class GGl extends Obj {
			// =cls "GL" GGl  Obj
			public GGl(Cls cls) {
				super(cls);
			}
			// =meth GGl "gl" post:
			public void post_(GObj model) {
				FnordView.this.model = model;
			}
			
		}
		
		public class FnordRenderer implements GLSurfaceView.Renderer {
			final static float RADIUS = 8f;
			
			private FloatBuffer cubeVCB = null;
			private FloatBuffer triVCB = null;
			private FloatBuffer axesVCB = null;
			int width = 0;
			int height = 0;
			int frameCount = 0;
			float theta = 0.0f;
			float touchX = -1;
			float touchY = -1;

			private void initShapes() {
				float unitCubeCoords[] = {
						// X, Y, Z
						-0.5f, -0.5f, -0.5f, 0, 0, -1,
						-0.5f, +0.5f, -0.5f, 0, 0, -1,
						+0.5f, -0.5f, -0.5f, 0, 0, -1,
						// X, Y, Z
						-0.5f, -0.5f, -0.5f, -1, 0, 0,
						-0.5f, +0.5f, -0.5f, -1, 0, 0,
						-0.5f, -0.5f, +0.5f, -1, 0, 0,
						// X, Y, Z
						-0.5f, -0.5f, -0.5f, 0, -1, 0,
						-0.5f, -0.5f, +0.5f, 0, -1, 0,
						+0.5f, -0.5f, -0.5f, 0, -1, 0,

						// X, Y, Z
						+0.5f, +0.5f, -0.5f, 0, 0, -1,
						-0.5f, +0.5f, -0.5f, 0, 0, -1,
						+0.5f, -0.5f, -0.5f, 0, 0, -1,
						// X, Y, Z
						-0.5f, +0.5f, +0.5f, -1, 0, 0,
						-0.5f, +0.5f, -0.5f, -1, 0, 0,
						-0.5f, -0.5f, +0.5f, -1, 0, 0,
						// X, Y, Z
						+0.5f, -0.5f, +0.5f, 0, -1, 0,
						-0.5f, -0.5f, +0.5f, 0, -1, 0,
						+0.5f, -0.5f, -0.5f, 0, -1, 0,

						// X, Y, Z
						-0.5f, -0.5f, +0.5f, 0, 0, +1,
						-0.5f, +0.5f, +0.5f, 0, 0, +1,
						+0.5f, -0.5f, +0.5f, 0, 0, +1,
						// X, Y, Z
						+0.5f, -0.5f, -0.5f, +1, 0, 0,
						+0.5f, +0.5f, -0.5f, +1, 0, 0,
						+0.5f, -0.5f, +0.5f, +1, 0, 0,
						// X, Y, Z
						-0.5f, +0.5f, -0.5f, 0, +1, 0,
						-0.5f, +0.5f, +0.5f, 0, +1, 0,
						+0.5f, +0.5f, -0.5f, 0, +1, 0,

						// X, Y, Z
						+0.4f, +0.4f, +0.4f, 0, 0, +1,
						-0.5f, +0.5f, +0.5f, 0, 0, +1,
						+0.5f, -0.5f, +0.5f, 0, 0, +1,
						// X, Y, Z
						+0.4f, +0.4f, +0.4f, +1, 0, 0,
						+0.5f, +0.5f, -0.5f, +1, 0, 0,
						+0.5f, -0.5f, +0.5f, +1, 0, 0,
						// X, Y, Z
						+0.4f, +0.4f, +0.4f, 0, +1, 0,
						-0.5f, +0.5f, +0.5f, 0, +1, 0,
						+0.5f, +0.5f, -0.5f, 0, +1, 0,
						};//
				
				float triangleCoords[] = {
						// X, Y, Z
						0.1f, 0.1f, 0, /**/ 0.4f, 0.4f, 0, 1,
						0.1f, 0.9f, 0, /**/ 0.4f, 0.4f, 0, 1,
						0.9f, 0.1f, 0, /**/ 0.4f, 0.4f, 0, 1,
						// X, Y, Z
						0, 0.1f, 0.1f, /**/ 0, 0.4f, 0.4f, 1,
						0, 0.9f, 0.1f, /**/ 0, 0.4f, 0.4f, 1,
						0, 0.1f, 0.9f, /**/ 0, 0.4f, 0.4f, 1,
						// X, Y, Z
						0.1f, 0, 0.1f, /**/ 0.4f, 0, 0.4f, 1,
						0.1f, 0, 0.9f, /**/ 0.4f, 0, 0.4f, 1,
						0.9f, 0, 0.1f, /**/ 0.4f, 0, 0.4f, 1,

						// X, Y, Z
						0.9f, 0.9f, 0, /**/ 0.4f, 0.4f, 0, 1,
						0.1f, 0.9f, 0, /**/ 0.4f, 0.4f, 0, 1,
						0.9f, 0.1f, 0, /**/ 0.4f, 0.4f, 0, 1,
						// X, Y, Z
						0, 0.9f, 0.9f, /**/ 0, 0.4f, 0.4f, 1,
						0, 0.9f, 0.1f, /**/ 0, 0.4f, 0.4f, 1,
						0, 0.1f, 0.9f, /**/ 0, 0.4f, 0.4f, 1,
						// X, Y, Z
						0.9f, 0, 0.9f, /**/ 0.4f, 0, 0.4f, 1,
						0.1f, 0, 0.9f, /**/ 0.4f, 0, 0.4f, 1,
						0.9f, 0, 0.1f, /**/ 0.4f, 0, 0.4f, 1,

						// X, Y, Z
						0.1f, 0.1f, 1, /**/ 0.2f, 0.2f, 0, 1,
						0.1f, 0.9f, 1, /**/ 0.2f, 0.2f, 0, 1,
						0.9f, 0.1f, 1, /**/ 0.2f, 0.2f, 0, 1,
						// X, Y, Z
						1, 0.1f, 0.1f, /**/ 0, 0.2f, 0.2f, 1,
						1, 0.9f, 0.1f, /**/ 0, 0.2f, 0.2f, 1,
						1, 0.1f, 0.9f, /**/ 0, 0.2f, 0.2f, 1,
						// X, Y, Z
						0.1f, 1, 0.1f, /**/ 0.2f, 0, 0.2f, 1,
						0.1f, 1, 0.9f, /**/ 0.2f, 0, 0.2f, 1,
						0.9f, 1, 0.1f, /**/ 0.2f, 0, 0.2f, 1,

						// X, Y, Z
						0.9f, 0.9f, 1, /**/ 0.2f, 0.2f, 0, 1,
						0.1f, 0.9f, 1, /**/ 0.2f, 0.2f, 0, 1,
						0.9f, 0.1f, 1, /**/ 0.2f, 0.2f, 0, 1,
						// X, Y, Z
						1, 0.9f, 0.9f, /**/ 0, 0.2f, 0.2f, 1,
						1, 0.9f, 0.1f, /**/ 0, 0.2f, 0.2f, 1,
						1, 0.1f, 0.9f, /**/ 0, 0.2f, 0.2f, 1,
						// X, Y, Z
						0.9f, 1, 0.9f, /**/ 0.2f, 0, 0.2f, 1,
						0.1f, 1, 0.9f, /**/ 0.2f, 0, 0.2f, 1,
						0.9f, 1, 0.1f, /**/ 0.2f, 0, 0.2f, 1,
						};//
				
				float refVertexAndColor[] = {
						0, 0, 0, /**/ 1, 0, 0, 1, //  X axis, red.
						1, 0, 0, /**/ 1, 0, 0, 1, //
						
						0, 0, 0, /**/ 0, 1, 0, 1, //  Y axis, green.
						0, 1, 0, /**/ 0, 1, 0, 1, //
						
						0, 0, 0, /**/ 0, 0, 1, 1, //  Z axis, blue.
						0, 0, 1, /**/ 0, 0, 1, 1,//
						
				};

				cubeVCB = newFloatBuffer(unitCubeCoords);
				triVCB = newFloatBuffer(triangleCoords);
				axesVCB = newFloatBuffer(refVertexAndColor);
			}

			public void onSurfaceCreated(GL10 gl, EGLConfig config) {
				try {
					terp.say("FNORD onSurfaceCreated");
					TerseActivity.this.glSurfaceView = FnordView.this;
					gl.glEnable(GL10.GL_LIGHTING);
					gl.glEnable(GL10.GL_LIGHT0);
					gl.glEnable(GL10.GL_DEPTH_TEST);
					gl.glEnable(GL10.GL_ALPHA_TEST);
					gl.glEnable(GL10.GL_AMBIENT_AND_DIFFUSE); // 
					gl.glEnable (GL10.GL_BLEND);
					gl.glBlendFunc (GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
					// Set the background frame color
					gl.glClearColor(0.1f, 0.1f, 0.4f, 1.0f);  // blue sky
					gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // black

					// initialize the triangle vertex array
					initShapes();

					// Enable use of vertex arrays
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				} catch (Exception e) {
					terp.say("FnordRenderer::onSurfaceCreated CAUGHT %s",
							Static.describe(e));
					postMortem(e);
				}
			}

			public void onDrawFrame(GL10 gl) {
				try {
					// TODO
					if (frameCount < 3) {
					  terp.say("FNORD onDrawFrame #" + frameCount);
					}
					/////////terp.say("bomb %d", 100 / frameCount);
					++frameCount;
					
					// Redraw background color
					gl.glClear(GL10.GL_COLOR_BUFFER_BIT
							| GL10.GL_DEPTH_BUFFER_BIT);

					
					gl.glViewport(0, 0, width, height);
					//gl.glMatrixMode(GL10.GL_PROJECTION); // Was using this.
					gl.glMatrixMode(GL10.GL_MODELVIEW);  // But changing to this.   Frestrum doesn't care?
					gl.glLoadIdentity();
					float h_over_w = height / width;
					gl.glFrustumf(-RADIUS, RADIUS, -h_over_w * RADIUS, h_over_w * RADIUS, -RADIUS, RADIUS);
					

					gl.glMatrixMode(GL10.GL_MODELVIEW);
					gl.glLoadIdentity();
					gl.glEnable(GL10.GL_DEPTH_TEST);
					//gl.glCullFace(GL10.GL_FRONT_AND_BACK);
					gl.glCullFace(GL10.GL_FRONT_AND_BACK);
					gl.glColor4f(0.8f, 0.8f, 0.8f, 0.7f);
	                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	                gl.glShadeModel(GL10.GL_SMOOTH);

	               
//	                float lightAmbient[] = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
//	                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,     0);

					{ // Lighting.
						// http://code.google.com/p/android-gl/source/browse/trunk/AndroidGL/src/edu/union/GLTutorialEleven.java?r=26

				        float lightAmbient[] = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
				        float lightDiffuse[] = new float[] { 0.8f, 0.2f, 0.8f, 1.0f };
				        float[] lightPos = new float[] {5,5,5,1};
				        lightPos = new float[] {2, 2, -10, 1};
				        
				        float matAmbient[] = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
				        float matDiffuse[] = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
				        
		                gl.glEnable(GL10.GL_LIGHTING);
		                gl.glEnable(GL10.GL_LIGHT0);
		                //gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
		                //gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);
		                
		                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,     0);
		                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,     0);
		                gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
					}
					
					gl.glPushMatrix();

					gl.glScalef(0.6f, 0.6f, -0.6f);
					if (touchX < 0) {
						theta = theta + 0.2f;
						gl.glRotatef(theta, 1, 0, 0);
						gl.glRotatef(theta / 3, 0, 1, 0);
						gl.glRotatef(theta / 10, 0, 0, 1);
					} else {
						gl.glRotatef((touchX * 360f/ width), 1, 0, 0);
						gl.glRotatef((touchY * 360f / height), 0, 1, 0);
					}
					if (model == null) {
						int strideOverNormal = 4 /*bytes per float*/ * (3 + 3) /*floats per vertex*/;
						gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
						gl.glEnable(GL10.GL_NORMALIZE);
						gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
						cubeVCB.position(0);
						gl.glVertexPointer(3, GL10.GL_FLOAT, strideOverNormal, cubeVCB);
						cubeVCB.position(3);
						gl.glNormalPointer(GL10.GL_FLOAT, strideOverNormal, cubeVCB);
						gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 36);
					} else {
						new GRender(gl, this).render(model);
					}

					int strideOverColor = 4 /*bytes per float*/ * (3 + 4) /*floats per vertex*/;

				    gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
					gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
					axesVCB.position(0);
					gl.glVertexPointer(3, GL10.GL_FLOAT, strideOverColor, axesVCB);
					axesVCB.position(3);
					gl.glColorPointer(4, GL10.GL_FLOAT, strideOverColor, axesVCB);
					gl.glDrawArrays(GL10.GL_LINES, 0, 6);
					
					gl.glPopMatrix();
					
				} catch (Exception e) {
					terp.say("FnordRenderer::onDrawFrame CAUGHT %s",
							Static.describe(e));
					postMortem(e);
				}
			}
			
			void drawCube(GL10 gl) {
				int strideOverNormal = 4 /*bytes per float*/ * (3 + 3) /*floats per vertex*/;
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			    gl.glEnable(GL10.GL_NORMALIZE);
			    gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				cubeVCB.position(0);
				gl.glVertexPointer(3, GL10.GL_FLOAT, strideOverNormal, cubeVCB);
				cubeVCB.position(3);
				gl.glNormalPointer(GL10.GL_FLOAT, strideOverNormal, cubeVCB);
				gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 36);
			}

			public void onSurfaceChanged(GL10 gl, int width, int height) {
				try {
					setOnTouchListener(new OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								int action = event.getAction();
								if (action == MotionEvent.ACTION_DOWN
										|| action == MotionEvent.ACTION_MOVE) {
									// Just save the X and Y points, for now.
									touchX = event.getRawX();
									touchY = event.getRawY();
									return true;
								}
								return false;
							}
					});
					
					terp.say("FNORD onSurfaceChanged(", width, ",", height, ")");
					this.width = width;
					this.height = height;
					gl.glViewport(0, 0, width, height);
				} catch (Exception e) {
					terp.say("FnordRenderer::onSurfaceChanged CAUGHT %s",
							Static.describe(e));
					postMortem(e);
				}
			}

		}
		class GRender extends GVisitor {
			GL10 gl;
			FnordRenderer rend;
			Stack<Obj> colors = new Stack<Obj>();
			GRender(GL10 gl, FnordRenderer rend) {
				this.gl = gl;
				this.rend = rend;
			}
			void render(GObj top) {
				colors.push(terp.newVec(Static.ints(1, 1, 1, 1))); // White
//				gl.glColor4f(0.5f, 1.0f, 0.5f, 1.0f);  // Greenish.
				top.visit(this);
			}
			@Override
			void visitGPrim(GPrim a) {
				pushTransform(a);
				// TODO: the Prim should do its drawing, not this hardwired drawCube().
				rend.drawCube(gl);
				popTransform();
			}
			@Override
			void visitGVec(GVec a) {
				pushTransform(a);
				int sz = a.vec.vec.size();
				for (int i = 0; i < sz; i++) {
					GObj x = (GObj) a.vec.vec.get(i);
					x.visit(this);
				}
				popTransform();
			}
			void popTransform() {
				colors.pop();
				gl.glPopMatrix();
			}
			void pushTransform(GObj a) {
				gl.glPushMatrix();
				gl.glTranslatef(a.px, a.py, a.pz);
				gl.glScalef(a.sx, a.sy, a.sz);
				gl.glRotatef(a.rz, 0, 0, 1);
				gl.glRotatef(a.ry, 0, 1, 0);
				gl.glRotatef(a.rx, 1, 0, 0);
				if (a.color == terp.instNil) {
					colors.push(colors.peek()); // dup
				} else {
					colors.push(a.color);
				}
				Vec color = colors.peek().mustVec();
				gl.glColor4f(Static.floatAt(color, 0), Static.floatAt(color, 1), Static.floatAt(color, 2), Static.floatAt(color, 3));
			}
		}
	}

	public static abstract class GVisitor {
		abstract void visitGPrim(GPrim obj);
		abstract void visitGVec(GVec vec);
	}
	public static abstract class GObj extends Obj {
		float px = 0, py = 0, pz = 0;  // Translation
		float sx = 1, sy = 1, sz = 1;  // Scale
		float rx = 0, ry = 0, rz = 0;  // Rot (Euclid)
		Obj color = terp().instNil;  // Vec or nil
		// =cls "GL" GObj  Obj
		GObj(Cls cls) {
			super(cls);
		}
		// =meth GObj "access" pos:
		public void pos_(Vec a) {
			px = Static.floatAt(a, 0);
			py = Static.floatAt(a, 1);
			pz = Static.floatAt(a, 2);
		}
		// =meth GObj "access" scale:
		public void scale_(Vec a) {
			sx = Static.floatAt(a, 0);
			sy = Static.floatAt(a, 1);
			sz = Static.floatAt(a, 2);
		}
		// =meth GObj "access" rot:
		public void rot_(Vec a) {
			rx = Static.floatAt(a, 0);
			ry = Static.floatAt(a, 1);
			rz = Static.floatAt(a, 2);
		}
		// =meth GObj "access" color:
		public void color_(Obj a) {
			color = a;
		}
		// =meth GObj "access" pos
		public Vec _pos() {
			return terp.mkFloatVec(px, py, pz);
		}
		// =meth GObj "access" sca
		public Vec _sca() {
			return terp.mkFloatVec(sx, sy, sz);
		}
		// =meth GObj "access" rot
		public Vec _rot() {
			return terp.mkFloatVec(rx, ry, rz);
		}
		// =meth GObj "access" color
		public Obj _color() {
			return color;
		}
		abstract void visit(GVisitor gv);
	}
	
	public static class GPrim extends GObj {
		FloatBuffer fbuf = null;
		int sz = 0;
		int mode = GL10.GL_TRIANGLES;
		// =cls  "GL" GPrim  GObj
		GPrim(Cls cls) {
			super(cls);
		}
		// =meth GPrimCls "new" new
		public static GPrim _new(Terp t) {
			AndyTerp terp = (AndyTerp) t;
			return new GPrim(terp.wrapandy.clsGPrim);
		}

//		// =meth GPrim "access" mesh:
//		public void mesh_(Vec a) {
//			sz = a.vec.size();
//			float[] ff = new float[sz * 3];
//			for (int i = 0; i < sz; i++) {
//				Vec xyz = Static.urAt(a, i).mustVec();
//				ff[i * 3 + 0] = Static.floatAt(xyz, 0);
//				ff[i * 3 + 1] = Static.floatAt(xyz, 1);
//				ff[i * 3 + 2] = Static.floatAt(xyz, 2);
//			}
//			fbuf = newFloatBuffer(ff);
//		}

		@Override
		void visit(GVisitor gv) {
			gv.visitGPrim(this);
		}
	}
	
	public static class GVec extends GObj {
		Vec vec;
		// =cls  "GL" GVec  GObj
		GVec(Cls cls) {
			super(cls);
		}
		// =meth GVecCls "new" new
		public static GVec _new(Terp t) {
			AndyTerp terp = (AndyTerp) t;
			return new GVec(terp.wrapandy.clsGVec);
		}

		// =meth GVec "access" vec
		public Vec _vec() {
			return vec;
		}
		// =meth GVec "access" vec:
		public void vec_(Vec a) {
			this.vec = a;
		}
		@Override
		void visit(GVisitor gv) {
			gv.visitGVec(this);
		}
	}
	
	public static class GFan extends GPrim {
		// =cls  "GL" GFan GPrim
		GFan(Cls cls) {
			super(cls);
			this.mode = GL10.GL_TRIANGLE_FAN;
		}
	}
	
	public static class GStrip extends GPrim {
		// =cls  "GL" GStrip GPrim
		GStrip(Cls cls) {
			super(cls);
			this.mode = GL10.GL_TRIANGLE_STRIP;
		}
	}

	public class TerseSurfView extends SurfaceView implements Callback {
		public SurfaceHolder holder0;
		public Context context;
		public boolean created;
		public Blk blk;
		public Blk eventBlk;
		public Thread thread;

		public TerseSurfView(Context context, Blk blk, Blk eventBlk) {
			super(context);
			this.context = context;
			this.created = false;
			this.blk = blk;
			this.eventBlk = eventBlk;
			this.holder0 = this.getHolder();
			holder0.addCallback(this);
			defineOnTouch();
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			defineOnTouch();
		}

		private void defineOnTouch() {
			// terp.say("eventBlk == %s", eventBlk);
			if (eventBlk != null) {
				// terp.say("Defining OnTouch =======");
				setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int action = event.getAction();
						if (action == MotionEvent.ACTION_DOWN
								|| action == MotionEvent.ACTION_MOVE) {
							Motion mot = new Motion(terp, TerseSurfView.this,
									event, eventBlk);
							boolean ok = terp.eventQueue.offer(mot);
							if (!ok) {
								terp.say("eventQ.offer refused");
							}
							return true;
						}
						return false;
					}
				});
			}
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder, int arg1,
				final int width, final int height) {
			terp.say("surfaceChanged <%d w=%d h=%d>", arg1, width, height);
			terp.say(".............. <getLeft=%d getTop=%d>", getLeft(),
					getTop());

			terp.runOnWorkThread(new Runnable() {
				@Override
				public void run() {
					Screen scr = new Screen(((AndyTerp) blk.terp()), holder,
							TerseSurfView.this, width, height);
					blk.evalWith1Arg(scr);
				}
			}, terp.fmt("evalWith1Arg[%s](screen)", blk));
			terp.say("started thread <%d w=%d h=%d> holder=%s", arg1, width,
					height, holder);
		}

		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			this.created = true;
			// this.holder = holder;
			terp.say("surfaceCreated %s %s", this, holder);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			this.created = false;
			// this.holder = holder;
			terp.say("surfaceDestroyed %s %s", this, holder);
		}
	}

	void startTerseActivity(String actPath, String actQuery, String... extrasKV) {

		Uri uri = new Uri.Builder().scheme("terse").path(actPath)
				.encodedQuery(actQuery).build();
		Intent intent = new Intent("android.intent.action.MAIN", uri);
		intent.setClass(getApplicationContext(), TerseActivity.class);
		for (int i = 0; i < extrasKV.length; i += 2) {
			intent.putExtra(extrasKV[i], extrasKV[i + 1]);
		}
		startActivity(intent);
	}

	static String stringAt(Ur p, String i) {
		return Static.stringAt(p, i);
	}

	static String stringAt(Ur p, int i) {
		return Static.stringAt(p, i);
	}

	static float floatAt(Ur p, int i) {
		return Static.floatAt(p, i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.top_menu:
			startTerseActivity("/", "");
			return true;
		case R.id.help_menu:
			startTerseActivity("/Help", "");
			return true;
		case R.id.saidWhat_menu:
			startTerseActivity("/SaidWhat", "");
			return true;
		case R.id.crash_menu:
			// "UserRequestedCrashError".charAt(666);
			throw new Error("UserRequestedCrashError");
		case R.id.forget_menu:
			try {
				FileOutputStream fos = terp.openFileWrite(terp.worldFilename);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			resetTerp();
			startTerseActivity("/", "");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
