// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.8;

library Doctor {
    struct aD {
        uint id;
        string disease;
    }

    function getDiagnosisId(aD storage self) public view returns (uint) {
        return self.id;
    }

    function getB() public pure returns (uint) { return 3; }

    function setA(string memory s, aD memory self) public {
        self.disease = s;
    }
}
