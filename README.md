# Notifications
SQS to Email streamer lambda function

Lambda function that streams user notifications from an SQS queue and sends digest emails to the users.

Tech stack:
------------
- Scala
- Akka-Streams
- Akka-Alpakka
- AWS SQS
- AWS Lamda
- Courier

Environment variables:

```
ACCESS_KEY - AWS access key
SECRET_KEY - AWS secret key
REGION - AWS region
SQS_QUEUE_URL - The url of the SQS queue
EMAIL_SUBJECT - The subject of the email
SOURCE_EMAIL - The sender of the email
EMAIL_USER_NAME - SMTP username
EMAIL_PASSWORD - SMTP password
SMTP_PORT - SMTP port
SMTP_HOST - SMTP host
```