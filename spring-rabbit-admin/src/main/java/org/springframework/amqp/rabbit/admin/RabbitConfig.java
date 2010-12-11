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

package org.springframework.amqp.rabbit.admin;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.*;

/**
 * User: Benjamin Bennett
 * Date: Nov 19, 2010
 * Time: 5:26:03 PM
 */
public class RabbitConfig extends RabbitAdmin {


	public RabbitConfig(RabbitTemplate tp) {
		super(tp);
	}

	public RabbitConfig(ConnectionFactory cf) {
		super(cf);
	}

	Set<Exchange> exchanges = new HashSet<Exchange>();
	Set<Binding> bindings = new HashSet<Binding>();

	public Set<Exchange> getExchanges() {
		return exchanges;
	}

	public void setExchanges(Set<Exchange> exchanges) {
		this.exchanges = exchanges;
	}

	public Set<Binding> getBindings() {
		return bindings;
	}

	public void setBindings(Set<Binding> bindings) {
		this.bindings = bindings;
	}

	public void init() throws Exception {
		//Going to cycle through all exchanges, and
		//Bindings to make sure exchange is declared
		// and queue for each binding.
		Map<String, Exchange> holdExchanges = new HashMap<String, Exchange>();
		Map<String, Queue> holdQueues = new HashMap<String, Queue>();
		for (Exchange exhConfig : getExchanges()) {
			holdExchanges.put(exhConfig.getName(), exhConfig);
		}
		Set<String> createExchanges = new HashSet<String>();
		List<Binding> createBindings = new ArrayList<Binding>();
		for (Binding b : getBindings()) {
			if (!holdExchanges.containsKey(b.getExchangeName())) {
				throw new RuntimeException("Call for exchange " + b.getExchangeName() + " but the exchange was not declared.");
			} else if (!createExchanges.contains(b.getExchangeName())) {
				createExchanges.add(b.getExchangeName());
			}
			Queue nQ = b.getQueue();
			if (!holdQueues.containsKey(b.getQueue().getName())) {
				holdQueues.put(b.getQueue().getName(), b.getQueue());
			} else {
				nQ = holdQueues.get(b.getQueue().getName());
			}
			Binding newBinding;
			Exchange ex = holdExchanges.get(b.getExchangeName());
			//bit of a pain , got to be  better way
			if (ex instanceof TopicExchange)
				newBinding = new Binding(nQ, ((TopicExchange) ex), b.getRoutingKey());
			else if (ex instanceof DirectExchange)
				newBinding = new Binding(nQ, ((DirectExchange) ex), b.getRoutingKey());
			else if (ex instanceof FanoutExchange) {
				if (b.getRoutingKey() == null || !b.getRoutingKey().isEmpty()) {
					throw new RuntimeException("Call for routing key on fan out exchange " + b.getRoutingKey() +
							" fan out exchanges cannot have routing keys.");
				}
				newBinding = new Binding(nQ, ((FanoutExchange) ex));
			} else {
				throw new RuntimeException("Cannot find exchange type for " + ex.getClass().getName());
			}
			createBindings.add(newBinding);
		}
		assert (createExchanges.size() == holdExchanges.size());
		for (Exchange exhConfig : getExchanges()) {
			declareExchange(exhConfig);
		}
		for (Map.Entry<String, Queue> kVp : holdQueues.entrySet()) {
			declareQueue(kVp.getValue());
		}
		for (Binding bD : createBindings) {
			declareBinding(bD);
		}
	}
}
