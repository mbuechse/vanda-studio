package org.vanda.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.util.Pair;

public interface Type {
	
	boolean canDecompose();
	
	boolean contains(Object v);
	
	Pair<String, List<Type>> decompose();
	
	boolean failsOccursCheck(Type rhs);
	
	void freshMap(Map<Object, Object> m);
	
	Type rename(Map<Object, Object> m);
	
	Type subst(Object variable, Type nt);
	
	void insertInto(Set<Type> types);

}
