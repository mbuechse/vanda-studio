package org.vanda.studio.modules;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.WindowSystem;
import org.vanda.util.Action;
import org.vanda.util.Message;
import org.vanda.util.Observer;

public class MessageModule implements Module {

	@Override
	public String getId() {
		return "Messages Module for Vanda Studio";
	}

	@Override
	public Object instantiate(Application a) {
		return new Messages(a);
	}

	private static final class Messages implements Observer<Message> {
		private final Application app;
		private final JList messageList;
		private final JScrollPane scrollPane;
		private final DefaultListModel listModel;

		@SuppressWarnings("serial")
		public Messages(Application a) {
			app = a;
			listModel = new DefaultListModel() {
				@Override
				public Object getElementAt(int index) {
					Message m = (Message) super.getElementAt(index);
					return "["
							+ DateFormat.getTimeInstance().format(m.getDate())
							+ "] " + m.getHeadline();
				}

			};
			messageList = new JList(listModel);
			messageList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						/*int idx1 = e.getFirstIndex();
						int idx2 = e.getLastIndex();

						int sel, desel;
						if (messageList.getSelectedIndex() == idx1) {
							sel = idx1;
							desel = idx2;
						} else {
							sel = idx2;
							desel = idx1;		
						}*/
					
						// ((Message) listModel.get(sel)).onSelect(app);
						// ((Message) listModel.get(desel)).onDeselect(app);
					}
				}

			});
			scrollPane = new JScrollPane(messageList);
			scrollPane.setName("Messages");
			app.getWindowSystem().addToolWindow(null, null, scrollPane, WindowSystem.NORTHEAST);
			app.getMessageObservable().addObserver(this);
			app.sendMessage(new Message() {

				final Date d = new Date();

				@Override
				public void appendActions(List<Action> as) {

				}

				@Override
				public String getHeadline() {
					return "Welcome to Vanda Studio!";
				}

				@Override
				public String getMessage() {
					return null;
				}

				@Override
				public Date getDate() {
					return d;
				}

			});
		}

		@Override
		public void notify(Message event) {
			listModel.add(0, event);
			messageList.setSelectedIndex(0);
			app.getWindowSystem().focusToolWindow(scrollPane);
		}

	}

}
