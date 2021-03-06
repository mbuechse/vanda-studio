package org.vanda.studio.modules;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.previews.AlignmentsPreviewFactory;
import org.vanda.studio.modules.previews.BerkeleyTreePreviewFactory;
import org.vanda.studio.modules.previews.LogPreviewFactory;
import org.vanda.studio.modules.previews.MonospacePreviewFactory;
import org.vanda.studio.modules.previews.ScoresPreviewFactory;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.AbstractRepository;
import org.vanda.util.Factory;
import org.vanda.util.MetaRepository;

public class PreviewsModule implements Module {

	private final MetaRepository<Type, Factory<String, JComponent>> previewFactoriesMeta;

	public PreviewsModule(MetaRepository<Type, Factory<String, JComponent>> previewFactoriesMeta) {
		this.previewFactoriesMeta = previewFactoriesMeta;
	}

	@Override
	public String getId() {
		return "Preview Module";
	}

	private static final class StaticRepository extends AbstractRepository<Type, Factory<String, JComponent>> {
		public void put(Type key, Factory<String, JComponent> value) {
			items.put(key, value);
		};
	}

	@Override
	public Object instantiate(Application app) {
		StaticRepository ar = new StaticRepository();
		ar.put(new CompositeType("PennTreeCorpus"), new BerkeleyTreePreviewFactory());
		ar.put(new CompositeType("Scores"), new ScoresPreviewFactory());
		ar.put(new CompositeType("Alignments"), new AlignmentsPreviewFactory());
		ar.put(new CompositeType("log"), new LogPreviewFactory());
		ar.put(null, new MonospacePreviewFactory(app));
		previewFactoriesMeta.addRepository(ar);
		return ar;
	}

}
