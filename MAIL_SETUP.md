# Email setup for password reset

Password recovery needs an SMTP account. No credentials are committed to the repo.

## 1. Supply the SMTP settings

Set these as environment variables (or JVM `-D` properties) before starting the backend:

| Variable | Example | Default |
| --- | --- | --- |
| `APP_MAIL_HOST` | `smtp.gmail.com` | `smtp.gmail.com` |
| `APP_MAIL_PORT` | `587` | `587` |
| `APP_MAIL_USERNAME` | `your-address@gmail.com` | empty |
| `APP_MAIL_PASSWORD` | Gmail **app password** | empty |
| `APP_MAIL_FROM` | `no-reply@yourdomain.com` | same as username |

Spring Boot maps `APP_MAIL_USERNAME` to the `app.mail.username` property used by
`MailConfig`. For Gmail, enable 2-factor authentication and create an **App password**
(Google Account → Security → App passwords); your normal account password will not work.

PowerShell example:

```powershell
$env:APP_MAIL_USERNAME = "you@gmail.com"
$env:APP_MAIL_PASSWORD = "your-16-character-app-password"
mvn spring-boot:run
```

If `APP_MAIL_USERNAME` / `APP_MAIL_PASSWORD` are empty, the app still starts, but
`POST /api/users/forgot-password` returns **503** because no email can be delivered.

## 2. Restart Redis

The verification code is stored in Redis with a TTL of `app.redis.otp-expiration`
(300 seconds by default). Redis must be running on the configured port.

## 3. Use the flow

1. `POST /api/users/forgot-password` with `{ "email": "you@example.com" }`
   → `{ "message": "If the email is registered, a verification code has been sent." }`
2. The registered user receives a 6-digit code by email.
3. `POST /api/users/reset-password` with
   `{ "email": "you@example.com", "otp": "123456", "newPassword": "newSecret" }`
   → `{ "message": "Password updated successfully" }`

The OTP is single-use and expires after 5 minutes. Wrong, expired or reused codes
return HTTP 400 with the standard error DTO. Unknown emails receive the same generic
message as known ones, so the endpoint cannot be used to enumerate accounts.

The old debug endpoints `POST /api/redis/otp/generate` and `/otp/verify` were removed:
they returned the code in the HTTP response, which allowed account takeover.
