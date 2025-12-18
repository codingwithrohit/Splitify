package com.example.splitify.data.local

import android.icu.text.SymbolTable
import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.data.local.entity.TripMemberEntity
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.ExpenseSplit
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripMember
import java.time.ZoneId


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

fun ExpenseEntity.toDomainModel(): Expense{
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
        createdAt = createdAt
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

        isLocal = false,
        isSynced = true,
        lastModified = System.currentTimeMillis()
    )
}

fun List<ExpenseEntity>.toExpenseDomainModels(): List<Expense>{
    return map { it.toDomainModel() }
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