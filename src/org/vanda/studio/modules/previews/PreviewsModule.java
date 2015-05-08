package org.vanda.studio.modules.previews;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.AbstractRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.PreviewFactory;

public class PreviewsModule implements Module {

	private final MetaRepository<Type, PreviewFactory> previewFactoriesMeta;

	public PreviewsModule(MetaRepository<Type, PreviewFactory> previewFactoriesMeta) {
		this.previewFactoriesMeta = previewFactoriesMeta;
	}

	@Override
	public String getName() {
		return "Preview Module";
	}

	private static final class StaticRepository extends AbstractRepository<Type, PreviewFactory> {
		public void put(Type key, PreviewFactory value) {
			items.put(key, value);
		};
	}

	@Override
	public Object createInstance(Application app) {
		StaticRepository ar = new StaticRepository();
		ar.put(new CompositeType("PennTreeCorpus"), new BerkeleyTreePreviewFactory(app));
		ar.put(new CompositeType("BerkeleyGrammar.sm6"), new BerkeleyGrammarPreviewFactory(app, ".prev"));
		ar.put(new CompositeType("LAPCFG-Grammar"), new BerkeleyGrammarPreviewFactory(app, "/level1.grammar"));
		ar.put(new CompositeType("EMSteps"), new DictionaryPreviewFactory(app));
		ar.put(new CompositeType("Scores"), new ScoresPreviewFactory(app));
		ar.put(new CompositeType("Alignments"), new AlignmentsPreviewFactory(app));
		ar.put(new CompositeType("log"), new LogPreviewFactory());
		ar.put(null, new MonospacePreviewFactory(app));
		previewFactoriesMeta.addRepository(ar);
		return ar;
	}

}
