// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;

import "./ChargeInvoiceManagement.sol";



abstract contract ChargeInvoiceManagementBaseImpl is ChargeInvoiceManagement {
	mapping(uint => Invoice) public invoices;

	event ChargeInvoiceCreated(Invoice i);

	function createInvoice(
		Invoice  memory i
	) public virtual {
		revert("IMPLEMENTATION REQUIRED");
	}
	function readInvoice(
		uint  id
	) public virtual returns (Invoice memory) {
		revert("IMPLEMENTATION REQUIRED");
	}
}
