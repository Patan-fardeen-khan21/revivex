# Revivex MVP

Revivex is a fully working, production-style payment and order management application built from scratch. It demonstrates a real-world integration with Razorpay, a modern React/Vite frontend, and a secure Spring Boot backend.

## 1. Project Overview
This MVP allows users to:
- Register and securely log in using JWT authentication.
- Browse available products and add them to a shopping cart.
- Proceed to checkout, generating a secure Razorpay order from the backend.
- Complete the payment via the Razorpay Checkout popup in the frontend.
- Have their payment signature verified securely on the backend.
- View their past orders and payment statuses.
- (Admins) View all orders and payments in the admin dashboard.

## 2. Architecture
- **Backend:** Java 17, Spring Boot 3.x (Web, Data JPA, Security, Validation), Oracle Database, Maven.
- **Frontend:** React, Vite, TypeScript, Axios, standard CSS styling (no Tailwind).
- **Payment Gateway:** Razorpay Java SDK.

## 3. Technology Stack
- **Database:** Oracle Database (e.g. Oracle Express Edition)
- **Security:** BCrypt Password Hashing, JWT (JSON Web Tokens), Role-Based Access Control (RBAC)
- **API Design:** RESTful conventions.
- **Error Handling:** Global ControllerAdvice for unified API responses.

## 4. Database Setup
Ensure you have Oracle Database (like Oracle XE) installed and running locally.
If necessary, create a user and grant privileges (if not using the default system user):
```sql
CREATE USER revivex IDENTIFIED BY revivex123;
GRANT CONNECT, RESOURCE, DBA TO revivex;
```

## 5. Oracle Configuration
Update the `.env` (or `.env.example`) file with your database credentials.
Spring Data JPA is configured to automatically update the schema (`ddl-auto: update`), so tables will be generated automatically on the first run. The `DataSeeder` will populate mock products if none exist.

## 6. Razorpay Test Account/Key Setup
1. Sign up for a [Razorpay Dashboard](https://dashboard.razorpay.com/) account.
2. Enable "Test Mode".
3. Navigate to **Settings > API Keys** and generate a new key pair.
4. Copy the `Key Id` and `Key Secret`.

## 7. Environment Variables
You must create a `.env` file (or set environment variables in your run configuration) at the root level (or configure them in `backend/src/main/resources/application.yml`).
```
DB_URL=jdbc:oracle:thin:@localhost:1521:xe
DB_USERNAME=system
DB_PASSWORD=your_oracle_password
JWT_SECRET=your_super_secret_key_needs_to_be_long_enough
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
```
*Note: In the frontend, create `frontend/.env` with:*
```
VITE_RAZORPAY_KEY_ID=your_razorpay_key_id
```

## 8. Backend Startup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   The backend will start on `http://localhost:8080`.

## 9. Frontend Startup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
   The frontend will be available at `http://localhost:5173`.

## 10. API Overview
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
### Products
- `GET /api/products`
- `GET /api/products/{id}`
### Cart
- `GET /api/cart`
- `POST /api/cart/items`
- `DELETE /api/cart/items/{id}`
### Orders
- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{id}`
### Payments
- `POST /api/payments/verify`
### Admin
- `GET /api/admin/orders`
- `GET /api/admin/payments`

## 11. Testing
To run backend tests (JUnit & Mockito):
```bash
cd backend
./mvnw test
```
The test suite includes `OrderServiceTest` which mocks the Razorpay SDK to ensure the order creation logic works securely without making external API calls.

## 12. Example Payment Flow
1. Register a new user at `http://localhost:5173/register`.
2. Browse products and add items to your cart.
3. Go to the Cart page and click **Proceed to Checkout**.
4. Click **Pay with Razorpay**.
5. Use Razorpay test credentials (e.g., test card details provided by Razorpay in Test Mode) to complete the transaction.
6. The frontend sends the Razorpay payment ID and signature to the backend.
7. The backend securely verifies the HMAC-SHA256 signature using the secret key.
8. The order status is updated to `PAID`.
9. Navigate to "Orders" to view the success state.

## 13. Troubleshooting
- **CORS Errors:** Ensure the frontend is running on `http://localhost:5173` or `http://localhost:3000` as configured in `SecurityConfig.java`.
- **Razorpay Verification Failed:** Ensure your frontend and backend are using the exact same Razorpay Key ID and Secret. Ensure your backend amount is calculated exactly in paise.
- **Database Connection Refused:** Verify Oracle Database is running and the credentials in `application.yml` (or your env vars) are correct.
