# Simple-LLM-Api
simple LLM api method testing

## Default system message 
```
curl --location --request GET 'http://localhost:8080/role/default/chat' \
--header 'Content-Type: application/json' \
--data 'Hi, what is the working hours of the hospital?'
```
## PromptTemplate usage with simple text
```
curl --location --request GET 'http://localhost:8080/chat?username=sovon' \
--header 'Content-Type: application/json' \
--data 'Project Aurora – Weekly Sprint Update & Operations Log

Team, here is the rundown for Sprint 14. We ran into a major blocker on Tuesday with the legacy database migration: roughly 12% of user profile records threw schema validation errors due to unexpected null fields in the legacy address tables. Sarah and the backend team deployed a sanitization script on Thursday evening, which resolved 95% of the errors, but we still have 450 flagged accounts that require manual reconciliation by customer ops before Monday'\''s staging cutover.

On the frontend side, the new checkout redesign is 85% code-complete. The mobile responsive tests passed across iOS Safari and Android Chrome, but we observed a significant 1.2-second latency spike during Stripe payment intent creation on slower 3G connections. Mark is optimizing the API payload to shave off redundant metadata.

Regarding budget and vendor contracts: AWS spend was 18% higher than projected this month ($14,200 vs. $12,000 budget), largely driven by temporary staging clusters left running over the weekend. DevOps has now implemented auto-shutdown rules for non-production environments at 8:00 PM daily to prevent further overrun. Finally, QA sign-off is postponed from Friday to next Tuesday, which shifts our public beta launch target to October 24th.'
````
## PromptTemplate usage with st file
```
curl --location --request GET 'http://localhost:8080/file/chat?username=sovon' \
--header 'Content-Type: application/json' \
--data 'Project Aurora – Weekly Sprint Update & Operations Log

Team, here is the rundown for Sprint 14. We ran into a major blocker on Tuesday with the legacy database migration: roughly 12% of user profile records threw schema validation errors due to unexpected null fields in the legacy address tables. Sarah and the backend team deployed a sanitization script on Thursday evening, which resolved 95% of the errors, but we still have 450 flagged accounts that require manual reconciliation by customer ops before Monday'\''s staging cutover.

On the frontend side, the new checkout redesign is 85% code-complete. The mobile responsive tests passed across iOS Safari and Android Chrome, but we observed a significant 1.2-second latency spike during Stripe payment intent creation on slower 3G connections. Mark is optimizing the API payload to shave off redundant metadata.

Regarding budget and vendor contracts: AWS spend was 18% higher than projected this month ($14,200 vs. $12,000 budget), largely driven by temporary staging clusters left running over the weekend. DevOps has now implemented auto-shutdown rules for non-production environments at 8:00 PM daily to prevent further overrun. Finally, QA sign-off is postponed from Friday to next Tuesday, which shifts our public beta launch target to October 24th.'
```

## Prompt Stuffing
```
curl --location --request GET 'http://localhost:8080/promptStuffing/chat' \
--header 'Content-Type: application/json' \
--data 'I have used 2 earned leaves. will rest of the leaves will be carried forwared?'
```