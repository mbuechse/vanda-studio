package org.vanda.runner.serialization;

import java.util.HashMap;

import org.vanda.xml.Factory;

public class PrioritiesBuilder {
	public HashMap<String, Integer> priorities;

	public PrioritiesBuilder() {
		priorities = new HashMap<String, Integer>();
	}

	public static Factory<PrioritiesBuilder> createFactory() {
		return new Fäctory();
	}

	public static final class Fäctory implements Factory<PrioritiesBuilder> {
		@Override
		public PrioritiesBuilder create() {
			return new PrioritiesBuilder();
		}
	}
}
