# APU Automotive Service Centre (APU-ASC) - Roles and Functionalities

## All Users
* **Login**: Access the system.
* **Edit Personal Profile**: Manage personal account details.
* **Notification System**: Triggers pop-up notifications by fetching `notification.txt` when a user logs in or performs certain actions.

## Customer
* **Register**: Sign up for a new account.
* **Book Appointment**: Triggers a pushed notification to appear at the Counter Staff's dashboard, waiting for the staff to assign it to a technician (Default status is "Pending").
  * **Payment Processing**:
    * **Online Payment**: Updates the payment status automatically.
    * **Physical Payment**: Payment status defaults to "Pending". Counter Staff must click a button (e.g., "Paid Physically") to manually update the status.
  * **Provide Comments**: Provide comments for Counter Staff and Technicians involved in specific appointments (Let counter staff and technician know more about the customer's situation).
* **Check Appointment Status**: View the current state of their appointments (e.g., Pending, Declined, Assigned to XXX).
  * **Provide Service Review**: Provide service review for the appointment (e.g. Good service!).
* **View Service Histories**:
  * View feedback provided by Technicians for individual appointments.
* **View Payment Histories**: View past transactions.

## Counter Staff
* **CRUD Customer Account**: Create, Read, Update, and Delete customer profiles.
* **Manage Appointments**:
  * Create new appointments.
  * Assign appointments to available technicians (allocating 1 hour for normal service and 3 hours for major service).
  * Update appointments. Any updates must notify both the customer and the assigned technicians.
* **Check Appointment Status**: View the statuses for all customers' appointments (e.g., Pending, Declined, Assigned to XXX).
* **Collect Payment**: Process physical payments made by customers.
  * **Generate Receipts**: Issue receipts for completed payments.

## Technicians
* **View Upcoming Appointments**: See scheduled jobs, provided they have been assigned to them.
  * **Check Details**: Review the comments by the customer and specific details of their individually assigned appointments.
* **Update Individual Appointment Status**: Change the status of their individual assigned appointments to “Completed” once finished.
  * **Provide Feedback**: Write feedback for their individually assigned appointments.

## Managers
* **CRUD Staff**: Create, Read, Update, and Delete accounts for managers, counter staff, and technicians.
* **Set Service Prices**: Define the costs for both Normal Services and Major Services.
* **Analyse Reports**: Generate and review system reports.
* **View Feedbacks, Comments and Service Reviews**: Access and review all feedback, comments and service reviews left across all customer services.

## UML Use Case Diagram
![UML Use Case Diagram](<UML Diagram/v1 UML Use Case Diagram.png>)