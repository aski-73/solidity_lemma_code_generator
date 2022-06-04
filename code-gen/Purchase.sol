// SPDX-License-Identifier: GPL 3.0
pragma solidity <0.9.0;

import "./PurchaseBase.sol";



contract Purchase is PurchaseBase {
	int32 public value;
	address payable public seller;
	address payable public buyer;
	constructor()  payable public {
		state = "CREATED";
		//Entry Activity of new State
		seller = payable(msg.sender);
		value = msg.value / 2;
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
		transfer(balance, seller);
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
		transfer(value, buyer);
	}
	function refundSeller() internal {
		state = "REFUNDING";
		//Entry Activity of new State
		emit SellerRefunded();
		transfer(3*value, seller);
	}
	function handle(
		string memory input
	) public {
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
		string memory a,
		string memory b
	) internal returns (bool) {
		return (keccak256(abi.encodePacked((a))) == keccak256(abi.encodePacked((b))));
	}
	function transfer(
		uint memory amount,
		address memory receiver
	) internal {
		address self = address(this);
		uint256 balance = self.balance;
		if (balance >= amount) {
			receiver.transfer(amount);
		}
	}
	
}
