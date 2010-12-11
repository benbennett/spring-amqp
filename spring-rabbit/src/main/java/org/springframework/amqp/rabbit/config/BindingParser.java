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

package org.springframework.amqp.rabbit.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * User: Benjamin Bennett 
 * Date: Nov 21, 2010
 * Time: 9:51:32 PM
 */
public class BindingParser extends AbstractSingleBeanDefinitionParser {
	@Override
	protected String getBeanClassName(Element element) {
		return "org.springframework.amqp.core.Binding";
	}

	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		//either you have a route or (exchange and routing key)
		String route = element.getAttribute("route");
		String exchange = element.getAttribute("exchange");
		String routingKey = element.getAttribute("routing-key");
		boolean hasExchangeAndRoute = StringUtils.hasText(exchange) || StringUtils.hasText(routingKey);
		boolean hasRoute = StringUtils.hasText(route);
		Assert.isTrue(hasRoute ^ hasExchangeAndRoute,
				"route attribute is mutually exclusive when setting exchange and routing-key attributes.");
		builder.addConstructorArgReference(element.getAttribute("queue"));
		if (hasRoute && !hasExchangeAndRoute) {
			builder.addConstructorArgReference(route);
		} else {
			builder.addConstructorArgReference(exchange);
			builder.addConstructorArgValue(routingKey);
		}

	}
}
