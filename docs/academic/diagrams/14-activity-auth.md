# Activity Diagram: User Authentication

### Explanation
This flowchart details the sequence of activities and decision points during user login and registration.

### Source Code References
- `AuthController.java`, `AuthService.java`

```mermaid
flowchart TD
    Start((Start)) --> Choice{New User?}
    
    Choice -- Yes --> RegForm[Fill Registration Form]
    RegForm --> SubmitReg[Submit Register]
    SubmitReg --> CheckEmail{Email Exists?}
    CheckEmail -- Yes --> Error1[Show Email Taken Error]
    Error1 --> RegForm
    CheckEmail -- No --> HashPass[Hash Password & Save User]
    HashPass --> GenToken1[Generate JWT Token]
    GenToken1 --> Home[Redirect to Home Feed]
    
    Choice -- No --> LoginForm[Fill Login Form]
    LoginForm --> SubmitLogin[Submit Login]
    SubmitLogin --> CheckCreds{Valid Credentials?}
    CheckCreds -- No --> Error2[Show Invalid Login Error]
    Error2 --> LoginForm
    CheckCreds -- Yes --> GenToken2[Generate JWT Token]
    GenToken2 --> Home
    
    Home --> End((End))
```
