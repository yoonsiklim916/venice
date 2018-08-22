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
package com.github.jlangch.venice.impl.types;

import java.util.Map;

import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class VncThreadLocal extends VncVal {

	public VncThreadLocal() { 
	}
	
	public VncThreadLocal(final Map<VncVal,VncVal> val) {
		val.entrySet().forEach(e -> set(Coerce.toVncKeyword(e.getKey()), e.getValue()));
	}
	
	public VncThreadLocal(final VncList lst) {
		assoc(lst);
	}

	public VncThreadLocal copy() { 
		return this;
	}

	public VncVal get(final VncKeyword key) {
		return ThreadLocalMap.get(key);
	}
	
	public void set(final VncKeyword key, final VncVal val) {
		ThreadLocalMap.set(key, val);
	}
	
	public void remove(final VncKeyword key) {
		ThreadLocalMap.remove(key);
	}

	
	public VncVal containsKey(final VncKeyword key) {
		return key != null && ThreadLocalMap.containsKey(key) ? Constants.True : Constants.False;
	}

	public VncThreadLocal assoc(final VncVal... mvs) {
		for (int i=0; i<mvs.length; i+=2) {
			set(Coerce.toVncKeyword(mvs[i]), mvs[i+1]);
		}
		return this;
	}

	public VncThreadLocal assoc(final VncList lst) {
		for (int i=0; i<lst.getList().size(); i+=2) {
			set(Coerce.toVncKeyword(lst.nth(i)), lst.nth(i+1));
		}
		return this;
	}

	public VncThreadLocal dissoc(final VncList lst) {
		for (int i=0; i<lst.getList().size(); i++) {
			remove(Coerce.toVncKeyword(lst.nth(i)));
		}
		return this;
	}

	public VncThreadLocal clear() {
		ThreadLocalMap.clear();
		return this;
	}

	
	@Override 
	public String toString() {
		return "ThreadLocal";
	}
}