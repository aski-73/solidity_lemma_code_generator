// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.8;
import "./InsuranceBase.sol";

contract FlightInsuranceBaseImpl is InsuranceBase {
	function addContract(PolicyContract memory c);
	function removeContract(string memory address);
}
