package org.vanda.fragment.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentBase;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.types.Type;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ElementVisitor;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class GeneratorImpl implements Generator, FragmentIO {

	private Profile prof;

	// The Generator class encapsulates stuff that should not be kept around
	// all the time, and resource acquisition is initialization, blah blah
	protected class GenerationProcess implements FragmentBase {

		private final WeakHashMap<Object, Fragment> map;
		private final HashMap<String, Fragment> fragments; // i.e., functions
		private final Map<String, Integer> uniqueMap;
		private final Map<Object, String> uniqueStrings;

		public GenerationProcess() {
			uniqueMap = new HashMap<String, Integer>();
			uniqueStrings = new HashMap<Object, String>();
			map = new WeakHashMap<Object, Fragment>();
			fragments = new HashMap<String, Fragment>();
		}

		public Fragment generateAtomic(Tool t) {
			Fragment result = map.get(t.getId());
			if (result == null) {
				result = prof.getFragmentToolMetaRepository().getRepository().getItem(t.getId());
				assert (result != null);
				// TODO this ^^ should be guaranteed via tool interfaces
				map.put(t, result);
				fragments.put(result.getId(), result);
			}
			return result;
		}

		public String generateFragment(MutableWorkflow w, SyntaxAnalysis synA, SemanticAnalysis semA)
				throws IOException {
			Fragment result = map.get(w);
			if (result == null) {
				String name = makeUnique(w.getName(), w);
				assert (synA.getFragmentType() != null);
				FragmentCompiler fc = prof.getCompiler(synA.getFragmentType());
				assert (fc != null);
				Job[] sorted = synA.getSorted();
				final ArrayList<Fragment> frags = new ArrayList<Fragment>(sorted.length);
				for (final Job ji : sorted) {
					ji.visit(new ElementVisitor() {
						@Override
						public void visitLiteral(Literal l) {
							frags.add(null);
						}

						@Override
						public void visitTool(final Tool t) {
							frags.add(generateAtomic(t));
						}
					});
				}
				result = fc.compile(name, synA, semA, frags, GeneratorImpl.this);
				assert (result != null);
				map.put(w, result);
				this.fragments.put(result.getId(), result);
			}
			return result.getId();
		}

		public String generate(MutableWorkflow ewf, SyntaxAnalysis synA, SemanticAnalysis semA) throws IOException {
			String root = generateFragment(ewf, synA, semA);
			return prof.getRootLinker(prof.getRootType()).link(root, null, null, null, null, this, GeneratorImpl.this).getId();
		}

		public String makeUnique(String prefix, Object key) {
			String result = uniqueStrings.get(key);
			if (result == null) {
				Integer n = uniqueMap.get(prefix);
				if (n == null)
					n = new Integer(0);
				uniqueMap.put(prefix, new Integer(n.intValue() + 1));
				result = prefix + "$" + n.toString();
				uniqueStrings.put(key, result);
			}
			return result;
		}

		@Override
		public Fragment getFragment(String name) {
			return fragments.get(name);
		}

	}

	@Override
	public String generate(MutableWorkflow ewf, SyntaxAnalysis synA, SemanticAnalysis semA) throws IOException {
		return new GenerationProcess().generate(ewf, synA, semA);
	}

	public GeneratorImpl(Profile prof) {
		this.prof = prof;
	}

	@Override
	public File createFile(String name) throws IOException {
		// TODO use path from runconfig (and use pathSeparator)
		// File result = new File(app.getProperty("outputPath") + name);
		File result = new File(System.getProperty("user.home") + "/.vanda/output/" + name);
		result.createNewFile();
		return result;
	}

	@Override
	public String findFile(String value) {
		return value;  // TODO use path from runconfig
	}

	@Override
	public Type getRootType() {
		return prof.getRootType();
	}

}
