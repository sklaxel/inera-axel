/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.xml.agreement;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.natpryce.makeiteasy.SameValueDonor;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.listOf;
import static com.natpryce.makeiteasy.Property.newProperty;

@SuppressWarnings("unchecked")
public class ShsAgreementMaker {
	private static final ObjectFactory factory = new ObjectFactory();
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private static final SameValueDonor<String> nullString = new SameValueDonor<String>(null);
	private static final SameValueDonor<Date> nullDate = new SameValueDonor<Date>(null);
	
	public static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000001";
	public static final String DEFAULT_PRINCIPAL = "0000000000";
	public static final String DEFAULT_PRINCIPAL_CN = "Default principal";
	public static final String DEFAULT_CUSTOMER = "1111111111";
	public static final String DEFAULT_CUSTOMER_CN = "Default customer";
	public static final String DEFAULT_PRODUCT_ID = "00000000-0000-0000-0002-000000000001";
	
	public static class ShsAgreementInstantiator implements Instantiator<ShsAgreement> {
		// ShsAgreement
		public static final Property<ShsAgreement, Shs> shs = newProperty();
		public static final Property<ShsAgreement, General> general = newProperty();
		public static final Property<ShsAgreement, String> transferType = newProperty();
		public static final Property<ShsAgreement, String> uuid = newProperty();
		public static final Property<ShsAgreement, String> version = newProperty();
		
		@Override
		public ShsAgreement instantiate(
				PropertyLookup<ShsAgreement> lookup) {
			ShsAgreement agreement = factory.createShsAgreement();
            agreement.setShs(lookup.valueOf(shs, a(Shs)));
            agreement.setGeneral(lookup.valueOf(general, a(General)));
            agreement.setTransferType(lookup.valueOf(transferType, "any"));
            agreement.setUuid(lookup.valueOf(uuid, DEFAULT_UUID));
            agreement.setVersion(lookup.valueOf(version, nullString));
            return agreement;
		}
	}
	
	public static final ShsAgreementInstantiator ShsAgreement = new ShsAgreementInstantiator();
	
	public static class GeneralInstantiator implements Instantiator<General> {
		
		public static final Property<General, String> description = newProperty();
		public static final Property<General, QoS> qoS = newProperty();
		public static final Property<General, Schedule> schedule = newProperty();
		public static final Property<General, Valid> valid = newProperty();
		
		@Override
		public General instantiate(
				PropertyLookup<General> lookup) {
			General general = factory.createGeneral();
			general.setDescription(lookup.valueOf(description, nullString));
			general.setQoS(lookup.valueOf(qoS, a(QoS)));
			general.setSchedule(lookup.valueOf(schedule, a(Schedule)));
			general.setValid(lookup.valueOf(valid, a(Valid)));
            return general;
		}
	}
	
	public static final GeneralInstantiator General = new GeneralInstantiator();
	
	public static class QoSInstantiator implements Instantiator<QoS> {
		
		public static final Property<QoS, String> description = newProperty();
		public static final Property<QoS, Frequency> frequency = newProperty();
		public static final Property<QoS, Open> open = newProperty();
		public static final Property<QoS, Response> response = newProperty();
		public static final Property<QoS, Volume> volume = newProperty();
		
		@Override
		public QoS instantiate(
				PropertyLookup<QoS> lookup) {
			QoS qoS = factory.createQoS();
			qoS.setDescription(lookup.valueOf(description, nullString));
			qoS.setFrequency(lookup.valueOf(frequency, new SameValueDonor<Frequency>(null)));
			qoS.setOpen(lookup.valueOf(open, a(Open)));
			qoS.setResponse(lookup.valueOf(response, new SameValueDonor<Response>(null)));
			qoS.setVolume(lookup.valueOf(volume, new SameValueDonor<Volume>(null)));
            return qoS;
		}
	}
	
	public static final QoSInstantiator QoS = new QoSInstantiator();
	
	public static class FrequencyInstantiator implements Instantiator<Frequency> {
		
		public static final Property<Frequency, Average> average = newProperty();
		public static final Property<Frequency, String> description = newProperty();
		public static final Property<Frequency, Peak> peak = newProperty();
		
		@Override
		public Frequency instantiate(
				PropertyLookup<Frequency> lookup) {
			Frequency frequency = factory.createFrequency();
			frequency.setAverage(lookup.valueOf(average, new SameValueDonor<Average>(null)));
			frequency.setDescription(lookup.valueOf(description, nullString));
			frequency.setPeak(lookup.valueOf(peak, new SameValueDonor<Peak>(null)));
            return frequency;
		}
	}
	
	public static final FrequencyInstantiator Frequency = new FrequencyInstantiator();
	
	public static class AverageInstantiator implements Instantiator<Average> {
		
		public static final Property<Average, String> numberPer = newProperty();
		public static final Property<Average, String> period = newProperty();
		
		@Override
		public Average instantiate(
				PropertyLookup<Average> lookup) {
			Average average = factory.createAverage();
			average.setNumberPer(lookup.valueOf(numberPer, nullString));
			average.setPeriod(lookup.valueOf(period, nullString));
            return average;
		}
	}
	
	public static final AverageInstantiator Average = new AverageInstantiator();
	
	public static class PeakInstantiator implements Instantiator<Peak> {
		
		public static final Property<Peak, String> numberPer = newProperty();
		public static final Property<Peak, String> period = newProperty();
		
		@Override
		public Peak instantiate(
				PropertyLookup<Peak> lookup) {
			Peak peak = factory.createPeak();
			peak.setNumberPer(lookup.valueOf(numberPer, nullString));
			peak.setPeriod(lookup.valueOf(period, nullString));
			
            return peak;
		}
	}
	
	public static final PeakInstantiator Peak = new PeakInstantiator();
	
	public static class OpenInstantiator implements Instantiator<Open> {
		
		public static final Property<Open, String> description = newProperty();
		public static final Property<Open, When> when = newProperty();
		public static final Property<Open, List<Serializable>> starttimeOrStoptime = newProperty();
		
		@Override
		public Open instantiate(
				PropertyLookup<Open> lookup) {
			Open open = factory.createOpen();
			open.setDescription(lookup.valueOf(description, nullString));
			open.setWhen(lookup.valueOf(when, a(When)));
			open.getStarttimeOrStoptime().addAll(lookup.valueOf(starttimeOrStoptime, new ArrayList<Serializable>()));
			
            return open;
		}
	}
	
	public static final OpenInstantiator Open = new OpenInstantiator();
	
	public static class WhenInstantiator implements Instantiator<When> {
		
		public static final Property<When, String> day = newProperty();
		public static final Property<When, String> description = newProperty();
		public static final Property<When, String> hours = newProperty();
		
		@Override
		public When instantiate(
				PropertyLookup<When> lookup) {
			When when = factory.createWhen();
			when.setDay(lookup.valueOf(day, "every"));
			when.setDescription(lookup.valueOf(description, nullString));
			when.setHours(lookup.valueOf(hours, "all"));
			
            return when;
		}
	}
	
	public static final WhenInstantiator When = new WhenInstantiator();
	
	public static class ResponseInstantiator implements Instantiator<Response> {
		
		public static final Property<Response, String> description = newProperty();
		public static final Property<Response, Reply> reply = newProperty();
		public static final Property<Response, Request> request = newProperty();
		public static final Property<Response, String> unit = newProperty();
		public static final Property<Response, String> within = newProperty();
		
		@Override
		public Response instantiate(
				PropertyLookup<Response> lookup) {
			Response response = factory.createResponse();
			response.setDescription(lookup.valueOf(description, "description"));
			response.setReply(lookup.valueOf(reply, new SameValueDonor<Reply>(null)));
			response.setRequest(lookup.valueOf(request, new SameValueDonor<Request>(null)));
			response.setUnit(lookup.valueOf(unit, nullString));
			response.setWithin(lookup.valueOf(within, nullString));
			
            return response;
		}
	}
	
	public static final ResponseInstantiator Response = new ResponseInstantiator();
	
	public static class ReplyInstantiator implements Instantiator<Reply> {
		
		public static final Property<Reply, NotAfter> notAfter = newProperty();
		public static final Property<Reply, NotBefore> notBefore = newProperty();
		
		@Override
		public Reply instantiate(
				PropertyLookup<Reply> lookup) {
			Reply reply = factory.createReply();
			reply.setNotAfter(lookup.valueOf(notAfter, new SameValueDonor<NotAfter>(null)));
			reply.setNotBefore(lookup.valueOf(notBefore, new SameValueDonor<NotBefore>(null)));
			
            return reply;
		}
	}
	
	public static final ReplyInstantiator Reply = new ReplyInstantiator();
	
	public static class NotAfterInstantiator implements Instantiator<NotAfter> {
		
		public static final Property<NotAfter, WeekTime> weekTime = newProperty();
		
		@Override
		public NotAfter instantiate(
				PropertyLookup<NotAfter> lookup) {
			NotAfter notAfter = factory.createNotAfter();
			notAfter.setWeekTime(lookup.valueOf(weekTime, new SameValueDonor<WeekTime>(null)));
			
            return notAfter;
		}
	}
	
	public static final NotAfterInstantiator NotAfter = new NotAfterInstantiator();
	
	public static class WeekTimeInstantiator implements Instantiator<WeekTime> {
		
		public static final Property<WeekTime, String> day = newProperty();
		public static final Property<WeekTime, String> time = newProperty();
		
		@Override
		public WeekTime instantiate(
				PropertyLookup<WeekTime> lookup) {
			WeekTime weekTime = factory.createWeekTime();
			weekTime.setDay(lookup.valueOf(day, nullString));
			weekTime.setTime(lookup.valueOf(time, nullString));
			
            return weekTime;
		}
	}
	
	public static final WeekTimeInstantiator WeekTime = new WeekTimeInstantiator();
	
	public static class NotBeforeInstantiator implements Instantiator<NotBefore> {
		
		public static final Property<NotBefore, WeekTime> weekTime = newProperty();
		
		@Override
		public NotBefore instantiate(
				PropertyLookup<NotBefore> lookup) {
			NotBefore notBefore = factory.createNotBefore();
			notBefore.setWeekTime(lookup.valueOf(weekTime, new SameValueDonor<WeekTime>(null)));
			
            return notBefore;
		}
	}
	
	public static final NotBeforeInstantiator NotBefore = new NotBeforeInstantiator();
	
	public static class RequestInstantiator implements Instantiator<Request> {
		
		public static final Property<Request, NotAfter> notAfter = newProperty();
		public static final Property<Request, NotBefore> notBefore = newProperty();
		
		@Override
		public Request instantiate(
				PropertyLookup<Request> lookup) {
			Request request = factory.createRequest();
			request.setNotAfter(lookup.valueOf(notAfter, new SameValueDonor<NotAfter>(null)));
			request.setNotBefore(lookup.valueOf(notBefore, new SameValueDonor<NotBefore>(null)));
			
            return request;
		}
	}
	
	public static final RequestInstantiator Request = new RequestInstantiator();
	
	public static class VolumeInstantiator implements Instantiator<Volume> {
		
		public static final Property<Volume, Average> average = newProperty();
		public static final Property<Volume, String> description = newProperty();
		public static final Property<Volume, Peak> peak = newProperty();
		public static final Property<Volume, String> perTransfer = newProperty();
		public static final Property<Volume, String> unit = newProperty();
		
		@Override
		public Volume instantiate(
				PropertyLookup<Volume> lookup) {
			Volume volume = factory.createVolume();
			volume.setAverage(lookup.valueOf(average, new SameValueDonor<Average>(null)));
			volume.setDescription(lookup.valueOf(description, nullString));
			volume.setPeak(lookup.valueOf(peak, new SameValueDonor<Peak>(null)));
			volume.setPerTransfer(lookup.valueOf(perTransfer, nullString));
			volume.setUnit(lookup.valueOf(unit, nullString));
			
            return volume;
		}
	}
	
	public static final VolumeInstantiator Volume = new VolumeInstantiator();
	
	public static class ScheduleInstantiator implements Instantiator<Schedule> {
		
		public static final Property<Schedule, Intervaltime> intervaltime = newProperty();
		public static final Property<Schedule, String> startdate = newProperty();
		public static final Property<Schedule, Starttime> starttime = newProperty();
		public static final Property<Schedule, String> stopdate = newProperty();
		public static final Property<Schedule, Stoptime> stoptime = newProperty();
		public static final Property<Schedule, String> timezone = newProperty();
		
		@Override
		public Schedule instantiate(
				PropertyLookup<Schedule> lookup) {
			Schedule schedule = factory.createSchedule();
			schedule.setIntervaltime(lookup.valueOf(intervaltime, a(Intervaltime)));
			schedule.setStartdate(lookup.valueOf(startdate, nullString));
			schedule.setStarttime(lookup.valueOf(starttime, new SameValueDonor<Starttime>(null)));
			schedule.setStopdate(lookup.valueOf(stopdate, nullString));
			schedule.setStoptime(lookup.valueOf(stoptime, new SameValueDonor<Stoptime>(null)));
			schedule.setTimezone(lookup.valueOf(timezone, nullString));
			
            return schedule;
		}
	}
	
	public static final ScheduleInstantiator Schedule = new ScheduleInstantiator();
	
	public static class IntervaltimeInstantiator implements Instantiator<Intervaltime> {
		
		public static final Property<Intervaltime, String> day = newProperty();
		public static final Property<Intervaltime, String> hour = newProperty();
		public static final Property<Intervaltime, String> min = newProperty();
		
		@Override
		public Intervaltime instantiate(
				PropertyLookup<Intervaltime> lookup) {
			Intervaltime intervaltime = factory.createIntervaltime();
			intervaltime.setDay(lookup.valueOf(day, nullString));
			intervaltime.setHour(lookup.valueOf(hour, nullString));
			intervaltime.setMin(lookup.valueOf(min, nullString));
			
            return intervaltime;
		}
	}
	
	public static final IntervaltimeInstantiator Intervaltime = new IntervaltimeInstantiator();
	
	public static class StarttimeInstantiator implements Instantiator<Starttime> {
		
		public static final Property<Starttime, Date> value = newProperty();
		
		@Override
		public Starttime instantiate(
				PropertyLookup<Starttime> lookup) {
			Starttime starttime = factory.createStarttime();
			
			try {
				starttime.setvalue(lookup.valueOf(value, timeFormat.parse("11:00")));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			
            return starttime;
		}
	}
	
	public static final StarttimeInstantiator Starttime = new StarttimeInstantiator();
	
	public static class StoptimeInstantiator implements Instantiator<Stoptime> {
		
		public static final Property<Stoptime, Date> value = newProperty();
		
		@Override
		public Stoptime instantiate(
				PropertyLookup<Stoptime> lookup) {
			Stoptime starttime = factory.createStoptime();
			
			try {
				starttime.setvalue(lookup.valueOf(value, timeFormat.parse("12:00")));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			
            return starttime;
		}
	}
	
	public static final StoptimeInstantiator Stoptime = new StoptimeInstantiator();
	
	public static class ValidInstantiator implements Instantiator<Valid> {
		
		public static final Property<Valid, ValidFrom> validFrom = newProperty();
		public static final Property<Valid, ValidTo> validTo = newProperty();
		
		@Override
		public Valid instantiate(
				PropertyLookup<Valid> lookup) {
			Valid valid = factory.createValid();
			valid.setValidFrom(lookup.valueOf(validFrom, a(ValidFrom)));
			valid.setValidTo(lookup.valueOf(validTo, new SameValueDonor<ValidTo>(null)));
			
            return valid;
		}
	}
	
	public static final ValidInstantiator Valid = new ValidInstantiator();
	
	public static class ValidFromInstantiator implements Instantiator<ValidFrom> {
		
		public static final Property<ValidFrom, Date> date = newProperty();
		
		@Override
		public ValidFrom instantiate(
				PropertyLookup<ValidFrom> lookup) {
			ValidFrom validFrom = factory.createValidFrom();
			validFrom.setDate(lookup.valueOf(date, new Date()));
			
            return validFrom;
		}
	}
	
	public static final ValidFromInstantiator ValidFrom = new ValidFromInstantiator();
	
	public static class ValidToInstantiator implements Instantiator<ValidTo> {
		
		public static final Property<ValidTo, Date> date = newProperty();
		
		@Override
		public ValidTo instantiate(
				PropertyLookup<ValidTo> lookup) {
			ValidTo validFrom = factory.createValidTo();
			validFrom.setDate(lookup.valueOf(date, nullDate));
			
            return validFrom;
		}
	}
	
	public static final ValidToInstantiator ValidTo = new ValidToInstantiator();
    
	public static class ShsInstantiator implements Instantiator<Shs> {
	    // Shs
	    public static final Property<Shs, Billing> billing = newProperty();
		public static final Property<Shs, Confirm> confirm = newProperty();
		public static final Property<Shs, Customer> customer = newProperty();
		public static final Property<Shs, Direction> direction = newProperty();
		public static final Property<Shs, String> error = newProperty();
		public static final Property<Shs, Principal> principal = newProperty();
		public static final Property<Shs, List<Product>> products = newProperty();
	
		@Override
		public se.inera.axel.shs.xml.agreement.Shs instantiate(
				PropertyLookup<se.inera.axel.shs.xml.agreement.Shs> lookup) {
			Shs shs = factory.createShs();
            shs.setBilling(lookup.valueOf(billing, new SameValueDonor<Billing>(null)));
            shs.setConfirm(lookup.valueOf(confirm, a(Confirm)));
            shs.setCustomer(lookup.valueOf(customer, a(Customer)));
            shs.setDirection(lookup.valueOf(direction, a(Direction)));
            shs.setError(lookup.valueOf(error, nullString));
            shs.setPrincipal(lookup.valueOf(principal, a(Principal)));
            shs.getProduct().addAll(lookup.valueOf(products, listOf(a(Product))));
            return shs;
		}
	}
	
	public static final ShsInstantiator Shs = new ShsInstantiator();
	
	public static class BillingInstantiator implements Instantiator<Billing> {
		public static final Property<Billing, String> currency = newProperty();
	    public static final Property<Billing, String> description = newProperty();
	    public static final Property<Billing, List<? extends Object>> perExchangeOrPerVolumeOrPerPeriod = newProperty();
	    public static final Property<Billing, String> required = newProperty();

		@Override
		public se.inera.axel.shs.xml.agreement.Billing instantiate(
				PropertyLookup<se.inera.axel.shs.xml.agreement.Billing> lookup) {
			Billing billing = factory.createBilling();
            billing.setCurrency(lookup.valueOf(currency, "SEK"));
            billing.setDescription(lookup.valueOf(description, "Description"));
            // TODO what should the default value be?
            billing.getPerExchangeOrPerVolumeOrPerPeriod().addAll(lookup.valueOf(perExchangeOrPerVolumeOrPerPeriod, new ArrayList<Object>()));
            billing.setRequired("false");
            
            return billing;
		}
		
	}
	
	public static final BillingInstantiator Billing = new BillingInstantiator();
	
	public static class PerExchangeInstantiator implements Instantiator<PerExchange> {
		public static final Property<PerExchange, String> price = newProperty();
		
		@Override
		public se.inera.axel.shs.xml.agreement.PerExchange instantiate(
				PropertyLookup<se.inera.axel.shs.xml.agreement.PerExchange> lookup) {
			PerExchange perExchange = factory.createPerExchange();
            perExchange.setPrice(lookup.valueOf(price, "100"));
            
            return perExchange;
		}
		
	}
    
    public static final PerExchangeInstantiator PerExchange = new PerExchangeInstantiator();
    
    public static class PerVolumeInstantiator implements Instantiator<PerVolume> {
	    public static final Property<PerVolume, String> price = newProperty();
	    public static final Property<PerVolume, String> unit = newProperty();
	    
        @Override
        public PerVolume instantiate(PropertyLookup<PerVolume> lookup) {
            PerVolume perVolume = factory.createPerVolume();
            perVolume.setPrice(lookup.valueOf(price, "100"));
            perVolume.setUnit(lookup.valueOf(unit, ""));
            
            return perVolume;
        }
    }
    
    public static final PerVolumeInstantiator PerVolume = new PerVolumeInstantiator();
    
    public static class PerPeriodInstantiator implements Instantiator<PerPeriod> {
	    public static final Property<PerPeriod, String> price = newProperty();
	    public static final Property<PerPeriod, String> unit = newProperty();
	    
        @Override
        public PerPeriod instantiate(PropertyLookup<PerPeriod> lookup) {
            PerPeriod perPeriod = factory.createPerPeriod();
            perPeriod.setPrice(lookup.valueOf(price, "100"));
            perPeriod.setUnit(lookup.valueOf(unit, ""));
            
            return perPeriod;
        }
    }
    
    public static final PerPeriodInstantiator PerPeriod = new PerPeriodInstantiator();
    
    // Confirm
    public static class ConfirmInstantiator implements Instantiator<Confirm> {
	    public static final Property<Confirm, Boolean> required = newProperty();
	    
        @Override
        public Confirm instantiate(PropertyLookup<Confirm> lookup) {
            Confirm confirm = factory.createConfirm();
            confirm.setRequired(lookup.valueOf(required, Boolean.FALSE));
            
            return confirm;
        }
    }
    
    public static final ConfirmInstantiator Confirm = new ConfirmInstantiator();
    
    public static class CustomerInstantiator implements Instantiator<Customer> {
	    public static final Property<Customer, String> commonName = newProperty();
	    public static final Property<Customer, String> labeledUri = newProperty();
	    public static final Property<Customer, String> value = newProperty();
	    
        @Override
        public Customer instantiate(PropertyLookup<Customer> lookup) {
            Customer confirm = factory.createCustomer();
            confirm.setCommonName(lookup.valueOf(commonName, DEFAULT_CUSTOMER_CN));
            confirm.setLabeledURI(lookup.valueOf(labeledUri, nullString));
            confirm.setvalue(lookup.valueOf(value, DEFAULT_CUSTOMER));
            
            return confirm;
        }
    }
    
    public static final CustomerInstantiator Customer = new CustomerInstantiator();
    
    // Direction
    public static class DirectionInstantiator implements Instantiator<Direction> {
	    public static final Property<Direction, String> description = newProperty();
	    public static final Property<Direction, String> flow = newProperty();
	    
        @Override
        public Direction instantiate(PropertyLookup<Direction> lookup) {
            Direction direction = factory.createDirection();
            direction.setDescription(lookup.valueOf(description, nullString));
            direction.setFlow(lookup.valueOf(flow, "any"));
            
            return direction;
        }
    }
    
    public static final DirectionInstantiator Direction = new DirectionInstantiator();
    
    public static class PrincipalInstantiator implements Instantiator<Principal> {
	    public static final Property<Principal, String> commonName = newProperty();
	    public static final Property<Principal, String> labeledUri = newProperty();
	    public static final Property<Principal, String> value = newProperty();
	    
        @Override
        public Principal instantiate(PropertyLookup<Principal> lookup) {
            Principal principal = factory.createPrincipal();
            principal.setCommonName(lookup.valueOf(commonName, DEFAULT_PRINCIPAL_CN));
            principal.setLabeledURI(lookup.valueOf(labeledUri, nullString));
            principal.setvalue(lookup.valueOf(value, DEFAULT_PRINCIPAL));
            
            return principal;
        }
    }
    
    public static final PrincipalInstantiator Principal = new PrincipalInstantiator();
    
    public static class ProductInstantiator implements Instantiator<Product> {
	    public static final Property<Product, String> commonName = newProperty();
	    public static final Property<Product, String> labeledUri = newProperty();
	    public static final Property<Product, String> value = newProperty();
	    
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            Product product = factory.createProduct();
            product.setCommonName(lookup.valueOf(commonName, "AXEL-TEST1"));
            product.setLabeledURI(lookup.valueOf(labeledUri, nullString));
            product.setvalue(lookup.valueOf(value, DEFAULT_PRODUCT_ID));
            
            return product;
        }
    }
    
    public static final ProductInstantiator Product = new ProductInstantiator();
}
