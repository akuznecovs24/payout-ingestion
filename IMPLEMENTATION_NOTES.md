# Getting Started

### 1. Project goal

Read payout files from different countries, check the data, and send every payout to a remote /payout API with delivery-guarantee (retry). 
The job runs once a day by scheduler or can be triggered by REST POST /api/payout/process

### 2. Main flow
* PayoutProcessor → finds every CountryPayoutSource
* CountryPayoutSource → fetch()-returns a yesterday file (if any)
* CountryPayoutParser → reads a fetched file
* PayoutRowMapper → validates and maps api mandatory fields
* PayoutSender → POST to remote API (with retry)

### 3. Why I used these building blocks

* Interfaces CountryPayoutSource, CountryPayoutParser
  * Easy to add a new country – make a new class that implements the interface and register it as Spring @Component. No other code must change.
* Java record PayoutRecord, SourcedPayout
  * Simple value holder, immutable, less code to write
* Early validation (PayoutRowMapper)
  * If any mandatory field is missing or wrong, the record is skipped before we reach the API. Saves network calls.
* WebClient instead of RestTemplate
  * modern, non-blocking API supported by Spring 6+
  * built-in retryWhen() operator
  * it will work fine later if we need higher throughput
* External config (application.properties)
  * payout.wakanda.folder, payout.api.url, payout.api.maxAttempts – can be changed with Spring Cloud Vault or plain env vars.
* Error handling	Any checked/unchecked problem is wrapped in ServiceErrorException:
  * Errors inside one file do not stop the whole job, log and continue.
  * Future idea: collect invalid records and send them back to the sender system.


### 4. Tests

* Unit tests (JUnit 5 + Mockito + AssertJ) for every small class:
  * PayoutRowMapperTest – validation logic
  * WakandaPayoutParserTest – CSV parsing
  * PayoutSenderTest – retry logic with mocked WebClient
  * PayoutProcessorTest – happy path and error branches
  * simple tests for configuration beans and controller
* Integration test PayoutIngestionTest (SpringBootTest + WireMock)
  * starts WireMock on random port → overrides payout.api.url
  * copies a real CSV sample to a temp folder → overrides payout.wakanda.folder
  * calls /api/payout/process on the running app
  * verifies that WireMock received the exact count of POST /payout requests.
