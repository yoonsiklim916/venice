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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class CoreFunctionsTest {
	
	@Test
	public void test_any_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) nil)"));	
		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) [])"));	
		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) [:a])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [:a 1 2])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [1])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [1 2])"));	
	}
	
	@Test
	public void test_apply() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(12), venice.eval("(apply + [3 4 5])"));
		
		assertEquals(Long.valueOf(10), venice.eval("(apply + 1 2 [3 4]))"));
	
		assertEquals(Long.valueOf(3), venice.eval("(apply + '(1 2))"));
	}
	
	@Test
	public void test_assoc() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc {} :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (ordered-map ) :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (sorted-map ) :a 1 :b 2))"));
		
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc {:a 4} :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (ordered-map :a 4) :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (sorted-map :a 4) :a 1 :b 2))"));
		
		assertEquals("[:a :b :z]", venice.eval("(str (assoc [:x :y :z] 0 :a 1 :b))"));
		assertEquals("[:x :y :z :a]", venice.eval("(str (assoc [:x :y :z] 99 :a))"));
		
		assertEquals("abz", venice.eval("(str (assoc \"xyz\" 0 \"a\" 1 \"b\"))"));
		assertEquals("xyza", venice.eval("(str (assoc \"xyz\" 99 \"a\"))"));
	}
	
	@Test
	public void test_assoc_in() {
		final Venice venice = new Venice();

		// map
		assertEquals("{:a 100}", venice.eval("(str (assoc-in {} [:a] 100))"));
		assertEquals("{:a {:b {:c 100}}}", venice.eval("(str (assoc-in {} [:a :b :c] 100))"));
		assertEquals("{:a {:b 1 :c 100}}", venice.eval("(str (assoc-in {:a {:b 1}} [:a :c] 100))"));

		// vector
		assertEquals("[100]", venice.eval("(str (assoc-in [] [0] 100))"));
		assertEquals("[0 100]", venice.eval("(str (assoc-in [0] [1] 100))"));
		assertEquals("[0 100]", venice.eval("(str (assoc-in [0 1] [1] 100))"));
		assertEquals("[[0 1] [2 100]]", venice.eval("(str (assoc-in [[0 1] [2 3]] [1 1] 100))"));
		assertEquals("[[0 1] [2 3 100]]", venice.eval("(str (assoc-in [[0 1] [2 3]] [1 2] 100))"));
		
		// map / vector
		assertEquals("{:a {:b [0 9 2]}}", venice.eval("(str (assoc-in {:a {:b [0 1 2]}} [:a :b 1] 9))"));
		assertEquals("{:a {:b [0 1 2 9]}}", venice.eval("(str (assoc-in {:a {:b [0 1 2]}} [:a :b 3] 9))"));
		
		// vector / map
		assertEquals("[0 1 {:a 1 :b {:c 9}}]", venice.eval("(str (assoc-in [0 1 {:a 1 :b {:c 2}}] [2 :b :c] 9))"));
	}
	
	@Test
	public void test_boolean() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(boolean nil)"));	
		assertTrue((Boolean)venice.eval("(boolean true)"));	
		assertFalse((Boolean)venice.eval("(boolean false)"));	
		assertTrue((Boolean)venice.eval("(boolean 0)"));
		assertTrue((Boolean)venice.eval("(boolean 1)"));
		assertTrue((Boolean)venice.eval("(boolean \"true\")"));
		assertTrue((Boolean)venice.eval("(boolean \"false\")"));
	}
	
	@Test
	public void test_boolean_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(boolean? nil)"));	
		assertTrue((Boolean)venice.eval("(boolean? true)"));	
		assertTrue((Boolean)venice.eval("(boolean? false)"));	
		assertFalse((Boolean)venice.eval("(boolean? 1)"));	
		assertFalse((Boolean)venice.eval("(boolean? -3.0)"));
		assertFalse((Boolean)venice.eval("(boolean? -3.0M)"));
		assertFalse((Boolean)venice.eval("(boolean? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(boolean? :a)"));
		assertFalse((Boolean)venice.eval("(boolean? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(boolean? '())"));
		assertFalse((Boolean)venice.eval("(boolean? [])"));
		assertFalse((Boolean)venice.eval("(boolean? {})"));
	}
	
	@Test
	public void test_butlast() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str (butlast nil))"));
		assertEquals("()", venice.eval("(str (butlast '()))"));
		assertEquals("()", venice.eval("(str (butlast '(1)))"));
		assertEquals("(1)", venice.eval("(str (butlast '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (butlast '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (butlast []))"));
		assertEquals("[]", venice.eval("(str (butlast [1]))"));
		assertEquals("[1]", venice.eval("(str (butlast [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (butlast [1 2 3]))"));
	}
	
	@Test
	public void test_bytebuf() {
		final Venice venice = new Venice();

		assertArrayEquals(new byte[0], ((ByteBuffer)venice.eval("(bytebuf)")).array());	
		assertArrayEquals(new byte[] {0,1,2}, ((ByteBuffer)venice.eval("(bytebuf [0 1 2])")).array());		
		assertEquals("(0 1 2)", venice.eval("(str (into '() (bytebuf [0 1 2])))"));		
		assertEquals("(97 98 99)", venice.eval("(str (into '() (bytebuf \"abc\")))"));		
		assertEquals("[0 1 2]", venice.eval("(str (into [] (bytebuf [0 1 2])))"));		
		assertEquals("[97 98 99]", venice.eval("(str (into [] (bytebuf \"abc\")))"));		
	}
	
	@Test
	public void test_bytebuf_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(bytebuf? (bytebuf))"));		
		assertFalse((Boolean)venice.eval("(bytebuf? 1)"));		
	}
	
	@Test
	public void test_bytebuf_from_string() {
		final Venice venice = new Venice();

		assertArrayEquals(new byte[] {97,98,99,100,101,102}, ((ByteBuffer)venice.eval("(bytebuf-from-string \"abcdef\" :UTF-8)")).array());		
	}
	
	@Test
	public void test_bytebuf_to_string() {
		final Venice venice = new Venice();

		assertEquals("abcdef",  venice.eval("(bytebuf-to-string (bytebuf [97 98 99 100 101 102]) :UTF-8)"));		
	}

	@Test
	public void test_bytebuf_sub() {
		final Venice venice = new Venice();

		assertArrayEquals(new byte[] {3,4,5}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 3)")).array());		
		assertArrayEquals(new byte[] {0,1,2}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 0 3)")).array());		
		assertArrayEquals(new byte[] {2,3,4}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 2 5)")).array());		
	}

	@Test
	public void test_class() {
		final Venice venice = new Venice();

		assertEquals("venice.Long", venice.eval("(class 1)"));
		assertEquals("venice.Double", venice.eval("(class 1.0)"));
		assertEquals("venice.List", venice.eval("(class '())"));
		assertEquals("venice.Vector", venice.eval("(class '[])"));
		assertEquals("venice.HashMap", venice.eval("(class '{})"));

		// Java Interop
		
		assertEquals("venice.JavaList(java.util.ArrayList)", venice.eval("(class (. :java.util.ArrayList :new))"));
		assertEquals("venice.JavaSet(java.util.HashSet)", venice.eval("(class (. :java.util.HashSet :new))"));
		assertEquals("venice.JavaMap(java.util.HashMap)", venice.eval("(class (. :java.util.HashMap :new))"));
	}
	
	@Test
	public void test_coalesce() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(coalesce )"));	
		assertEquals(null, venice.eval("(coalesce nil)"));	
		assertEquals(null, venice.eval("(coalesce nil nil nil)"));	
		assertEquals(Long.valueOf(1L), venice.eval("(coalesce 1)"));	
		assertEquals(Long.valueOf(1L), venice.eval("(coalesce nil 1 2)"));	
	}
	
	@Test
	public void test_coll_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(coll? nil)"));	
		assertFalse((Boolean)venice.eval("(coll? 1)"));	
		assertTrue((Boolean)venice.eval("(coll? '(1 2))"));	
		assertTrue((Boolean)venice.eval("(coll? [1 2])"));	
		assertTrue((Boolean)venice.eval("(coll? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(coll? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (sorted-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (set 1 2))"));	
	}
	
	@Test
	public void test_comp() {
		final Venice venice = new Venice();

		assertEquals("24", venice.eval("((comp str +) 8 8 8)"));
		
		assertEquals("(-5 -7 -9 -11)", venice.eval("(str (map (comp - (partial + 3) (partial * 2)) [1 2 3 4]))")); 

		assertEquals("-315", venice.eval("(str (reduce (comp - (partial + 3) (partial * 2)) [1 2 3 4]))")); 

		assertEquals("(1 2 3)", venice.eval("(str (map (comp inc) [0 1 2]))"));

		assertEquals("[false false]", venice.eval("(str (filter (comp not) [false true false]))"));

		assertEquals("[1 2 3]", venice.eval("(str (filter (comp not zero?) [0 1 0 2 0 3]))"));
		
		assertEquals("5", venice.eval(
								"(do " +
								"   (def fifth (comp first rest rest rest rest)) " +
								"   (str (fifth [1 2 3 4 5])))"));
	}
	
	@Test
	public void test_compare() {
		final Venice venice = new Venice();

		// nil
		assertEquals(Long.valueOf(-1), venice.eval("(compare nil 1)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare nil nil)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare 1 nil)"));

		// boolean
		assertEquals(Long.valueOf(0),  venice.eval("(compare false false)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare true true)"));
		assertEquals(Long.valueOf(-1), venice.eval("(compare false true)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare true false)"));

		// symbol
		assertEquals(Long.valueOf(-1), venice.eval("(compare 'a 'b)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare 'a 'a)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare 'b 'a)"));

		// keyword
		assertEquals(Long.valueOf(-1), venice.eval("(compare :a :b)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare :a :a)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare :b :a)"));

		// long
		assertEquals(Long.valueOf(-1), venice.eval("(compare 0 1)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare 1 1)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare 1 0)"));

		// double
		assertEquals(Long.valueOf(-1), venice.eval("(compare 0.0 1.0)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare 1.0 1.0)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare 1.0 0.0)"));

		// decimal
		assertEquals(Long.valueOf(-1), venice.eval("(compare 0.0M 1.0M)"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare 1.0M 1.0M)"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare 1.0M 0.0M)"));

		// string
		assertEquals(Long.valueOf(-1), venice.eval("(compare \"a\" \"b\")"));
		assertEquals(Long.valueOf(-1), venice.eval("(compare \"aa\" \"b\")"));
		assertEquals(Long.valueOf(-1), venice.eval("(compare \"aaa\" \"bb\")"));
		assertEquals(Long.valueOf(-1), venice.eval("(compare \"aaa\" \"bbb\")"));
		assertEquals(Long.valueOf(0),  venice.eval("(compare \"aaa\" \"aaa\")"));
		assertEquals(Long.valueOf(1),  venice.eval("(compare \"bbb\" \"aaa\")"));
	}
	
	@Test
	public void test_conj() {
		final Venice venice = new Venice();

		// List
		assertEquals("(1)", venice.eval("(str (conj '() 1))"));
		assertEquals("(1 2)", venice.eval("(str (conj '(2) 1))"));
		assertEquals("(1 2 3)", venice.eval("(str (conj '(2 3) 1))"));
		assertEquals("(1 2)", venice.eval("(str (conj '() 2 1))"));
		assertEquals("(1 2 3)", venice.eval("(str (conj '(3) 2 1))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (conj '(3 4) 2 1))"));
		assertEquals("((1 2))", venice.eval("(str (conj '() '(1 2)))"));
		assertEquals("((1 2) (3 4))", venice.eval("(str (conj '((3 4)) '(1 2)))"));
		assertEquals("(0 (1 2) (3 4) (5 6))", venice.eval("(str (conj '((5 6)) '(3 4) '(1 2) 0))"));

		// Vector
		assertEquals("[1]", venice.eval("(str (conj [] 1))"));
		assertEquals("[1 2]", venice.eval("(str (conj [1] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (conj [1 2] 3))"));		
		assertEquals("[1 2]", venice.eval("(str (conj [] 1 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (conj [1] 2 3))"));
		assertEquals("[1 2 3 4]", venice.eval("(str (conj [1 2] 3 4))"));
		assertEquals("[[1 2]]", venice.eval("(str (conj [] [1 2]))"));
		assertEquals("[[1 2] [3 4]]", venice.eval("(str (conj [[1 2]] [3 4]))"));
		assertEquals("[[1 2] [3 4] [5 6] 7]", venice.eval("(str (conj [[1 2]] [3 4] [5 6] 7))"));
		
		// Map
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1 :b 2) [:c 3]))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1 :b 2) {:c 3}))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1) (ordered-map :b 2 :c 3)))"));
	}

	@Test
	public void test_concat() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (concat '()))"));
		assertEquals("()", venice.eval("(str (concat '() '()))"));
		assertEquals("()", venice.eval("(str (concat '() '() '()))"));
		assertEquals("(1)", venice.eval("(str (concat '(1)))"));
		assertEquals("(1)", venice.eval("(str (concat '(1) '() '()))"));
		assertEquals("(1 2)", venice.eval("(str (concat '(1 2) '() '()))"));
		assertEquals("(1 2 3)", venice.eval("(str (concat '(1 2) '(3) '()))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (concat '(1 2) '(3 4) '()))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (concat '(1 2) '(3 4) '(5)))"));
		assertEquals("(1 2 3 4 5 6)", venice.eval("(str (concat '(1 2) '(3 4) nil '(5 6)))"));

		assertEquals("()", venice.eval("(str (concat []))"));
		assertEquals("()", venice.eval("(str (concat [] []))"));
		assertEquals("()", venice.eval("(str (concat [] [] []))"));
		assertEquals("(1)", venice.eval("(str (concat [1]))"));
		assertEquals("(1)", venice.eval("(str (concat [1] [] []))"));
		assertEquals("(1 2)", venice.eval("(str (concat [1 2] [] []))"));
		assertEquals("(1 2 3)", venice.eval("(str (concat [1 2] [3] []))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (concat [1 2] [3 4] []))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (concat [1 2] [3 4] [5]))"));
		assertEquals("(1 2 3 4 5 6)", venice.eval("(str (concat [1 2] [3 4] nil [5 6]))"));

		assertEquals("([:a 1] [:b 2])", venice.eval("(str (concat (ordered-map :a 1 :b 2)))"));
		
		assertEquals("()", venice.eval("(str (concat \"\"))"));
		assertEquals("(a)", venice.eval("(str (concat \"a\"))"));
		assertEquals("(a b c d e f)", venice.eval("(str (concat \"abc\" \"def\"))"));
		
		
		// Java Interop
		
		assertEquals(
				"(1 2 3 4)", 
				venice.eval(
						"(str (concat                                 " +
						"        (doto (. :java.util.ArrayList :new)  " +
						"	           (. :add 1)                     " +
						"	           (. :add 2))                    " +
						"        '(3 4)))                             "));

		assertEquals(
				"([a 1] [b 2] 3 4)", 
				venice.eval(
						"(str (concat                                    " +
						"        (doto (. :java.util.LinkedHashMap :new) " +
						"	           (. :put :a 1)                     " +
						"	           (. :put :b 2))                    " +
						"        '(3 4)))                                "));

	}
	
	@Test
	public void test_cons() {
		final Venice venice = new Venice();

		// List
		assertEquals("(1)", venice.eval("(str (cons 1 '()))"));
		assertEquals("(1 2)", venice.eval("(str (cons 1 '(2)))"));
		assertEquals("(1 2 3)", venice.eval("(str (cons 1 '(2 3)))"));	
		assertEquals("((1 2))", venice.eval("(str (cons '(1 2) '()))"));
		assertEquals("((1 2) 3)", venice.eval("(str (cons '(1 2) '(3)))"));
		assertEquals("((1 2) 3 4)", venice.eval("(str (cons '(1 2) '(3 4)))"));

		// Vector
		assertEquals("(1)", venice.eval("(str (cons 1 []))"));
		assertEquals("(1 2)", venice.eval("(str (cons 1 [2]))"));
		assertEquals("(1 2 3)", venice.eval("(str (cons 1 [2 3]))"));
		assertEquals("([1 2])", venice.eval("(str (cons [1 2] []))"));
		assertEquals("([1 2] 3)", venice.eval("(str (cons [1 2] [3]))"));
		assertEquals("([1 2] 3 4)", venice.eval("(str (cons [1 2] [3 4]))"));
		
		// Map
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (cons {:c 3} (ordered-map :a 1 :b 2)))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (cons (ordered-map :c 3) (ordered-map :a 1 :b 2)))"));
	}
	
	@Test
	public void test_contains() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(contains? {:a 1 :b 2} :b)"));
		assertFalse((Boolean)venice.eval("(contains? {:a 1 :b 2} :z)"));

		assertTrue((Boolean)venice.eval("(contains? (ordered-map :a 1 :b 2) :b)"));
		assertFalse((Boolean)venice.eval("(contains? (ordered-map :a 1 :b 2) :z)"));

		assertTrue((Boolean)venice.eval("(contains? (sorted-map :a 1 :b 2) :b)"));
		assertFalse((Boolean)venice.eval("(contains? (sorted-map :a 1 :b 2) :z)"));
	}
	
	@Test
	public void test_count() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0L), venice.eval("(count nil)"));

		assertEquals(Long.valueOf(1L), venice.eval("(count '(1))"));
		assertEquals(Long.valueOf(2L), venice.eval("(count '(1 2))"));
		assertEquals(Long.valueOf(3L), venice.eval("(count '(1 2 3))"));

		assertEquals(Long.valueOf(0L), venice.eval("(count [])"));
		assertEquals(Long.valueOf(1L), venice.eval("(count [1])"));
		assertEquals(Long.valueOf(2L), venice.eval("(count [1 2])"));
		assertEquals(Long.valueOf(3L), venice.eval("(count [1 2 3])"));

		assertEquals(Long.valueOf(0L), venice.eval("(count {})"));
		assertEquals(Long.valueOf(1L), venice.eval("(count {:a 1})"));
		assertEquals(Long.valueOf(2L), venice.eval("(count {:a 1 :b 2})"));
		assertEquals(Long.valueOf(3L), venice.eval("(count {:a 1 :b 2 :c 3})"));
		
		assertEquals(Long.valueOf(0), venice.eval("(count (ordered-map ))"));
		assertEquals(Long.valueOf(1), venice.eval("(count (ordered-map :a 1))"));
		assertEquals(Long.valueOf(2), venice.eval("(count (ordered-map :a 1 :b 2))"));

		assertEquals(Long.valueOf(0), venice.eval("(count (sorted-map ))"));
		assertEquals(Long.valueOf(1), venice.eval("(count (sorted-map :a 1))"));
		assertEquals(Long.valueOf(2), venice.eval("(count (sorted-map :a 1 :b 2))"));

		assertEquals(Long.valueOf(0), venice.eval("(count \"\")"));
		assertEquals(Long.valueOf(1), venice.eval("(count \"a\")"));
		assertEquals(Long.valueOf(2), venice.eval("(count \"ab\")"));
		
		assertEquals(Long.valueOf(0L),venice.eval("(count (bytebuf))"));		
		assertEquals(Long.valueOf(3L),venice.eval("(count (bytebuf [0 1 2]))"));		


		// Java Interop
		
		assertEquals(
				Long.valueOf(2L), 
				venice.eval(
						"(count                                   " +
						"    (doto (. :java.util.ArrayList :new)  " +
						"	       (. :add 1)                     " +
						"	       (. :add 2)))                   " ));

		assertEquals(
				Long.valueOf(2L), 
				venice.eval(
						"(count                                       " +
						"    (doto (. :java.util.LinkedHashMap :new)  " +
						"	       (. :put :a 1)                      " +
						"	       (. :put :b 2)))                    " ));
	}

	@Test
	public void test_decimal() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("0"), venice.eval("(decimal false)"));
		assertEquals(new BigDecimal("1"), venice.eval("(decimal true)"));
		assertEquals(new BigDecimal("3"), venice.eval("(decimal 3)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("3.0M"));
		assertEquals(new BigDecimal("3.00"), venice.eval("(decimal 3 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.00"), venice.eval("(decimal \"3.0\" 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.30"), venice.eval("(decimal 3.3 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.12"), venice.eval("(decimal 3.123456M 2 :HALF_UP)"));
	}
	
	@Test
	public void test_decimal_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(decimal? nil)"));	
		assertFalse((Boolean)venice.eval("(decimal? true)"));	
		assertFalse((Boolean)venice.eval("(decimal? 1)"));	
		assertFalse((Boolean)venice.eval("(decimal? -3.0)"));
		assertTrue((Boolean)venice.eval("(decimal? -3.0M)"));
		assertFalse((Boolean)venice.eval("(decimal? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(decimal? :a)"));
		assertFalse((Boolean)venice.eval("(decimal? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(decimal? '())"));
		assertFalse((Boolean)venice.eval("(decimal? [])"));
		assertFalse((Boolean)venice.eval("(decimal? {})"));
	}
	
	@Test
	public void test_docoll() {
		final Venice venice = new Venice();

		// docoll on list
		final String script1 = 
				"(do                                                      " +
				"   (def counter (atom 0))                                " +
				"                                                         " +
				"   (def sum (fn [x] (swap! counter (fn [n] (+ n x)))))   " +
				"                                                         " +
				"   (docoll sum '(1 2 3 4))                               " +
				"   (deref counter)                                       " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script1));

		// docoll on vector
		final String script2 = 
				"(do                                                      " +
				"   (def counter (atom 0))                                " +
				"                                                         " +
				"   (def sum (fn [x] (swap! counter (fn [n] (+ n x)))))   " +
				"                                                         " +
				"   (docoll sum [1 2 3 4])                                " +
				"   (deref counter)                                       " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script2));

		// docoll on map
		final String script3 = 
				"(do                                                          " +
				"   (def counter (atom 0))                                    " +
				"                                                             " +
				"   (def sum (fn [[k v] x] (swap! counter (fn [n] (+ n v))))) " +
				"                                                             " +
				"   (docoll sum {:a 1 :b 2 :c 3 :d 4})                        " +
				"   (deref counter)                                           " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script3));
	}
	
	@Test
	public void test_dedupe() {
		final Venice venice = new Venice();

		assertEquals("(0)", venice.eval("(str (dedupe '(0 0 0)))"));
		assertEquals("(0 1 2 1 3)", venice.eval("(str (dedupe '(0 1 2 2 2 1 3)))"));
		assertEquals("[0]", venice.eval("(str (dedupe [0 0 0]))"));
		assertEquals("[0 1 2 1 3]", venice.eval("(str (dedupe [0 1 2 2 2 1 3]))"));
	}

	@Test
	public void test_distinct() {
		final Venice venice = new Venice();

		assertEquals("(0 1 2 3)", venice.eval("(str (distinct '(0 1 2 1 3 3)))"));
		assertEquals("[0 1 2 3]", venice.eval("(str (distinct [0 1 2 1 3 3]))"));
	}

	@Test
	public void test_difference() {
		final Venice venice = new Venice();

		assertEquals("#{1 2 3}", venice.eval("(str (difference (set 1 2 3)))"));
		assertEquals("#{1}", venice.eval("(str (difference (set 1 2) (set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (difference (set 1 2 3) (set 1) (set 1 4) (set 3)))"));
	}

	@Test
	public void test_dissoc() {
		final Venice venice = new Venice();

		assertEquals("{:a 1}", venice.eval("(str (dissoc {:a 1 :b 2} :b)))"));
		assertEquals("{:a 1 :c 3}", venice.eval("(str (dissoc (ordered-map :a 1 :b 2 :c 3) :b)))"));
		assertEquals("{:a 1 :c 3}", venice.eval("(str (dissoc (sorted-map :a 1 :b 2 :c 3) :b)))"));
		
		assertEquals("[:y :z]", venice.eval("(str (dissoc [:x :y :z] 0))"));
		assertEquals("[:x :z]", venice.eval("(str (dissoc [:x :y :z] 1))"));
		assertEquals("[:x :y]", venice.eval("(str (dissoc [:x :y :z] 2))"));
		assertEquals("[:z]", venice.eval("(str (dissoc [:x :y :z] 0 0))"));
		assertEquals("[]", venice.eval("(str (dissoc [:x :y :z] 0 0 0))"));
		
		assertEquals("yz", venice.eval("(str (dissoc \"xyz\" 0))"));
		assertEquals("xz", venice.eval("(str (dissoc \"xyz\" 1))"));
		assertEquals("xy", venice.eval("(str (dissoc \"xyz\" 2))"));
		assertEquals("z", venice.eval("(str (dissoc \"xyz\" 0 0))"));
		assertEquals("", venice.eval("(str (dissoc \"xyz\" 0 0 0))"));
	}

	@Test
	public void test_double() {
		final Venice venice = new Venice();

		assertEquals(Double.valueOf(0.0), venice.eval("(double nil)"));
		assertEquals(Double.valueOf(0.0), venice.eval("(double false)"));
		assertEquals(Double.valueOf(1.0), venice.eval("(double true)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3.0)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3.0M)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double \"3.0\")"));
	}
	
	@Test
	public void test_double_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(double? nil)"));	
		assertFalse((Boolean)venice.eval("(double? true)"));	
		assertFalse((Boolean)venice.eval("(double? 1)"));	
		assertTrue((Boolean)venice.eval("(double? -3.0)"));
		assertFalse((Boolean)venice.eval("(double? -3.0M)"));
		assertFalse((Boolean)venice.eval("(double? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(double? :a)"));
		assertFalse((Boolean)venice.eval("(double? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(double? '())"));
		assertFalse((Boolean)venice.eval("(double? [])"));
		assertFalse((Boolean)venice.eval("(double? {})"));
	}
	
	@Test
	public void test_equals_Q() {
		final Venice venice = new Venice();
		
		// Nil
		assertTrue((Boolean)venice.eval("(== nil nil)"));
		
		// Boolean
		assertTrue((Boolean)venice.eval("(== true true)"));
		assertFalse((Boolean)venice.eval("(== true false)"));

		// Long
		assertTrue((Boolean)venice.eval("(== 2 2)"));
		assertFalse((Boolean)venice.eval("(== 2 3)"));

		// Double
		assertTrue((Boolean)venice.eval("(== 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3.0)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(== 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3.0M)"));

		// String
		assertTrue((Boolean)venice.eval("(== \"aa\" \"aa\")"));
		assertFalse((Boolean)venice.eval("(== \"aa\" \"zz\")"));

		// Keyword
		assertTrue((Boolean)venice.eval("(== :a :a)"));
		assertFalse((Boolean)venice.eval("(== :a :b)"));

		// String/Keyword
		assertTrue((Boolean)venice.eval("(== \"aa\" :aa)"));
		assertTrue((Boolean)venice.eval("(== :aa \"aa\")"));

		// List
		assertTrue((Boolean)venice.eval("(== '(1 2) '(1 2))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '(1 4))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '(1))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '())"));
		assertFalse((Boolean)venice.eval("(== '(1 2) nil)"));

		// Vector
		assertTrue((Boolean)venice.eval("(== [1 2] [1 2])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [1 4])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [1])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [])"));
		assertFalse((Boolean)venice.eval("(== [1 2] nil)"));

		// Map
		assertTrue((Boolean)venice.eval("(== {a: 1 b: 2} {a: 1 b: 2})"));
		assertFalse((Boolean)venice.eval("(== {a: 1 b: 2} {a: 1 b: 3})"));
		assertFalse((Boolean)venice.eval("(== {a: 1 b: 2} {a: 1 c: 2})"));
		assertFalse((Boolean)venice.eval("(== {a: 1 b: 2} {a: 1})"));
		assertFalse((Boolean)venice.eval("(== {a: 1 b: 2} {})"));
		assertFalse((Boolean)venice.eval("(== {a: 1 b: 2} nil)"));
	}

	public void test_drop() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (drop 0 '()))"));
		assertEquals("()", venice.eval("(str (drop 1 '()))"));
		
		assertEquals("(1)", venice.eval("(str (drop 0 '(1)))"));
		assertEquals("()", venice.eval("(str (drop 1 '(1)))"));
		assertEquals("()", venice.eval("(str (drop 2 '(1)))"));
		
		assertEquals("(1 2)", venice.eval("(str (drop 0 '(1 2)))"));
		assertEquals("(2)", venice.eval("(str (drop 1 '(1 2)))"));
		assertEquals("()", venice.eval("(str (drop 2 '(1 2)))"));
		assertEquals("()", venice.eval("(str (drop 3 '(1 2)))"));
		
		assertEquals("(1 2 3)", venice.eval("(str (drop 0 '(1 2 3)))"));
		assertEquals("(2 3)", venice.eval("(str (drop 1 '(1 2 3)))"));
		assertEquals("(3)", venice.eval("(str (drop 2 '(1 2 3)))"));
		assertEquals("()", venice.eval("(str (drop 3 '(1 2 3)))"));
		assertEquals("()", venice.eval("(str (drop 4 '(1 2 3)))"));

		
		assertEquals("[]", venice.eval("(str (drop 0 []))"));
		assertEquals("[]", venice.eval("(str (drop 1 []))"));
		
		assertEquals("[1]", venice.eval("(str (drop 0 [1]))"));
		assertEquals("[]", venice.eval("(str (drop 1 [1]))"));
		assertEquals("[1]", venice.eval("(str (drop 2 [1]))"));
		
		assertEquals("[1 2]", venice.eval("(str (drop 0 [1 2]))"));
		assertEquals("[2]", venice.eval("(str (drop 1 [1 2]))"));
		assertEquals("[]", venice.eval("(str (drop 2 [1 2]))"));
		assertEquals("[]", venice.eval("(str (drop 3 [1 2]))"));
		
		assertEquals("[1 2 3]", venice.eval("(str (drop 0 [1 2 3]))"));
		assertEquals("[2 3]", venice.eval("(str (drop 1 [1 2 3]))"));
		assertEquals("[3]", venice.eval("(str (drop 2 [1 2 3]))"));
		assertEquals("[]", venice.eval("(str (drop 3 [1 2 3]))"));
		assertEquals("[]", venice.eval("(str (drop 4 [1 2 3]))"));
	}

	@Test
	public void test_drop_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '()))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("(4 5)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(3 4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(3 2 1 0)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));

		
		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) []))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[4 5]", venice.eval("(str (drop-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[3 4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[3 2 1 0]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}
	
	@Test
	public void test_empty_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(empty? nil)"));	
		assertTrue((Boolean)venice.eval("(empty? \"\")"));	
		assertTrue((Boolean)venice.eval("(empty? '())"));	
		assertTrue((Boolean)venice.eval("(empty? [])"));	
		assertTrue((Boolean)venice.eval("(empty? {})"));	

		assertFalse((Boolean)venice.eval("(empty? \"a\")"));	
		assertFalse((Boolean)venice.eval("(empty? '(1))"));	
		assertFalse((Boolean)venice.eval("(empty? [1])"));	
		assertFalse((Boolean)venice.eval("(empty? {:a 1})"));	
	}
	
	@Test
	public void test_empty_to_nil() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(empty-to-nil \"\")"));	
		assertNull(venice.eval("(empty-to-nil '())"));	
		assertNull(venice.eval("(empty-to-nil [])"));	
		assertNull(venice.eval("(empty-to-nil {})"));	

		assertNotNull(venice.eval("(empty-to-nil \"a\")"));	
		assertNotNull(venice.eval("(empty-to-nil '(1))"));	
		assertNotNull(venice.eval("(empty-to-nil [1])"));	
		assertNotNull(venice.eval("(empty-to-nil {:a 1})"));	
	}
	
	@Test
	public void test_every_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) nil)"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [])"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [:a])"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [:a 1 2])"));	
		assertTrue((Boolean)venice.eval("(every? (fn [x] (number? x)) [1])"));	
		assertTrue((Boolean)venice.eval("(every? (fn [x] (number? x)) [1 2])"));	
	}
	
	@Test
	public void test_false_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(false? nil)"));	
		assertFalse((Boolean)venice.eval("(false? true)"));	
		assertTrue((Boolean)venice.eval("(false? false)"));	
		assertFalse((Boolean)venice.eval("(false? 1)"));	
	}

	@Test
	public void test_flatten() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (flatten '()))"));
		assertEquals("(1)", venice.eval("(str (flatten '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (flatten '(1 2)))"));

		assertEquals("()", venice.eval("(str (flatten '(())))"));
		assertEquals("(1)", venice.eval("(str (flatten '(1 ())))"));
		assertEquals("(1 2)", venice.eval("(str (flatten '(1 2 ())))"));

		assertEquals("(1 2)", venice.eval("(str (flatten '((1 2))))"));
		assertEquals("(1 2 3)", venice.eval("(str (flatten '(1 (2 3))))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (flatten '(1 2 (3 4))))"));

		assertEquals("(1 2 3 4 5 6)", venice.eval("(str (flatten '(1 2 (3 4 (5 6)))))"));

		assertEquals("(:a 1 :b 2)", venice.eval("(str (flatten '({:a 1 :b 2})))"));
		assertEquals("(1 :a 2 :b 3)", venice.eval("(str (flatten '(1 (:a 2 :b 3))))"));
		assertEquals("(1 2 :a 3 :b 4)", venice.eval("(str (flatten '(1 2 (:a 3 :b 4))))"));
		assertEquals("(1 2 :a 3 :b 4 5 6)", venice.eval("(str (flatten '(1 2 (:a 3 :b (4 5 6)))))"));

		
		assertEquals("[]", venice.eval("(str (flatten []))"));
		assertEquals("[1]", venice.eval("(str (flatten [1]))"));
		assertEquals("[1 2]", venice.eval("(str (flatten [1 2]))"));
		
		assertEquals("[]", venice.eval("(str (flatten [[]]))"));
		assertEquals("[1]", venice.eval("(str (flatten [1 []]))"));
		assertEquals("[1 2]", venice.eval("(str (flatten [1 2 []]))"));
		
		assertEquals("[1 2]", venice.eval("(str (flatten [[1 2]]))"));
		assertEquals("[1 2 3]", venice.eval("(str (flatten [1 [2 3]]))"));
		assertEquals("[1 2 3 4]", venice.eval("(str (flatten [1 2 [3 4]]))"));

		assertEquals("[1 2 3 4 5 6]", venice.eval("(str (flatten [1 2 [3 4 [5 6]]]))"));

		assertEquals("[:a 1 :b 2]", venice.eval("(str (flatten [{:a 1 :b 2}]))"));
		assertEquals("[1 :a 2 :b 3]", venice.eval("(str (flatten [1 {:a 2 :b 3}]))"));
		assertEquals("[1 2 :a 3 :b 4 5 6]", venice.eval("(str (flatten [1 2 {:a 3 :b [4 5 6]}]))"));
	}
	
	@Test
	public void test_flush() {
		final Venice venice = new Venice();

		venice.eval("(flush )");
		venice.eval("(flush (. :java.lang.System :out))");
	}
	
	@Test
	public void test_filter() {
		final Venice venice = new Venice();

		assertEquals("(2 4 6 8)", venice.eval("(str (filter even? (range 1 10 1)))"));
	}
	
	@Test
	public void test_find() {
		final Venice venice = new Venice();

		assertEquals("[:b 2]", venice.eval("(str (find {:a 1 :b 2} :b))"));
		assertEquals(null, venice.eval("(find {:a 1 :b 2} :z)"));
	}
	
	@Test
	public void test_first() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(first \"\")"));
		assertEquals("1", venice.eval("(first \"1\")"));
		assertEquals("1", venice.eval("(first \"12\")"));
		assertEquals("1", venice.eval("(first \"123\")"));

		assertNull(venice.eval("(first '())"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1))"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1 2))"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1 2 3))"));

		assertNull(venice.eval("(first [])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1 2])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1 2 3])"));
	}

	@Test
	public void test_fn_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(do (def add5 (fn [x] (+ x 5))) (fn? add5))"));
	}

	@Test
	public void test_gensym() {
		final Venice venice = new Venice();

		assertNotNull(venice.eval("(gensym)"));
		assertNotNull(venice.eval("(gensym :abc)"));
		assertNotNull(venice.eval("(gensym \"abc\")"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym))"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym :abc))"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym \"abc\"))"));
		assertTrue(((String)venice.eval("(str (gensym :abc__))")).startsWith("abc__"));
	}
	
	@Test
	public void test_get() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(2L), venice.eval("(get {:a 1 :b 2} :b)"));
		assertEquals(null, venice.eval("(get {:a 1 :b 2} :z)"));
		assertEquals(null, venice.eval("(get {} :z)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get {:a 1 :b 2} :z 9)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get {} :z 9)"));

		assertEquals(Long.valueOf(2L), venice.eval("(get (ordered-map :a 1 :b 2) :b)"));
		assertEquals(null, venice.eval("(get (ordered-map :a 1 :b 2) :z)"));
		assertEquals(null, venice.eval("(get (ordered-map ) :z)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get (ordered-map :a 1 :b 2) :z 9)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get (ordered-map ) :z 9)"));

		assertEquals(Long.valueOf(2L), venice.eval("(get (sorted-map :a 1 :b 2) :b)"));
		assertEquals(null, venice.eval("(get (sorted-map :a 1 :b 2) :z)"));
		assertEquals(null, venice.eval("(get (sorted-map ) :z)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get (sorted-map :a 1 :b 2) :z 9)"));
		assertEquals(Long.valueOf(9L), venice.eval("(get (sorted-map ) :z 9)"));
	}
	
	@Test
	public void test_get_in() {
		final Venice venice = new Venice();
		
		// map
		assertEquals("", venice.eval("(str (get-in {:a 1} [:b]))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} [:a]))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} [:b :c]))"));

		assertEquals("9", venice.eval("(str (get-in {:a 1} [:b] 9))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} [:a] 9))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} [:b :c] 9))"));

		assertEquals("", venice.eval("(str (get-in {:a 1} '(:b)))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} '(:a)))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} '(:b :c)))"));

		assertEquals("9", venice.eval("(str (get-in {:a 1} '(:b) 9))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} '(:a) 9))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} '(:b :c) 9))"));
		
		// list
		assertEquals("", venice.eval("(str (get-in '() [0]))"));
		assertEquals("1", venice.eval("(str (get-in '(1) [0]))"));
		assertEquals("1", venice.eval("(str (get-in '(1 2) [0]))"));
		assertEquals("2", venice.eval("(str (get-in '(1 2) [1]))"));
		assertEquals("", venice.eval("(str (get-in '(1 2) [2]))"));
		assertEquals("", venice.eval("(str (get-in '(1 2) [0 0]))"));
		assertEquals("1", venice.eval("(str (get-in '((1) 2) [0 0]))"));
		assertEquals("2", venice.eval("(str (get-in '((1 2) 2) [0 1]))"));
		assertEquals("", venice.eval("(str (get-in '((1 2) 2) [0 2]))"));

		assertEquals("9", venice.eval("(str (get-in '() [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '(1) [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '(1 2) [0] 9))"));
		assertEquals("2", venice.eval("(str (get-in '(1 2) [1] 9))"));
		assertEquals("9", venice.eval("(str (get-in '(1 2) [2] 9))"));
		assertEquals("9", venice.eval("(str (get-in '(1 2) [0 0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '((1) 2) [0 0] 9))"));
		assertEquals("2", venice.eval("(str (get-in '((1 2) 2) [0 1] 9))"));
		assertEquals("9", venice.eval("(str (get-in '((1 2) 2) [0 2] 9))"));
		
		// vector
		assertEquals("", venice.eval("(str (get-in [] [0]))"));
		assertEquals("1", venice.eval("(str (get-in [1] [0]))"));
		assertEquals("1", venice.eval("(str (get-in [1 2] [0]))"));
		assertEquals("2", venice.eval("(str (get-in [1 2] [1]))"));
		assertEquals("", venice.eval("(str (get-in [1 2] [2]))"));
		assertEquals("", venice.eval("(str (get-in [1 2] [0 0]))"));
		assertEquals("1", venice.eval("(str (get-in ['(1) 2] [0 0]))"));
		assertEquals("2", venice.eval("(str (get-in ['(1 2) 2] [0 1]))"));
		assertEquals("", venice.eval("(str (get-in ['(1 2) 2] [0 2]))"));

		assertEquals("9", venice.eval("(str (get-in [] [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in [1] [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in [1 2] [0] 9))"));
		assertEquals("2", venice.eval("(str (get-in [1 2] [1] 9))"));
		assertEquals("9", venice.eval("(str (get-in [1 2] [2] 9))"));
		assertEquals("9", venice.eval("(str (get-in [1 2] [0 0] 9))"));
		assertEquals("1", venice.eval("(str (get-in ['(1) 2] [0 0] 9))"));
		assertEquals("2", venice.eval("(str (get-in ['(1 2) 2] [0 1] 9))"));
		assertEquals("9", venice.eval("(str (get-in ['(1 2) 2] [0 2] 9))"));
		
		// map / vector
		assertEquals(":y", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 1]))"));
		assertEquals("", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 5]))"));
		assertEquals("9", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 5] 9))"));
		
		// vector / map
		assertEquals("1", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :c]))"));
		assertEquals("", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :x]))"));
		assertEquals("9", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :x] 9))"));
	}

	@Test
	public void test_gt() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(> 2 3)"));
		assertFalse((Boolean)venice.eval("(> 2 2)"));
		assertTrue((Boolean)venice.eval("(> 2 1)"));
		assertFalse((Boolean)venice.eval("(> 2 3.0)"));
		assertFalse((Boolean)venice.eval("(> 2 2.0)"));
		assertTrue((Boolean)venice.eval("(> 2 1.0)"));
		assertFalse((Boolean)venice.eval("(> 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2 1.0M)"));

		// Double
		assertFalse((Boolean)venice.eval("(> 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1.0)"));
		assertFalse((Boolean)venice.eval("(> 2.0 3)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1)"));
		assertFalse((Boolean)venice.eval("(> 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1.0M)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(> 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0M 3))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2))"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 3.0))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2.0))"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1.0))"));

		// String
		assertFalse((Boolean)venice.eval("(> \"k\" \"z\")"));
		assertFalse((Boolean)venice.eval("(> \"k\" \"k\")"));
		assertTrue((Boolean)venice.eval("(> \"k\" \"a\")"));
	}

	@Test
	public void test_gte() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(>= 2 3)"));
		assertTrue((Boolean)venice.eval("(>= 2 2)"));
		assertTrue((Boolean)venice.eval("(>= 2 1)"));
		assertFalse((Boolean)venice.eval("(>= 2 3.0)"));
		assertTrue((Boolean)venice.eval("(>= 2 2.0)"));
		assertTrue((Boolean)venice.eval("(>= 2 1.0)"));
		assertFalse((Boolean)venice.eval("(>= 2 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2 1.0M)"));

		// Double
		assertFalse((Boolean)venice.eval("(>= 2.0 3.0)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1.0)"));
		assertFalse((Boolean)venice.eval("(>= 2.0 3)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1)"));
		assertFalse((Boolean)venice.eval("(>= 2.0 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1.0M)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(>= 2.0M 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1.0M)"));
		assertFalse((Boolean)venice.eval("(>= 2.0M 3))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1))"));
		assertFalse((Boolean)venice.eval("(>= 2.0M 3.0))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2.0))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1.0))"));

		// String
		assertFalse((Boolean)venice.eval("(>= \"k\" \"z\")"));
		assertTrue((Boolean)venice.eval("(>= \"k\" \"k\")"));
		assertTrue((Boolean)venice.eval("(>= \"k\" \"a\")"));
	}

	@Test
	public void test_group_by() {
		final Venice venice = new Venice();
		
		assertEquals("{1 [a] 2 [as aa] 3 [asd] 4 [asdf qwer]}", venice.eval("(str (group-by count [\"a\" \"as\" \"asd\" \"aa\" \"asdf\" \"qwer\"]))"));

		assertEquals("{false [0 2 4 6 8] true [1 3 5 7 9]}", venice.eval("(str (group-by odd? (range 0 10)))"));
	}
	
	@Test
	public void test_hash_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str {})"));
		assertEquals("{:a 1}", venice.eval("(str {:a 1})"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str {:a 1 :b 2})"));

		assertEquals("{}", venice.eval("(str (hash-map))"));
		assertEquals("{:a 1}", venice.eval("(str (hash-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (hash-map :a 1 :b 2))"));
	}
	
	@Test
	public void test_hash_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(hash-map? nil)"));	
		assertFalse((Boolean)venice.eval("(hash-map? 1)"));	
		assertFalse((Boolean)venice.eval("(hash-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? [1 2])"));	
		assertTrue((Boolean)venice.eval("(hash-map? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(hash-map? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (set 1 2))"));	
	}

	@Test
	public void test_identity() {
		final Venice venice = new Venice();

		assertEquals("4", venice.eval("(str (identity 4))"));
		assertEquals("[1 2 3 4 true 1234]", venice.eval("(str (filter identity [1 2 3 nil 4 false true 1234]))"));		
	}	
	
	@Test
	public void test_interleave() {
		final Venice venice = new Venice();
		
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b :c] [1 2]))"));
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b] [1 2 3]))"));
	}
	
	@Test
	public void test_interpose() {
		final Venice venice = new Venice();
		
		assertEquals("(1 - 2 - 3)", venice.eval("(str (interpose \"-\" [1 2 3]))"));
		assertEquals("1-2-3", venice.eval("(apply str (interpose \"-\" [1 2 3]))"));
	}

	@Test
	public void test_intersection() {
		final Venice venice = new Venice();

		assertEquals("#{1}", venice.eval("(str (intersection (set 1)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (set 2 3) (set 2 4)))"));
		assertEquals("#{}", venice.eval("(str (intersection (set 1 2) (set 3 4)))"));
	}
		
	@Test
	public void test_into() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (into '() '()))"));
		assertEquals("(1)", venice.eval("(str (into '() '(1)))"));
		assertEquals("(2 1)", venice.eval("(str (into '() '(1 2)))"));

		assertEquals("()", venice.eval("(str (into '() []))"));
		assertEquals("(1)", venice.eval("(str (into '() [1]))"));
		assertEquals("(2 1)", venice.eval("(str (into '() [1 2]))"));

		assertEquals("[]", venice.eval("(str (into [] '()))"));
		assertEquals("[1]", venice.eval("(str (into [] '(1)))"));
		assertEquals("[1 2]", venice.eval("(str (into [] '(1 2)))"));

		assertEquals("[]", venice.eval("(str (into [] []))"));
		assertEquals("[1]", venice.eval("(str (into [] [1]))"));
		assertEquals("[1 2]", venice.eval("(str (into [] [1 2]))"));

		
		assertEquals("(0)", venice.eval("(str (into '(0) '()))"));
		assertEquals("(1 0)", venice.eval("(str (into '(0) '(1)))"));
		assertEquals("(2 1 0)", venice.eval("(str (into '(0) '(1 2)))"));

		assertEquals("(0)", venice.eval("(str (into '(0) []))"));
		assertEquals("(1 0)", venice.eval("(str (into '(0) [1]))"));
		assertEquals("(2 1 0)", venice.eval("(str (into '(0) [1 2]))"));
		assertEquals("(3 2 1)", venice.eval("(str (into '() '(1 2 3)))"));
		assertEquals("([3 4] [1 2])", venice.eval("(str (into '() {1 2, 3 4}))"));
		assertEquals("(6 5 4 1 2 3)", venice.eval("(str (into '(1 2 3) '(4 5 6)))"));
		assertEquals("(6 5 4 1 2 3)", venice.eval("(str (into '(1 2 3) [4 5 6]))"));

		assertEquals("(a b c)", venice.eval("(str (into '() \"abc\"))"));
		
				
		assertEquals("[0]", venice.eval("(str (into [0] '()))"));
		assertEquals("[0 1]", venice.eval("(str (into [0] '(1)))"));
		assertEquals("[0 1 2]", venice.eval("(str (into [0] '(1 2)))"));

		assertEquals("[0]", venice.eval("(str (into [0] []))"));
		assertEquals("[0 1]", venice.eval("(str (into [0] [1]))"));
		assertEquals("[0 1 2]", venice.eval("(str (into [0] [1 2]))"));
		assertEquals("[[1 2] [3 4]]", venice.eval("(str (into [] {1 2, 3 4}))"));
		assertEquals("[1 2 3 4 5 6]", venice.eval("(str (into [1 2 3] '(4 5 6)))"));
		assertEquals("[1 2 3 4 5 6]", venice.eval("(str (into [1 2 3] [4 5 6]))"));
		assertEquals("[a b c]", venice.eval("(str (into [] \"abc\"))"));
		
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) [[:a 1] [:b 2] [:c 3]] ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) [{:a 1} {:b 2} {:c 3}] ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) { :a 1 :b 2 :c 3} ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) (ordered-map :a 1 :b 2 :c 3) ))"));
	}
	
	@Test
	public void test_key() {
		final Venice venice = new Venice();

		assertEquals(":b", venice.eval("(str (key (find {:a 1 :b 2} :b)))"));
	}
	
	@Test
	public void test_keys() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (keys {})))"));
		assertEquals("(:a :b :c)", venice.eval("(str (sort (keys {:a 1 :b 2 :c 3})))"));

		assertEquals("()", venice.eval("(str (sort (keys (ordered-map ))))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (ordered-map :a 1 :b 2 :c 3)))"));

		assertEquals("()", venice.eval("(str (sort (keys (sorted-map ))))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (sorted-map :a 1 :b 2 :c 3)))"));
	}
	
	@Test
	public void test_keyword() {
		final Venice venice = new Venice();

		assertEquals(":a", venice.eval("(str (keyword :a))"));
		assertEquals(":a", venice.eval("(str (keyword \"a\"))"));
		assertTrue((Boolean)venice.eval("(keyword? (keyword :a))"));	
		assertTrue((Boolean)venice.eval("(keyword? (keyword \"a\"))"));	
		
		// keywords act like functions on maps
		assertEquals(Long.valueOf(100), venice.eval("(:a {:a 100 :b 200})"));
	}
	
	@Test
	public void test_list() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (list))"));
		assertEquals("(1 2 3)", venice.eval("(str (list 1 2 3))"));
		assertEquals("(())", venice.eval("(str (list '()))"));
		assertEquals("([])", venice.eval("(str (list []))"));
		assertEquals("((1 2))", venice.eval("(str (list '(1 2)))"));
		assertEquals("((1 2) 3 4)", venice.eval("(str (list '(1 2) 3 4))"));
	}
	
	@Test
	public void test_keyword_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(keyword? nil)"));	
		assertFalse((Boolean)venice.eval("(keyword? true)"));	
		assertFalse((Boolean)venice.eval("(keyword? 1)"));	
		assertFalse((Boolean)venice.eval("(keyword? -3.0)"));
		assertFalse((Boolean)venice.eval("(keyword? -3.0M)"));
		assertFalse((Boolean)venice.eval("(keyword? \"ABC\")"));
		assertTrue((Boolean)venice.eval("(keyword? :a)"));
		assertFalse((Boolean)venice.eval("(keyword? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(keyword? '())"));
		assertFalse((Boolean)venice.eval("(keyword? [])"));
		assertFalse((Boolean)venice.eval("(keyword? {})"));
	}
	
	@Test
	public void test_last() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(last \"\")"));
		assertEquals("1", venice.eval("(last \"1\")"));
		assertEquals("2", venice.eval("(last \"12\")"));
		assertEquals("3", venice.eval("(last \"123\")"));

		assertEquals(null, venice.eval("(last '())"));
		assertEquals(Long.valueOf(1), venice.eval("(last '(1))"));
		assertEquals(Long.valueOf(2), venice.eval("(last '(1 2))"));
		assertEquals(Long.valueOf(3), venice.eval("(last '(1 2 3))"));

		assertEquals(null, venice.eval("(last [])"));
		assertEquals(Long.valueOf(1), venice.eval("(last [1])"));
		assertEquals(Long.valueOf(2), venice.eval("(last [1 2])"));
		assertEquals(Long.valueOf(3), venice.eval("(last [1 2 3])"));
	}
	
	@Test
	public void test_ordered_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(ordered-map? nil)"));	
		assertFalse((Boolean)venice.eval("(ordered-map? 1)"));	
		assertFalse((Boolean)venice.eval("(ordered-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? [1 2])"));	
		assertFalse((Boolean)venice.eval("(ordered-map? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(ordered-map? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (set 1 2))"));	
	}
	
	@Test
	public void test_ordered_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (ordered-map))"));
		assertEquals("{:a 1}", venice.eval("(str (ordered-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (ordered-map :a 1 :b 2))"));
	}
	
	@Test
	public void test_list_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(list? nil)"));	
		assertTrue((Boolean)venice.eval("(list? '())"));	
		assertFalse((Boolean)venice.eval("(list? [])"));	
		assertFalse((Boolean)venice.eval("(list? 1)"));	
	}

	@Test
	public void test_load_file() {
		final Venice venice = new Venice();
		
		try {
			final File file = File.createTempFile("load", ".txt");
			try {				
				venice.eval(
						"(io/spit file \"(def x 1) (+ x 2)\")", 
						Parameters.of("file", file.getAbsolutePath()));
	
				assertEquals(
						Long.valueOf(3), 
						venice.eval(
								"(load-file file)", 
								Parameters.of("file", file.getAbsolutePath())));					
			}
			finally {
				file.delete();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void test_load_string1() {
		final Venice venice = new Venice();

		final String script =
				"(load-string \"   " +
				"   (def x 1)      " + 
				"   (+ x 2) \")    ";

		assertEquals(Long.valueOf(3), venice.eval(script));
	}

	@Test
	public void test_load_string2() {
		final Venice venice = new Venice();

		final String script =
				"(do                               " +
				"   (load-string \" (def x 1) \")  " + 
				"   (+ x 2))                       ";

		assertEquals(Long.valueOf(3), venice.eval(script));
	}

	@Test
	public void test_long() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0), venice.eval("(long nil)"));
		assertEquals(Long.valueOf(0), venice.eval("(long false)"));
		assertEquals(Long.valueOf(1), venice.eval("(long true)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3.1)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3.0M)"));
		assertEquals(Long.valueOf(3), venice.eval("(long \"3\")"));
	}

	@Test
	public void test_long_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(long? nil)"));	
		assertFalse((Boolean)venice.eval("(long? true)"));	
		assertTrue((Boolean)venice.eval("(long? 1)"));	
		assertFalse((Boolean)venice.eval("(long? -3.0)"));
		assertFalse((Boolean)venice.eval("(long? -3.0M)"));
		assertFalse((Boolean)venice.eval("(long? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(long? :a)"));
		assertFalse((Boolean)venice.eval("(long? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(long? '())"));
		assertFalse((Boolean)venice.eval("(long? [])"));
		assertFalse((Boolean)venice.eval("(long? {})"));
	}

	@Test
	public void test_lt() {
		final Venice venice = new Venice();

		// Long
		assertTrue((Boolean)venice.eval("(< 2 3)"));
		assertFalse((Boolean)venice.eval("(< 2 2)"));
		assertFalse((Boolean)venice.eval("(< 2 1)"));
		assertTrue((Boolean)venice.eval("(< 2 3.0)"));
		assertFalse((Boolean)venice.eval("(< 2 2.0)"));
		assertFalse((Boolean)venice.eval("(< 2 1.0)"));
		assertTrue((Boolean)venice.eval("(< 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2 1.0M)"));

		// Double
		assertTrue((Boolean)venice.eval("(< 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1.0)"));
		assertTrue((Boolean)venice.eval("(< 2.0 3)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1)"));
		assertTrue((Boolean)venice.eval("(< 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1.0M)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(< 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1.0M)"));
		assertTrue((Boolean)venice.eval("(< 2.0M 3))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1))"));
		assertTrue((Boolean)venice.eval("(< 2.0M 3.0))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2.0))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1.0))"));

		// String
		assertTrue((Boolean)venice.eval("(< \"k\" \"z\")"));
		assertFalse((Boolean)venice.eval("(< \"k\" \"k\")"));
		assertFalse((Boolean)venice.eval("(< \"k\" \"a\")"));
	}

	@Test
	public void test_lte() {
		final Venice venice = new Venice();

		// Long
		assertTrue((Boolean)venice.eval("(<= 2 3)"));
		assertTrue((Boolean)venice.eval("(<= 2 2)"));
		assertFalse((Boolean)venice.eval("(<= 2 1)"));
		assertTrue((Boolean)venice.eval("(<= 2 3.0)"));
		assertTrue((Boolean)venice.eval("(<= 2 2.0)"));
		assertFalse((Boolean)venice.eval("(<= 2 1.0)"));
		assertTrue((Boolean)venice.eval("(<= 2 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2 1.0M)"));

		// Double
		assertTrue((Boolean)venice.eval("(<= 2.0 3.0)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1.0)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 3)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1.0M)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(<= 2.0M 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 3))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2))"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 3.0))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2.0))"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1.0))"));

		// String
		assertTrue((Boolean)venice.eval("(<= \"k\" \"z\")"));
		assertTrue((Boolean)venice.eval("(<= \"k\" \"k\")"));
		assertFalse((Boolean)venice.eval("(<= \"k\" \"a\")"));
	}

	@Test
	public void test_macro_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(do (defmacro _not [x] `(if ~x false true)) (macro? _not))"));
	}
	
	@Test
	public void test_map() {
		final Venice venice = new Venice();

		assertEquals("(2 3 4 5 6)", venice.eval("(str (map inc '(1 2 3 4 5)))"));

		assertEquals("(2 3 4 5 6)", venice.eval("(str (map inc [1 2 3 4 5]))"));

		assertEquals("(5 7 9)", venice.eval("(str (map + [1 2 3] [4 5 6]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3] [4 5 6] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3 9 9] [4 5 6 9] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3] [4 5 6 9] [7 8 9]))"));

		assertEquals("(1 3)", venice.eval("(str (map (fn [x] (get x :a)) [{:a 1 :b 2} {:a 3 :b 4}]))"));
		
		assertEquals("(true false true)", venice.eval("(str (map not [false, true, false]))"));
		
		assertEquals("((1 1) (2 2) (3 3))", venice.eval("(str (map list [1 2 3] [1 2 3]))"));	
	}	

	@Test
	public void test_mapcat() {
		final Venice venice = new Venice();

		// mapcat built with concat and map
		assertEquals(
				"(aa bb cc dd ee ff)", 
				venice.eval("(str (apply concat (map (fn [x] (str/split x \"[0-9]+\")) [\"aa1bb\" \"cc2dd\" \"ee3ff\"])))"));

		
		// mapcat
		assertEquals(
				"(aa bb cc dd ee ff)", 
				venice.eval("(str (mapcat (fn [x] (str/split x \"[0-9]+\")) [\"aa1bb\" \"cc2dd\" \"ee3ff\"]))"));

		assertEquals(
				"(0 1 2 3 4 5 6 7 8 9)",
				venice.eval("(str (mapcat reverse [[3 2 1 0] [6 5 4] [9 8 7]]))"));
	}
	
	@Test
	public void test_mapv() {
		final Venice venice = new Venice();

		assertEquals("[2 3 4 5 6]", venice.eval("(str (mapv inc '(1 2 3 4 5)))"));

		assertEquals("[2 3 4 5 6]", venice.eval("(str (mapv inc [1 2 3 4 5]))"));

		assertEquals("[5 7 9]", venice.eval("(str (mapv + [1 2 3] [4 5 6]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3] [4 5 6] [7 8 9]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3 9 9] [4 5 6 9] [7 8 9]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3] [4 5 6 9] [7 8 9 10]))"));

		assertEquals("[1 3]", venice.eval("(str (mapv (fn [x] (get x :a)) [{:a 1 :b 2} {:a 3 :b 4}]))"));
		
		assertEquals("[true false true]", venice.eval("(str (mapv not [false, true, false]))"));
		
		assertEquals("[(1 1) (2 2) (3 3)]", venice.eval("(str (mapv list [1 2 3] [1 2 3]))"));	
		
		assertEquals("[4 5 6]", venice.eval("(str (mapv (fn [x] (get {14 4 15 5 16 6 } x 0.0)) [14 15 16]))"));	
	}	
		
	@Test
	public void test_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(map? nil)"));	
		assertFalse((Boolean)venice.eval("(map? 1)"));	
		assertFalse((Boolean)venice.eval("(map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(map? [1 2])"));	
		assertTrue((Boolean)venice.eval("(map? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(map? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(map? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(map? (set 1 2))"));	
	}

	@Test
	public void test_match() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(match \"123\" \"[0-9]+\")"));
		assertFalse((Boolean)venice.eval("(match \"123a\" \"[0-9]+\")"));
	}

	@Test
	public void test_match_not() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(match-not \"123\" \"[0-9]+\")"));
		assertTrue((Boolean)venice.eval("(match-not \"123a\" \"[0-9]+\")"));
	}

	@Test
	public void test_memoize() {
		final Venice venice = new Venice();


		final String script1 = 
				"(do                                  " +
				"   (def counter (atom 0))            " +
				"                                     " +
				"   (def test                         " +
				"        (fn [a]                      " +
				"            (do                      " +
				"               (swap! counter inc)   " +
				"               (+ a 100))))          " +
				"                                     " +
				"   (def test-memo (memoize test))    " +
				"                                     " +
				"   [ (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 2)                   " +
				"     (test-memo 2)                   " +
				"     (deref counter) ]               " +
				")                                    ";

		final String script2 = 
				"(do                                  " +
				"   (def counter (atom 0))            " +
				"                                     " +
				"   (def test-memo                    " +
				"        (memoize                     " +
				"           (fn [a]                   " +
				"              (do                    " +
				"                 (swap! counter inc) " +
				"                 (+ a 100)))))       " +
				"                                     " +
				"   [ (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 2)                   " +
				"     (test-memo 2)                   " +
				"     (deref counter) ]               " +
				")                                    ";

		assertEquals("[101 101 101 102 102 2]", venice.eval("(str " + script1 + ")"));
		assertEquals("[101 101 101 102 102 2]", venice.eval("(str " + script2 + ")"));
	}

	@Test
	public void test_merge() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 9 :c 3 :d 4}", venice.eval("(str (sorted-map (merge {:a 1 :b 2 :c 3} {:b 9 :d 4})))"));
		assertEquals("{:a 1}", venice.eval("(str (merge {:a 1} nil))"));
		assertEquals("{:a 1}", venice.eval("(str (merge nil {:a 1}))"));
		assertEquals(null, venice.eval("(merge nil nil)"));
	}

	@Test
	public void test_meta() {
		final Venice venice = new Venice();

		assertNotNull(venice.eval("(meta 3)"));
		assertNotNull(venice.eval("(meta '(1 2 3))"));
		assertNotNull(venice.eval("(meta [1 2 3])"));
		assertNotNull(venice.eval("(meta { :a 1 })"));
		
		assertEquals("{:a 1}", venice.eval("(str (meta (with-meta [1 2 3] { :a 1 })))"));
	}
	
	@Test
	public void test_nfirst() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (nfirst nil 1))"));
		assertEquals("()", venice.eval("(str (nfirst '() 1))"));
		assertEquals("()", venice.eval("(str (nfirst '(1 2 3) 0))"));
		assertEquals("(1)", venice.eval("(str (nfirst '(1 2 3) 1))"));
		assertEquals("(1 2)", venice.eval("(str (nfirst '(1 2 3) 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (nfirst '(1 2 3) 3))"));
		assertEquals("(1 2 3)", venice.eval("(str (nfirst '(1 2 3) 4))"));

		assertEquals("[]", venice.eval("(str (nfirst [] 1))"));
		assertEquals("[]", venice.eval("(str (nfirst [1 2 3] 0))"));
		assertEquals("[1]", venice.eval("(str (nfirst [1 2 3] 1))"));
		assertEquals("[1 2]", venice.eval("(str (nfirst [1 2 3] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (nfirst [1 2 3] 3))"));
		assertEquals("[1 2 3]", venice.eval("(str (nfirst [1 2 3] 4))"));
	}

	@Test
	public void test_nil_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(nil? nil)"));	
		assertFalse((Boolean)venice.eval("(nil? true)"));	
		assertFalse((Boolean)venice.eval("(nil? false)"));	
		assertFalse((Boolean)venice.eval("(nil? 1)"));	
	}
	
	@Test
	public void test_nlast() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (nlast nil 1))"));
		assertEquals("()", venice.eval("(str (nlast '() 1))"));
		assertEquals("()", venice.eval("(str (nlast '(1 2 3) 0))"));
		assertEquals("(3)", venice.eval("(str (nlast '(1 2 3) 1))"));
		assertEquals("(2 3)", venice.eval("(str (nlast '(1 2 3) 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (nlast '(1 2 3) 3))"));
		assertEquals("(1 2 3)", venice.eval("(str (nlast '(1 2 3) 4))"));

		assertEquals("[]", venice.eval("(str (nlast [] 1))"));
		assertEquals("[]", venice.eval("(str (nlast [1 2 3] 0))"));
		assertEquals("[3]", venice.eval("(str (nlast [1 2 3] 1))"));
		assertEquals("[2 3]", venice.eval("(str (nlast [1 2 3] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (nlast [1 2 3] 3))"));
		assertEquals("[1 2 3]", venice.eval("(str (nlast [1 2 3] 4))"));
	}

	@Test
	public void test_not_equals_Q() {
		final Venice venice = new Venice();
		
		// Nil
		assertFalse((Boolean)venice.eval("(!= nil nil)"));
		
		// Boolean
		assertFalse((Boolean)venice.eval("(!= true true)"));
		assertTrue((Boolean)venice.eval("(!= true false)"));

		// Long
		assertFalse((Boolean)venice.eval("(!= 2 2)"));
		assertTrue((Boolean)venice.eval("(!= 2 3)"));

		// Double
		assertFalse((Boolean)venice.eval("(!= 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(!= 2.0 3.0)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(!= 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(!= 2.0M 3.0M)"));

		// String
		assertFalse((Boolean)venice.eval("(!= \"aa\" \"aa\")"));
		assertTrue((Boolean)venice.eval("(!= \"aa\" \"zz\")"));

		// Keyword
		assertFalse((Boolean)venice.eval("(!= :a :a)"));
		assertTrue((Boolean)venice.eval("(!= :a :b)"));

		// List
		assertFalse((Boolean)venice.eval("(!= '(1 2) '(1 2))"));
		assertTrue((Boolean)venice.eval("(!= '(1 2) '(1 4))"));
		assertTrue((Boolean)venice.eval("(!= '(1 2) '(1))"));
		assertTrue((Boolean)venice.eval("(!= '(1 2) '())"));
		assertTrue((Boolean)venice.eval("(!= '(1 2) nil)"));

		// Vector
		assertFalse((Boolean)venice.eval("(!= [1 2] [1 2])"));
		assertTrue((Boolean)venice.eval("(!= [1 2] [1 4])"));
		assertTrue((Boolean)venice.eval("(!= [1 2] [1])"));
		assertTrue((Boolean)venice.eval("(!= [1 2] [])"));
		assertTrue((Boolean)venice.eval("(!= [1 2] nil)"));

		// Map
		assertFalse((Boolean)venice.eval("(!= {a: 1 b: 2} {a: 1 b: 2})"));
		assertTrue((Boolean)venice.eval("(!= {a: 1 b: 2} {a: 1 b: 3})"));
		assertTrue((Boolean)venice.eval("(!= {a: 1 b: 2} {a: 1 c: 2})"));
		assertTrue((Boolean)venice.eval("(!= {a: 1 b: 2} {a: 1})"));
		assertTrue((Boolean)venice.eval("(!= {a: 1 b: 2} {})"));
		assertTrue((Boolean)venice.eval("(!= {a: 1 b: 2} nil)"));
	}
	
	@Test
	public void test_nth() {
		final Venice venice = new Venice();

		assertEquals("1", venice.eval("(nth \"1\" 0)"));
		assertEquals("2", venice.eval("(nth \"12\" 1 )"));
		assertEquals("3", venice.eval("(nth \"123\" 2)"));

		assertEquals(Long.valueOf(1), venice.eval("(nth '(1) 0)"));
		assertEquals(Long.valueOf(2), venice.eval("(nth '(1 2) 1)"));
		assertEquals(Long.valueOf(3), venice.eval("(nth '(1 2 3) 2)"));
		
		assertEquals(Long.valueOf(1), venice.eval("(nth [1] 0)"));
		assertEquals(Long.valueOf(2), venice.eval("(nth [1 2] 1)"));
		assertEquals(Long.valueOf(3), venice.eval("(nth [1 2 3] 2)"));
	}

	@Test
	public void test_number_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(number? nil)"));	
		assertFalse((Boolean)venice.eval("(number? true)"));	
		assertTrue((Boolean)venice.eval("(number? 1)"));	
		assertTrue((Boolean)venice.eval("(number? -3.0)"));
		assertTrue((Boolean)venice.eval("(number? -3.0M)"));
		assertFalse((Boolean)venice.eval("(number? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(number? :a)"));
		assertFalse((Boolean)venice.eval("(number? '())"));
		assertFalse((Boolean)venice.eval("(number? [])"));
		assertFalse((Boolean)venice.eval("(number? {})"));
	}
	
	@Test
	public void test_partial() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(6), venice.eval("((partial * 2) 3)"));
		
		assertEquals("[2, 4, 6, 8]", venice.eval("(map (partial * 2) [1 2 3 4])").toString());
		
		assertEquals("500", venice.eval(
								"(do " +
								"   (def hundred-times (partial * 100)) " +
								"   (str (hundred-times 5)))"));
	}

	@Test
	public void test_partition() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (partition 3 '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 '(1 2 3)))"));
		assertEquals("((1 2 3) (4))", venice.eval("(str (partition 3 '(1 2 3 4)))"));
		assertEquals("((1 2 3) (4 5))", venice.eval("(str (partition 3 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6) (7))", venice.eval("(str (partition 3 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 2 3 '()))"));
		assertEquals("((1))", venice.eval("(str (partition 2 3 '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 2 3 '(1 2)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 2 3 '(1 2 3)))"));
		assertEquals("((1 2) (4))", venice.eval("(str (partition 2 3 '(1 2 3 4)))"));
		assertEquals("((1 2) (4 5))", venice.eval("(str (partition 2 3 '(1 2 3 4 5)))"));
		assertEquals("((1 2) (4 5))", venice.eval("(str (partition 2 3 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2) (4 5) (7))", venice.eval("(str (partition 2 3 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 4 3 '()))"));
		assertEquals("((1))", venice.eval("(str (partition 4 3 '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 4 3 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 4 3 '(1 2 3)))"));
		assertEquals("((1 2 3) (4))", venice.eval("(str (partition 4 3 '(1 2 3 4)))"));
		assertEquals("((1 2 3) (4 5))", venice.eval("(str (partition 4 3 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 4 3 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6) (7))", venice.eval("(str (partition 4 3 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 4 3 [:a :b :c] '()))"));
		assertEquals("((1 :a :b :c))", venice.eval("(str (partition 4 3 [:a :b :c] '(1)))"));
		assertEquals("((1 2 :a :b))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2)))"));
		assertEquals("((1 2 3 :a))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2 3)))"));
		assertEquals("((1 2 3 :a) (4 :a :b :c))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2 3 4)))"));
		assertEquals("((1 2 3 :a) (4 5 :a :b))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 :a) (4 5 6 :a))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 :a) (4 5 6 :a) (7 :a :b :c))", venice.eval("(str (partition 4 3 [:a :b :c] '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 5 3 [:a :b :c] '()))"));
		assertEquals("((1 :a :b :c))", venice.eval("(str (partition 5 3 [:a :b :c] '(1)))"));
		assertEquals("((1 2 :a :b :c))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2)))"));
		assertEquals("((1 2 3 :a :b))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2 3)))"));
		assertEquals("((1 2 3 :a :b) (4 :a :b :c))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2 3 4)))"));
		assertEquals("((1 2 3 :a :b) (4 5 :a :b :c))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 :a :b) (4 5 6 :a :b))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 :a :b) (4 5 6 :a :b) (7 :a :b :c))", venice.eval("(str (partition 5 3 [:a :b :c] '(1 2 3 4 5 6 7)))"));
	}
	
	@Test
	public void test_peek() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(peek nil)"));
		assertEquals(null, venice.eval("(peek '())"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1))"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1 2))"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1 2 3))"));

		assertEquals(null, venice.eval("(peek nil)"));
		assertEquals(null, venice.eval("(peek [])"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek [1])"));
		assertEquals(Long.valueOf(2L), venice.eval("(peek [1 2])"));
		assertEquals(Long.valueOf(3L), venice.eval("(peek [1 2 3])"));
	}
	
	@Test
	public void test_pop() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (pop '()))"));
		assertEquals("()", venice.eval("(str (pop '(1)))"));
		assertEquals("(2)", venice.eval("(str (pop '(1 2)))"));
		assertEquals("(2 3)", venice.eval("(str (pop '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (pop []))"));
		assertEquals("[]", venice.eval("(str (pop [1]))"));
		assertEquals("[1]", venice.eval("(str (pop [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (pop [1 2 3]))"));
	}

	@Test
	public void test_pos() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(pos? -3)"));
		assertFalse((Boolean)venice.eval("(pos? 0)"));
		assertTrue((Boolean)venice.eval("(pos? 3)"));
	
		// Double
		assertFalse((Boolean)venice.eval("(pos? -3.0)"));
		assertFalse((Boolean)venice.eval("(pos? 0.0)"));
		assertTrue((Boolean)venice.eval("(pos? 3.0)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(pos? -3.0M)"));
		assertFalse((Boolean)venice.eval("(pos? 0.0M)"));
		assertTrue((Boolean)venice.eval("(pos? 3.0M)"));
	}
	
	@Test
	public void test_pr_str() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(pr-str )"));	
		assertEquals("nil", venice.eval("(pr-str nil)"));	
		assertEquals("\"abc\"", venice.eval("(pr-str \"abc\")"));
		assertEquals("\"abc\" 1 2 \"def\" nil true", venice.eval("(pr-str \"abc\" 1 2 \"def\" nil true)"));
		assertEquals("true", venice.eval("(pr-str true)"));	
		assertEquals("1", venice.eval("(pr-str 1)"));	
		assertEquals("-3.0", venice.eval("(pr-str -3.0)"));
		assertEquals("-3.0123M", venice.eval("(pr-str -3.0123M)"));
		assertEquals(":a", venice.eval("(pr-str :a)"));
		assertEquals("a", venice.eval("(pr-str (symbol :a))"));
		assertEquals("(1 2)", venice.eval("(pr-str '(1 2))"));
		assertEquals("[1 2]", venice.eval("(pr-str [1 2])"));
		assertEquals("{:a 1 :b 2}", venice.eval("(pr-str (ordered-map :a 1 :b 2))"));
		assertEquals("{:a \"1\" :b \"2\"}", venice.eval("(pr-str (ordered-map :a \"1\" :b \"2\"))"));
	}
	
	@Test
	public void test_reduce() {
		final Venice venice = new Venice();

		// ((100 + 3) * 2) + 1 => 207
		assertEquals(Long.valueOf(207), venice.eval("((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)"));
		
		assertEquals(Long.valueOf(15), venice.eval("(reduce + [1 2 3 4 5])"));
		assertEquals(Long.valueOf(0),  venice.eval("(reduce + [])"));
		assertEquals(Long.valueOf(1),  venice.eval("(reduce + [1])"));
		assertEquals(Long.valueOf(3),  venice.eval("(reduce + [1 2])"));
		assertEquals(Long.valueOf(3),  venice.eval("(reduce + 3 [])"));
		assertEquals(Long.valueOf(9),  venice.eval("(reduce + 4 [2 3])"));
	}

	@Test
	public void test_reduce_kv() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (reduce-kv (fn [m k v] (assoc m k v)) (sorted-map ) {:a 1 :b 2 :c 3}))"));
		
		assertEquals("{:a 2 :b 3 :c 4}", venice.eval("(str (reduce-kv (fn [m k v] (assoc m k (inc v))) (sorted-map ) {:a 1 :b 2 :c 3}))"));
	}
	
	@Test
	public void test_remove() {
		final Venice venice = new Venice();

		assertEquals("(1 3 5 7 9)", venice.eval("(str (remove even? (range 1 10 1)))"));
	}

	@Test
	public void test_repeat() {
		final Venice venice = new Venice();

		// Long
		assertEquals("()", venice.eval("(str (repeat 0 1))"));
		assertEquals("(1 1 1 1)", venice.eval("(str (repeat 4 1))"));
	
		// Double
		assertEquals("()", venice.eval("(str (repeat 0 1.0))"));
		assertEquals("(1.0 1.0 1.0 1.0)", venice.eval("(str (repeat 4 1.0))"));
		
		// Decimal
		assertEquals("()", venice.eval("(str (repeat 0 1.0M))"));
		assertEquals("(1.0M 1.0M 1.0M 1.0M)", venice.eval("(str (repeat 4 1.0M))"));

		// Vector
		assertEquals("()", venice.eval("(str (repeat 0 [5]))"));
		assertEquals("([4 5] [4 5] [4 5] [4 5])", venice.eval("(str (repeat 4 [4 5]))"));

		// String
		assertEquals("(a a a a)", venice.eval("(str (repeat 4 \"a\"))"));
	}
	
	@Test
	public void test_rest() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str (rest nil))"));
		assertEquals("()", venice.eval("(str (rest '()))"));
		assertEquals("()", venice.eval("(str (rest '(1)))"));
		assertEquals("(2)", venice.eval("(str (rest '(1 2)))"));
		assertEquals("(2 3)", venice.eval("(str (rest '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (rest []))"));
		assertEquals("[]", venice.eval("(str (rest [1]))"));
		assertEquals("[2]", venice.eval("(str (rest [1 2]))"));
		assertEquals("[2 3]", venice.eval("(str (rest [1 2 3]))"));
	}

	@Test
	public void test_reverse() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (reverse '()))"));
		assertEquals("(1)", venice.eval("(str (reverse '(1)))"));
		assertEquals("(2 1)", venice.eval("(str (reverse '(1 2)))"));
		assertEquals("(3 2 1)", venice.eval("(str (reverse '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (reverse []))"));
		assertEquals("[1]", venice.eval("(str (reverse [1]))"));
		assertEquals("[2 1]", venice.eval("(str (reverse [1 2]))"));
		assertEquals("[3 2 1]", venice.eval("(str (reverse [1 2 3]))"));
		
		assertEquals("[[9 8 7] [6 5 4] [3 2 1 0]]", venice.eval("(str (reverse [[3 2 1 0] [6 5 4] [9 8 7]]))"));
	}
	
	@Test
	public void test_second() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(second \"\")"));
		assertNull(venice.eval("(second \"1\")"));
		assertEquals("2", venice.eval("(second \"12\")"));
		assertEquals("2", venice.eval("(second \"123\")"));

		assertNull(venice.eval("(second '())"));
		assertNull(venice.eval("(second '(1))"));
		assertEquals(Long.valueOf(2), venice.eval("(second '(1 2))"));
		
		assertNull(venice.eval("(second [])"));
		assertNull(venice.eval("(second [1])"));
		assertEquals(Long.valueOf(2), venice.eval("(second [1 2])"));
	}

	@Test
	public void test_seq() {
		final Venice venice = new Venice();

		assertEquals("(1)", venice.eval("(str (seq '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (seq '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (seq [1 2]))"));
		assertEquals("(a b c)", venice.eval("(str (seq \"abc\"))"));
		assertEquals("([:a 1] [:b 2])", venice.eval("(str (seq {:a 1 :b 2}))"));	

		// Corner cases
		assertEquals(null, venice.eval("(seq nil)"));
		assertEquals(null, venice.eval("(seq \"\")"));
		assertEquals(null, venice.eval("(seq '())"));
		assertEquals(null, venice.eval("(seq [])"));
		assertEquals(null, venice.eval("(seq {})"));

	}
	
	@Test
	public void test_sequential_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(sequential? nil)"));	
		assertFalse((Boolean)venice.eval("(sequential? 1)"));	
		assertTrue((Boolean)venice.eval("(sequential? '(1 2))"));	
		assertTrue((Boolean)venice.eval("(sequential? [1 2])"));	
		assertFalse((Boolean)venice.eval("(sequential? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(sequential? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (set 1 2))"));	
	}
	
	@Test
	public void test_set() {
		final Venice venice = new Venice();

		assertEquals("#{}", venice.eval("(str (set ))"));
		assertEquals("#{1 2 3}", venice.eval("(str (set 1 2 3))"));
		assertEquals("#{[1 2]}", venice.eval("(str (set [1 2]))"));
		assertEquals("#{[1 2] 4}", venice.eval("(str (set [1 2] 4))"));
	}
	
	@Test
	public void test_set_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(set? nil)"));	
		assertFalse((Boolean)venice.eval("(set? 1)"));	
		assertFalse((Boolean)venice.eval("(set? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(set? [1 2])"));	
		assertFalse((Boolean)venice.eval("(set? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(set? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(set? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(set? (sorted-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(set? (set 1 2))"));	
	}
	
	@Test
	public void test_sorted_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(sorted-map? nil)"));	
		assertFalse((Boolean)venice.eval("(sorted-map? 1)"));	
		assertFalse((Boolean)venice.eval("(sorted-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? [1 2])"));	
		assertFalse((Boolean)venice.eval("(sorted-map? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(sorted-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (set 1 2))"));	
	}
	
	@Test
	public void test_some_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(some? nil)"));	
		assertTrue((Boolean)venice.eval("(some? true)"));	
		assertTrue((Boolean)venice.eval("(some? false)"));	
		assertTrue((Boolean)venice.eval("(some? 1)"));	
	}
	
	@Test
	public void test_sort() {
		final Venice venice = new Venice();

		// list
		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(1)", venice.eval("(str (sort '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort '(3 2 1)))"));

		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(1.0)", venice.eval("(str (sort '(1.0)))"));
		assertEquals("(1.0 2.0)", venice.eval("(str (sort '(2.0 1.0)))"));
		assertEquals("(1.0 2.0 3.0)", venice.eval("(str (sort '(3.0 2.0 1.0)))"));

		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(a)", venice.eval("(str (sort '(\"a\")))"));
		assertEquals("(a b)", venice.eval("(str (sort '(\"b\" \"a\")))"));
		assertEquals("(a b c)", venice.eval("(str (sort '(\"c\" \"b\" \"a\")))"));

		assertEquals("(1)", venice.eval("(str (sort '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort '(3 2 1)))"));

		assertEquals("((1 1) (1 2) (1 3) (2 1) (2 2))", venice.eval("(str (sort '((1 2) (1 1) (2 1) (1 3) (2 2))))"));

		assertEquals("(1 2 3)", venice.eval("(str (sort compare '(2 3 1)))"));
		assertEquals("(3 2 1)", venice.eval("(str (sort (comp (partial * -1) compare) '(2 3 1)))"));

		// vector
		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[1]", venice.eval("(str (sort [1]))"));
		assertEquals("[1 2]", venice.eval("(str (sort [2 1]))"));
		assertEquals("[1 2 3]", venice.eval("(str (sort [3 2 1]))"));

		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[1.0]", venice.eval("(str (sort [1.0]))"));
		assertEquals("[1.0 2.0]", venice.eval("(str (sort [2.0 1.0]))"));
		assertEquals("[1.0 2.0 3.0]", venice.eval("(str (sort [3.0 2.0 1.0]))"));

		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[a]", venice.eval("(str (sort [\"a\"]))"));
		assertEquals("[a b]", venice.eval("(str (sort [\"b\" \"a\"]))"));
		assertEquals("[a b c]", venice.eval("(str (sort [\"c\" \"b\" \"a\"]))"));
	
		
		// set
		assertEquals("()", venice.eval("(str (sort (set )))"));
		assertEquals("(1)", venice.eval("(str (sort (set 1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort (set 2 1)))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (sort (set 5 4 3 2 1)))"));
	
		
		// map
		assertEquals("()", venice.eval("(str (sort {}))"));
		assertEquals("([:a 1])", venice.eval("(str (sort {:a 1}))"));
		assertEquals("([:a 1] [:b 2])", venice.eval("(str (sort {:b 2 :a 1}))"));
		assertEquals("([:a 1] [:b 2] [:c 3])", venice.eval("(str (sort {:c 3 :b 2 :a 1}))"));
	}

	@Test
	public void test_sort_compare_fn() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("(1)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(3 2 1)))"));

		// sort reverse
		assertEquals("()", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '()))"));
		assertEquals("(1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(1)))"));
		assertEquals("(2 1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(2 1)))"));
		assertEquals("(3 2 1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(2 3 1)))"));

		assertEquals("()", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("(1.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(1.0)))"));
		assertEquals("(1.0 2.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(2.0 1.0)))"));
		assertEquals("(1.0 2.0 3.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(3.0 2.0 1.0)))"));

		assertEquals("()", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("(a)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(\"a\")))"));
		assertEquals("(a b)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(\"b\" \"a\")))"));
		assertEquals("(a b c)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(\"c\" \"b\" \"a\")))"));

		
		assertEquals("[]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) []))"));
		assertEquals("[1]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [1]))"));
		assertEquals("[1 2]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [2 1]))"));
		assertEquals("[1 2 3]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [3 2 1]))"));

		assertEquals("[]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) []))"));
		assertEquals("[1.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [1.0]))"));
		assertEquals("[1.0 2.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [2.0 1.0]))"));
		assertEquals("[1.0 2.0 3.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [3.0 2.0 1.0]))"));

		assertEquals("[]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) []))"));
		assertEquals("[a]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [\"a\"]))"));
		assertEquals("[a b]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [\"b\" \"a\"]))"));
		assertEquals("[a b c]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [\"c\" \"b\" \"a\"]))"));
	}

	@Test
	public void test_sort_by() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort-by last '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last '((:a 1))))"));
		assertEquals("((:b 1) (:a 2))", venice.eval("(str (sort-by last '((:a 2) (:b 1))))"));
		assertEquals("((:c 1) (:b 2) (:a 3))", venice.eval("(str (sort-by last '((:a 3) (:b 2) (:c 1))))"));

		assertEquals("()", venice.eval("(str (sort-by first '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by first '((:a 1))))"));
		assertEquals("((:a 2) (:b 1))", venice.eval("(str (sort-by first '((:a 2) (:b 1))))"));
		assertEquals("((:a 3) (:b 2) (:c 1))", venice.eval("(str (sort-by first '((:b 2) (:a 3) (:c 1))))"));
	}

	@Test
	public void test_sort_by_compare_fn() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 1))))"));
		assertEquals("((:b 1) (:a 2))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 2) (:b 1))))"));
		assertEquals("((:c 1) (:b 2) (:a 3))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 3) (:b 2) (:c 1))))"));

		// sort reverse
		assertEquals("()", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 1))))"));
		assertEquals("((:a 2) (:b 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 2) (:b 1))))"));
		assertEquals("((:a 3) (:b 2) (:c 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 3) (:b 2) (:c 1))))"));
	}
	
	@Test
	public void test_sorted_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (sorted-map))"));
		assertEquals("{:a 1}", venice.eval("(str (sorted-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (sorted-map :a 1 :b 2))"));
	}
	
	@Test
	public void test_string_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(string? nil)"));	
		assertFalse((Boolean)venice.eval("(string? true)"));	
		assertFalse((Boolean)venice.eval("(string? 1)"));	
		assertFalse((Boolean)venice.eval("(string? -3.0)"));
		assertFalse((Boolean)venice.eval("(string? -3.0M)"));
		assertTrue((Boolean)venice.eval("(string? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(string? :a)"));
		assertFalse((Boolean)venice.eval("(string? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(string? '())"));
		assertFalse((Boolean)venice.eval("(string? [])"));
		assertFalse((Boolean)venice.eval("(string? {})"));
	}
	
	@Test
	public void test_str() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str )"));	
		assertEquals("", venice.eval("(str nil)"));	
		assertEquals("abc", venice.eval("(str \"abc\")"));
		assertEquals("abcdef", venice.eval("(str \"abc\" \"def\")"));
		assertEquals("true", venice.eval("(str true)"));	
		assertEquals("1", venice.eval("(str 1)"));	
		assertEquals("-3.0", venice.eval("(str -3.0)"));
		assertEquals("-3.0123M", venice.eval("(str -3.0123M)"));
		assertEquals(":a", venice.eval("(str :a)"));
		assertEquals("a", venice.eval("(str (symbol :a))"));
		assertEquals("()", venice.eval("(str '())"));
		assertEquals("(1)", venice.eval("(str '(1))"));
		assertEquals("(1 2)", venice.eval("(str '(1 2))"));
		assertEquals("[]", venice.eval("(str [])"));
		assertEquals("[1]", venice.eval("(str [1])"));
		assertEquals("[1 2]", venice.eval("(str [1 2])"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (ordered-map :a 1 :b 2 ))"));
	}

	@Test
	public void test_subtract() {
		final Venice venice = new Venice();

		// Long
		assertEquals(Long.valueOf(-3), venice.eval("(- 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(- 4 1)"));
		assertEquals(Long.valueOf(1), venice.eval("(- 4 1 1 1)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(- 4 1.0)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(- 4 1.0M)"));

		// Double
		assertEquals(Double.valueOf(-4.0D), venice.eval("(- 4.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(- 4.0 1.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(- 4.0 1.0 1.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(- 4.0 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0 2.0M)"));

		// Decimal
		assertEquals(new BigDecimal("-4.00"), venice.eval("(- 4.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(- 4.0M 1.0M)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 1.0M 1.0M)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 2.0)"));
	}

	@Test
	public void test_subvec() {
		final Venice venice = new Venice();

		assertEquals("[3 4 5]", venice.eval("(str (subvec [0 1 2 3 4 5] 3))"));		
		assertEquals("[0 1 2]", venice.eval("(str (subvec [0 1 2 3 4 5] 0 3))"));		
		assertEquals("[2 3 4]", venice.eval("(str (subvec [0 1 2 3 4 5] 2 5))"));		
	}

	@Test
	public void test_symbol() {
		final Venice venice = new Venice();
		
		assertEquals("a", venice.eval("(str (symbol \"a\"))"));
		assertEquals("a", venice.eval("(str (symbol :a))"));
		assertTrue((Boolean)venice.eval("(symbol? (symbol \"a\"))"));	
		assertTrue((Boolean)venice.eval("(symbol? (symbol :a))"));	
	}

	@Test
	public void test_symbol_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(symbol? nil)"));	
		assertFalse((Boolean)venice.eval("(symbol? true)"));	
		assertFalse((Boolean)venice.eval("(symbol? 1)"));	
		assertFalse((Boolean)venice.eval("(symbol? -3.0)"));
		assertFalse((Boolean)venice.eval("(symbol? -3.0M)"));
		assertFalse((Boolean)venice.eval("(symbol? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(symbol? :a)"));
		assertTrue((Boolean)venice.eval("(symbol? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(symbol? '())"));
		assertFalse((Boolean)venice.eval("(symbol? [])"));
		assertFalse((Boolean)venice.eval("(symbol? {})"));
	}

	@Test
	public void test_take() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (take 0 '()))"));
		assertEquals("()", venice.eval("(str (take 1 '()))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1)))"));
		assertEquals("(1)", venice.eval("(str (take 2 '(1)))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1 2)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take 2 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take 3 '(1 2)))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1 2 3)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1 2 3)))"));
		assertEquals("(1 2)", venice.eval("(str (take 2 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(str (take 3 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(str (take 4 '(1 2 3)))"));

		
		assertEquals("[]", venice.eval("(str (take 0 []))"));
		assertEquals("[]", venice.eval("(str (take 1 []))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1]))"));
		assertEquals("[1]", venice.eval("(str (take 2 [1]))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1 2]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take 2 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take 3 [1 2]))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1 2 3]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1 2 3]))"));
		assertEquals("[1 2]", venice.eval("(str (take 2 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(str (take 3 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(str (take 4 [1 2 3]))"));
	}

	@Test
	public void test_take_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '()))"));
		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("(1)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(1)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));


		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) []))"));
		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[1]", venice.eval("(str (take-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[1]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}

	@Test
	public void test_true_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(true? nil)"));	
		assertTrue((Boolean)venice.eval("(true? true)"));	
		assertFalse((Boolean)venice.eval("(true? false)"));	
		assertFalse((Boolean)venice.eval("(true? 1)"));	
	}

	@Test
	public void test_union() {
		final Venice venice = new Venice();

		assertEquals("#{1 2 3}", venice.eval("(str (union (set 1 2 3)))"));
		assertEquals("#{1 2 3}", venice.eval("(str (union (set 1 2) (set 2 3)))"));
		assertEquals("#{1 2 3 4}", venice.eval("(str (union (set 1 2 3) (set 1 2) (set 1 4) (set 3)))"));
	}
	
	@Test
	public void test_update() {
		final Venice venice = new Venice();
		
		// list
		assertEquals("(3)", venice.eval("(str (update '() 0 (fn [x] 3)))"));
		assertEquals("(0 1)", venice.eval("(str (update '(0) 1 (fn [x] 1)))"));
		assertEquals("(3 1)", venice.eval("(str (update '(0 1) 0 (fn [x] 3)))"));
		assertEquals("(4 1)", venice.eval("(str (update '(0 1) 0 (fn [x] (+ x 4))))"));
		
		// vector
		assertEquals("[3]", venice.eval("(str (update [] 0 (fn [x] 3)))"));
		assertEquals("[0 1]", venice.eval("(str (update [0] 1 (fn [x] 1)))"));
		assertEquals("[3 1]", venice.eval("(str (update [0 1] 0 (fn [x] 3)))"));
		assertEquals("[4 1]", venice.eval("(str (update [0 1] 0 (fn [x] (+ x 4))))"));
		
		// map
		assertEquals("{:a 0}", venice.eval("(str (update {} :a (fn [x] 0)))"));
		assertEquals("{:a 1 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] 1)))"));
		assertEquals("{:a 3 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] 3)))"));
		assertEquals("{:a 4 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] (+ x 4))))"));		
	}
	
	@Test
	public void test_update_BANG() {
		final Venice venice = new Venice();
		
		// list
		assertEquals("(3)", venice.eval("(str (update! '() 0 (fn [x] 3)))"));
		assertEquals("(0 1)", venice.eval("(str (update! '(0) 1 (fn [x] 1)))"));
		assertEquals("(3 1)", venice.eval("(str (update! '(0 1) 0 (fn [x] 3)))"));
		assertEquals("(4 1)", venice.eval("(str (update! '(0 1) 0 (fn [x] (+ x 4))))"));
		
		// vector
		assertEquals("[3]", venice.eval("(str (update! [] 0 (fn [x] 3)))"));
		assertEquals("[0 1]", venice.eval("(str (update! [0] 1 (fn [x] 1)))"));
		assertEquals("[3 1]", venice.eval("(str (update! [0 1] 0 (fn [x] 3)))"));
		assertEquals("[4 1]", venice.eval("(str (update! [0 1] 0 (fn [x] (+ x 4))))"));
		
		// map
		assertEquals("{:a 0}", venice.eval("(str (update! {} :a (fn [x] 0)))"));
		assertEquals("{:a 1 :b 1}", venice.eval("(str (update! {:a 0 :b 1} :a (fn [x] 1)))"));
		assertEquals("{:a 3 :b 1}", venice.eval("(str (update! {:a 0 :b 1} :a (fn [x] 3)))"));
		assertEquals("{:a 4 :b 1}", venice.eval("(str (update! {:a 0 :b 1} :a (fn [x] (+ x 4))))"));		
	}
	
	@Test
	public void test_val() {
		final Venice venice = new Venice();

		assertEquals("2", venice.eval("(str (val (find {:a 1 :b 2} :b)))"));
	}
	
	@Test
	public void test_vals() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (vals {})))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort (vals {:a 1 :b 2 :c 3})))"));

		assertEquals("()", venice.eval("(str (sort (vals (ordered-map ))))"));
		assertEquals("(1 2 3)", venice.eval("(str (vals (ordered-map :a 1 :b 2 :c 3)))"));

		assertEquals("()", venice.eval("(str (sort (vals (sorted-map ))))"));
		assertEquals("(1 2 3)", venice.eval("(str (vals (sorted-map :a 1 :b 2 :c 3)))"));
	}

	@Test
	public void test_vary_meta() {
		final Venice venice = new Venice();
								
		assertEquals("1", venice.eval("(str (get (meta (vary-meta [] assoc :a 1)) :a))"));
	}
	
	@Test
	public void test_vector() {
		final Venice venice = new Venice();

		assertEquals("[]", venice.eval("(str [])"));
		assertEquals("[1 2 3]", venice.eval("(str [1 2 3])"));
		
		final Object result = venice.eval("(do [1 2 3])");
		assertTrue(result instanceof List);

		assertEquals("[]", venice.eval("(str (vector))"));
		assertEquals("[1 2 3]", venice.eval("(str (vector 1 2 3))"));
		assertEquals("[[]]", venice.eval("(str (vector []))"));
		assertEquals("[()]", venice.eval("(str (vector '()))"));
		assertEquals("[[1 2]]", venice.eval("(str (vector [1 2]))"));
		assertEquals("[[1 2] 3 4]", venice.eval("(str (vector [1 2] 3 4))"));
	}
	
	@Test
	public void test_vector_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(vector? nil)"));	
		assertFalse((Boolean)venice.eval("(vector? '())"));	
		assertTrue((Boolean)venice.eval("(vector? [])"));	
		assertFalse((Boolean)venice.eval("(vector? 1)"));	
	}

	@Test
	public void test_with_meta() {
		final Venice venice = new Venice();
		
		assertEquals("[1 2 3]", venice.eval("(str (with-meta [1 2 3] { :a 1 }))"));
		assertEquals("{:a 1}", venice.eval("(str (meta (with-meta [1 2 3] { :a 1 })))"));
	}

	@Test
	public void test_zipmap() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 2 :c 3 :d 4 :e 5}", venice.eval("(str (zipmap [:a :b :c :d :e] [1 2 3 4 5]))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (zipmap [:a :b :c] [1 2 3 4 5]))"));		
	}	
	
}
