[33mfee4145[m [Task 118] När det kommer ett “error” från mottagaren så ska requesten hittas och karantäniseras.
[33m74b4286[m AXEL-139, AXEL-140 Implemented support for administering multiple LDAP servers.
[33m3a58f81[m [Task 109] Label/history ska inte kopieras när man gör en response.
[33m0816505[m [Task 110] Make label/history Date instead of String.
[33mf004cd9[m Add activemq blueprint to karaf system dist.
[33mb55cd36[m Axel-138 Added possibility to configure multiple LDAP servers. Added a DirectoryServiceGateway that searches the LDAP servers in order to retrieve directory data.
[33ma169178[m Added support for public agreements retrieved from DirectoryService for MongoAgreementService. So that we can support the Many to one scenario.
[33meafc865[m Made it more obvious that the urn form is used for the organization number
[33m49afae4[m Fixed resolveRecipient for agreements with any flow. Added unit tests for resolveRecipients
[33mc02f2f5[m Fix ack POST parameter for shsCmdLine fetch - part_2
[33m54c2ac6[m Added message history list to message detail view in gui.
[33mfffc4b1[m Minor correction.
[33m678e77c[m Added Axel O info on "HomePage".
[33m441adeb[m [FitNesse] Enhance Ts41K001
[33m09acead[m Fix ack POST parameter for shsCmdLine fetch
[33m930f441[m Added unit tests for findMessages(filter) used by admin gui.
[33mb5b03f7[m Added hit count to message list in admin gui.
[33m18df4b4[m Merge branch 'message-gui'
[33m675465a[m Add timestamp formatting to message list gui.
[33mb77f75e[m Cleanup
[33mb06ea66[m Fix criteria table css.
[33mf6b8346[m CSS trial and error.
[33m3d6a2e4[m [FitNesse] Enhance Ts41K001
[33mfffe194[m Added 'state' and 'acknowledge' to search criteria in message list.
[33m98fd93c[m [FitNesse] Add asynchronous test Ts41K001
[33mdfcdfb7[m Added search criteria in message list view.
[33mbd33e41[m Implement pagination in message store admin service and sort messages with most recent first.
[33ma5ceea3[m Current message is excluded from 'Related messages'.
[33mf5f3fc7[m Added list of related messages to message view.
[33m79d5d0f[m Restructure message view and add meta data to display.
[33m6ede877[m Add Hawtio to Axel startup
[33mbc143ef[m Set page size for message list to 15.
[33m283fc66[m [FitNesse] Run both interoperability and system tests
[33mf1d91f7[m Refactor synchronous tests in interoperability
[33m51bf4ee[m Add an 'ellipse-text' css class.
[33m2a02914[m GUI for message log.
[33mec9ce97[m Create Interoperability test suite
[33mba7d802[m Send FitNesse result to proper Jenkins folder
[33m36423e1[m Change FitNesse from JUnit to TestNG
[33m8178d78[m Add <parent> to FitNesse pom.xml
[33m9d4c3c2[m Make it run the whole FitNesse suite + fix logging
[33m83174d9[m Fix pom.xml for FitNesse
[33mff23c74[m Add DS endpoints in Karaf distribution property file.
[33m9ec30e0[m Name all routes in delivery service route builder.
[33m6400978[m Correct directory mode on 'bin' directory in shs-cmdline tar.gz file.
[33m8cd769b[m Disable a Mockito verify call in unit test.
[33m462010a[m Remove need for external running mongodb in tests.
[33m9837f03[m Add product service rest client to riv/shs bridge war file.
[33m8e65d38[m Add ProductService as a CXF REST service to war-files.
[33m1a215cc[m Make a war of standalone shs broker, standalone riv/shs bridge and a war with broker and bridge.
[33m451b0ae[m Added RIV/SHS bridge and it's webconsole to shs broker war-file.
[33md35a6c2[m Added shs webconsole to war-file.
[33m74eb204[m Support for defining synchronous plugins with spring contexts in shs broker war.
[33m57dc2fd[m Added a war packaging of riv/shs bridge.
[33m1e384c6[m Rename shs broker war artifact.
[33mfced37b[m Rename war-file for shs broker.
[33m9b6ff45[m Rename all camel-context.xml to spring-context.xml
[33mc6712ec[m First stab at making a war-package of the Axel SHS Broker.
[33m786c4c8[m Initial checkin of fitnesse tests
[33m14f1f52[m Added SonarQube plugin.
[33me13125d[m Added tests for filter arguments in the list messages http delivery service.
[33me09e078[m Create and send a confirm message when a message is acknowledged by client.
[33m7e97ecf[m Add tests to delivery service component.
[33mcdb9323[m Added license headers.
[33m2e7572f[m Implemented "ack" from shs cmdline client when fetching message.
[33m4733afe[m Fix issue with resolving dtd on shs message xml in shs cmdline client.
[33m116d421[m Fixed some issues introduced with previous commits.
[33m9cad5cb[m Adjust filter noack criteria in message listing.
[33me83cc7c[m Implemented "acknowledge message" in DeliveryService.
[33me4b9c74[m Implement "fetch message" in delivery service.
[33m06aa931[m AXEL-84, AXEL-85 Implemented list, fetch and fetchall in shs-cmdline
[33mabec7e7[m Added DeliveryService and implemented "list messages".
[33mfe06412[m JavaDoc
[33m701ba86[m Log a warning when sorting on meta name instead of value.
[33m97550a8[m Implemented acknowledge() in MessageLogService.
[33mec710a2[m Implemented filtering on originator in MessageLogService.listMessages().
[33m9104314[m Implemented filtering on meta-data in MessageLogService.listMessages().
[33ma456b46[m Implemented sorting by specified attribute in MongoMessageLogService
[33m8529003[m Remake of sort order attribute in MessageLogService.
[33m4982aef[m Add support for sorting on 'arrival' in MongoMessageLogService.
[33mb9fcb7d[m Add support for filtering on 'since' in MongoMessageLogService.
[33m554b90f[m Add support for filtering on contentId in MongoMessageLogService.
[33ma7c6a3e[m Add support for filtering on corrId in MongoMessageLogService.
[33ma93348a[m Add support for filtering on endRecipient in MongoMessageLogService.
[33mc2c6f15[m Refactor tests and fix status searching in MongoMessageLogService.
[33m9ce22d0[m Added support for noAck in listMessages(filter)
[33m7709f18[m Added support for maxHits in listMessages(filter)
[33mf45f91f[m Add messageList(filter) to MessageLogService.
[33m830a48a[m Slightly different error message logging when quarantining messages.
[33m3819d8e[m Added error handling of asynchronous messages.
[33m5994931[m Admin-messages should pass validateAgreement.
[33m142b18f[m Modified license header to reflect the fact that we use the LGPL.
[33m435fbd1[m Make surefire use "target" as tempdir.
[33m1926b92[m Renamed routebuilders in shs-receiveservice.
[33mbd7f60e[m Restructure modules and java packages.
[33mcd478b6[m Aggregate javadoc grouping configuration.
[33m5c30001[m Fixed so that synchronous replies are written to the message store Removed obsolete Mock classes
[33mf913645[m Cleanup of test cases in broker-synch and use mockito to mock deps.
[33mb6d17fc[m Implement simple asynchronous message routing tests.
[33m87d42ae[m ActiveMQ broker in unit test need not be persistent.
[33m9875a29[m Added messageReceived() and messageSent() to message log service.
[33ma74d7f3[m Moved all activemq broker to asynchron broker component and updated some tests.
[33m6168bde[m Added distributionManagement Downgraded slf4j
[33m1ef879c[m Added asynchronous broker component.
[33mbefde9f[m Fixed typo
[33m5fc3eae[m Maven cleanup Upgraded maven plugins to latest versions Added used dependencies that where not declared Removed unused dependencies that were declared
[33m3a51465[m Added an embedded activemq broker in the karaf distribution. also added hawtio in features files.
[33m2f1e024[m Fixed issue with file permissions of etc directory in axel distribution.
[33m1c8f9b3[m Added activemq-data directory to git ignore.
[33m2ff8532[m Disable stream caching and enable jms on jetty receive service endpoint.
[33mcd07c01[m Added async shs response headers.
[33m03f0699[m Add activemq and log the async message to the axel.shs.in queue.
[33me2230f0[m Initial import of existing sources.
[33m198bf23[m Removed test file.
[33m487d34e[m Test commit
