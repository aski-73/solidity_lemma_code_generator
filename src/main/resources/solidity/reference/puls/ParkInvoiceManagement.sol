//SPDX-License-Identifier:MIT
pragma solidity ^0.8.1;

contract ParkInvoiceManagement {

    mapping(uint => Invoice) invoices;

    struct Invoice {
        uint id;
        uint bookingId;
        string invoiceAmount;
    }

    modifier checkInvoiceNotExists(uint _id) {
        if(invoices[_id].id == 0) {
            revert("Park Invoice does not exist!");
        }
        _;
    }

    event ParkInvoiceCreated(uint id, uint bookingId, string invoiceAmount);

    function createInvoice(uint _id, uint _bookingId, string memory _invoiceAmount) external {
        Invoice memory invoice = Invoice(_id,_bookingId,_invoiceAmount);
        invoices[_id] = invoice;
        emit ParkInvoiceCreated(_id, _bookingId, _invoiceAmount);
    }

    function readInvoice(uint _id) external view checkInvoiceNotExists(_id) returns(uint, uint, string memory) {
        Invoice memory invoice = invoices[_id];
        return(invoice.id, invoice.bookingId, invoice.invoiceAmount);
    }
}