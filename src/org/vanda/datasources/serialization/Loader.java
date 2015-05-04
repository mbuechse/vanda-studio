package org.vanda.datasources.serialization;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.vanda.datasources.DataSourceMount;
import org.vanda.util.Observer;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ElementHandlerFactory;
import org.vanda.xml.Factory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SingleElementHandlerFactory;

public class Loader {

	private final List<DataSourceType<?>> types;

	public Loader(List<DataSourceType<?>> types) {
		this.types = types;
	}

	@SuppressWarnings("unchecked")
	private ElementHandlerFactory<MountBuilder> sourceHandler() {
		CompositeElementHandlerFactory<MountBuilder> cehf = new CompositeElementHandlerFactory<MountBuilder>();
		for (DataSourceType<?> dst : types)
			cehf.addHandler(dst.load());
		return cehf;
	}

	private ElementHandlerFactory<List<DataSourceMount>> mountHandler(ElementHandlerFactory<MountBuilder> sourceHandler) {
		ComplexFieldProcessor<List<DataSourceMount>, MountBuilder> xxx = new ComplexFieldProcessor<List<DataSourceMount>, MountBuilder>() {
			@Override
			public void process(List<DataSourceMount> rds1, MountBuilder mb) {
				if (mb.ds != null)  // formerly, Double and Integer got stored, but that was a mistake!
					rds1.add(new DataSourceMount(mb.prefix, mb.ds));
			}
		};
		return new SimpleElementHandlerFactory<List<DataSourceMount>, MountBuilder>("mount", sourceHandler,
				MountBuilder.createFactory(), xxx, MountBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<Observer<List<DataSourceMount>>> rootHandler(
			ElementHandlerFactory<List<DataSourceMount>> mountHandler) {
		Factory<List<DataSourceMount>> xxx = new Factory<List<DataSourceMount>>() {
			@Override
			public List<DataSourceMount> create() {
				return new LinkedList<DataSourceMount>();
			}
		};
		return new SimpleElementHandlerFactory<Observer<List<DataSourceMount>>, List<DataSourceMount>>("root",
				mountHandler, xxx, new ComplexFieldProcessor<Observer<List<DataSourceMount>>, List<DataSourceMount>>() {
					@Override
					public void process(Observer<List<DataSourceMount>> b1, List<DataSourceMount> b2) {
						b1.notify(b2);
					}
				}, null, null);
	}

	private ParserImpl<List<DataSourceMount>> createParser(Observer<List<DataSourceMount>> o) {
		ParserImpl<List<DataSourceMount>> p = new ParserImpl<List<DataSourceMount>>(o);
		p.setRootState(new SimpleRootHandler<List<DataSourceMount>>(p, rootHandler(mountHandler(sourceHandler()))));
		return p;
	}

	public List<DataSourceMount> load(String filename) throws Exception {
		MyObserver o = new MyObserver();
		ParserImpl<List<DataSourceMount>> p = createParser(o);
		try {
			p.init(new File(filename));
			p.process();
		} finally {
			p.done();
		}
		return o.value;
	}

	private static class MyObserver implements Observer<List<DataSourceMount>> {

		List<DataSourceMount> value;

		@Override
		public void notify(List<DataSourceMount> event) {
			value = event;
		}

	}

}
