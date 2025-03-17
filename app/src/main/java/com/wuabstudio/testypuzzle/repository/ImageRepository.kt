package com.wuabstudio.testypuzzle.repository

import android.util.Log
import com.wuabstudio.testypuzzle.api.PixabayApiService
import com.wuabstudio.testypuzzle.api.PixabayImage
import com.wuabstudio.testypuzzle.api.RetrofitClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImageRepository {
    private val TAG = "ImageRepository"
    
    // Retrofit istemcisini kullan
    private val apiService = RetrofitClient.pixabayApiService
    
    suspend fun getRandomImages(query: String = "nature"): List<PixabayImage> {
        try {
            Log.d(TAG, "Fetching images for query: $query with API key: ${PixabayApiService.API_KEY}")
            
            val response = apiService.searchImages(
                query = query
            )
            
            if (response.isSuccessful) {
                val pixabayResponse = response.body()
                Log.d(TAG, "API call successful. Total images: ${pixabayResponse?.total ?: 0}")
                return pixabayResponse?.images ?: emptyList()
            } else {
                Log.e(TAG, "API call failed with code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return emptyList()
        }
    }
} 