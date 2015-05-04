package org.vanda.util;


public interface MetaRepository<S, T> {

	<T1 extends T> void addRepository(Repository<S, T1> r);
	
	Observable<T> getAddObservable();
	
	Observable<T> getModifyObservable();
	
	Repository<S, T> getRepository();

	Observable<T> getRemoveObservable();

	Observable<Repository<S, ? extends T>> getRepositoryAddObservable();

	Observable<Repository<S, ? extends T>> getRepositoryRemoveObservable();

	<T1 extends T> void removeRepository(Repository<S, T1> r);

}
