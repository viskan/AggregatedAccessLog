/*
 * Copyright 2013 Linus Brimstedt
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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Accesslog that logs aggregated information rather than every access.
 *
 * @author Linus Brimstedt
 * @version 1.0
 */
public final class AggregateAccessLogValve extends ValveBase
{

	private static Log log = LogFactory.getLog(LogAggregate.class);

	/**
	 * The descriptive information regarding the implementation
	 */
	private static final String info = "com.viskan.tomcat.valve.AggregatingAccessLogValveValve/1.0";

	static final long HEADER_SIZE = 500;

	final Map<String, LogAggregate> aggregations = new ConcurrentHashMap<String, LogAggregate>();

	public AggregateAccessLogValve()
	{
		log.info(this.getClass().getSimpleName() + " enabled");
	}

	/**
	 * background process will dump current data to log
	 */
	@Override
	public void backgroundProcess()
	{
		super.backgroundProcess();

		for (Entry<String, LogAggregate> e : aggregations.entrySet())
		{
			LogAggregate aggregate = e.getValue();
			log.info(e.getKey() + ": " + aggregate.getAccessCount() + ", " + aggregate.getTotalBytes());
		}
	}

	/**
	 * Returns the descriptive information regarding this valve implementation
	 */
	@Override
	public String getInfo()
	{
		return info;
	}

	/**
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
	public void invoke(Request request, Response response) throws IOException, ServletException
	{

		if (getNext() != null)
		{
			getNext().invoke(request, response);
		}

		String host = getHost(request);
		LogAggregate aggregate = getAggregate(host);

		aggregate.addBytes(response.getContentCountLong() + HEADER_SIZE);
		aggregate.incrementAccessCount();
	}

	private LogAggregate getAggregate(String host)
	{
		LogAggregate aggregate = aggregations.get(host);

		if (aggregate == null)
		{
			synchronized (aggregations)
			{
				aggregate = aggregations.get(host);
				if (aggregate == null)
				{
					aggregate = new LogAggregate(host);
					aggregations.put(host, aggregate);
				}
			}
		}
		return aggregate;
	}

	private String getHost(Request request)
	{
		return request.getContext().getParent().getName();
	}

}
