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
 * Simple container collecting information to describe a binding. Takes String destination and exchange names as
 * arguments to facilitate wiring using code based configuration. Can be used in conjunction with {@link AmqpAdmin}, or
 * created via a {@link BindingBuilder}.
 * 
 * @author Mark Pollack
 * @author Mark Fisher
 * @author Dave Syer
 * 
 * @see AmqpAdmin
 */
public class Binding extends Route {

	public static enum DestinationType {
		QUEUE, EXCHANGE;
	}

	private final String destination;

	private final Map<String, Object> arguments;

	private final DestinationType destinationType;

	public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
			Map<String, Object> arguments) {
		super(new CustomExchange(exchange,"default"),routingKey);
		this.destination = destination;
		this.destinationType = destinationType;
		this.arguments = arguments;
	}

	public Binding(Queue queue, Exchange exchange, String routingKey,  Map<String, Object> arguments) {
		super(exchange, routingKey);
		this.destination = queue.getName();
		this.destinationType = DestinationType.QUEUE;
		this.arguments = arguments;
	}

	public Binding(Queue queue, Route route) {
		super(route);
		this.destination = queue.getName();
		this.destinationType = DestinationType.QUEUE;
		this.arguments = null;
	}

	public Binding(Queue queue, FanoutExchange exchange) {
		this(queue, (Exchange) exchange, "",null);
	}
	public Binding(Queue queue, HeadersExchange exchange, Map<String, Object> arguments) {
		this(queue, (Exchange) exchange, "",null);
	}

	public Binding(Queue queue, DirectExchange exchange, String routingKey) {
		this(queue, (Exchange) exchange, routingKey,null);
	}

	public Binding(Queue queue, TopicExchange exchange, String routingKey) {
		this(queue, (Exchange) exchange, routingKey,null);
	}

	public String getDestination() {
		return this.destination;
	}

	public DestinationType getDestinationType() {
		return this.destinationType;
	}
	public String getRoutingKey(){
		return this.getRoute().getRoutingKey();
	}
	public Route getRoute() {
		return (Route) this;
	}

	public Map<String, Object> getArguments() {
		return this.arguments;
	}

	public boolean isDestinationQueue() {
		return DestinationType.QUEUE.equals(destinationType);
	}

	@Override
	public String toString() {
		return "Binding [destination=" + destination + ", exchange=" + this.getRoute().getExchangeName() + ", routingKey=" + this.getRoute().getRoutingKey() + "]";
	}

}
