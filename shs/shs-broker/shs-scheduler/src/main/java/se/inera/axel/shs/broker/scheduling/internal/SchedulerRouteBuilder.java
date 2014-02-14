package se.inera.axel.shs.broker.scheduling.internal;

import org.apache.camel.builder.RouteBuilder;

public class SchedulerRouteBuilder extends RouteBuilder {
	
    @Override
    public void configure() throws Exception {
    	
    	from("quartz2://releaseStaleFetchingInProgressTimer?cron={{releaseStaleFetchingInProgressRepetitionRate}}")
    	.beanRef("messageLogService", "releaseStaleFetchingInProgress()");
    			
	  	from("quartz2://archiveMessagesTimer?cron={{archiveMessagesRepetitionRate}}")
	  	.beanRef("messageLogService", "archiveMessages({{archiveMessagesOldLimit}})");
    	
	  	from("quartz2://removeArchivedMessagesTimer?cron={{removeArchivedMessagesRepetitionRate}}")
	  	.beanRef("messageLogService", "removeArchivedMessages({{removeArchivedMessagesOldLimit}})");
	  	 
    	from("quartz2://removeSuccefullyTranferedMessagesTimer?cron={{removeSuccefullyTransferedMessagesRepetitionRate}}")
    	.beanRef("messageLogService", "removeSuccessfullyTransferedMessages()");
    	  
    	from("quartz2://removeArchivedMessageEntriesTimer?cron={{removeArchivedMessageEntriesMessagesRepetitionRate}}")
    	.beanRef("messageLogService", "removeArchivedMessageEntries({{removeArchivedMessageEntriesOldLimit}})");

    }
}
