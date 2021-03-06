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

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Symbols {
	
	public Symbols() {
	}

	public boolean contains(final VncSymbol key) {
		return symbols.containsKey(key.getName());
	}

	public VncVal get(final VncSymbol key) {
		return symbols.get(key.getName());
	}

	public void set(final VncSymbol key, final VncVal value) {
		if (key != null) {
			symbols.put(key.getName(), value);
		}
	}
	
	@Override
	public String toString() {
		return symbols.toString();
	}
	
	
	private final Map<String,VncVal> symbols = new HashMap<>();
}
