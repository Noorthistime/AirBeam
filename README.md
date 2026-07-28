# AirBeam
A full-stack peer-to-peer (P2P) file sharing web application allowing users to share, manage, and download files securely using Java Spring Boot, PostgreSQL, and modern web frontend technologies.

## Project Structure

```text
AirBeam/
├── src/main/
│   ├── java/com/project/p2p/
│   │   ├── controller/                 # REST API Controllers
│   │   │   ├── UserController.java
│   │   │   └── FileController.java
│   │   ├── service/                    # Business Logic Layer
│   │   │   ├── FileService.java
│   │   │   └── UserService.java
│   │   ├── model/                      # Entity Classes & DTOs
│   │   │   ├── UserAccount.java
│   │   │   └── FileMetadata.java
│   │   ├── repository/                 # Data Access Objects (Spring Data JPA)
│   │   │   └── UserRepository.java
│   │   └── P2pApplication.java         # Spring Boot Main Class
│   └── resources/
│       ├── static/                     # Frontend Assets (HTML, CSS, JS)
│       │   ├── index.html              # Main Single Page Application UI
│       │   ├── css/                    # Stylesheets
│       │   └── js/                     # Frontend Logic
│       ├── application.properties      # Development Configs
│       └── application-prod.properties # Production Configs
├── storage/                            # Local storage for P2P shared chunks
├── pom.xml                             # Maven configuration
└── README.md                           # Project Documentation
```

## Features

### 1. User Management
- User registration with validation
- Secure login with session management
- User profile management and customization
- Admin dashboard for user administration
- Logout functionality

### 2. Peer-to-Peer File Sharing
- Upload and share files securely across connected peers
- Receive shared files dynamically in real-time
- "Wanna Receive?" toggle to enable or disable incoming files
- View shared files across the network

### 3. Storage & File Management
- Real-time tracking of file storage and chunks
- Chunked file upload and automated merging
- Delete specific shared files from the network
- Instant availability updates

### 4. Admin Control & Session Management
- Comprehensive Admin Control Panel
- Monitor all registered user accounts
- Terminate user accounts remotely with instant notification
- Real-time polling for user session status changes

## Technologies Used
- **Backend:** Java Spring Boot
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Web Server:** Embedded Apache Tomcat
- **Frontend:** HTML5, CSS3, Vanilla JavaScript

## Database Setup

### Prerequisites
- PostgreSQL Server (12.0+)
- Java 17 or higher
- Maven 3.6+

### Installation Steps

#### 1. Create Database
```bash
psql -u postgres
CREATE DATABASE airbeam;
```

#### 2. Database Configuration
Edit `src/main/resources/application.properties` and update your PostgreSQL connection details:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/airbeam
spring.datasource.username=postgres
spring.datasource.password=your_password
```

## Building the Project

### Using Maven
```bash
# Clean and build the project
mvn clean install

# Package as executable JAR
mvn package
```

## Running the Application

1. Start your PostgreSQL Server
2. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
3. Access the application in your browser:
   ```text
   http://localhost:8082/
   ```

## Default Login Credentials
- Please register a new user account on the home screen to begin.
- Admin access can be assigned via the database `admin` column.

## API Endpoints

### Authentication & Users
- `POST /api/users/register` - Process new account registration
- `POST /api/users/login` - Process login and create session
- `POST /api/users/delete-account` - Terminate user account (Admin/User)
- `GET /api/users/status` - Check active session status

### Files & P2P Sharing
- `POST /api/files/upload` - Upload file chunks to P2P network
- `GET /api/files/list` - View available shared files
- `DELETE /api/files/remove` - Remove a shared file from the network

## Database Tables

### user_accounts
- `user_id` (Primary Key, VARCHAR)
- `display_name` (VARCHAR)
- `password` (VARCHAR)
- `admin` (BOOLEAN)
- `created_at` (TIMESTAMP)

## Security Features
- Session-based user authentication
- SQL injection prevention using Spring Data JPA & Prepared Statements
- Input validation and parameter sanitization
- Real-time account termination enforcement via polling

## Future Enhancements
- Password encryption using BCrypt
- WebRTC integration for direct peer-to-peer browser file transfer
- Advanced reporting and storage statistics for Admins
- Mobile application and progressive web app (PWA) support
- End-to-end encryption for shared files

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL server is running
- Check database name, URL, and credentials in `application.properties`
- Ensure the PostgreSQL JDBC driver is present in `pom.xml`

### Session Issues
- Clear browser cookies and local storage
- Restart the embedded Tomcat / Spring Boot server
- Verify account status using the status endpoint

### Maven Build Issues
```bash
# Force update dependencies
mvn dependency:resolve -U

# Clean Maven cache and rebuild
mvn clean install -DskipTests
```

## Performance Optimization
- Implement database connection pooling (HikariCP)
- Cache frequently accessed static files and assets
- Optimize chunk merging algorithms for large file uploads

## License
This project is open source and available under the MIT License.

## Author
Developed by: Noor Mohammad

## Support
For issues and questions, please refer to the documentation or contact support.

## Deployment Checklist
- [x] PostgreSQL database created and configured
- [x] `application.properties` credentials updated
- [x] `pom.xml` dependencies resolved
- [x] JAR file built successfully
- [x] Embedded Tomcat server configured
- [x] Application deployed and tested
- [x] Login functionality verified
- [x] Session management working
