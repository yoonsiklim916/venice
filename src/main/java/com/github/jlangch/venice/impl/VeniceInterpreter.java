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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.Closeable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.Functions;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.JavaInteropFn;
import com.github.jlangch.venice.impl.javainterop.JavaInteropProxifyFn;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.CatchBlock;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;


public class VeniceInterpreter {
	
	public VeniceInterpreter() {
		
	}
	
	
	// read
	public VncVal READ(final String script, final String filename) {
		return Reader.read_str(script, filename);
	}

	// eval
	public static boolean is_pair(final VncVal x) {
		return x instanceof VncList && !((VncList)x).isEmpty();
	}

	public static VncVal quasiquote(final VncVal ast) {
		if (!is_pair(ast)) {
			return new VncList(new VncSymbol("quote"), ast);
		} 
		else {
			final VncVal a0 = Coerce.toVncList(ast).nth(0);
			if ((a0 instanceof VncSymbol) && (Coerce.toVncSymbol(a0).getName().equals("unquote"))) {
				return ((VncList)ast).nth(1);
			} 
			else if (is_pair(a0)) {
				final VncVal a00 = Coerce.toVncList(a0).nth(0);
				if ((a00 instanceof VncSymbol) && (((VncSymbol)a00).getName().equals("splice-unquote"))) {
					return new VncList(new VncSymbol("concat"),
										Coerce.toVncList(a0).nth(1),
									   quasiquote(((VncList)ast).rest()));
				}
			}
			return new VncList(new VncSymbol("cons"),
							   quasiquote(a0),
							   quasiquote(((VncList)ast).rest()));
		}
	}

	/**
	 * Returns true if ast is a list that contains a symbol as the first element 
	 * and that symbol refers to a function in the env environment and that 
	 * function has the is_macro attribute set to true. 
	 * Otherwise, it returns false.
	 * 
	 * @param ast ast
	 * @param env env
	 * @return true if the ast starts with a macro
	 */
	public boolean is_macro_call(final VncVal ast, final Env env) {
		if (Types.isVncList(ast) && !((VncList)ast).isEmpty()) {
			final VncVal a0 = Coerce.toVncList(ast).nth(0);
			if (Types.isVncSymbol(a0)) {
				final VncSymbol macroName = (VncSymbol)a0;
				if (env.findEnv(macroName) != null) {
					final VncVal fn = env.get(macroName);
					if (Types.isVncFunction(fn) && ((VncFunction)fn).isMacro()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Recursively expands a macro. It calls is_macro_call with ast and env and 
	 * loops while that condition is true. Inside the loop, the first element 
	 * of the ast list (a symbol), is looked up in the environment to get 
	 * the macro function. This macro function is then called/applied with 
	 * the rest of the ast elements (2nd through the last) as arguments. 
	 * The return value of the macro call becomes the new value of ast. 
	 * When the loop completes because ast no longer represents a macro call, 
	 * the current value of ast is returned.
	 * 
	 * @param ast ast
	 * @param env env
	 * @return the expanded macro
	 */
	public VncVal macroexpand(VncVal ast, final Env env) {
		while (is_macro_call(ast, env)) {
			final VncSymbol macroName = Coerce.toVncSymbol(Coerce.toVncList(ast).nth(0));
			final VncFunction macroFn = Coerce.toVncFunction(env.get(macroName));
			final VncList macroFnArgs = Coerce.toVncList(ast).rest();
			ast = macroFn.apply(macroFnArgs);
		}
		return ast;
	}

	public VncVal eval_ast(final VncVal ast, final Env env) {
		if (ast instanceof VncSymbol) {
			return env.get((VncSymbol)ast);
		} 
		else if (ast instanceof VncList) {
			final VncList old_lst = (VncList)ast;
			final VncList new_lst = old_lst.empty();
			new_lst.setMeta(old_lst.getMeta().copy());
			
			old_lst.forEach(mv -> new_lst.addAtEnd(EVAL(mv, env)));
			return new_lst;
		} 
		else if (ast instanceof VncMap) {
			final VncMap old_map = (VncMap)ast;
			final VncMap new_map = (VncMap)old_map.empty();
			new_map.setMeta(old_map.getMeta().copy());

			((VncMap)ast).getMap().entrySet().forEach(entry ->
				new_map.getMap().put(
						entry.getKey(), 
						EVAL((VncVal)entry.getValue(), env)));
			return new_map;
		} 
		else {
			return ast;
		}
	}

	public VncVal EVAL(VncVal orig_ast, Env env) {
		RecursionPoint recursionPoint = null;
		
		while (true) {
			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!orig_ast.isList()) {
				return eval_ast(orig_ast, env);
			}
	
			// apply list
			final VncVal expanded = macroexpand(orig_ast, env);
			if (!expanded.isList()) {
				return eval_ast(expanded, env);
			}
			
			final VncList ast = (VncList)expanded;
			if (ast.isEmpty()) { 
				return ast; 
			}
			
			final VncVal a0 = ast.nth(0);		
			final String a0sym = a0 instanceof VncSymbol ? ((VncSymbol)a0).getName() : "__<*fn*>__";
			
			switch (a0sym) {			
				case "def": { // (def meta-data? name value)
					final boolean hasMeta = ast.size() > 3;
					final VncMap defMeta = hasMeta ? (VncHashMap)EVAL(ast.nth(1), env) : new VncHashMap();
					final VncSymbol defName = Coerce.toVncSymbol(ast.nth(hasMeta ? 2 : 1));
					final VncVal defVal = ast.nth(hasMeta ? 3 : 2);
					final VncVal res = EVAL(defVal, env);
					env.setGlobal(defName, MetaUtil.addDefMeta(res, defMeta));
					return res;
				}
				
				case "eval":
					orig_ast = Coerce.toVncList(eval_ast(ast.slice(1), env)).last();
					break;
					
				case "let":  { // (let [bindings*] exprs*)
					env = new Env(env);

					final VncList bindings = Coerce.toVncList(ast.nth(1));
					final VncList expressions = ast.slice(2);
				
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = EVAL(bindings.nth(i+1), env);

						final Env _env = env;
						Destructuring
							.destructure(sym, val)
							.forEach(b -> _env.set(b.sym, b.val));
					}
						
					if (expressions.isEmpty()) {
						orig_ast = Constants.Nil;
					}
					else {
						eval_ast(expressions.slice(0, expressions.size()-1), env);
						orig_ast = expressions.last();
					}
					break;
				}
					
				case "loop": { // (loop [bindings*] exprs*)
					env = new Env(env);					

					final VncList bindings = Coerce.toVncList(ast.nth(1));
					final VncVal expressions = ast.nth(2);
					
					final VncList bindingNames = new VncList();
					for(int i=0; i<bindings.size(); i+=2) {
						final VncVal sym = bindings.nth(i);
						final VncVal val = EVAL(bindings.nth(i+1), env);

						final Env _env = env;
						Destructuring
							.destructure(sym, val)
							.forEach(b -> { 
								_env.set(b.sym, b.val); 
								bindingNames.addAtEnd(b.sym);
							 });
					}
					
					recursionPoint = new RecursionPoint(bindingNames, expressions, env);
					orig_ast = expressions;
					break;
				}

				case "recur":  // (recur exprs*)
					// [1] calculate new values
					final VncList recur_values = new VncList();
					for(int i=1; i<ast.size(); i++) {
						recur_values.addAtEnd(EVAL(ast.nth(i), env));
					}
					// [2] bind the values
					final VncList recur_bindingNames = recursionPoint.getLoopBindingNames();					
					final Env recur_env = recursionPoint.getLoopEnv();
					for(int i=0; i<recur_bindingNames.size(); i++) {
						final VncSymbol key = Coerce.toVncSymbol(recur_bindingNames.nth(i));
						recur_env.set(key, recur_values.nth(i));
					}
					orig_ast = recursionPoint.getLoopExpressions();
					env = recur_env;
					break;
					
				case "quote":
					return ast.nth(1);
					
				case "quasiquote":
					orig_ast = quasiquote(ast.nth(1));
					break;
	
				case "defmacro":
					return defmacro_(ast, env);

				case "macroexpand": 
					return macroexpand(ast.nth(1), env);
					
				case "try":  // (try expr (catch :Exception e expr) (finally expr))
					return try_(ast, env);
					
				case "try-with": // (try-with [bindings*] expr (catch :Exception e expr) (finally expr))
					env = new Env(env);
					return try_with_(ast, env);
					
				case "do":
					if (ast.size() < 2) {
						orig_ast = Constants.Nil;
					}
					else {
						final VncList head_exprs = ast.slice(1, ast.size()-1);
						eval_ast(head_exprs, env);
						orig_ast = ast.last();
					}
					break;
					
				case "if": 
					final VncVal condArg = ast.nth(1);
					VncVal cond = EVAL(condArg, env);
					if (cond == Nil || cond == False) {
						// eval false slot form
						if (ast.size() > 3) {
							orig_ast = ast.nth(3);
						} 
						else {
							return Nil;
						}
					} 
					else {
						// eval true slot form
						orig_ast = ast.nth(2);
					}
					break;
					
				case "fn":
					final VncList fnParams = Coerce.toVncList(ast.nth(1));
					final VncList preConditions = getFnPreconditions(ast.nth(2));
					final VncVal fnBody = preConditions == null ? ast.nth(2) : ast.nth(3);
					final Env cur_env = env;
					final VncFunction fn = new VncFunction(fnBody, env, fnParams) {
												public VncVal apply(final VncList args) {
													final Env localEnv = new Env(cur_env);
													
													// destructuring fn params -> args
													Destructuring
														.destructure(fnParams, args)
														.forEach(b -> localEnv.set(b.sym, b.val));
													
													validateFnPreconditions(preConditions, localEnv);
													
													return EVAL(fnBody, localEnv);
												}
											};
					return fn;

				case "import":
					ast.slice(1).forEach(clazz -> javaImports.add(Coerce.toVncString(clazz).getValue()));
					return Nil;

				default:
					final VncList el = Coerce.toVncList(eval_ast(ast, env));
					if (Types.isVncFunction(el.nth(0))) {
						final VncFunction f = (VncFunction)el.nth(0);
//						final VncVal fnast = f.getAst();
//						if (fnast != null) {
//							orig_ast = fnast;
//							env = f.genEnv(el.slice(1));
//						} 
//						else {
							final VncList fnArgs = el.rest();
							MetaUtil.copyTokenPos(el, fnArgs);
							return f.apply(fnArgs);
//						}
					}
					else if (Types.isVncKeyword(el.nth(0))) {
						// keyword as function to access map: (:a {:a 100})
						final VncKeyword k = (VncKeyword)el.nth(0);
						final VncList fnArgs = el.rest();
						MetaUtil.copyTokenPos(el, fnArgs);
						return k.apply(fnArgs);
					}
					else {
						throw new VncException(
								String.format(
										"Not a function or keyword: '%s'", 
										PRINT(el.nth(0))));
					}
			}
		}
	}

	// print
	public String PRINT(final VncVal exp) {
		return Printer._pr_str(exp, true);
	}
	
	public VncVal RE(final String script, final String filename, final Env env) {
		final VncVal ast = READ(script, filename);
		return EVAL(ast, env);
	}
	
	public Env createEnv(final PrintStream stdout) {
		return createEnv(stdout, null);
	}

	public Env createEnv(final PrintStream stdout, final List<String> preloadedExtensionModules) {
		final Env env = new Env(null);

		
		// core functions defined in Java
		Functions.functions.keySet().forEach(
				key -> env.set(
						Types.isVncSymbol(key) ? (VncSymbol)key : ((VncString)key).toSymbol(), 
						Functions.functions.get(key)));

		// JavaInterop function
		env.set(new VncSymbol("."), JavaInteropFn.create(javaImports)); 
		env.set(new VncSymbol("proxify"), new JavaInteropProxifyFn(javaImports)); 

		// set Venice version
		env.set(new VncSymbol("*version*"), new VncString(Version.VERSION));

		// set system newline
		env.set(new VncSymbol("*newline*"), new VncString(System.lineSeparator()));

		// set system stdout
		env.set(new VncSymbol("*out*"), new VncJavaObject(stdout == null ? new PrintStream(System.out, true) : stdout));

		// load core.venice 
		RE("(eval " + ModuleLoader.load("core") + ")", "core.venice", env);
		
		if (preloadedExtensionModules != null) {
			preloadedExtensionModules.forEach(
				m -> RE("(eval " + ModuleLoader.load(m) + ")", m + ".venice", env));
		}
		
		return env;
	}
	
	/**
	 * Resolves a class name.
	 * 
	 * @param className A simple class name like 'Math' or a class name
	 *                  'java.lang.Math'
	 * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed 
	 *         value if a mapping does nor exist 
	 */
	public String resolveClassName(final String className) {
		return javaImports.resolveClassName(className);
	}
	
	private VncVal defmacro_(final VncList ast, final Env env) {
		final boolean hasMeta = ast.size() > 4;
		final VncMap defMeta = hasMeta ? (VncMap)EVAL(ast.nth(1), env) : new VncHashMap();
		final VncVal macroName = ast.nth(hasMeta ? 2 : 1);
		final VncList macroParams = Coerce.toVncList(ast.nth(hasMeta ? 3 : 2));
		final VncVal macroFnAst = ast.nth(hasMeta ? 4 : 3);

		final String sMacroName = Types.isVncSymbol(macroName) 
									? ((VncSymbol)macroName).getName() 
									: ((VncString)macroName).getValue();

		final Env _env = env;
		final VncFunction macroFn = 
				new VncFunction(sMacroName, macroFnAst, env, macroParams) {
					public VncVal apply(final VncList args) {
						final Env localEnv = new Env(_env);

						// destructuring macro params -> args
						Destructuring
							.destructure(macroParams, args)
							.forEach(b -> localEnv.set(b.sym, b.val));
						
						// run the macro
						final VncVal result = EVAL(macroFnAst, localEnv);
						return result;
					}
				};

		macroFn.setMacro();
		env.set((VncSymbol)macroName, MetaUtil.addDefMeta(macroFn, defMeta));
		return macroFn;
	}
	
	private VncVal try_(final VncList ast, final Env env) {
		VncVal result = Nil;

		try {
			result = EVAL(ast.nth(1), env);
		} 
		catch (Throwable th) {
			CatchBlock catchBlock = null;
			if (ast.size() > 2) {
				catchBlock = findCatchBlockMatchingThrowable(ast.slice(2), th);
				if (catchBlock != null) {
					env.set(catchBlock.getExSym(), new VncJavaObject(th));
					
					final VncVal blocks = eval_ast(catchBlock.getBody(), env);
					result = Coerce.toVncList(blocks).first();
				}
			}
			
			if (catchBlock == null) {
				throw th;
			}
		}
		finally {
			if (ast.size() > 2) {
				final VncList finallyBlock = findFirstFinallyBlock(ast.slice(2));
				if (finallyBlock != null) {
					eval_ast(finallyBlock.slice(1), env);
				}
			}
		}
		
		return result;
	}

	private VncVal try_with_(final VncList ast, final Env env) {
		final VncList bindings = Coerce.toVncList(ast.nth(1));
		final List<Binding> boundResources = new ArrayList<>();
		
		for(int i=0; i<bindings.size(); i+=2) {
			final VncVal sym = bindings.nth(i);
			final VncVal val = EVAL(bindings.nth(i+1), env);

			if (Types.isVncSymbol(sym)) {
				env.set((VncSymbol)sym, val);
				boundResources.add(new Binding((VncSymbol)sym, val));
			}
			else {
				throw new VncException(
						String.format(
								"Invalid 'try-with' destructuring symbol value type %s. Expected symbol. %s",
								Types.getClassName(sym),
								ErrorMessage.buildErrLocation(ast)));
			}
		}

		
		VncVal result = Nil;
		try {
			try {
				result = EVAL(ast.nth(2), env);
			} 
			catch (Throwable th) {
				CatchBlock catchBlock = null;
				if (ast.size() > 3) {
					catchBlock = findCatchBlockMatchingThrowable(ast.slice(3), th);
					if (catchBlock != null) {
						env.set(catchBlock.getExSym(), new VncJavaObject(th));

						final VncVal blocks = eval_ast(catchBlock.getBody(), env);
						result = Coerce.toVncList(blocks).first();
					}
				}
						
				if (catchBlock == null) {
					throw th;
				}
			}
			finally {
				// finally is only for side effects
				if (ast.size() > 3) {
					final VncList finallyBlock = findFirstFinallyBlock(ast.slice(3));
					if (finallyBlock != null) {
						eval_ast(finallyBlock.slice(1), env);
					}
				}
			}
		}
		finally {
			// close resources in reverse order
			Collections.reverse(boundResources);
			boundResources.stream().forEach(b -> {
				final VncVal resource = b.val;
				if (Types.isVncJavaObject(resource)) {
					final Object r = ((VncJavaObject)resource).getDelegate();
					if (r instanceof AutoCloseable) {
						try {
							((AutoCloseable)r).close();
						}
						catch(Exception ex) {
							throw new VncException(
									String.format(
											"'try-with' failed to close resource %s. %s",
											b.sym.getName(),
											ErrorMessage.buildErrLocation(ast)));
						}
					}
					else if (r instanceof Closeable) {
						try {
							((Closeable)r).close();
						}
						catch(Exception ex) {
							throw new VncException(
									String.format(
											"'try-with' failed to close resource %s. %s",
											b.sym.getName(),
											ErrorMessage.buildErrLocation(ast)));
						}
					}
				}
			});
		}
		
		return result;
	}

	private CatchBlock findCatchBlockMatchingThrowable(
			final VncList blocks, 
			final Throwable th
	) {
		final VncList block = blocks
								.stream()
								.map(b -> (VncList)b)
								.filter(b -> ((VncSymbol)b.first()).getName().equals("catch"))
								.filter(b -> isCatchBlockMatchingThrowable(b, th))
								.findFirst()
								.orElse(null);
		
		if (block != null) {
			final VncSymbol sym = Coerce.toVncSymbol(block.nth(2));
			return new CatchBlock(sym, block.slice(3));
		}
		else {
			return null;
		}
	}
	
	private boolean isCatchBlockMatchingThrowable(
		final VncList block, 
		final Throwable th
	) {
		final String className = resolveClassName(((VncString)block.nth(1)).getValue());
		final Class<?> targetClass = ReflectionAccessor.classForName(className);
		
		if (targetClass.isAssignableFrom(th.getClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private VncList findFirstFinallyBlock(final VncList blocks) {
		for(int ii=0; ii<blocks.size(); ii++) {
			final VncList block = Coerce.toVncList(blocks.nth(ii));
			
			final VncSymbol sym = Coerce.toVncSymbol(block.nth(0));
			if (sym.getName().equals("finally")) {
				return block;
			}
		}
		return null;
	}

	private VncList getFnPreconditions(final VncVal prePostConditions) {
		if (Types.isVncMap(prePostConditions)) {
			final VncVal val = ((VncMap)prePostConditions).get(PRE_CONDITION_KEY);
			if (Types.isVncList(val)) {
				return (VncList)val;
			}
		}
		
		return null;
	}
	
	private boolean isFnConditionTrue(final VncVal result) {
		return (Types.isVncList(result)) ? ((VncList)result).first() == True : result == True;
	}

	private void validateFnPreconditions(final VncList preConditions, final Env env) {
		if (preConditions != null) {
	 		final Env local = new Env(env);	
	 		preConditions.forEach(v -> {
				if (!isFnConditionTrue(EVAL(v, local))) {
					throw new AssertionException(
							String.format(
									"pre-condition assert failed: %s. %s",
									((VncString)CoreFunctions.str.apply(new VncList(v))).getValue(),
									ErrorMessage.buildErrLocation(v)));		
				}
 			});
		}
	}

	
	private static final VncKeyword PRE_CONDITION_KEY = new VncKeyword(":pre");

	private final JavaImports javaImports = new JavaImports();
}
