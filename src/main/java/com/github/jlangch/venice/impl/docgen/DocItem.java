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
package com.github.jlangch.venice.impl.docgen;

import java.util.List;


public class DocItem {
	
	public DocItem(
			final String name, 
			final List<String> signatures,
			final String description,
			final String examples,
			final String id
	) {
		this.name = name;
		this.signatures = signatures;
		this.description = description;
		this.examples = examples;
		this.id = id;
	}

	public DocItem(final String name, final String id) {
		this(name, null, null, null, id);
	}

	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public List<String> getSignatures() {
		return signatures;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getExamples() {
		return examples;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocItem other = (DocItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	

	private final String name;
	private final String id;
	
	private final List<String> signatures;
	private final String description;
	private final String examples;
}
