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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import terse.vm.Static;
import terse.vm.Ur.Dict;
import terse.vm.Ur.Vec;
import terse.vm.Terp;

// This is used by web but not by Android.
public class Async extends Thread {
	Terp.Factory factory;
	String defaultImage;

	HashMap<String, Terp> terps = new HashMap<String, Terp>();
	public BlockingQueue<Job> inQueue = new ArrayBlockingQueue<Job>(32);

	static long serial = 10;

	synchronized long getSerial() {
		++serial;
		return serial;
	}

	static Pattern OK_FILENAME = Pattern.compile("[A-Za-z0-9_]+");

	public Async(Terp.Factory factory, String defaultImage) {
		this.factory = factory;
		this.defaultImage = defaultImage;
	}

	public class Job {
		//Runnable runnable;
		String path;
		HashMap<String, String> query;
		public ArrayBlockingQueue<Result> reply;
		long id;

		Job(String path, HashMap<String, String> query) {
			this.path = path;
			this.query = query;
			this.reply = new ArrayBlockingQueue<Result>(1);
			this.id = getSerial();
		}

//		Job(Runnable runnable) {
//			this.runnable = runnable;
//			this.reply = new ArrayBlockingQueue<Result>(1);
//			this.id = getSerial();
//		}

		public String toString() {
			return "Job [id=" + id + ", path=" + path + ", query=" + query
					+ ", reply=" + reply + "]";
		}
	}

	public Job newJob(String path, HashMap<String, String> query) {
		return new Job(path, query);
	}

//	public Job newJob(Runnable runnable) {
//		return new Job(runnable);
//	}

	public class Result {
		int httpStatus;
		String contentType;
		public Dict renderMe;
		public Terp terp;

		public Result(int httpStatus, String contentType, Dict renderMe,
				Terp terp) {
			this.httpStatus = httpStatus;
			this.contentType = contentType;
			this.renderMe = renderMe;
			this.terp = terp;
		}

		public String toString() {
			return "Result [contentType=" + contentType + ", httpStatus="
					+ httpStatus + ", renderMe=" + renderMe + "]";
		}
	}

	Result newResult(int httpStatus, String contentType, Dict renderMe,
			Terp terp) {
		return new Result(httpStatus, contentType, renderMe, terp);
	}

	Result errorResult(String msg, Object... objects) {
		Terp tmp;
		try {
			tmp = factory.createTerp(false, "");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.toString());
		}
		Dict z = tmp
				.newDict(Static.urs(
						tmp.newVec(Static.urs(tmp.newStr("type"),
								tmp.newStr("text"))),
						tmp.newVec(Static.urs(tmp.newStr("value"),
								tmp.newStr(Static.fmt(msg, objects))))));
		return newResult(404, "", z, tmp);
	}

	public void run() {
		while (true) {
			Job job;
			try {
				job = inQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			assert job != null;

			String imageName = job.query.get("i");
			if (imageName == null || imageName.equals("")) {
				imageName = defaultImage;
			}
			Terp terp = terps.get(imageName);
			if (terp == null) {
				// TODO: load from .tti file.
				if (!OK_FILENAME.matcher(imageName).matches()) {
					Result errorReply = errorResult(
							"BAD FILENAME SYNTAX FOR IMAGE: <%s>", imageName);
					boolean ok = job.reply.offer(errorReply);
					assert ok;
					continue;
				}
				try {
					terp = factory.createTerp(true, imageName);
					terp.say("Async: new terp for image=<%s>", imageName);
					terp.say("Async: 4 terp=: <%s>", terp);
				} catch (Exception e) {
					e.printStackTrace();
					Result errorReply = errorResult(
							"CANNOT LOAD TERP WITH IMAGE: <%s>", imageName);
					boolean ok = job.reply.offer(errorReply);
					assert ok;
					continue;
				}
				terps.put(imageName, terp);
			}

			terp.say("Async: handling... ", job);
			Dict renderMe = terp.handleUrl(job.path, job.query);
			Result reply = new Result(200, "", renderMe, terp);
			boolean ok = job.reply.offer(reply);
			assert ok : Static.fmt("WHO PUT MY Q? %s <%s>  <%s>", imageName,
					job, reply);

			terp.say("Async: replied.  Looping.");
		}
	}
}
