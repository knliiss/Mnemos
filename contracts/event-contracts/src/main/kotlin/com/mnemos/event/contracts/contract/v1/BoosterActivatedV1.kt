package com.mnemos.event.contracts.contract.v1

import com.mnemos.event.contracts.shared.ContractBoosterType

data class BoosterActivatedV1(
    val activatedBy: String,
    val type: ContractBoosterType,
)
