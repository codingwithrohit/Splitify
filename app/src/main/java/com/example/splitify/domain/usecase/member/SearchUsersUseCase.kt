package com.example.splitify.domain.usecase.member

import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.model.User
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import com.example.splitify.util.Result

class SearchUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(query: String): Flow<Result<List<User>>> {
        return authRepository.searchUsersByUsername(query)
    }
}