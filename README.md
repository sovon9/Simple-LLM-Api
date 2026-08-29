# Simple-LLM-Api
simple LLM api method testing

## Chat Memory
```
curl --location --request GET 'http://localhost:8080/chat-memory' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data 'Hi, my name is Sovon.'
```
## Custom chat memory with max messages=10
````
curl --location --request GET 'http://localhost:8080/chat-memory' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data 'what is  my name?'
````