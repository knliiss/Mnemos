package com.mnemos.event.contracts.contract.v1

import com.mnemos.event.contracts.shared.ContractRainType

data class RainActivatedV1(
    val activatedBy: String,
    val type: ContractRainType,
)
