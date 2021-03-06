/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.amqp.rabbit.connection;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import org.springframework.amqp.AmqpIOException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import com.rabbitmq.client.Channel;

/**
 * NOTE: this ConnectionFactory implementation is considered <b>experimental</b> at this stage. There are concerns to be
 * addressed in relation to the statefulness of channels. Therefore, we recommend using {@link SingleConnectionFactory}
 * for now.
 * 
 * A {@link ConnectionFactory} implementation that returns the same Connections from all {@link #createConnection()}
 * calls, and ignores calls to {@link com.rabbitmq.client.Connection#close()} and caches
 * {@link com.rabbitmq.client.Channel}.
 * 
 * <p>
 * By default, only one single Session will be cached, with further requested Channels being created and disposed on
 * demand. Consider raising the {@link #setChannelCacheSize(int) "channelCacheSize" value} in case of a high-concurrency
 * environment.
 * 
 * <p>
 * <b>NOTE: This ConnectionFactory requires explicit closing of all Channels obtained form its shared Connection.</b>
 * This is the usual recommendation for native Rabbit access code anyway. However, with this ConnectionFactory, its use
 * is mandatory in order to actually allow for Channel reuse.
 * 
 * @author Mark Pollack
 * @author Mark Fisher
 * @author Dave Syer
 */
// TODO are there heartbeats and/or exception thrown if a connection is broken?
public class CachingConnectionFactory extends SingleConnectionFactory implements DisposableBean {

	private int channelCacheSize = 1;

	private final LinkedList<ChannelProxy> cachedChannelsNonTransactional = new LinkedList<ChannelProxy>();

	private final LinkedList<ChannelProxy> cachedChannelsTransactional = new LinkedList<ChannelProxy>();

	private volatile boolean active = true;

	/**
	 * Create a new CachingConnectionFactory initializing the hostname to be the value returned from
	 * InetAddress.getLocalHost(), or "localhost" if getLocalHost() throws an exception.
	 */
	public CachingConnectionFactory() {
		super();
	}

	/**
	 * Create a new CachingConnectionFactory given a host name.
	 * 
	 * @param hostName the host name to connect to
	 */
	public CachingConnectionFactory(String hostName) {
		super(hostName);
	}

	/**
	 * Create a new CachingConnectionFactory for the given target ConnectionFactory.
	 * 
	 * @param rabbitConnectionFactory the target ConnectionFactory
	 */
	public CachingConnectionFactory(com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory) {
		super(rabbitConnectionFactory);
	}

	public void setChannelCacheSize(int sessionCacheSize) {
		Assert.isTrue(sessionCacheSize >= 1, "Channel cache size must be 1 or higher");
		this.channelCacheSize = sessionCacheSize;
	}

	public int getChannelCacheSize() {
		return this.channelCacheSize;
	}

	protected Channel getChannel(Connection connection, boolean transactional) throws IOException {
		LinkedList<ChannelProxy> channelList = transactional ? this.cachedChannelsTransactional : this.cachedChannelsNonTransactional;
		Channel channel = null;
		synchronized (channelList) {
			if (!channelList.isEmpty()) {
				channel = channelList.removeFirst();
			}
		}
		if (channel != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Found cached Rabbit Channel");
			}
		} else {
			channel = getCachedChannelProxy(connection, channelList, transactional);
		}
		return channel;
	}

	protected ChannelProxy getCachedChannelProxy(Connection connection, LinkedList<ChannelProxy> channelList, boolean transactional) {
		Channel targetChannel = createBareChannel(connection, transactional);
		if (logger.isDebugEnabled()) {
			logger.debug("Creating cached Rabbit Channel from " + targetChannel);
		}
		return (ChannelProxy) Proxy.newProxyInstance(ChannelProxy.class.getClassLoader(),
				new Class[] { ChannelProxy.class }, new CachedChannelInvocationHandler(connection, targetChannel,
						channelList, transactional));
	}

	private Channel createBareChannel(Connection connection, boolean transactional) {
		try {
			return connection.createChannel(transactional);
		} catch (IOException e) {
			throw new AmqpIOException(e);
		}
	}

	/**
	 * Reset the Channel cache and underlying shared Connection, to be reinitialized on next access.
	 */
	public void resetConnection() {
		this.active = false;
		synchronized (this.cachedChannelsNonTransactional) {
			for (ChannelProxy channel : cachedChannelsNonTransactional) {
				try {
					channel.getTargetChannel().close();
				} catch (Throwable ex) {
					logger.trace("Could not close cached Rabbit Channel", ex);
				}
			}
			this.cachedChannelsNonTransactional.clear();
		}
		this.active = true;
		super.resetConnection();
	}

	@Override
	public String toString() {
		return "CachingConnectionFactory [channelCacheSize=" + channelCacheSize + ", host=" + this.getHost()
				+ ", port=" + this.getPort() + ", active=" + active + "]";
	}

	private class CachedChannelInvocationHandler implements InvocationHandler {

		private volatile Channel target;

		private final LinkedList<ChannelProxy> channelList;

		private final Connection connection;

		private final Object targetMonitor = new Object();

		private final boolean transactional;

		public CachedChannelInvocationHandler(Connection connection, Channel target,
				LinkedList<ChannelProxy> channelList, boolean transactional) {
			this.connection = connection;
			this.target = target;
			this.channelList = channelList;
			this.transactional = transactional;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (methodName.equals("txSelect") && !this.transactional) {
				throw new OperationNotSupportedException("Cannot start transaction on non-transactional channel");
			}
			if (methodName.equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			} else if (methodName.equals("hashCode")) {
				// Use hashCode of Channel proxy.
				return System.identityHashCode(proxy);
			} else if (methodName.equals("toString")) {
				return "Cached Rabbit Channel: " + this.target;
			} else if (methodName.equals("close")) {
				// Handle close method: don't pass the call on.
				if (active) {
					synchronized (this.channelList) {
						if (this.channelList.size() < getChannelCacheSize()) {
							logicalClose((ChannelProxy) proxy);
							// Remain open in the channel list.
							return null;
						}
					}
				}

				// If we get here, we're supposed to shut down.
				physicalClose();
				return null;
			} else if (methodName.equals("getTargetChannel")) {
				// Handle getTargetChannel method: return underlying Channel.
				return this.target;
			} try {
				return method.invoke(this.target, args);
			} catch (InvocationTargetException ex) {
				if (!this.target.isOpen()) {
					// Basic re-connection logic...
					logger.debug("Detected closed channel on exception.  Re-initializing: " + target);
					synchronized (targetMonitor) {
						if (!this.target.isOpen()) {
							this.target = createBareChannel(connection, transactional);
						}
					}
				}
				throw ex.getTargetException();
			}
		}

		/**
		 * GUARDED by channelList
		 * 
		 * @param proxy the channel to close
		 */
		private void logicalClose(ChannelProxy proxy) throws Exception {
			// Allow for multiple close calls...
			if (!this.channelList.contains(proxy)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Returning cached Channel: " + this.target);
				}
				this.channelList.addLast(proxy);
			}
		}

		private void physicalClose() throws Exception {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing cached Channel: " + this.target);
			}
			if (this.target.isOpen()) {
				synchronized (targetMonitor) {
					if (this.target.isOpen()) {
						this.target.close();
					}
				}
			}
		}

	}

}
