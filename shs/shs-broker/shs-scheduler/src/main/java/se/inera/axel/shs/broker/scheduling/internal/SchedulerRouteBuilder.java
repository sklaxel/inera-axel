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
    	
    	from("quartz2://releaseStaleFetchingInProgressTimer?cron={{releaseStaleFetchingInProgressRepetitionRate}}")
    	.routeId("quartz2releaseStaleFetchingInProgress")
    	.log("starting releaseStaleFetchingInProgress")
    	.beanRef("messageLogService", "releaseStaleFetchingInProgress()")
    	.log("finished releaseStaleFetchingInProgress");
    			
	  	from("quartz2://archiveMessagesTimer?cron={{archiveMessagesRepetitionRate}}")
	  	.routeId("quartz2archiveMessages")
	  	.log("starting archiveMessages")
	  	.beanRef("messageLogService", "archiveMessages({{archiveMessagesOldLimit}})")
	  	.log("finished archiveMessages");
    	
	  	from("quartz2://removeArchivedMessagesTimer?cron={{removeArchivedMessagesRepetitionRate}}")
	  	.routeId("quartz2removeArchivedMessages")
	  	.log("starting removeArchivedMessages")
	  	.beanRef("messageLogService", "removeArchivedMessages({{removeArchivedMessagesOldLimit}})")
	  	.log("finished removeArvhivedMessages");
	  	 
    	from("quartz2://removeSuccefullyTranferedMessagesTimer?cron={{removeSuccefullyTransferedMessagesRepetitionRate}}")
    	.routeId("quartz2removeSuccefullyTranferedMessages")
    	.log("starting removeSuccefullyTranferedMessages")
    	.beanRef("messageLogService", "removeSuccessfullyTransferedMessages()")
    	.log("finished removeSuccefullyTranferedMessages");
    	  
    	from("quartz2://removeArchivedMessageEntriesTimer?cron={{removeArchivedMessageEntriesMessagesRepetitionRate}}")
    	.routeId("quartz2removeArchivedMessageEntries")
    	.log("starting removeArchivedMessageEntries")
    	.beanRef("messageLogService", "removeArchivedMessageEntries({{removeArchivedMessageEntriesOldLimit}})")
    	.log("finished removeArchivedMessageEntries");

    }
}
