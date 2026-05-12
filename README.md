# AI Chat - Android AI-Powered Chat Application

## 📱 Overview

**AI Chat** is a modern Android application that enables users to have intelligent conversations with OpenAI's GPT models. Built with Jetpack Compose and following Clean Architecture principles, the app provides a seamless chat experience with conversation management, persistent storage, and an intuitive user interface.

### ✨ Key Features

- 💬 **Real-time AI Conversations**: Chat with OpenAI's powerful language models (GPT-4o-mini by default)
- 📚 **Multiple Conversations**: Manage and organize multiple chat sessions simultaneously
- 🎯 **Smart Conversation Titles**: Automatically generated conversation titles using AI
- 💾 **Local Persistence**: All conversations are saved locally using Room database
- 🗑️ **Conversation Management**: Create, select, and delete conversations with ease
- 🎨 **Modern UI**: Beautiful Material 3 design with dark/light theme support
- 🌐 **Multi-language Ready**: Fully internationalized with support for multiple languages

### 🛠️ Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room (with KSP for code generation)
- **AI Integration**: OpenAI Kotlin SDK
- **Async**: Kotlin Coroutines & Flows
- **Language**: Kotlin

---

## 🎬 Demo

[App demo (YouTube Short)](https://www.youtube.com/shorts/n7H_vJSfGQI)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 24 (minimum) / SDK 34 (target)
- OpenAI API key

### OpenAI API Key Setup

To use the application, you need to configure your OpenAI API key.

#### Steps to configure your API key:

1. **Get your OpenAI API key:**
   - Go to [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)
   - Sign in or create an account
   - Create a new API key

2. **Configure the API key in the project:**
   - Open the `local.properties` file in the project root
   - Add the following line at the end of the file:
   ```properties
   OPENAI_API_KEY=your-api-key-here
   ```
   - Replace `your-api-key-here` with your actual OpenAI API key

3. **Sync the project:**
   - In Android Studio, click "Sync Now" or manually sync
   - Build the project

#### Example of local.properties:

```properties
sdk.dir=/Users/your-username/Library/Android/sdk
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### Important:

- **DO NOT** share your API key publicly
- **DO NOT** commit the `local.properties` file to the repository
- The `local.properties` file is in `.gitignore` by default

---

## 📦 Project Structure

```
app/
├── data/
│   ├── datasource/      # OpenAI API integration
│   ├── local/           # Room database (DAOs, entities, converters)
│   ├── mapper/          # Data mappers between layers
│   ├── model/           # Domain models
│   └── repository/      # Repository pattern implementation
├── di/                  # Hilt dependency injection modules
├── ui/
│   ├── screens/chat/    # Chat screen composables
│   └── theme/           # Material 3 theming
└── util/                # Utility classes (StringProvider, etc.)
```

---

## 🤖 Available Models

By default, the app uses the `gpt-4o-mini` model, but you can change it in `OpenAIDataSource.kt` to other models such as:

- `gpt-3.5-turbo`
- `gpt-4`
- `gpt-4-turbo-preview`
- Other models available in the OpenAI documentation

To change the model, locate the following line in `OpenAIDataSource.kt`:

```kotlin
model = ModelId("gpt-4o-mini")
```

---

## 💰 Costs

Please note that using the OpenAI API has associated costs. Check the pricing at:
[https://openai.com/pricing](https://openai.com/pricing)

**Recommendations:**
- Monitor your API usage in the OpenAI dashboard
- Set usage limits to avoid unexpected charges
- Consider using cheaper models like `gpt-3.5-turbo` for testing

---

## 🎯 Features in Detail

### Conversation Management

- **Create New Conversations**: Start a fresh conversation at any time
- **Auto-Generated Titles**: Conversations are automatically titled based on the first user message using AI
- **Switch Between Conversations**: Access your conversation history from the sidebar menu
- **Delete Conversations**: Remove conversations you no longer need
- **Persistent Storage**: All conversations are saved locally and survive app restarts

### Chat Interface

- **Message History**: View your complete conversation history
- **Real-time Responses**: Get AI responses as you send messages
- **Loading Indicators**: Visual feedback while waiting for AI responses
- **Error Handling**: User-friendly error messages for network or API issues
- **Empty State**: Helpful placeholder when starting a new conversation

### UI/UX

- **Material 3 Design**: Modern, consistent design language
- **Responsive Layout**: Adapts to different screen sizes
- **Navigation Drawer**: Easy access to all your conversations
- **Visual Feedback**: Selected conversation highlighting
- **Smooth Animations**: Polished user experience

---

## 📝 License

This project is a sample/learning application. Feel free to use it as a reference for your own projects.

---

## 🤝 Contributing

This is a personal project, but feel free to fork it and adapt it to your needs!

---

## 📧 Contact

For questions or suggestions, feel free to open an issue on GitHub.

---

**Note**: This app requires an active internet connection to communicate with the OpenAI API.
