// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.8;

interface InsuranceProviderBase {
	enum STATE {
		NO_CLAIM,
		CRASH_OCCURED,
		UNCONFIRMED,
		CLAIM_APPROVED,
		CUSTOMER_OBJECTION,
		INSURER_OBJECTION,
		COURT
	}

	function addContract(PolicyContract memory c);
	function removeContract(string memory address);
}
