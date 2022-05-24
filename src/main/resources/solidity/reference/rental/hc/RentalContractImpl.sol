pragma solidity >=0.8;

import "../gc/RentalContractBaseImpl.sol";


contract RentalContractImpl is RentalContractBaseImpl{
	enum ActionChoices { GoLeft, GoRight, GoStraight, SitStill }
}

