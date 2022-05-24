//SPDX-License-Identifier:MIT
pragma solidity ^0.8.1;

contract ChargeBookingManagement {

    mapping(uint => Booking) chargeBookings;
   string private testVar;

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

    modifier checkBookingExists(uint _id) {
        if(chargeBookings[_id].id != 0) {
            revert("Charge Booking already exist!"); //Transaktion verweigern
        }
        _;
    }

    modifier checkBookingNotExists(uint _id) {
        if(chargeBookings[_id].id == 0) {
            revert("Charge Booking does not exist!");
        }
        _;
    }

    event ChargeBookingCreated(uint id, string bookerId, string parkingSpaceId, string bookingStart, string bookingEnd,
        string parkingPricePerHour, string chargingPricePerKWh, uint verifyCode, bool isCanceled);
    event ChargeBookingUpdated(uint id, string bookingStart, string bookingEnd, uint verifyCode);
    event ChargeBookingCanceled(uint id);


    function createBooking(uint _id,
                           string memory _bookerId,
                           string memory _parkingSpaceId,
                           string memory _bookingStart,
                           string memory _bookingEnd,
                           string memory _parkPricePerHour,
                           string memory _chargePricePerKWh,
                           uint _verifyCode) external checkBookingExists(_id)
    {
        Booking memory booking = Booking(_id, _bookerId, _parkingSpaceId, _bookingStart, _bookingEnd,
                                    _parkPricePerHour, _chargePricePerKWh, _verifyCode, false);
        chargeBookings[_id] = booking;
        emit ChargeBookingCreated(_id, _bookerId, _parkingSpaceId, _bookingStart, _bookingEnd,
                                _parkPricePerHour, _chargePricePerKWh, _verifyCode, false);
    }

    function readBooking(uint _id) external view checkBookingNotExists(_id)
        returns(uint, string memory, string memory, string memory, string memory, string memory, string memory, uint, bool) {
        Booking memory booking = chargeBookings[_id];
        return(
            booking.id,
            booking.bookerId,
            booking.parkingSpaceId,
            booking.bookingStart,
            booking.bookingEnd,
            booking.parkingPricePerHour,
            booking.chargingPricePerKWh,
            booking.verifyCode,
            booking.isCanceled
        );
    }

    function updateBooking(uint _id,
                           string memory _bookingStart,
                           string memory _bookingEnd,
                           uint _verifyCode) external checkBookingNotExists(_id) {
        Booking memory booking = chargeBookings[_id];
        booking.bookingStart = _bookingStart;
        booking.bookingEnd = _bookingEnd;
        booking.verifyCode = _verifyCode;
        chargeBookings[_id] = booking;
        emit ChargeBookingUpdated(_id, _bookingStart, _bookingEnd, _verifyCode);
    }

    function cancelBooking(uint _id) external checkBookingNotExists(_id) {
        Booking memory booking = chargeBookings[_id];
        booking.isCanceled = true;
        chargeBookings[_id] = booking;
        emit ChargeBookingCanceled(_id);
    }

    function test(string storage _testVar) private {

    }

    function callerTest() public {
        test("test");
    }
}
