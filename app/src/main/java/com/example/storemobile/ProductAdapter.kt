package com.example.storemobile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storemobile.activities.UpdateActivity
import com.example.storemobile.models.Product
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class ProductAdapter(
    var products: MutableList<Product>,
    context: Context
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.product_name)
        val productManufacturer: TextView = view.findViewById(R.id.product_manufacturer)
        val productCost: TextView = view.findViewById(R.id.product_cost)
        val editButton: Button = view.findViewById(R.id.edit_product_button)
        val deleteButton: Button = view.findViewById(R.id.delete_product_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.product_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.productName.text = products[position].productName
        holder.productManufacturer.text = products[position].manufacturer!!.manufacturerName
        holder.productCost.text = products[position].cost.toString()

        holder.editButton.setOnClickListener {
            val context = holder.itemView.context
            val json = Gson().toJson(products[position])
            val intent = Intent(context, UpdateActivity::class.java)
            intent.putExtra("product", json)
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    deleteProductFromServer(products[position])
                    products.remove(products[position])
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("Ошибка", "Произошла ошибка при удалении: ${e.message}")
                }
            }
        }
    }

    private suspend fun deleteProductFromServer(currentProduct: Product) =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request =
                Request.Builder()
                    .url("http://192.168.1.49:5171/api/Product/${currentProduct.productId}")
                    .delete()
                    .build()

            client.newCall(request).execute().use { responce ->
                val responseBody = responce.body?.string()
                println(responseBody)
            }
        }

}