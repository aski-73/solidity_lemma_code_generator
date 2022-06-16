// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;



interface ChargeBookingManagement  {
	
	struct Booking {
		uint id;
		string bookerId;
		string parkingSpaceId;
		string bookingStart;
		string bookingEnd;
		string parkingPricePerHour;
		string chargingPricePerKWh;
		uint verifyCode;
		bool isCanceled;
	}
	
	function createBooking(
		Booking  memory b
	) external;
	function readBooking(
		uint  id
	) external returns (Booking memory);
	function updateBooking(
		uint  id,
		string  memory bookingStart,
		string  memory bookingEnd,
		uint  verifyCode
	) external;
	function cancelBooking(
		uint  id
	) external;
}
