# Complete Jobs & Applications Testing Workflow (Swagger UI) - Continued

*This is the corrected continuation of your guide, covering the Express Interest, Formal Request, and Application flows. The previous tutorial had a few structural mistakes that have been fixed below.*

---

## PART 2: The Express Interest & Networking Flow

### Step 39: Bookmark a Candidate (Enterprise)
*As an Enterprise, you found a student you really like and want to bookmark them. You must be authorized as **ENTERPRISE** (🔒).*

1. **Log in** with the Enterprise owner:
   * **Endpoint:** `POST /api/v1/auth/login`
   * **Payload:** 
     ```json
     {
       "email": "olivia.bennett@example.com",
       "password": "Secure@123"
     }
     ```
2. Copy the `accessToken` from the response and **Authorize** Swagger.
3. Find the endpoint: **`POST /api/v1/interests/bookmark`**
4. Paste this request body:
   ```json
   {
     "studentId": "bbd25dd8-90ab-457b-8ad6-38becf4ec4d8",
     "openingId": null 
   }
   ```
5. Click **Execute**.
6. **Important:** Look at the `200 OK` JSON response. Copy the `"id"` value (e.g., `5098574a-174a-462d-b151-5b4b4a4ae9fc`). You will need this for the next step!


### Step 40: Escalate Bookmark to a Formal Request (Enterprise)
*You (the Enterprise) decided you actually want to talk to this candidate, so you escalate your Bookmark to a Formal Request.*

1. **Stay logged in** as Olivia / Enterprise (do not change your token).
2. Find the endpoint: **`POST /api/v1/interests/{interestId}/formal-request`**
3. Paste the `"id"` you copied from Step 39 into the `interestId` path parameter field.
4. Click **Execute**.
5. **Expected Result:** `200 OK`. The system automatically escalates the interest stage and fires off a notification to the student!

---

## PART 3: The Student Experience

### Step 41: Student Checks Notifications
*The Student logs in and sees that NovaTech Solutions has formally requested to connect.*

1. Scroll up to `POST /api/v1/auth/login` and log in as the Student:
   * **Payload:** 
     ```json
     {
       "email": "emily.carter81@example.test",
       "password": "Secure@123"
     }
     ```
2. Copy the new `accessToken`, go to the top of Swagger, click **Logout**, then **Authorize** and paste the new token.
3. Find the endpoint: **`GET /api/v1/notifications`**
4. Click **Execute**.
5. **Expected Result:** `200 OK`. In the response list, you should see a shiny new notification titled `"Formal Request Received"`!


### Step 42: Student Applies to a Job (Student)
*The Student is thrilled and decides to apply for a Job Opening at the Enterprise.*

> **Prerequisite Note:** To apply for a job, an Opening must actually exist in the database! Since your database currently has no job openings, you'll need to create one as an Enterprise first.

1. **Stay logged in** as Emily (Student).
2. Find the endpoint: **`POST /api/v1/applications/jobs/{jobId}/apply`**
3. Paste a valid Opening UUID into the `jobId` parameter. (If you don't have one, just use a dummy UUID like `11111111-2222-3333-4444-555555555555` to see how the system handles a "Job Not Found" error).
4. Click **Execute**.
5. **Expected Result:** `201 CREATED`. You have successfully submitted a job application!
