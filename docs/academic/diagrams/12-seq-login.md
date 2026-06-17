# Sequence Diagram: Login

### Explanation
This sequence diagram shows the strict flow of a user authenticating and receiving a JWT.

### Source Code References
- `AuthController.java` (`@PostMapping("/login")`), `AuthService.java`, `JwtService.java`.

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant AuthService
    participant AuthenticationManager
    participant JwtService
    participant DB as MySQL

    User->>Frontend: Enter Email & Password
    Frontend->>AuthController: POST /api/auth/login
    AuthController->>AuthService: login(request)
    AuthService->>AuthenticationManager: authenticate(Token)
    AuthenticationManager->>DB: findByEmail()
    DB-->>AuthenticationManager: UserDetails
    AuthenticationManager->>AuthenticationManager: Verify Password Hash
    
    alt Valid Credentials
        AuthenticationManager-->>AuthService: Authentication Object
        AuthService->>JwtService: generateToken(User)
        JwtService-->>AuthService: JWT String
        AuthService-->>AuthController: AuthResponse(token, userDto)
        AuthController-->>Frontend: 200 OK
        Frontend->>Frontend: Store Token in Context/Storage
        Frontend-->>User: Redirect to Home Feed
    else Invalid Credentials
        AuthenticationManager-->>AuthService: throws BadCredentialsException
        AuthService-->>AuthController: Exception Thrown
        AuthController-->>Frontend: 401 Unauthorized
        Frontend-->>User: Show Error "Bad Credentials"
    end
```
