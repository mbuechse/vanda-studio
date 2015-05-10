package org.vanda.studio.modules.assignments;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.swing.data.AssignmentTablePanel;
import org.vanda.swing.data.ElementSelector;
import org.vanda.util.Action;
import org.vanda.util.Factory;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;

public class AssignmentTableToolFactory implements ToolFactory {
	
	private final Application app;
	private final Factory<DataSource, ElementSelector> fr;
	private final RootDataSource rds;

	private class OpenAssignmentTableAction implements Action {

		private WorkflowEditor wfe;
		private JFrame f = null;
		private final Observer<Application> shutdownObserver;
		protected Observer<DatabaseEvent<Database>> databaseObserver;

		public OpenAssignmentTableAction(WorkflowEditor wfe) {
			this.wfe = wfe;
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
			app.getShutdownObservable().addObserver(shutdownObserver);
		}

		@Override
		public String getName() {
			return "Open assignment table...";
		}

		@Override
		public void invoke() {
			// Create a new editor only if none is opened,
			// otherwise bring existing one to the front
			if (f != null)
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						f.toFront();
						f.repaint();
					}
				});
			else {
				AssignmentTablePanel lt = new AssignmentTablePanel(wfe.getView().getWorkflow(), wfe.getDatabase(), rds, fr);
				f = new JFrame("Assignment table");
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent arg0) {
						f = null;
					}
				});
				f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				f.setContentPane(lt);
				f.pack();
				f.setLocationRelativeTo(null);
				// find another way, maybe use the editor's main component
				// wfe.getApplication().getWindowSystem().getMainWindow()
				f.setVisible(true);

			}
		}

	}

	public AssignmentTableToolFactory(Application app, Factory<DataSource, ElementSelector> fr, RootDataSource rds) {
		this.app = app;
		this.fr = fr;
		this.rds = rds;
	}

	@Override
	public Object instantiate(final WorkflowEditor wfe) {
		final OpenAssignmentTableAction a = new OpenAssignmentTableAction(wfe);
		wfe.addAction(a, "application-vnd.sun.xml.calc", KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK), 7);

		// disables assignment table if database is empty
		final DatabaseListener<Database> listener = new DatabaseListener<Database>() {
			private boolean active = true;

			@Override
			public void cursorChange(Database d) {
			}

			@Override
			public void dataChange(Database d, Integer key) {
				if (active && d.getSize() == 0) {
					wfe.disableAction(a);
					active = false;
				} else if (!active && d.getSize() > 0) {
					wfe.enableAction(a);
					active = true;
				}
			}

			@Override
			public void nameChange(Database d) {
			}
		};
		a.databaseObserver = new Observer<DatabaseEvent<Database>>() {

			@Override
			public void notify(DatabaseEvent<Database> event) {
				event.doNotify(listener);
			}

		};
		wfe.getDatabase().getObservable().addObserver(a.databaseObserver);

		// initialize button
		listener.dataChange(wfe.getDatabase(), null);

		return a;
	}

	@Override
	public String getId() {
		return "Assignment table editor plugin";
	}
}
