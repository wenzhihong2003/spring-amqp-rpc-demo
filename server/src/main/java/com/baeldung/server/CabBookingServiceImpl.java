package com.baeldung.server;

import com.baeldung.api.Booking;
import com.baeldung.api.BookingException;
import com.baeldung.api.CabBookingService;

public class CabBookingServiceImpl implements CabBookingService {
    @Override
    public Booking bookRide(String pickUpLocation) throws BookingException {
        System.out.println("-----------------------in server....................");
        return new Booking("----------pickUpLocation------bookiing--from server---------------");
    }
}
