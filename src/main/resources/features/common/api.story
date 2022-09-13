Meta:
@api

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal


Scenario: API - GET Test
Meta:
@api1
@description GET Test

Given Generate the GET uri and parameters for 'reqresapi.uri' endpoint
When I send a GET request
Then I validate the response with '200'

Scenario: API - POST Test
Meta:
@api2
@description POST Test

Given Generate the POST uri and payload for 'reqresapi.uri' endpoint
When I send a POST request
Then I validate the response with '201'

Scenario: API - PUT Test
Meta:
@api3
@description PUT Test

Given Generate the PUT uri and payload for 'reqresapi.uri' endpoint
When I send a PUT request
Then I validate the response with '200'

Scenario: API - PATCH Test
Meta:
@api4
@description PATCH Test

Given Generate the PATCH uri and payload for 'reqresapi.uri' endpoint
When I send a PATCH request
Then I validate the response with '200'

Scenario: API - DELETE Test
Meta:
@api5
@description DELETE Test

Given Generate the DELETE uri and parameters for 'reqresapi.uri' endpoint
When I send a DELETE request
Then I validate the response with '204'
