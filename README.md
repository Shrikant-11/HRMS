# HRMS - Organizational Structure Management System

A comprehensive Spring Boot application that models a company's employee hierarchy with departments, managers, and a CEO.  
The system enforces strict business rules around reporting structures, department assignments, and role responsibilities.

---

## Features

### Core Functionality
- **Employee Management**: Create, update, and manage employees with different roles.
- **Department Management**: Create and manage departments with department heads.
- **Hierarchical Structure**: Enforce reporting relationships and organizational constraints.
- **Role-based Access Control**: Different permissions for Admin and Employee roles.
- **JWT Authentication**: Secure API access with JSON Web Tokens.

### Business Rules Enforced
- Every employee must belong to one department and have one manager (except the CEO).
- The CEO has no manager and does not belong to any department.
- Each department has exactly one department head, who must report to the CEO.
- No two employees from the same department can report directly to the CEO.
- Employees and their managers must be from the same department.

---

## User Roles

### Admin (HR)
- Can manage all employees and departments.
- Can assign managers and move employees between departments.
- Can create and delete departments.
- Can assign department heads.
- Full access to organizational structure management.

### Employee
- Can view their own profile and manager.
- Can view their direct reports (if any).
- Can view employees in their department (if department head).
- Cannot modify organizational data.

---

## Technology Stack
- **Spring Boot** 3.2.5
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory for development)
- **Lombok** for reducing boilerplate code
- **SpringDoc OpenAPI** for API documentation
- **Java** 21

---

## Access URLs

- **Swagger Local URL** http://localhost:8080/swagger-ui/index.html#/
- **H2 Database URL** http://localhost:8080/h2-console/
- **H2 Database credentials** dbuser/password

## API Endpoints

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login with email and password |
| POST | `/api/auth/register` | Register new employee |
| GET | `/api/auth/me` | Get current user profile |

### Employee APIs
| Method | Path | Description | Who can access |
|--------|------|-------------|----------------|
| POST | `/api/v1/employees` | Create a new employee. Validates CEO uniqueness, manager/department rules. If `deptHead=true`, sets department head (must report to CEO). | ADMIN (service enforces rules; Dept Head may create only in own department) |
| PUT | `/api/v1/employees/{id}` | Full update of employee (cannot change manager/department here). | ADMIN; restricted to target's manager, department head of same department, or CEO |
| PATCH | `/api/v1/employees/{id}` | Partial update (cannot change manager/department here). | ADMIN; restricted to target's manager, department head of same department, or CEO |
| GET | `/api/v1/employees/{id}` | Get employee by ID. | Authenticated; CEO, self, or department head of same department |
| GET | `/api/v1/employees` | Get all employees. | CEO only |
| GET | `/api/v1/employees/department/{deptId}` | List employees in a department. | CEO or the head of that department |
| GET | `/api/v1/employees/manager/{managerId}` | List direct reports of a manager. | CEO, the manager, or department head of same department |
| PUT | `/api/v1/employees/{empId}/manager/{managerId}` | Assign a manager to an employee (validates rules). | ADMIN |
| GET | `/api/v1/employees/reportings` | Get my direct reportings. | Authenticated |
| GET | `/api/v1/employees/profile` | Get current user profile. | Authenticated |
| PUT | `/api/v1/employees/{empId}/move` | Move a non-head employee to another department and assign a manager. | CEO or Department Head (restricted to own department) |
| PUT | `/api/v1/employees/department-heads/{headId}/move` | Move a department head to another department and assign a replacement head. | CEO only |
| DELETE | `/api/v1/employees/{id}` | Delete an employee (cannot delete CEO or a head directly; not self). | ADMIN; restricted to target's manager, department head of same department, or CEO |

### Department APIs
| Method | Path | Description | Who can access |
|--------|------|-------------|----------------|
| POST | `/api/v1/departments` | Create a new department. | CEO only |
| PUT | `/api/v1/departments/{id}` | Full update of a department. | CEO only |
| PATCH | `/api/v1/departments/{id}` | Partial update of a department. | CEO only |
| GET | `/api/v1/departments/{id}` | Get department by ID (with head info and employee count). | Authenticated |
| GET | `/api/v1/departments` | Get all departments. | Authenticated |
| DELETE | `/api/v1/departments/{id}` | Delete a department (cannot delete if it has a head or employees). | CEO only |

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+

### Running the Application
```bash
# Clone the repository
git clone <repo-url>
cd hrms-springboot-full

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
