package de.sistar.experiments;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;

import java.util.Collection;
import java.util.List;


public class JAggregatorRoute extends RouteBuilder {


	@Override
	public void configure() throws Exception {
		AbstractListAggregationStrategy<Message> aggregationStrategy = new AbstractListAggregationStrategy<Message>() {
			@Override
			public Message getValue(Exchange exchange) {
				return exchange.getIn();
			}
		};

		class MyP implements Predicate {
			@Override
			public boolean matches(Exchange exchange) {
				Long actSerial = exchange.getIn().getHeader("actSerial", Long.class);
				List<Message> l = (List<Message>) exchange.getProperty(Exchange.GROUPED_EXCHANGE);
				final Collection<Long> actSerials = Collections2.transform(l, new Function<Message, Long>() {
					@Override
					public Long apply(Message input) {
						return input.getHeader("actSerial", Long.class);
					}
				});

				Long maxSerialFromGrouped = Ordering.<Long>natural().max(actSerials);
				return actSerial - maxSerialFromGrouped > 5L;
			}
		}

		from("direct:c")
				.setHeader("actSerial", simple("${bean:serialBean?method=nextVal}"))
				.aggregate(header("corid"), aggregationStrategy).completionPredicate(new MyP())
				.to("mock:aggregated");
	}
}
