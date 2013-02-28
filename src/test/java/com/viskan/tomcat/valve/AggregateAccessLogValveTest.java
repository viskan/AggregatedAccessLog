/*
 * Copyright 2010 Linus Brimstedt
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
 *
 */
package com.viskan.tomcat.valve;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AggregateAccessLogValve
 *
 * @author Linus Brimstedt
 *
 */
public class AggregateAccessLogValveTest {

	private AggregateAccessLogValve logValve;
	private Context context;
	private Request req;
	private Response resp;
	private Container container;

	@Before
	public void setup() {
		logValve = new AggregateAccessLogValve();

		req = mock(Request.class);
		resp = mock(Response.class);
		context = mock(Context.class);
		container = mock(Container.class);
	}

	@Test
	public void test_access_count() throws Exception {
		doRequest("test", 100, logValve);
		doRequest("another", 10, logValve);
		doRequest("test", 1000, logValve);
		doRequest("test", 10000, logValve);

		assertEquals("Expecting 3 requests counted for 'test'", 3, logValve.aggregations.get("test").getAccessCount());
		assertEquals("Expecting 1 request counted for 'another'", 1, logValve.aggregations.get("another").getAccessCount());
	}

	@Test
	public void test_bytes_calculation() throws Exception {
		doRequest("test", 100, logValve);
		doRequest("another", 10, logValve);
		doRequest("test", 1000, logValve);
		doRequest("test", 10000, logValve);

		assertEquals("Expected 11100 + " + AggregateAccessLogValve.HEADER_SIZE + " * 3 bytes transferred", 11100 + AggregateAccessLogValve.HEADER_SIZE * 3, logValve.aggregations.get("test").getTotalBytes());
		assertEquals("Expected 10 + " + AggregateAccessLogValve.HEADER_SIZE + " bytes transferred", 10 + AggregateAccessLogValve.HEADER_SIZE, logValve.aggregations.get("another").getTotalBytes());
	}

	@Test
	public void test_values_are_reset_ny_resets_values() throws Exception {
		doRequest("test", 100, logValve);
		doRequest("another", 10, logValve);
		doRequest("test", 1000, logValve);
		doRequest("test", 10000, logValve);

		logValve.aggregations.get("test").reset();
		assertEquals("Expected 0", 0, logValve.aggregations.get("test").getTotalBytes());
		assertEquals("Expected 0", 0, logValve.aggregations.get("test").getAccessCount());
		assertEquals("Expected 10 + " + AggregateAccessLogValve.HEADER_SIZE + " bytes transferred", 10 + AggregateAccessLogValve.HEADER_SIZE, logValve.aggregations.get("another").getTotalBytes());
	}




	private void doRequest(String host, long size, Valve valveToTestWith) throws Exception {
		when(resp.getContentCountLong()).thenReturn(size);
		when(context.getParent()).thenReturn(container);
		when(container.getName()).thenReturn(host);
		when(req.getContext()).thenReturn(context);

		valveToTestWith.invoke(req, resp);
	}


}
