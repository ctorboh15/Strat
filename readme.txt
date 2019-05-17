Starting the application

1.) Please have at least java jdk8 running with in your env
2.) To start the application simply run the command `java -jar assignment-0.0.1-SNAPSHOT.jar` this will bring up the applications tomcat container on port 8080;


Using the application.

1.) Logging in
    To use this app you must first login and generate a JWT token will need to be passed in all subsequent requests.

    The login request URL takes a POST call with the user credentials being in the payload. If successful you should be return a token string to use with your requests.

    E.G `curl -X POST \
           http://localhost:8080/login \
           -H 'Content-Type: application/json' \
           -d '{
         	"first_name":"Ty",
         	"last_name":"Adams"
         }'`


2.) Searching for residents

To search for residents use the "/residence/people/search?first_name={{first_name_here}}&last_name={{last_name_here}}" url endpoint. This endpoint
is not case sensitive. See below example
E.G.

`curl -X GET \
  'http://localhost:8080/residence/people/search?first_name=Ty&last_name=Adams' \
  -H 'Accept: */*' \
  -H 'Authorization: Bearer {{Token}}' \
  -H 'Connection: keep-alive' \
  -H 'Content-Type: application/json' \`


3.) Getting Residents by unit

    To retrieve all residents within a unit use the "residence/unit/{unit}" url endpoint. This is a GET request that takes requires the unit to be in the URL path
    See Below Example

    `curl -X GET \
       http://localhost:8080/residence/unit/101 \
       -H 'Accept: */*' \
       -H 'Authorization: Bearer {{token}}' \
       -H 'Connection: keep-alive' \
       -H 'Content-Type: application/json' \`

 4.) Adding a resident to a unit

     To create a resident to a unit submit a POST request the "residence/unit/{unit}" url endpoint. This will create a new resident for the given unit. In the
     event that you want to make this resident an admin ... set the "isAdmin" flag to true. By default the application will default this flag to false.

     E.G.
     `curl -X POST \
        http://localhost:8080/residence/unit/101 \
        -H 'Accept: */*' \
        -H 'Authorization: Bearer {{token}}' \
        -H 'Connection: keep-alive' \
        -H 'Content-Type: application/json' \
        -d '{
      	"first_name":"Cemah",
      	"last_name":"Torboh",
      	"isAdmin":true
      }'`

 5.) Updating a resident

      To update resident to a unit submit a PUT request the "residence/unit/{unit}" url endpoint. This will update a resident for the given unit. In the
      event that you want to make this resident an admin ... set the "isAdmin" flag to true. For now this endpoint will only change the admin flag. Because
      of some design decisions to change a residents name ... it's recommended you first remove the resident and recreate them with the appropriate name change.

       E.G.
           `curl -X PUT \
              http://localhost:8080/residence/unit/101 \
              -H 'Accept: */*' \
              -H 'Authorization: Bearer {{token}}' \
              -H 'Connection: keep-alive' \
              -H 'Content-Type: application/json' \
              -d '{
            	"first_name":"Cemah",
            	"last_name":"Torboh",
            	"isAdmin":false
            }'`

 6.) Deleting a resident

       To remove resident to a unit submit a DELETE request the "residence/unit/{unit}" url endpoint.

        E.G.
            `curl -X DELETE \
               http://localhost:8080/residence/unit/101 \
               -H 'Accept: */*' \
               -H 'Authorization: Bearer {{token}}' \
               -H 'Connection: keep-alive' \
               -H 'Content-Type: application/json' \
               -d '{
             	"first_name":"Cemah",
             	"last_name":"Torboh"
             }'`

 7.) Getting all Residents

       To retrieve all residents submit a GET request to the "/residence/people" url. This will return you all residents in the property.
       Note this will only give you resident information ... not device information.
        E.G.
            `curl -X GET \
              'http://localhost:8080/residence/people' \
              -H 'Accept: */*' \
              -H 'Authorization: Bearer {{Token}}' \
              -H 'Connection: keep-alive' \
              -H 'Content-Type: application/json' \`


 ** Note **
 The jar is configured to generate a `property_data_changes.json` file when changes have been made in the application.
 The file will be generated in the same directory that the jar is running from.