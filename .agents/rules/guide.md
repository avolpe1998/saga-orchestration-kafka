---
trigger: manual
---

create a new README.md file.

- it must be very clean 
- it must be very professional
- it must include all the essential information
- it must include:
   - explanation global of what this monorepo is:
      - it has 3 microservices, orders, inventory, service
      - the purpose of the project is use the SAGA pattern with orhcesttratior
      - the main support components:
      	- kafka for...
      	- wiremock for mocking the banck request, it will refuse the request of the payment service if request is more then 100 dollars
      - any other things I forgot to mention that is essential to know ...
   - how to run each microservice
   - prerequisites
   - exaplanation of order/service/script folder and its content:
   	- curl: explain here you can find the call to start orders, check them and so on from terminal
   	- sql: you find a query in sql to insert a product directly on db using a db tool (I don't know the true name) like dbeaver or the script to run on terminal to execue the query directly within db
   - explanation of docker folder, dockecompose
   
- this documentation must be complete, probably I forgot to mention some important configuration/component you check everything for me
- is super important that everything is well organized, clear, and minimal, shouldn't be a mess bomb to read
