/*
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

print('------------------------------ START ------------------------------');
// ============================================================
// Preparation
// ============================================================
print('Drop indexes');
db.shsMessageEntry.dropIndexes();

// ============================================================
// Create indexes
// ============================================================
print('Create indexes');

// ------------------------------------------------------------
// Indexes for MongoMessageLogService
// ------------------------------------------------------------
// ............................................................
// messageLogRepository.find...()
// ............................................................
// !!!
db.shsMessageEntry.ensureIndex( { "label.txId" : 1 } );

// ............................................................
// mongoTemplate.find...()
// ............................................................
// !!!
db.shsMessageEntry.ensureIndex( { "label.corrId" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.txId" : 1 } );

// releaseStaleFetchingInProgress
db.shsMessageEntry.ensureIndex( { "state" : 1, "stateTimeStamp" : 1 } );

// ............................................................
// listMessages()
// ............................................................
// 3 levels + sort
// !!!
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "stateTimeStamp" : 1 } );

// 4 levels + sort
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.product.value" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "acknowledged" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.status" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.endRecipient.value" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.originatorOrFrom.value" : 1, "stateTimeStamp" : 1 } );
// !!!
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.corrId" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.content.contentId" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.meta.name" : 1, "stateTimeStamp" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.meta.value" : 1, "stateTimeStamp" : 1 } );

// 5 levels + sort
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1, "label.transferType" : 1, "state" : 1, "label.product.value" : 1, "acknowledged" : 1, "stateTimeStamp" : 1 } );

// ............................................................
// TODO - indexes for archiving
// ............................................................
// db.shsMessageEntry.ensureIndex( { "archived" : 1, "stateTimeStamp" : 1 } );
// db.shsMessageEntry.ensureIndex( { "stateTimeStamp" : 1, "state" : 1 } );

// ------------------------------------------------------------
// Indexes for MongoMessageLogAdminService
// ------------------------------------------------------------
db.shsMessageEntry.ensureIndex( { "label.to.value" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.originatorOrFrom.value" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.txId" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.corrId" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.content.dataOrCompound.filename" : 1 } );
db.shsMessageEntry.ensureIndex( { "label.product.value" : 1 } );
db.shsMessageEntry.ensureIndex( { "acknowledged" : 1 } );
db.shsMessageEntry.ensureIndex( { "state" : 1 } );

// ============================================================
// Printout stats to see how much room the indexes take
// ============================================================
print('db.shsMessageEntry.stats(1024 * 1024)');
printjson(db.shsMessageEntry.stats(1024 * 1024));
print('------------------------------ END ------------------------------');
