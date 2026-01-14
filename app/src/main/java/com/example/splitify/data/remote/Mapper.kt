package com.example.splitify.data.remote

import com.example.splitify.data.local.entity.ExpenseEntity
import com.example.splitify.data.local.entity.ExpenseSplitEntity
import com.example.splitify.data.local.entity.SettlementEntity
import com.example.splitify.data.local.entity.TripEntity
import com.example.splitify.data.local.entity.TripMemberEntity
import com.example.splitify.data.remote.dto.ExpenseDto
import com.example.splitify.data.remote.dto.ExpenseSplitDto
import com.example.splitify.data.remote.dto.ExpenseUpdateDto
import com.example.splitify.data.remote.dto.SettlementDto
import com.example.splitify.data.remote.dto.TripDto
import com.example.splitify.data.remote.dto.TripMemberDto
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Trip
import com.example.splitify.util.formatDate
import com.example.splitify.util.formatDateTime
import java.time.Instant
import java.time.ZoneOffset


/**
 * Convert Room entity to Supabase DTO
 * Used when uploading to Supabase
 */

fun Trip.toDto() = TripDto(
    id = id,
    name = name,
    description = description,
    createdBy = createdBy,
    inviteCode = inviteCode,
    startDate = startDate,
    endDate = endDate
)

fun TripEntity.toDto(): TripDto {
    return TripDto(
        id = this.id,
        name = this.name,
        description = this.description,
        createdBy = this.createdBy,
        inviteCode = this.inviteCode,
        startDate = this.startDate,
        endDate = this.endDate
    )
}
fun TripDto.toEntity(createdAt: Long): TripEntity {
    return TripEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate,
        inviteCode = this.inviteCode,
        createdBy = this.createdBy,
        createdAt = createdAt,
        // Logic: Since it's coming FROM the server, these must be:
        isLocal = false,
        isSynced = true,
        lastModified = System.currentTimeMillis()
    )
}

fun TripMemberEntity.toDto(): TripMemberDto {
    return TripMemberDto(
        id = id,
        tripId = tripId,
        userId = userId,
        displayName = displayName,
        role = role,
        avatarUrl = avatarUrl,
        joinedAt = Instant.ofEpochMilli(joinedAt)
            .atOffset(ZoneOffset.UTC)
            .toString()  // Convert timestamp to ISO string
    )
}


fun TripMemberDto.toEntity(): TripMemberEntity {
    return TripMemberEntity(
        id = id,
        tripId = tripId,
        userId = userId,
        displayName = displayName,
        role = role,
        avatarUrl = avatarUrl,
        joinedAt = joinedAt?.let {
            Instant.parse(it).toEpochMilli()
        } ?: System.currentTimeMillis(),
        isSynced = true,  // Just downloaded from server
        lastModified = System.currentTimeMillis()
    )
}


fun ExpenseEntity.toDto(): ExpenseDto {
    return ExpenseDto(
        id = id,
        tripId = tripId,
        description = description,
        amount = amount,
        category = category,
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = Instant.ofEpochMilli(createdAt)
            .atOffset(ZoneOffset.UTC)
            .toString(),
        updatedAt = updatedAt?.let {
            Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toString()
        }
    )
}

fun ExpenseDto.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        tripId = tripId,
        description = description,
        amount = amount,
        category = category,
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = createdAt?.let {
            Instant.parse(it).toEpochMilli()
        } ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.let {
            Instant.parse(it).toEpochMilli()
        },
        isLocal = false,
        isSynced = true,
        lastModified = System.currentTimeMillis()
    )
}

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        tripId = tripId,
        description = description,
        amount = amount,
        category = Category.valueOf(category),
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Expense.toEntity(
    isLocal: Boolean = true,
    isSynced: Boolean = false
): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        tripId = tripId,
        description = description,
        amount = amount,
        category = category.name,
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isLocal = isLocal,
        isSynced = isSynced,
        lastModified = System.currentTimeMillis()
    )
}

fun Expense.toDto(): ExpenseDto {
    return ExpenseDto(
        id = id,
        tripId = tripId,
        description = description,
        amount = amount,
        category = category.name,
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = null,
        updatedAt = null
    )
}

fun Expense.toUpdateDto(): ExpenseUpdateDto {
    return ExpenseUpdateDto(
        tripId = tripId,
        description = description,
        amount = amount,
        category = category.name,
        expenseDate = expenseDate,
        paidBy = paidBy,
        createdBy = createdBy,
        paidByName = paidByName,
        isGroupExpense = isGroupExpense,
        createdAt = null,
        updatedAt = null
    )
}



fun ExpenseSplitEntity.toDto(): ExpenseSplitDto {
    return ExpenseSplitDto(
        id = id,
        expenseId = expenseId,
        memberId = memberId,
        memberName = memberName,
        amountOwed = amountOwed,
        createdAt = Instant.ofEpochMilli(createdAt)
            .atOffset(ZoneOffset.UTC)
            .toString()
    )
}

fun ExpenseSplitDto.toEntity(): ExpenseSplitEntity {
    return ExpenseSplitEntity(
        id = id,
        expenseId = expenseId,
        memberId = memberId,
        memberName = memberName,
        amountOwed = amountOwed,
        createdAt = createdAt?.let {
            Instant.parse(it).toEpochMilli()
        } ?: System.currentTimeMillis()
    )
}


fun SettlementEntity.toDto(): SettlementDto {
    return SettlementDto(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        status = status,
        notes = notes,
        createdAt = Instant.ofEpochMilli(createdAt)
            .atOffset(ZoneOffset.UTC)
            .toString(),
        settledAt = settledAt?.let {
            Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toString()
        }
    )
}

fun SettlementDto.toEntity(): SettlementEntity {
    return SettlementEntity(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        status = status,
        notes = notes,
        createdAt = createdAt?.let {
            Instant.parse(it).toEpochMilli()
        } ?: System.currentTimeMillis(),
        settledAt = settledAt?.let {
            Instant.parse(it).toEpochMilli()
        }
    )
}