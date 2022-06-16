// SPDX-License-Identifier: GPL 3.0
pragma solidity >=0.8.0 <0.9.0;

import "./ChargeInvoiceManagementBaseImpl.sol";

contract ChargeInvoiceManagementImpl is ChargeInvoiceManagementBaseImpl {
	modifier checkInvoiceNotExists(uint _id) {
		if(invoices[_id].id == 0) {
			revert("Charge Invoice does not exist!");
		}
		_;
	}

	function createInvoice(Invoice memory i) public override {
		Invoice memory invoice = Invoice(i.id, i.bookingId, i.invoiceAmount);
		invoices[i.id] = invoice;
		emit ChargeInvoiceCreated(i);
	}
	function readInvoice(uint id) public view override checkInvoiceNotExists(id) returns (Invoice memory) {
		Invoice memory invoice = invoices[id];
		return invoice;
	}
}
