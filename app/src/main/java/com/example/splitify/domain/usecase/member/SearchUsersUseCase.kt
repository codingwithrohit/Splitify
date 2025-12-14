package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.repository.TripMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import com.example.splitify.util.Result

class SearchUsersUseCase @Inject constructor(
    private val repository: TripMemberRepository
) {
    operator fun invoke(query: String): Flow<Result<List<TripMember>>> {
        if (query.length < 2) {
            return flowOf(Result.Success(emptyList()))
        }
        return repository.searchUsers(query)
    }
}