package org.vanda.workflows.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Util;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.data.Databases.DataChange;

public final class Database {

	private final ArrayList<HashMap<Integer, String>> assignments;
	private final ArrayList<String> assignmentNames;
	private int cursor;
	private final MultiplexObserver<DatabaseEvent<Database>> observable;
	private int update;
	private LinkedList<DatabaseEvent<Database>> events;

	public Database() {
		assignments = new ArrayList<HashMap<Integer, String>>();
		assignmentNames = new ArrayList<String>();
		cursor = 0;
		observable = new MultiplexObserver<DatabaseEvent<Database>>();
		events = new LinkedList<DatabaseEvent<Database>>();
		addRow(false);
	}

	public void beginUpdate() {
		update++;
	}

	public void endUpdate() {
		update--;
		if (update == 0) {
			LinkedList<DatabaseEvent<Database>> ev = events;
			events = new LinkedList<DatabaseEvent<Database>>();
			Util.notifyAll(observable, ev);
		}
	}

	public String get(Integer key) {
		String result = null;
		if (cursor < assignments.size())
			result = assignments.get(cursor).get(key);
		if (result == null)
			result = "";
		return result;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int c) {
		if (cursor != c) {
			beginUpdate();
			try {
				cursor = c;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public String getName() {
		if (cursor < assignmentNames.size()) 
			return assignmentNames.get(cursor);
		else 
			return "";
	}

	public void setName(String name) {
		setName(name, cursor);
	}
	
	public void setName(String name, int i) {
		beginUpdate();
		if (i == assignmentNames.size()) {
			assignmentNames.add(name);
		} else {
			assignmentNames.set(i, name);
		}
		events.add(new Databases.NameChange<Database>(this));
		endUpdate();
	}

	public HashMap<Integer, String> getRow(int location) {
		return assignments.get(location);
	}

	public Map<Integer, String> addRow(boolean copyContent) {
		HashMap<Integer, String> row = new HashMap<Integer, String>();
		beginUpdate();
		try {
			row.put(new Integer(0), Integer.toHexString(new Object().hashCode()));
			if (copyContent) {
				for (Entry<Integer, String> e : assignments.get(cursor).entrySet()) {
					if (e.getKey() != 0) {
						row.put(e.getKey(), e.getValue());
						events.add(new DataChange<Database>(this, e.getKey()));
					}
				}
				// copies current name and adds "(i)" as suffix
				// where i is the smallest integer > 1 that is not used
				String nameProto = assignmentNames.get(cursor);
				if (nameProto.matches(".*[(]\\d+[)]")) {
					int i = nameProto.lastIndexOf('(');
					nameProto = nameProto.subSequence(0, i).toString();
				}
				int i;
				for (i = 2; ; ++i) {
					if (!assignmentNames.contains(nameProto + "(" + i + ")"))
						break;				
				}
				assignmentNames.add(nameProto + "(" + i + ")");
			} else {
				assignmentNames.add("new assignment");
			}
			assignments.add(row);
		} finally {
			endUpdate();
		}
		return row;
	}

	public void delRow() {
		if (getSize() == 1)
			return;
		if (cursor < assignments.size()) {
			beginUpdate();
			try {
				HashMap<Integer, String> theRow = assignments.get(cursor);
				assignments.remove(cursor);
				assignmentNames.remove(cursor);
				for (Entry<Integer, String> e : theRow.entrySet())
					events.add(new DataChange<Database>(this, e.getKey()));
			} finally {
				cursor = 0;
				events.add(new CursorChange<Database>(this));
				endUpdate();
			}
		}
	}

	public Observable<DatabaseEvent<Database>> getObservable() {
		return observable;
	}

	public int getSize() {
		return assignments.size();
	}

	public boolean hasNext() {
		return cursor < assignments.size();
	}

	public boolean hasPrev() {
		return cursor > 0;
	}

	public void home() {
		setCursor(0);
	}

	public void next() {
		if (cursor < assignments.size()) {
			beginUpdate();
			try {
				cursor++;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void prev() {
		if (cursor > 0) {
			beginUpdate();
			try {
				cursor--;
				events.add(new CursorChange<Database>(this));
			} finally {
				endUpdate();
			}
		}
	}

	public void put(Integer key, String value) {
		Map<Integer, String> row;
		beginUpdate();
		try {
			String oldvalue = null;
			if (value == null) {
				if (cursor < assignments.size())
					oldvalue = assignments.get(cursor).remove(key);
			} else {
				if (cursor == assignments.size()) {
					row = addRow(false);
					/*m = new HashMap<Integer, String>();
					m.put(new Integer(0), Integer.toHexString(new Object().hashCode()));
					assignments.add(m);
					assignmentNames.add("");*/
				} else
					row = assignments.get(cursor);
				oldvalue = row.put(key, value);
			}
			if (value != oldvalue)
				events.add(new DataChange<Database>(this, key));
		} finally {
			endUpdate();
		}
	}

	public String getName(int i) {
		return assignmentNames.get(i);
	}

}
