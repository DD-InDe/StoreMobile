## Настройка проектов

### API

В `Program.cs` прописываем **Cors** (Cross-Origin Resource Sharing) для обмена ресурсами с разными источникми.
```csharp
// ...
builder.Services.AddCors(c => { c.AddDefaultPolicy(opt => { opt.AllowAnyHeader().AllowAnyOrigin().AllowAnyMethod(); }); });
// ...
app.UseCors()
```
В `launchSetting.json` в параметре http вместо localhost прописываем IP-адрес и порт.
```json
{
"applicationUrl":"http://[ip-address]:[port]"
}
```

### Android
Необходимые зависимости:
+ **implementation("com.google.code.gson:gson:2.8.9")** - _для работы с json_
+ **implementation("com.google.android.exoplayer:extension-okhttp:2.19.1")** - _для работы с веб-запросами_

В `AndroidManifest.xml` добавляем разрешение для выполнения сетевых операций.
```xaml
<uses-permission android:name="android.permission.INTERNET" />
```
Атрибуты **application** (обязательно, т.к. без них не будет работать подключение):
```xaml
android:allowBackup="true"
android:allowClearUserData="true"
```
