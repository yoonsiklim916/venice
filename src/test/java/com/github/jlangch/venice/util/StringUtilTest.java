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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;


public class StringUtilTest {

	@Test
	public void escapeTest() {
		assertEquals("", StringUtil.escape(""));
		assertEquals("a", StringUtil.escape("a"));
		assertEquals("abc-123", StringUtil.escape("abc-123"));
		
		assertEquals(" \\n \\r \\t \\\" \\\\ ", StringUtil.escape(" \n \r \t \" \\ "));				
	}
	
	public void unescapeTest() {
		assertEquals("", StringUtil.unescape(""));
		assertEquals("a", StringUtil.unescape("a"));
		assertEquals("abc-123", StringUtil.unescape("abc-123"));

		assertEquals(" \n \r \t \" \\ ", StringUtil.unescape(" \\n \\r \\t \\\" \\\\ "));				
	}
}
