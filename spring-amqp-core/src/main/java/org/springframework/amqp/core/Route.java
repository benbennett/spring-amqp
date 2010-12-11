/*
 * Copyright 2002-2010 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.amqp.core;

import org.springframework.amqp.core.Exchange;

/**
 * User: Benjamin Bennett
 * Date: Nov 21, 2010
 * Time: 8:35:07 PM
 */
public class Route {

	private Exchange exchange;

	private String routingKey;

	public Route(Route route){
		this.exchange = route.getExchange();
		this.routingKey = route.getRoutingKeyString();
	}
	public Route(Exchange exchange, String routingKey) {
		this.exchange = exchange;
		this.routingKey = routingKey;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public String getExchangeName() {
		return this.getExchange().getName();
	}

	public String getRoutingKeyString() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
}
