# APU Automotive Service Centre System

Welcome to the APU Automotive Service Centre System repository. This application handles the core operations of our automotive service centre, including managing users, appointments, feedback, services, and payments.

## Default Base Users

For initial setup and testing purposes, the system is seeded with a set of default "base users". This ensures you have access to all distinct roles and functionalities immediately upon startup.

| User ID | Username | Name | Role | Contact |
|---|---|---|---|---|
| MGR0001 | **manager** | Vert | Manager | vert@gmail.com / 01234567890 |
| CUS0001 | **cus** | John Doe | Customer | john@gmail.com / 01234567890 |
| STF0001 | **staff** | Elivius | CounterStaff | elivius@gmail.com / 01234678898 |
| TEC0001 | **tech** | Jones | Technician (Body Kit) | jones@gmail.com / 01234567890 |

> The default password for **all** of these base users is: `123123`

You can log in using the `Username` and `Password` combination to access their specific dashboards and privileges. If you register new users through the application, their credentials will be individually hashed and stored as expected.
