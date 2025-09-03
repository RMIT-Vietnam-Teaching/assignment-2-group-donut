package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.Branch

interface IBranchRepository {
    suspend fun getBranches(): Result<List<Branch>>
}