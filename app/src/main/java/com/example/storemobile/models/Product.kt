package com.example.storemobile.models

data class Product(
    var productId:Int = 0,
    var productName:String,
    var cost:Float,
    var categoryId:Int,
    var manufacturerId:Int,
    val providerId:Int,
    var category: Category? = null,
    var manufacturer: Manufacturer? = null,
    var provider: Provider? = null
)
