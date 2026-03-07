# Spring AI Banking Sentinel

> **An Evaluated RAG + MCP Assistant for Modern Fintech**

This repository demonstrates a production-grade AI Agent built with **Spring AI**, 
using **MCP** (Model Context Protocol) for core banking actions and **Langfuse** for evaluating RAG faithfulness and tool-call accuracy.


```shell
curl -X POST "http://localhost:8080/2/chat" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userTier=Metal&accountId=ACC-1001&question=Can I dispute this €50 charge from 20 days ago?"


curl -X POST "http://localhost:8080/2/chat" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userTier=Metal&accountId=ACC-1001&question=I think I have a fraudulent charge"

curl -X POST "http://localhost:8080/2/chat" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userTier=Metal&accountId=ACC-1001&question=please help me"

curl -X POST "http://localhost:8080/2/chat" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userTier=Metal&accountId=ACC-1001&question=I do not have netflix"

curl -X POST "http://localhost:8080/2/chat" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userTier=Metal&accountId=ACC-1001&question=yes"
```