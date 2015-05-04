package org.vanda.studio.modules.datasources;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.serialization.DataSourceType;
import org.vanda.datasources.serialization.Loader;
import org.vanda.datasources.serialization.Storer;
import org.vanda.util.AbstractRepository;
import org.vanda.util.Util;

public class DataSourceRepository extends AbstractRepository<String, DataSourceMount> {

	private final String path;
	private final List<DataSourceType<?>> dataSourceTypes;
	
	public DataSourceRepository(String path) {
		super();
		this.path = path;
		this.dataSourceTypes = new LinkedList<DataSourceType<?>>();
	}
	
	public void addDataSourceType(DataSourceType<?> dst) {
		dataSourceTypes.add(dst);
	}

	public void addItem(DataSourceMount newitem) {
		DataSourceMount item = items.remove(newitem.getId());
		if (item != newitem)
			removeObservable.notify(item);
		items.put(newitem.getId(), newitem);
		if (item != newitem)
			addObservable.notify(newitem);		
	}
	
	public void removeItem(String id) {
		DataSourceMount item = items.remove(id);
		if (item != null) {
			removeObservable.notify(item);
		}
	}

	@Override
	public void refresh() {
		List<DataSourceMount> newitems = null;
		Loader l = new Loader(dataSourceTypes);
		try {
			newitems = l.load(path);
		} catch (Exception e) {
			return;
		}
		Util.notifyAll(removeObservable, items.values());
		items.clear();
		for (DataSourceMount e : newitems)
			items.put(e.id, e);
		Util.notifyAll(addObservable, items.values());
	}
	
	public void store() {
		Path p1 = (new File(path)).toPath();
		Path p2 = (new File(path + ".backup")).toPath();
		try {
			Files.move(p1, p2, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			// app.sendMessage(new ExceptionMessage(e));
		}

		try {
			new Storer(dataSourceTypes).store(items.values(), path);
		} catch (Exception e) {
			//app.sendMessage(new ExceptionMessage(e));
		}
		
	}
	
}
