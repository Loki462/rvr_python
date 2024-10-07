/**
 * 
 */
package observer;

import utilities.SF_ORG_Analytics;

/**
 * @author n691581
 *
 */
public class Observer {
	
	private SF_ORG_Analytics sfOrgAnalyticsLocal;
	
	public Observer(SF_ORG_Analytics sfOrgAnalytics) {
		this.sfOrgAnalyticsLocal = sfOrgAnalytics;
		this.sfOrgAnalyticsLocal.attach((java.util.Observer) this);
		System.out.println("Observer invoked");
	}
	
	public void update() {
		System.out.println("Observer updated");
	}
}
