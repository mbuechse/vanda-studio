package org.vanda.studio.modules.build;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.vanda.fragment.model.Profile;
import org.vanda.studio.app.Application;
import org.vanda.util.Action;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.util.RepositoryItem;

public final class ProfileManager {

	private final Application app;
	// private final SimpleRepository<Profile> repository;
	private JPanel contentPane;
	private final ProfileOpener profOpener;

	public ProfileManager(Application app, Repository<String, Profile> repository, ProfileOpener profOpener) {
		this.app = app;
		this.profOpener = profOpener;
		// this.repository = repository;
		JList<String> list = new JList<String>(new RepositoryListModel<Profile>(repository));
		JScrollPane listScroll = new JScrollPane(list);
		contentPane = new JPanel(new BorderLayout());
		contentPane.add(listScroll, BorderLayout.WEST);
		contentPane.setName("Fragment Profiles");
		app.getWindowSystem().addAction(contentPane, new Action() {
			@Override
			public String getName() {
				return "Test";
			}

			@Override
			public void invoke() {

			}
		}, null);
		app.getWindowSystem().addContentWindow(null, contentPane,
				new CloseAction());

	}
	
	public void focus() {
		app.getWindowSystem().focusContentWindow(contentPane);
	}

	private final class CloseAction implements Action {
		@Override
		public String getName() {
			return "Close";
		}

		@Override
		public void invoke() {
			app.getWindowSystem().removeContentWindow(contentPane);
			contentPane = null;
			profOpener.closeManager();
		}
	}

	private static final class RepositoryListModel<T extends RepositoryItem>
			implements ListModel<String>, Observer<T> {

		private final Repository<String, T> repository;
		private List<T> items;
		private final Set<ListDataListener> listeners;

		public RepositoryListModel(Repository<String, T> repository) {
			this.repository = repository;
			listeners = new HashSet<ListDataListener>();
			items = new ArrayList<T>(repository.getItems());
			repository.getAddObservable().addObserver(this);
			repository.getRemoveObservable().addObserver(this);
			repository.getModifyObservable().addObserver(this);
		}

		@Override
		public int getSize() {
			return repository.getItems().size();
		}

		@Override
		public String getElementAt(int index) {
			return items.get(index).getName();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		@Override
		public void notify(T event) {
			for (ListDataListener l : listeners)
				l.contentsChanged(new ListDataEvent(this,
						ListDataEvent.INTERVAL_REMOVED, 0, items.size()));
			items.clear();
			items.addAll(repository.getItems());
			for (ListDataListener l : listeners)
				l.contentsChanged(new ListDataEvent(this,
						ListDataEvent.INTERVAL_ADDED, 0, items.size()));
		}

	}
	
	public interface ProfileOpener {
		void closeManager();
	}

}
