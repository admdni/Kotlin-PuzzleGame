package com.wuabstudio.testypuzzle.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApiService {
    companion object {
        const val BASE_URL = "https://pixabay.com/api/"
        // Kullanıcı tarafından sağlanan doğru API anahtarı
        const val API_KEY = "49382694-4aedd19f1c06290812299b7ae"
    }
    
    @GET(".")
    suspend fun searchImages(
        @Query("key") apiKey: String = API_KEY,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
        @Query("per_page") perPage: Int = 20,
        @Query("safesearch") safeSearch: Boolean = true,
        @Query("orientation") orientation: String = "horizontal",
        @Query("min_width") minWidth: Int = 800,
        @Query("min_height") minHeight: Int = 600
    ): Response<PixabayResponse>
} 