// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;

import "./ChargeBookingManagement.sol";


abstract contract ChargeBookingManagementBaseImpl is ChargeBookingManagement {
    mapping(uint => Booking) public chargeBookings;

    event ChargeBookingCreated(Booking b);
    event ChargeBookingUpdated(uint id, string bookingStart, string bookingEnd, uint verifyCode);
    event ChargeBookingCanceled(uint id);

    function createBooking(Booking  memory b) public virtual {
        revert("IMPLEMENTATION REQUIRED");
    }

    function readBooking(uint id) public virtual returns (Booking memory) {
        revert("IMPLEMENTATION REQUIRED");
    }

    function updateBooking(uint id, string  memory bookingStart, string  memory bookingEnd, uint verifyCode) public virtual {
        revert("IMPLEMENTATION REQUIRED");
    }

    function cancelBooking(uint id) public virtual {
        revert("IMPLEMENTATION REQUIRED");
    }
}
