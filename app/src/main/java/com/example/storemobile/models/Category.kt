package com.example.storemobile.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String
)
