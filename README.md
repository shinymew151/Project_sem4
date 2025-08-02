# 🎬 Faceless Video Generator

An automated system that crawls Vietnamese news headlines from VNExpress, generates articles using ChatGPT, converts them to speech using Google TTS (male voice), and creates lip-sync videos using Gooey.ai API.

## 🆕 Latest Updates

- **5-Part Video Generation**: Increased from 3 to 5 parts for better article coverage
- **Male Voice**: Changed from female to male Vietnamese TTS voice
- **30-Second Coverage**: Optimized for full 30-second videos with comprehensive content
- **Improved Article Generation**: Longer articles (150-200 words) for better content coverage

## 🎯 Features

- **Automated News Crawling**: Fetches latest headlines from VNExpress
- **AI Article Generation**: Creates detailed Vietnamese articles using OpenAI GPT
- **Male Vietnamese TTS**: Converts text to speech using Google Cloud TTS (male voice)
- **Lip-Sync Video Creation**: Generates realistic talking avatar videos using Gooey.ai
- **Video Merging**: Combines 5 video parts into a single 30-second video
- **Web Dashboard**: Step-by-step workflow interface
- **Video Management**: View and manage all generated videos

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   VNExpress     │───▶│   ChatGPT API   │───▶│  Google TTS API │
│   (Headlines)   │    │   (Articles)    │    │  (Male Voice)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Final Video    │◀───│   FFmpeg        │◀───│   Gooey.ai API │
│  (30 seconds)   │    │   (Merge)       │    │   (Lip-sync)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔄 Workflow Steps

### 1. **Headline Crawling** 📰
- Connects to VNExpress homepage using Jsoup
- Extracts the latest news headline
- Stores headline for article generation

### 2. **Article Generation** 📝
- Uses OpenAI GPT-3.5-turbo to generate Vietnamese articles
- Optimized prompts for 150-200 word articles
- Structured content suitable for 5-part video division
- Natural language optimized for male voice narration

### 3. **Video Part Creation** 🎬
- **Text Splitting**: Divides article into 5 equal parts (~6 seconds each)
- **TTS Generation**: Converts each part to speech using Google Cloud TTS (Vietnamese male voice)
- **Lip-Sync Creation**: Generates talking avatar videos using Gooey.ai API
- **Parallel Processing**: Creates all 5 parts simultaneously for efficiency

### 4. **Video Merging** 🔗
- Uses FFmpeg to concatenate all 5 video parts
- Creates final 30-second video with seamless transitions
- Saves to database with metadata

## 📋 Prerequisites

### Required Software
```bash
# Java 17 or higher
java -version

# FFmpeg (for video merging)
ffmpeg -version

# Maven (for building)
mvn -version
```

### API Keys Required
1. **OpenAI API Key** - For article generation
2. **Google Cloud TTS API Key** - For speech synthesis
3. **Gooey.ai API Key** - For lip-sync video generation

## 🚀 Installation Guide

### 1. Clone Repository
```bash
git clone <repository-url>
cd FacelessVideo
```

### 2. Install FFmpeg

#### Windows:
```bash
# Using Chocolatey
choco install ffmpeg

# Or download from https://ffmpeg.org/download.html
# Add to PATH environment variable
```

#### macOS:
```bash
# Using Homebrew
brew install ffmpeg
```

#### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install ffmpeg
```

### 3. Create Required Directories
```bash
mkdir -p output assets uploads
```

### 4. Add Avatar Image
```bash
# Place your avatar image as:
assets/avatar.jpg
```

### 5. Configure API Keys
Create `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.base.url=http://localhost:8080

# Database (H2 for development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# API Keys (REPLACE WITH YOUR ACTUAL KEYS)
openai.api.key=sk-your-openai-api-key-here
google.tts.api.key=your-google-tts-api-key-here
gooey.api.key=your-gooey-api-key-here

# Logging
logging.level.com.ai.video.FacelessVideo=INFO
```

### 6. Build and Run
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 7. Access Dashboard
Open your browser and navigate to:
```
http://localhost:8080/dashboard
```

## 🔧 Configuration Details

### Google Cloud TTS Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable the Text-to-Speech API
3. Create a service account and download JSON key
4. Use the API key in your configuration

### Gooey.ai Setup
1. Sign up at [Gooey.ai](https://gooey.ai/)
2. Go to your profile settings
3. Generate an API key
4. Add to your configuration

### OpenAI Setup
1. Sign up at [OpenAI](https://openai.com/)
2. Go to API settings
3. Generate an API key
4. Add to your configuration

## 🏃‍♂️ Usage Instructions

### Using the Web Dashboard

1. **Start the Application**
   ```bash
   mvn spring-boot:run
   ```

2. **Open Dashboard**
   - Navigate to `http://localhost:8080/dashboard`

3. **Follow the 4-Step Workflow**:
   - **Step 1**: Click "Get Headline" to crawl VNExpress
   - **Step 2**: Click "Generate Article" to create Vietnamese content
   - **Step 3**: Click "Create 5 Videos" to generate video parts
   - **Step 4**: Click "Merge & Save" to create final video

### API Endpoints

```bash
# Get headline
GET /get-headline

# Generate article
POST /generate-article
Content-Type: application/json
{
  "headline": "Your headline here"
}

# Generate videos
POST /generate-videos
Content-Type: application/json
{
  "article": "Your article content here"
}

# Merge and save
POST /merge-and-save

# Reset workflow
POST /reset-workflow

# Check workflow status
GET /workflow-status

# Test FFmpeg
GET /test-ffmpeg
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/ai/video/FacelessVideo/
│   │   ├── controller/
│   │   │   ├── ArticleController.java      # Basic video generation
│   │   │   ├── CrawlController.java        # Main workflow controller
│   │   │   ├── FileController.java         # File upload handling
│   │   │   └── ViewController.java         # Web interface
│   │   ├── entity/
│   │   │   └── VideoRecord.java            # Database entity
│   │   ├── repository/
│   │   │   └── VideoRecordRepository.java  # Data access
│   │   ├── service/
│   │   │   ├── ChatGPTService.java         # OpenAI integration
│   │   │   ├── GoogleTTSService.java       # Google TTS (Male voice)
│   │   │   ├── GooeyLipSyncService.java    # Gooey.ai integration
│   │   │   ├── VideoProcessingService.java # FFmpeg operations
│   │   │   ├── QueueService.java           # Task management
│   │   │   └── TranslationService.java     # Translation utilities
│   │   ├── config/
│   │   │   └── StaticResourceConfig.java   # Static file serving
│   │   └── util/
│   │       └── ProcessRunner.java          # Process execution
│   └── resources/
│       ├── templates/
│       │   └── dashboard.html              # Web dashboard
│       └── application.properties          # Configuration
├── assets/
│   └── avatar.jpg                          # Avatar image for videos
├── output/                                 # Generated files
└── uploads/                                # Uploaded files
```

## 🔍 Code Workflow Explanation

### 1. **CrawlController.java**
Main workflow orchestrator with 4 endpoints:

```java
@GetMapping("/get-headline")     // Step 1: Crawl VNExpress
@PostMapping("/generate-article") // Step 2: Generate article
@PostMapping("/generate-videos")  // Step 3: Create 5 videos
@PostMapping("/merge-and-save")   // Step 4: Merge and save
```

### 2. **GoogleTTSService.java**
Handles text-to-speech conversion:

```java
// Male Vietnamese voice configuration
"name": "vi-VN-Neural2-D"
"pitch": -2.0  // Lower pitch for masculine sound

// Split text into 5 parts for better coverage
splitTextIntoFiveParts(String text)
```

### 3. **GooeyLipSyncService.java**
Manages lip-sync video creation:

```java
// File upload approach for better reliability
uploadFilesToGooeyWithRetry(File audioFile, File imageFile)

// Retry logic for rate limiting
generateVideoWithRetry(String audioPath, String imagePath, int attempt)
```

### 4. **VideoProcessingService.java**
Handles video merging with FFmpeg:

```java
// FFmpeg concatenation command
ffmpeg -f concat -safe 0 -i video_list.txt -c copy output.mp4
```

## 🐛 Troubleshooting

### Common Issues

#### 1. **FFmpeg Not Found**
```bash
# Error: ffmpeg: command not found
# Solution: Install FFmpeg and add to PATH
```

#### 2. **API Rate Limits**
```bash
# Error: 429 Too Many Requests
# Solution: Wait and retry (automatic retry implemented)
```

#### 3. **File Upload Failures**
```bash
# Error: File not found
# Solution: Check file paths and permissions
```

#### 4. **Database Connection Issues**
```bash
# Error: Unable to connect to database
# Solution: Check H2 configuration in application.properties
```

### Debug Mode
Enable debug logging:

```properties
logging.level.com.ai.video.FacelessVideo=DEBUG
logging.level.org.springframework.web=DEBUG
```

## 📊 Performance Optimization

### Parallel Processing
- Uses thread pool with 5 threads for simultaneous video generation
- Async processing for TTS and video creation
- Reduces total processing time from ~15 minutes to ~5 minutes

### Resource Management
- Automatic cleanup of temporary files
- Memory-efficient file handling
- Connection pooling for API calls

## 🔒 Security Considerations

1. **API Key Protection**: Store keys in environment variables for production
2. **File Validation**: Validate uploaded files
3. **Rate Limiting**: Implement request throttling
4. **Input Sanitization**: Clean user inputs

## 🌐 Production Deployment

### Environment Variables
```bash
export OPENAI_API_KEY=your-key-here
export GOOGLE_TTS_API_KEY=your-key-here
export GOOEY_API_KEY=your-key-here
export SERVER_BASE_URL=https://yourdomain.com
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y ffmpeg
COPY target/FacelessVideo-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📈 Future Enhancements

- [ ] Multiple avatar support
- [ ] Custom voice training
- [ ] Background music integration
- [ ] Subtitle generation
- [ ] Multiple language support
- [ ] Batch processing
- [ ] Video quality options
- [ ] Cloud storage integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 💬 Support

For issues and questions:
1. Check the troubleshooting section
2. Review the logs for error details
3. Create an issue with detailed information

## 🙏 Acknowledgments

- **VNExpress** for news content
- **OpenAI** for GPT API
- **Google Cloud** for TTS services
- **Gooey.ai** for lip-sync technology
- **FFmpeg** for video processing

---
