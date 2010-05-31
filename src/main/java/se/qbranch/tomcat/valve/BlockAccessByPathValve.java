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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;

/**
 * Concrete implementation of <code>RequestFilterValve</code> that filters
 * requests based on request path and the remote clients IP address. This
 * implementation takes a step away from the contract and uses 404 instead of
 * 403 to block client access.
 * <p>
 * Specify one or more (comma separated) regular expressions in the path arg,
 * those will paths will only be available when the remote client:s IP is not
 * specified in deny, or when the IP is specified in allow. The valve can be
 * used in Engine, Host or the Context element. For information on how allow
 * and deny are handled see javadoc for {@link RequestFilterValve}.
 * </p>
 * <p>
 * Example of only allowing access from 127.0.0.1 on the tomcat manager application.<BR/>
 * Usage: <code>&lt;Valve className="se.qbranch.tomcat.valve.BlockAccessByPathValve" path="/manager/.*" allow="127\.0\.0\.1"/&gt;</code>
 * </p>
 * 
 * @author Leonard Axelsson
 * @version 1.0
 */
public final class BlockAccessByPathValve extends RequestFilterValve {

	/**
	 * The descriptive information regarding the implementation
	 */
	private static final String info = "se.qbranch.tomcat.valve.BlockAccessByPathValve/1.0";

	/**
	 * The path to block access on, specified as a list of comma-separated
	 * regular expressions.
	 */
	private String path;

	/**
	 * The paths to block access on as regular expression objects. Patterns are
	 * parsed from <code>path</code>.
	 */
	private Pattern[] paths;

	/**
	 * Returns the descriptive information regarding this valve implementation
	 */
	@Override
	public String getInfo() {
		return info;
	}

	/**
	 * Returns the string representation of the regular expressions for the paths that are blocked.
	 * 
	 * @return comma separated regular expressions for paths to be blocked
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path to block access on and builds <code>Pattern</code> objects
	 * from it.
	 * 
	 * @param path
	 *            the paths to block access on specified as a comma separated
	 *            list
	 */
	public void setPath(String path) {
		paths = precalculate(path);
		this.path = path;
	}

	/**
	 * Extracts the remote address and request path from the request object and
	 * match to the regular expressions specified in path, allow and deny
	 * 
	 * 
	 * @param request
	 *            The servlet request to be processed
	 * @param response
	 *            The servlet response to be created
	 * 
	 * @exception IOException
	 *                IOException if an input/output error occurs
	 * @exception ServletException
	 *                ServletException if a servlet error occurs
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		String remoteAddr = request.getRemoteAddr();
		String requestedPath = request.getRequestURI();

		boolean pathBlocked = false;
		for (Pattern p : paths) {
			if (p.matcher(requestedPath).matches()) {
				pathBlocked = true;
				break;
			}
		}

		if (pathBlocked) {
			// Check the deny patterns, if any
			for (int i = 0; i < denies.length; i++) {
				if (denies[i].matcher(remoteAddr).matches()) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}

			// Check the allow patterns, if any
			for (int i = 0; i < allows.length; i++) {
				if (allows[i].matcher(remoteAddr).matches()) {
					getNext().invoke(request, response);
					return;
				}
			}

			// Allow if denies specified but not allows
			if ((denies.length > 0) && (allows.length == 0)) {
				getNext().invoke(request, response);
				return;
			}

			// Deny this request
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		getNext().invoke(request, response);
	}

}
