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
	  	 
//    	from("quartz2://removeSuccefullyTranferedMessagesTimer?cron={{removeSuccefullyTransferedMessagesRepetitionRate}}")
//    	.routeId("quartz2removeSuccefullyTranferedMessages")
//    	.log("starting removeSuccefullyTranferedMessages")
//    	.beanRef("messageLogService", "removeSuccessfullyTransferedMessages()")
//    	.log("finished removeSuccefullyTranferedMessages");
    	  
    	from("quartz2://removeArchivedMessageEntriesTimer?cron={{removeArchivedMessageEntriesMessagesRepetitionRate}}")
    	.routeId("quartz2removeArchivedMessageEntries")
    	.log("starting removeArchivedMessageEntries")
    	.beanRef("messageLogService", "removeArchivedMessageEntries({{removeArchivedMessageEntriesOldLimit}})")
    	.log("finished removeArchivedMessageEntries");

    }
}
