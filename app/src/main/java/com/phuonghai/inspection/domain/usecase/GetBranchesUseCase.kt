package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Branch
import com.phuonghai.inspection.domain.repository.IBranchRepository
import javax.inject.Inject

class GetBranchesUseCase @Inject constructor(
    private val branchRepository: IBranchRepository
) {
    suspend operator fun invoke(): Result<List<Branch>> {
        return branchRepository.getBranches()
    }
}