// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.8;

library Common {
    struct Test {
        string name;
    }

    function isEqual(string memory a, string memory b) internal pure returns (bool) {
        return (keccak256(abi.encodePacked((a))) == keccak256(abi.encodePacked((b))));
    }

    function transfer(uint amount, address payable receiver) internal {
        if (address(this).balance >= amount)
            receiver.transfer(amount);
    }
}
