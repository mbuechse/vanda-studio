package org.vanda.studio.modules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.DataSourceRepository;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.datasources.IntegerDataSource;
import org.vanda.datasources.serialization.DirectoryDataSourceType;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.swing.datasources.DataSourceEditor;
import org.vanda.swing.datasources.DataSourceFactory;
import org.vanda.swing.datasources.DataSourceRepositoryEditor;
import org.vanda.swing.datasources.DirectoryDataSourceEditor;
import org.vanda.swing.datasources.DirectoryDataSourceFactory;
import org.vanda.swing.datasources.DoubleDataSourceEditor;
import org.vanda.swing.datasources.IntegerDataSourceEditor;
import org.vanda.swing.datasources.DoubleDataSourceEditor.Fäctory;
import org.vanda.util.Action;
import org.vanda.util.CompositeFactory;
import org.vanda.util.Factory;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Observer;

public class DataSourceModule implements Module {

	@Override
	public String getName() {
		return "Data Sources";
	}

	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/datasources.xml";
	private final MetaRepository<String, DataSourceMount> mountMeta;
	
	public DataSourceModule(MetaRepository<String, DataSourceMount> mountMeta) {
		this.mountMeta = mountMeta;
	}

	@Override
	public Object createInstance(Application app) {
		DataSourceRepository dsr = new DataSourceRepository(PROPERTIES_FILE);
		dsr.addDataSourceType(new DirectoryDataSourceType());
		ListRepository<DataSourceMount> dsr2 = new ListRepository<DataSourceMount>();
		mountMeta.addRepository(dsr);
		mountMeta.addRepository(dsr2);

		CompositeFactory<DataSource, DataSourceEditor> fr = new CompositeFactory<DataSource, DataSourceEditor>();
		fr.put(IntegerDataSource.class, new IntegerDataSourceEditor.Fäctory());
		fr.put(DirectoryDataSource.class, new DirectoryDataSourceEditor.Fäctory(app));
		fr.put(DoubleDataSource.class, new DoubleDataSourceEditor.Fäctory());

		dsr.refresh();
		dsr2.addItem(new DataSourceMount("Integer", new IntegerDataSource()));
		dsr2.addItem(new DataSourceMount("Double", new DoubleDataSource()));

		List<DataSourceFactory> dsf = new LinkedList<DataSourceFactory>();
		dsf.add(new DirectoryDataSourceFactory(System.getProperty("user.home") + "/.vanda", ".*", null));

		app.getWindowSystem().addAction(null, new DataSourceEditorAction(app, dsr, fr, dsf), null, 2);
		return null;
	}

	private final class DataSourceEditorAction implements Action {

		private final DataSourceRepository dataSourceRepository;
		private final Factory<DataSource, DataSourceEditor> fr;
		private final Collection<DataSourceFactory> dsf;
		private final Application app;
		private Observer<Application> shutdownObserver; // keep reference
		private DataSourceRepositoryEditor ed;

		public DataSourceEditorAction(Application app, DataSourceRepository dataSourceRepository,
				Factory<DataSource, DataSourceEditor> fr, Collection<DataSourceFactory> dsf) {
			this.app = app;
			this.dataSourceRepository = dataSourceRepository;
			this.fr = fr;
			this.dsf = dsf;

			// Eventually close open editor on system shutdown
			shutdownObserver = new Observer<Application>() {
				@Override
				public void notify(Application event) {
					if (ed != null) {
						ed.dispose();
						ed = null;
					}
				}
			};
			app.getShutdownObservable().addObserver(shutdownObserver);
		}

		@Override
		public String getName() {
			return "Edit Data Sources...";
		}

		@Override
		public void invoke() {
			// TODO do not keep reference in this class; use (potentially
			// improved) WindowSystem
			if (ed != null) {
				ed.bringToFront();
			} else {
				ed = new DataSourceRepositoryEditor(dataSourceRepository, fr, dsf, app);
			}
		}
	}

}
