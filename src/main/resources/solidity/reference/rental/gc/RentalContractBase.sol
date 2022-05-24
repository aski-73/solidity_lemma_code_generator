// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.8;

import "../hc/RentalContract.sol";

interface RentalContractBase is RentalContract {
    enum Test {
        A, B, C
    }
    struct Tenant {
        address blockchainAddress;
        string name;
    }

    struct Owner {
        address payable blockchainAddress;
        string name;
    }

    struct RentalObject {
        uint id;
        uint pricePerDay;
    }

    function initContract(Owner memory owner, RentalObject memory obj) external;

    function grantAccess(Tenant memory tenant, uint endDate) external;

    function exit() external;
}
