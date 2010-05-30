/*
 * Copyright 2010 Leonard Axelsson
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
package se.qbranch.tomcat.valve;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class BlockAccessByPathValveTest {
	
	private BlockAccessByPathValve allowLocalhostValve;
	private BlockAccessByPathValve deny81NetValve;
	private InvokedValve invokedValve;
	private Request req;
	private Response resp;
	
	@Before
	public void setup() {
		allowLocalhostValve = new BlockAccessByPathValve();
		deny81NetValve = new BlockAccessByPathValve();
		invokedValve = new InvokedValve();
		
		allowLocalhostValve.setNext(invokedValve);
		allowLocalhostValve.setPath("/probe/.*,/manager/.*");
		allowLocalhostValve.setAllow("127\\.0\\.0\\.1");
		
		deny81NetValve.setNext(invokedValve);
		deny81NetValve.setPath("/probe/.*,/manager/.*");
		deny81NetValve.setDeny("81\\..*");
		
		req = mock(Request.class);
		resp = mock(Response.class);
	}

	@Test
	public void blockAccessWhenIpStartsWith81OnBlockedPath() throws Exception {
		doRequest("/manager/index.html", "81.0.2.6");
		
		verify(resp).sendError(404);
		assertFalse("Should not allow access on /manager from 81.0...", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessWhenIpStartsWith81OnNonBlockedPath() throws Exception {
		doRequest("/", "81.0.2.6");
		
		assertTrue("Should not block access on nonblocked path.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromLocalhostOnBlockedPath() throws Exception {	
		doRequest("/manager/test", "127.0.0.1");
		assertTrue("Should not have blocked the request.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromLocalhostOnNonBlockedPath() throws Exception {
		doRequest("/", "127.0.0.1");
		assertTrue("Should allow access on / when ip is 127.0.0.1.", invokedValve.wasInvoked());
	}
	
	@Test
	public void blockAccessFromNonLocalhostIpOnBlockedPath() throws Exception {
		doRequest("/probe/", "81.5.23.0");
		
		verify(resp).sendError(404);
		assertFalse("Should block access on /probe/ when ip is not 127.0.0.1.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromNonLocalhostIpOnNonBlockedPath() throws Exception{
		doRequest("/demo", "81.5.23.0");
		assertTrue("Should allow access on /demo/", invokedValve.wasInvoked());
	}
	
	private void doRequest(String path, String remoteAddr) throws Exception {
		when(req.getRemoteAddr()).thenReturn(remoteAddr);
		when(req.getRequestURI()).thenReturn(path);
		
		allowLocalhostValve.invoke(req, resp);
	}
	
	
	private class InvokedValve extends ValveBase {

		private boolean invoked = false;
		
		public boolean wasInvoked() {
			return invoked;
		}
		
		@Override
		public void invoke(Request arg0, Response arg1) throws IOException,
				ServletException {
			invoked = true;
		}
		
	}
}
