1. Reference: 
this project is forked from the origin Swagger Codegen project(https://github.com/swagger-api/swagger-codegen). This is such a powerful tool for REST API. It can direcetly generated Model/API for both client/server for different language. Please refer to the reference in Swagger Codegen Project to get related materials. So we did some customization based on it to satisfy our requirement. 

2. Our customization:
Microstrategy has one inner testing automation tool called xYati, which is coded with Java and only used in MicroStrategy. This tool works based on action(something like request template, coded with Java). We customized the Swagger Codegen to generate the xYati action Java files to support the testing for Microstrategy REST API. 

3. Future plan:
Currently it can already satisfy our requirement, so we will not update this project until we have further requirement.

Questions, please contact with:
lizhu@microstrategy.com
bizhu@microstrategy.com
