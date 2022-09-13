Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal


Scenario: Login Test
Meta:
@testlogin
@description Login & Logout

Given Open the Login Page
When User fill details and click Login
Then Verify Login is success
And Log out of application

