package org.vanda.runner;

/**
 * Stores all information that is relevant for the execution system.
 * TODO this class is /specific/ to the execution system
 * it should be named ShellCompilerRunConfig
 * TODO store path information for the data sources 
 * @author kgebhardt
 *
 */
public class RunConfig {
	private String path;
	private boolean debug;  // TODO respect this setting
	
	public RunConfig(String path) {
		this.path = path;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public String getPath() {
		return path;
	}
	
	/**
	 * @return a Comparator that is used by the TopSorter to implement Job priorities
	public Comparator<Job> generateComparator() {
		return new Comparator<Job>() {

			@Override
			public int compare(Job arg0, Job arg1) {
				Integer prio0 = jobPriorities.get(arg0.getId());
				Integer prio1 = jobPriorities.get(arg1.getId());
				if (prio0 == null && prio1 == null) {
					return arg0.hashCode() - arg1.hashCode();
				} else if (prio0 == null) {
					return -1;
				} else if (prio1 == null) {
					return 1;
				} else {
					int diff = prio0 - prio1;
					if (diff != 0)
						return diff;
					else
						return arg0.hashCode() - arg1.hashCode();
				}
			}
		};
		
	}
	 */
}
