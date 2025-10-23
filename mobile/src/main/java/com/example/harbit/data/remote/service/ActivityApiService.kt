package com.example.harbit.data.remote.service

import com.example.harbit.data.remote.dto.ActivityDistributionResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ActivityApiService {
    
    /**
     * Get activity distribution for a date range.
     * 
     * For a single day, pass the same date for both parameters.
     * 
     * @param dateStart Start date (YYYY-MM-DD format). Required.
     * @param dateEnd End date (YYYY-MM-DD format). Required.
     * @param timezoneOffset Timezone offset in minutes from UTC (e.g., -300 for UTC-5).
     */
    @GET("/activities/distribution")
    suspend fun getActivityDistribution(
        @Query("date_start") dateStart: String,
        @Query("date_end") dateEnd: String,
        @Query("timezone_offset") timezoneOffset: Int
    ): Response<ActivityDistributionResponseDto>
}
