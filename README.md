AirBeam
A full-stack peer-to-peer (P2P) file sharing web application allowing users to share, manage, and download files securely using Java Spring Boot, PostgreSQL, and modern web frontend technologies.

Project Structure
AirBeam/
├── src/main/
│   ├── java/com/project/p2p/
│   │   ├── controller/         # REST API Controllers
│   │   │   ├── UserController.java
│   │   │   └── FileController.java
│   │   ├── service/            # Business Logic Layer
│   │   │   ├── FileService.java
│   │   │   └── UserService.java
│   │   ├── model/              # Entity Classes & DTOs
│   │   │   ├── UserAccount.java
│   │   │   └── FileMetadata.java
│   │   ├── repository/         # Data Access Objects (Spring Data JPA)
│   │   │   └── UserRepository.java
│   │   └── P2pApplication.java # Spring Boot Main Class
│   └── resources/
│       ├── static/             # Frontend Assets (HTML, CSS, JS)
│       │   ├── index.html      # Main Single Page Application UI
│       │   ├── css/            # Stylesheets
│       │   └── js/             # Frontend Logic
│       ├── application.properties      # Development Configs
│       └── application-prod.properties # Production Configs
├── storage/                    # Local storage for P2P shared chunks
├── pom.xml                     # Maven configuration
└── README.md                   # Project Documentation

Features
1. User Management
User registration with validation
Secure login with session management
User profile management and profile customization
Admin dashboard for user administration (terminate accounts)
Logout functionality
2. Peer-to-Peer File Sharing
Upload and share files securely to connected peers
Receive shared files dynamically
Wanna Receive? toggle to enable/disable incoming files
View shared files in the network
3. Storage & Inventory
Real-time tracking of file storage
Chunked file handling and merging
Delete specific files from the network
4. Admin Capabilities
Admin control panel
Monitor all registered users
Terminate user sessions or accounts remotely
Dynamic polling for account status changes

Technologies Used
Backend: Java Spring Boot
Database: PostgreSQL
Build Tool: Maven
Web Server: Embedded Tomcat
Frontend: HTML5, CSS3, Vanilla JavaScript

Database Setup
Prerequisites
PostgreSQL Server
Java 17 or higher
Maven 3.6+

Installation Steps
Database Configuration
Edit src/main/resources/application.properties
Update PostgreSQL connection details:
spring.datasource.url=jdbc:postgresql://<your-db-host>/airbeam
spring.datasource.username=<your_username>
spring.datasource.password=<your_password>

Building the Project
Using Maven
# Clean and build
mvn clean install

# Package as JAR
mvn package

Running the Application
Start PostgreSQL Database
Run the Spring Boot application
mvn spring-boot:run
Access the application
http://localhost:8082/

Default Login Credentials
Please register a new account on the start screen.

API Endpoints
Authentication & Users
POST /api/users/register - Process registration
POST /api/users/login - Process login
POST /api/users/delete-account - Admin/User account deletion
GET /api/users/status - Check user active status
Files & P2P
POST /api/files/upload - Upload file chunks
GET /api/files/list - View available files
DELETE /api/files/remove - Delete shared file

Database Tables
user_accounts
user_id (Primary Key)
display_name
password
admin (Boolean)
created_at

Security Features
Session-based authentication
SQL injection prevention (Spring Data JPA)
Input validation
Real-time account termination enforcement

Future Enhancements
Password encryption (BCrypt)
WebRTC integration for direct peer transfers
Advanced reporting and statistics for Admins
Mobile responsive UI enhancements

Troubleshooting
Database Connection Issues
Verify PostgreSQL server is running
Check database name, render URL, and credentials
Session Issues
Clear browser cookies and local storage
Check session timeout settings

Performance Optimization
Implement connection pooling (HikariCP)
Cache frequently accessed static files
Optimize chunk merging algorithms for large files

License
This project is open source and available under the MIT License.

Author
Developed by: Noor Mohammad

Support
For issues and questions, please refer to the documentation or contact support.

Deployment Checklist
 PostgreSQL database configured
 application.properties updated
 pom.xml dependencies resolved
 JAR file built successfully
 Application deployed
 Login functionality verified
 Session management working
