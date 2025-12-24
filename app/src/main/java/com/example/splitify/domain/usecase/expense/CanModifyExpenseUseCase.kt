package com.example.splitify.domain.usecase.expense

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import javax.inject.Inject

class CanModifyExpenseUseCase @Inject constructor(){

    operator fun invoke(
        expense: Expense,
        currentUserMember: TripMember?,
        currentUserId: String?
        ): Boolean {

        if (currentUserMember == null || currentUserId == null) {
            return false
        }

        // Rule 1: Trip admin can modify any expense
        if (currentUserMember.role == MemberRole.ADMIN) {
            return true
        }

        // Rule 2: Expense creator can modify their own expenses
        if (expense.createdBy == currentUserId) {
            return true
        }

        // Rule 3: If registered user is the payer (and has userId set)
        if (currentUserMember.userId != null &&
            currentUserMember.id == expense.paidBy) {
            return true
        }

        return false
    }
}