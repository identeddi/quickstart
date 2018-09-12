/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.cluster.hasingleton.service.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.as.naming.InitialContext;
import org.jboss.logging.Logger;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;


/**
 * A simple example to demonstrate a implementation of a cluster-wide singleton timer.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Singleton
@Startup
public class SchedulerBean implements Scheduler {
    private static Logger LOGGER = Logger.getLogger(SchedulerBean.class);
    @Resource
    private TimerService timerService;

    @Resource(lookup = "java:jboss/clustering/group/server")  
    private Group channelGroup;   

    MyListenerClusterGroupChanged myListenerClusterGroupChanged = new MyListenerClusterGroupChanged();;
    
    List<Node> prevList = new ArrayList<>();
    @Timeout
    public void scheduler(Timer timer) {
    	List<Node> newList = channelGroup.getNodes();
    	      
    	if(!prevList.equals(newList))
    	{
    		myListenerClusterGroupChanged.membershipChanged(prevList,newList,false);
    	}
    	prevList = newList;
    	
    	if(myListenerClusterGroupChanged.isMaster())
        {
        	LOGGER.info("Primary HASingletonTimer on server ");
        }
        else
        {
        	LOGGER.info("Slave node.");
       	
        }
        
        
    }
    @PostConstruct
    public void postConstruct()
    {
        LOGGER.info("postConstruct HATimerService");
    	List<Node> newList = channelGroup.getNodes();
	      
    	if(!prevList.equals(newList))
    	{
    		myListenerClusterGroupChanged.membershipChanged(prevList,newList,false);
    	}
    	prevList = newList;
        initialize("local");
   	
    }

    @Override
    public void initialize(String info) {
        ScheduleExpression sexpr = new ScheduleExpression();
        // set schedule to every 10 seconds for demonstration
        sexpr.hour("*").minute("*").second("0/10");
        // persistent must be false because the timer is started by the HASingleton service
        timerService.createCalendarTimer(sexpr, new TimerConfig(info, false));
    }

    @Override
    public void stop() {
        LOGGER.info("Stop all existing HASingleton timers");
        for (Timer timer : timerService.getTimers()) {
            LOGGER.trace("Stop HASingleton timer: " + timer.getInfo());
            timer.cancel();
        }
    }
}
