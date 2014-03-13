/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.scheduling.internal;

import org.apache.camel.builder.RouteBuilder;

public class SchedulerRouteBuilder extends RouteBuilder {
	
    @Override
    public void configure() throws Exception {
    	
    	from("quartz2://releaseStaleFetchingInProgress?cron={{releaseStaleFetchingInProgressCron}}&stateful=true")
    	.routeId("releaseStaleFetchingInProgress")
    	.log("starting ${routeId}")
    	.beanRef("messageLogService", "releaseStaleFetchingInProgress()")
        .log("released ${body} messages")
    	.log("finished ${routeId}");
    			
	  	from("quartz2://archiveMessages?cron={{archiveMessagesCron}}&stateful=true")
	  	.routeId("archiveMessages")
	  	.log("starting ${routeId}")
	  	.beanRef("messageLogService", "archiveMessages({{archiveMessagesOldLimit}})")
        .log("archived ${body} messages")
	  	.log("finished ${routeId}");
    	
	  	from("quartz2://removeArchivedMessages?cron={{removeArchivedMessagesCron}}&stateful=true")
	  	.routeId("removeArchivedMessages")
	  	.log("starting ${routeId}")
	  	.beanRef("messageLogService", "removeArchivedMessages({{removeArchivedMessagesOldLimit}})")
        .log("deleted ${body} archived messages")
	  	.log("finished ${routeId}");
	  	 
    	from("quartz2://removeSuccessfullyTransferredMessages?cron={{removeSuccessfullyTransferredMessagesCron}}&stateful=true")
    	.routeId("removeSuccessfullyTransferredMessages")
    	.log("starting ${routeId}")
    	.beanRef("messageLogService", "removeSuccessfullyTransferredMessages()")
        .log("deleted ${body} transferred messages")
    	.log("finished ${routeId}");
    	  
    	from("quartz2://removeArchivedMessageEntries?cron={{removeArchivedMessageEntriesCron}}&stateful=true")
    	.routeId("removeArchivedMessageEntries")
    	.log("starting ${routeId}")
    	.beanRef("messageLogService", "removeArchivedMessageEntries({{removeArchivedMessageEntriesOldLimit}})")
        .log("deleted ${body} archived message entries")
    	.log("finished ${routeId}");

    }
}
