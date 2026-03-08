# Spring AI Banking Sentinel

> **An Evaluated RAG + MCP Assistant for Modern Fintech**

This repository demonstrates a production-grade AI Agent built with **Spring AI**, 
using **MCP** (Model Context Protocol) for core banking actions and **Langfuse** for evaluating RAG faithfulness and tool-call accuracy.


Langfuse docker compose in https://github.com/langfuse/langfuse/blob/main/docker-compose.yml

docker compose -f docker-compose-langfuse.yml up -d


LANGFUSE_SECRET_KEY="sk-lf-b3441325-b3e9-4d2e-8451-eac50ebe1a4d"
LANGFUSE_PUBLIC_KEY="pk-lf-b82aec5c-74bf-4169-b89a-4cb5708310fc"

export MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_HEADERS_AUTHORIZATION="Basic $(echo -n "$LANGFUSE_PUBLIC_KEY:$LANGFUSE_SECRET_KEY" | base64)"



https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/