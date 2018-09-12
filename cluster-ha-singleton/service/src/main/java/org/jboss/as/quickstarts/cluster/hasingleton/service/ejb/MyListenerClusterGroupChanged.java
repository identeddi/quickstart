package org.jboss.as.quickstarts.cluster.hasingleton.service.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.wildfly.clustering.group.Group.Listener;
import org.jboss.logging.Logger;
import org.wildfly.clustering.group.Node;

public class MyListenerClusterGroupChanged implements Listener {
    private static Logger LOGGER = Logger.getLogger(MyListenerClusterGroupChanged.class);

    List<String> serverPriorities =new ArrayList<String>(
    	    Arrays.asList("server-one", "server-two", "server-three"));

    boolean master = false;
    
	@Override
    public void membershipChanged(List<Node> prev, List<Node> curr, boolean merge) {
 
        for (Node node: prev)
        	System.out.println("PREVIOUS CUSTER VIEW: " +node.getName() + " "+node.getSocketAddress());
        System.out.println("==================================================");
 
        for (Node node: curr)
        	System.out.println("NEW CLUSTER VIEW " +node.getName() + " "+node.getSocketAddress());
 
        System.out.println("==================================================");
        System.out.println("Merged ? "+merge);
        
    	String serverName = System.getProperty("jboss.node.name");
    	serverName = serverName.contains(":") ? serverName.split(":")[1] : serverName;

    	int position = -1;
        for (int i = 0;i<curr.size();i++)
        {
        	Node node = curr.get(i);
        	String nodeName = node.getName().split("/")[0];
        	
        	nodeName = nodeName.contains(":") ? nodeName.split(":")[1] : nodeName;
        	if(serverName.equals(nodeName))
        	{
	        	position = i;
        	}
        	System.out.println(nodeName + " " + node.getSocketAddress());
        }
    	System.out.println("node position: " + position);
        
    	
        if(position == 0)
        {
        	LOGGER.info("Primary HASingleton on server " + serverName);
        	master = true;
        }
        else
        {
        	LOGGER.info("Slave node.");
        	master = false;
        }
       
 
    }
 
	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

}
