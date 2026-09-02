package com.mnemos.account.persistence.mapper

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.persistence.mapper.AccountPersistenceMapper.toEntity
import com.mnemos.account.persistence.mapper.AccountPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountPersistenceMapperTest {
    @Test
    fun `update persists transferred owner member id`() {
        val originalOwnerMemberId = AccountMemberId.generate()
        val newOwnerMemberId = AccountMemberId.generate()
        val account =
            Account.create(
                id = AccountId.generate(),
                ownerMemberId = originalOwnerMemberId,
                cristalixUuid = CristalixUuid(UUID.randomUUID()),
                cristalixNickname = CristalixNickname("OwnerTransferTest"),
            )
        val entity = account.toEntity()

        account.transferOwnership(newOwnerMemberId)
        entity.updateFrom(account)

        assertEquals(newOwnerMemberId.id, entity.ownerMemberId)
    }
}
