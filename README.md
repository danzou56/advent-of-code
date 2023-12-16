# Advent of Code

My own solutions for Advent of Code, primarily written in Kotlin.

| Year | Incomplete | Notes                                   | 
|------|------------|-----------------------------------------|
| 2021 | None  🎉   | 24 - slow                               |         
| 2022 | 22/2, 25/2 | 16, 19 - slow, 17/2 - broken on example |
| 2023 | Ongoing    |                                         |         

## Automation Information

This repository contains code that automatically requests a user's input data if
* an `.env` file or appropriate environment is provided containing `AOC_TOKEN`, `AOC_GIT_URL`, and 
  `CONTACT_EMAIL`
* that input file does not already exist on the file system

This repository follows the [automation guidelines](https://www.reddit.com/r/adventofcode/wiki/faqs/automation)
on the [/r/adventofcode](https://www.reddit.com/r/adventofcode/) community wiki:
* Once inputs are downloaded, they are cached on the local filesystem and are not requested again
* A `User-Agent` header is always provided; if `AOC_GIT_URL` and `CONTACT_EMAIL` are not provided,
  the HTTP request is never performed
* Automated retrieval of statistics are not a part of this repository
* **Warning**: if multiple input files are missing, this repository has no guardrails to prevent 
  successive requests
