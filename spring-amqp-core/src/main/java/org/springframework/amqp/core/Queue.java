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

package org.springframework.amqp.core;

import java.util.Map;

/**
 * Simple container collecting information to describe a queue. Used in conjunction with AmqpAdmin.
 * 
 * @author Mark Pollack
 * @see AmqpAdmin
 */
public class Queue {

	private  String name;

	private  boolean durable;

	private  boolean exclusive;

	private  boolean autoDelete;

	private final java.util.Map<java.lang.String, java.lang.Object> arguments;

	/**
	 * The queue is non-durable, non-exclusive and non auto-delete.
	 * 
	 * @param name the name of the queue.
	 */
	public Queue(String name) {
		this(name, false, false, false);
	}

	/**
	 * Construct a new queue, given a name and durability flag. The queue is non-exclusive and non auto-delete.
	 * 
	 * @param name the name of the queue.
	 * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
	 */
	public Queue(String name, boolean durable) {
		this(name, durable, false, false, null);
	}

	/**
	 * Construct a new queue, given a name, durability, exclusive and auto-delete flags.
	 * @param name the name of the queue.
	 * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
	 * @param exclusive true if we are declaring an exclusive queue (the queue will only be used by the declarer's
	 * connection)
	 * @param autoDelete true if the server should delete the queue when it is no longer in use
	 */
	public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete) {
		this(name, durable, exclusive, autoDelete, null);
	}

	/**
	 * Construct a new queue, given a name, durability flag, and auto-delete flag, and arguments.
	 * @param name the name of the queue.
	 * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
	 * @param exclusive true if we are declaring an exclusive queue (the queue will only be used by the declarer's
	 * connection)
	 * @param autoDelete true if the server should delete the queue when it is no longer in use
	 * @param arguments the arguments used to declare the queue
	 */
	public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
		super();
		this.name = name;
		this.durable = durable;
		this.exclusive = exclusive;
		this.autoDelete = autoDelete;
		this.arguments = arguments;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * A durable queue will survive a server restart
	 * 
	 * @return true if durable
	 */
	public boolean isDurable() {
		return this.durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isExclusive() {
		return this.exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public boolean isAutoDelete() {
		return this.autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public java.util.Map<java.lang.String, java.lang.Object> getArguments() {
		return this.arguments;
	}

	@Override
	public String toString() {
		return "Queue [name=" + name + ", durable=" + durable + ", autoDelete=" + autoDelete + ", exclusive="
				+ exclusive + ", arguments=" + arguments + "]";
	}

}
