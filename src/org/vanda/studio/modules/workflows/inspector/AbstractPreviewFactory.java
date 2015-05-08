package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.types.Type;
import org.vanda.util.PreviewFactory;
import org.vanda.util.Repository;

public interface AbstractPreviewFactory {
	JComponent createPreview(Repository<Type, PreviewFactory> previewFactories);
	// JComponent createButtons(Repository<Type, PreviewFactory> previewFactories);
	// TODO void addActions(...);
}