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

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.isJavaIoFile;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.IOStreamUtil;
import com.github.jlangch.venice.impl.util.MimeTypes;


public class IOFunctions {

	
	///////////////////////////////////////////////////////////////////////////
	// IO functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_file = new VncFunction("io/file") {
		{
			setArgLists("(io/file path) (io/file parent child)");
			
			setDoc( "Returns a java.io.File. path, parent, and child can be a string " +
					"or java.io.File");
			
			setExamples(
					"(io/file \"/temp/test.txt\")",
					"(io/file \"/temp\" \"test.txt\")",
					"(io/file (io/file \"/temp\") \"test.txt\")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/file", args);

			assertArity("io/file", args, 1, 2);
						
			if (args.size() == 1) {
				final VncVal path = args.nth(0);
				if (Types.isVncString(path)) {
					return new VncJavaObject(new File(((VncString)path).getValue()));					
				}
				else if (isJavaIoFile(path) ) {
					return path;
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as path. %s",
							Types.getClassName(path),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				final VncVal parent = args.nth(0);
				final VncVal child = args.nth(1);
				
				File parentFile;

				if (Types.isVncString(parent)) {
					parentFile = new File(((VncString)parent).getValue());					
				}
				else if (isJavaIoFile(parent) ) {
					parentFile = (File)((VncJavaObject)parent).getDelegate();
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as parent. %s",
							Types.getClassName(parent),
							ErrorMessage.buildErrLocation(args)));
				}

				if (Types.isVncString(child)) {
					 return new VncJavaObject(new File(parentFile, ((VncString)child).getValue()));					
				}
				else {
					throw new VncException(String.format(
							"Function 'io/file' does not allow %s as child. %s",
							Types.getClassName(child),
							ErrorMessage.buildErrLocation(args)));
				}
			}		
		}
	};

	public static VncFunction io_file_Q = new VncFunction("io/file?") {
		{
			setArgLists("(io/file? x)");
			
			setDoc("Returns true if x is a java.io.File.");
			
			setExamples(
					"(io/file? (io/file \"/temp/test.txt\"))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/file?", args);

			assertArity("io/file?", args, 1);
						
			final VncVal path = args.nth(0);
			return isJavaIoFile(path) ? True : False;
		}
	};

	public static VncFunction io_exists_file_Q = new VncFunction("io/exists-file?") {
		{
			setArgLists("(io/exists-file? x)");
			
			setDoc("Returns true if the file x exists. x must be a java.io.File.");
			
			setExamples(
					"(io/exists-file? (io/file \"/temp/test.txt\"))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/exists-file?", args);

			assertArity("io/exists-file?", args, 1);
									
			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/exists-file?' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			return file.isFile() ? True : False;
		}
	};

	public static VncFunction io_exists_dir_Q = new VncFunction("io/exists-dir?") {
		{
			setArgLists("(io/exists-dir? x)");
			
			setDoc("Returns true if the file x exists and is a directory. x must be a java.io.File.");
			
			setExamples(
					"(io/exists-dir? (io/file \"/temp\"))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/exists-dir?", args);

			assertArity("io/exists-dir?", args, 1);
									
			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/exists-dir?' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			return file.isDirectory() ? True : False;
		}
	};

	public static VncFunction io_delete_file = new VncFunction("io/delete-file") {
		{
			setArgLists("(io/delete-file f & files)");
			
			setDoc("Deletes one or multiple files. f must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/delete-file", args);

			assertMinArity("io/delete-file", args, 1);

			args.forEach(f -> {
				if (!isJavaIoFile(f) ) {
					throw new VncException(String.format(
							"Function 'io/delete-file' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

				final File file = (File)((VncJavaObject)f).getDelegate();
				try {
					Files.deleteIfExists(file.toPath());	
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to delete file %s", file.getPath()),
							ex);
				}
			});
			
			return Nil;
		}
	};

	public static VncFunction io_delete_file_on_exit = new VncFunction("io/delete-file-on-exit") {
		{
			setArgLists("(io/delete-file-on-exit x)");
			
			setDoc("Deletes a file on JVM exit. x must be a string or java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/delete-file-on-exit", args);

			assertArity("io/delete-file-on-exit", args, 1);

			File file;
			if (Types.isVncString(args.nth(0)) ) {
				file = new File(((VncString)args.nth(0)).getValue());
			}
			else if (isJavaIoFile(args.nth(0)) ) {
				file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			}
			else {
				throw new VncException(String.format(
						"Function 'io/delete-file-on-exit' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}

			try {
				file.deleteOnExit();;	
			}
			catch(Exception ex) {
				throw new VncException(
						String.format("Failed to marke file %s to delete on exit", file.getPath()),
						ex);
			}
			
			return Nil;
		}
	};

	public static VncFunction io_list_files = new VncFunction("io/list-files") {
		{
			setArgLists("(io/list-files dir filterFn?)");
			
			setDoc( "Lists files in a directory. dir must be a java.io.File. filterFn " +
					"is an optional filter that filters the files found");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/list-files", args);

			assertArity("io/list-files", args, 1, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/list-files' does not allow %s as x. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			
			final File file = (File)((VncJavaObject)args.nth(0)).getDelegate();
			try {
				final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.nth(1)) : null;

				final VncList files = new VncList();

				for(File f : file.listFiles()) {
					final VncVal result = (filterFn == null) 
											? True 
											: filterFn.apply(new VncList(new VncJavaObject(f)));						
					if (result == True) {
						files.addAtEnd(new VncJavaObject(f));
					}
				}
				
				return files;
			}
			catch(Exception ex) {
				throw new VncException(
						String.format(
								"Failed to list files %s. %s", 
								file.getPath(),
								ErrorMessage.buildErrLocation(args)), 
						ex);
			}
		}
	};

	public static VncFunction io_copy_file = new VncFunction("io/copy-file") {
		{
			setArgLists("(io/copy-file input output)");
			
			setDoc( "Copies input to output. Returns nil or throws IOException. " + 
					"Input and output must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/copy-file", args);

			assertArity("io/copy-file", args, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/copy-file' does not allow %s as input. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!isJavaIoFile(args.nth(1)) ) {
				throw new VncException(String.format(
						"Function 'io/copy-file' does not allow %s as output. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}


			final File from = (File)((VncJavaObject)args.nth(0)).getDelegate();
			final File to = (File)((VncJavaObject)args.nth(1)).getDelegate();
			
			try {
				Files.copy(from.toPath(), to.toPath());
			}
			catch(Exception ex) {
				throw new VncException(
						String.format(
								"Failed to copy file %s to %s. %s", 
								from.getPath(), 
								to.getPath(),
								ErrorMessage.buildErrLocation(args)),
						ex);
			}
			
			return Nil;
		}
	};
	
	public static VncFunction io_move_file = new VncFunction("io/move-file") {
		{
			setArgLists("(io/move-file source target)");
			
			setDoc( "Moves source to target. Returns nil or throws IOException. " + 
					"Source and target must be a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/move-file", args);

			assertArity("io/move-file", args, 2);

			if (!isJavaIoFile(args.nth(0)) ) {
				throw new VncException(String.format(
						"Function 'io/move-file' does not allow %s as source. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!isJavaIoFile(args.nth(1)) ) {
				throw new VncException(String.format(
						"Function 'io/move-file' does not allow %s as target. %s",
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}


			final File from = (File)((VncJavaObject)args.nth(0)).getDelegate();
			final File to = (File)((VncJavaObject)args.nth(1)).getDelegate();
			
			try {
				Files.move(from.toPath(), to.toPath());
			}
			catch(Exception ex) {
				throw new VncException(
						String.format(
								"Failed to move file %s to %s. %s", 
								from.getPath(), 
								to.getPath(),
								ErrorMessage.buildErrLocation(args)),
						ex);
			}
			
			return Nil;
		}
	};

	public static VncFunction io_tmp_dir = new VncFunction("io/tmp-dir") {
		{
			setArgLists("(io/tmp-dir)");
			
			setDoc("Returns the tmp dir as a java.io.File.");
			
			setExamples("(io/tmp-dir )");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/tmp-dir", args);

			assertArity("io/tmp-dir", args, 0);

			return new VncJavaObject(new File(System.getProperty("java.io.tmpdir")));
		}
	};

	public static VncFunction io_user_dir = new VncFunction("io/user-dir") {
		{
			setArgLists("(io/user-dir)");
			
			setDoc("Returns the user dir (current working dir) as a java.io.File.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/user-dir", args);

			assertArity("io/user-dir", args, 0);

			return new VncJavaObject(new File(System.getProperty("user.dir")));
		}
	};

	public static VncFunction io_slurp = new VncFunction("io/slurp") {
		{
			setArgLists("(io/slurp file & options)");
			
			setDoc( "Returns the file's content as text (string) or binary (bytebuf). \n" +
					"Defaults to binary=false and encoding=UTF-8. \n" +
					"Options: :encoding \"UTF-8\" :binary true/false.");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/slurp", args);

			assertMinArity("io/slurp", args, 1);

			try {	
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/slurp' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

				
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 
				
				if (binary == True) {
					final byte[] data = Files.readAllBytes(file.toPath());
					
					return new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
									
					final byte[] data = Files.readAllBytes(file.toPath());
					
					return new VncString(new String(data, encoding));
				}
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_spit = new VncFunction("io/spit") {
		{
			setArgLists("(io/spit f content & options)");
			
			setDoc( "Opens f, writes content, and then closes f. \n" +
					"Options default to append=true and encoding=UTF-8. \n" +
					"Options: :append true/false, :encoding \"UTF-8\"");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/spit", args);

			assertMinArity("io/spit", args, 2);

			try {
				// Currently just string content is supported!
				
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/spit' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal append = options.get(new VncKeyword("append")); 
				
				final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();

				byte[] data;
				
				if (Types.isVncString(content)) {
					data = ((VncString)content).getValue().getBytes(encoding);
				}
				else if (Types.isVncByteBuffer(content)) {
					data = ((VncByteBuffer)content).getValue().array();
				}
				else {
					throw new VncException(String.format(
							"Function 'io/spit' does not allow %s as content. %s",
							Types.getClassName(content),
							ErrorMessage.buildErrLocation(args)));
				}

				final List<OpenOption> openOptions = new ArrayList<>();
				openOptions.add(StandardOpenOption.CREATE);
				openOptions.add(StandardOpenOption.WRITE);
				
				if (append != False) {
					openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
				}
				
				Files.write(
						file.toPath(), 
						data, 
						openOptions.toArray(new OpenOption[0]));
				
				return Nil;
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_slurp_stream = new VncFunction("io/slurp-stream") {
		{
			setArgLists("(io/slurp-stream is & options)");
			
			setDoc( "Slurps binary or string data from an input stream. " +
					"Supports the option :binary to either slurp binary or string data. " +
					"For string data an optional encoding can be specified.\n" +
					"Options: :encoding \"UTF-8\" :binary true/false. ");
			
			setExamples(
				"(do \n" +
				"   (import :java.io.FileInputStream) \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (io/delete-file-on-exit file) \n" +
				"        (io/spit file \"123456789\" :append true) \n" +
				"        (try-with [is (. :FileInputStream :new file)] \n" +
				"           (io/slurp-stream is :binary false))) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/slurp-stream", args);

			assertMinArity("io/slurp-stream", args, 1);

			try {	
				final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
								
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 

				if (binary == True) {
					final byte[] data = IOStreamUtil.copyIStoByteArray(is);
					return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

					return new VncString(IOStreamUtil.copyIStoString(is, encoding));
				}
			} 
			catch (VncException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_spit_stream = new VncFunction("spit-stream") {
		{
			setArgLists("(io/spit-stream os content & options)");
			
			setDoc( "Writes content (string or bytebuf) to the output stream os. " +
					"If content is of type string an optional encoding (defaults to " +
					"UTF-8) is supported. The stream can optionally be flushed after " +
					"the operation.\n" +
					"Options: :flush true/false :encoding \"UTF-8\"");
			
			setExamples(
				"(do \n" +
				"   (import :java.io.FileOutputStream) \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (io/delete-file-on-exit file) \n" +
				"        (try-with [os (. :FileOutputStream :new file)] \n" +
				"           (io/spit-stream os \"123456789\" :flush true))) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/spit-stream", args);


			assertMinArity("io/spit-stream", args, 2);

			try {
				final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal encVal = options.get(new VncKeyword("encoding")); 					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();

				final VncVal flushVal = options.get(new VncKeyword("flush")); 
				final boolean flush = flushVal == True ? true : false;

				byte[] data;
				
				if (Types.isVncString(content)) {
					data = ((VncString)content).getValue().getBytes(encoding);
				}
				else if (Types.isVncByteBuffer(content)) {
					data = ((VncByteBuffer)content).getValue().array();
				}
				else {
					throw new VncException(String.format(
							"Function 'spit-stream' does not allow %s as content. %s",
							Types.getClassName(content),
							ErrorMessage.buildErrLocation(args)));
				}
				
				os.write(data);
				
				if (flush) {
					os.flush();
				}
				
				return Nil;
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_mime_type = new VncFunction("io/mime-type") {
		{
			setArgLists("(io/mime-type file)");
			
			setDoc( "Returns the mime-type for the file if available else nil");

			setExamples(
					"(io/mime-type \"document.pdf\")",
					"(io/mime-type (io/file \"document.pdf\"))");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/mime-type", args);

			assertMinArity("io/mime-type", args, 1);

			if (Types.isVncString(args.first()) ) {
				return new VncString(
							MimeTypes.getMimeTypeFromFileName(
									((VncString)args.first()).getValue()));
			}
			else if (isJavaIoFile(args.first()) ) {
				return new VncString(
						MimeTypes.getMimeTypeFromFile(
								(File)(Coerce.toVncJavaObject(args.first()).getDelegate())));
			}
			else {
				throw new VncException(String.format(
						"Function 'io/mime-type' does not allow %s as f. %s",
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// IO TEMP functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_temp_file = new VncFunction("io/temp-file") {
		{
			setArgLists("(io/temp-file prefix suffix)");
			
			setDoc( "Creates an empty temp file with prefix and suffix. \n" +
					"io/temp-file with its companions io/spit-temp-file and " +
					"io/slurp-temp-file provide safe file access in sandboxed " +
					"environments.");
			
			setExamples(
				"(do \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (io/spit-temp-file file \"123456789\" :append true) \n" +
				"        (io/slurp-temp-file file :binary false :remove true)) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/temp-file", args);

			assertArity("io/temp-file", args, 2);

			final String prefix = Coerce.toVncString(args.first()).getValue();
			final String suffix = Coerce.toVncString(args.second()).getValue();
			try {
				final String path = File.createTempFile(prefix, suffix).getPath();
				tempFiles.add(path);
				return new VncString(path);
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_slurp_temp_file = new VncFunction("io/slurp-temp-file") {
		{
			setArgLists("(io/slurp-temp-file file & options)");
			
			setDoc( "Slurps binary or string data from a previously created temp file. " +
					"Supports the option :binary to either slurp binary or string data. " +
					"For string data an optional encoding can be specified. \n" +
					"Ensures that the caller can only read back files he previously " +
					"created, but no other files. This allows callers to work with files " +
					"without escaping the sandbox. The file must have been created with " +
					"io/temp_file. \n" +
					"Options: :encoding \"UTF-8\" :binary true/false.");
			
			setExamples(
				"(do \n" +
				"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
				"        (io/spit-temp-file file \"123456789\" :append true) \n" +
				"        (io/slurp-temp-file file :binary false :remove true)) \n" +
				")");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/slurp-temp-file", args);

			assertMinArity("io/slurp-temp-file", args, 1);

			try {	
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/slurp-temp-file' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}

				
				if (!tempFiles.contains(file.getPath())) {
					throw new VncException(String.format(
							"Function 'io/slurp-temp-file' tries to access the unknown temp file '%s'. %s",
							file.getPath(),
							ErrorMessage.buildErrLocation(args)));
				}
				
				final VncHashMap options = new VncHashMap(args.slice(1));

				final VncVal binary = options.get(new VncKeyword("binary")); 
	
				final VncVal remove = options.get(new VncKeyword("remove")); 

				if (binary == True) {
					final byte[] data = Files.readAllBytes(file.toPath());
					
					if (remove == True) {
						file.delete();
						tempFiles.remove(file.getPath());
					}
					
					return new VncByteBuffer(ByteBuffer.wrap(data));
				}
				else {
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
									
					final byte[] data = Files.readAllBytes(file.toPath());

					if (remove == True) {
						file.delete();
						tempFiles.remove(file.getPath());
					}

					return new VncString(new String(data, encoding));
				}
			} 
			catch (VncException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_spit_temp_file = new VncFunction("io/spit-temp-file") {
		{
			setArgLists("(io/spit-temp-file f content & options)");
			
			setDoc( "Spits binary or string data from to previously created temp file. \n" +
					"Ensures that the caller can only write to files he previously " +
					"created, but no other files. This allows callers to work with files " +
					"without escaping the sandbox. The file must have been created with " +
					"io/temp_file. \n" +
					"Defaults to append=true and encoding=UTF-8.\n" +
					"Options: :append true/false, :encoding \"UTF-8\"");
		}
		
		public VncVal apply(final VncList args) {
			JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/spit-temp-file", args);

			assertMinArity("io/spit-temp-file", args, 2);

			try {
				// Currently just string content is supported!
				
				File file;
				
				if (Types.isVncString(args.nth(0)) ) {
					file = new File(((VncString)args.nth(0)).getValue());
				}
				else if (isJavaIoFile(args.nth(0)) ) {
					file = (File)(Coerce.toVncJavaObject(args.nth(0)).getDelegate());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/spit-temp-file' does not allow %s as f. %s",
							Types.getClassName(args.nth(0)),
							ErrorMessage.buildErrLocation(args)));
				}
				
				if (!tempFiles.contains(file.getPath())) {
					throw new VncException(String.format(
							"Function 'io/spit-temp-file' tries to access the unknown temp file '%s'. %s",
							file.getPath(),
							ErrorMessage.buildErrLocation(args)));
				}
		
				final VncVal content = args.nth(1);

				final VncHashMap options = new VncHashMap(args.slice(2));

				final VncVal append = options.get(new VncKeyword("append")); 
				
				final VncVal encVal = options.get(new VncKeyword("encoding")); 
					
				final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();

				byte[] data;
				
				if (Types.isVncString(content)) {
					data = ((VncString)content).getValue().getBytes(encoding);
				}
				else if (Types.isVncByteBuffer(content)) {
					data = ((VncByteBuffer)content).getValue().array();
				}
				else {
					throw new VncException(String.format(
							"Function 'io/spit-temp-file' does not allow %s as content. %s",
							Types.getClassName(content),
							ErrorMessage.buildErrLocation(args)));
				}

				final List<OpenOption> openOptions = new ArrayList<>();
				openOptions.add(StandardOpenOption.CREATE);
				openOptions.add(StandardOpenOption.WRITE);
				
				if (append != False) {
					openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
				}
				
				Files.write(
						file.toPath(), 
						data, 
						openOptions.toArray(new OpenOption[0]));
				
				return Nil;
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};

	public static VncFunction io_load_classpath_resource = new VncFunction("io/load-classpath-resource") {
		{
			setArgLists("(io/load-classpath-resource name)");
			
			setDoc("Loads a classpath resource.");
		}
		
		public VncVal apply(final VncList args) {
			try {	
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("io/load-classpath-resource", args);

				assertArity("io/load-classpath-resource", args, 1);
				
				final VncVal name = args.first();
				
				if (Types.isVncString(name)) {
					final String res = ((VncString)args.first()).getValue();
					final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
					return data == null ? Nil : new VncByteBuffer(data);
				}
				else if (Types.isVncKeyword(name)) {
					final String res = ((VncKeyword)args.first()).getValue();
					final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
					return data == null ? Nil : new VncByteBuffer(data);
				}
				else if (Types.isVncSymbol(name)) {
					final String res = ((VncSymbol)args.first()).getName();
					final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
					return data == null ? Nil : new VncByteBuffer(data);
				}
				else {
					return Nil;
				}
			} 
			catch (Exception ex) {
				throw new VncException(ex.getMessage(), ex);
			}
		}
	};
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()								
					.put("io/file",						io_file)
					.put("io/file?",					io_file_Q)
					.put("io/exists-file?",				io_exists_file_Q)
					.put("io/exists-dir?",				io_exists_dir_Q)
					.put("io/list-files",				io_list_files)
					.put("io/delete-file",				io_delete_file)
					.put("io/delete-file-on-exit",		io_delete_file_on_exit)				
					.put("io/copy-file",				io_copy_file)
					.put("io/move-file",				io_move_file)
					.put("io/temp-file",				io_temp_file)
					.put("io/tmp-dir",					io_tmp_dir)
					.put("io/user-dir",					io_user_dir)
					.put("io/slurp",					io_slurp)
					.put("io/spit",						io_spit)
					.put("io/spit-temp-file",			io_spit_temp_file)
					.put("io/slurp-temp-file",			io_slurp_temp_file)
					.put("io/slurp-stream",	    		io_slurp_stream)
					.put("io/spit-stream",	    		io_spit_stream)							
					.put("io/mime-type",	    		io_mime_type)	
					.put("io/load-classpath-resource",	io_load_classpath_resource)
					.toMap();

	
	private static final HashSet<String> tempFiles = new HashSet<>();
}
