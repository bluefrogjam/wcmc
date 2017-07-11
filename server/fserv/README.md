# README #

This project provides a simple REST based file server. To upload and share files.

## What is this repository for? ##

We required a simple service, to distribute data and files across several platforms.

## Using this service

The recommended way to use this service, is by utilizing the build docker image

docker run -p 8080:8080 -v /localStrorage:/storage eros.fiehnlab.ucdavis.edu/wcmc-fserv:latest

## Exposed endpoints

currently this service exposes the following endpoints:

* GET /rest/download/fileName
* POST /rest/upload as MultiPartFile, with the name 'file' as attribute
* GET /rest/exists/fileName

## Clients

This service has been tested using CURL and the dedicated fserv4j module, which is located under /rest/api/fserv4j