package org.vanda.util;

public interface Factory<D, E> {
	E instantiate(D d);
}