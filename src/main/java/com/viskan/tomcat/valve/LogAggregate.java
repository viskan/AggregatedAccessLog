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

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

class LogAggregate implements LogAggregateMBean
{
    private static Log log = LogFactory.getLog(LogAggregate.class);

    private AtomicInteger accessCount = new AtomicInteger();
	private AtomicLong totalBytes = new AtomicLong();

	public LogAggregate(String host)
	{

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	    ObjectName name;
		try
		{
			name = new ObjectName("com.viskan.tomcat.valve:type=LogAggregate,name=" + host);
			mbs.registerMBean(this, name);
		}
		catch (Exception e)
		{
			log.error("Unable to register mbean for " + host);
		}
	}

	void incrementAccessCount()
	{
		accessCount.incrementAndGet();
	}

	void addBytes(long bytes)
	{
		totalBytes.addAndGet(bytes);
	}

	public long getTotalBytes()
	{
		return totalBytes.get();
	}

	public int getAccessCount()
	{
		return accessCount.get();
	}
}
