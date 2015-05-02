package org.vanda.studio.modules.workflows.run;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import org.vanda.workflows.hyper.Job;

/**
 * Stores all information that is relevant for the execution system. 
 * @author kgebhardt
 *
 */
public class RunConfig {
	private final String path;
	private final Date date;
	private final Map<String, Integer> jobPriorities;
	
	public RunConfig(String path, Map<String, Integer> jobPriorities) {
		this.path = path;
		this.date = new Date();
		this.jobPriorities = jobPriorities;
	}
	
	public String getPath() {
		return path;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Map<String, Integer> getJobPriorities() {
		return jobPriorities;
	}
	
	/**
	 * @return a Comparator that is used by the TopSorter to implement Job priorities
	 */
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
}
