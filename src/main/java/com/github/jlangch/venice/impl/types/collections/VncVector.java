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
package com.github.jlangch.venice.impl.types.collections;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncVector extends VncList {

	public VncVector(final List<VncVal> val) {
		super(val);
	}
	
	public VncVector(final VncVal... mvs) {
		super(mvs);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public VncVector copy() {
		final VncVector v = new VncVector((ArrayList<VncVal>)((ArrayList<VncVal>)getList()).clone());
		v.setMeta(getMeta());
		return v;
	}

	@Override
	public boolean isList() { 
		return false; 
	}
	
	@Override
	public VncVector rest() {
		if (isEmpty()) {
			return new VncVector();
		} 
		else {
			return new VncVector(getList().subList(1, getList().size()));
		}
	}

	@Override
	public VncVector slice(final int start) {
		return slice(start, getList().size());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncVector(getList().subList(start, end));
	}
	
	@Override
	public VncVector empty() {
		return new VncVector();
	}

	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncList)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncList)o).nth(ii));
				if (c != 0) {
					return c;
				}
			}
			
		}
		
		return 0;
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}

}