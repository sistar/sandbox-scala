package de.sistar.experiments;


import java.util.concurrent.atomic.AtomicLong;

public class SerialBean {
	private AtomicLong cnt = new AtomicLong();

	public Long nextVal() {
		return cnt.incrementAndGet();
	}
}
