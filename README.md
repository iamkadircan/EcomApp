# EcomApp 🚀

**EcomApp** is a multi-module e-commerce application developed for practice purposes, showcasing various modern Android development practices and technologies. Product information (images, price, ratings, etc.) is sourced from [DummyJSON](https://dummyjson.com/).

## Features ✨

- **Coil Async Image Loading**: Efficient and fast image loading using [Coil](https://coil-kt.github.io/coil/).
- **StateFlow for UI State Management**: Manages UI state effectively with `StateFlow`.
- **Kotlin Coroutines**: Handles asynchronous operations and task management seamlessly.
- **Preferences DataStore**: Stores necessary data using preferencesdatastore .
- **Firebase Authentication & Firestore**: Integrates Firebase Auth for user authentication and Firestore for cloud-based data storage.
- **Room Database**: Provides local data caching with `Room Database`.
- **Custom SQL Queries in DAOs**: Offers more control over data with custom SQL queries in DAO interfaces.
- **Custom Pagination for UI**: Implements a custom pagination system for smooth data handling in the UI.
- **Data Sync** : Required data is synced between firebase and room.
- **Multi-Module Architecture**: Designed as a multi-module project for better manageability and scalability.
- **Hilt for Dependency Injection**: Manages dependencies efficiently using [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
- **Retrofit for Network Operations**: Handles network requests and data fetching using `Retrofit`.
- **MVVM and Clean Architecture**: Ensures a well-structured, testable codebase by following MVVM and Clean Architecture principles.
- **Jetpack Compose for UI**: Builds the UI with [Jetpack Compose](https://developer.android.com/jetpack/compose).

## Technologies Used 🛠️

- **Kotlin**
- **Jetpack Compose**
- **Hilt**
- **Retrofit**
- **Room Database**
- **Firebase Authentication & Firestore**
- **Coil**
- **Kotlin Coroutines**
- **StateFlow**
- **Preferences DataStore**

**Module Tree Structure**

<p>📦 EcomApp</p>
<ul>
  <li>📂 app</li>
  <li>📦 core
    <ul>
      <li>📂 common</li>
      <li>📂 data</li>
      <li>📂 database</li>
      <li>📂 datastore</li>
      <li>📂 domain</li>
      <li>📂 network</li>
    </ul>
  </li>
  <li>📦 feature
    <ul>
      <li>📂 cart</li>
      <li>📂 component</li>
      <li>📂 detail</li>
      <li>📂 favorites</li>
      <li>📂 login</li>
      <li>📂 products</li>
      <li>📂 profile</li>
      <li>📂 register</li>
    </ul>
  </li>
</ul>

    

## TODO 📝

- **Refactor `EcomRepositoryImpl` Class**: The `EcomRepositoryImpl` class is currently bulky. Plan to add two  helper classes: `LocalDataHelper` and `FirebaseHelper` for better modularity and readability.
- **Enhance Adaptive Layout**: Improve the adaptive layout to support various screen sizes and device types, ensuring a consistent user experience across all platforms.
- **Add usecases** : For sticking mvvm principles.


## Architecture Overview 🏗️

EcomApp follows the **MVVM (Model-View-ViewModel)** and **Clean Architecture** principles. This architectural approach enhances the app's testability, maintainability, and scalability. The project is divided into multiple modules, each serving a specific functionality to ensure a clear separation of concerns.

## Demo 📽️

Check out the app in action:

##Demo Video



https://github.com/user-attachments/assets/814b9d43-f628-452b-ac05-25e1fc8a532d




## Screenshots 📸

![Screenshot 1](https://github.com/user-attachments/assets/86a67c5c-8f63-4627-b98e-35cda34168e5)
![Screenshot 2](https://github.com/user-attachments/assets/6ff0b17c-b038-4336-a6bf-6a374a91fb85)
![Screenshot 3](https://github.com/user-attachments/assets/19662506-3969-4fae-9406-9958b4522a79)
![Screenshot 4](https://github.com/user-attachments/assets/ef0a70ad-06cf-4a12-b8dd-696659a47d1d)
![Screenshot 5](https://github.com/user-attachments/assets/cdb6a73a-004d-4bf3-bc62-e19a9a596b5c)
![Screenshot 6](https://github.com/user-attachments/assets/2d5d1ba1-51f6-4aa5-ba33-3a8eee475b7b)
![Screenshot 7](https://github.com/user-attachments/assets/bccc1abd-f3c8-467f-a809-a4def557767d)
![Screenshot 8](https://github.com/user-attachments/assets/453117e0-18dd-4ad4-8c63-c16008b56ebc)
