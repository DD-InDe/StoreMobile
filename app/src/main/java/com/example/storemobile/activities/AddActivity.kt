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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val categorySpinner: Spinner = findViewById(R.id.category)
        val manufacturerSpinner: Spinner = findViewById(R.id.manufacturer)
        val providerSpinner: Spinner = findViewById(R.id.provider)

        val name: EditText = findViewById(R.id.new_name)
        val cost: EditText = findViewById(R.id.new_cost)
        val context = this

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
            val product = Product(
                productName = name.text.toString(),
                cost = cost.text.toString().toFloat(),
                categoryId = selectedCategory,
                manufacturerId = selectedManufacturer,
                providerId = selectedProvider)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    postProductToServer(product)
                } catch (e: Exception) {
                    Log.e("Ошибка", "Произошла ошибка при сохранении: ${e.message}")
                }
            }
        }
    }

    private suspend fun postProductToServer(newProduct: Product) = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val json = Gson().toJson(newProduct)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)

        val request =
            Request.Builder().url("http://192.168.1.49:5171/api/Product")
                .post(body)
                .build()

        client.newCall(request).execute().use {}
    }

    private suspend fun loadCategoryFromServer(): List<Category> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://192.168.1.49:5171/api/Category")
            .build()

        val response = client.newCall(request).execute()
        val json = response.body?.string()

        val categoryList = Gson().fromJson(json, Array<Category>::class.java).toList()

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