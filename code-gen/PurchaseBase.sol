// SPDX-License-Identifier: GPL 3.0
pragma solidity <0.9.0;

interface PurchaseBase  {
	function _abort() external;
	function _confirmPurchase() external;
	function _confirmReceived() external;
	function _refundSeller() external;
}
