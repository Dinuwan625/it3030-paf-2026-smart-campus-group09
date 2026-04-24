# LMS Final Guide (Concise)

## 1. Overview
This project runs with:
- Backend: Spring Boot on port 8089
- Frontend: React on port 3000
- Database: MongoDB on localhost:27017
- Auth: Spring Security session-based login + Google OAuth2

## 2. Prerequisites
Install and verify:
- JDK 17+: `java -version`
- Node.js + npm: `node -v` and `npm -v`
- MongoDB: `mongod --version` or `mongosh --version`

## 3. Backend Setup
From project root:

```bash
cd demo
.\mvnw.cmd clean compile -DskipTests
.\mvnw.cmd spring-boot:run
```

Backend should be available at:
- `http://localhost:8089`

## 4. Frontend Setup
In a new terminal:

```bash
cd frontend
npm install
npm start
```

Frontend should be available at:
- `http://localhost:3000`

## 5. Required Configuration
File: `demo/src/main/resources/application.properties`

```properties
server.port=8089
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=smartcampus

spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8089/login/oauth2/code/{registrationId}

app.frontend.url=http://localhost:3000
```

## 6. Google Cloud Console
In OAuth Client settings, ensure:
- Authorized redirect URI: `http://localhost:8089/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost:3000` and `http://localhost:8089`

## 7. Login Behavior
- Form login submits to: `POST http://localhost:8089/login`
- Google login starts at: `GET http://localhost:8089/oauth2/authorization/google`
- Session cookie used: `JSESSIONID`
- Success redirect currently goes to frontend home (`/`)

## 8. Quick Validation
1. Register a user from `/register`
2. Login with username/email + password from `/login`
3. Check navbar shows `Welcome, <your-name>`
4. Open catalogue page and verify data loads
5. Logout and verify session ends

## 9. Troubleshooting
- Redirect loop to login:
  - Restart backend after security changes
  - Verify user credentials exist in MongoDB
  - Verify browser accepts cookies for localhost
- Google `redirect_uri_mismatch`:
  - Ensure exact URI matches in Google Console
- Catalogue not loading:
  - Confirm backend running on 8089
  - Confirm authenticated session exists
- Welcome name shows `User`:
  - Re-login so session user info refreshes

## 10. Key Files
- `demo/src/main/java/com/smartcampus/demo/Config/SecurityConfig.java`
- `demo/src/main/java/com/smartcampus/demo/Config/OAuth2AuthenticationSuccessHandler.java`
- `demo/src/main/java/com/smartcampus/demo/Controller/UserInfoController.java`
- `demo/src/main/resources/application.properties`
- `frontend/src/components/Login.jsx`
- `frontend/src/components/Navbar.jsx`
- `frontend/src/components/LearningPlanList.jsx`

## 11. New User Setup (New Laptop)

Use this checklist when handing the project to a new team member.

### A. Clone and install prerequisites
1. Install JDK 17, Node.js/npm, and MongoDB.
2. Clone the project repository.
3. Start MongoDB service.

### B. Configure backend locally
Open `demo/src/main/resources/application.properties` and verify:

```properties
server.port=8089
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=smartcampus
app.frontend.url=http://localhost:3000
```

### C. Configure Google OAuth2 credentials
New team member has two options:

1. Use shared team OAuth client:
- Reuse existing client-id and client-secret.
- Their Google account must be allowed in the OAuth consent screen (if app is in Testing mode).

2. Use personal OAuth client:
- Create a new OAuth client in Google Cloud Console.
- Replace local values in `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8089/login/oauth2/code/{registrationId}
```

In both cases, Google Console must include:
- Authorized redirect URI: `http://localhost:8089/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost:3000` and `http://localhost:8089`

### D. Start the project
Backend:

```bash
cd demo
.\mvnw.cmd clean compile -DskipTests
.\mvnw.cmd spring-boot:run
```

Frontend (new terminal):

```bash
cd frontend
npm install
npm start
```

### E. Verify working setup
1. Open `http://localhost:3000`.
2. Register a new user.
3. Login using username/email + password.
4. Test `Continue with Google`.
5. Confirm navbar shows `Welcome, <name>` and catalogue loads.

### F. Team security note
- Do not commit real OAuth secrets to git.
- Prefer local-only values or environment-based overrides for client secret.
