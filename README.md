<h1 align="center">📖 Diary App</h1>
<h2>📸 Screenshots 📸 </h2>

| Home Screen                                           | WriteScreen                                   |
| ----------------------------------------------------- | --------------------------------------------- |
| ![homescreen](https://github.com/HarshWavikar/Diary-App/assets/122421974/e3e793ae-323d-454c-99dd-7143fde6b726) | ![writescreen](https://github.com/HarshWavikar/Diary-App/assets/122421974/04ee63bb-0a29-488b-92a5-79dd2dca14f9) |


<h2 align="start">🛠️ Architecture 🛠️</h2>

Modules
DiaryApp is initially built using Android Clean Architecture that follows the more familiar Model-View-ViewModel (MVVM) pattern.

On the second branch the project is restructured into a Multi-Module Architecture using layered features.

Here's an overview of the app's architectural modular components:

App Module: This is the main module of the DiaryApp, whhich acts as the orchestrator of the different features and modules. It handles the navigation flow between the Authentication, Home, and the Write features, ensuring a cohesive and seamless user experience. The App Module integrates the dependencies from the feature modules and manages the overall lifecycle of the app.

buildSrc: The buildSrc module serves as a central location for managing project configuration and dependencies. This module allows for a streamlined and standardized setup of project configurations, build scripts, and dependencies, simplifying the build process and ensuring consistency across the app.

Data Module:The Data module in the Android Diary App is responsible for managing data storage and retrieval using both MongoDB and Room. It handles the setup and integration of Mongo Realm, allowing seamless connectivity to the MongoDB backend. The Data module provides functionalities for inserting, fetching, updating, and deleting diary entries in the MongoDB database. Additionally, DiaryApp leverages Room Librayto provide offline access and local caching of diary entries, enhancing the app's responsiveness and offline capabilities.

Common/Core Modules: The app includes two core modules: UI and Utils. The UI module contains common Compose functions, components, and UI-related code that are shared across different features. This module promotes code reuse and consistency in the app's user interface. The Utils module provides essential utilities such as model classes, connectivity observers, constants, strings and drawable resources. It ensures a centralized and efficient management of commonly used resources and functionalities.

Feature Modules: This module includes three destinations: Auth, Home and Write. 'Auth' handles the authentication with the users. 'Home' displays all the data/diaries in our application. And 'Write' module allows you to create a new diary note in your app.
