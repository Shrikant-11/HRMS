# HRMS - Organizational Structure Management System

A comprehensive Spring Boot application that models a company's employee hierarchy with departments, managers, and a CEO. The system enforces strict business rules around reporting structures, department assignments, and role responsibilities.

## Features

### Core Functionality
- **Employee Management**: Create, update, and manage employees with different roles
- **Department Management**: Create and manage departments with department heads
- **Hierarchical Structure**: Enforce reporting relationships and organizational constraints
- **Role-based Access Control**: Different permissions for Admin and Employee roles
- **JWT Authentication**: Secure API access with JSON Web Tokens

### Business Rules Enforced
- Every employee must belong to one department and have one manager (except the CEO)
- The CEO has no manager and does not belong to any department
- Each department has exactly one department head, who must report to the CEO
- No two employees from the same department can report directly to the CEO
- Employees and their managers must be from the same department

### User Roles

#### Admin (HR)
- Can manage all employees and departments
- Can assign managers and move employees between departments
- Can create and delete departments
- Can assign department heads
- Full access to organizational structure management

#### Employee
- Can view their own profile and manager
- Can view their direct reports (if any)
- Can view employees in their department (if department head)
- Cannot modify organizational data

## Technology Stack

- **Spring Boot 3.2.5**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory for development)
- **Lombok** for reducing boilerplate code
- **SpringDoc OpenAPI** for API documentation
- **Java 21**

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login with email and password
- `POST /api/auth/register` - Register new employee
- `GET /api/auth/me` - Get current user profile

### Employee APIs

| Method | Path | What it does | Who can access |
|---|---|---|---|
| POST | `/api/v1/employees` | Create a new employee. Validates CEO uniqueness, manager/department rules. If `deptHead=true`, sets department head (must report to CEO). | ADMIN (service enforces extra rules; Dept Head may create only in own department) |
| PUT | `/api/v1/employees/{id}` | Full update of employee (cannot change manager/department here). | ADMIN; service restricts to target's manager, department head of same department, or CEO |
| PATCH | `/api/v1/employees/{id}` | Partial update of employee (cannot change manager/department here). | ADMIN; service restricts to target's manager, department head of same department, or CEO |
| GET | `/api/v1/employees/{id}` | Get employee by ID. | Authenticated (ADMIN or EMPLOYEE); service allows CEO, self, or department head of same department |
| GET | `/api/v1/employees` | Get all employees. | ADMIN route; service restricts to CEO only |
| GET | `/api/v1/employees/department/{deptId}` | List employees in a department. | CEO or the head of that department |
| GET | `/api/v1/employees/manager/{managerId}` | List direct reports of a manager. | CEO, the manager themselves, or department head of same department |
| PUT | `/api/v1/employees/{empId}/manager/{managerId}` | Assign a manager to an employee (validates manager rules). | ADMIN |
| GET | `/api/v1/employees/reportings` | Get my direct reportings. | Authenticated (ADMIN or EMPLOYEE) |
| GET | `/api/v1/employees/profile` | Get current user profile. | Authenticated (ADMIN or EMPLOYEE) |
| PUT | `/api/v1/employees/{empId}/move` | Move a non-head employee to another department and assign a manager in one request. Enforces manager belongs to target department or is CEO, and CEO direct-report cap per department. | CEO or Department Head (Dept Head limited to employees in their department) |
| PUT | `/api/v1/employees/department-heads/{headId}/move` | Move a department head to another department and simultaneously assign a replacement head for the source department. Handles existing head in target by demoting them to report to the moving head. | CEO only |
| DELETE | `/api/v1/employees/{id}` | Delete an employee (cannot delete CEO or a head directly; not self). | ADMIN; service restricts to target's manager, department head of same department, or CEO |

### Department APIs

| Method | Path | What it does | Who can access |
|---|---|---|---|
| POST | `/api/v1/departments` | Create a new department. | ADMIN route; service restricts to CEO only |
| PUT | `/api/v1/departments/{id}` | Full update of a department. | ADMIN route; service restricts to CEO only |
| PATCH | `/api/v1/departments/{id}` | Partial update of a department. | ADMIN route; service restricts to CEO only |
| GET | `/api/v1/departments/{id}` | Get department by ID (includes head info and employee count). | Authenticated (ADMIN or EMPLOYEE) |
| GET | `/api/v1/departments` | Get all departments. | Authenticated (ADMIN or EMPLOYEE) |
| DELETE | `/api/v1/departments/{id}` | Delete a department (cannot delete if it has a head or employees). | ADMIN route; service restricts to CEO only |

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hrms-springboot-full
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

### Sample Data

The application initializes with sample data including:

**Admin Users:**
- CEO: `ceo@company.com` / `password`
- Engineering Head: `eng.head@company.com` / `password`
- Marketing Head: `marketing.head@company.com` / `password`
- HR Head: `hr.head@company.com` / `password`

**Employee Users:**
- Developer: `dev1@company.com` / `password`
- Marketer: `marketer1@company.com` / `password`
- HR Staff: `hr.staff@company.com` / `password`

### API Usage Examples

#### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ceo@company.com",
    "password": "password"
  }'
```

#### 2. Create Employee (Admin only)
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "New Employee",
    "email": "new.employee@company.com",
    "password": "password",
    "role": "EMPLOYEE",
    "departmentId": 1,
    "managerId": 2,
    "isCeo": false,
    "isDeptHead": false
  }'
```

#### 3. Get All Employees (CEO only)
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer <your-jwt-token>"
```

#### 4. Create Department (CEO only)
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "Finance",
    "description": "Finance and Accounting Team"
  }'
```

## Database Schema

### Employee Table
- `id` (Primary Key)
- `name` (Not null)
- `email` (Unique, not null)
- `password` (Not null, encrypted)
- `role` (ADMIN/EMPLOYEE)
- `department_id` (Foreign Key)
- `manager_id` (Foreign Key to Employee)
- `is_ceo` (Boolean)
- `is_dept_head` (Boolean)

### Department Table
- `id` (Primary Key)
- `name` (Unique, not null)
- `description`
- `head_id` (Foreign Key to Employee)

## Business Logic

### Employee Creation Rules
1. Only one CEO can exist
2. Non-CEO employees must have a manager
3. Department heads must belong to a department
4. Email must be unique

### Department Head Assignment Rules
1. CEO cannot be a department head
2. Employee must belong to the department to be its head
3. Department can have only one head
4. Department head must report to CEO

### Employee Movement Rules
1. CEO cannot be moved
2. Department heads cannot be moved directly (use dedicated head move APIs)
3. Employee and manager must be in same department

### Manager Assignment Rules
1. CEO cannot have a manager
2. Employee cannot be their own manager
3. Employee and manager must be in same department
4. Only one employee per department can report directly to CEO

## Security

- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Role-based Authorization**: Different permissions for Admin and Employee roles
- **Password Encryption**: BCrypt password hashing
- **Input Validation**: Comprehensive validation for all inputs
- **Exception Handling**: Global exception handler with proper error responses

## Configuration

Key configuration properties in `application.yaml`:
- Database connection settings
- JWT secret and expiration
- Server port and logging levels

## Development

### Project Structure
```
src/main/java/com/hrms/
├── controller/          # REST controllers
├── DTOs/              # Data Transfer Objects
├── exception/          # Custom exceptions
├── model/             # Entity models
├── repository/         # Data access layer
├── security/          # Security configuration
├── service/           # Business logic
│   └── impl/         # Service implementations
└── config/            # Configuration classes
```

### Adding New Features
1. Create/update entity models in `model/`
2. Add repository methods in `repository/`
3. Implement business logic in `service/impl/`
4. Create DTOs in `DTOs/`
5. Add controller endpoints in `controller/`
6. Update security configuration if needed

## Testing

The application includes comprehensive business rule validation and can be tested using:
- Swagger UI for API testing
- H2 Console for database inspection
- Sample data for testing different scenarios

## License

This project is licensed under the MIT License. #   H R M S  
 