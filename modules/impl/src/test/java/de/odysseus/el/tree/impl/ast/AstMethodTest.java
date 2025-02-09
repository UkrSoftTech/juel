/*
 * Copyright 2006-2009 Odysseus Software GmbH
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
package de.odysseus.el.tree.impl.ast;

import jakarta.el.BeanELResolver;
import jakarta.el.ELException;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;

import de.odysseus.el.TestCase;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.util.SimpleContext;
import de.odysseus.el.util.SimpleResolver;

public class AstMethodTest extends TestCase {
	AstMethod parseNode(String expression) {
		return (AstMethod)parse(expression).getRoot().getChild(0);
	}

	SimpleContext context;
	Bindings bindings;
	
	long foo = 1l;
	
	public long getFoo() {
		return foo;
	}

	public void setFoo(long value) {
		foo = value;
	}

	public long bar() {
		return 1l;
	}

	public long bar(long value) {
		return value;
	}

	public Object getNullObject() {
		return null;
	}
	
	@Override
	protected void setUp() throws Exception {
		context = new SimpleContext(new SimpleResolver(new BeanELResolver()));
		context.getELResolver().setValue(context, null, "base", this);
		
		bindings = new Bindings(null, new ValueExpression[1]);
	}

	public void testEval() {
		try { parseNode("${base.bad()}").eval(bindings, context); fail(); } catch (MethodNotFoundException e) {}
		assertEquals(1l, parseNode("${base.bar()}").eval(bindings, context));
		assertEquals(3l, parseNode("${base.bar(3)}").eval(bindings, context));
	}

	public void testAppendStructure() {
		StringBuilder s = new StringBuilder();
		parseNode("${foo.bar(1)}").appendStructure(s, new Bindings(null, null, null));
		assertEquals("foo.bar(1)", s.toString());
	}

	public void testIsLiteralText() {
		assertFalse(parseNode("${foo.bar()}").isLiteralText());
	}

	public void testIsLeftValue() {
		assertFalse(parseNode("${foo.bar()}").isLeftValue());
	}

	public void testGetType() {
		assertNull(parseNode("${base.foo()}").getType(bindings, context));
	}

	public void testIsReadOnly() {
		assertTrue(parseNode("${base.foo()}").isReadOnly(bindings, context));
	}

	public void testSetValue() {
		try { parseNode("${base.foo()}").setValue(bindings, context, 0); fail(); } catch (ELException e) {}
	}

	public void testGetValue() {
		assertEquals("1", parseNode("${base.bar()}").getValue(bindings, context, String.class));
		assertEquals("3", parseNode("${base.bar(3)}").getValue(bindings, context, String.class));

		assertNull(parseNode("${base.nullObject.toString()}").getValue(bindings, context, Object.class));
	}
	
	public void testGetValueReference() {
		assertNull(parseNode("${base.bar()}").getValueReference(bindings, context));
	}

	public void testInvoke() {
		assertEquals(1l, parseNode("${base.bar()}").invoke(bindings, context, null, null, new Object[]{999l}));
		assertEquals(3l, parseNode("${base.bar(3)}").invoke(bindings, context, null, new Class[]{long.class}, new Object[]{999l}));
		
		try {
			parseNode("${base.nullObject.toString()}").invoke(bindings, context, null, null, new Object[0]);
			fail();
		} catch (PropertyNotFoundException e) {
			// ok
		}
	}

	public void testGetMethodInfo() {
		assertNull(parseNode("${base.bar()}").getMethodInfo(bindings, context, null, new Class[]{long.class}));
	}
}
