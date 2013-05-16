package org.vanda.render.jgraph;

import org.vanda.workflows.elements.RendererAssortment;
import com.mxgraph.view.mxStylesheet;

public interface LayoutManagerFactoryInterface {
	public RendererAssortment<LayoutManagerInterface> getRendererAssortment();
	public abstract mxStylesheet getStylesheet();
}
