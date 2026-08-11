package com.mnemos.notification.application.dungeon.usecase.create

interface CreateDungeonSubscriptionUseCase {
    fun execute(command: CreateDungeonSubscriptionCommand): CreateDungeonSubscriptionResult
}
