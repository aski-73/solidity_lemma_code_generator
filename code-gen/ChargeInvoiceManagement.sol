// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;



interface ChargeInvoiceManagement  {
	
	struct Invoice {
		uint id;
		uint bookingId;
		string invoiceAmount;
	}
	
	function createInvoice(
		Invoice  memory i
	) external;
	function readInvoice(
		uint  id
	) external returns (Invoice memory);
}
