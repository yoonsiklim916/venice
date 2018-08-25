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
package com.github.jlangch.venice.impl.javainterop;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class JavaInteropFn extends VncFunction {

	private JavaInteropFn(final JavaImports javaImports) {
		super(".");
		
		this.javaImports = javaImports;
		
		setArgLists(
				"(. classname :new args)", 
			        "(. classname :class)",
			        "(. classname :method args)",
			        "(. classname :field)",
				"(. object :method args)", 
			        "(. object :field)",
				"(. object :class)");
		
		setDoc(
				"Java interop. Calls a constructor or an object method. " +
				"The function is sandboxed");
		
		setExamples(
				";; access static field \n"
					+ "(. :java.lang.Math :PI)",
				";; invoke constructor \n"
						+ "(. :java.time.ZonedDateTime :now)",
				";; invoke constructor with param \n"
						+ "(. (. :java.lang.Long :new 10) :toString)", 
				";; invoke static method \n"
						+ "(. :java.lang.Math :min 10 20)", 
				";; get class name \n"
						+ "(. :java.lang.Math :class)", 
				";; get class name \n"
						+ "(. \"java.lang.Math\" :class)", 
				";; get class name \n"
						+ "(. (. :java.io.File :new \"/temp\") :class)");
	}

	
	public static JavaInteropFn create(final JavaImports javaImports) {
		return new JavaInteropFn(javaImports);
	}

	public VncVal apply(final VncList args) {
		JavaInterop.getInterceptor().checkBlackListedVeniceFunction(".", args);
		
		return JavaInteropUtil.applyJavaAccess(args, javaImports);
	}

	
	private final JavaImports javaImports;
}
