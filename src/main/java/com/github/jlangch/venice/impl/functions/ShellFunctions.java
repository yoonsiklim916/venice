/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ShellException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.IOStreamUtil;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;


public class ShellFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Shell
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction sh = new VncFunction("sh") {
		{
			setArgLists("(sh & args)");
			
			setDoc( "Passes the given strings to Runtime.exec() to launch a sub-process.\n" + 
					"\n" +
					" Options are\n" + 
					"  :in        may be given followed by input source as InputStream, Reader, \n" + 
					"             File, ByteBuf, or String, to be fed to the sub-process's stdin.\n" + 
					"  :in-enc    option may be given followed by a String, used as a character\n" + 
					"             encoding name (for example \"UTF-8\" or \"ISO-8859-1\") to\n" + 
					"             convert the input string specified by the :in option to the\n" + 
					"             sub-process's stdin.  Defaults to UTF-8.\n" + 
					"             If the :in option provides a byte array, then the bytes are passed\n" + 
					"             unencoded, and this option is ignored.\n" + 
					"  :out-enc   option may be given followed by :bytes or a String. If a\n" + 
					"             String is given, it will be used as a character encoding\n" + 
					"             name (for example \"UTF-8\" or \"ISO-8859-1\") to convert\n" + 
					"             the sub-process's stdout to a String which is returned.\n" + 
					"             If :bytes is given, the sub-process's stdout will be stored\n" + 
					"             in a Bytebuf and returned.  Defaults to UTF-8.\n" + 
					"  :env       override the process env with a map.\n" + 
					"  :dir       override the process dir with a String or java.io.File.\n" + 
					"  :throw-ex  If true throw an exception if the exit code is not equal to\n" + 
					"             zero, if false returns the exit code. Defaults to false.\n" + 
					"             It's recommended to use (with-sh-throw (sh \"foo\")) instead.\n" + 
					"\n" +
					"You can bind :env, :dir for multiple operations using with-sh-env or\n" + 
					"with-sh-dir. with-sh-throw is binds :throw-ex as true.\n" + 
					"\n" +
					" sh returns a map of\n" + 
					"  :exit => sub-process's exit code\n" + 
					"  :out  => sub-process's stdout (as Bytebuf or String)\n" + 
					"  :err  => sub-process's stderr (String via platform default encoding)");
			
			setExamples(
					"(println (sh \"ls\" \"-l\"))",
					"(println (sh \"ls\" \"-l\" \"/tmp\"))", 
					"(println (sh \"sed\" \"s/[aeiou]/oo/g\" :in \"hello there\\n\"))",
					"(println (sh \"cat\" :in \"x\\u25bax\\n\"))",
					"(println (sh \"echo\" \"x\\u25bax\"))", 
					"(println (sh \"/bin/sh\" \"-c\" \"ls -l\"))", 
					
					";; reads 4 single-byte chars\n" +
					"(println (sh \"echo\" \"x\\u25bax\" :out-enc \"ISO-8859-1\"))",
					
					";; reads binary file into bytes[]\n" +
					"(println (sh \"cat\" \"birds.jpg\" :out-enc :bytes))", 
					
					";; working directory\n" +
					"(println (with-sh-dir \"/tmp\" (sh \"ls\" \"-l\") (sh \"pwd\")))", 
					
					";; throw an exception if the shell's subprocess exit code is not equal to 0\n" +
					"(println (with-sh-throw (sh \"ls\" \"-l\")))", 
					
					";; windows\n" +
					"(println (sh \"cmd\" \"/c dir 1>&2\"))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("sh", args);

			assertMinArity("sh", args, 1);

			validateArgs(args);
			
			final VncVector v = parseArgs(args);

			final VncList cmd = Coerce.toVncList(v.first());
			final VncMap opts = Coerce.toVncMap(v.second());

			MetaUtil.copyTokenPos(args, cmd);
			MetaUtil.copyTokenPos(args, opts);
			
			final ExecutorService executor = Executors.newFixedThreadPool(
												3,
												ThreadPoolUtil.createThreadFactory(
														"venice-shell-pool-%d", 
														threadPoolCounter,
														true /* daemon threads */));
			try {
				return exec(cmd, opts, executor);
			}
			finally {
				executor.shutdownNow();
			}
		}
	};
		
	
	///////////////////////////////////////////////////////////////////////////
	// Util
	///////////////////////////////////////////////////////////////////////////

	private static VncVal exec(
			final VncList cmd, 
			final VncMap opts, 
			final ExecutorService executor
	) {
		//   Streams:
		//
		//   +-----------------+                  +-----------------+
		//   |              out|----------------->|in               |
		//   |   PARENT      in|<-----------------|err      CHILD   |
		//   |               in|<-----------------|out              |
		//   +-----------------+                  +-----------------+
		
		try {
			final String[] cmdArr = toStringArray(cmd);
			final String[] envArr = toEnvStrings(opts.get(new VncKeyword(":env")));
			final File dir_ = toFile(opts.get(new VncKeyword(":dir")));
			
			final VncVal in = opts.get(new VncKeyword(":in"));
			final VncVal inEnc = opts.get(new VncKeyword(":in-enc"));
			final VncVal outEnc = opts.get(new VncKeyword(":out-enc"));
			
			//new ProcessBuilder().inheritIO().directory(dir_).command(cmdArr).environment()
					
			final Process proc = Runtime.getRuntime().exec(cmdArr, envArr, dir_);

			final OutputStream stdin = proc.getOutputStream();

			Future<Object> future_stdin = null;
			if (in != Nil) {
				// spit to subprocess' stdin a string, a bytebuf, or a File
				
				if (Types.isVncString(in)) {
					future_stdin = executor.submit(
									() -> copyAndClose((VncString)in, getEncoding(inEnc), stdin));
				}
				else if (Types.isVncByteBuffer(in)) {
					future_stdin = executor.submit(
									() -> copyAndClose((VncByteBuffer)in, stdin));
				}
				else if (Types.isVncJavaObject(in) && ((VncJavaObject)in).getDelegate() instanceof File) {
					future_stdin = executor.submit(
									() -> copyAndClose((File)((VncJavaObject)in).getDelegate(), stdin));
				}
			}
			
			if (future_stdin == null) {
				// we're not sending anything to the subprocess' stdin
				stdin.close();
			}
			
			try(InputStream stdout = proc.getInputStream();
				InputStream stderr = proc.getErrorStream()
			) {
				// slurp the subprocess' stdout (as string or bytebuf)
				final String enc = getEncoding(outEnc);
				final Future<VncVal> future_stdout =
						executor.submit(() -> "bytes".equals(enc)
												? new VncByteBuffer(IOStreamUtil.copyIStoByteArray(stdout))
												: new VncString(IOStreamUtil.copyIStoString(stdout, enc)));
				
				// slurp the subprocess' stderr as string with platform default encoding
				final Future<VncVal> future_stderr = 
						executor.submit(() -> new VncString(IOStreamUtil.copyIStoString(stderr, null)));
				
				final int exitCode = proc.waitFor();
					
				if (future_stdin != null) {
					future_stdin.get();
				}

				if (exitCode != 0 && opts.get(new VncKeyword(":throw-ex")) == Constants.True) {
					throw new ShellException(
								String.format(
									"Shell execution failed: (sh %s). Exit code: %d. %s", 
									((VncString)CoreFunctions.pr_str.apply(cmd)).getValue(),
									exitCode,
									ErrorMessage.buildErrLocation(cmd)),
								exitCode);
				}
				else {				
					return new VncHashMap(
							new VncKeyword(":exit"), new VncLong(exitCode),
							new VncKeyword(":out"),  future_stdout.get(),
							new VncKeyword(":err"),  future_stderr.get());
				}
			}
		}
		catch(ShellException ex) {
			throw ex;
		}
		catch(Exception ex) {
			throw new VncException(
					String.format(
						"Shell execution processing failed: (sh %s). %s", 
						((VncString)CoreFunctions.pr_str.apply(cmd)).getValue(),
						ErrorMessage.buildErrLocation(cmd)),
					ex);
		}
	}

	private static File toFile(final VncVal dir) {
		if (dir == Nil) {
			return null;
		}
		else if (Types.isVncString(dir)) {
			return new File(((VncString)dir).getValue());
		}
		else if (Types.isVncJavaObject(dir)) {
			final Object delegate = ((VncJavaObject)dir).getDelegate();
			if (delegate instanceof File) {
				return (File)delegate;
			}
		}
	
		return null;
	}

	private static String[] toStringArray(final VncList list) {
		return list
				.getList()
				.stream()
				.map(it -> CoreFunctions.name.apply(new VncList(it)))
				.map(it -> ((VncString)it).getValue())
				.collect(Collectors.toList())
				.toArray(new String[] {});
	}

	private static String[] toEnvStrings(final VncVal envMap) {
		if (envMap == Nil) {
			return null;
		}
		else {
			return ((VncMap)envMap)
						.entries()
						.stream()
						.map(e -> 
							String.format(
									"%s=%s", 
									CoreFunctions.name.apply(new VncList(e.getKey())),
									e.getValue().toString()))
						.collect(Collectors.toList())
						.toArray(new String[] {});
		}
	}
	
	private static void validateArgs(final VncList args) {
		args.forEach(arg -> {
			if (!(Types.isVncString(arg) || Types.isVncKeyword(arg))) {
				throw new VncException(
						String.format(
								"sh: accepts strings and keyword only. Got an argument of type %s. %s",
								Types.getClassName(arg),
								ErrorMessage.buildErrLocation(arg)));
			}
		});
	}

	private static VncVector parseArgs(final VncList args) {
		final VncThreadLocal th = new VncThreadLocal();
		
		final VncMap defaultOptions = new VncHashMap(
										new VncKeyword(":out-enc"), new VncString("UTF-8"),
										new VncKeyword(":in-enc"), new VncString("UTF-8"),
										new VncKeyword(":dir"), th.get(":*sh-dir*"),
										new VncKeyword(":throw-ex"), th.get(":*sh-throw-ex*"));

		final VncMap defaultEnv = (VncMap)th.get(":*sh-env*", new VncHashMap());
		
		final VncVector v = Coerce.toVncVector(
								CoreFunctions.split_with.apply(
									new VncList(CoreFunctions.string_Q, args)));

		final VncList cmd = Coerce.toVncList(v.first());

		final VncMap sh_opts = new VncHashMap((VncList)v.second());

		// merge options
		VncMap opts = (VncMap)CoreFunctions.merge.apply(
									new VncList(defaultOptions, sh_opts));
		
		// add merged :env map
		opts = opts.assoc(
					new VncKeyword(":env"),
					CoreFunctions.merge.apply(
							new VncList(
									defaultEnv, 
									sh_opts.get(new VncKeyword(":env")))));
		
		return new VncVector(cmd, opts);
	}
	
	private static String getEncoding(final VncVal enc) {
		return enc == Nil
				? Charset.defaultCharset().name()
				: ((VncString)CoreFunctions.name.apply(new VncList(enc))).getValue();
	}
	
	private static Object copyAndClose(
			final VncString data, 
			final String encoding, 
			final OutputStream os
	) throws IOException {
		IOStreamUtil.copyStringToOS(((VncString)data).getValue(), os, encoding);
		os.flush();
		os.close();
		return null;
	}
	
	private static Object copyAndClose(final VncByteBuffer data, final OutputStream os) 
	throws IOException {
		IOStreamUtil.copyByteArrayToOS(((VncByteBuffer)data).getValue().array(), os);
		os.flush();
		os.close();
		return null;
	}
	
	private static Object copyAndClose(final File data, final OutputStream os) 
	throws IOException {
		IOStreamUtil.copyFileToOS(data, os);
		os.flush();
		os.close();
		return null;
	}

	
	private final static AtomicLong threadPoolCounter = new AtomicLong(0);
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("sh",	sh)					
					.toMap();	
}
