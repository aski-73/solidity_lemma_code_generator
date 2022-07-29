# LEMMA Models for the Park and Charge Platform (PACP) Case Study

This repository was created in the context of our paper "*Towards Holistic Modeling of Microservice Architectures: A Case Study and an Approach*"  for the [Second International Workshop on Model-Driven Engineering for Software Architecture (MDE4SA 2021)](http://mde4sa2021.disim.univaq.it/). It can be used for the replication of our results in Chapter 4 ("Modeling Case Study Microservices with LEMMA") of the paper. More specifically, the repository comprises the [LEMMA](https://github.com/SeelabFhdo/lemma) models for all PACP microservices concerning their design, implementation, and operation.

To use the models for replication purposes, you can install LEMMA locally as described in its documentation (cf. https://seelabfhdo.github.io/lemma-docs/getting-started/index.html). 


### Detailed Content Description
The repository contains the models of four PACP microservices as presented in the paper. Each microservice consists of one or more domain models, service models, and operation models expressed in the corresponding  viewpoint-specific LEMMA modeling languages. Furthermore, the repository provides the ["technology" folder](https://github.com/SeelabFhdo/mde4sa-2021/tree/master/technology) for the LEMMA technology models mentioned in the paper.

**Booking Management Microservice ([BMM](https://github.com/SeelabFhdo/mde4sa-2021/tree/master/Booking%20Management%20Microservice))**<br/>
Microservice that enables the management of park bookings as well as park and charge bookings. It provides appropriate API methods for booking creation and for booking data requests.

**Charging Station Management Microservice ([CSMM](https://github.com/SeelabFhdo/mde4sa-2021/tree/master/Charging%20Station%20Management%20Microservice))**<br/>
Microservice that provides the technical charging station management. This service can be used to create and provide new parking spaces as well as parking spaces with charging stations. It is also possible to request charging stations.

**Charging Station Search Microservice ([CSSM](https://github.com/SeelabFhdo/mde4sa-2021/tree/master/Charging%20Station%20Search%20Microservice))**<br/>
Microservice that allows citizens to search for available parking spaces and charging stations. 

**Environmental Data Analysis Microservice ([EDAM](https://github.com/SeelabFhdo/mde4sa-2021/tree/master/Environmental%20Data%20Analysis%20Microservice))**<br/>
Microservice that is responsible for the analysis of air quality indicators. For this purpose, the service provides functions for creating environment sensor units and for retrieving various sensor data.
