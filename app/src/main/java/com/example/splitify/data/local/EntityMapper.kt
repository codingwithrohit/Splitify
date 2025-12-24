package com.example.splitify.data.local

import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.SettlementEntity
import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.data.local.entity.TripMemberEntity
import com.example.splitify.data.local.entity.relations.ExpenseWithSplits
import com.example.splitify.data.local.entity.relations.ExpenseWithSplitsRelation
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripMember


//Extension functions to convert between Entity and Domain models
//Convert TripEntity (database) to Trip (domain)
fun TripEntity.toDomain(): Trip {
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
    return map{it.toDomain()}
}

//Convert list of domain model to entities
fun List<Trip>.toEntities(): List<TripEntity>{
    return map { it.toEntity() }
}

fun ExpenseEntity.toDomain(): Expense{
    return Expense(
        id = id,
        tripId = tripId,
        amount = amount,
        description = description,
        category = Category.valueOf(category.uppercase()),
        expenseDate = expenseDate,
        paidBy = paidBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = createdAt,
        createdBy = createdBy
    )
}

fun Expense.toEntity(): ExpenseEntity{
    return ExpenseEntity(
        id = id,
        tripId = tripId,
        amount = amount,
        description = description,
        category = category.name.lowercase(),
        expenseDate = expenseDate,
        paidBy = paidBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = createdAt,
        createdBy = createdBy,
        isLocal = false,
        isSynced = true,
        lastModified = System.currentTimeMillis()
    )
}

fun List<ExpenseEntity>.toExpenseDomainModels(): List<Expense>{
    return map { it.toDomain() }
}

fun ExpenseWithSplitsRelation.toDomain(): ExpenseWithSplits {
    return ExpenseWithSplits(
        expense = expense.toDomain(),
        splits = splits.map { it.toDomain() }
    )
}


fun TripMemberEntity.toDomain(): TripMember {
    return TripMember(
        id = id,
        tripId = tripId,
        userId = userId,
        displayName = displayName,
        role = when (role.lowercase()) {
            "admin" -> MemberRole.ADMIN
            else -> MemberRole.MEMBER
        },
        joinedAt = joinedAt,
        avatarUrl = avatarUrl
    )
}

fun TripMember.toEntity(): TripMemberEntity {
    return TripMemberEntity(
        id = id,
        tripId = tripId,
        userId = userId,
        displayName = displayName,
        role = role.name.lowercase(),
        joinedAt = joinedAt,
        avatarUrl = avatarUrl
    )
}

fun ExpenseSplitEntity.toDomain(): ExpenseSplit {
    return ExpenseSplit(
        id = id,
        expenseId = expenseId,
        memberId = memberId,
        memberName = memberName,
        amountOwed = amountOwed,
        createdAt = createdAt
    )
}

fun ExpenseSplit.toEntity(): ExpenseSplitEntity {
    return ExpenseSplitEntity(
        id = id,
        expenseId = expenseId,
        memberId = memberId,
        memberName = memberName,
        amountOwed = amountOwed,
        createdAt = createdAt
    )
}

fun SettlementEntity.toDomain(): Settlement{
    return Settlement(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        status = status.toSettlementStatus(),
        notes = notes,
        settledAt = settledAt,
        createdAt = createdAt
    )
}

fun Settlement.toEntity(): SettlementEntity{
    return SettlementEntity(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        status = status.name,
        notes = notes,
        settledAt = settledAt,
        createdAt = createdAt
    )
}

fun List<SettlementEntity>.toDomain(): List<Settlement> {
    return map { it.toDomain() }
}

private fun String.toSettlementStatus(): SettlementStatus {
    return try {
        SettlementStatus.valueOf(this)
    } catch (e: IllegalArgumentException) {
        // Default to PENDING if invalid status in database
        SettlementStatus.PENDING
    }
}