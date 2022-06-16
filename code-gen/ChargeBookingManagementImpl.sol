// SPDX-License-Identifier: GPL 3.0
pragma solidity <0.9.0;

import "./ChargeBookingManagementBaseImpl.sol";

contract ChargeBookingManagementImpl is ChargeBookingManagementBaseImpl {
    modifier checkBookingExists(uint _id) {
        if (chargeBookings[_id].id != 0) {
            revert("Charge Booking already exist!");
            //Transaktion verweigern
        }
        _;
    }

    modifier checkBookingNotExists(uint _id) {
        if (chargeBookings[_id].id == 0) {
            revert("Charge Booking does not exist!");
        }
        _;
    }

    function createBooking(Booking memory b) public override checkBookingExists(b.id) {
        Booking memory booking = Booking(b.id, b.bookerId, b.parkingSpaceId, b.bookingStart, b.bookingEnd,
            b.parkingPricePerHour, b.chargingPricePerKWh, b.verifyCode, false);
        chargeBookings[b.id] = booking;
        emit ChargeBookingCreated(booking);
    }

    function readBooking(uint id) public override view checkBookingNotExists(id) returns (Booking memory) {
        return chargeBookings[id];
    }

    function updateBooking(uint id, string memory bookingStart, string memory bookingEnd, uint verifyCode) public override checkBookingNotExists(id) {
        Booking memory booking = chargeBookings[id];
        booking.bookingStart = bookingStart;
        booking.bookingEnd = bookingEnd;
        booking.verifyCode = verifyCode;
        chargeBookings[id] = booking;
        emit ChargeBookingUpdated(id, bookingStart, bookingEnd, verifyCode);
    }

    function cancelBooking(uint id) public override checkBookingNotExists(id)  {
        Booking memory booking = chargeBookings[id];
        booking.isCanceled = true;
        chargeBookings[id] = booking;
        emit ChargeBookingCanceled(id);
    }
}
