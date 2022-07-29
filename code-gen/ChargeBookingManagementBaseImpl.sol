// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;

import "./ChargeBookingManagement.sol";



abstract contract ChargeBookingManagementBaseImpl is ChargeBookingManagement {
	mapping(uint => Booking) public chargeBookings;

	event ChargeBookingCreated(Booking b);
	event ChargeBookingUpdated(uint id, string bookingStart, string bookingEnd, uint verifyCode);
	event ChargeBookingCanceled(uint id);
}
