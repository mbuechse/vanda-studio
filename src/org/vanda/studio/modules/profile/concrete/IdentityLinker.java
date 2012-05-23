package org.vanda.studio.modules.profile.concrete;

import java.util.List;

import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;

public class IdentityLinker implements FragmentLinker {

	@Override
	public String getCategory() {
		return "Boxes";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "This box can be used to contain any kind of subworkflow.";
	}

	@Override
	public String getId() {
		return Profile.identityLinker;
	}

	@Override
	public String getName() {
		return "Identity Box";
	}

	@Override
	public String getVersion() {
		return "n/a";
	}

	@Override
	public List<String> convertInputs(List<String> outer) {
		return outer;
	}

	@Override
	public List<String> convertOutputs(List<String> inner) {
		return inner;
	}

	@Override
	public Fragment link(String name, FragmentBase fb, FragmentIO io) {
		return fb.getFragment(name);
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
	}

}