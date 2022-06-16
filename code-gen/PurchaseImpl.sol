// SPDX-License-Identifier: GPL 3.0
pragma solidity <0.9.0;

import "./PurchaseBaseImpl.sol";

contract PurchaseImpl is PurchaseBaseImpl {
    modifier onlyBuyer() {
        if (msg.sender != buyer)
            revert OnlyBuyer();
        _;
    }
    modifier onlySeller() {
        if (msg.sender != seller)
            revert OnlySeller();
        _;
    }
    modifier inState(string memory _state) {
        if (!isEqual(state, _state))
            revert InvalidState();
        _;
    }
    function _abort() external override onlySeller inState("CREATED") {
        abort();
    }
    function _confirmPurchase() external override inState("CREATED") {
        handle("confirmPurchase");
    }

    function _confirmReceived() external override onlyBuyer inState("LOCKED") {
        confirmReceived();
    }

    function _refundSeller() external override onlySeller inState("RELEASE") {
        refundSeller();
    }
    function init() internal override {
        super.init();
        if ((2 * value ) != msg.value)
            revert ValueNotEven();
    }
}
