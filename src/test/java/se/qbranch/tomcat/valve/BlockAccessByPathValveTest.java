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

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class BlockAccessByPathValveTest {
	
	private BlockAccessByPathValve allowLocalhostValve;
	private BlockAccessByPathValve denyLocalhostValve;
	private InvokedValve invokedValve;
	private Request req;
	private Response resp;
	
	@Before
	public void setup() {
		allowLocalhostValve = new BlockAccessByPathValve();
		denyLocalhostValve = new BlockAccessByPathValve();
		invokedValve = new InvokedValve();
		
		allowLocalhostValve.setNext(invokedValve);
		allowLocalhostValve.setPath("/probe/.*,/manager/.*");
		allowLocalhostValve.setAllow("127\\.0\\.0\\.1");
		
		denyLocalhostValve.setNext(invokedValve);
		denyLocalhostValve.setPath("/probe/.*,/manager/.*");
		denyLocalhostValve.setDeny("127\\.0\\.0\\.1");
		
		req = mock(Request.class);
		resp = mock(Response.class);
	}

	@Test
	public void blockAccessFromLocalhostOnBlockedPath() throws Exception {
		doRequest("/manager/index.html", "127.0.0.1", denyLocalhostValve);
		
		verify(resp).sendError(404);
		assertFalse("Should not allow access on /manager from 127.0.0.1", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromLocalhostOnNonBlockedPathWhenLocalhostIsBlocked() throws Exception {
		doRequest("/", "127.0.0.1", denyLocalhostValve);
		
		assertTrue("Should not block access on nonblocked path even though ip is set to block.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromLocalhostOnBlockedPath() throws Exception {	
		doRequest("/manager/test", "127.0.0.1", allowLocalhostValve);
		assertTrue("Should not have blocked the request.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromLocalhostOnNonBlockedPath() throws Exception {
		doRequest("/", "127.0.0.1", allowLocalhostValve);
		assertTrue("Should allow access on / when ip is 127.0.0.1.", invokedValve.wasInvoked());
	}
	
	@Test
	public void blockAccessFromNonLocalhostIpOnBlockedPath() throws Exception {
		doRequest("/probe/", "81.5.23.0", allowLocalhostValve);
		
		verify(resp).sendError(404);
		assertFalse("Should block access on /probe/ when ip is not 127.0.0.1.", invokedValve.wasInvoked());
	}
	
	@Test
	public void allowAccessFromNonLocalhostIpOnNonBlockedPath() throws Exception{
		doRequest("/demo", "81.5.23.0", allowLocalhostValve);
		assertTrue("Should allow access on /demo/", invokedValve.wasInvoked());
	}
	
	private void doRequest(String path, String remoteAddr, Valve valveToTestWith) throws Exception {
		when(req.getRemoteAddr()).thenReturn(remoteAddr);
		when(req.getRequestURI()).thenReturn(path);
		
		valveToTestWith.invoke(req, resp);
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
