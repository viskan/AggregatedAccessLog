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
 * Implementation of a tomcat valve that blocks access on certain paths.
 * 
 * @author Leonard Axelsson
 * @version 1.0 
 * 
 */
public class BlockAccessByPathValve extends RequestFilterValve {
	
	private String path;

	private Pattern[] paths;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		paths = precalculate(path);
		this.path = path;
	}

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		String remoteAddr = request.getRemoteAddr();
		String requestedPath = request.getRequestURI();
		
		boolean pathBlocked = false;
		for(Pattern p: paths) {
			if(p.matcher(requestedPath).matches()) {
				pathBlocked = true;
				break;
			}
		}
		
		if(pathBlocked) {
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
