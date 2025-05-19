# Bank-solution

Bank solution API documentation 
A modular banking application that exposes RESTful APIs for managing Customers, Accounts, and Cards. Each module is independently hosted and documented via Swagger UI.
 Service-discovery is the Eureka, it runs on a default port. url: http://localhost:8761/eureka/
Technologies used
• Java 17+
• Spring Boot
• PostgreSQL 


Customer-Service-Application APIs
Swagger url:  http://localhost:8083/swagger-ui/index.html#/
POST: http://localhost:8083/customers

PUT: http://localhost:8083/customers?customerId=2

GET: http://localhost:8083/customers?customerId=CUS202500003

GET: http://localhost:8083/customers/search?q=Antony&startDate=2025-05-16&endDate=2025-05-17&size=10
DELETE: http://localhost:8083/customers?customerId=4

Note: For this API 
GET: http://localhost:8083/customers?customerId=CUS202500003
cudtomerId is a String e.g CUS202500003 and for the other APIs it a Long.

When passing date range on this API 
GET: http://localhost:8083/customers/search?q=Antony&startDate=2025-05-16&endDate=2025-05-17&size=10 manually trim the spaces before start date  and end date.




Account-Service APIs

Swagger url: http://localhost:8086/swagger-ui/index.html#

POST: http://localhost:8086/accounts

PUT: http://localhost:8086/accounts?accountId=2


GET: http://localhost:8086/accounts?accountId=%2011001250500002

GET: http://localhost:8086/accounts/search?page=0&size=10


DELETE: http://localhost:8086/accounts?accountId=1




Note: For this API 
GET: GET: http://localhost:8086/accounts?accountId=%2011001250500002
accountId is a String e.g 11001250500002 and for the other APIs it a Long.






Account-Service APIs

Swagger url: http://localhost:8085/swagger-ui/index.html#


POST: http://localhost:8085/cards


PUT: http://localhost:8085/cards?cardId=1

GET: http://localhost:8085/cards?cardId=1&showSensitive=false


GET: http://localhost:8085/cards/search?showSensitive=false&page=0&size=10


DELETE: http://localhost:8085/cards?cardId=2


Note: All the cardId is of type Long


