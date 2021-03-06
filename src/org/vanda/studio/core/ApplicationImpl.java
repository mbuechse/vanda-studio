/**
 * 
 */
package org.vanda.studio.core;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.types.Type;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Message;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;

/**
 * @author buechse
 * 
 */
public final class ApplicationImpl implements Application {

	protected UIMode mode;
	protected final ArrayList<UIMode> modes;
	// to keep a reference to the ModuleManager 
	protected ModuleManager moduleManager;
	protected final MultiplexObserver<Message> messageObservable;
	protected final MultiplexObserver<Application> modeObservable;
	protected final MultiplexObserver<Application> shutdownObservable;
	protected final WindowSystemImpl windowSystem;
	protected final HashSet<Type> types;
	protected final Observer<Tool> typeObserver;
	protected final Properties properties;

	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/studio.conf";

	public ApplicationImpl() {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		messageObservable = new MultiplexObserver<Message>();
		mode = modes.get(0);
		// converterToolRepository = new CompositeRepository<Tool>();
		modeObservable = new MultiplexObserver<Application>();
		shutdownObservable = new MultiplexObserver<Application>();
		windowSystem = new WindowSystemImpl(this);
		properties = new Properties();
		try {
			properties.loadFromXML(new FileInputStream(PROPERTIES_FILE));
		} catch (Exception e) {
			sendMessage(new ExceptionMessage(e));
		}
		
		types = new HashSet<Type>();
		typeObserver = new Observer<Tool>() {

			@Override
			public void notify(Tool event) {
				for (Port p : event.getInputPorts()) {
					p.getType().insertInto(types);
				}
				for (Port p : event.getOutputPorts()) {
					p.getType().insertInto(types);
				}
			}

		};
		// toolRepository.getAddObservable().addObserver(typeObserver);
		
		// Register a Monospace font that can display all Unicode characters
		try {
			URL url = ClassLoader.getSystemClassLoader().getResource(
					"unifont-5.1.20080907.ttf");
			
			Font monospaceFont = Font.createFont(Font.TRUETYPE_FONT, new File(url.getFile()));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(monospaceFont);
		} catch (Exception e) {
		}
	}

	@Override
	public void shutdown() {
		shutdownObservable.notify(this);
	}

	@Override
	public String createUniqueId() {
		return UUID.randomUUID().toString().toUpperCase();
	}

	@Override
	public Observable<Application> getUIModeObservable() {
		return modeObservable;
	}

	@Override
	public Observable<Application> getShutdownObservable() {
		return shutdownObservable;
	}

	@Override
	public UIMode getUIMode() {
		return mode;
	}

	@Override
	public Collection<UIMode> getUIModes() {
		return modes;
	}

	@Override
	public void setUIMode(UIMode m) {
		if (mode != m && modes.contains(m)) {
			mode = m;
			modeObservable.notify(this);
		}
	}

	@Override
	public WindowSystem getWindowSystem() {
		return windowSystem;
	}

	protected static void addUIModes(Collection<UIMode> modes) {
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Normal Mode";
			}

			@Override
			public boolean isLargeContent() {
				return false;
			}

			@Override
			public boolean isLargeUI() {
				return false;
			}
		});
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Beamer Mode";
			}

			@Override
			public boolean isLargeContent() {
				return true;
			}

			@Override
			public boolean isLargeUI() {
				return false;
			}
		});
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Tablet Mode";
			}

			@Override
			public boolean isLargeContent() {
				return true;
			}

			@Override
			public boolean isLargeUI() {
				return true;
			}
		});
	}

	@Override
	public Observable<Message> getMessageObservable() {
		return messageObservable;
	}

	@Override
	public void sendMessage(Message m) {
		messageObservable.notify(m);
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	@Override
	public String getProperty(String key) {
		if (!properties.containsKey(key)) {
			if (key.equals("inputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/input/");
			if (key.equals("toolsPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/interfaces/");
			if (key.equals("lastInputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/input/");
			if (key.equals("outputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/output/");
		}
		return properties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
		try {
			properties.storeToXML(new FileOutputStream(PROPERTIES_FILE), null);
		} catch (Exception e) {
			sendMessage(new ExceptionMessage(e));
		}
	}
	
	public void setModuleManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}
}
