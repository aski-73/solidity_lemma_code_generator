//SPDX-License-Identifier:MIT
pragma solidity ^0.8.1;

contract ParkBookingManagement {

    mapping (uint => Booking) parkBookings;

    struct Booking {
        uint id;
        string bookerId;
        string parkingSpaceId;
        string bookingStart;
        string bookingEnd;
        string pricePerHour;
        bool isCanceled;
    }

    modifier checkBookingExists(uint _id) {
        if(parkBookings[_id].id != 0) {
            revert();
        }
        _;
    }

    modifier checkBookingNotExists(uint _id) {
        if(parkBookings[_id].id == 0) {
            revert();
        }
        _;
    }

    event ParkBookingCreated(uint id, string bookerId, string parkingSpaceId, string bookingStart,
        string bookingEnd, string pricePerHour, bool isCanceled);
    event ParkBookingUpdated(uint id, string bookingStart, string bookingEnd);
    event ParkBookingCanceled(uint id);

    function createBooking(uint _id,
                           string memory _bookerId,
                           string memory _parkingSpaceId,
                           string memory _bookingStart,
                           string memory _bookingEnd,
                           string memory _pricePerHour) external checkBookingExists(_id)
    {
        Booking memory booking =
            Booking(_id,_bookerId,_parkingSpaceId,_bookingStart,_bookingEnd,_pricePerHour,false);
        parkBookings[_id] = booking;
        emit ParkBookingCreated(_id, _bookerId, _parkingSpaceId, _bookingStart, _bookingEnd, _pricePerHour, false);
    }

    function readBooking(uint _id) external view checkBookingNotExists(_id)
        returns(uint, string memory, string memory, string memory, string memory, string memory, bool)
    {
        Booking memory booking = parkBookings[_id];
        return (
            booking.id,
            booking.bookerId,
            booking.parkingSpaceId,
            booking.bookingStart,
            booking.bookingEnd,
            booking.pricePerHour,
            booking.isCanceled
        );
    }

    function updateBooking(uint _id, string memory _bookingStart, string memory _bookingEnd)
        external checkBookingNotExists(_id) {
        Booking memory booking = parkBookings[_id];
        booking.bookingStart = _bookingStart;
        booking.bookingEnd = _bookingEnd;
        parkBookings[_id] = booking;
        emit ParkBookingUpdated(_id, _bookingStart, _bookingEnd);
    }

    function cancelBooking(uint _id) external checkBookingNotExists(_id) {
        parkBookings[_id].isCanceled = true;
        emit ParkBookingCanceled(_id);
    }
}
