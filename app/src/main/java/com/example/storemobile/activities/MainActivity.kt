package com.example.storemobile.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storemobile.ProductAdapter
import com.example.storemobile.R
import com.example.storemobile.models.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.TypeAdapters
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val add_button:FloatingActionButton = findViewById(R.id.add_button)

        add_button.setOnClickListener {
            val intent = Intent(this,AddActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun loadProductsFromServer(): MutableList<Product> =
        withContext(Dispatchers.IO) {
            val OkHttp = OkHttpClient()

            val request: Request =
                Request.Builder().url("http://192.168.1.49:5171/api/Product").build()
            lateinit var products: MutableList<Product>

            try {
                val res = OkHttp.newCall(request).execute().body?.string().toString()

                val type: Type = object : TypeToken<MutableList<Product>>() {}.type
                products = Gson().fromJson<MutableList<Product>>(res, type)
            } catch (e: Exception) {
                println(e.message)
            }
            products
        }

    override fun onResume() {
        loadProductsInRV(this)
        super.onResume()
    }

    private fun loadProductsInRV(context: Context){
        val productList: RecyclerView = findViewById(R.id.products_rv)
        lifecycleScope.launch {withContext(Dispatchers.IO) {

                try {
                    val products = loadProductsFromServer()
                    runOnUiThread {
                        productList.layoutManager = LinearLayoutManager(context)
                        productList.adapter = ProductAdapter(products, context)
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }
    }
}