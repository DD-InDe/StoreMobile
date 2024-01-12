package com.example.storemobile.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import com.example.storemobile.R
import com.example.storemobile.models.Category
import com.example.storemobile.models.Manufacturer
import com.example.storemobile.models.Product
import com.example.storemobile.models.Provider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.reflect.Type

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        val json = intent.getStringExtra("product")
        val type: Type = object : TypeToken<Product>() {}.type
        var product = Gson().fromJson<Product>(json, type)

        val categorySpinner: Spinner = findViewById(R.id.category)
        val manufacturerSpinner: Spinner = findViewById(R.id.manufacturer)
        val providerSpinner: Spinner = findViewById(R.id.provider)

        val name: EditText = findViewById(R.id.update_name)
        val cost: EditText = findViewById(R.id.update_cost)
        val context = this

        name.setText(product.productName)
        cost.setText(product.cost.toString())

        lateinit var categoryList: List<Category>
        lateinit var manufacturerList: List<Manufacturer>
        lateinit var providerList: List<Provider>

        val button: Button = findViewById(R.id.update_product_button)

        lifecycleScope.launch {
            try {
                categoryList = loadCategoryFromServer()
                manufacturerList = loadManufacturerFromServer()
                providerList = loadProviderFromServer()

                val categoryNames = categoryList.map { it.categoryName }
                val categoryAdapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryNames)
                val manufacturerNames = manufacturerList.map { it.manufacturerName }
                val manufacturerAdapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    manufacturerNames
                )
                val providerNames = providerList.map { it.providerName }
                val providerAdapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_item, providerNames)

                categorySpinner.adapter = categoryAdapter
                manufacturerSpinner.adapter = manufacturerAdapter
                providerSpinner.adapter = providerAdapter

                categorySpinner.setSelection(categoryList.indexOf(product.category))
                manufacturerSpinner.setSelection(manufacturerList.indexOf(product.manufacturer))
                providerSpinner.setSelection(providerList.indexOf(product.provider))
            } catch (e: Exception) {
                Log.e("Ошибка", "Произошла ошибка при загрузке данных. ${e.message}")
            }
        }
        button.setOnClickListener {
            val selectedCategory =
                categoryList[categorySpinner.selectedItemId.toInt()].categoryId
            val selectedManufacturer =
                manufacturerList[manufacturerSpinner.selectedItemId.toInt()].manufacturerId
            val selectedProvider =
                providerList[providerSpinner.selectedItemId.toInt()].providerId
            product.productName = name.text.toString()
            product.cost = cost.text.toString().toFloat()
            product.categoryId = selectedCategory
            product.manufacturerId = selectedManufacturer
            product.productId = selectedProvider

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    putProductToServer(product)
                } catch (e: Exception) {
                    Log.e("Ошибка", "Произошла ошибка при сохранении: ${e.message}")
                }
            }
        }
    }

    private suspend fun putProductToServer(updateProduct: Product) = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        updateProduct.category = null
        updateProduct.manufacturer = null
        updateProduct.provider = null

        val json = Gson().toJson(updateProduct)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)

        val request =
            Request.Builder().url("http://192.168.1.49:5171/api/Product/${updateProduct.productId}")
                .put(body)
                .build()

        client.newCall(request).execute().use { responce ->
            val responseBody = responce.body?.string()
            println(responseBody)
        }
    }

    private suspend fun loadCategoryFromServer(): List<Category> = withContext(Dispatchers.IO) {
        // создание экземпляра OkHttpClient
        val client = OkHttpClient()

        // создание запроса к серверу
        val request = Request.Builder()
            .url("http://192.168.1.49:5171/api/Category")
            .build()

        // выполнение запроса и получение ответа
        val response = client.newCall(request).execute()
        val json = response.body?.string()

        // парсинг ответа в список объектов Product с использованием Gson
        val categoryList = Gson().fromJson(json, Array<Category>::class.java).toList()

        // закрытие ресурсов и возврат списка объектов Product
        response.close()
        categoryList
    }

    private suspend fun loadManufacturerFromServer(): List<Manufacturer> =
        withContext(Dispatchers.IO) {
            // создание экземпляра OkHttpClient
            val client = OkHttpClient()

            // создание запроса к серверу
            val request = Request.Builder()
                .url("http://192.168.1.49:5171/api/Manufacturer")
                .build()

            // выполнение запроса и получение ответа
            val response = client.newCall(request).execute()
            val json = response.body?.string()

            // парсинг ответа в список объектов Product с использованием Gson
            val manufacturerList = Gson().fromJson(json, Array<Manufacturer>::class.java).toList()

            // закрытие ресурсов и возврат списка объектов Product
            response.close()
            manufacturerList
        }

    private suspend fun loadProviderFromServer(): List<Provider> = withContext(Dispatchers.IO) {
        // создание экземпляра OkHttpClient
        val client = OkHttpClient()

        // создание запроса к серверу
        val request = Request.Builder()
            .url("http://192.168.1.49:5171/api/Provider")
            .build()

        // выполнение запроса и получение ответа
        val response = client.newCall(request).execute()
        val json = response.body?.string()

        // парсинг ответа в список объектов Product с использованием Gson
        val providerList = Gson().fromJson(json, Array<Provider>::class.java).toList()

        // закрытие ресурсов и возврат списка объектов Product
        response.close()
        providerList
    }
}