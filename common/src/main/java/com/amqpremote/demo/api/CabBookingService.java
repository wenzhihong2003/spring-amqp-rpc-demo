package com.amqpremote.demo.api;

public interface CabBookingService {
    Booking bookRide(String pickUpLocation) throws BookingException;
}
