package com.amqpremote.demo.server;

import com.amqpremote.demo.api.Booking;
import com.amqpremote.demo.api.BookingException;
import com.amqpremote.demo.api.CabBookingService;

public class CabBookingServiceImpl implements CabBookingService {
    @Override
    public Booking bookRide(String pickUpLocation) throws BookingException {
        System.out.println("-----------------------in server....................");
        return new Booking("----------pickUpLocation------bookiing--from server---------------");
    }
}
