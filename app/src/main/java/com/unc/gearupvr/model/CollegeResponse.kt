package com.unc.gearupvr.model

data class CollegeResponse(
    val count: Int,
    val next: String,
    val previous: String,
    val offset: Int,
    val results: List<College>
) 