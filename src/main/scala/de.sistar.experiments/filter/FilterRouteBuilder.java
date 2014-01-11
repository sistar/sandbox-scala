package de.sistar.experiments.filter;

import org.apache.camel.builder.RouteBuilder;

public class FilterRouteBuilder extends RouteBuilder {
	@Override
	public void configure() throws Exception {
		from("direct:start").to("vm:x");
		from("vm:y").to("mock:gold");
	}
}
