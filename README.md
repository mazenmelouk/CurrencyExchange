# Currency Exchange 

![build badge](https://github.com/mazenmelouk/CurrencyExchange/actions/workflows/maven.yml/badge.svg?branch=main)

A simple project for teaching/learning purposes to build a simple REST API serving converting currencies for its users.

## Table of Contents

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Once a user issues a query to convert from one currency to another, the application interfaces with an external provider
https://v6.exchangerate-api.com/ to fetch the exchange data and uses a database to store the fetched data.
It utilizes Spring Boot, Maven, Postgres and Java to achieve its goals.

## Technologies

- Java 17 (Recommended 21)
- Spring Boot
- Maven
- Postgres

## Getting Started

Assuming you have git setup
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Git, to be able to clone and contribute to the project https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
- Java 17 or later, installed on your machine
  - Check SDKMAN https://sdkman.io/ :wink:
- Maven https://maven.apache.org/install.html
- Postgres, ideally via Docker https://hub.docker.com/_/postgres

### Installation

1. Clone the repository:
```shell
git clone git@github.com:mazenmelouk/CurrencyExchange.git
```

2. Navigate to the project directory:
```shell
cd CurrencyExchange
```

3. Build the project using Maven:
```shell
mvn clean install
```

## Contributing

If you'd like to contribute to this project, please follow these guidelines:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature-branch-name`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Create a new Pull Request

## License

This project is licensed under the [GNU General Public License] - see the [LICENSE.md](LICENSE.md) file for details.

