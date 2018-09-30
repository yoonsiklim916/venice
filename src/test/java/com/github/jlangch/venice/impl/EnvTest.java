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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncSymbol;


public class EnvTest {

	@Test
	public void testSingleLevel() {
		final Env env = new Env();
		
		env.set(new VncSymbol("a"), new VncLong(100));
		env.set(new VncSymbol("b"), new VncLong(200));
		env.set(new VncSymbol("c"), new VncLong(300));
		env.setGlobal(new VncSymbol("g"), new VncLong(900));
		
		assertEquals(new VncLong(100), env.get(new VncSymbol("a")));
		assertEquals(new VncLong(200), env.get(new VncSymbol("b")));
		assertEquals(new VncLong(300), env.get(new VncSymbol("c")));
		assertEquals(new VncLong(900), env.get(new VncSymbol("g")));

		assertThrows(VncException.class, () -> env.get(new VncSymbol("x")));
	}

	@Test
	public void testMultiLevel() {
		final Env env_1 = new Env();
		env_1.set(new VncSymbol("a"), new VncLong(100));
		
		final Env env_2 = new Env(env_1);
		env_2.set(new VncSymbol("b"), new VncLong(200));
		
		final Env env_3 = new Env(env_2);
		env_3.set(new VncSymbol("c"), new VncLong(300));

		env_3.setGlobal(new VncSymbol("g"), new VncLong(900));

		assertEquals(new VncLong(100), env_1.get(new VncSymbol("a")));
		assertEquals(new VncLong(900), env_1.get(new VncSymbol("g")));

		assertEquals(new VncLong(100), env_2.get(new VncSymbol("a")));
		assertEquals(new VncLong(200), env_2.get(new VncSymbol("b")));
		assertEquals(new VncLong(900), env_2.get(new VncSymbol("g")));

		assertEquals(new VncLong(100), env_3.get(new VncSymbol("a")));
		assertEquals(new VncLong(200), env_3.get(new VncSymbol("b")));
		assertEquals(new VncLong(300), env_3.get(new VncSymbol("c")));
		assertEquals(new VncLong(900), env_3.get(new VncSymbol("g")));

		assertThrows(VncException.class, () -> env_1.get(new VncSymbol("x")));
		assertThrows(VncException.class, () -> env_2.get(new VncSymbol("x")));
		assertThrows(VncException.class, () -> env_3.get(new VncSymbol("x")));
		
		env_3.set(new VncSymbol("a"), new VncLong(101));
		env_3.setGlobal(new VncSymbol("g"), new VncLong(901));
		assertEquals(new VncLong(100), env_1.get(new VncSymbol("a")));
		assertEquals(new VncLong(100), env_2.get(new VncSymbol("a")));
		assertEquals(new VncLong(101), env_3.get(new VncSymbol("a")));
		assertEquals(new VncLong(901), env_1.get(new VncSymbol("g")));
		assertEquals(new VncLong(901), env_2.get(new VncSymbol("g")));
		assertEquals(new VncLong(901), env_3.get(new VncSymbol("g")));
	}
	
}