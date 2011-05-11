/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.amqp.core;

import java.util.Map;


/**
 * Common properties that describe all exchange types.  
 * <p>Subclasses of this class are typically used with administrative operations that declare an exchange.
 * 
 * @author Mark Pollack
 * 
 * @see AmqpAdmin
 */
public abstract class AbstractExchange implements Exchange {

	private  String name;

	private  boolean durable;

	private  boolean autoDelete;

	private Map<String, Object> arguments = null;

	/**
	 * Construct a new Exchange for bean usage. 
	 * @param name the name of the exchange.
	 */
	public AbstractExchange(String name) {
		this(name, false, false);
	}
	
	/**
	 * Construct a new Exchange, given a name, durability flag, auto-delete flag. 
	 * @param name the name of the exchange.
	 * @param durable true if we are declaring a durable exchange (the exchange will survive a server restart)
	 * @param autoDelete true if the server should delete the exchange when it is no longer in use
	 */
	public AbstractExchange(String name, boolean durable, boolean autoDelete) {
		this(name, durable, autoDelete, null);
	}

	/**
	 * Construct a new Exchange, given a name, durability flag, and auto-delete flag, and arguments. 
	 * @param name the name of the exchange.
	 * @param durable true if we are declaring a durable exchange (the exchange will survive a server restart)
	 * @param autoDelete true if the server should delete the exchange when it is no longer in use
	 * @param arguments the arguments used to declare the exchange
	 */
	public AbstractExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
		super();
		this.name = name;
		this.durable = durable;
		this.autoDelete = autoDelete;
		this.arguments = arguments;
	}

	public abstract String getType();

	public String getName() {
		return name;
	}

	public boolean isDurable() {
		return durable;
	}

	/**
	 * Set the durability of this exchange definition.
	 * @param durable true if describing a durable exchange (the exchange will survive a server restart)
	 */
	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	/**
	 * Set the auto-delete lifecycle of this exchange.
	 * An non-auto-deleted exchange lasts until the server is shut down.
	 * @param autoDelete true if the server should delete the exchange when it is no longer in use.
	 */
	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	/**
	 * Return the collection of arbitrary arguments to use when declaring an exchange.
	 * @return the collection of arbitrary arguments to use when declaring an exchange.
	 */
	public Map<String, Object> getArguments() {
		return arguments;
	}

	/**
	 * Set the collection of arbitrary arguments to use when declaring an exchange.
	 * @param arguments A collection of arbitrary arguments to use when declaring an exchange.
	 */
	public void setArguments(Map<String, Object> arguments) {
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return "Exchange [name=" + name + 
						 ", type=" + this.getType() +
						 ", durable=" + durable +
						 ", autoDelete=" + autoDelete + 
						 ", arguments="	+ arguments + "]";
	}

}
