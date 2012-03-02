package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

/**
 * Nested composite of IHyperworkflow composite pattern
 * @author afischer
 *
 */
public class NestedHyperworkflow implements IHyperworkflow{

	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<IHyperworkflow, List<Port>> portBlockageMap;
	
	private List<Connection> connections;
	private List<IHyperworkflow> children;
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		connections = new ArrayList<Connection>();
		children = new ArrayList<IHyperworkflow>();
	}
	
	public NestedHyperworkflow(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	/**
	 * Copy constructor
	 * @param toCopy
	 */
	public NestedHyperworkflow(NestedHyperworkflow toCopy) {
		this(toCopy.parent, toCopy.name, toCopy.id, new ArrayList<Port>(toCopy.inputPorts), new ArrayList<Port>(toCopy.outputPorts));
		
//		children = new ArrayList<IHyperworkflow>(toCopy.children);
		children = new ArrayList<IHyperworkflow>();
		for (IHyperworkflow child : toCopy.children) {
			IHyperworkflow childCopy = null;
			//ensure that an or-child is copied and gets the current NestedHyperworkflow copy as parent
			if (child instanceof Or) childCopy = new Or((Or)child, this);	
			else childCopy = child;		//other children are just re-used
			children.add(childCopy);
		}
		
		connections = new ArrayList<Connection>(toCopy.connections);
		
		//copy the portBlockageMap, has to be done semi-manually since "new HashMap(toCopy.portBlockageMap)" only does a shallow copy and thus, refers to the SAME entry lists 
		portBlockageMap = new HashMap<IHyperworkflow, List<Port>>();
		for (IHyperworkflow hwf : toCopy.portBlockageMap.keySet()) {
			portBlockageMap.put(hwf, new ArrayList<Port>(toCopy.portBlockageMap.get(hwf)));
		}
	}
	
	/** 
	 * Copy constructor that sets the parent of the copy to another NestedHyperworkflow
	 * @param toCopy
	 */
	public NestedHyperworkflow(NestedHyperworkflow toCopy, NestedHyperworkflow newParent) {
		this(toCopy);
		parent = newParent;
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public Map<IHyperworkflow, List<Port>> getPortBlockageMap() { return portBlockageMap; }
	public int getId() {	return id; }
	public List<Port> getInputPorts() { return inputPorts;	}
	public String getName() { return name; }
	
	/** @return a list of connections */
	public List<Connection> getConnections() { return connections; 	}

	/** @return a list of direct Hyperworkflow children of the current NestedHyperworkflow */
	public List<IHyperworkflow> getChildren() {	return children; }
	
	/**
	 * Adds a new connection to the NestedHyperworkflow's connections-List.
	 * The connection must not exist already, the connection source and target has to be child of the current NestedHyperworkflow 
	 * or the NestedHyperworkflow itself and the target port must not be blocked by another connection
	 * @param conn - the Connection to add
	 * @return true, if adding was successful
	 */
	public boolean addConnection(Connection conn) {
		//TODO infer types as far as possible if conn now blocks a generic port
		//TODO prevent cycles by connection adding
		
		//check for null reference, ensure connection does not already exist, check port compatibility
		if (conn != null && !connections.contains(conn) && conn.getSrcPort().isCompatibleTo(conn.getTargPort())) {
			
			//ensure that source is NestedHyperworkflow itself or a child and has the specified source port
			if ((children.contains(conn.getSource()) && conn.getSource().getOutputPorts().contains(conn.getSrcPort())) || 
					(conn.getSource().equals(this) && this.getInputPorts().contains(conn.getSrcPort()))) {
				//ensure target is a child or current NestedHyperworkflow itself and has the specified target port
				if ((children.contains(conn.getTarget()) && conn.getTarget().getInputPorts().contains(conn.getTargPort())) || 
						(conn.getTarget().equals(this) && this.getOutputPorts().contains(conn.getTargPort()))) {
					
					//check if target port is not blocked already
					Connection targetBlocked;
					if (!portBlockageMap.containsKey(conn.getTarget())) portBlockageMap.put(conn.getTarget(), new ArrayList<Port>());
					List<Port> blockedPorts = portBlockageMap.get(conn.getTarget());
					if (!blockedPorts.contains(conn.getTargPort())) targetBlocked = null;
					else targetBlocked = new Connection(null, null, null, null); 
					
					//if targetBlocked is null, the port is still open
					if (targetBlocked == null) {
						
						//try to add connection and block the previously free target input port
						if (connections.add(conn) && portBlockageMap.get(conn.getTarget()).add(conn.getTargPort())) {

							//if the connection is only between two simple Elements remove the now occupied ports from current NestedHyperworkflow
							if (conn.getSource() instanceof IElement && conn.getTarget() instanceof IElement) {
								List<Port> emptyList = new ArrayList<Port>();
								List<Port> inputs = new ArrayList<Port>();
								inputs.add(conn.getTargPort());
								List<Port> outputs = new ArrayList<Port>();
								outputs.add(conn.getSrcPort());
								//actual port removal from parent Hyperworkflows
								if (!(this.propagatePortRemoval(conn.getSource(), emptyList, outputs) && this.propagatePortRemoval(conn.getTarget(), inputs, emptyList))) {
									//propagation failed -> UNDO EVERYTHING
									this.propagateAdditionalPorts(conn.getSource(), emptyList, outputs);
									this.propagateAdditionalPorts(conn.getTarget(), inputs, emptyList);
									
									//remove previously blocked port from portBlockageMap
									portBlockageMap.get(conn.getTarget()).remove(conn.getTargPort());
									
									//if there are no more blocked ports for the target tool, remove its map entries completely
									if (portBlockageMap.get(conn.getTarget()) != null && portBlockageMap.get(conn.getTarget()).isEmpty())
										portBlockageMap.remove(conn.getTarget());

									connections.remove(conn);
									return false;
								}
							}

							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Removes an existing  connection from the NestedHyperworkflow's connections-List
	 * as well as the blockage flag of the connection's target port
	 * @param conn - the Connection to remove
	 * @return true, if removal was successful
	 */
	public boolean removeConnection(Connection conn) {
		//TODO infer types as far as possible if the removal of conn frees an otherwise generic port
		
		//check for null reference and make sure the connection exists
		if (conn != null && connections.contains(conn)) {
			
			//try to remove connection and free the previously blocked target input port
			if (connections.remove(conn) && portBlockageMap.get(conn.getTarget()).remove(conn.getTargPort())) {
				
				//if the connection is only between two simple Elements add the now free ports to current NestedHyperworkflow
				if (conn.getSource() instanceof IElement && conn.getTarget() instanceof IElement) {
					List<Port> emptyList = new ArrayList<Port>();
					List<Port> inputs = new ArrayList<Port>();
					inputs.add(conn.getTargPort());
					List<Port> outputs = new ArrayList<Port>();
					outputs.add(conn.getSrcPort());
					//actual port adding to parent Hyperworkflows
					if (!(this.propagateAdditionalPorts(conn.getSource(), emptyList, outputs) && this.propagateAdditionalPorts(conn.getTarget(), inputs, emptyList))) {
						//propagation failed -> UNDO EVERYTHING
						this.propagatePortRemoval(conn.getSource(), emptyList, outputs);
						this.propagatePortRemoval(conn.getTarget(), inputs, emptyList);
						portBlockageMap.get(conn.getTarget()).add(conn.getTargPort());
						connections.add(conn);
						return false;
					}
				}
				
				//if there are no more blocked ports for the target tool, remove its map entries completely
				if (portBlockageMap.get(conn.getTarget()) != null && portBlockageMap.get(conn.getTarget()).isEmpty())
					portBlockageMap.remove(conn.getTarget());
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a Hyperworkflow child to the NestedHyperworkflow
	 * @param hwf - child to add
	 * @return true, if adding was successful
	 */
	public boolean addChild(IHyperworkflow hwf) {
		//check for null reference and make sure the new child does not exist already
		if (hwf != null && !children.contains(hwf)) {
			
			//add child if possible
			if (children.add(hwf)) {
				//check for necessary creation of new inner ports
				if (propagateAdditionalPorts(hwf, hwf.getInputPorts(), hwf.getOutputPorts())) return true;
				else {
					//undo everything
					children.remove(hwf);
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Removes a Hyperworkflow child from the NestedHyperworkflow,
	 * all associated connections, and, if present, nested children
	 * @param hwf- child to remove
	 * @return true, if removal was successful
	 */
	public boolean removeChild(IHyperworkflow hwf) {
		if (hwf != null && children.contains(hwf)) {
			
			//partition connections into incoming and outgoing
			List<Connection> incoming = new ArrayList<Connection>();
			List<Connection> outgoing = new ArrayList<Connection>();
			for (Connection c : connections) {
				if (c.getTarget().equals(hwf)) incoming.add(c);
				if (c.getSource().equals(hwf)) outgoing.add(c);
			}
			
			//remove incoming connections
			while (incoming.size() > 0) {
				removeConnection(incoming.remove(0));
			}
			
			//remove outgoing connections
			while (outgoing.size() > 0) {
				removeConnection(outgoing.remove(0));
			}
			
			//remove child if possible
			if (children.remove(hwf)) {
				//check for necessary removal of new inner ports
				if (propagatePortRemoval(hwf, hwf.getInputPorts(), hwf.getOutputPorts())) return true;
				else {
					//undo everything
					children.add(hwf);
					for (Connection c : incoming) this.addConnection(c);
					for (Connection c : outgoing) this.addConnection(c);
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Adds the specified ports of a given Hyperworkflow hwf to the current NestedHyperworkflow and all its parents
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole adding process is undone
	 */
	private boolean propagateAdditionalPorts(IHyperworkflow hwf, List<Port> additionalInputs, List<Port> additionalOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		//create new input ports based on the specified ports of a child node
		for (Port p : additionalInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
		}
		//create new output ports based on the specified ports of a child node
		for (Port p : additionalOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (!this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
		}
		
		//add the new input and output ports
		this.getInputPorts().addAll(innerInputPorts);
		this.getOutputPorts().addAll(innerOutputPorts);
		
		//propagate the port adding to own parent
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			//all went well, thus the port adding was successful
			return true;
		} else {
			//some parent had a problem with the adding propagation -> UNDO EVERYTHING
			this.getInputPorts().removeAll(innerInputPorts);		//remove previously added input ports
			this.getOutputPorts().removeAll(innerOutputPorts);	//remove previously added output ports
			return false;
		}
	}
	
	/**
	 * Removes the specified ports of a given Hyperworkflow hwf from the current NestedHyperworkflow and all its parents 
	 * (along with all connections that are somehow linked to those ports)
	 * @param hwf
	 * @return true, if propagation process is successful. On occurrence of any problem the whole removal process is undone
	 */
	private boolean propagatePortRemoval(IHyperworkflow hwf, List<Port> removedInputs, List<Port> removedOutputs) {
		List<Port> innerInputPorts = new ArrayList<Port>();
		List<Port> innerOutputPorts = new ArrayList<Port>();
		
		if (this.getParent() == null) return true;
		
		//find inner input ports that will be removed
		for (Port p : removedInputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getInputPorts().contains(newPort)) innerInputPorts.add(newPort);
			else return false;
		}
		//find inner output ports that will be removed
		for (Port p : removedOutputs) {
			Port newPort = new Port(hwf.getName()+"."+p.getName(), p.getType());
			if (this.getOutputPorts().contains(newPort)) innerOutputPorts.add(newPort);
			else return false;
		}
		
		//get all incoming and outgoing connections that are linked to the soon-to-be-removed ports
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : parent.getConnections()) {
			if (c.getTarget().equals(this) && innerInputPorts.contains(c.getTargPort())) incoming.add(c);
			if (c.getSource().equals(this) && innerOutputPorts.contains(c.getSrcPort())) outgoing.add(c);
		}
		
		//delete incoming connection to input ports that will be removed
		for (Connection c : incoming) {
			parent.removeConnection(c);
		}
		//delete outgoing connections from output ports that will be removed
		for (Connection c : outgoing) {
			parent.removeConnection(c);
		}
		
		//remove inner input and output ports
		this.getInputPorts().removeAll(innerInputPorts);
		this.getOutputPorts().removeAll(innerOutputPorts);
		
		//propagate the port removal to own parent
		if (getParent().propagateAdditionalPorts(this, innerInputPorts, innerOutputPorts)) {
			//all went well, thus the port removal was successful
			return true;
		} else {
			//some parent had a problem with the removal propagation -> UNDO EVERYTHING
			this.getInputPorts().addAll(innerInputPorts);	//add previously removed input ports
			this.getOutputPorts().addAll(innerOutputPorts);	//add previously removed output ports
			for (Connection c : incoming) parent.addConnection(c);	//add previously removed incoming connections
			for (Connection c : outgoing) parent.addConnection(c);	//add previously removed outgoing connections
			return false;	//report failure
		}
	}
	
	@Override
	public boolean equals(Object other) {
		//NestedHyperworkflows are equal if they have the same attributes (parent is ignored and not compared)
		boolean result = (other != null && other instanceof NestedHyperworkflow);
		if (result) {
			NestedHyperworkflow oh = (NestedHyperworkflow)other;
			result = (	getId() == oh.getId() &&
							getName().equals(oh.getName()) &&
							getChildren().equals(oh.getChildren()) &&
							getConnections().equals(oh.getConnections()) &&
							getInputPorts().equals(oh.getInputPorts()) &&
							getOutputPorts().equals(oh.getOutputPorts()) &&
							getPortBlockageMap().equals(oh.getPortBlockageMap())	);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name + ": " + children + ", " + connections;
	}
	
	//Map<NestedHyperworkflow, Collection<IHyperworkflow>> unfoldMap;
	
	@Override
	public Collection<IHyperworkflow> unfold() {
		Map<NestedHyperworkflow, Collection<IHyperworkflow>> unfoldMap = new HashMap<NestedHyperworkflow, Collection<IHyperworkflow>>();
		int orCount = 0;
		
		for (IHyperworkflow child : this.children) {
			//unfold all nested children and write results into a map
			if (child instanceof NestedHyperworkflow) {
				Collection<IHyperworkflow> unfoldResult = child.unfold();
				unfoldMap.put((NestedHyperworkflow)child, unfoldResult);
			}
			//count number of or-nodes
			if (child instanceof Or) orCount++;
		}
		
		//-------------------------------------------------------------------------------------------------------------
		//--------- remove nested children and replace them by their unfolded versions ---------
		//-------------------------------------------------------------------------------------------------------------
		/*				-----		---------		----
		 * 	hwf1 = | a |-----| 3x b |----| c |	where b is a nested child that represents 3 different workflows
		 * 				-----		---------		----
		 * 		|
		 * 		|
		 * 		--->	-----		---------		----
		 * 				| a |-----|   b1  |----| c |	= hwf1_1
		 * 				-----		---------		----
		 * 				-----		---------		----
		 * 				| a |-----|   b2  |----| c |	= hwf1_2
		 * 				-----		---------		----
		 * 				-----		---------		----
		 * 				| a |-----|   b3  |----| c |	= hwf1_3
		 * 				-----		---------		----
		 */
		List<IHyperworkflow> hwfList = new ArrayList<IHyperworkflow>();	//working list that contains intermediate unfolding results
		hwfList.add(new NestedHyperworkflow(this));	//put current NestedHyperworkflow in the list to start unfolding
		//iterate over all nested children
		for (NestedHyperworkflow nested : unfoldMap.keySet()) {
			//current size of working list
			int workingListSize = hwfList.size();
			//iterate over all elements (intermediate results) of the working list in reverse order (allows manipulation of the list during iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {
					
				//get incoming and outgoing connections
				List<Connection> incoming = new ArrayList<Connection>();
				List<Connection> outgoing = new ArrayList<Connection>();
				for (Connection c : ((NestedHyperworkflow)(hwfList.get(i))).getConnections()) {
					if (c.getTarget().equals(nested)) incoming.add(c);
					if (c.getSource().equals(nested)) outgoing.add(c);
				}
					
				//iterate over all unfolding results of the current nested child
				for (IHyperworkflow unfoldedChild : unfoldMap.get(nested)) {
					NestedHyperworkflow copy = new NestedHyperworkflow((NestedHyperworkflow)hwfList.get(i)); //create copy of original workflow
						
					copy.removeChild(nested);		//remove original folded child from current copy
					copy.addChild(unfoldedChild);	//add unfolded child to replace the removed original
					for (Connection c : incoming) {
						//add input connections
						copy.addConnection(new Connection(c.getSource(), c.getSrcPort(), unfoldedChild, c.getTargPort()));
					}
					for (Connection c : outgoing) {
						//add output connections
						copy.addConnection(new Connection(unfoldedChild, c.getSrcPort(), c.getTarget(), c.getTargPort()));
					}
					
					//add changed copy (see hwf1_i in the figure above) to the end of the result list if it does not already exist
					if (!hwfList.contains(copy)) hwfList.add(copy);	
				}
				
				//remove original (folded) workflow from list (see hwf1 in the figure above)
				hwfList.remove(i);	
			}
		}
		
		//-------------------------------------------------------------------------------------------------------------
		//----------------------------------------- unfold Or nodes ---------------------------------------------
		//-------------------------------------------------------------------------------------------------------------
		/*				-----		-----------									-----		----
		 * 	hwf1 = | a |-----|			|		-----						| a |-----| c |	= hwf1_1
		 * 				-----		|			|------| c |			=>		-----		----
		 * 				-----		| OR_1	|		-----						-----		----
		 * 				| b |-----|			|									| b |-----| c |	= hwf1_2
		 * 				-----		-----------									-----		----
		 */
		//iterate x times over the working list where x is the number of or nodes in the original NestedHyperworkflow
		for (int x = 0; x < orCount; x++) {
			int workingListSize = hwfList.size();
			//iterate over working list elements in reverse order (allows manipulation of the list during iteration)
			for (int i = workingListSize - 1; i >= 0; i--) {
				//get first or-node of current element
				Or firstOr = null;
				for (IHyperworkflow node : ((NestedHyperworkflow)(hwfList.get(i))).getChildren()){
					if (node instanceof Or) {
						firstOr = (Or)node;
						break;
					}
				}
				//there is at least one or-node (OR_1 in picture above)
				if (firstOr != null) {
					//unfold the or-node of the current IHyperworkflow copy (return the list containing hwf1_1 and hwf1_2 from picture above)
					Collection<IHyperworkflow> orUnfold = firstOr.unfold();
					for (IHyperworkflow instance : orUnfold) {
						//add all unfold() results that do not already exist
						if (!hwfList.contains(instance)) hwfList.add(instance);
					}
					//remove the original IHyperworkflow from the working list (hwf1 from picture above)
					hwfList.remove(i);
				}
			}
		}
		
		return hwfList;
	}
	
	public static void main(String[] args) {
		//Hyperworkflow parts
		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
			IElement alpha = new Tool(root, "alpha", 1);
				alpha.getOutputPorts().add(new Port("out", EPortType.FILE));
			NestedHyperworkflow beta = new NestedHyperworkflow(root, "beta", 2);
				IElement beta1 = new Tool(beta, "beta1", 21);
					beta1.getInputPorts().add(new Port("in", EPortType.FILE));
					beta1.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement beta2 = new Tool(beta, "beta2", 22);
					beta2.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement beta3 = new Tool(beta, "beta3", 23);
					beta3.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement orBeta = new Or(beta, "orBeta", 24);
					orBeta.getInputPorts().add(new Port("in3", EPortType.GENERIC));
				IElement beta4 = new Tool(beta, "beta4", 25);
					beta4.getInputPorts().add(new Port("in", EPortType.FILE));
					beta4.getOutputPorts().add(new Port("out", EPortType.FILE));
				beta.addChild(beta1);
				beta.addChild(beta2);
				beta.addChild(beta3);
				beta.addChild(orBeta);
				beta.addChild(beta4);
			IElement gamma = new Tool(root, "gamma", 3);
				gamma.getOutputPorts().add(new Port("out", EPortType.FILE));
			IElement or1 = new Or(root, "or1", 4);
			NestedHyperworkflow delta = new NestedHyperworkflow(root, "delta", 5);
				IElement delta1 = new Tool(delta, "delta1", 51);
					delta1.getInputPorts().add(new Port("in", EPortType.FILE));
					delta1.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement delta2 = new Tool(delta, "delta2", 52);
					delta2.getOutputPorts().add(new Port("out", EPortType.FILE));
				IElement orDelta = new Or(delta, "orDelta", 53);
				IElement delta3 = new Tool(delta, "delta3", 54);
					delta3.getInputPorts().add(new Port("in", EPortType.FILE));
					delta3.getOutputPorts().add(new Port("out", EPortType.FILE));
				delta.addChild(delta1);
				delta.addChild(delta2);
				delta.addChild(orDelta);
				delta.addChild(delta3);
			IElement epsilon = new Tool(root, "epsilon", 6);
				epsilon.getOutputPorts().add(new Port("out", EPortType.FILE));
			IElement or2 = new Or(root, "or2", 7);
			IElement eta = new Tool(root, "eta", 8);
				eta.getInputPorts().add(new Port("in", EPortType.FILE));
			root.addChild(alpha);
			root.addChild(beta);
			root.addChild(gamma);
			root.addChild(or1);
			root.addChild(delta);
			root.addChild(epsilon);
			root.addChild(or2);
			root.addChild(eta);
			
		//Connections within beta		
		System.out.println(beta.addConnection(new Connection(beta, beta.getInputPorts().get(0), beta1, beta1.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta1, beta1.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta2, beta2.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(1))));
		System.out.println(beta.addConnection(new Connection(beta3, beta3.getOutputPorts().get(0), orBeta, orBeta.getInputPorts().get(2))));
		System.out.println(beta.addConnection(new Connection(orBeta, orBeta.getOutputPorts().get(0), beta4, beta4.getInputPorts().get(0))));
		System.out.println(beta.addConnection(new Connection(beta4, beta4.getOutputPorts().get(0), beta, beta.getOutputPorts().get(0))));
		
		//Connections within delta
		System.out.println(delta.addConnection(new Connection(delta, delta.getInputPorts().get(0), delta1, delta1.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta1, delta1.getOutputPorts().get(0), orDelta, orDelta.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta2, delta2.getOutputPorts().get(0), orDelta, orDelta.getInputPorts().get(1))));
		System.out.println(delta.addConnection(new Connection(orDelta, orDelta.getOutputPorts().get(0), delta3, delta3.getInputPorts().get(0))));
		System.out.println(delta.addConnection(new Connection(delta3, delta3.getOutputPorts().get(0), delta, delta.getOutputPorts().get(0))));
		
		//Connections within root
		System.out.println(root.addConnection(new Connection(alpha, alpha.getOutputPorts().get(0), beta, beta.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(beta, beta.getOutputPorts().get(0), or1, or1.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(gamma, gamma.getOutputPorts().get(0), or1, or1.getInputPorts().get(1))));
		System.out.println(root.addConnection(new Connection(or1, or1.getOutputPorts().get(0), delta, delta.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(epsilon, epsilon.getOutputPorts().get(0), or2, or2.getInputPorts().get(0))));
		System.out.println(root.addConnection(new Connection(delta, delta.getOutputPorts().get(0), or2, or2.getInputPorts().get(1))));
		System.out.println(root.addConnection(new Connection(or2, or2.getOutputPorts().get(0), eta, eta.getInputPorts().get(0))));
		
		for (IHyperworkflow hwf : root.unfold()) {
			System.out.println(hwf);
		}
		
//		//-------------------------------------------------------------------------------------------------------------
//		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
//		IElement t0 = new Tool(root, "t0", 6);
//		NestedHyperworkflow nested = new NestedHyperworkflow(root, "nested", 1);
//		IElement t1 = new Tool(nested, "t1", 2);
//		IElement t2 = new Tool(nested, "t2", 3);
//		IElement t3 = new Tool(nested, "t3", 4);
//		IElement or = new Or(nested, "or", 5);
//		t1.getInputPorts().add(new Port("in", EPortType.FILE));
//		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
//		t2.getOutputPorts().add(new Port("out", EPortType.FILE));
//		t3.getInputPorts().add(new Port("in", EPortType.FILE));
//		t0.getOutputPorts().add(new Port("out", EPortType.FILE));
//		
//		System.out.println("Add children: ");
//		System.out.println("t1: " + nested.addChild(t1));
//		System.out.println("t2: " + nested.addChild(t2));
//		System.out.println("t3: " + nested.addChild(t3));
//		System.out.println("or: " + nested.addChild(or));
//		System.out.println("nested: " + root.addChild(nested));
//		System.out.println("t0: " + root.addChild(t0));
//		
//		System.out.println("\nAdd connections: ");
//		System.out.println("t1 -> or: " + nested.addConnection(new Connection(t1, t1.getOutputPorts().get(0), or, or.getInputPorts().get(0))));
//		System.out.println("t2 -> or: " + nested.addConnection(new Connection(t2, t2.getOutputPorts().get(0), or, or.getInputPorts().get(1))));
//		System.out.println("or -> t3: " + nested.addConnection(new Connection(or, or.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
//		System.out.println("t0 -> nested: " + root.addConnection(new Connection(t0, t0.getOutputPorts().get(0), nested, nested.getInputPorts().get(0))));
//		System.out.println("nested.t1.in -> t1.in: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(0), t1, t1.getInputPorts().get(0))));
//		
//		System.out.println("\nUnfold root: ");
//		for (IHyperworkflow hwf : root.unfold()) {
//			System.out.println(hwf + "\n");
//			for (IHyperworkflow child : ((NestedHyperworkflow)hwf).getChildren()) {
//				System.out.println(child);
//				System.out.println("inputPorts: " + child.getInputPorts());
//				System.out.println("outputPorts: " + child.getOutputPorts() + "\n");
//			}
//			System.out.println("----------------------------");
//		}
//		
//		//Serialization and Deserialization of root
//		XStream xs = new XStream();
//		NestedHyperworkflow object = (NestedHyperworkflow)(xs.fromXML(xs.toXML(root)));
//		System.out.println(object.unfold());

//		//-------------------------------------------------------------------------------------------------------------
//		NestedHyperworkflow root = new NestedHyperworkflow(null, "root", 0);
//		NestedHyperworkflow nested = new NestedHyperworkflow(root, "nested", 7);
//		IElement t1 = new Tool(root, "t1", 1);
//		t1.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t2 = new Tool(root, "t2", 2);
//		t2.getOutputPorts().add(new Port("out1", EPortType.FILE));
//		t2.getOutputPorts().add(new Port("out2", EPortType.FILE));
//		IElement t3 = new Tool(root, "t3", 3);
//		t3.getInputPorts().add(new Port("in", EPortType.FILE));
//		t3.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t4 = new Tool(root, "t4", 4);
//		t4.getInputPorts().add(new Port("in1", EPortType.FILE));
//		t4.getInputPorts().add(new Port("in2", EPortType.FILE));
//		IElement t5 = new Tool(root, "t5", 5);
//		t5.getInputPorts().add(new Port("in1", EPortType.FILE));
//		t5.getInputPorts().add(new Port("in2", EPortType.FILE));
//		t5.getOutputPorts().add(new Port("out", EPortType.FILE));
//		IElement t6 = new Tool(root, "t6", 6);
//		t6.getInputPorts().add(new Port("in", EPortType.FILE));
//		t6.getOutputPorts().add(new Port("out", EPortType.FILE));
//		
//		System.out.println("Add children: ");
//		System.out.println("t1: " + root.addChild(t1));
//		System.out.println("t2: " + root.addChild(t2));
//		System.out.println("t3: " + root.addChild(t3));
//		System.out.println("t4: " + root.addChild(t4));
//		System.out.println("nested: " + root.addChild(nested));
//		System.out.println("nested.t5: " + nested.addChild(t5));
//		System.out.println("nested.t6: " + nested.addChild(t6));
//		
//		System.out.println("\nAdd connections: ");
//		System.out.println("t1 -> nested: " + root.addConnection(new Connection(t1, t1.getOutputPorts().get(0), nested, nested.getInputPorts().get(1))));
//		System.out.println("t2 -> t3: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(0), t3, t3.getInputPorts().get(0))));
//		System.out.println("t2 -> nested: " + root.addConnection(new Connection(t2, t2.getOutputPorts().get(1), nested, nested.getInputPorts().get(0))));
//		System.out.println("t3 -> t4: " + root.addConnection(new Connection(t3, t3.getOutputPorts().get(0), t4, t4.getInputPorts().get(0))));
//		System.out.println("nested -> t4: " + root.addConnection(new Connection(nested, nested.getOutputPorts().get(1), t4, t4.getInputPorts().get(1))));
//		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(0), t5, t5.getInputPorts().get(0))));
//		System.out.println("nested -> t5: " + nested.addConnection(new Connection(nested, nested.getInputPorts().get(1), t5, t5.getInputPorts().get(1))));
//		System.out.println("nested.t5 -> nested.t6: " + nested.addConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
//		System.out.println("nested.t6 -> nested: " + nested.addConnection(new Connection(t6, t6.getOutputPorts().get(0), nested, nested.getOutputPorts().get(0))));
//		
//		System.out.println("\nExisting connections: ");
//		System.out.println(root.getConnections());
//		System.out.println(nested.getConnections());
//		
//		System.out.println("\nRemove connections: ");
//		System.out.println("nested.t5 -> nested.t6: " + nested.removeConnection(new Connection(t5, t5.getOutputPorts().get(0), t6, t6.getInputPorts().get(0))));
//		System.out.println(nested.getConnections());
//		
//		System.out.println("\nUnfold root: ");
//		System.out.println(root.unfold());		
	}
}
