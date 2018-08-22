package org.jboss.as.quickstarts.cluster.hasingleton.service.ejb;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

@Singleton
@Startup
public class RegisterClusterListener {
	 @Resource(lookup = "java:jboss/clustering/group/server")  
	    private Group channelGroup;  
	 
	 	MyListenerClusterGroupChanged clusterListener = new MyListenerClusterGroupChanged ();
	    @PostConstruct
	    public void check() {
	    	System.out.println("Listener registering");
	        channelGroup.addListener(clusterListener);
	    	System.out.println("Listener registering");
	    	clusterListener.membershipChanged(new ArrayList<Node>(),channelGroup.getNodes(),false);
	    }
	    
	    public boolean isMaster()
	    {
	    	return clusterListener.isMaster();
	    }
}
