package com.example.splitify.data.local

import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.domain.model.Trip


//Extension functions to convert between Entity and Domain models
//Convert TripEntity (database) to Trip (domain)
fun TripEntity.toDomainModel(): Trip {
    return Trip(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        inviteCode = inviteCode,
        createdBy = createdBy,
        createdAt = createdAt,
        isLocal = isLocal
    )
}

//Convert Trip(domain) to TripEntity(database)
fun Trip.toEntity(): TripEntity{
    return TripEntity(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        inviteCode = inviteCode,
        createdBy = createdBy,
        createdAt = createdAt,
        isLocal = isLocal,
        isSynced = !isLocal,
        lastModified = System.currentTimeMillis()

    )
}

//Convert list of Entities to Domain model
fun List<TripEntity>.toDomainModels(): List<Trip>{
    return map{it.toDomainModel()}
}

//Convert list of domain model to entities
fun List<Trip>.toEntities(): List<TripEntity>{
    return map { it.toEntity() }
}