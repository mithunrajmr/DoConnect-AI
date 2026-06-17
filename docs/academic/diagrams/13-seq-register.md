# Sequence Diagram: Register

### Explanation
This sequence diagram maps the user registration flow, including validation.

### Source Code References
- `AuthController.java` (`@PostMapping("/register")`), `AuthService.java`.

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant AuthService
    participant PasswordEncoder
    participant UserRepository

    User->>Frontend: Enter Name, Email, Password
    Frontend->>AuthController: POST /api/auth/register
    AuthController->>AuthService: register(request)
    AuthService->>UserRepository: existsByEmail(email)
    
    alt Email exists
        UserRepository-->>AuthService: true
        AuthService-->>AuthController: Exception (Email taken)
        AuthController-->>Frontend: 400 Bad Request
    else Email available
        UserRepository-->>AuthService: false
        AuthService->>PasswordEncoder: encode(password)
        PasswordEncoder-->>AuthService: Hash
        AuthService->>UserRepository: save(User)
        UserRepository-->>AuthService: Saved User
        AuthService-->>AuthController: AuthResponse (with JWT)
        AuthController-->>Frontend: 201 Created
        Frontend-->>User: Redirect to Home
    end
```
