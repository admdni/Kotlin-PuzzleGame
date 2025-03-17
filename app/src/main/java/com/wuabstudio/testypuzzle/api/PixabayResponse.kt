package com.wuabstudio.testypuzzle.api

import com.google.gson.annotations.SerializedName

data class PixabayResponse(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("totalHits")
    val totalHits: Int = 0,
    @SerializedName("hits")
    val images: List<PixabayImage> = emptyList()
)

data class PixabayImage(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("webformatURL")
    val webformatURL: String = "",
    @SerializedName("largeImageURL")
    val largeImageURL: String = "",
    @SerializedName("previewURL")
    val previewURL: String = "",
    @SerializedName("tags")
    val tags: String = ""
) 