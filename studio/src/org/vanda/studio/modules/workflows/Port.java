package org.vanda.studio.modules.workflows;

/**
 * @author afischer
 */
public class Port {
	private String name;
	private EPortType type;
	private boolean inUse;
		
	public Port(String name, EPortType type) {
		this.name = name;
		this.type = type;
		this.inUse = false;
	}
	
	public String getName() {
		return this.name;
	}
	
	public EPortType getType() {
		return this.type;
	}
	
	public boolean isInUse() {
		return this.inUse;
	}
	
	/**
	 * Checks whether the current port and the specified port are compatible, 
	 * i.e. they share the same type
	 * 
	 * @param otherPort
	 * @return true iff both ports are compatible
	 */
	public boolean isCompatibleTo(Port otherPort) {
		return name.equals(otherPort.name) && type.equals(otherPort.type);
	}
		
	@Override
	public boolean equals(Object other) {
		boolean result = (other != null && other instanceof Port);
		if (result) {
			Port op = (Port)other;
			result = name.equals(op.name) && type.equals(op.type);
		}
		return result;
	}
}
