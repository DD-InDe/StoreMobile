# Разбор кода

## Адаптер для кастомного списка RecyclerView

##### Разметка product_in_list.xml

Для начала создаем файл с разметкой _Layout Resource File_ для одного элемента списка.

```xml

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:layout_marginBottom="20dp" android:background="#D4E157" android:orientation="vertical">

    <TextView android:id="@+id/product_name" android:layout_width="wrap_content"
        android:layout_height="wrap_content" android:padding="5dp" android:textSize="30sp"
        android:textStyle="bold" />

    <TextView android:id="@+id/product_manufacturer" android:layout_width="wrap_content"
        android:layout_height="wrap_content" android:padding="5dp" android:textSize="20sp"
        android:textStyle="bold" />

    <TextView android:id="@+id/product_cost" android:layout_width="wrap_content"
        android:layout_height="wrap_content" android:layout_gravity="end" android:padding="5dp"
        android:textSize="24sp" android:textStyle="bold" />

    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:gravity="center_horizontal" android:orientation="horizontal">

        <Button android:id="@+id/edit_product_button" android:layout_width="180dp"
            android:layout_height="wrap_content" android:layout_gravity="center"
            android:layout_marginHorizontal="5dp" android:text="Редактировать" />

        <Button android:id="@+id/delete_product_button" android:layout_width="180dp"
            android:layout_height="wrap_content" android:layout_marginHorizontal="5dp"
            android:layout_gravity="center" android:text="Удалить" />
    </LinearLayout>
</LinearLayout>
```

Здесь:

+ 3 текстовых поля:
    + product_name
    + product_manufacturer
    + product_manufacturer
+ 2 кнопки:
    + edit_product_button
    + delete_product_button

##### Класс ProductAdapter

Создаем kotlin класс `ProductAdapter`, в качестве параметров передаем изменяемый список и контекст.

```kotlin
class ProductAdapter(
    var products: MutableList<Product>,
    context: Context
)
```

Внутри `ProductAdapter` создаем класс `MyViewHolder` и наследуем от RecyclerView.ViewHolder().
Создаем переменные с ссылками на элементы управления.

```kotlin
class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val productName: TextView = view.findViewById(R.id.product_name)
    val productManufacturer: TextView = view.findViewById(R.id.product_manufacturer)
    val productCost: TextView = view.findViewById(R.id.product_cost)
    val editButton: Button = view.findViewById(R.id.edit_product_button)
    val deleteButton: Button = view.findViewById(R.id.delete_product_button)
}
```

Наследуем у RecyclerView.Adapter<[вписываем свой класс].MyViewHolder()>

```kotlin
class ProductAdapter(
    var products: MutableList<Product>,
    context: Context
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>()
```

Вндреяем методы от производного класса.
В `onCreateViewHolder` мы устанавливаем отображение элементов и указываем наш файл разметки

```kotlin
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val view =
        LayoutInflater.from(parent.context).inflate(R.layout.product_in_list, parent, false)
    return MyViewHolder(view)
}
```

В getItemCount() просто возвращаем кол-во объектов внутри нашего листа _products_.

```kotlin
override fun getItemCount(): Int {
    return products.count()
}
```

В onBindViewHolder() объявляем переменные и передаем ссылки на элементы управления.

```kotlin
 override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    holder.productName.text = products[position].productName
    holder.productManufacturer.text = products[position].manufacturer!!.manufacturerName
    holder.productCost.text = products[position].cost.toString()
//      ...
}
```

## Вывод товаров

Создаем новое Activity **MainActivity**

#### Разметка activity_main.xml

```xml

<TextView android:id="@+id/textview" android:layout_width="match_parent"
    android:layout_height="wrap_content" android:gravity="center_horizontal" android:text="Продукты"
    android:textSize="30sp" android:textStyle="bold" app:layout_constraintTop_toTopOf="parent" />

<androidx.recyclerview.widget.RecyclerView android:id="@+id/products_rv"
android:layout_width="match_parent" android:layout_height="match_parent"
android:layout_marginTop="50dp" android:padding="10dp" />

<com.google.android.material.floatingactionbutton.FloatingActionButton android:id="@+id/add_button"
android:layout_width="wrap_content" android:layout_height="wrap_content"
android:layout_margin="20dp" app:layout_constraintRight_toRightOf="parent"
app:layout_constraintBottom_toBottomOf="parent" android:clickable="true"
app:srcCompat="@drawable/ic_add" android:contentDescription="add" android:focusable="true" />
```

Здесь:

+ TextView
+ RecyclerView
+ FloatingActionButton

#### Класс MainActivity

В методе **onCreate()** объявляем переменную и передаем ссылку на кнопку.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val add_button: FloatingActionButton = findViewById(R.id.add_button)

//          ...
}
```

Для загрузки данных с сервера объявим асинхронный метод `loadProductsFromServer()`, который будет
возвращать MutableList<\Product>. Эта функция у нас с ключевым словом **suspend**, которое означает,
что функция является корутином. В корутинах Kotlin есть так называемый **"implicit return"** (
неявное возвращение), что означает, что результат последнего выражения внутри корутины считается
результатом функции.

```kotlin
private suspend fun loadProductsFromServer(): MutableList<Product> =
    withContext(Dispatchers.IO) {
        // ...
        products
    }
```

Для работы с удаленным сервером (API) нужно использовать Gson для работы с
json и OkHttp для работы с запросами.

Сначала мы создаем объекты `OkHttpClient()` и `Request` с URL-адресом нашего метода в API. После
чего в try-catch создаем переменную с запросом на сервер и переменную с информацией о типе данных
при десериализации (что мы хотим получить из JSON). После чего в переменную **products** записываем
считанные из json данные, указав при этом в параметрах fromJson запрос и тип данных.

###### loadProductsFromServer()

```kotlin
//  ...
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
//  ...
```

Для отображения этих данных в RecyclerView создадим отдельный метод `loadProductsInRV()`. В
качествве параметра будем передавать контекст, это нужно будет для вызова адаптера. И сразу находим
RecylcerView.

```kotlin
private fun loadProductsInRV(context: Context) {
    val productList: RecyclerView = findViewById(R.id.products_rv)
//      ...
}
```

Для работы с асинхронными методами внутри других методов нам необходимо использовать *
*lifecycleScope.launch { }** а внутри  **withContext(Dispatchers.IO) { }**. Это позволит вызывать
функцию во внешнем потоке. Внутри этого потока в блоке try-catch загружаем лист с продуктами из
асинхронного метода `loadProductsFromServer()`. После чего, используя блок **runOnUiThread {}**
указываем RecyclerView **layoutManager** и **adapter**.

```kotlin
//...
lifecycleScope.launch {
    withContext(Dispatchers.IO) {

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
//...
```

## Добавление новых товаров

Создаем новое Activity, добавляем текстовые поля и кнопку для сохранения.

##### Разметка acitivity_add.xml

```xml

<TextView android:gravity="center_horizontal" android:layout_height="wrap_content"
    android:layout_marginVertical="20dp" android:layout_width="match_parent"
    android:text="Добавление товара" android:textSize="22sp" />

<EditText android:hint="Имя продукта" android:id="@+id/new_name"
android:layout_gravity="center_horizontal" android:layout_height="wrap_content"
android:layout_width="250dp" />

<EditText android:hint="Цена продукта" android:id="@+id/new_cost"
android:layout_gravity="center_horizontal" android:layout_height="wrap_content"
android:layout_width="250dp" />

<LinearLayout android:gravity="center_horizontal" android:layout_gravity="center_horizontal"
android:layout_height="wrap_content" android:layout_marginVertical="10dp"
android:layout_width="match_parent" android:orientation="horizontal">

<TextView android:layout_height="wrap_content" android:layout_width="wrap_content"
    android:text="Категория:" android:textSize="18sp" />

<Spinner android:id="@+id/category" android:layout_height="wrap_content"
    android:layout_width="wrap_content" />
</LinearLayout>

<LinearLayout android:gravity="center_horizontal" android:layout_gravity="center_horizontal"
android:layout_height="wrap_content" android:layout_marginVertical="10dp"
android:layout_width="match_parent" android:orientation="horizontal">

<TextView android:layout_height="wrap_content" android:layout_width="wrap_content"
    android:text="Производитель:" android:textSize="18sp" />

<Spinner android:id="@+id/manufacturer" android:layout_height="wrap_content"
    android:layout_width="wrap_content" />
</LinearLayout>


<LinearLayout android:gravity="center_horizontal" android:layout_gravity="center_horizontal"
android:layout_height="wrap_content" android:layout_marginVertical="10dp"
android:layout_width="match_parent" android:orientation="horizontal">

<TextView android:layout_height="wrap_content" android:layout_width="wrap_content"
    android:text="Поставщик:" android:textSize="18sp" />

<Spinner android:id="@+id/provider" android:layout_height="wrap_content"
    android:layout_width="wrap_content" />
</LinearLayout>

<Button android:id="@+id/update_product_button" android:layout_gravity="center_horizontal"
android:layout_height="wrap_content" android:layout_marginVertical="10dp"
android:layout_width="200dp" android:text="Сохранить" />
```

##### Класс AddActivity

В главной функции onCreate() объявляем переменные и связываем их с элементами управления.

```kotlin
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

//      ...
}
```

Создаем несколько функций для загрузки данных о Поставщиках, Категориях и Производителях из API для
дальнейшей загрузки их в Spinner`ы.

После этого в `onCreate()` вызываем эти функции,, используя **lifecycleScope.launch{ }** для
загрузки данных в Spinner'ы. Т.к. кастомного адаптера для Spinner'а нет, загружать туда будем не
просто листы объектов, а только названия? для этого будем использовать **map**.

###### onCreate()

```kotlin
//...
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
//  ...
```

Создадим асинхронный метод `postProductToServer()` для добавления нового продукта в БД (отправки
запроса API),
которому будем передавать наш новый продукт. Помимо **client**, мы объявляем переменную **json**
в которой будем хранить сериализованный объект Product. Также будет переменная **body** которая
содержит сериализованный json, устанавливается тип контента "application/json" с кодировкой UTF-8.

```kotlin
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
```

Теперь в методе `onCreate()` зададим нашей кнопке метод **setOnClickListener{ }**. В нем мы будем
создавать новый экземпляр класса и передавать параметры. После чего этот экземпляр передадим методу
`postProductToServer()`, используя **CoroutineScope()**.

###### onCreate()

```kotlin
//...
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
        providerId = selectedProvider
    )

    CoroutineScope(Dispatchers.Main).launch {
        try {
            postProductToServer(product)
        } catch (e: Exception) {
            Log.e("Ошибка", "Произошла ошибка при сохранении: ${e.message}")
        }
    }
}
//...
```

## Обновление данных о товаре

Создаем новое Activity с названием `UpdateActivity` остается такой же как и в `acvity_add.xml`.
Исключения будут в коде.

Создаем метод для кнопки "Редактировать" у `ProductAdapter`, в нем мы будем
передавать `UpdateActivity`редактируемый продукт. Для передачи объекта другому Activity мы
используем метод **putExtra** у объекта Intent и в качестве параметра будем передавать json.

###### onBindViewHolder()

```kotlin
//...
holder.editButton.setOnClickListener {
    val context = holder.itemView.context
    val json = Gson().toJson(products[position])
    val intent = Intent(context, UpdateActivity::class.java)
    intent.putExtra("product", json)
    context.startActivity(intent)
}
//...
```

Теперь в функции `onCreate()` у UpdateActivity получаем json и десериализируем в объект, также
задаем переменным элементы управления. Полям "Название" и "Цена" мы задаем полученные значения.

###### onCreate()

```kotlin
//...
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
//...
```

Создаем методы для загрузки данных о Поставщиках, Производителях и Категориях из API, эти методы уже
прописаны в `AddActivity`.

После чего в методе `onCreate()` внутри **lifecycleScope** загружаем данные в Spinner'ы. И указываем
им значения нашего объекта.

###### onCreate()

```kotlin
//...
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
//...
```

Создаем асинхронный метод для обновления данных в БД. Изменение только в запросе. Вместо **post**
используем **put** и в url указываем ссылку на метод PUT.

```kotlin
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

    client.newCall(request).execute().use {}
}
```

Теперь можно кнопке "Сохранить" добавить метод **setOnClickListener { }**, внутри которого мы
изменяем параметры текущего объекта и внутри **CoroutineScope()** вызываем
метод `putProductToServer()`.

###### onCreate()

```kotlin
//...
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
//...
```

## Удаление данных о товаре

Удаление мы делаем внутри `ProductAdapter` на кнопку "Удалить".

Для этого создаем асинхронный метод `deleteProductFromServer()`, которму передаем наш текущий
объект. Внутри него создаем переменные **client** и **request**, которому задаем метод **delete()**
в url указываем метод на удаление из API.

```kotlin
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
```

Кнопке задаем метод **setOnClickListener { }**, в котором внутри **CorountineScope** вызываем
метод `deleteProductFromServer()` и из текущего списка объектов, который мы получили
из `MainActivity` удаляем также наш объект, после чего вызываем метод notifyDataSetChanged(),
который обновит интерфейс пользователя.

Для использования **notifyDataSetChanged()** необходимо будет перед методом прописать 
**@SuppressLint("NotifyDataSetChanged")**

###### onBindViewHolder()
```kotlin
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
```