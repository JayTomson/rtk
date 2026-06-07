# ReadTracker

Удобный трекер чтения книг и легких новелл для Android, разработанный на Kotlin и Jetpack Compose. Приложение спроектировано с акцентом на точный учет прочитанного объема текста (в словах и томах), визуализацию прогресса, аналитику и гибкую кастомизацию.

## Ключевые возможности

* **Учет и каталогизация**: ведение списка тайтлов с распределением по статусам («Читаю», «В планах», «Завершено», «На паузе», «Брошено»). Детальный трекинг слов и глав по каждому тому.
* **Полноценная аналитика**: наглядные графики и карточки с общей статистикой — общее количество прочитанных слов за всё время, число прочитанных томов, завершенных франшиз и веб-новелл, а также интерактивные прогресс-бары.
* **Кастомизация UI**: поддержка трех тем оформления (AMOLED, Тёмная и Светлая темы) с использованием динамического оранжевого акцента и аккуратным Material 3 дизайном.
* **Поделиться прогрессом**: генерация стильных карточек со статистикой или списком книг для удобного экспорта и демонстрации в соцсетях.
* **Резервное копирование**: импорт и экспорт всей библиотеки через JSON-файлы.

## Стек технологий

* **Язык**: Kotlin
* **Интерфейс**: Jetpack Compose (Material Design 3, Plus Jakarta Sans)
* **Архитектура**: MVVM (Model-View-ViewModel) + StateFlow
* **Локальная БД**: Room database для быстрого и надежного хранения данных на устройстве

## Сборка в Google Colab

Для быстрой сборки релизной версии APK без необходимости локальной установки настроенного окружения или Android Studio, вы можете запустить процесс компиляции в [Google Colab](https://colab.research.google.com/).

### Инструкция по сборке:

1. Создайте новый блокнот в Google Colab.
2. Клонируйте или загрузите файлы проекта в директорию `/content/EpubEdit`.
3. Создайте новую ячейку кода, вставьте следующий скрипт и запустите его:

```python
# ==============================================================================
# 1. УСТАНОВКА ПЕРЕМЕННЫХ ОКРУЖЕНИЯ ДЛЯ JVM И ПУТЕЙ ПОДПИСИ
# ==============================================================================
%env _JAVA_OPTIONS=-XX:-UseContainerSupport
%env JDK_JAVA_OPTIONS=-XX:-UseContainerSupport
%env KEYSTORE_PATH=/content/my-upload-key.jks
%env STORE_PASSWORD=my_secure_password_123
%env KEY_PASSWORD=my_secure_password_123

import os
import shutil
import glob

# ==============================================================================
# 2. ПРОВЕРКА И ГЕНЕРАЦИЯ КЛЮЧА ПОДПИСИ (KEYSTORE)
# ==============================================================================
keystore_path = "/content/my-upload-key.jks"

if not os.path.exists(keystore_path):
    print("🔑 Файл ключа не найден. Генерируем новый Keystore подписи...")
    !keytool -genkey -v \
      -keystore {keystore_path} \
      -alias upload \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -storepass my_secure_password_123 \
      -keypass my_secure_password_123 \
      -dname "CN=ReadTracker, O=AIStudio, C=US"
    print("✅ Ключ успешно создан по абсолютному пути:", keystore_path)
else:
    print("✅ Ключ подписи уже существует. Скачивание/генерация пропущены.")

# ==============================================================================
# 2.5. АВТОПРИВЯЗКА ИЛИ СКАЧИВАНИЕ ANDROID SDK (РЕШЕНИЕ ВАШЕЙ ОШИБКИ)
# ==============================================================================
colab_sdk_path = "/content/android-sdk"

if not os.path.exists(f"{colab_sdk_path}/cmdline-tools/latest"):
    print("📦 Android SDK полностью отсутствует в системе. Скачиваем официальные инструменты...")
    # Создаем структуру папок
    os.makedirs(f"{colab_sdk_path}/cmdline-tools", exist_ok=True)

    # Скачиваем официальный легковесный пакет cmdline-tools от Google
    !wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /content/sdk.zip
    !unzip -q /content/sdk.zip -d {colab_sdk_path}/cmdline-tools

    # Приводим структуру папок к стандарту Google (переименовываем в 'latest')
    if os.path.exists(f"{colab_sdk_path}/cmdline-tools/cmdline-tools"):
        os.rename(f"{colab_sdk_path}/cmdline-tools/cmdline-tools", f"{colab_sdk_path}/cmdline-tools/latest")

    # Чистим архив за собой
    if os.path.exists("/content/sdk.zip"): os.remove("/content/sdk.zip")

    print("📜 Автоматически принимаем лицензионные соглашения Android SDK...")
    # Принимаем все лицензии, чтобы Gradle мог сам докачивать нужные платформы в фоне
    !yes | {colab_sdk_path}/cmdline-tools/latest/bin/sdkmanager --sdk_root={colab_sdk_path} --licenses > /dev/null
    print("✅ Базовый Android SDK успешно развернут!")
else:
    print("✅ Локальный Android SDK уже подготовлен. Пропускаем скачивание.")

# Регистрируем SDK в системе
os.environ["ANDROID_HOME"] = colab_sdk_path
os.environ["ANDROID_SDK_ROOT"] = colab_sdk_path

# ==============================================================================
# 3. ПЕРЕХОД В ПРОЕКТ И НАСТРОЙКА GRADLE
# ==============================================================================
%cd /content/rtk

if not os.path.exists("build.gradle") and not os.path.exists("build.gradle.kts"):
    print("\n⚠️ Внимание: В текущей папке /content/rtk не найден файл build.gradle.")
    print("Если у вас проект Flutter/React Native, перейдите в подпапку android: %cd /content/rtk/android")
else:
    # Прописываем путь к SDK в local.properties нашего проекта
    with open("local.properties", "w") as f:
        f.write(f"sdk.dir={colab_sdk_path}\n")

    REQUIRED_GRADLE = "9.3.1"
    properties_path = "gradle/wrapper/gradle-wrapper.properties"

    need_gradle_install = True
    if os.path.exists("gradlew") and os.path.exists(properties_path):
        with open(properties_path, "r") as f:
            if REQUIRED_GRADLE in f.read():
                need_gradle_install = False

    if need_gradle_install:
        print(f"📦 Локальный gradlew нужной версии ({REQUIRED_GRADLE}) не найден. Настраиваем...")
        if os.path.exists("gradlew"): os.remove("gradlew")

        !wget -q https://services.gradle.org/distributions/gradle-{REQUIRED_GRADLE}-bin.zip
        !unzip -q gradle-{REQUIRED_GRADLE}-bin.zip

        print(f"🔄 Генерируем локальный Gradle Wrapper {REQUIRED_GRADLE}...")
        !./gradle-{REQUIRED_GRADLE}/bin/gradle wrapper --gradle-version {REQUIRED_GRADLE} --distribution-type bin

        if os.path.exists(f"gradle-{REQUIRED_GRADLE}-bin.zip"): os.remove(f"gradle-{REQUIRED_GRADLE}-bin.zip")
        if os.path.exists(f"gradle-{REQUIRED_GRADLE}"): shutil.rmtree(f"gradle-{REQUIRED_GRADLE}")
        print(f"✅ Gradle Wrapper {REQUIRED_GRADLE} успешно настроен!")
    else:
        print(f"✅ Локальный gradlew версии {REQUIRED_GRADLE} обнаружен. Скачивание пропущено.")

    !chmod +x gradlew

    # ==============================================================================
    # 4. ЗАПУСК СБОРКИ РЕЛИЗНОЙ (RELEASE) ВЕРСИИ APK
    # ==============================================================================
    print("🚀 Запуск сборки Release APK... (В процессе Gradle сам докачает нужные Build-Tools)")
    !./gradlew assembleRelease --no-daemon

    # ==============================================================================
    # 5. ПОИСК И КОПИРОВАНИЕ ГОТОВОГО ФАЙЛА APK ДЛЯ СКАЧИВАНИЯ
    # ==============================================================================
    print("\n=== 🎉 Сборка завершена! Поиск релизных файлов APK ===")

    release_apks = glob.glob("**/outputs/apk/release/*.apk", recursive=True)
    if release_apks:
        for apk in release_apks:
             dest_path = "/content/app-release.apk"
             shutil.copy(apk, dest_path)
             print(f"🌟 Успех! Скомпилированный APK скопирован в: {dest_path}")
             print("👉 Теперь вы можете скачать файл 'app-release.apk' прямо из левой панели файлов в Google Colab!")
    else:
        print("❌ Релизный APK файл не найден. Пожалуйста, проверьте логи Gradle выше на наличие ошибок компиляции.")
```

---

*Изначально я "писал" это приложение через нейронки на Flutter, но так как появилась возможность в AI Studio делать андроид приложение, решил переписать его под Котлин. Вроде как норм вышло, мне даже больше нравится, чем на Flutter, но если хотите попробовать приложение на Flutter, держите ссылку: [Flutter-версия в Telegram](https://t.me/nvlSSP/2)*

:>
