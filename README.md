# Spring AI Banking Sentinel

> **An Evaluated RAG + MCP Assistant for Modern Fintech**

This repository demonstrates a production-grade AI Agent built with **Spring AI**,
using tool calling for core banking actions and **Langfuse** for observability and evaluation of RAG faithfulness and tool-call accuracy.

## Stack

- **Spring Boot 4.x** + **Spring AI 2.0.0-M2**
- **AWS Bedrock** (Converse API) as the LLM provider
- **PGVector** as the vector store for RAG
- **Langfuse** for tracing and evaluation via OpenTelemetry (OTLP)

## Running the vector store

Start PGVector for the RAG vector store:

```bash
docker compose up -d
```

This starts a PostgreSQL instance with the `pgvector` extension on port `5432`.

To stop it:

```bash
docker compose down
```

## Running Langfuse locally

The `docker-compose-langfuse.yml` is based on the [official Langfuse docker-compose.yml](https://github.com/langfuse/langfuse/blob/main/docker-compose.yml) with two modifications:
- All ports except `3000` (the main UI/API) are commented out to avoid collisions with other local containers
- `LANGFUSE_INIT_*` environment variables are set to auto-provision an organization, project, and user on first startup

Start the Langfuse stack (includes PostgreSQL, ClickHouse, Redis, MinIO):

```bash
docker compose -f docker-compose-langfuse.yml up -d
```

This auto-provisions:
- **Organization**: `rogervinas-bank`
- **Project**: `banking-sentinel`
- **API keys**: `publickey-local` / `secretkey-local`
- **User**: `admin@local.dev` / `password`

Langfuse UI is available at http://localhost:3000.

To stop it:

```bash
docker compose -f docker-compose-langfuse.yml down
```

To start from scratch (removes all data including traces, users, and projects):

```bash
docker compose -f docker-compose-langfuse.yml down -v
```

## Running the application

The `application.yml` is pre-configured to send traces to the local Langfuse instance using the auto-provisioned API keys.

You need to configure AWS Bedrock credentials and models via environment variables or `system.properties`:

```
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=eu-central-1
AWS_BEDROCK_CHAT_MODEL=...
AWS_BEDROCK_EMBEDDING_MODEL=...
```

Then run:

```bash
./gradlew bootRun
```

## Spring AI + Spring Boot 4.x observability workarounds

Spring AI 2.0.0-M2 observation handlers (`ChatModelPromptContentObservationHandler`, `ChatModelCompletionObservationHandler`, etc.) only log to SLF4J instead of adding key values to the observation context, which means the data doesn't reach OTel-based backends like Langfuse.

Additionally, some auto-configurations depend on a Micrometer `Tracer` bean that Spring Boot 4.x no longer provides (it was part of `spring-boot-actuator-autoconfigure` in Spring Boot 3.x).

This project provides custom `ObservationFilter` implementations in `com.rogervinas.bank.observation` that add the data as high-cardinality key values using [OTel semantic conventions for GenAI](https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/):

| Filter | Replaces | OTel attributes |
|---|---|---|
| `ChatModelObservationFilter` | `ChatModelPromptContentObservationHandler` + `ChatModelCompletionObservationHandler` | `gen_ai.input.messages`, `gen_ai.output.messages`, `gen_ai.tool.definitions`, `gen_ai.response.model` (fix) |
| `ChatClientObservationFilter` | `ChatClientPromptContentObservationHandler` + `ChatClientCompletionObservationHandler` | `gen_ai.input.messages`, `gen_ai.output.messages` |
| `ImageModelObservationFilter` | `ImageModelPromptContentObservationHandler` | `gen_ai.input.messages` |
| `VectorStoreObservationFilter` | `VectorStoreQueryResponseObservationHandler` | `gen_ai.retrieval.documents` |
| `ToolCallingObservationFilter` | `ToolCallingContentObservationFilter` | `gen_ai.tool.name`, `gen_ai.tool.description`, `gen_ai.tool.call.arguments`, `gen_ai.tool.call.result` |
