// SPDX-License-Identifier: GPL 3.0
pragma solidity >= 0.8.0 <0.9.0;

import "./Purchase.sol";

contract PurchaseBaseImpl is Purchase {
	uint public value;
	address payable public seller;
	address payable public buyer;
	string public state = "START";
	constructor()   {
		init();
	}

	error OnlyBuyer();
	error OnlySeller();
	error InvalidState();
	error ValueNotEven();

	event Aborted();
	event PurchaseConfirmed();
	event ItemReceived();
	event SellerRefunded();

	function abort() internal {
		state = "ABORTING";
		//Entry Activity of new State
		emit Aborted();
		seller.transfer(address(this).balance);
	}
	function confirmPurchase() internal {
		state = "LOCKED";
		//Entry Activity of new State
		emit PurchaseConfirmed();
		buyer = payable(msg.sender);
	}
	function confirmReceived() internal {
		state = "RELEASE";
		//Entry Activity of new State
		emit ItemReceived();
		buyer.transfer(value);
	}
	function refundSeller() internal {
		state = "REFUNDING";
		//Entry Activity of new State
		emit SellerRefunded();
		seller.transfer(3 * value);
	}
	function init() internal {
		state = "CREATED";
		//Entry Activity of new State
		seller = payable(msg.sender);
		value = msg.value / 2;
	}
	function handle(
		string  memory input
	) public override payable {
		if (isEqual(state, "CREATED") && isEqual(input, "abort()") && msg.sender == seller) {
			abort();
		}
		else if (isEqual(state, "ABORTING")) {
			state = "INACTIVE";
		}
		else if (isEqual(state, "CREATED") && isEqual(input, "confirmPurchase()") && msg.value == 2 * value) {
			confirmPurchase();
		}
		else if (isEqual(state, "LOCKED") && isEqual(input, "confirmReceived()") && msg.sender == buyer) {
			confirmReceived();
		}
		else if (isEqual(state, "RELEASE") && isEqual(input, "refundSeller()") && msg.sender == seller) {
			refundSeller();
		}
		else if (isEqual(state, "REFUNDING")) {
			state = "INACTIVE";
		}
		else if (isEqual(state, "INACTIVE")) {
			state = "END";
		}
	}
	function isEqual(
		string  memory a,
		string  memory b
	) internal pure  returns (
		bool  name
	) {
		return (keccak256(abi.encodePacked((a))) == keccak256(abi.encodePacked((b))));
	}
}
