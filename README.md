# AIMealTracker

### Application Overview  
AIMealTracker is a desktop application for tracking consumed meals and their nutritional values. It allows users to add and delete meals, as well as display a daily caloric summary. **Meal data is approximated by AI and should be considered as an estimate – it may not always be 100% accurate.**  

---

### 2. Prerequisites  
To use the application, an API key from [OpenRouter.ai](https://openrouter.ai/) is required.  

#### **First Launch**  
When the application is launched for the first time, it will prompt the user to enter an API key. The provided key will be stored in the configuration file.  

#### **Changing the API Key**  
If you want to change the API key, you have two options:
1. **Delete the `.AIMealTracker_config` file** – the application will request a new key upon the next launch.
2. **Open the `.AIMealTracker_config` file** in a text editor and manually replace the stored key.  

---

### 3. Installation and Launch  
The application is distributed as a **JAR** file, which can be run on any operating system with **Java 8+** installed.  

#### **Running the Application**  
In the terminal/cmd, enter:  
```sh
java -jar AIMealTracker.jar
```
or simply double-click the `.jar` file.  

---

### 4. File Structure  
The application saves user data in the **AIMealTracker** directory within the home folder. Two main files are stored there:

- **`database.db`** – A local SQLite database storing saved meals.
- **`.AIMealTracker_config`** – A configuration file containing the API key.  

