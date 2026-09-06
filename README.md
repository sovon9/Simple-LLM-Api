# Simple-LLM-Api
simple LLM api method testing

## Chat using RAG
```
curl --location --request GET 'http://localhost:8080/rag/chat' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data 'what are the perks of this company?'
```

## Chat using Document RAG
```
curl --location --request GET 'http://localhost:8080/rag/doc/manual/chat' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data ' how can I apply for a leave?'
```

## chat using advisor
```
curl --location --request GET 'http://localhost:8080/rag/doc/advisor/chat' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data ' how can I apply for a leave?'
```

## chat using web search
```
curl --location --request GET 'http://localhost:8080/rag/websearch/chat' \
--header 'Content-Type: application/json' \
--header 'username: Sovon' \
--data 'how is the Indian stock market on 4th September?'
```