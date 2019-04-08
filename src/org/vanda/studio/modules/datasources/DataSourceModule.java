package org.vanda.studio.modules.datasources;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.vanda.datasources.DataSourceEditor;
import org.vanda.datasources.DirectoryDataSourceFactory;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.datasources.IntegerDataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.datasources.serialization.DataSourceType;
import org.vanda.datasources.serialization.DirectoryDataSourceType;
import org.vanda.datasources.serialization.Loader;
import org.vanda.datasources.serialization.Storer;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

public class DataSourceModule implements Module {

	@Override
	public String getName() {
		return "Data Sources";
	}

	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/datasources.xml";

	@Override
	public Object createInstance(Application a) {
		RootDataSource rds = a.getRootDataSource();
		rds.addItem(new DirectoryDataSourceFactory(System.getProperty("user.home") + "/.vanda", ".*", null));
		List<DataSourceType<?>> dsts = new LinkedList<DataSourceType<?>>();
		dsts.add(new DirectoryDataSourceType());
		Loader l = new Loader(rds, dsts);
		try {
			l.load(PROPERTIES_FILE);
		} catch (Exception e) {
			// do nothing if file does not exist or so
		}

		rds.mount("Integer", new IntegerDataSource());
		rds.mount("Double", new DoubleDataSource());
		/*
		 * rds.mount("europarl", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "europarl_eng-ger", ".*"));
		 * rds.mount("example1", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_eng-spa", ".*"));
		 * rds.mount("example2", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_arc-cen", ".*"));
		 * rds.mount("example3", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_astronauts", ".*"));
		 * rds.mount("Berkeley Grammars", new DirectoryDataSource( new
		 * CompositeType("BerkeleyGrammar.sm6"), "grammars", ".*gr"));
		 * rds.mount("LAPCFG Grammars", new DirectoryDataSource( new
		 * CompositeType("LAPCFG-Grammar"), "lapcfg", ".*"));
		 */

		Storer st = new Storer(dsts);
		a.getWindowSystem().addAction(null, new DataSourceEditorAction(a, rds, st), null, 2);
		return null;
	}

	private final class DataSourceEditorAction implements Action {

		private RootDataSource ds;
		private Storer st;
		private Application a;
		private JFrame f = null;
		private Observer<Application> shutdownObserver;

		public DataSourceEditorAction(Application a, RootDataSource ds, Storer st) {
			this.ds = ds;
			this.st = st;
			this.a = a;

			// Eventually close open editor on system shutdown
			shutdownObserver = new Observer<Application>() {
				@Override
				public void notify(Application event) {
					if (f != null) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								f.dispose();
							}
						});
					}
				}
			}; 
			a.getShutdownObservable().addObserver(shutdownObserver);
		}

		@Override
		public String getName() {
			return "Edit Data Sources...";
		}

		@Override
		public void invoke() {
			if (f != null) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						f.toFront();
						f.repaint();
					}
				});
			} else {
				f = new JFrame("Data Source Editor");
				f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						f = null;
					}
				});
				DataSourceEditor ed = ds.createEditor(a);
				ed.addWriteAction(new StoreAction(a, st, ds));
				f.setContentPane(ed.getComponent());
				f.setSize(new Dimension(500, 400));
				f.setLocationRelativeTo(a.getWindowSystem().getMainWindow());
				f.setVisible(true);
			}
		}

	}

	private final class StoreAction implements Action {

		private Storer st;
		private Application app;
		private RootDataSource rds;

		public StoreAction(Application app, Storer st, RootDataSource rds) {
			this.st = st;
			this.app = app;
			this.rds = rds;
		}

		@Override
		public String getName() {
			return "StoreAction";
		}

		@Override
		public void invoke() {
			Path p1 = (new File(PROPERTIES_FILE)).toPath();
			Path p2 = (new File(PROPERTIES_FILE + ".backup")).toPath();
			try {
				Files.move(p1, p2, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}

			try {
				st.store(rds, PROPERTIES_FILE);
			} catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}
		}

	}

}
